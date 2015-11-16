package database.contract;

import android.provider.BaseColumns;

/**
 * Created by Florian on 22.06.2015.
 */
public final class CategoryContract {

    public CategoryContract(){ }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + CategoriesRow.TABLE_NAME + " (" +
                    CategoriesRow._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CategoriesRow.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    CategoriesRow.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    CategoriesRow.COLUMN_NAME_TYPE + INTEGER_TYPE + COMMA_SEP +
                    CategoriesRow.COLUMN_NAME_COLOR + INTEGER_TYPE + COMMA_SEP +
                    CategoriesRow.COLUMN_NAME_ICON + BLOB_TYPE +
                    ")";

    public static final String SQL_DROP_TABLE =
            "DROP TABLE IF EXISTS " + CategoriesRow.TABLE_NAME;

    public static abstract class CategoriesRow implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_COLOR = "color";
        public static final String COLUMN_NAME_ICON = "icon";
    }
}

