package data._inactive;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import application.OtjApplication;
import cache.DataCacheHelper;
import data.Category;
import data.Selectable;
import data.WorkEntry;
import data.WorkEntryType;
import data.helper.WorkItemHelper;
import database.DatabaseUiCallback;
import util.TextUtils;

/**
 * Author:  Florian Wolf
 * Email:   florian.wolf@maibornwolff.de
 * on 13/11/15.
 *
 * This container is used for holding multiple WorkEntries e.g. due to a planned vacation or sick leave
 */
public class WorkEntryContainer implements Parcelable, Comparable<WorkEntryContainer>, Selectable {

    // We change nothing => NO_EDIT
    static final int NO_EDIT = -1;

    // We change the dates => New  entries => DEEP_EDIT
    static final int DEEP_EDIT = 0;

    // We change the title / description => No new entries => FLAT_EDIT
    static final int FLAT_EDIT = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NO_EDIT, DEEP_EDIT, FLAT_EDIT})
    @interface IModificationState { }

    // These entries will be removed and replace if we modify the containers data
    private List<WorkEntry> mOriginalEntries = new ArrayList<>();

    private List<WorkEntry> mEntries = new ArrayList<>();
    private Category mEntriesCategory = null;

    private String mTitle;
    private String mDescription;
    private long mStartDate;
    private long mEndDate;

    private boolean mIsSelected = false;

    private @IModificationState int mModificationState = NO_EDIT;

    public WorkEntryContainer(@Nullable List<WorkEntry> entries, @NonNull Category category) {
        setData(entries);
        setCopyData();
        this.mEntriesCategory = category;

        // Set entry container data according to the first object
        if(getData().size() > 0){

            mTitle = getData().get(0).getTitle();
            mDescription = getData().get(0).getText();
            mEndDate = getData().get(0).getDate();
            mStartDate = getData().get(getData().size()-1).getDate();
        } else {

            // Set default start today and end tomorrow
            DateTime dateTime = new DateTime(System.currentTimeMillis());

            // Only modify end date via method to not modify work entry data twice
            mStartDate = dateTime.getMillis();
            modifyEndDate(dateTime.plusDays(1).getMillis());
        }
    }

    public @NonNull List<WorkEntry> getData(){
        return mEntries;
    }

    public void setData(@Nullable List<WorkEntry> entries){
        mEntries = entries == null ? new ArrayList<WorkEntry>() : entries;
    }

    public void addEntry(@NonNull WorkEntry entry){
        mEntries.add(entry);
    }

    public void removeEntry(@NonNull WorkEntry entry){
        mEntries.remove(entry);
    }

    public void modifyTitle(@Nullable String title){

        mTitle = title;

        for(WorkEntry entry : mEntries){
            entry.setTitle(title);
        }

        setModificationState(FLAT_EDIT);
    }

    public void modifyDescription(@Nullable String description){

        mDescription = description;

        for(WorkEntry entry : mEntries){
            entry.setText(description);
        }

        setModificationState(FLAT_EDIT);
    }

    public boolean modifyStartDate(long startDate){

        mStartDate = startDate;

        // I thought about fiddling around with the work entries dates here but ultimately descided that we can
        // take the performace hit to ensure that our data is 100% correct
        createAllWorkEntries();

        setModificationState(DEEP_EDIT);
        return true;
    }

    public boolean modifyEndDate(long endDate){

        mEndDate = endDate;

        // See #modifyStartDate for reference
        createAllWorkEntries();

        setModificationState(DEEP_EDIT);
        return true;
    }

    public @Nullable String getTitle(){
        return mTitle;
    }

    public @Nullable String getDescription(){
        return mDescription;
    }

    public long getStartDate(){
        return mStartDate;
    }

    public long getEndDate(){
        return mEndDate;
    }

    public @NonNull Category getCategory(){
        return mEntriesCategory;
    }

    /*
        Validation
     */

    public boolean hasValidDates(){
        return getStartDate() < getEndDate();
    }

    public boolean hasValidTitle() {
        return TextUtils.isValidText(getTitle());
    }

    /*
        Logic
     */

    private void setModificationState(@IModificationState int state) {

        // If we're at deep already, stay deep!
        if(mModificationState != DEEP_EDIT) {
            mModificationState = state;
        }
    }

    private void createAllWorkEntries() {

        // Nullify old date
        setData(null);

        // Create as many new work entries as needed
        DateTime dateTime = new DateTime(mStartDate);
        while(mEndDate >= dateTime.getMillis()){

            final WorkEntry workEntry = new WorkEntry(dateTime.getMillis(), mTitle, mDescription, WorkEntryType.AUTOMATIC);
            workEntry.addWorkBlock(WorkItemHelper.generateAutoCategoryWorkBlock(workEntry, dateTime.getMillis(), getCategory(), OtjApplication.getContext()));
            addEntry(workEntry);
            dateTime = dateTime.plusDays(1);
        }
    }

    /*
        Consistency
     */

    private void setCopyData() {

        clearCopyData();
        for(WorkEntry entry : mEntries){
            mOriginalEntries.add((WorkEntry) entry.copy());
        }
    }

    private void clearCopyData() {

        mOriginalEntries.clear();
    }

    /**
     * Stores the edited object in database
     *
     * Depending on the edit we did we either modify the existing entries or delete the old
     * and create a bunch of new entries
     */
    public void store(@NonNull Context context, @Nullable DatabaseUiCallback callback){

        DataCacheHelper dataCacheHelper = new DataCacheHelper(context);

        switch (mModificationState){
            case NO_EDIT:
                break;
            case FLAT_EDIT:
                for(WorkEntry entry : mEntries) {
                    dataCacheHelper.modifyWorkEntry(entry, callback);
                }
                break;
            case DEEP_EDIT:
                for(WorkEntry entry : mOriginalEntries){
                    dataCacheHelper.deleteWorkEntry(entry, callback);
                }
                for(WorkEntry entry : mEntries){
                    dataCacheHelper.addNewEntry(entry, callback);
                }
                break;
            default:
                break;
        }
    }

    /*
         Parcelable
    */
    public WorkEntryContainer(Parcel parcel){

        WorkEntry[] tempArray = new WorkEntry[parcel.readInt()];
        tempArray = parcel.createTypedArray(WorkEntry.CREATOR);

        mEntries = new ArrayList<>(Arrays.asList(tempArray));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

        WorkEntry[] entries = new WorkEntry[mEntries.size()];
        int i = 0;
        for(WorkEntry entry : mEntries){
            entries[i++] = entry;
        }

        parcel.writeInt(entries.length);
        parcel.writeTypedArray(entries, flags);
    }

    public static final Parcelable.Creator<WorkEntryContainer> CREATOR =
            new Parcelable.Creator<WorkEntryContainer>() {

                @Override
                public WorkEntryContainer createFromParcel(Parcel source) {
                    return new WorkEntryContainer(source);
                }

                @Override
                public WorkEntryContainer[] newArray(int size) {
                    return new WorkEntryContainer[size];
                }
            };

    /*
        Comparable
     */

    @Override
    public int compareTo(@NonNull WorkEntryContainer another) {

        /**
         * Sort in desc order to have the newest one on top
         */
        if (this.getStartDate() < another.getStartDate()) {
            return 1;
        }else if(this.getStartDate() == another.getStartDate()) {
            return 0;
        }

        return -1;
    }

    /*
        Selectable
     */

    @Override
    public void setSelected(boolean selected) {
        this.mIsSelected = selected;
    }

    @Override
    public boolean getSelected() {
        return this.mIsSelected;
    }
}
