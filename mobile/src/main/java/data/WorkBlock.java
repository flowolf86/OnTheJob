package data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.florianwolf.onthejob.R;

import java.util.List;

import cache.CategoryCacheHelper;
import data.factory.CategoryFactory;
import util.ColorUtils;
import util.TextUtils;

public class WorkBlock implements Parcelable, Selectable, Comparable<WorkBlock>, Copyable, Restorable {

    public static final String PARCELABLE_IDENTIFIER = "work_block_parcel";
    public static final int INVALID_INTEGER = Integer.MAX_VALUE;
    public static final long INVALID_LONG = Long.MAX_VALUE;

    // Metadata
    public long _id = INVALID_LONG;
    private long _reference_id = 0;
    public long creation_time = INVALID_LONG;
    public long last_update = INVALID_LONG;

    // Work time related
    private long work_start = INVALID_LONG;
    private long work_end = INVALID_LONG;

    // Work subject related
    private String title = null;
    private String text = null;
    public long _category_reference_id = Category.WORK_DAY;

    // Helper for recycler views
    public boolean isSelected = false;
    private Category categoryReference = null;

    // Changed
    public boolean hasChanged = false;

    // Init with default values. Set values later.
    public WorkBlock(long _referenceId) {
        this.creation_time = System.currentTimeMillis();
        this.last_update = System.currentTimeMillis();

        // No block shall exist without an entry
        this._reference_id = _referenceId;
    }

    public WorkBlock(final long workStart, final long workEnd, final String title, final String text, final int _category_reference_id, long _referenceId) {
        this(System.currentTimeMillis(), System.currentTimeMillis(), System.currentTimeMillis(), workStart, workEnd, title, text, _category_reference_id, _referenceId);
    }

    public WorkBlock(long itemId, long creationTime, long lastUpdateTime, long workStart, long workEnd, String title, String text, long _category_reference_id, long _referenceId) {

        this._id = itemId;
        this.creation_time = creationTime;
        this.last_update = lastUpdateTime;

        setWorkStart(workStart);
        setWorkEnd(workEnd);
        setTitle(title);
        setText(text);

        this._category_reference_id = _category_reference_id;
        this._reference_id = _referenceId;

        setCategoryReference();
        setChanged(false);
    }

    /*
        Getter and Setter
     */

    public long getWorkStart() {
        return work_start;
    }

    public void setWorkStart(long work_start) {
        if(work_start != getWorkStart()){
            setChanged(true);
        }
        this.work_start = work_start;
    }

    public long getWorkEnd() {
        return work_end;
    }

    public void setWorkEnd(long work_end) {
        if(work_end != getWorkEnd()){
            setChanged(true);
        }
        this.work_end = work_end;
    }

    public @NonNull String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(@NonNull String title) {
        if(!getTitle().equals(title)){
            setChanged(true);
        }
        this.title = title;
    }

    public @NonNull String getText() {
        return text == null ? "" : text;
    }

    public void setText(@Nullable String text) {
        if(!getText().equals(text)){
            setChanged(true);
        }
        this.text = text == null ? "" : text;
    }

    public void setChanged(boolean hasChanged){
        this.hasChanged = hasChanged;
    }


    public boolean hasChanged(){
        return hasChanged;
    }

    /*
        Helper
     */

    public long getVacationMillies(){
        if(_category_reference_id == Category.VACATION){
            return work_end - work_start;
        }
        return 0L;
    }

    public long getSickLeaveMillies(){
        if(_category_reference_id == Category.SICK_LEAVE){
            return work_end - work_start;
        }
        return 0L;
    }

    /*
        Restorable
     */

    @Override
    public void restore(Object object) {

        if(!(object instanceof WorkBlock)){
            throw new IllegalArgumentException("Object must be instance of WorkBlock");
        }

        this._id = ((WorkBlock) object)._id;
        this.creation_time = ((WorkBlock) object).creation_time;
        this.last_update = ((WorkBlock) object).last_update;
        this.work_start = ((WorkBlock) object).work_start;
        this.work_end = ((WorkBlock) object).work_end;
        this.title = ((WorkBlock) object).title;
        this.text = ((WorkBlock) object).text;
        this._category_reference_id = ((WorkBlock) object)._category_reference_id;
        this._reference_id = ((WorkBlock) object)._reference_id;
    }

