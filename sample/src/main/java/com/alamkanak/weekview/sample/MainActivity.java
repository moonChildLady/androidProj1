package com.alamkanak.weekview.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Created by Raquib-ul-Alam Kanak on 7/21/2014.
 * Website: http://alamkanak.github.io/
 */
public class MainActivity extends ActionBarActivity implements WeekView.MonthChangeListener,
        WeekView.EventClickListener, WeekView.EventLongPressListener, WeekView.EmptyViewClickListener, WeekView.EmptyViewLongPressListener, WeekView.ScrollListener{

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    private final int userInputRequestCode = 689;
    private ToDoListDbHelper dbHelper = null;
    public static final int progress_bar_type = 0;
    private ProgressDialog prgDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        mWeekView.setEmptyViewClickListener(this);
        mWeekView.setEmptyViewLongPressListener(this);
        setupDateTimeInterpreter(false);

        File wallpaperDirectory = new File(Environment.getExternalStorageDirectory().
                getAbsolutePath()+"/toDoList/");
// have the object build the directory structure, if needed.
        wallpaperDirectory.mkdirs();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.action_week_view);
        switch (id){
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12==0 ? "12": hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {

        // Populate the week view with some events.
        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

        dbHelper = new ToDoListDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + ToDoListDbContract.ToDoListDbEntry.TABLE_NAME+" WHERE "+ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_YEAR+" = "+newYear+ " AND "+ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_MONTH +" = "+newMonth, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry._ID));
                String startdatetime = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STARTDATETIME));
                String enddatetime = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_ENDDATETIME));
                String jobdesc = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_JOBDESC));
                String photo = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_PHOTO));
                String audio = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_AUDIO));
                String sms = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_SMS));
                String color = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_COLOR));
                String status = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STATUS));
                //String entry = isbn + ":" + title + ":" + author;




                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
                    Date startdate = format.parse(startdatetime);
                    SimpleDateFormat startYear = new SimpleDateFormat("yyyy");
                    SimpleDateFormat startMonth = new SimpleDateFormat("MM");
                    SimpleDateFormat startDay = new SimpleDateFormat("dd");
                    SimpleDateFormat startHour = new SimpleDateFormat("H");
                    SimpleDateFormat startminutes = new SimpleDateFormat("m");

                    String startYYYY = startYear.format(startdate);
                    String startMM = startMonth.format(startdate);
                    String startdd = startDay.format(startdate);
                    String starth = startHour.format(startdate);
                    String startm = startminutes.format(startdate);


                    Date enddate = format.parse(enddatetime);
                    SimpleDateFormat endYear = new SimpleDateFormat("yyyy");
                    SimpleDateFormat endMonth = new SimpleDateFormat("MM");
                    SimpleDateFormat endDay = new SimpleDateFormat("dd");
                    SimpleDateFormat endHour = new SimpleDateFormat("H");
                    SimpleDateFormat endminutes = new SimpleDateFormat("m");
                    String endYYYY = endYear.format(enddate);
                    String endMM = endMonth.format(enddate);
                    String enddd = endDay.format(enddate);
                    String endh = endHour.format(enddate);
                    String endm = endminutes.format(enddate);


                    Calendar startTime = Calendar.getInstance();
                    startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(starth));
                    startTime.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startdd));
                    startTime.set(Calendar.MINUTE, Integer.parseInt(startm));
                    startTime.set(Calendar.MONTH, Integer.parseInt(startMM)-1);
                    startTime.set(Calendar.YEAR, Integer.parseInt(startYYYY));
                    Calendar endTime = Calendar.getInstance();
                    endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endh));
                    endTime.set(Calendar.DAY_OF_MONTH, Integer.parseInt(enddd));
                    endTime.set(Calendar.MINUTE, Integer.parseInt(endm));
                    endTime.set(Calendar.MONTH, Integer.parseInt(endMM) - 1);
                    endTime.set(Calendar.YEAR, Integer.parseInt(endYYYY));

                    WeekViewEvent event = new WeekViewEvent(id, getEventTitle(startTime, jobdesc, (status.equals("1")?true:false)), startTime, endTime);
                    if(color.equals("1")) {
                        event.setColor(getResources().getColor(R.color.event_color_02));
                    }else{
                        event.setColor(getResources().getColor(R.color.event_color_03));
                    }
                    events.add(event);
                } catch (ParseException e) {
                    e.printStackTrace();
                }





            }
        }
        catch (SQLiteException e) {
            Toast.makeText(this, "Failed to retrieve DB records", Toast.LENGTH_LONG).show();
        }
        db.close();

