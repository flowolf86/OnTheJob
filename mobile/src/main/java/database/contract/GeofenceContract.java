package database.contract;

import android.provider.BaseColumns;

public final class GeofenceContract {

    public GeofenceContract(){ }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + GeofenceRow.TABLE_NAME + " (" +
                    GeofenceRow._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    GeofenceRow.COLUMN_NAME_TIMESTAMP + INTEGER_TYPE + COMMA_SEP +
                    GeofenceRow.COLUMN_NAME_FENCE_ID + TEXT_TYPE + COMMA_SEP +
                    GeofenceRow.COLUMN_NAME_TYPE + INTEGER_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + GeofenceRow.TABLE_NAME;

    public static abstract class GeofenceRow implements BaseColumns {
        public static final String TABLE_NAME = "geofence_events";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_FENCE_ID = "fence_id";
    }
}

