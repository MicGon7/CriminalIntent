package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mAdapter;
    private int itemPosition;
    private boolean mSubtitleVisible;
    private TextView mNoCrimesTextView;
    private ImageButton mNoCrimesAddButton;
    private CrimeLab mCrimeLab;


    //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Explicitly tell the FragmentManager that your fragment should receive a call to
        // onCreateOptionsMenu.
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeLab = CrimeLab.get(getActivity());
        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);

        // LayoutManager is required or Recycler view will crash.
        // This will get the current activity which should be CrimeListActivity.
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNoCrimesTextView = view.findViewById(R.id.textview_noCrimes);
        mNoCrimesAddButton = view.findViewById(R.id.add_button);
        mNoCrimesAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    // Reloading the list in onResume()
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);

        subtitleItem.setTitle(mSubtitleVisible ? R.string.hide_subtitle : R.string.show_subtitle);

    }

    // Respond to selection of the MenuItem by creating a new Crime, adding it to CrimeLab,
    // and then starting an instance of CrimePagerActivity to edit the new Crime.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSubtitle() {
        int crimeSize = mCrimeLab.getCrimes().size();
        String subtitle = getResources().
                getQuantityString(R.plurals.subtitle_plural, crimeSize, crimeSize);


        if (!mSubtitleVisible) {
            subtitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    // Create adapter and pass in list of crimes.
    private void updateUI() {
        List<Crime> crimes = mCrimeLab.getCrimes();

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
        handleNoCrimesVisibility();


        updateSubtitle();
    }

    private void handleNoCrimesVisibility() {
        mNoCrimesTextView.setVisibility(View.VISIBLE);
        mNoCrimesAddButton.setVisibility(View.VISIBLE);

        if (!mCrimeLab.getCrimes().isEmpty()) {
            mNoCrimesTextView.setVisibility(View.INVISIBLE);
            mNoCrimesAddButton.setVisibility(View.INVISIBLE);

            return;
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
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE MM/dd/yyyy h:mm a");

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save subtitle visibility state across rotation.
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }
}
