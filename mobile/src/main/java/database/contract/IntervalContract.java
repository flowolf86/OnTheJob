package database.contract;

import android.provider.BaseColumns;

/**
 * Created by Florian on 22.06.2015.
 */
public final class IntervalContract {

    public IntervalContract(){ }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + IntervalRow.TABLE_NAME + " (" +
                    IntervalRow._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    IntervalRow.COLUMN_NAME_CREATION_TIME + INTEGER_TYPE + COMMA_SEP +
                    IntervalRow.COLUMN_NAME_LAST_UPDATE_TIME + INTEGER_TYPE + COMMA_SEP +
                    IntervalRow.COLUMN_NAME_START_TIME + INTEGER_TYPE + COMMA_SEP +
                    IntervalRow.COLUMN_NAME_END_TIME + INTEGER_TYPE + COMMA_SEP +
                    IntervalRow.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    IntervalRow.COLUMN_NAME_TEXT + TEXT_TYPE + COMMA_SEP +
                    IntervalRow.COLUMN_NAME_CATEGORY_ID + INTEGER_TYPE +
            ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + IntervalRow.TABLE_NAME;

    public static abstract class IntervalRow implements BaseColumns {
        public static final String TABLE_NAME = "interval";
        public static final String COLUMN_NAME_CREATION_TIME = "creation_time";
        public static final String COLUMN_NAME_LAST_UPDATE_TIME = "last_update";
        public static final String COLUMN_NAME_START_TIME = "start_time";
        public static final String COLUMN_NAME_END_TIME = "end_time";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_reference_id";
    }
}
