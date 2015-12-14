package util;

import configuration.WorkConfiguration;
import data.manager.SharedPreferencesManager;

/**
 * Author:  Florian Wolf
 * Email:   florian.wolf@maibornwolff.de
 * on 09/11/15.
 */
public class UserUtils {

    public static int getUserWeeklyWorkLoad(SharedPreferencesManager sharedPreferencesManager) {
        return sharedPreferencesManager.get(SharedPreferencesManager.ID_WORKLOAD, WorkConfiguration.DEFAULT_WEEKLY_WORKLOAD);
    }

    public static int getUserWeeklyWorkDays(SharedPreferencesManager sharedPreferencesManager) {
        return sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_DAYS_NUMBER, WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS);
    }

    public static int getUserYearlyVacationDays(SharedPreferencesManager sharedPreferencesManager) {
        return sharedPreferencesManager.get(SharedPreferencesManager.ID_VACATION, WorkConfiguration.DEFAULT_YEARLY_VACATION);
    }

    /**
     * IMPORTANT: Will return INT_MAX for unlimited sick leave!
     *
     * @param sharedPreferencesManager
     * @return
     */
    public static int getUserYearlySickDays(SharedPreferencesManager sharedPreferencesManager) {
        return sharedPreferencesManager.get(SharedPreferencesManager.ID_SICK_LEAVE, WorkConfiguration.DEFAULT_YEARLY_SICK_LEAVE);
    }

    public static int[] getUserWorkDaysArray(SharedPreferencesManager sharedPreferencesManager) {
        int[] weekSelectionInt = new int[7];
        String weekSelection = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_DAYS, null);

        if (weekSelection == null) {
            weekSelectionInt = WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS_WHICH;
        } else {

            String[] weekSelectionSplit = weekSelection.split(WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS_WHICH_SPLIT_CHAR);

            try {
                int count = 0;
                for (String i : weekSelectionSplit) {
                    weekSelectionInt[count] = Integer.valueOf(i);
                    count++;
                }
            } catch (NumberFormatException e) {
            }
        }

        return weekSelectionInt;
    }
}
