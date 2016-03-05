package com.ashwin.sadhanasms;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener, Dialog.OnClickListener {
    private static final String BUNDLE_KEY = "BUNDLE_KEY";

    static View rootView;
    // Use the current date as the default date in the picker
    final Calendar c = Calendar.getInstance();
    TextView WU, DR, TB, Japa, Hearing, Reading, Study, ServiceTime;
    EditText ServiceType, SentBy;
    DbHelper db;
    Cursor cursor;
    DialogFragment newFragment;
    Bundle args;
    OnMainActivityFragmentInteractionListener listener;
    int hrs = c.get(Calendar.HOUR_OF_DAY);
    int min = c.get(Calendar.MINUTE);

    PendingIntent pendingIntent;

    int[] time = new int[2];

    public MainActivityFragment() {
    }

    //convert time to 12hr format
    public static void updateTimeAndSetView(int hrs, int mins, int id) {

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

        ((TextView) rootView.findViewById(id)).setText(time);
    }

    //just set view without updating time
    public static void updateTimeAndSetView2(int hrs, int mins, int id) {
        String time = "";

        if (hrs == 0 && mins == 0) {
            time = String.valueOf(0);
        } else if (hrs == 0 && mins > 0) {
            time = mins + "min";
        } else if (hrs > 0 && mins == 0) {
            time = hrs + "hrs";
        } else if (hrs > 0 && mins > 0) {
            time = hrs + ":" + mins + "hrs";
        }

        ((TextView) rootView.findViewById(id)).setText(time);
    }

    static String pad(int i) {

        if (i >= 10)
            return String.valueOf(i);

        return "0" + String.valueOf(i);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        setHasOptionsMenu(true);

        db = new DbHelper(getContext());
        cursor = db.getInfo();

        initWidgets();

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage("Do you want to really send?").setPositiveButton("Send", MainActivityFragment.this).setNegativeButton("Cancel", null);

                AlertDialog sendDialog = builder.create();
                sendDialog.show();

            }
        });

        //schedule notification
        String sleepTime = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_TIME));
        String[] time = sleepTime.split(":");

        int hrs = Integer.parseInt(time[0]);
        int min = Integer.parseInt(time[1]);

        scheduleNotification(getNotification("Please click here to send SMS and sleep"), hrs, min);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.findItem(R.id.action_profile).setVisible(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnMainActivityFragmentInteractionListener) {
            listener = (OnMainActivityFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMainactivityFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    //Notification
    private void scheduleNotification(Notification notification, int hrs, int min) {

        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);

        pendingIntent = PendingIntent.getBroadcast(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hrs);
        calendar.set(Calendar.MINUTE, min);

        //check if time is earlier than current time
        Calendar now = Calendar.getInstance();
        if (calendar.before(now)) {
            calendar.add(Calendar.DATE, 1);
        }

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private Notification getNotification(String content) {
        Intent intent1 = new Intent(getContext(), MainActivity.class);
        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(getContext());

        builder.setContentTitle("Hare Krishna");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.ic_launcher_small);
        builder.setContentIntent(pendingNotificationIntent);

        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return builder.build();
        } else
            return builder.getNotification();
    }


    void initWidgets() {
        WU = (TextView) rootView.findViewById(R.id.tvWakeUp);
        DR = (TextView) rootView.findViewById(R.id.tvDayRest);
        TB = (TextView) rootView.findViewById(R.id.tvToBed);
        Japa = (TextView) rootView.findViewById(R.id.tvJapa);
        Hearing = (TextView) rootView.findViewById(R.id.tvHearing);
        Reading = (TextView) rootView.findViewById(R.id.tvReading);
        Study = (TextView) rootView.findViewById(R.id.tvStudy);
        ServiceTime = (TextView) rootView.findViewById(R.id.tvServiceTime);
        ServiceType = (EditText) rootView.findViewById(R.id.etServiceType);
        SentBy = (EditText) rootView.findViewById(R.id.etSentBy);

        WU.setOnClickListener(this);
        DR.setOnClickListener(this);
        TB.setOnClickListener(this);
        Japa.setOnClickListener(this);
        Hearing.setOnClickListener(this);
        Reading.setOnClickListener(this);
        Study.setOnClickListener(this);
        ServiceTime.setOnClickListener(this);

        SentBy.setText(cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_NAME)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.tvWakeUp:
                newFragment = new TimePickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvWakeUp);

                time[0] = 3;
                time[1] = 45;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "timePicker");

                break;

            case R.id.tvDayRest:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvDayRest);

                time[0] = 0;
                time[1] = 30;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "durationPicker");
                break;

            case R.id.tvToBed:
                newFragment = new TimePickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvToBed);

                time[0] = hrs;
                time[1] = min;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "timePicker");
                break;

            case R.id.tvJapa:
                newFragment = new TimePickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvJapa);

                time[0] = 6;
                time[1] = 30;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "timePicker");
                break;

            case R.id.tvHearing:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvHearing);

                time[0] = 0;
                time[1] = 30;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "durationPicker");
                break;

            case R.id.tvReading:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvReading);

                time[0] = 0;
                time[1] = 30;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "durationPicker");
                break;

            case R.id.tvStudy:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvStudy);

                time[0] = 2;
                time[1] = 0;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "durationPicker");
                break;

            case R.id.tvServiceTime:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvServiceTime);

                time[0] = 0;
                time[1] = 30;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "durationPicker");
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        String contact = cursor.getString(cursor.getColumnIndex(DbHelper.COLUMN_CONTACT_NO));

        String textBody = "HK Pr. Pamho" +
                "\nWU:" + WU.getText().toString() +
                "\nDR:" + DR.getText().toString() +
                "\nTB:" + TB.getText().toString() +
                "\nJapa:" + Japa.getText().toString() +
                "\nHearing:" + Hearing.getText().toString() +
                "\nReading:" + Reading.getText().toString() +
                "\nStudy:" + Study.getText().toString() +
                "\nService:" + ServiceTime.getText().toString() + "(" + ServiceType.getText().toString() + ")" +
                "\nYS " + SentBy.getText().toString();


        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contact, null, textBody, null, null);

            Toast.makeText(getContext(), "SMS sent\nHari Bol!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Hari Hari!!\nSMS failed to send :(", Toast.LENGTH_SHORT).show();
        }

    }

    public interface OnMainActivityFragmentInteractionListener {
        void onMainActivityFragmentInteraction();
    }

    //TimePicker Fragment
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        int hrs;
        int min;
        Bundle args;
        int[] time = new int[2];

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            args = getArguments();
            time = args.getIntArray(BUNDLE_KEY);
            hrs = time[0];
            min = time[1];


            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hrs, min, false);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Bundle args = getArguments();

            updateTimeAndSetView(hourOfDay, minute, args.getInt("id"));
        }
    }


    //DurationPicker Fragment
    public static class DurationPickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        int hrs;
        int min;
        Bundle args;
        int[] time;


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            args = getArguments();
            time = args.getIntArray(BUNDLE_KEY);
            hrs = time[0];
            min = time[1];

            // Create a new instance of DatePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hrs, min, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Bundle args = getArguments();

            updateTimeAndSetView2(hourOfDay, minute, args.getInt("id"));
        }
    }
}
