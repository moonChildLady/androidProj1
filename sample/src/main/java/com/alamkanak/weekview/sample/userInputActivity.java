package com.alamkanak.weekview.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.sample.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class userInputActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    int starthour, startmin, endhour = -1, endmin = -1, isNewrecord = -1;
    ImageView ivImage;
    private TimePicker timePickerTo, timePickerFrom;
    private ToDoListDbHelper dbHelper = null;
    Button btnAddPhoto, btnRecordAudio, btnPlayRecord,btnCancel, btnSubmit;
    DatePicker dp, enddp;
    EditText etDesc;
    Switch swSMS, swRepeat;
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean haveSoundFile = false;
    private boolean isRecording = false;
    Long intentid;
    short[] bufferSound = null;
    private MediaPlayer mediaPlayer;
    Spinner spinYear;
    String filePath = Environment.getExternalStorageDirectory().getPath()+ "/toDoList/";

    String ts, attachedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);

//        Bundle extras = getIntent().getExtras();
//        //Date dt = new Date(extras.getString("time"));
//        String dt = extras.getString("time");

        timePickerFrom = (TimePicker) findViewById(R.id.timePickerFrom);
        timePickerTo = (TimePicker) findViewById(R.id.timePickerTo);

        btnAddPhoto = (Button) findViewById(R.id.btnAddPhoto);
        btnRecordAudio = (Button) findViewById(R.id.btnRecordAudio);
        btnPlayRecord = (Button) findViewById(R.id.btnPlayRecord);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        etDesc = (EditText) findViewById(R.id.etDesc);
        swSMS = (Switch) findViewById(R.id.swSMS);
        swRepeat = (Switch) findViewById(R.id.swRepeat);

        timePickerFrom.setIs24HourView(true);
        timePickerTo.setIs24HourView(true);

        ivImage = (ImageView) findViewById(R.id.ivImage);
        spinYear = (Spinner)findViewById(R.id.spRepeat);
        dp = (DatePicker) findViewById(R.id.datePicker);
        enddp = (DatePicker) findViewById(R.id.datePickerRepeat);


        Intent i = getIntent();
        int hour = i.getIntExtra("hour", 0);
        int year = i.getIntExtra("year", 0);
        int month = i.getIntExtra("month", 0);
        int day = i.getIntExtra("day", 0);

        intentid = i.getLongExtra("id", 0);
        Toast.makeText(this, "db failed to create" + intentid, Toast.LENGTH_SHORT).show();

        enableButtons(false);
        //swSMS.setChecked(true);
        if(intentid>0){
            dbHelper = new ToDoListDbHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            try {
                Cursor cursor = db.rawQuery("SELECT * FROM " + ToDoListDbContract.ToDoListDbEntry.TABLE_NAME+" WHERE "+ToDoListDbContract.ToDoListDbEntry._ID+" = "+intentid, null);
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

                    hour = Integer.parseInt(starth);
                    year = Integer.parseInt(startYYYY);
                    month = Integer.parseInt(startMM);
                    day = Integer.parseInt(startdd);

                    endhour = Integer.parseInt(endh);
                    endmin = Integer.parseInt(endm);

                    if(sms.equals("1")){
                        swSMS.setChecked(true);
                    }else {
                        swSMS.setChecked(false);
                    }

                    File audioFile = new  File(filePath+audio+".3gpp");
                    if(audioFile.exists()){
                        enableButtons(true);
                        ts = audio;
                    }

                    File imgFile = new  File(filePath+photo);

                    if(imgFile.exists()){

                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


                        ivImage.setImageBitmap(myBitmap);

                    }
                    etDesc.setText(jobdesc);
                    isNewrecord = 0;



                }
            }
            catch (SQLiteException e) {
                Toast.makeText(this, "Failed to retrieve DB records", Toast.LENGTH_LONG).show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            db.close();




        }
        spinYear.setVisibility(View.GONE);
        enddp.setVisibility(View.GONE);
        if(isNewrecord != -1) {

            swRepeat.setVisibility(View.GONE);

            //enddp.setVisibility(View.GONE);


        }
        String combind = String.valueOf(year)+String.valueOf(month)+String.valueOf(day)+String.valueOf(hour);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMddH");

        try {
            Date date = format.parse(combind);
            SimpleDateFormat destDf = new SimpleDateFormat("yyyy-MM-dd");
            combind = destDf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //setTitle("Detail Of "+combind);

        dp.updateDate(year, month - 1, day);
        enddp.updateDate(year, month - 1, day);

        timePickerFrom.setCurrentHour(hour);
        timePickerFrom.setCurrentMinute(0);

        timePickerTo.setCurrentHour((endhour > -1) ? endhour : hour + 1);
        timePickerTo.setCurrentMinute((endmin > -1) ? endmin : 0);
        timePickerFrom.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                //updateDisplay(hourOfDay, minute);

                validateTheTime();
            }
        });

        timePickerTo.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                //updateDisplay(hourOfDay, minute);

                validateTheTime();
            }
        });

        btnAddPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnRecordAudio.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                File older = new File(filePath + ts + ".3gpp");
                older.delete();

                Long tsLong = System.currentTimeMillis() / 1000;
                ts = tsLong.toString();

                recordAudio(ts);
                enableButtons(true);
            }
        });

        btnPlayRecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playAudio(ts);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                setResult(Activity.RESULT_CANCELED, result);
                File older = new File(filePath+ts+".3gpp");
                older.delete();
                finish();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                insertRecord();
                //Toast.makeText(this, "db failed to create" + intentid.toString(), Toast.LENGTH_SHORT).show();

                Intent result = new Intent();
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });


        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int a = 2; a <= 30; a++) {
            years.add(Integer.toString(a));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);


        spinYear.setAdapter(adapter);
        //setButtonHandlers();

        swRepeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked){
                    enddp.setVisibility(View.VISIBLE);
                }else{
                    enddp.setVisibility(View.GONE);
                }
            }
        });
        //textView01 = (TextView) findViewById(R.id.TextView01);
        //textView01.setText(String.valueOf(dt));
        //textView01.setText(String.valueOf(year)+String.valueOf(month)+String.valueOf(day)+String.valueOf(hour));




    }

