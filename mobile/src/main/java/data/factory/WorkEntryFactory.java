package data.factory;

import android.content.Context;
import android.support.annotation.NonNull;

import com.florianwolf.onthejob.R;

import data.WorkEntry;
import data.WorkEntryType;
import util.DateUtils;

public class WorkEntryFactory {

    public static WorkEntry buildNewEmptyManualWorkEntry(){

        return new WorkEntry(
                WorkEntry.INVALID_LONG,
                null,
                "",
                WorkEntryType.MANUAL);
    }

    public static WorkEntry buildNewAutomaticWorkEntry(Context context, long timestamp, @NonNull String geofenceString){

        return new WorkEntry(
                timestamp,
                context.getString(R.string.automatic_work_entry_title, DateUtils.getDayOfWeekString(System.currentTimeMillis())),
                context.getString(R.string.automatic_work_entry_text, geofenceString),
                WorkEntryType.AUTOMATIC);
    }

    public static WorkEntry buildNewEmptyTodaySickDayWorkEntry(Context context){

        return new WorkEntry(
                System.currentTimeMillis(),
                context.getString(R.string.sick_day_entry_title, DateUtils.getDayOfWeekString(System.currentTimeMillis())),
                context.getString(R.string.sick_day_entry_description),
                WorkEntryType.MANUAL);
    }

    public static WorkEntry buildNewEmptyTodayVacationDayWorkEntry(Context context){

        return new WorkEntry(
                System.currentTimeMillis(),
                context.getString(R.string.vacation_day_entry_title, DateUtils.getDayOfWeekString(System.currentTimeMillis())),
                context.getString(R.string.vacation_day_entry_description),
                WorkEntryType.MANUAL);
    }
}