    /*
        Copyable
     */

    public Object copy(){

        return new WorkBlock(this._id, this.creation_time, this.last_update, this.work_start, this.work_end, this.title, this.text, this._category_reference_id, this._reference_id);
    }

    /*
        Validation
     */

    public boolean isInitial(){

        return work_start == INVALID_LONG && work_end == INVALID_LONG && _reference_id == INVALID_LONG && !TextUtils.isValidText(getTitle()) && !TextUtils.isValidText(getText());
    }

    public boolean isComplete(){

        return work_start != INVALID_LONG && work_end != INVALID_LONG && _reference_id != INVALID_LONG;
    }

    public boolean hasValidDates(){

        return work_start != INVALID_LONG && work_end != INVALID_LONG && work_end > work_start;
    }

    // I'm not proud of that... Should have linked the block and category better.
    public void setCategoryReference(){

        CategoryCacheHelper helper = CategoryCacheHelper.getInstance();
        List<Category> categories = helper.getCategories();

        for(Category category : categories){
            if(category._id == this._category_reference_id){
                categoryReference = category;
                break;
            }
        }
    }

    public void setCategory(@NonNull Category category){
        this.hasChanged = true;
        categoryReference = category;
        _category_reference_id = (int) category._id;
    }

    // If no reference has been found (e.g. the user deleted the category)
    // we return the default Category.WORK_DAY
    public Category getCategory(){
        return categoryReference == null ? CategoryFactory.getWorkDayCategory() : categoryReference;
    }

    public long getReferenceId() {
        return _reference_id;
    }

    /**
     * Use with extreme care. Only use when you know
     * what you're doing.
     *
     * @param _reference_id
     */
    public void setReferenceId(long _reference_id) {
        this._reference_id = _reference_id;
    }

    public int getIconId(){

        int iconId = -1;
        if(_category_reference_id == Category.WORK_DAY) {
            iconId = R.drawable.fw_category_work_day;
        }
        if(_category_reference_id == Category.VACATION) {
            iconId = R.drawable.fw_vacation_3;
        }
        if(_category_reference_id == Category.SICK_LEAVE) {
            iconId = R.drawable.fw_sick_leave_2;
        }

        if(iconId == -1) {
            // TODO This is the default right now
            boolean isDarkColor = ColorUtils.isDarkColor(getCategory().color);
            iconId = isDarkColor ? R.drawable.fw_category_default : R.drawable.fw_category_default; //TODO
        }

        return iconId;
    }

    /*
        Parcelable
     */
    public WorkBlock(Parcel parcel){
        this._id = parcel.readLong();
        this._reference_id = parcel.readLong();
        this.creation_time = parcel.readLong();
        this.last_update = parcel.readLong();
        this.work_start = parcel.readLong();
        this.work_end = parcel.readLong();
        this.title = parcel.readString();
        this.text = parcel.readString();
        this._category_reference_id = parcel.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(_id);
        parcel.writeLong(_reference_id);
        parcel.writeLong(creation_time);
        parcel.writeLong(last_update);
        parcel.writeLong(work_start);
        parcel.writeLong(work_end);
        parcel.writeString(title);
        parcel.writeString(text);
        parcel.writeLong(_category_reference_id);
    }

    public static final Parcelable.Creator<WorkBlock> CREATOR =
            new Parcelable.Creator<WorkBlock>() {

                @Override
                public WorkBlock createFromParcel(Parcel source) {
                    return new WorkBlock(source);
                }

                @Override
                public WorkBlock[] newArray(int size) {
                    return new WorkBlock[size];
                }
            };

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
        Comparable
     */
    @Override
    public int compareTo(WorkBlock another) {

        /**
         * Sort in desc order to have the newest one on top
         */
        if (this.work_start < another.work_start) {
            return 1;
        }else if(this.work_start == another.work_start) {
            return 0;
        }

        return -1;
    }
}
