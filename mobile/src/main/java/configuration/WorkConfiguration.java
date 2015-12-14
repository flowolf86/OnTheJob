package configuration;

/**
 * Created by Florian on 30.06.2015.
 */
public class WorkConfiguration {

    public static final int DEFAULT_WEEKLY_WORKLOAD = 40;
    public static final int DEFAULT_WEEKLY_MAX_WORKLOAD = 80;

    public static final int DEFAULT_WEEKLY_WORK_DAYS = 5;
    public static final int[] DEFAULT_WEEKLY_WORK_DAYS_WHICH = new int[]{1,1,1,1,1,0,0};
    public static final String DEFAULT_WEEKLY_WORK_DAYS_WHICH_SPLIT_CHAR = ",";

    public static final int DEFAULT_YEARLY_MAX_VACATION = 60;
    public static final int DEFAULT_YEARLY_MAX_SICK_LEAVE = 31;

    public static final int DEFAULT_YEARLY_VACATION = 30;
    public static final int DEFAULT_YEARLY_SICK_LEAVE = 10;
    public static final int UNLIMITED_YEARLY_SICK_LEAVE = Integer.MAX_VALUE;

    public static final int DEFAULT_WORK_RADIUS = 200;
    public static final int DEFAULT_WORK_MAX_RADIUS = 800;

    public static final int DEFAULT_HOUR_OF_DAY_WORK_START = 9;
}
