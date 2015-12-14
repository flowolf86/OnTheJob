package data;

import android.support.annotation.IntDef;

import com.florianwolf.onthejob.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WorkEntryType {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({AUTOMATIC, MANUAL})
    public @interface EntryType{ }

    public static final int AUTOMATIC = 100;
    public static final int MANUAL = 110;

    /*
        Other
     */
    public static final int MANUAL_ICON_ID = R.drawable.fw_work_entry;
    public static final int CHECK_ICON_ID = R.drawable.ic_check_black_24dp;

    public static final int VACATION_ICON_ID = R.drawable.fw_vacation_3;
    public static final int SICK_LEAVE_ICON_ID = R.drawable.fw_sick_leave_2;
}
