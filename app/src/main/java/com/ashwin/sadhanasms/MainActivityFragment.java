package com.ashwin.sadhanasms;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
    TextView WU, DR, TB, JAPA, HR, RE, IS, PS;
    EditText SentBy;
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
            am_pa = "am";
        } else if (hrs == 12) {
            am_pa = "pm";
        } else if (hrs > 12) {
            hrs -= 12;
            am_pa = "pm";
        } else
            am_pa = "am";

        if (mins == 0)
            time = hrs + am_pa;
        else
            time = hrs + "." + pad(mins) + am_pa;

        ((TextView) rootView.findViewById(id)).setText(time);
    }

    //just set view without updating time
    public static void updateTimeAndSetView2(int hrs, int mins, int id) {
        String time = "";

        if (hrs == 0 && mins == 0) {
            time = String.valueOf(0);
        } else if (hrs == 0 && mins > 0) {
            time = mins + "m";
        } else if (hrs > 0 && mins == 0) {
            time = hrs + "h";
        } else if (hrs > 0 && mins > 0) {
            time = hrs + "h" + mins + "m";
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

                builder.setMessage("Are you sure?").setPositiveButton("Send", MainActivityFragment.this).setNegativeButton("Cancel", null);

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
        JAPA = (TextView) rootView.findViewById(R.id.tvJapa);
        RE = (TextView) rootView.findViewById(R.id.tvReading);
        HR = (TextView) rootView.findViewById(R.id.tvHearing);
        IS = (TextView) rootView.findViewById(R.id.tvIServiceTime);
        PS = (TextView) rootView.findViewById(R.id.tvPServiceTime);
        SentBy = (EditText) rootView.findViewById(R.id.etSentBy);

        WU.setOnClickListener(this);
        DR.setOnClickListener(this);
        TB.setOnClickListener(this);
        JAPA.setOnClickListener(this);
        HR.setOnClickListener(this);
        RE.setOnClickListener(this);
        IS.setOnClickListener(this);
        PS.setOnClickListener(this);

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
                time[1] = 30;
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


            case R.id.tvIServiceTime:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvIServiceTime);

                time[0] = 0;
                time[1] = 30;
                args.putIntArray(BUNDLE_KEY, time);

                newFragment.setArguments(args);

                newFragment.show(getActivity().getFragmentManager(), "durationPicker");
                break;
            case R.id.tvPServiceTime:
                newFragment = new DurationPickerFragment();

                args = new Bundle();
                args.putInt("id", R.id.tvPServiceTime);

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
                "\nWU - " + WU.getText().toString() +
                "\nDR - " + DR.getText().toString() +
                "\nTB - " + TB.getText().toString() +
                "\nJAPA - " + JAPA.getText().toString() +
                "\nRE - " + RE.getText().toString() +
                "\nHR - " + HR.getText().toString() +
                "\nIS - " + IS.getText().toString() +
                "\nPS - " + PS.getText().toString() +
                "\nYS\n" + SentBy.getText().toString();


        sendSMS(contact, textBody);

    }

    private void sendSMS(String phoneNumber, String messageBody){
        String SMS_SENT = "SMS_SENT";
        String SMS_DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(getContext(), 0, new Intent(SMS_SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(getContext(), 0, new Intent(SMS_DELIVERED), 0);

        //When the sms has been sent
        getContext().registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(getContext(), "SMS sent successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getContext(), "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter(SMS_SENT));

        //When the sms is delivered
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(getContext(), "SMS delivered successfully", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getContext(), "Service is currently unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter(SMS_DELIVERED));

        //get the default instance of sms manager
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, messageBody, sentPI, deliveredPI);
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