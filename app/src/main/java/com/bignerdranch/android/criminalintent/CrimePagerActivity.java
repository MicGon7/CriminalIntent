package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

/**
 * Created by michaelgonzalez on 3/6/18.
 */


public class CrimePagerActivity extends AppCompatActivity {
    private static final String TAG = "CriminalIntent";
    private static final String EXTRA_CRIME_ID =
            "com.bignerdranch.android.criminalintent.crime_id";

    private ViewPager mViewPager;
    private List<Crime> mCrimes;
    private Button mFirstCrimeButton;
    private Button mLastCrimeButton;
    private int currentItemPosition;


    public static Intent newIntent(Context packageContext, UUID crimeId) {
        Intent intent = new Intent(packageContext, CrimePagerActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        UUID crimeId = (UUID) getIntent().getSerializableExtra((EXTRA_CRIME_ID));

        mViewPager = findViewById(R.id.crime_view_pager);
        mCrimes = CrimeLab.get(this).getCrimes();
        mFirstCrimeButton = findViewById(R.id.button_firstCrime);
        mLastCrimeButton = findViewById(R.id.button_lastCrime);
        FragmentManager fragmentManager = getSupportFragmentManager();

        // FragmentStatePagerAdapter destroys the unneeded fragment.
        // State refers to the fact that it will save out your fragment's Bundle
        // from the onSaveInstanceState(Bundle) when it's destroyed.
        // The new fragment will be restored using that instance state when the user
        // navigates back. (memory frugal with memory)

        // Fragment Pager Adapter never destroys the fragment but calls detach(Fragment) instead
        // of remove(Fragment) call used by the FragmentStatePagerAdapter. So, the fragment instance
        // remains alive in the FragmentManager. (Good for a small interface that has a fixed
        // number of fragments - keep fragments in memory can make your controller code
        // easier to manage)

        handleButtonAccess();


        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {

            // getItem only gets called when new fragment is created. x [] [] [] x (x = not created)

            @Override
            public Fragment getItem(int position) {
                Log.d(TAG, "New Fragment Loaded @ " + position);
                Crime crime = mCrimes.get(position);
                currentItemPosition = position;


                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected Called." + mViewPager.getCurrentItem());
                currentItemPosition = position;

                handleButtonAccess();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        mFirstCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(0);
                mFirstCrimeButton.setEnabled(false);
                mLastCrimeButton.setEnabled(true);

            }
        });

        mLastCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Last Button Clicked!");
                mViewPager.setCurrentItem(mCrimes.size() - 1);
                mLastCrimeButton.setEnabled(false);
                mFirstCrimeButton.setEnabled(true);

            }
        });

        // Setting the initial pager item.
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(crimeId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }

    }

    private void handleButtonAccess() {
        if(currentItemPosition  != 0 || currentItemPosition != 100) {
            mFirstCrimeButton.setEnabled(true);
            mLastCrimeButton.setEnabled(true);
        }
        if (currentItemPosition == 0) {
            mFirstCrimeButton.setEnabled(false);
        } else if (currentItemPosition == 100) {
            mLastCrimeButton.setEnabled(false);
        }
    }
}
