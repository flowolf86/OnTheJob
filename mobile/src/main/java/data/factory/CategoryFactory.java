package data.factory;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.florianwolf.onthejob.R;

import java.util.Random;

import application.OtjApplication;
import data.Category;

public class CategoryFactory {

    private static Category mWorkDayCategory = null;
    private static Category mVacationCategory = null;
    private static Category mSickLeaveCategory = null;

    private static @NonNull Resources getResources(){
        return OtjApplication.getAppResources();
    }

    public static @NonNull Category getWorkDayCategory(){
        if(mWorkDayCategory == null) {
            mWorkDayCategory = new Category(Category.WORK_DAY, getResources().getString(R.string.work_day_category), getResources().getString(R.string.work_day_category_description), ContextCompat.getColor(OtjApplication.getContext(), Category.WORK_DAY_COLOR), null, Category.CATEGORY_TYPE_SYSTEM);
        }
        return mWorkDayCategory;
    }

    public static @NonNull Category getVacationCategory(){
        if(mVacationCategory == null) {
            mVacationCategory = new Category(Category.VACATION, getResources().getString(R.string.vacation_category), getResources().getString(R.string.vacation_category_description), ContextCompat.getColor(OtjApplication.getContext(), Category.VACATION_COLOR), null, Category.CATEGORY_TYPE_SYSTEM);
        }
        return mVacationCategory;
    }

    public static @NonNull Category getSickLeaveCategory(){
        if(mSickLeaveCategory == null) {
            mSickLeaveCategory = new Category(Category.SICK_LEAVE, getResources().getString(R.string.sick_leave_category), getResources().getString(R.string.sick_leave_category_description), ContextCompat.getColor(OtjApplication.getContext(), Category.SICK_LEAVE_COLOR), null, Category.CATEGORY_TYPE_SYSTEM);
        }
        return mSickLeaveCategory;
    }

    public static @NonNull Category getNewEmtpyUserCategory(){
        Random rnd = new Random();
        @ColorInt int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return new Category(System.currentTimeMillis(), getResources().getString(R.string.empty_category), getResources().getString(R.string.empty_category_description), color, null, Category.CATEGORY_TYPE_USER);
    }
}