//    @Override
//    protected void onStop() {
//        super.onStop();  // Always call the superclass method first
//
//        File older = new File(filePath+ts+".3gpp");
//        //older.delete();
//
//    }

    @Override
    public void onBackPressed() {
        // Write your code here

        super.onBackPressed();
        File older = new File(filePath+ts+".3gpp");
        older.delete();
    }
    private void validateTheTime() {

        int tp1H = timePickerTo.getCurrentHour();
        int tp2H = timePickerFrom.getCurrentHour();

        if(tp1H < tp2H){
            Toast.makeText(this, " "+ 1, Toast.LENGTH_SHORT).show();
        }
    }

    private void insertRecord() {


          //  Toast.makeText(this, "db failed to create" + ID, Toast.LENGTH_SHORT).show();
        int startday = dp.getDayOfMonth();
        int startmonth  = dp.getMonth() + 1;
        int startyear = dp.getYear();

        int endyear = enddp.getYear();
        int endmonth = enddp.getMonth() + 1;
        int endday = enddp.getDayOfMonth();

        int timeFromHr = timePickerFrom.getCurrentHour();
        int timeFromMin = timePickerFrom.getCurrentMinute();

        int timeToHr = timePickerTo.getCurrentHour();
        int timeToMin = timePickerTo.getCurrentMinute();

        String STARTDATETIME = String.valueOf(startyear)
                + String.format("%02d", startmonth)
                + String.format("%02d", startday)
                + String.format("%02d", timeFromHr)
                + String.format("%02d", timeFromMin);
        // while(fromDate.before(toDate)) {

        String ENDDATETIME = String.valueOf(endyear)
                + String.format("%02d", endmonth)
                + String.format("%02d", endday)
                + String.format("%02d", timeToHr)
                + String.format("%02d", timeToMin);

        String ENDHOUR = String.valueOf(startyear)
                + String.format("%02d", startmonth)
                + String.format("%02d", startday)
                + String.format("%02d", timeToHr)
                + String.format("%02d", timeToMin);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(STARTDATETIME));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //c.add(Calendar.DATE, 40);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE

        Calendar c2 = Calendar.getInstance();
        try {
            c2.setTime(sdf.parse(ENDDATETIME));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c3 = Calendar.getInstance();
        try {
            c3.setTime(sdf.parse(ENDHOUR));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String output = sdf.format(c.getTime());
        String output2 = sdf.format(c2.getTime());
        System.out.println("STARTDATETIME" + output);
        System.out.println("STARTDATETIME" + output2);

        dbHelper = new ToDoListDbHelper(getApplicationContext());
        SQLiteDatabase db=null;
        String SMS = null;
        //  gets database in write mode
        db = dbHelper.getWritableDatabase();

        //  create a new map of values, where column names are the keys

        String JOBDESC = etDesc.getText().toString();

        if(swSMS.isChecked()){
            SMS = "1";
        }else{
            SMS = "2";
        }
        while(c.getTimeInMillis() <= c2.getTimeInMillis()) {
            // insert into the database


            System.out.println("STARTDATETIME" + sdf.format(c.getTime()));
            System.out.println("STARTDATETIME" + sdf.format(c3.getTime()));
        ContentValues values = new ContentValues();


        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STARTDATETIME, sdf.format(c.getTime()));
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_ENDDATETIME, sdf.format(c3.getTime()));


        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_JOBDESC, JOBDESC);
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_PHOTO, attachedPhoto);
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_AUDIO, ts);
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_SMS, SMS);
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_COLOR, "1");
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_STATUS, "0");
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_YEAR, String.valueOf(startyear));
        values.put(ToDoListDbContract.ToDoListDbEntry.COLUMN_NAME_MONTH, String.valueOf(startmonth));
        //System.out.println("STARTDATETIME" + STARTDATETIME);
        //System.out.println("STARTDATETIME" + ENDDATETIME);
        //long newRowId;
        if (isNewrecord == -1) {

            db.insert(
                    ToDoListDbContract.ToDoListDbEntry.TABLE_NAME,
                    null, // inserting a row with null values is prohibited
                    values
            );

        } else {

            db.update(
                    ToDoListDbContract.ToDoListDbEntry.TABLE_NAME,
                    values,
                    ToDoListDbContract.ToDoListDbEntry._ID + "=" + intentid,
                    null
            );

        }
