package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by michaelgonzalez on 2/27/18.
 */

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int itemPosition;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);

        // LayoutManager is required or Recycler view will crash.
        // This will get the current activity which should be CrimeListActivity.
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        updateUI();

        return view;
    }

    // Reloading the list in onResume()
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    // Create adapter and pass in list of crimes.
    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();


        // call notifyDataSetChange() if the CrimeAdapter is already set up.
        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {

            // inefficient - reloads all items.
            // mAdapter.notifyDataSetChanged();

            // efficient - reloads a single item.
            mAdapter.notifyItemChanged(itemPosition);
        }
    }

    // ViewHolder - holds on to a view - similar to UMG/VRFAT recipe rows.
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.crime_solved);


        }

        //Called in OnBindViewHolder
        private void bind(Crime crime) {
            mCrime = crime;

            // Challenge 9: Format date.
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM, dd, yyyy");

            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(sdf.format(mCrime.getDate()));
            mSolvedImageView.setVisibility(crime.isSolved() ? View.VISIBLE : View.GONE);
        }

        // Stashing and passing a Crime id to CrimeActivity

        @Override
        public void onClick(View view) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());

            itemPosition = getAdapterPosition();


            startActivity(intent);
        }


    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new CrimeHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }


        // This will get me the index of the current item in Crimes.
        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }


    }
}
