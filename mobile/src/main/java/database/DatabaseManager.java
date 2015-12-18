package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cache.CategoryCacheCallback;
import cache.CategoryCacheHelper;
import cache.DataCacheHelper;
import cache.EntryCacheCallback;
import cache.GeofenceDbCallback;
import cache.IntervalCacheCallback;
import data.Category;
import data.Category.CategoryType;
import data.GeofenceEvent;
import data.Interval;
import data.WorkBlock;
import data.WorkEntry;
import data.WorkEntryType;
import data.factory.CategoryFactory;
import database.contract.CategoryContract;
import database.contract.GeofenceContract;
import database.contract.IntervalContract;
import database.contract.WorkBlockContract;
import database.contract.WorkBlockContract.WorkBlockRow;
import database.contract.WorkEntryContract;
import database.contract.WorkEntryContract.WorkEntryRow;
import support.AppBroadcaster;

import static database.contract.CategoryContract.CategoriesRow;
import static database.contract.GeofenceContract.GeofenceRow;
import static database.contract.IntervalContract.IntervalRow;

public class DatabaseManager extends SQLiteOpenHelper {

    public static Context mContext = null;

    public static final int NO_ERROR = -1;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "WorkHours.db";

    public DatabaseManager(@NonNull final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WorkEntryContract.SQL_CREATE_TABLE);
        db.execSQL(WorkBlockContract.SQL_CREATE_TABLE);
        db.execSQL(CategoryContract.SQL_CREATE_TABLE);
        db.execSQL(IntervalContract.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Update db here. Store data and write into new structure.
        // Store old data first!!!
        // db.execSQL(WorkEntryContract.SQL_DELETE_ENTRIES);
        // onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /*
        ALL DATA
     */

    public void readEntriesFromDatabase(final EntryCacheCallback cacheCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getReadableDatabase();

                // Fill the categories cache first to be able to reference categories in blocks
                try {
                    Thread t = readCategoriesFromDatabase(CategoryCacheHelper.getInstance());
                    t.join();
                }catch(Exception ignore){ }

                // Create table if not exists
                db.execSQL(WorkEntryContract.SQL_CREATE_TABLE);

                String[] entryProjection = {
                        WorkEntryRow._ID,
                        WorkEntryRow.COLUMN_NAME_CREATION_TIME,
                        WorkEntryRow.COLUMN_NAME_LAST_UPDATE_TIME,
                        WorkEntryRow.COLUMN_NAME_REFERENCE_TIME,
                        WorkEntryRow.COLUMN_NAME_TITLE,
                        WorkEntryRow.COLUMN_NAME_TEXT,
                        WorkEntryRow.COLUMN_NAME_TYPE
                };

                String sortOrder = WorkEntryRow._ID + " DESC";

                List<WorkEntry> results = new ArrayList<>();

                Cursor cursor = db.query(
                        WorkEntryRow.TABLE_NAME, // The table to query
                        entryProjection,         // The columns to return
                        null,                    // The columns for the WHERE clause
                        null,                    // The values for the WHERE clause
                        null,                    // don't group the rows
                        null,                    // don't filter by row groups
                        sortOrder                // The sort order
                );

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkEntryRow._ID));
                    long creationTime = cursor.getLong(cursor.getColumnIndexOrThrow(WorkEntryRow.COLUMN_NAME_CREATION_TIME));
                    long lastUpdateTime = cursor.getLong(cursor.getColumnIndexOrThrow(WorkEntryRow.COLUMN_NAME_LAST_UPDATE_TIME));
                    long referenceTime = cursor.getLong(cursor.getColumnIndexOrThrow(WorkEntryRow.COLUMN_NAME_REFERENCE_TIME));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(WorkEntryRow.COLUMN_NAME_TITLE));
                    String text = cursor.getString(cursor.getColumnIndexOrThrow(WorkEntryRow.COLUMN_NAME_TEXT));
                    @WorkEntryType.EntryType int type = cursor.getInt(cursor.getColumnIndexOrThrow(WorkEntryRow.COLUMN_NAME_TYPE));

                    WorkEntry entry = new WorkEntry(itemId, creationTime, lastUpdateTime, referenceTime, title, text, type);

                    readWorkBlocksToEntry(db, entry);

                    results.add(entry);
                    Log.d("DB", "READ REPORT: " + results.size() + " entries, " + entry.getWorkBlocks().size() + " block/s.");
                    cursor.moveToNext();
                }
                cursor.close();

                if(cacheCallback != null) {
                    cacheCallback.onDbReadEntriesComplete(results);
                }
            }
        };

        new Thread(r).start();
    }

    private void readWorkBlocksToEntry(SQLiteDatabase db, WorkEntry entry) {

        // Create table if not exists
        db.execSQL(WorkBlockContract.SQL_CREATE_TABLE);

        String[] blockProjection = {
                WorkBlockRow._ID,
                WorkBlockRow.COLUMN_NAME_CREATION_TIME,
                WorkBlockRow.COLUMN_NAME_LAST_UPDATE_TIME,
                WorkBlockRow.COLUMN_NAME_WORK_START,
                WorkBlockRow.COLUMN_NAME_WORK_END,
                WorkBlockRow.COLUMN_NAME_TITLE,
                WorkBlockRow.COLUMN_NAME_TEXT,
                WorkBlockRow.COLUMN_NAME_CATEGORY_ID,
                WorkBlockRow.COLUMN_NAME_ENTRY_ID
        };

        String sortOrder = WorkEntryRow._ID + " DESC";
        String whereClause = WorkBlockRow.COLUMN_NAME_ENTRY_ID + "=?";
        String [] whereArgs = {String.valueOf(entry.getId())};

        Cursor cursor = db.query(
                WorkBlockRow.TABLE_NAME, // The table to query
                blockProjection,         // The columns to return
                whereClause,             // The columns for the WHERE clause
                whereArgs,               // The values for the WHERE clause
                null,                    // don't group the rows
                null,                    // don't filter by row groups
                sortOrder                // The sort order
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkBlockRow._ID));
            long creationTime = cursor.getLong(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_CREATION_TIME));
            long lastUpdateTime = cursor.getLong(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_LAST_UPDATE_TIME));
            long workStart = cursor.getLong(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_WORK_START));
            long workEnd = cursor.getLong(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_WORK_END));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_TITLE));
            String text = cursor.getString(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_TEXT));
            int category = cursor.getInt(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_CATEGORY_ID));
            int refId = cursor.getInt(cursor.getColumnIndexOrThrow(WorkBlockRow.COLUMN_NAME_ENTRY_ID));

            entry.addWorkBlock(new WorkBlock(itemId, creationTime, lastUpdateTime, workStart, workEnd, title, text, category, refId));
            cursor.moveToNext();
        }
        cursor.close();
    }

    /*
        ENTRY SPECIFIC
     */

    public void createEntryInDatabase(final WorkEntry workEntry, final EntryCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Insert the entry
                ContentValues values = new ContentValues();
                values.put(WorkEntryRow.COLUMN_NAME_CREATION_TIME, System.currentTimeMillis());
                values.put(WorkEntryRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                values.put(WorkEntryRow.COLUMN_NAME_REFERENCE_TIME, workEntry.getDate());
                values.put(WorkEntryRow.COLUMN_NAME_TITLE, workEntry.getTitle());
                values.put(WorkEntryRow.COLUMN_NAME_TEXT, workEntry.getText());
                values.put(WorkEntryRow.COLUMN_NAME_TYPE, workEntry.type);

                long newEntryRowId = db.insert(WorkEntryRow.TABLE_NAME, null, values);
                int blockInsertFailCount = 0;

                // Insert all the blocks if the entry insert was successful
                if(newEntryRowId != -1) {
                    for (WorkBlock block : workEntry.getWorkBlocks()) {

                        values = new ContentValues();
                        values.put(WorkBlockRow.COLUMN_NAME_CREATION_TIME, System.currentTimeMillis());
                        values.put(WorkBlockRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                        values.put(WorkBlockRow.COLUMN_NAME_WORK_START, block.getWorkStart());
                        values.put(WorkBlockRow.COLUMN_NAME_WORK_END, block.getWorkEnd());
                        values.put(WorkBlockRow.COLUMN_NAME_TITLE, block.getTitle());
                        values.put(WorkBlockRow.COLUMN_NAME_TEXT, block.getText());
                        values.put(WorkBlockRow.COLUMN_NAME_CATEGORY_ID, block._category_reference_id);
                        values.put(WorkBlockRow.COLUMN_NAME_ENTRY_ID, newEntryRowId);

                        long newBlockRowId = db.insert(WorkBlockRow.TABLE_NAME, null, values);
                        blockInsertFailCount = newBlockRowId == -1 ? ++blockInsertFailCount : blockInsertFailCount;
                    }
                }

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                // Refresh entry id
                workEntry.setId(newEntryRowId);

                Log.d("DB", "WRITE REPORT: " + (newEntryRowId == -1 ? 0 : 1) + " entry/ies, " + (workEntry.getWorkBlocks().size() - blockInsertFailCount) + " block/s. " + blockInsertFailCount + " block/s failed.");
            }
        };

        new Thread(r).start();
    }

    public void removeEntryFromDatabase(final WorkEntry entryToDelete, final EntryCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception{

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Remove entry
                String entrySelection = WorkEntryRow._ID + " LIKE ?";
                String[] entrySelectionArgs = { String.valueOf(entryToDelete.getId()) };
                int entryRowCount = db.delete(WorkEntryRow.TABLE_NAME, entrySelection, entrySelectionArgs);

                // Remove blocks
                String blockSelection = WorkBlockRow.COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] blockSelectionArgs = { String.valueOf(entryToDelete.getId()) };
                int blockRowCount = db.delete(WorkBlockRow.TABLE_NAME, blockSelection, blockSelectionArgs);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "DELETE REPORT: " + entryRowCount + " entries, " + blockRowCount + " block/s.");
            }
        };

        new Thread(r).start();
    }

    public void modifyEntryInDatabase(final WorkEntry entry, final EntryCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // New value for one column
                ContentValues values = new ContentValues();
                values.put(WorkEntryRow.COLUMN_NAME_CREATION_TIME, entry.creation_time);
                values.put(WorkEntryRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                values.put(WorkEntryRow.COLUMN_NAME_REFERENCE_TIME, entry.getDate());
                values.put(WorkEntryRow.COLUMN_NAME_TITLE, entry.getTitle());
                values.put(WorkEntryRow.COLUMN_NAME_TEXT, entry.getText());
                values.put(WorkEntryRow.COLUMN_NAME_TYPE, entry.type);

                // Which row to update, based on the ID
                String selection = WorkEntryRow._ID + " LIKE ?";
                String[] selectionArgs = { String.valueOf(entry.getId()) };

                int count = db.update(
                        WorkEntryRow.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                try {
                    //Remove all old blocks
                    Thread t = removeAllBlocksOfEntryFromDatabase(null, entry);
                    t.join();

                    // Write new entries
                    for(WorkBlock block : entry.getWorkBlocks()){
                        // We ignore joining threads here because it's not needed / bad for performance
                        createBlockInDatabase(entry, block, null, null);
                    }
                }catch(Exception ignore){ }

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }
                Log.d("DB", "MODIFY REPORT: " + count + " entry/ies.");
            }
        };

        new Thread(r).start();
    }

    public Thread removeAllBlocksOfEntryFromDatabase(final EntryCacheCallback cacheCallback, final WorkEntry workEntry) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                String blockSelection = WorkBlockRow.COLUMN_NAME_ENTRY_ID + " LIKE ?";
                String[] blockSelectionArgs = { String.valueOf(workEntry.getId()) };
                int blockRowCount = db.delete(WorkBlockRow.TABLE_NAME, blockSelection, blockSelectionArgs);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "DELETE REPORT: " + blockRowCount + " block/s.");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    /*
        BLOCK SPECIFIC
     */

    public Thread createBlockInDatabase(final WorkEntry workEntry, final WorkBlock workBlock, final EntryCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception {

        final Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(WorkBlockRow.COLUMN_NAME_CREATION_TIME, System.currentTimeMillis());
                values.put(WorkBlockRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                values.put(WorkBlockRow.COLUMN_NAME_WORK_START, workBlock.getWorkStart());
                values.put(WorkBlockRow.COLUMN_NAME_WORK_END, workBlock.getWorkEnd());
                values.put(WorkBlockRow.COLUMN_NAME_TITLE, workBlock.getTitle());
                values.put(WorkBlockRow.COLUMN_NAME_TEXT, workBlock.getText());
                values.put(WorkBlockRow.COLUMN_NAME_CATEGORY_ID, workBlock._category_reference_id);
                values.put(WorkBlockRow.COLUMN_NAME_ENTRY_ID, workEntry.getId());

                long newBlockRowId = db.insert(WorkBlockRow.TABLE_NAME, null, values);

                // Set reference id
                workBlock._id = newBlockRowId;
                workBlock.setReferenceId(workEntry.getId());

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "WRITE REPORT: " + (newBlockRowId == -1 ? 0 : 1) + " block/s. ");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    /**
     * This will modify a block in the database OR create one (!)
     */
    public void modifyBlockInDatabase(final WorkEntry workEntry, final WorkBlock workBlock, final EntryCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // New values
                ContentValues values = new ContentValues();
                values.put(WorkBlockRow.COLUMN_NAME_CREATION_TIME, workBlock.creation_time);
                values.put(WorkBlockRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                values.put(WorkBlockRow.COLUMN_NAME_WORK_START, workBlock.getWorkStart());
                values.put(WorkBlockRow.COLUMN_NAME_WORK_END, workBlock.getWorkEnd());
                values.put(WorkBlockRow.COLUMN_NAME_TITLE, workBlock.getTitle());
                values.put(WorkBlockRow.COLUMN_NAME_TEXT, workBlock.getText());
                values.put(WorkBlockRow.COLUMN_NAME_CATEGORY_ID, workBlock._category_reference_id);
                values.put(WorkBlockRow.COLUMN_NAME_ENTRY_ID, workEntry.getId());

                // Which row to update, based on the ID
                String selection = WorkBlockRow._ID + " LIKE ?";
                String[] selectionArgs = { String.valueOf(workBlock._id) };

                int count = db.update(
                        WorkBlockRow.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                if(count == 0){
                    try {
                        Log.d("DB", "MODIFY REPORT: Unable to modify. Try to create block.");
                        createBlockInDatabase(workEntry, workBlock, cacheCallback, uiCallback);
                    }catch(Exception ignore){ }
                }

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "MODIFY REPORT: " + count + " block/s.");
            }
        };

        new Thread(r).start();
    }

    public void removeBlockFromDatabase(final WorkBlock workBlock, final EntryCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                String blockSelection = WorkBlockRow._ID + " LIKE ?";
                String[] blockSelectionArgs = { String.valueOf(workBlock._id) };
                int blockRowCount = db.delete(WorkBlockRow.TABLE_NAME, blockSelection, blockSelectionArgs);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "DELETE REPORT: " + blockRowCount + " block/s.");
            }
        };

        new Thread(r).start();
    }

    /*
        INTERVAL SPECIFIC
     */

    public void initIntervalDatabaseIfNotExists(SQLiteDatabase db){

        try {
            db.execSQL(IntervalContract.SQL_CREATE_TABLE);
        }catch(SQLException e){
            Log.e("DB", "Unable to create interval table.");
        }
    }

    public void createIntervalInDatabase(final Interval interval, final IntervalCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Insert the entry
                ContentValues values = new ContentValues();
                values.put(IntervalRow.COLUMN_NAME_CREATION_TIME, System.currentTimeMillis());
                values.put(IntervalRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                values.put(IntervalRow.COLUMN_NAME_START_TIME, interval.getStartDate());
                values.put(IntervalRow.COLUMN_NAME_END_TIME, interval.getEndDate());
                values.put(IntervalRow.COLUMN_NAME_TITLE, interval.getTitle());
                values.put(IntervalRow.COLUMN_NAME_TEXT, interval.getDescription());
                values.put(IntervalRow.COLUMN_NAME_CATEGORY_ID, interval.getCategory().getId());

                long newIntervalRowId = db.insert(IntervalRow.TABLE_NAME, null, values);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                // Refresh entry id
                interval.setId(newIntervalRowId);

                Log.d("DB", "WRITE REPORT: " + (newIntervalRowId == -1 ? 0 : 1) + " interval/s");
            }
        };

        new Thread(r).start();
    }

    public void removeIntervalFromDatabase(final Interval intervalToDelete, final IntervalCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception{

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                String intervalSelection = IntervalRow._ID + " LIKE ?";
                String[] intervalSelectionArgs = { String.valueOf(intervalToDelete.getId()) };
                int intervalRowCount = db.delete(IntervalRow.TABLE_NAME, intervalSelection, intervalSelectionArgs);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "DELETE REPORT: " + intervalRowCount + " interval/s");
            }
        };

        new Thread(r).start();
    }

    public void modifyIntervalInDatabase(final Interval interval, final IntervalCacheCallback cacheCallback, final DatabaseUiCallback uiCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // New value for one column
                ContentValues values = new ContentValues();
                values.put(IntervalRow.COLUMN_NAME_CREATION_TIME, System.currentTimeMillis());
                values.put(IntervalRow.COLUMN_NAME_LAST_UPDATE_TIME, System.currentTimeMillis());
                values.put(IntervalRow.COLUMN_NAME_START_TIME, interval.getStartDate());
                values.put(IntervalRow.COLUMN_NAME_END_TIME, interval.getEndDate());
                values.put(IntervalRow.COLUMN_NAME_TITLE, interval.getTitle());
                values.put(IntervalRow.COLUMN_NAME_TEXT, interval.getDescription());
                values.put(IntervalRow.COLUMN_NAME_CATEGORY_ID, interval.getCategory().getId());

                // Which row to update, based on the ID
                String selection = IntervalRow._ID + " LIKE ?";
                String[] selectionArgs = { String.valueOf(interval.getId()) };

                int count = db.update(
                        IntervalRow.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }
                Log.d("DB", "MODIFY REPORT: " + count + " interval/s.");
            }
        };

        new Thread(r).start();
    }

    public Thread readIntervalsFromDatabase(final IntervalCacheCallback cacheCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Create table if not exists
                initIntervalDatabaseIfNotExists(db);

                String[] entryProjection = {
                        IntervalRow._ID,
                        IntervalRow.COLUMN_NAME_CREATION_TIME,
                        IntervalRow.COLUMN_NAME_LAST_UPDATE_TIME,
                        IntervalRow.COLUMN_NAME_START_TIME,
                        IntervalRow.COLUMN_NAME_END_TIME,
                        IntervalRow.COLUMN_NAME_TITLE,
                        IntervalRow.COLUMN_NAME_TEXT,
                        IntervalRow.COLUMN_NAME_CATEGORY_ID
                };

                String sortOrder = CategoriesRow._ID + " DESC";

                List<Interval> results = new ArrayList<>();

                Cursor cursor = db.query(
                        IntervalRow.TABLE_NAME, // The table to query
                        entryProjection,         // The columns to return
                        null,                    // The columns for the WHERE clause
                        null,                    // The values for the WHERE clause
                        null,                    // don't group the rows
                        null,                    // don't filter by row groups
                        sortOrder                // The sort order
                );

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    long _id = cursor.getLong(cursor.getColumnIndexOrThrow(IntervalRow._ID));
                    long creationTime = cursor.getLong(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_CREATION_TIME));
                    long lastUpdate = cursor.getLong(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_LAST_UPDATE_TIME));
                    long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_START_TIME));
                    long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_END_TIME));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_TITLE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_TEXT));
                    long categoryRefId = cursor.getLong(cursor.getColumnIndexOrThrow(IntervalRow.COLUMN_NAME_CATEGORY_ID));

                    // Get the categories synchronosly. Make sure we have them up and running at this point of time!
                    try {
                        readCategoriesFromDatabase(CategoryCacheHelper.getInstance()).join();
                    }catch(Exception e){ }

                    List<Category> categories = CategoryCacheHelper.getInstance().getCategories();
                    Category intervalCategory = null;
                    for(Category category : categories){
                        if(categoryRefId == category._id){
                            intervalCategory = category;
                            break;
                        }
                    }

                    if(intervalCategory == null){
                        Log.w("Database", "Unable to find category.... Skipping interval");
                    } else {
                        results.add(new Interval(_id, creationTime, lastUpdate, title, description, startTime, endTime, intervalCategory));
                    }

                    cursor.moveToNext();
                }
                cursor.close();

                if(cacheCallback != null) {
                    cacheCallback.onDbReadIntervalsComplete(results);
                }

                //AppBroadcaster.sendCategoryCacheUpdatedBroadcast(mContext);
                Log.d("DB", "READ REPORT: " + results.size() + " interval/-s.");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    /*
        CATEGORIES SPECIFIC
     */

    public void initCategoryDatabaseIfNotExists(SQLiteDatabase db){

        db.execSQL(CategoryContract.SQL_CREATE_TABLE);

        try {
            createCategoryInDatabaseIfNotExists(db, CategoryFactory.getWorkDayCategory());
            createCategoryInDatabaseIfNotExists(db, CategoryFactory.getVacationCategory());
            createCategoryInDatabaseIfNotExists(db, CategoryFactory.getSickLeaveCategory());
        }catch(Exception e){
            Log.e("DB", "Unable to create system categories... This will end badly.");
        }
    }

    public Thread readCategoriesFromDatabase(final CategoryCacheCallback cacheCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Create table if not exists
                initCategoryDatabaseIfNotExists(db);

                String[] entryProjection = {
                        CategoriesRow._ID,
                        CategoriesRow.COLUMN_NAME_NAME,
                        CategoriesRow.COLUMN_NAME_DESCRIPTION,
                        CategoriesRow.COLUMN_NAME_TYPE,
                        CategoriesRow.COLUMN_NAME_COLOR,
                        CategoriesRow.COLUMN_NAME_ICON
                };

                String sortOrder = CategoriesRow._ID + " DESC";

                List<Category> results = new ArrayList<>();

                Cursor cursor = db.query(
                        CategoriesRow.TABLE_NAME, // The table to query
                        entryProjection,         // The columns to return
                        null,                    // The columns for the WHERE clause
                        null,                    // The values for the WHERE clause
                        null,                    // don't group the rows
                        null,                    // don't filter by row groups
                        sortOrder                // The sort order
                );

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(CategoriesRow._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(CategoriesRow.COLUMN_NAME_NAME));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(CategoriesRow.COLUMN_NAME_DESCRIPTION));
                    @CategoryType int type = cursor.getInt(cursor.getColumnIndexOrThrow(CategoriesRow.COLUMN_NAME_TYPE));
                    @ColorInt int color = cursor.getInt(cursor.getColumnIndexOrThrow(CategoriesRow.COLUMN_NAME_COLOR));
                    byte[] icon = cursor.getBlob(cursor.getColumnIndexOrThrow(CategoriesRow.COLUMN_NAME_ICON));

                    Category entry = new Category(itemId, title, description, color, icon, type);
                    results.add(entry);

                    cursor.moveToNext();
                }
                cursor.close();

                if(cacheCallback != null) {
                    cacheCallback.onDbReadCategoriesComplete(results);
                }

                AppBroadcaster.sendCategoryCacheUpdatedBroadcast(mContext);
                Log.d("DB", "READ REPORT: " + results.size() + " category/-ies.");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public Thread createCategoryInDatabase(final Category category, final CategoryCacheCallback cacheCallback) throws Exception {

        final Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(CategoriesRow.COLUMN_NAME_NAME, category.name);
                values.put(CategoriesRow.COLUMN_NAME_DESCRIPTION, category.description);
                values.put(CategoriesRow.COLUMN_NAME_TYPE, category.category_type);
                values.put(CategoriesRow.COLUMN_NAME_COLOR, category.color);
                values.put(CategoriesRow.COLUMN_NAME_ICON, category.icon);

                long newBlockRowId = db.insert(CategoriesRow.TABLE_NAME, null, values);

                // Set reference id
                category._id = newBlockRowId;

                if(cacheCallback != null) {
                    cacheCallback.triggerDatabaseRead(mContext);
                }
                refreshEntryCache();    //TODO Lose cupling

                Log.d("DB", "WRITE REPORT: " + (newBlockRowId == -1 ? 0 : 1) + " category/ies. ");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public void createCategoryInDatabaseIfNotExists(final SQLiteDatabase db, final Category category) throws Exception {

        final Runnable r = new Runnable() {
            @Override
            public void run() {

                ContentValues values = new ContentValues();
                values.put(CategoriesRow._ID, category._id);
                values.put(CategoriesRow.COLUMN_NAME_NAME, category.name);
                values.put(CategoriesRow.COLUMN_NAME_DESCRIPTION, category.description);
                values.put(CategoriesRow.COLUMN_NAME_TYPE, category.category_type);
                values.put(CategoriesRow.COLUMN_NAME_COLOR, category.color);
                values.put(CategoriesRow.COLUMN_NAME_ICON, category.icon);

                long newBlockRowId = db.insertWithOnConflict(CategoriesRow.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);

                if(newBlockRowId != 0) {
                    Log.d("DB", "WRITE REPORT: Setup default category " + category.name + ". Rows: " + (newBlockRowId == -1 ? 0 : 1));
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    public void modifyCategoryInDatabase(final Category category, final CategoryCacheCallback cacheCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // New values
                ContentValues values = new ContentValues();
                values.put(CategoriesRow.COLUMN_NAME_NAME, category.name);
                values.put(CategoriesRow.COLUMN_NAME_DESCRIPTION, category.description);
                values.put(CategoriesRow.COLUMN_NAME_TYPE, category.category_type);
                values.put(CategoriesRow.COLUMN_NAME_COLOR, category.color);
                values.put(CategoriesRow.COLUMN_NAME_ICON, category.icon);

                // Which row to update, based on the ID
                String selection = CategoriesRow._ID + " LIKE ?";
                String[] selectionArgs = { String.valueOf(category._id) };

                int count = db.update(
                        CategoriesRow.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                if(count == 0){
                    try {
                        Log.d("DB", "MODIFY REPORT: Unable to modify. Try to create category.");
                        createCategoryInDatabase(category, cacheCallback);

                    }catch(Exception ignore){ }
                }

                if(cacheCallback != null) {
                    cacheCallback.triggerDatabaseRead(mContext);
                }
                refreshEntryCache();    //TODO Lose cupling

                Log.d("DB", "MODIFY REPORT: " + count + " category/-ies.");
            }
        };

        new Thread(r).start();
    }

    public void removeCategoryFromDatabase(final Category category, final CategoryCacheCallback cacheCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                String blockSelection = CategoriesRow._ID + " LIKE ?";
                String[] blockSelectionArgs = { String.valueOf(category._id) };
                int blockRowCount = db.delete(CategoriesRow.TABLE_NAME, blockSelection, blockSelectionArgs);

                refreshEntryCache();
                if(cacheCallback != null) {
                    cacheCallback.triggerDatabaseRead(mContext);
                }
                refreshEntryCache();    //TODO Lose cupling

                Log.d("DB", "DELETE REPORT: " + blockRowCount + " category/ies.");
            }
        };

        new Thread(r).start();
    }

    /*
        GEOFENCE SPECIFIC
     */

    public void initGeofenceDatabaseIfNotExists(SQLiteDatabase db){

        try {
            db.execSQL(GeofenceContract.SQL_CREATE_TABLE);
        }catch(SQLException e){
            Log.e("DB", "Unable to create geofence database.");
        }
    }

    public Thread readAllGeofenceEventsFromDatabase(@Nullable final GeofenceDbCallback cacheCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Create table if not exists
                initGeofenceDatabaseIfNotExists(db);

                String[] entryProjection = {
                        GeofenceRow._ID,
                        GeofenceRow.COLUMN_NAME_TIMESTAMP,
                        GeofenceRow.COLUMN_NAME_FENCE_ID,
                        GeofenceRow.COLUMN_NAME_TYPE
                };

                String sortOrder = GeofenceRow.COLUMN_NAME_TIMESTAMP + " ASC";

                List<GeofenceEvent> results = new ArrayList<>();

                Cursor cursor = db.query(
                        GeofenceRow.TABLE_NAME,  // The table to query
                        entryProjection,         // The columns to return
                        null,                    // The columns for the WHERE clause
                        null,                    // The values for the WHERE clause
                        null,                    // don't group the rows
                        null,                    // don't filter by row groups
                        sortOrder                // The sort order
                );

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    try {
                        long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(GeofenceRow._ID));
                        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GeofenceRow.COLUMN_NAME_TIMESTAMP));
                        String fenceId = cursor.getString(cursor.getColumnIndexOrThrow(GeofenceRow.COLUMN_NAME_FENCE_ID));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(GeofenceRow.COLUMN_NAME_TYPE));

                        GeofenceEvent entry = new GeofenceEvent(itemId, timestamp, fenceId, type);
                        results.add(entry);
                    }catch(IllegalArgumentException e){

                        // If we encounter an illegal argument
                        if(cacheCallback != null) {
                            cacheCallback.onDbGeofencesFail(e.getMessage());
                        }
                        cursor.close();
                        return;
                    }

                    cursor.moveToNext();
                }
                cursor.close();

                if(cacheCallback != null) {
                    cacheCallback.onDbReadGeofencesComplete(results);
                }

                Log.d("DB", "READ REPORT: " + results.size() + " geofence events.");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public Thread readLastXGeofenceEventsFromDatabase(final int numberOfLastGeofences, @Nullable final GeofenceDbCallback cacheCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();
                // Create table if not exists
                initGeofenceDatabaseIfNotExists(db);

                String[] entryProjection = {
                        GeofenceRow._ID,
                        GeofenceRow.COLUMN_NAME_TIMESTAMP,
                        GeofenceRow.COLUMN_NAME_FENCE_ID,
                        GeofenceRow.COLUMN_NAME_TYPE
                };

                String sortOrder = GeofenceRow.COLUMN_NAME_TIMESTAMP + " DESC";

                List<GeofenceEvent> results = new ArrayList<>();

                Cursor cursor = db.query(
                        GeofenceRow.TABLE_NAME,  // The table to query
                        entryProjection,         // The columns to return
                        null,                    // The columns for the WHERE clause
                        null,                    // The values for the WHERE clause
                        null,                    // don't group the rows
                        null,                    // don't filter by row groups
                        sortOrder,                // The sort order
                        String.valueOf(numberOfLastGeofences)
                );

                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    try {
                        long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(GeofenceRow._ID));
                        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(GeofenceRow.COLUMN_NAME_TIMESTAMP));
                        String fenceId = cursor.getString(cursor.getColumnIndexOrThrow(GeofenceRow.COLUMN_NAME_FENCE_ID));
                        int type = cursor.getInt(cursor.getColumnIndexOrThrow(GeofenceRow.COLUMN_NAME_TYPE));

                        GeofenceEvent entry = new GeofenceEvent(itemId, timestamp, fenceId, type);
                        results.add(entry);
                    }catch(IllegalArgumentException e){

                        // If we encounter an illegal argument
                        if(cacheCallback != null) {
                            cacheCallback.onDbGeofencesFail(e.getMessage());
                        }
                        cursor.close();
                        return;
                    }

                    cursor.moveToNext();
                }
                cursor.close();

                if(cacheCallback != null) {
                    cacheCallback.onDbReadGeofencesComplete(results);
                }

                Log.d("DB", "READ REPORT: " + results.size() + " geofence events.");
            }
        };

        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public void createGeofenceInDatabase(@NonNull final GeofenceEvent geofenceEvent, @Nullable final GeofenceDbCallback cacheCallback) throws Exception{

        final Runnable r = new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                initGeofenceDatabaseIfNotExists(db);

                // Insert the entry
                ContentValues values = new ContentValues();
                values.put(GeofenceRow.COLUMN_NAME_TIMESTAMP, geofenceEvent.timestamp);
                values.put(GeofenceRow.COLUMN_NAME_FENCE_ID, geofenceEvent.geofenceId);
                values.put(GeofenceRow.COLUMN_NAME_TYPE, geofenceEvent.geofenceType);

                long newEntryRowId = db.insert(GeofenceRow.TABLE_NAME, null, values);

                geofenceEvent._id = newEntryRowId;

                if(cacheCallback != null) {
                    cacheCallback.onDbCreateGeofenceComplete(geofenceEvent);
                }

                Log.d("DB", "WRITE REPORT: " + (newEntryRowId == -1 ? 0 : 1) + " geofence events.");
            }
        };

        new Thread(r).start();
    }

    public void removeGeofenceEventsFromDatabase(@NonNull final GeofenceEvent entryToDelete, @Nullable final EntryCacheCallback cacheCallback) throws Exception{

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // Remove entry
                String entrySelection = GeofenceRow._ID + " LIKE ?";
                String[] entrySelectionArgs = { String.valueOf(entryToDelete._id) };
                int entryRowCount = db.delete(WorkEntryRow.TABLE_NAME, entrySelection, entrySelectionArgs);

                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }

                Log.d("DB", "DELETE REPORT: " + entryRowCount + " geofence events.");
            }
        };

        new Thread(r).start();
    }

    public void modifyGeofenceInDatabase(@NonNull final GeofenceEvent geofenceEvent, @Nullable final EntryCacheCallback cacheCallback) throws Exception {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DatabaseManager.this.getWritableDatabase();

                // New value for one column
                ContentValues values = new ContentValues();
                values.put(GeofenceRow.COLUMN_NAME_TIMESTAMP, geofenceEvent.timestamp);
                values.put(GeofenceRow.COLUMN_NAME_FENCE_ID, geofenceEvent.geofenceId);
                values.put(GeofenceRow.COLUMN_NAME_TYPE, geofenceEvent.geofenceType);

                // Which row to update, based on the ID
                String selection = GeofenceRow._ID + " LIKE ?";
                String[] selectionArgs = { String.valueOf(geofenceEvent._id) };

                int count = db.update(
                        GeofenceRow.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);


                if(cacheCallback != null) {
                    cacheCallback.onDbOperationComplete(null, NO_ERROR);
                }
                Log.d("DB", "MODIFY REPORT: " + count + " geofence events.");
            }
        };

        new Thread(r).start();
    }

    /*
        GENERAL
     */

    public void wipeDatabase() {

        Runnable r = new Runnable() {
            @Override
            public void run() {
                mContext.deleteDatabase(DatabaseManager.DATABASE_NAME);
            }
        };

        new Thread(r).start();
    }

    private void refreshEntryCache() {

        DataCacheHelper dataCacheHelper = new DataCacheHelper(mContext);
        dataCacheHelper.refreshEntryCache();
    }
}
