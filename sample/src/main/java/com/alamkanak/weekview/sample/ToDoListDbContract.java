package com.alamkanak.weekview.sample;

import android.provider.BaseColumns;

/**
 * Created by Ming on 14/5/2015.
 */
public class ToDoListDbContract {
    public ToDoListDbContract() {};

    // Inner class that defines the table contents
    public static abstract class ToDoListDbEntry implements BaseColumns {
        public static final String TABLE_NAME = "TBL_TODOLIST";
        public static final String COLUMN_NAME_STARTDATETIME = "STARTDATETIME";
        public static final String COLUMN_NAME_ENDDATETIME ="ENDDATETIME";
        public static final String COLUMN_NAME_JOBDESC = "JOBDESC";
        public static final String COLUMN_NAME_PHOTO = "PHOTO";
        public static final String COLUMN_NAME_AUDIO = "AUDIO";
        public static final String COLUMN_NAME_SMS = "SMS";
        public static final String COLUMN_NAME_COLOR = "COLOR";
        public static final String COLUMN_NAME_STATUS = "JOBSTATUS";
        public static final String COLUMN_NAME_YEAR = "YEAR";
        public static final String COLUMN_NAME_MONTH = "MONTH";
    }

}
