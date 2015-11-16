package data.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.florianwolf.onthejob.R;

import java.util.concurrent.TimeUnit;

import configuration.WorkConfiguration;
import data.Category;
import data.WorkBlock;
import data.WorkEntry;
import data.factory.WorkBlockFactory;
import data.factory.WorkEntryFactory;
import data.manager.SharedPreferencesManager;

/**
 * Created by Florian on 29.06.2015.
 */
public class WorkItemHelper {

    /**
     * This returns a new sick day entry for today.
     * If an entry for today is already saved it will add a work block with the remaining time of your daily
     * workload to the work entry.
     *
     * @param todayEntry the entry for today. if null new entry including the sick block is being created
     * @param context an android context
     * @return
     */
    public static WorkEntry generateSickDayWorkEntryOrWorkBlock(@Nullable WorkEntry todayEntry, @NonNull Context context){

        long dailyWorkLoad = getDailyWorkload(context);

        if(todayEntry != null){
            long workBlockDuration = todayEntry.getTotalWorkBlockDurationInMillies();

            WorkBlock workBlock;
            if(workBlockDuration >= dailyWorkLoad){
                // If we have already worked more then the daily work load we create a block with length 0
                workBlock = WorkBlockFactory.buildNewSickWorkBlock(context, todayEntry, 0);
            }else{
                workBlock = WorkBlockFactory.buildNewSickWorkBlock(context, todayEntry, dailyWorkLoad - workBlockDuration);
            }
            todayEntry.addWorkBlock(workBlock);
            return todayEntry;
        }

        WorkEntry workEntry = WorkEntryFactory.buildNewEmptyTodaySickDayWorkEntry(context);
        WorkBlock workBlock = WorkBlockFactory.buildNewSickWorkBlock(context, workEntry, dailyWorkLoad);
        workEntry.addWorkBlock(workBlock);
        return workEntry;
    }

    /**
     * This returns a new vacation day entry for today.
     * If an entry for today is already saved it will add a work block with the remaining time of your daily
     * workload to the work entry.
     *
     * @param todayEntry the entry for today. if null new entry including the vacation block is being created
     * @param context an android context
     * @return
     */
    public static WorkEntry generateVacationDayWorkEntryOrWorkBlock(@Nullable WorkEntry todayEntry, @NonNull Context context){

        long dailyWorkLoad = getDailyWorkload(context);

        if(todayEntry != null){
            long workBlockDuration = todayEntry.getTotalWorkBlockDurationInMillies();

            WorkBlock workBlock;
            if(workBlockDuration >= dailyWorkLoad){
                // If we have already worked more then the daily work load we create a block with length 0
                workBlock = WorkBlockFactory.buildNewVacationWorkBlock(context, todayEntry, 0);
            }else{
                workBlock = WorkBlockFactory.buildNewVacationWorkBlock(context, todayEntry, dailyWorkLoad - workBlockDuration);
            }
            todayEntry.addWorkBlock(workBlock);
            return todayEntry;
        }

        final WorkEntry workEntry = WorkEntryFactory.buildNewEmptyTodayVacationDayWorkEntry(context);
        final WorkBlock workBlock = WorkBlockFactory.buildNewVacationWorkBlock(context, workEntry, dailyWorkLoad);
        workEntry.addWorkBlock(workBlock);
        return workEntry;
    }

    /**
     *
     * @param workEntry a work entry. if null a new entry for today is being created automatically
     * @param context an android context
     * @return
     */
    public static WorkBlock generateAutoCategoryWorkBlock(@NonNull WorkEntry workEntry, long workBlockStartTime, @NonNull Category category, @NonNull Context context){

        long dailyWorkLoad = getDailyWorkload(context);

        final WorkBlock workBlock = WorkBlockFactory.buildNewEmptyWorkBlock(workEntry);
        workBlock.setCategory(category);

        if(category.getId() == Category.VACATION){
            workBlock.setTitle(context.getString(R.string.vacation_day_block_title));
            workBlock.setTitle(context.getString(R.string.vacation_day_block_description));
        }

        if(category.getId() == Category.SICK_LEAVE){
            workBlock.setTitle(context.getString(R.string.sick_day_block_title));
            workBlock.setText(context.getString(R.string.sick_day_block_description));
        }

        workBlock.work_start = workBlockStartTime;
        workBlock.work_end = workBlockStartTime + dailyWorkLoad;

        return workBlock;
    }

    private static int getWorkLoad(SharedPreferencesManager pMan){
        return pMan.get(SharedPreferencesManager.ID_WORKLOAD, WorkConfiguration.DEFAULT_WEEKLY_WORKLOAD);
    }

    private static int getWorkDays(SharedPreferencesManager pMan){
        return pMan.get(SharedPreferencesManager.ID_WORK_DAYS, WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS);
    }

    private static long getDailyWorkload(Context context){
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        return TimeUnit.HOURS.toMillis(getWorkLoad(sharedPreferencesManager) / getWorkDays(sharedPreferencesManager));
    }
}
