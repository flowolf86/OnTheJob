package data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.florianwolf.onthejob.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;

/**
 * Created by Florian on 24.06.2015.
 */
public class Category implements Parcelable, Selectable{

    /*
        IntDefs
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({WORK_DAY, VACATION, SICK_LEAVE, OTHER})
    public @interface CategoryMode{ }

    public static final int WORK_DAY = 1;
    public static final int VACATION = 2;
    public static final int SICK_LEAVE = 3;
    public static final int OTHER = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CATEGORY_TYPE_SYSTEM, CATEGORY_TYPE_USER})
    public @interface CategoryType{ }

    public static final int CATEGORY_TYPE_SYSTEM = 100;
    public static final int CATEGORY_TYPE_USER = 200;

    /*
        Variables & Statics
     */

    public static final int WORK_DAY_COLOR = R.color.accent;
    public static final int VACATION_COLOR = R.color.primary;
    public static final int SICK_LEAVE_COLOR = R.color.grey_300;

    public long _id = System.currentTimeMillis();
    public String name = null;
    public String description = null;
    public int color = 0;
    public byte[] icon = null;
    public @CategoryType int category_type = CATEGORY_TYPE_SYSTEM;

    // Helper for recycler views
    public boolean isSelected = false;

    private Category() { }

    public Category(long _id, String name, String description, @ColorInt int color, byte[] icon, @CategoryType int category_type){
        this._id = _id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.icon = icon;
        this.category_type = category_type;
    }

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public Category getCopy(){
        return new Category(_id, name, description, color, icon, category_type);
    }

    public @NonNull String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAllDataExcludingId(@NonNull Category newData){

        name = newData.name;
        description = newData.description;
        color = newData.color;
        icon = newData.icon;
        category_type = newData.category_type;
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
    public Category(Parcel parcel){
        this._id = parcel.readLong();
        this.name = parcel.readString();
        this.description = parcel.readString();
        this.color = parcel.readInt();
        parcel.readByteArray(icon);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(_id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeInt(color);
        parcel.writeByteArray(icon);
    }

    public static final Parcelable.Creator<Category> CREATOR =
            new Parcelable.Creator<Category>() {

                @Override
                public Category createFromParcel(Parcel source) {
                    return new Category(source);
                }

                @Override
                public Category[] newArray(int size) {
                    return new Category[size];
                }
            };

    /*
        Comparator
     */
    public static Comparator<Category> CategoryNameComperator = new Comparator<Category>() {

        public int compare(Category cat1, Category cat2) {

            String catName1 = cat1.name.toUpperCase();
            String catName2 = cat2.name.toUpperCase();

            //ascending order
            return catName1.compareTo(catName2);

            //descending order
            //return catName2.compareTo(catName1);
        }
    };
}
