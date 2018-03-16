package com.bignerdranch.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by michaelgonzalez on 2/27/18.
 */

public class CrimeLab {
    // Singleton
    private static CrimeLab sCrimeLab;


    private List<Crime> mCrimes;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    // Load list with temporary crime objects.
    private CrimeLab(Context context) {
        mCrimes = new ArrayList<>();

    }

    public void addCrime(Crime c) {
        mCrimes.add(c);
    }

    public void removeCrime(Crime c) {
        mCrimes.remove(c);
    }

    public List<Crime> getCrimes() {

        return mCrimes;
    }

    // Inefficient - Challenge 9.2: Improve the performance of the lookup.
    public Crime getCrime(UUID id) {
        for(Crime crime : mCrimes) {
            if(crime.getId().equals(id)){
                return crime;
            }
        }
        return null;
    }

}
