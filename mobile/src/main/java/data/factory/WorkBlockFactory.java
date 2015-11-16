package data.factory;

import android.content.Context;

import com.florianwolf.onthejob.R;

import data.Category;
import data.WorkBlock;
import data.WorkEntry;

/**
 * Created by Florian on 29.06.2015.
 */
public class WorkBlockFactory {

    public static WorkBlock buildNewEmptyWorkBlock(WorkEntry workEntry){

        return new WorkBlock(WorkBlock.INVALID_LONG, WorkBlock.INVALID_LONG, null, null, Category.WORK_DAY, workEntry.getId());
    }

    public static WorkBlock buildNewSickWorkBlock(Context context, WorkEntry workEntry, long sickDuration){

        long now = System.currentTimeMillis();
        return new WorkBlock(now, now+sickDuration, context.getString(R.string.sick_day_block_title), context.getString(R.string.sick_day_block_description), Category.SICK_LEAVE, workEntry.getId());
    }

    public static WorkBlock buildNewVacationWorkBlock(Context context, WorkEntry workEntry, long vacationDuration){

        long now = System.currentTimeMillis();
        return new WorkBlock(now, now+vacationDuration, context.getString(R.string.vacation_day_block_title), context.getString(R.string.vacation_day_block_description), Category.VACATION, workEntry.getId());
    }
}
