package data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import util.DateUtils;

public class WorkEntry implements Parcelable, Selectable, Comparable<WorkEntry>, Copyable, Restorable{

    public static final String PARCELABLE_OBJECT_IDENTIFIER = "work_entry_object_parcel";
    public static final String PARCELABLE_ARRAYLIST_IDENTIFIER = "work_entry_list_parcel";

    public static final int INVALID_INTEGER = Integer.MAX_VALUE;
    public static final long INVALID_LONG = Long.MAX_VALUE;

    // Metadata
    private long _id = INVALID_LONG;
    public long creation_time = INVALID_LONG;
    public long last_update = INVALID_LONG;                     // This gets updated on db operation
    private long reference_time = INVALID_LONG;

    // Work type related
    private String title = null;
    private String text = null;
    public int type = WorkEntryType.AUTOMATIC;

    // Work time related
    private List<WorkBlock> work_blocks = new ArrayList<>();

    // Helper for recycler views
    public boolean isSelected = false;

    // Init with default values. Set values later.
    public WorkEntry() {
        this.creation_time = System.currentTimeMillis();
        this.last_update = System.currentTimeMillis();
    }

    /**
     *
     * @param referenceTime - Day the work entry is related to
     * @param title - The title of the entry
     * @param text - The description of the entry
     * @param type - The type of the entry found in @link: WorkEntryType
     */
    public WorkEntry(final long referenceTime, @Nullable final String title, @Nullable final String text, @WorkEntryType.EntryType final int type){
        this(System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), referenceTime, title, text, type);
    }

    /**
     *  Constructor used by database created objects
     */
    public WorkEntry(long itemId, long creationTime, long lastUpdateTime, long referenceTime, @Nullable String title, @Nullable String text, @WorkEntryType.EntryType int type) {

        this._id = itemId;
        this.creation_time = creationTime;
        this.last_update = lastUpdateTime;
        this.reference_time = referenceTime;

        setTitle(title);
        setText(text);
        this.type = type;
    }

    public List<WorkBlock> getWorkBlocks(){
        return work_blocks;
    }

    public void addWorkBlock(WorkBlock block){

        // Set correct reference id, just to make sure
        block.setReferenceId(getId());
        work_blocks.add(block);
    }

    public void addAllWorkBlocks(List<WorkBlock> blocks){
        work_blocks.addAll(blocks);
    }

    public boolean removeWorkBlock(WorkBlock block){
        return work_blocks.remove(block);
    }

    public void removeAllWorkBlocks(){
        work_blocks = new ArrayList<>();
    }

    public int[] getTotalWorkBlockDuration(){

        return DateUtils.getDurationInHoursAndMinutes(getTotalWorkBlockDurationInMillies());
    }

    public long getTotalWorkBlockDurationInMinutes(){

        return TimeUnit.MILLISECONDS.toMinutes(getTotalWorkBlockDurationInMillies());
    }

    public long getTotalWorkBlockDurationInMillies(){

        long duration = 0;

        for(WorkBlock block : work_blocks){
            duration += (block.work_end - block.work_start);
        }

        return duration;
    }

    public long getDate(){
        return reference_time;
    }

    public void setDate(long timestamp) {

        // Get difference
        long timestampDifference = reference_time - timestamp;

        // Set new timestamp
        reference_time = timestamp;

        // Set all timestamp of the blocks accordingly
        for(WorkBlock block : getWorkBlocks()){
            block.work_start += timestampDifference;
        }
    }

    public @NonNull String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public @NonNull String getText() {
        return text == null ? "" : text;
    }

    public void setText(@Nullable String text) {
        this.text = text == null ? "" : text;
    }

    public void setId(long id){

        // Set new reference id for all blocks
        for(WorkBlock block : getWorkBlocks()){
            block.setReferenceId(id);
        }

        this._id = id;
    }

    public long getId(){
        return _id;
    }

    /*
        Validation
     */

    public boolean hasValidDate(){

        return reference_time != INVALID_LONG;
    }

    /*
        Restorable
     */

    @Override
    public void restore(Object object) {

        if(!(object instanceof WorkEntry)){
            throw new IllegalArgumentException("Object must me instance of WorkEntry");
        }

        this._id = ((WorkEntry) object)._id;
        this.creation_time = ((WorkEntry) object).creation_time;
        this.last_update = ((WorkEntry) object).last_update;
        this.reference_time = ((WorkEntry) object).reference_time;
        this.title = ((WorkEntry) object).title;
        this.text = ((WorkEntry) object).text;
        this.removeAllWorkBlocks();
        this.addAllWorkBlocks(((WorkEntry) object).getWorkBlocks());
    }

    /*
        Copyable
     */

    public Object copy(){
        
        WorkEntry workEntry = new WorkEntry(this._id, this.creation_time, this.last_update, this.reference_time, this.title, this.text, this.type);

        for(WorkBlock block : this.getWorkBlocks()){
            workEntry.addWorkBlock((WorkBlock) block.copy());
        }

        return workEntry;
    }

    /*
        Selectable
     */

    @Override
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean getSelected() {
        return isSelected;
    }

    /*
         Parcelable
    */
    public WorkEntry(Parcel parcel){
        this._id = parcel.readLong();
        this.creation_time = parcel.readLong();
        this.last_update = parcel.readLong();
        this.reference_time = parcel.readLong();
        this.title = parcel.readString();
        this.text = parcel.readString();
        this.type = parcel.readInt();

        WorkBlock[] tempArray = new WorkBlock[parcel.readInt()];
        tempArray = parcel.createTypedArray(WorkBlock.CREATOR);
        //parcel.readTypedArray(tempArray, WorkBlock.CREATOR);

        work_blocks = new ArrayList<>(Arrays.asList(tempArray));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(_id);
        parcel.writeLong(creation_time);
        parcel.writeLong(last_update);
        parcel.writeLong(reference_time);
        parcel.writeString(title);
        parcel.writeString(text);
        parcel.writeInt(type);

        WorkBlock[] blocks = new WorkBlock[work_blocks.size()];
        int i = 0;
        for(WorkBlock block : work_blocks){
            blocks[i++] = block;
        }

        parcel.writeInt(blocks.length);
        parcel.writeTypedArray(blocks, flags);
    }

    public static final Parcelable.Creator<WorkEntry> CREATOR =
            new Parcelable.Creator<WorkEntry>() {

                @Override
                public WorkEntry createFromParcel(Parcel source) {
                    return new WorkEntry(source);
                }

                @Override
                public WorkEntry[] newArray(int size) {
                    return new WorkEntry[size];
                }
            };

    /*
        Comparable
     */

    @Override
    public int compareTo(@NonNull WorkEntry another) {

        /**
         * Sort in desc order to have the newest one on top
         */
        if (this.reference_time < another.reference_time) {
            return 1;
        }else if(this.reference_time == another.reference_time) {
            return 0;
        }

        return -1;
    }
}
