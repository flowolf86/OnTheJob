package cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import application.OtjApplication;
import data.WorkBlock;
import data.WorkEntry;
import database.DatabaseManager;
import database.DatabaseUiCallback;
import support.AppBroadcaster;

/**
 * Created by Florian on 22.06.2015.
 */
public class EntryCache implements EntryCacheCallback {

    private static EntryCache INSTANCE = null;

    private static int mDatabaseState = DatabaseState.OUT_OF_DATE;

    private static List<WorkEntry> mWorkEntryLastValidDatabaseCopy = Collections.synchronizedList(new ArrayList<WorkEntry>());
    private static List<WorkEntry> mWorkEntryEntries = Collections.synchronizedList(new ArrayList<WorkEntry>());

    private static DatabaseManager mDbHelper = null;

    private EntryCache() { }

    private EntryCache(@NonNull Context context){
        mDbHelper = new DatabaseManager(context);

        // Initial cache filling
        try {
            mDbHelper.readEntriesFromDatabase(this);
        }catch(Exception e){
            Log.w("DB", "Unable to read data from database.");
        }
    }

    public static void init(@NonNull Context context){
        getInstance(context);
    }

    protected static EntryCache getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new EntryCache(context);
        }
        return INSTANCE;
    }

    public void setDatabaseStatusOutOfDate(){
        mDatabaseState = DatabaseState.OUT_OF_DATE;
    }

    public void setDatabaseStatusUpToDate(){
        // This does not take concurrent environments into account yet...
        mDatabaseState = DatabaseState.UP_TO_DATE;
        AppBroadcaster.sendDataCacheUpdatedBroadcast(OtjApplication.getContext());
    }

    /*
        Interaction
     */

    public List<WorkEntry> getAllWorkEntries() {
        return mWorkEntryEntries;
    }

    public List<WorkEntry> getLastValidDbCacheCopy() {
        return mWorkEntryLastValidDatabaseCopy;
    }

    public boolean updateWorkEntryReferenceInDataCache(@NonNull WorkEntry newEntry){

        int location = -1;
        for(WorkEntry oldEntry : getAllWorkEntries()){
            if(newEntry.getId() == oldEntry.getId()){
                location = getAllWorkEntries().indexOf(oldEntry);
            }
        }

        if(location != -1){
            getAllWorkEntries().set(location, newEntry);
            return true;
        }
        return false;
    }

    /*
        Entries
     */
    public void addNewEntry(@NonNull WorkEntry workEntry, @Nullable DatabaseUiCallback uiCallback){

        // Add to cache
        if(!mWorkEntryEntries.contains(workEntry)) {
            mWorkEntryEntries.add(workEntry);

            // TODO This is the exception to the rule. We add the new entry here also, though it is not yet written in the database.
            // TODO Add this ONLY When the entry has been written to db!!!
            mWorkEntryLastValidDatabaseCopy.add((WorkEntry) workEntry.copy());
        }

        // Add to database
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.createEntryInDatabase(workEntry, this, uiCallback);
        }catch(Exception e){
            if(uiCallback != null) {
                uiCallback.onDbOperationFail("[SAVE FAIL (HARD)]");
            }
            e.printStackTrace();
        }
    }

    public void modifyWorkEntry(@NonNull WorkEntry modifiedEntry, @Nullable DatabaseUiCallback uiCallback){

        boolean isIdentityVerified = updateWorkEntryReferenceInDataCache(modifiedEntry);

        // Modify in database
        try {

            if(isIdentityVerified) {
                setDatabaseStatusOutOfDate();
                mDbHelper.modifyEntryInDatabase(modifiedEntry, this, uiCallback);
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

    public void deleteWorkEntry(@NonNull WorkEntry workEntry, @Nullable DatabaseUiCallback uiCallback){

        // Delete from cache
        if(mWorkEntryEntries.contains(workEntry)){
            mWorkEntryEntries.remove(workEntry);
        }

        // Delete from db
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.removeEntryFromDatabase(workEntry, this, uiCallback);
        }catch(Exception e){
            if(uiCallback != null) {
                uiCallback.onDbOperationFail("[DELETE FAIL (HARD)]");
            }
            e.printStackTrace();
        }
    }

    public void removeAllBlocksOfEntryFromDatabase(@NonNull final WorkEntry data) {

        // Delete from cache
        // -- Do not remove the data from cache

        // Delete from db
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.removeAllBlocksOfEntryFromDatabase(this, data);
        }catch(Exception ignore){
            Log.d("DB", "Unable to delete all work blocks from work entry.");
        }
    }

    /*
        Blocks
     */

    public void addNewBlock(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock, @Nullable DatabaseUiCallback uiCallback){

        workEntry.addWorkBlock(workBlock);

        // Set the blocks reference id to the entries id for consistency reasons
        // This should never be needed in the first place!
        if(workEntry.getId() != workBlock.getReferenceId()){
            workBlock.setReferenceId(workEntry.getId());
            Log.w("CACHE", "Had to manually sync ids of entry and block! Something's not OK.");
        }

        // Add to database
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.createBlockInDatabase(workEntry, workBlock, this, uiCallback);
        }catch(Exception e){
            if(uiCallback != null) {
                uiCallback.onDbOperationFail("[SAVE FAIL (HARD)]");
            }
            e.printStackTrace();
        }
    }

    public void modifyWorkBlock(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock, @Nullable DatabaseUiCallback uiCallback){

        // Modify in cache
        // -- It already is in the cache

        // Modify in database
        try {

            if(workBlock != null) {
                setDatabaseStatusOutOfDate();
                mDbHelper.modifyBlockInDatabase(workEntry, workBlock, this, uiCallback);
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

    public void deleteWorkBlock(@NonNull WorkEntry workEntry,@NonNull WorkBlock workBlock, @Nullable DatabaseUiCallback uiCallback){

        // Delete from cache
        workEntry.getWorkBlocks().remove(workBlock);

        // Delete from db
        try {
            setDatabaseStatusOutOfDate();
            mDbHelper.removeBlockFromDatabase(workBlock, this, uiCallback);
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
            mDbHelper.readEntriesFromDatabase(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDbReadEntriesComplete(@NonNull List<WorkEntry> resultSet) {
        mWorkEntryEntries = resultSet;
        mWorkEntryLastValidDatabaseCopy.clear();

        // Copy
        for(WorkEntry entry : resultSet){
            mWorkEntryLastValidDatabaseCopy.add((WorkEntry)entry.copy());
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
