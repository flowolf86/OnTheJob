package database.contract;

import android.provider.BaseColumns;

/**
 * Created by Florian on 22.06.2015.
 */
public final class WorkEntryContract {

    public WorkEntryContract(){ }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + WorkEntryRow.TABLE_NAME + " (" +
                    WorkEntryRow._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    WorkEntryRow.COLUMN_NAME_CREATION_TIME + INTEGER_TYPE + COMMA_SEP +
                    WorkEntryRow.COLUMN_NAME_LAST_UPDATE_TIME + INTEGER_TYPE + COMMA_SEP +
                    WorkEntryRow.COLUMN_NAME_REFERENCE_TIME + INTEGER_TYPE + COMMA_SEP +
                    WorkEntryRow.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    WorkEntryRow.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    WorkEntryRow.COLUMN_NAME_TYPE + INTEGER_TYPE +
            ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + WorkEntryRow.TABLE_NAME;

    public static abstract class WorkEntryRow implements BaseColumns {
        public static final String TABLE_NAME = "work_entry";
        public static final String COLUMN_NAME_CREATION_TIME = "creation_time";
        public static final String COLUMN_NAME_LAST_UPDATE_TIME = "last_update";
        public static final String COLUMN_NAME_REFERENCE_TIME = "reference_time";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_TYPE = "type";
    }
}