//                Calendar c = Calendar.getInstance();
//                c.setTime(fromDate);
//                c.add(Calendar.DATE, 7);  // number of days to add



            c.add(Calendar.DATE, 7);
            c3.add(Calendar.DATE, 7);
        }
        db.close();

        //Date fromDate = df.parse(STARTDATETIME);
        //for(int i = 0; i< repeatNo;i++) {







        //clearForm();
        //Toast.makeText(this, "Row ID: " + newRowId, Toast.LENGTH_LONG).show();
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(userInputActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        String fileName = System.currentTimeMillis() + ".jpg";
        File destination = new File(filePath,
                fileName);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        attachedPhoto = fileName;
        ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);
        String fileName = System.currentTimeMillis() + ".jpg";
        File destination = new File(filePath,
                fileName);

        Bitmap thumbnail = (Bitmap) bm;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        attachedPhoto = fileName;
        ivImage.setImageBitmap(bm);
    }


//    private void setButtonHandlers() {
//        ((Button) findViewById(R.id.btnRecordAudio)).setOnClickListener(btnClick);
//        ((Button) findViewById(R.id.btnStopRecord)).setOnClickListener(btnClick);
//        ((Button) findViewById(R.id.btnPlayRecord)).setOnClickListener(btnClick);
//    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean haveSoundFile) {
        //enableButton(R.id.btnRecordAudio, !haveSoundFile);
        enableButton(R.id.btnPlayRecord, haveSoundFile);
    }


    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private void writeAudioDataToFile() {
        bufferSound = new short[1024];

    }

    private void playRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.read(bufferSound, 0, 1024);
            //recorder.release();
            //recorder = null;
            //recordingThread = null;
        }
    }
//    private void writeAudioDataToFile() {
//        // Write the output audio in byte
//
//        String filePath = "/sdcard/voice8K16bitmono.pcm";
//        short sData[] = new short[BufferElements2Rec];
//
//        FileOutputStream os = null;
//        try {
//            os = new FileOutputStream(filePath);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        while (isRecording) {
//            // gets the voice output from microphone to byte format
//
//            recorder.read(sData, 0, BufferElements2Rec);
//            System.out.println("Short wirting to file" + sData.toString());
//            try {
//                // // writes the data to file from buffer
//                // // stores the voice buffer
//                byte bData[] = short2byte(sData);
//                os.write(bData, 0, BufferElements2Rec * BytesPerElement);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        try {
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

//    private View.OnClickListener btnClick = new View.OnClickListener() {
//        public void onClick(View v) {
//            switch (v.getId()) {
//                case R.id.btnRecordAudio: {
//                    enableButtons(true);
//                    startRecording();
//                    break;
//                }
//                case R.id.btnStopRecord: {
//                    enableButtons(false);
//                    stopRecording();
//                    break;
//                }
//                case R.id.btnPlayRecord: {
//                    enableButtons(false);
//                    stopRecording();
//                    break;
//                }
//            }
//        }
//    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }



    public void recordAudio(String fileName) {
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(filePath +fileName+".3gpp");
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }

        final ProgressDialog mProgressDialog = new ProgressDialog(userInputActivity.this);
        mProgressDialog.setTitle("Recording");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mProgressDialog.dismiss();
                recorder.stop();
                recorder.release();
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface p1) {
                recorder.stop();
                recorder.release();
            }
        });
        try {
            recorder.start();
            mProgressDialog.show();
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    public void playAudio(String fileName) {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(filePath + fileName+".3gpp"));;



        final ProgressDialog mProgressDialog = new ProgressDialog(userInputActivity.this);
        mProgressDialog.setTitle("Playing");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("Stop Playing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mProgressDialog.dismiss();
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface p1) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });
        try {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            mProgressDialog.show();
        } catch (Exception e){
            e.printStackTrace();
        }


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_user_input, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
