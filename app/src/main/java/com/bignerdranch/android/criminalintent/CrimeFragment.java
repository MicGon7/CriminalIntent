package com.bignerdranch.android.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.text.format.DateFormat;

import java.util.Date;
import java.util.UUID;

/**
 * Created by michaelgonzalez on 2/25/18.
 */


public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mSuspectButton;
    private Button mReportButton;


    /* CrimeActivity will call this method when it needs to create a CrimeFragment, passing
       in the UUID it retrieved from its extra.
    */
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Getting crime ID from the arguments
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        setHasOptionsMenu(true);

    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This space is intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This one is also left blank
            }
        });

        mDateButton = v.findViewById(R.id.button_crimeDate);
        updateDate();

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                // Open communication between fragments.
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });


        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = v.findViewById(R.id.button_crimeReport);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an implicit intent and pass it into startActivity(Intent).

                // MANUAL WAY
               // Intent i = new Intent(Intent.ACTION_SEND); // 1: Action
//                i.setType("text/plain"); // 3: Data tpe.
//                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
//                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Crime_report_subject));
//                i = Intent.createChooser(i, getString(R.string.send_report));

                // RECOMMENDED WAY
                Intent i =  ShareCompat.IntentBuilder.from(getActivity())
                        .setChooserTitle(getString(R.string.send_report))
                        .setSubject(getString(R.string.Crime_report_subject))
                        .setText(getCrimeReport())
                        .setType("text/plain")
                        .getIntent();

               i = Intent.createChooser(i, getString(R.string.send_report));


                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);

        mSuspectButton = v.findViewById(R.id.button_crimeSuspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);

            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        // Avoid crash if contact app does not exist on device.
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        return v;

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.remove_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);

                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Retrieve the extra from DatePicker and set the date on the Crime and refresh
    // the text of the date button.

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();

        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where" clause here.
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields,
                            null,
                            null,
                            null);

            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }

                // Pull out the first column of the first row of data that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        }

    }

    private void updateDate() {
        mDateButton.setText((mCrime.getDate().toString()));
    }

    // Creates four strings and then pieces them together and returns a complete report.
    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd, yyyy";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            //TODO Fix suspect value not showing up.
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }
}
