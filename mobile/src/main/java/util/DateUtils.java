package util;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Florian on 23.06.2015.
 */
public class DateUtils {

    public static String getCurrentYear(){

        String formatString = "yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    public static String getDate(long timestamp){

        String formatString;

        if(Locale.getDefault() != Locale.US){
            formatString = "EEEE, dd.MM.yyyy";
        } else {
            formatString = "EEEE, MM/dd/yyyy";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getDateShort(long timestamp){

        String formatString;

        if(Locale.getDefault() != Locale.US){
            formatString = "EEE, dd.MM.yyyy";
        } else {
            formatString = "EEE, MM/dd/yyyy";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getDateShorter(long timestamp){

        String formatString;

        if(Locale.getDefault() != Locale.US){
            formatString = "EEE, dd.MM.yy";
        } else {
            formatString = "EEE, MM/dd/yy";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getDateShortest(long timestamp){

        String formatString;

        if(Locale.getDefault() != Locale.US){
            formatString = "dd.MM.yy";
        } else {
            formatString = "MM/dd/yy";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getTime(long timestamp){

        String formatString;

        if(Locale.getDefault() != Locale.US){
            formatString = "HH:mm";
        } else {
            formatString = "hh:mm a";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     *
     * @param timestamp1
     * @param timestamp2
     * @return int[] with index 0 being hours and index 1 being minutes
     */
    public static int[] getDurationBetweenDates(long timestamp1, long timestamp2){

        long duration = (timestamp1 > timestamp2) ? (timestamp1 - timestamp2) : (timestamp2 - timestamp1);
        return getDurationInHoursAndMinutes(duration);
    }

    public static int getTodayDayOfWeekIndexStartingWithZero(){
        LocalDate date = new LocalDate();
        return date.getDayOfWeek() - 1;
    }

    public static long getNoonTimeStampForSpecificDayOfTheCurrentWeek(int dayOfWeek){

        // Get noon
        LocalTime lt = new LocalTime(12,0);

        LocalDate now = new LocalDate();
        LocalDate weekDay = now.withDayOfWeek(dayOfWeek);

        DateTime dateTime = weekDay.toDateTime(lt);

        return dateTime.getMillis();
    }

    public static int[] getDurationInHoursAndMinutes(long duration){

        int[] resultSet = new int[2];
        resultSet[0] = (int) TimeUnit.MILLISECONDS.toHours(duration);
        resultSet[1] = ((int) TimeUnit.MILLISECONDS.toMinutes(duration)) % 60;
        return resultSet;
    }

    public static boolean isSameDay(long timestamp1, long timestamp2) {

        Calendar cal1 = Calendar.getInstance(Locale.getDefault());
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance(Locale.getDefault());
        cal2.setTimeInMillis(timestamp2);

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isSameYear(long timestamp1, long timestamp2) {

        Calendar cal1 = Calendar.getInstance(Locale.getDefault());
        cal1.setTimeInMillis(timestamp1);

        Calendar cal2 = Calendar.getInstance(Locale.getDefault());
        cal2.setTimeInMillis(timestamp2);

        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    /**
     *
     * @param timestamp1 the timestamp for date 1
     * @param timestamp2 the timestamp which should be checked if it is the next day of date 1
     * @return
     */
    public static boolean isNextDay(long timestamp1, long timestamp2) {

        long adjustedTimestamp = timestamp1 + TimeUnit.DAYS.toMillis(1);
        return isSameDay(adjustedTimestamp, timestamp2);
    }

    /**
     *
     * @param context
     * @param timestamp
     * @return The day as String (e.g. Monday, Tuesday etc.) for the timestamp
     */
    public static String getDayOfWeekString(long timestamp){

        final String formatString = "EEEE";

        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * <p>Checks if a long timestamp is today.</p>
     * @param timestamp the timestamp to check
     * @return true if the date is today.
     */
    public static boolean isToday(long timestamp) {
        return isSameDay(timestamp, System.currentTimeMillis());
    }

    /**
     * Returns the number of days between two dates
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getDaysBetweenDates(long startDate, long endDate) {
        return Days.daysBetween(new LocalDate(startDate), new LocalDate(endDate)).getDays();
    }

    /**
     * Returns the number of days between two dates + 1
     * e.g. 27.11 - 28.11 will return 2 for the number of days
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getNumberOfDays(long startDate, long endDate) {
        return Days.daysBetween(new LocalDate(startDate), new LocalDate(endDate)).getDays() + 1;
    }

    /**
     * Returns HH:MM:SS for any given long value of milliseconds
     *
     * @param millies
     * @return
     */
    public static String getHoursMinutesSeconds(long millis) {

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hours));
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis - TimeUnit.MINUTES.toMillis(minutes));

        return String.format("%02d:", hours) + String.format("%02d:", minutes) + String.format("%02d", seconds);
    }
}
