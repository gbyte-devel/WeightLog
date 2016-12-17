package co.gbyte.weightlog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import co.gbyte.weightlog.model.Weight;
import co.gbyte.weightlog.model.WeightLab;

import static co.gbyte.weightlog.R.string.height_pref_key;

/**
 * Created by walt on 19/10/16.
 *
 */
public class LogFragment extends Fragment {

    private RecyclerView mWeightRecycleView;

    private WeightAdapter mAdapter;
    private SharedPreferences mUserPrefs;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weight_list, container, false);
        mWeightRecycleView = (RecyclerView) view.findViewById(R.id.weight_recycler_view);
        mWeightRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mUserPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(mUserPrefs.contains(getString(height_pref_key))) {
            Toast.makeText(getActivity(), "Height has been set up", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "No height yet", Toast.LENGTH_LONG).show();
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_log_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                intent = new Intent(this.getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_item_new_weight:
                intent = WeightActivity.newIntent(getActivity());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateUI() {
        WeightLab weightLab = WeightLab.get(getActivity());
        List<Weight> weights = weightLab.getWeights();

        if (mAdapter == null) {
            mAdapter = new WeightAdapter(weights);
            mWeightRecycleView.setAdapter(mAdapter);
        } else {
            mAdapter.setWeights(weights);
            mAdapter.notifyDataSetChanged();
            // ToDo: Don't forget to switch to:
            //mAdapter.notifyItemChanged(<position>);
            //
            // Note:
            // The challenge is discovering which position has
            // changed and reloading the correct item.
        }
    }

    private class WeightHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Weight mWeight;

        TextView mDateTextView;
        TextView mTimeTextView;
        TextView mWeightTextView;
        TextView mWeightChangeTextView;

        WeightHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_weight_date_text_view);
            mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_weight_time_text_view);
            mWeightTextView =
                    (TextView) itemView.findViewById(R.id.list_item_weight_weight_text_view);
            mWeightChangeTextView =
                    (TextView) itemView.findViewById(R.id.list_item_weight_change_text_view);
        }

        void bindWeight(Weight weight, Double weightChange) {
            mWeight = weight;

            mDateTextView.setText(DateFormat.getDateFormat(getActivity())
                                  .format(mWeight.getTime()));
            mTimeTextView.setText(DateFormat.getTimeFormat(getActivity())
                                  .format(mWeight.getTime()));
            mWeightTextView.setText(mWeight.getWeightStringKg());
            if (weightChange != null) {
                mWeightChangeTextView.setText(weightChange.toString());
                if (weightChange < 0) {
                    mWeightChangeTextView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.colorWeightLoss));
                    mWeightTextView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.colorWeightLossDark));
                } else if (weightChange > 0) {
                    mWeightChangeTextView.setText("+" + weightChange.toString());
                    mWeightChangeTextView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.colorWeightGain));
                    mWeightTextView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.colorWeightGainDark));
                } else {
                    mWeightChangeTextView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.colorSecondaryText));
                    mWeightTextView.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.colorSecondaryText));
                }


            } else {
                mWeightChangeTextView.setText("");
                mWeightTextView.setTextColor(ContextCompat.getColor(getContext(),
                        R.color.colorPrimaryText));
            }
        }

        @Override
        public void onClick(View view) {
            Intent intent = WeightActivity.newIntent(getActivity(), mWeight.getId());
            startActivity(intent);
        }
    }

    private class WeightAdapter extends RecyclerView.Adapter<WeightHolder> {
        private List<Weight> mWeights;

        WeightAdapter(List<Weight> weights) {
            mWeights = weights;
        }

        @Override
        public WeightHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_weight, parent, false);
            return new WeightHolder(view);
        }

        @Override
        public void onBindViewHolder(WeightHolder holder, int position) {
            Weight weight = mWeights.get(position);
            Double difference;
            if (position < mWeights.size() - 1) {
                Weight prevWeight = mWeights.get(position + 1);
                difference =  (double) (weight.getWeight() - prevWeight.getWeight()) / 1000.0;
            } else {
                difference = null;
            }

            holder.bindWeight(weight, difference);
        }

        @Override
        public int getItemCount() {
            return mWeights.size();
        }

        void setWeights(List<Weight> weights) {
            mWeights = weights;
        }
    }
}
