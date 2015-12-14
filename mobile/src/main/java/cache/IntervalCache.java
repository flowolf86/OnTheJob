package cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import application.OtjApplication;
import data.Interval;
import database.DatabaseManager;
import database.DatabaseUiCallback;
import support.AppBroadcaster;

/**
 * Created by Florian on 22.06.2015.
 */
public class IntervalCache implements IntervalCacheCallback {

    private static IntervalCache INSTANCE = null;

    private static int mDatabaseState = DatabaseState.OUT_OF_DATE;

    private static List<Interval> mIntervalLastValidDatabaseCopy = Collections.synchronizedList(new ArrayList<Interval>());
    private static List<Interval> mIntervalEntries = Collections.synchronizedList(new ArrayList<Interval>());

    private static DatabaseManager mDbHelper = null;

    private IntervalCache() { }

    private IntervalCache(@NonNull Context context){

        mDbHelper = new DatabaseManager(context);

        // Initial cache filling
        try {
            mDbHelper.readIntervalsFromDatabase(this);
        }catch(Exception e){
            Log.w("DB", "Unable to read data from database.");
        }
    }

    public static void init(@NonNull Context context){
        getInstance(context);
    }

    protected static IntervalCache getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new IntervalCache(context);
        }
        return INSTANCE;
    }

    public void setDatabaseStatusOutOfDate(){
        mDatabaseState = DatabaseState.OUT_OF_DATE;
    }

    public void setDatabaseStatusUpToDate(){
        // This does not take concurrent environments into account yet...
        mDatabaseState = DatabaseState.UP_TO_DATE;
        AppBroadcaster.sendDataCacheUpdatedBroadcast(OtjApplication.getContext());  //TODO
    }

    /*
        Interaction
     */

    public List<Interval> getAllIntervals() {
        return mIntervalEntries;
    }

    public List<Interval> getLastValidDbCacheCopy() {
        return mIntervalLastValidDatabaseCopy;
    }

    public boolean updateIntervalReferenceInDataCache(@NonNull Interval newInterval){

        int location = -1;
        for(Interval oldInterval : getAllIntervals()){
            if(newInterval.getId() == oldInterval.getId()){
                location = getAllIntervals().indexOf(oldInterval);
            }
        }

        if(location != -1){
            getAllIntervals().set(location, newInterval);
            return true;
        }
        return false;
    }

    /*
        Intervals
     */
    public void addInterval(@NonNull Interval interval, @Nullable DatabaseUiCallback uiCallback){

        // Add to cache
        if(!getAllIntervals().contains(interval)) {
            getAllIntervals().add(interval);

            // TODO This is the exception to the rule. We add the new entry here also, though it is not yet written in the database.
            // TODO Add this ONLY When the entry has been written to db!!!
            mIntervalLastValidDatabaseCopy.add((Interval) interval.copy());
        }

        // Add to database
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.createIntervalInDatabase(interval, this, uiCallback);
        }catch(Exception e){
            if(uiCallback != null) {
                uiCallback.onDbOperationFail("[SAVE FAIL (HARD)]");
            }
            e.printStackTrace();
        }
    }

    public void modifyInterval(@NonNull Interval modifiedInterval, @Nullable DatabaseUiCallback uiCallback){

        boolean isIdentityVerified = updateIntervalReferenceInDataCache(modifiedInterval);

        // Modify in database
        try {

            if(isIdentityVerified) {
                setDatabaseStatusOutOfDate();
                mDbHelper.modifyIntervalInDatabase(modifiedInterval, this, uiCallback);
            }else{
                Log.w("CACHE", "Could not verify the identity of the entry.");
            }
        }catch(Exception e){
            if(uiCallback != null) {
                uiCallback.onDbOperationFail("[MODIFY FAIL (HARD)]");
            }
            e.printStackTrace();
        }
    }

    public void deleteInterval(@NonNull Interval interval, @Nullable DatabaseUiCallback uiCallback){

        // Delete from cache
        if(mIntervalEntries.contains(interval)){
            mIntervalEntries.remove(interval);
        }

        // Delete from db
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.removeIntervalFromDatabase(interval, this, uiCallback);
        }catch(Exception e){
            if(uiCallback != null) {
                uiCallback.onDbOperationFail("[DELETE FAIL (HARD)]");
            }
            e.printStackTrace();
        }
    }

    /*
        Callbacks
     */

    @Override
    public void onDbOperationComplete(String msg, int errorId) {

        // Something modified our database. We need to update the cache.
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.readIntervalsFromDatabase(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDbReadIntervalsComplete(@NonNull List<Interval> resultSet) {

        Log.d("DB", "Read "+resultSet.size() + " intervals!");

        mIntervalEntries = resultSet;
        mIntervalLastValidDatabaseCopy.clear();

        // Copy
        for(Interval interval : resultSet){
            mIntervalLastValidDatabaseCopy.add((Interval)interval.copy());
        }

        setDatabaseStatusUpToDate();
    }

    @Override
    public void onDbWiped() {
        setDatabaseStatusUpToDate();
    }

    /*
        Inner class
     */
    private static class DatabaseState {
        public static int UP_TO_DATE = 0;
        public static int OUT_OF_DATE = 1;
    }
}