//        Calendar startTime = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, 3);
//        startTime.set(Calendar.MINUTE, 0);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        Calendar endTime = (Calendar) startTime.clone();
//        endTime.add(Calendar.HOUR, 1);
//        endTime.set(Calendar.MONTH, newMonth-1);
//        WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_01));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, 3);
//        startTime.set(Calendar.MINUTE, 30);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        endTime = (Calendar) startTime.clone();
//        endTime.set(Calendar.HOUR_OF_DAY, 4);
//        endTime.set(Calendar.MINUTE, 30);
//        endTime.set(Calendar.MONTH, newMonth-1);
//        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_02));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, 4);
//        startTime.set(Calendar.MINUTE, 20);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        endTime = (Calendar) startTime.clone();
//        endTime.set(Calendar.HOUR_OF_DAY, 5);
//        endTime.set(Calendar.MINUTE, 0);
//        event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_03));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, 5);
//        startTime.set(Calendar.MINUTE, 30);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        endTime = (Calendar) startTime.clone();
//        endTime.add(Calendar.HOUR_OF_DAY, 2);
//        endTime.set(Calendar.MONTH, newMonth-1);
//        event = new WeekViewEvent(2, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_02));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.HOUR_OF_DAY, 5);
//        startTime.set(Calendar.MINUTE, 0);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        startTime.add(Calendar.DATE, 1);
//        endTime = (Calendar) startTime.clone();
//        endTime.add(Calendar.HOUR_OF_DAY, 3);
//        endTime.set(Calendar.MONTH, newMonth - 1);
//        event = new WeekViewEvent(3, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_03));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.DAY_OF_MONTH, 15);
//        startTime.set(Calendar.HOUR_OF_DAY, 3);
//        startTime.set(Calendar.MINUTE, 0);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        endTime = (Calendar) startTime.clone();
//        endTime.add(Calendar.HOUR_OF_DAY, 3);
//        event = new WeekViewEvent(4, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_04));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.DAY_OF_MONTH, 1);
//        startTime.set(Calendar.HOUR_OF_DAY, 3);
//        startTime.set(Calendar.MINUTE, 0);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        endTime = (Calendar) startTime.clone();
//        endTime.add(Calendar.HOUR_OF_DAY, 3);
//        event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_01));
//        events.add(event);
//
//        startTime = Calendar.getInstance();
//        startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH));
//        startTime.set(Calendar.HOUR_OF_DAY, 15);
//        startTime.set(Calendar.MINUTE, 0);
//        startTime.set(Calendar.MONTH, newMonth-1);
//        startTime.set(Calendar.YEAR, newYear);
//        endTime = (Calendar) startTime.clone();
//        endTime.add(Calendar.HOUR_OF_DAY, 3);
//        event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
//        event.setColor(getResources().getColor(R.color.event_color_02));
//        events.add(event);

        return events;
    }


    private String getEventTitle(Calendar time, String jobdesc, boolean jobdone) {
        String jobstatus ="";
        if(jobdone){
            jobstatus = "Done";
        }
        return String.format(jobdesc+ " %02d:%02d %s/%d" +"\n"+ jobstatus, time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

        Intent intent = new Intent(this, userInputActivity.class);
        intent.putExtra("id", event.getId());

        startActivityForResult(intent, userInputRequestCode);

        Toast.makeText(MainActivity.this, "Clicked " + event.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        //Toast.makeText(MainActivity.this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
        final long eventID = event.getId();

        dbHelper = new ToDoListDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();


            Cursor cursor = db.rawQuery("SELECT * FROM " + ToDoListDbContract.ToDoListDbEntry.TABLE_NAME + " WHERE " + ToDoListDbContract.ToDoListDbEntry._ID + " = " +event.getId() + " AND " + ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STATUS+ " = 0", null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry._ID));
                String startdatetime = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STARTDATETIME));
                String enddatetime = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_ENDDATETIME));
                String jobdesc = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_JOBDESC));
                String photo = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_PHOTO));
                String audio = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_AUDIO));
                String sms = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_SMS));
                String color = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_COLOR));
                String status = cursor.getString(cursor.getColumnIndex(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STATUS));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                StringBuilder build = new StringBuilder();
                try {
                    build.append("Job Description: ")
                            .append((jobdesc.isEmpty()) ? "" : jobdesc)
                            .append("\n")
                            .append("Start Date Time: ")
                            .append(sdf1.format(sdf.parse(startdatetime)))
                            .append("\n")
                            .append("End Date Time: ")
                            .append(sdf1.format(sdf.parse(enddatetime)))
                            .append("\n")
                            .append("Send SMS: ")
                            .append((sms.equals("1")) ? "YES" : "NO");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                final String SMSBODY = build.toString();
                boolean sentSMS = false;
                if(sms.equals("1")) {
                    sentSMS = true;
                }else{
                    sentSMS = false;
                }
                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(MainActivity.this);
                myAlertDialog.setTitle("Job Detail");
                myAlertDialog.setMessage(SMSBODY);
                final boolean finalSentSMS = sentSMS;
                myAlertDialog.setPositiveButton("Finish", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the OK button is clicked
                        //String params = "";
                        //int ID = event.getId();
                        dbHelper = new ToDoListDbHelper(getApplicationContext());
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        ContentValues values = new ContentValues();


                        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STATUS, 1);
                        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_COLOR, "2");
                        db.update(
                                ToDoListDbContract.ToDoListDbEntry.TABLE_NAME,
                                values,
                                ToDoListDbContract.ToDoListDbEntry._ID + "=" + eventID,
                                null
                        );
                        if(finalSentSMS) {
                            new sendSms().execute("62452312", SMSBODY);
                        }
                        mWeekView.notifyDatasetChanged();

                    }});
                myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the Cancel button is clicked
                    }
                });
                myAlertDialog.show();
            }


    }

    @Override
    public void onEmptyViewClicked(Calendar time, Calendar tempTime, boolean clickedTwice) {
        if(clickedTwice) {
            //Toast.makeText(MainActivity.this, "Double click empty" + time.get(Calendar.HOUR_OF_DAY), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, userInputActivity.class);
            intent.putExtra("hour", time.get(Calendar.HOUR_OF_DAY));
            intent.putExtra("year", time.get(Calendar.YEAR));
            intent.putExtra("month", time.get(Calendar.MONTH)+1);
            intent.putExtra("day", time.get(Calendar.DAY_OF_MONTH));

            startActivityForResult(intent, userInputRequestCode);
        }
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(MainActivity.this, "Long pressed empty: " + time.get(Calendar.HOUR_OF_DAY), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) {
        //Toast.makeText(MainActivity.this, "Long pressed empty: ", Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult(int reqID, int resultCode, Intent data){
        if(reqID == userInputRequestCode){
            if(resultCode== Activity.RESULT_OK){
                mWeekView.notifyDatasetChanged();
                //Toast.makeText(MainActivity.this, "Long pressed empty: ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                prgDialog = new ProgressDialog(this);
                prgDialog.setMessage("Sending SMS. Please wait...");
                prgDialog.setIndeterminate(false);
                prgDialog.setMax(100);
                prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                prgDialog.setCancelable(false);
                prgDialog.show();
                return prgDialog;
            default:
                return null;
        }
    }

    class sendSms extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Shows Progress Bar Dialog and then call doInBackground method
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... f_url) {
            SmsManager sms = SmsManager.getDefault();
            try {
                sms.sendTextMessage(f_url[0], null, f_url[1], null, null);
                return "Done";
            } catch (Exception e) {
                return "False";
            }
        }

        protected void onProgressUpdate(String... progress) {
            //Not sure what to do here
            prgDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result){
            if(result.equals("Done"))
            {
                //progressDialog.setVisibility(View.GONE);
                //progressDialog.dismiss();
                dismissDialog(progress_bar_type);
                //Intent intn = new Intent(this, MainActivity.class);
                //startActivity(intn);
            }else{
                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(MainActivity.this);
                myAlertDialog.setTitle("ERROR");
                myAlertDialog.setMessage("SMS cannot sent!");
                myAlertDialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the OK button is clicked
                        //String params = "";

                        new sendSms().execute();

                    }});
                myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the Cancel button is clicked
                    }});
                myAlertDialog.show();
            }

        }

    }
}
