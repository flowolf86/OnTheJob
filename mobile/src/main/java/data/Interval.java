package data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import cache.DataCacheHelper;
import database.DatabaseUiCallback;
import util.TextUtils;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 13/11/15.
 *
 * This class defines an interval of time in which something happens (e.g. a vacation, sick-leave)
 */
public class Interval implements Parcelable, Selectable, Restorable, Copyable, Comparable<Interval> {

    private long _id;
    private long mCreationTimestamp;
    private long mLastUpdateTimestamp;
    private String mTitle;
    private String mDescription;
    private long mStartDate;
    private long mEndDate;
    private Category mCategory;

    private boolean mIsSelected = false;

    public Interval(@NonNull String title, @NonNull String description, long startDate, long endDate, @NonNull Category category) {

        setId(System.currentTimeMillis()); // Temp ID
        setCreationTimestamp(System.currentTimeMillis());
        setObjectUpdated();
        setTitle(title);
        setDescription(description);
        setStartDate(startDate);
        setEndDate(endDate);
        setCategory(category);
    }

    // From database
    public Interval(long id, long creationTimestamp, long lastUpdated, @NonNull String title, @NonNull String description, long startDate, long endDate, @NonNull Category category) {

        setId(id);
        setCreationTimestamp(creationTimestamp);
        setLastUpdatedManually(lastUpdated);
        setTitle(title);
        setDescription(description);
        setStartDate(startDate);
        setEndDate(endDate);
        setCategory(category);
    }

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public @NonNull String getTitle() {
        return mTitle == null ? "" : mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
        setObjectUpdated();
    }

    public @NonNull String getDescription() {
        return mDescription == null ? "" : mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
        setObjectUpdated();
    }

    public long getStartDate() {
        return mStartDate;
    }

    public void setStartDate(long startDate) {
        this.mStartDate = startDate;
        setObjectUpdated();
    }

    public long getEndDate() {
        return mEndDate;
    }

    public void setEndDate(long endDate) {
        this.mEndDate = endDate;
        setObjectUpdated();
    }

    public @NonNull Category getCategory() {
        return mCategory;
    }

    public void setCategory(@NonNull Category category) {
        this.mCategory = category;
        setObjectUpdated();
    }

    public boolean hasSameCategory(Category category) {
        return mCategory.getId() == category.getId();
    }

    public long getCreationTimestamp() {
        return mCreationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.mCreationTimestamp = creationTimestamp;
    }

    private long getLastUpdated() {
        return mLastUpdateTimestamp;
    }

    private void setLastUpdatedManually(long timestamp) {
        this.mLastUpdateTimestamp = timestamp;
    }

    private void setObjectUpdated() {
        this.mLastUpdateTimestamp = System.currentTimeMillis();
    }

    /*
        Copyable
     */

    public Object copy(){

        return new Interval(getId(), getCreationTimestamp(), getLastUpdated(), getTitle(), getDescription(), getStartDate(), getEndDate(), getCategory());
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

    /**
     * Stores the edited object in database
     *
     * Depending on the edit we did we either modify the existing entries or delete the old
     * and create a bunch of new entries
     */
    public void store(@NonNull Context context, @Nullable DatabaseUiCallback callback){

        final DataCacheHelper dataCacheHelper = new DataCacheHelper(context);
        final List<Interval> intervalList = dataCacheHelper.getAllIntervals();
        boolean isEdit = false;
        for(Interval interval : intervalList){
            if(interval.getId() == getId()){
                isEdit = true;
                break;
            }
        }

        if(isEdit){
            dataCacheHelper.modifyInterval(this, callback);
        } else {
            dataCacheHelper.addNewInterval(this, callback);
        }
    }

    /*
         Parcelable
    */
    public Interval(Parcel parcel){
        setId(parcel.readLong());
        setCreationTimestamp(parcel.readLong());
        setLastUpdatedManually(parcel.readLong());
        setTitle(parcel.readString());
        setDescription(parcel.readString());
        setStartDate(parcel.readLong());
        setEndDate(parcel.readLong());
        setCategory((Category) parcel.readParcelable(Category.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(getId());
        parcel.writeLong(getCreationTimestamp());
        parcel.writeLong(getLastUpdated());
        parcel.writeString(getTitle());
        parcel.writeString(getDescription());
        parcel.writeLong(getStartDate());
        parcel.writeLong(getEndDate());
        parcel.writeParcelable(getCategory(), flags);
    }

    public static final Parcelable.Creator<Interval> CREATOR =
            new Parcelable.Creator<Interval>() {

                @Override
                public Interval createFromParcel(Parcel source) {
                    return new Interval(source);
                }

                @Override
                public Interval[] newArray(int size) {
                    return new Interval[size];
                }
            };

    /*
        Restorable
     */

    @Override
    public void restore(Object object) {

        if(!(object instanceof Interval)){
            throw new IllegalArgumentException("Object must me instance of Interval");
        }

        this.setId(((Interval) object).getId());
        this.setCreationTimestamp(((Interval) object).getCreationTimestamp());
        this.setLastUpdatedManually(((Interval) object).getLastUpdated());
        this.setTitle(((Interval) object).getTitle());
        this.setDescription(((Interval) object).getDescription());
        this.setStartDate(((Interval) object).getStartDate());
        this.setEndDate(((Interval) object).getEndDate());
        this.setCategory(((Interval) object).getCategory());
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

    /*
        Comparable
     */

    @Override
    public int compareTo(@NonNull Interval another) {

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
}
