package database.contract;

import android.provider.BaseColumns;

/**
 * Created by Florian on 23.06.2015.
 */
public final class WorkBlockContract {

    public WorkBlockContract(){ }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + WorkBlockRow.TABLE_NAME + " (" +
                    WorkBlockRow._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    WorkBlockRow.COLUMN_NAME_CREATION_TIME + INTEGER_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_LAST_UPDATE_TIME + INTEGER_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_WORK_START + INTEGER_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_WORK_END + INTEGER_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_CATEGORY_ID + INTEGER_TYPE + COMMA_SEP +
                    WorkBlockRow.COLUMN_NAME_ENTRY_ID + INTEGER_TYPE + COMMA_SEP +
                    "FOREIGN KEY (" + WorkBlockRow.COLUMN_NAME_ENTRY_ID + ") REFERENCES "+ WorkEntryContract.WorkEntryRow.TABLE_NAME + " (" + WorkEntryContract.WorkEntryRow._ID + ")" +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + WorkBlockRow.TABLE_NAME;

    public static abstract class WorkBlockRow implements BaseColumns {
        public static final String TABLE_NAME = "work_block";
        public static final String COLUMN_NAME_ENTRY_ID = "_entry_id";
        public static final String COLUMN_NAME_CREATION_TIME = "creation_time";
        public static final String COLUMN_NAME_LAST_UPDATE_TIME = "last_update";
        public static final String COLUMN_NAME_WORK_START = "work_start";
        public static final String COLUMN_NAME_WORK_END= "work_end";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_CATEGORY_ID = "category";
    }
}
