package com.bignerdranch.android.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by michaelgonzalez on 2/27/18.
 */

public class CrimeListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
