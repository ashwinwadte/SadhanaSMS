package com.ashwin.sadhanasms;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLoginFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private static final String BUNDLE_KEY = "BUNDLE_KEY";

    static View rootView;
    static TextView tvSleepTime;
    static String hrs = "";
    static String mins = "";
    EditText etName, etCounselorName, etContact;
    Button bSave;
    String name;
    String counselorName;
    String contact;
    String sleepTime;
    DbHelper db;
    Cursor cursor;

    private OnLoginFragmentInteractionListener mListener;
    private Bundle args;

    public LoginFragment() {
        // Required empty public constructor
    }

    //convert time to 12hr format
    public static String getUpdatedTime(int hrs, int mins) {

        String am_pa;
        String time;

        if (hrs == 0) {
            hrs += 12;
            am_pa = "AM";
        } else if (hrs == 12) {
            am_pa = "PM";
        } else if (hrs > 12) {
            hrs -= 12;
            am_pa = "PM";
        } else
            am_pa = "AM";

        if (mins == 0)
            time = hrs + am_pa;
        else
            time = hrs + ":" + pad(mins) + am_pa;

        return time;
    }

    static String pad(int i) {

        if (i >= 10)
            return String.valueOf(i);

        return "0" + String.valueOf(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        args = getArguments();

        etName = (EditText) rootView.findViewById(R.id.etName);
        etCounselorName = (EditText) rootView.findViewById(R.id.etCounselorName);
        etContact = (EditText) rootView.findViewById(R.id.etContact);

        bSave = (Button) rootView.findViewById(R.id.bSave);
        bSave.setOnClickListener(this);

        tvSleepTime = (TextView) rootView.findViewById(R.id.tvSleepTime);
        tvSleepTime.setOnClickListener(this);

        db = new DbHelper(getContext());
        cursor = db.getInfo();

        if (!args.getBoolean(BUNDLE_KEY))
            previousRecord();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.bSave:
                try {
                    name = etName.getText().toString();
                    counselorName = etCounselorName.getText().toString();
                    contact = etContact.getText().toString();


                    if (args.getBoolean(BUNDLE_KEY)) {
                        sleepTime = hrs + ":" + mins;
                        db.insert(name, counselorName, contact, sleepTime);
                    } else {
                        if (!hrs.equals("") && !mins.equals(""))
                            sleepTime = hrs + ":" + mins;
                        else
                            sleepTime = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TIME));

                        db.update(1, name, counselorName, contact, sleepTime);
                    }

                    mListener.onLoginFragmentInteraction();

                    Snackbar.make(v, "Info saved. You can edit info in settings", Snackbar.LENGTH_LONG).show();

                } catch (Exception e) {
                    if (etName.getText().toString().equals("")) {
                        etName.setHint("Your name required.");
                        etName.setHintTextColor(Color.RED);
                    }

                    if (etCounselorName.getText().toString().equals("")) {
                        etCounselorName.setHint("Counselor name required.");
                        etCounselorName.setHintTextColor(Color.RED);
                    }

                    if (etContact.getText().toString().equals("")) {
                        etContact.setHint("Mob No. required.");
                        etContact.setHintTextColor(Color.RED);
                    }

                    if (tvSleepTime.getText().toString().equals("")) {
                        tvSleepTime.setHint("Everyday sleeping time required.");
                        tvSleepTime.setHintTextColor(Color.RED);
                    }
                }
                break;

            case R.id.tvSleepTime:
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.show(getActivity().getFragmentManager(), "timePicker");

                break;
        }
    }

    private void previousRecord() {
        Cursor cursor = db.getInfo();

        name = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_NAME));
        counselorName = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_COUNSELOR));
        contact = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CONTACT_NO));

        contact = contact.replace("+91", "");

        sleepTime = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TIME));
        String[] time = sleepTime.split(":");

        int hrs = Integer.parseInt(time[0]);
        int mins = Integer.parseInt(time[1]);

        etName.setText(name);
        etCounselorName.setText(counselorName);
        etContact.setText(contact);
        tvSleepTime.setText(getUpdatedTime(hrs, mins));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoginFragmentInteractionListener {
        void onLoginFragmentInteraction();
    }

    //TimePicker Fragment
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, 21, 30, false);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            tvSleepTime.setText(getUpdatedTime(hourOfDay, minute));
            hrs = String.valueOf(hourOfDay);
            mins = String.valueOf(minute);
        }
    }
}
