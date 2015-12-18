package cache;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import configuration.WorkConfiguration;
import data.Category;
import data.Interval;
import data.WorkBlock;
import data.WorkEntry;
import data.factory.CategoryFactory;
import data.manager.SharedPreferencesManager;
import database.DatabaseUiCallback;
import util.DateUtils;
import util.UserUtils;

public class DataCacheHelper {

    private EntryCache mEntryCache = null;
    private IntervalCache mIntervalCache = null;

    public DataCacheHelper(@NonNull Context context){
        mEntryCache = EntryCache.getInstance(context);
        mIntervalCache = IntervalCache.getInstance(context);
    }

    public List<WorkEntry> getAllWorkEntries() {
        return mEntryCache.getAllWorkEntries();
    }

    /**
     * Returns the intervals for the chosen category mode
     *
     * @param mode Category.SICK_LEAVE or Category.VACATION will give results if available
     * @return
     */
    public ArrayList<Interval> getIntervalsForMode(final @Category.CategoryMode int mode) {

        final Category category = mode == Category.VACATION ? CategoryFactory.getVacationCategory() : CategoryFactory.getSickLeaveCategory();

        final List<Interval> intervals = mIntervalCache.getAllIntervals();
        final ArrayList<Interval> modeIntervals = new ArrayList<>();

        for(final Interval interval : intervals){
            if(interval.hasSameCategory(category)){
                modeIntervals.add(interval);
            }
        }

        return modeIntervals;
    }

    public List<Interval> getAllIntervals() {

        return mIntervalCache.getAllIntervals();
    }

    public void refreshEntryCache(){
        mEntryCache.refreshCache();
    }

    public void refreshIntervalCache(){
        mIntervalCache.refreshCache();
    }

    public List<WorkEntry> getCacheDbRestoreData() {
        return mEntryCache.getLastValidDbCacheCopy();
    }

    private int getHoursWorkedToday(){
        return 0;
    }

    private long getTodayStartWorkTimestamp(){
        return 0;
    }

    private long getTodayEndWorkTimestamp(){
        return 0;
    }

    public List<WorkEntry> getAllWorkEntriesForCurrentWeek(){
        List<WorkEntry> results = new ArrayList<>();

        for(int i = DateTimeConstants.MONDAY; i <= DateTimeConstants.SUNDAY; i++){
            WorkEntry entry = getWorkEntryForTimestampDay(DateUtils.getNoonTimeStampForSpecificDayOfTheCurrentWeek(i));

            if(entry != null){
                results.add(entry);
            }
        }

        return results;
    }

    /*
        Calculation Helper
     */
    public int[][] getHoursAndMinutesWorkedForEachDayOfTheCurrentWeek(){
        int[][] resultSet = new int[7][2];

        for(int i = DateTimeConstants.MONDAY; i <= DateTimeConstants.SUNDAY; i++){
            resultSet[i-1] = DateUtils.getDurationInHoursAndMinutes(getMilliesWorkedOnWeekdayCurrentWeek(i));
        }

        return resultSet;
    }

    public long getMilliesWorkedOnWeekdayCurrentWeek(int dayOfWeek){

        long timestamp = DateUtils.getNoonTimeStampForSpecificDayOfTheCurrentWeek(dayOfWeek);
        return getMilliesWorkedOnTimestampDay(timestamp);
    }

    public int[] getHoursAndMinutesWorkedCurrentWeek(){
        return DateUtils.getDurationInHoursAndMinutes(getMilliesWorkedCurrentWeek());
    }

    public long getMilliesWorkedCurrentWeek(){

        // Get noon
        LocalTime lt = new LocalTime(12,0);

        LocalDate now = new LocalDate();
        LocalDate weekDay = now.withDayOfWeek(DateTimeConstants.MONDAY);

        int days = 6;
        int i=0;
        long totalMillies = 0L;

        while(i < days) {

            DateTime dateTime = weekDay.toDateTime(lt);
            totalMillies += getMilliesWorkedOnTimestampDay(dateTime.getMillis());

            weekDay = weekDay.plusDays(1);
            i++;
        }

        return totalMillies;
    }

    public long getMinutesWorkedOnTimestampDay(long timestamp){

        WorkEntry todayEntry = getWorkEntryForTimestampDay(timestamp);
        return todayEntry == null ? 0 : todayEntry.getTotalWorkBlockDurationInMinutes();
    }

    public long getMilliesWorkedOnTimestampDay(long timestamp){

        WorkEntry todayEntry = getWorkEntryForTimestampDay(timestamp);
        return todayEntry == null ? 0 : todayEntry.getTotalWorkBlockDurationInMillies();
    }

    public @Nullable WorkEntry getWorkEntryForTimestampDay(long timestamp) {

        WorkEntry todayEntry = null;

        for(WorkEntry entry : getAllWorkEntries()){
            todayEntry = DateUtils.isSameDay(timestamp, entry.getDate()) ? entry : null;

            if(todayEntry != null){
                break;
            }
        }

        return todayEntry;
    }

    public double getTakenVacationDaysThisYear(@NonNull SharedPreferencesManager sharedPreferencesManager){
        return getCategoryDaysThisYear(sharedPreferencesManager, Category.VACATION);
    }

    public double getSickDaysThisYear(@NonNull SharedPreferencesManager sharedPreferencesManager){
        return getCategoryDaysThisYear(sharedPreferencesManager, Category.SICK_LEAVE);
    }

    private double getCategoryDaysThisYear(@NonNull SharedPreferencesManager sharedPreferencesManager, @Category.CategoryMode int category){

        long milliesThisYear;

        // First check all intervals
        int numberOfDays = 0;

        int[] weekSelectionInt = UserUtils.getUserWorkDaysArray(sharedPreferencesManager);

        for(Interval interval : getIntervalsForMode(category)){

            long checkDate = interval.getStartDate();
            while(checkDate <= interval.getEndDate() || DateUtils.isSameDay(interval.getEndDate(), checkDate)){

                if(DateUtils.isSameYear(System.currentTimeMillis(), checkDate)){

                    if(isWorkDayForUser(weekSelectionInt, checkDate)) {
                        numberOfDays++;
                    }
                }
                checkDate += TimeUnit.HOURS.toMillis(24);
            }
        }

        milliesThisYear = convertAbsoluteWorkDaysMilliesDependingOnUserPreferences(sharedPreferencesManager, numberOfDays);

        // Now, check all blocks that have the corresponding category
        for(WorkEntry workEntry : getAllWorkEntries()){
            for(WorkBlock block : workEntry.getWorkBlocks()){
                switch (category){
                    case Category.VACATION:
                        milliesThisYear += block.getVacationMillies();
                        break;
                    case Category.SICK_LEAVE:
                        milliesThisYear += block.getSickLeaveMillies();
                        break;
                    default:
                        break;
                }
            }
        }

        long hours = TimeUnit.MILLISECONDS.toHours(milliesThisYear);
        return convertToAdjustedValueDependingOnUserPreferences(sharedPreferencesManager, hours);
    }

    public boolean isWorkDayForUser(Context context, long checkDate) {

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        int[] weekSelectionInt = UserUtils.getUserWorkDaysArray(sharedPreferencesManager);

        return isWorkDayForUser(weekSelectionInt, checkDate);
    }

    public boolean isWorkDayForUser(int[] userWorkDays, long checkDate) {

        LocalDate newDate = new LocalDate(checkDate);

        for(int i = 0; i < userWorkDays.length; i++){
            if(newDate.getDayOfWeek() == i+1 && userWorkDays[i] == 1){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a double value in days depending on the user workload
     * e.g. 16 hours for a user with daily workload of 8 hours will return 2.0
     * for a user with 5 hours daily workload it will return 3.2
     *
     * @param hours
     * @return
     */
    private double convertToAdjustedValueDependingOnUserPreferences(SharedPreferencesManager sharedPreferencesManager, long hours){
        int weeklyWorkload = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORKLOAD, WorkConfiguration.DEFAULT_WEEKLY_WORKLOAD);
        int weeklyWorkdays = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_DAYS_NUMBER, WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS);

        double dailyWorkload = weeklyWorkload / weeklyWorkdays;

        NumberFormat formatter = new DecimalFormat("#0.0");
        Double finalValue;
        try {
            finalValue = Double.parseDouble(formatter.format(hours / dailyWorkload));
        }catch (NumberFormatException e){
            formatter = new DecimalFormat("#0,0");
            try {
                finalValue = Double.parseDouble(formatter.format(hours / dailyWorkload));
            }catch (NumberFormatException e1){
                return .0;
            }
        }

        return finalValue;
    }

    private long convertAbsoluteWorkDaysMilliesDependingOnUserPreferences(SharedPreferencesManager sharedPreferencesManager, int days){
        int weeklyWorkload = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORKLOAD, WorkConfiguration.DEFAULT_WEEKLY_WORKLOAD);
        int weeklyWorkdays = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_DAYS_NUMBER, WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS);

        double dailyWorkload = weeklyWorkload / weeklyWorkdays;

        return (long) (days * dailyWorkload) * TimeUnit.HOURS.toMillis(1);
    }

    /*
        Database Helper
     */

    /**
     * Adds the work entry to the database depending on the _id.
     * Adds all work blocks of the specific entry as well.
     *
     * @param workEntry
     * @param uiCallback optional callback for the ui. may be null.
     */
    public void addNewEntry(@NonNull WorkEntry workEntry, @Nullable DatabaseUiCallback uiCallback){
        mEntryCache.addNewEntry(workEntry, uiCallback);
    }

    /**
     * Modifies the work entry in the database depending on the _id.
     * Modifies all work blocks of the specific entry as well.
     *
     * Does NOT auto-create the specific entry in db if not found.
     *
     * @param workEntry
     * @param uiCallback optional callback for the ui. may be null.
     */
    public void modifyWorkEntry(@NonNull WorkEntry workEntry, @Nullable DatabaseUiCallback uiCallback){
        mEntryCache.modifyWorkEntry(workEntry, uiCallback);
    }

    /**
     * Deletes the work entry in the database depending on the _id.
     * Deletes all work blocks of the specific entry as well.
     *
     * @param workEntry
     * @param uiCallback optional callback for the ui. may be null.
     */
    public void deleteWorkEntry(@Nullable WorkEntry workEntry, @Nullable DatabaseUiCallback uiCallback){

        if(workEntry == null){
            return;
        }

        mEntryCache.deleteWorkEntry(workEntry, uiCallback);
    }

    //TODO JavaDoc

    public void addNewBlock(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock, @Nullable DatabaseUiCallback uiCallback){
        mEntryCache.addNewBlock(workEntry, workBlock, uiCallback);
    }

    private void modifyWorkBlock(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock, @Nullable DatabaseUiCallback uiCallback){
        mEntryCache.modifyWorkBlock(workEntry, workBlock, uiCallback);
    }

    private void deleteWorkBlock(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock, @Nullable DatabaseUiCallback uiCallback){
        mEntryCache.deleteWorkBlock(workEntry, workBlock, uiCallback);
    }

    //TODO JavaDoc
    public void addNewInterval(@NonNull Interval interval, @Nullable DatabaseUiCallback uiCallback){
        mIntervalCache.addInterval(interval, uiCallback);
    }

    public void modifyInterval(@NonNull Interval interval, @Nullable DatabaseUiCallback uiCallback){
        mIntervalCache.modifyInterval(interval, uiCallback);
    }

    public void deleteInterval(@NonNull Interval interval, @Nullable DatabaseUiCallback uiCallback){
        mIntervalCache.deleteInterval(interval, uiCallback);
    }

    /*
        Calendar
     */

    public HashSet<CalendarDay> getAllEventDays(){

        HashSet<CalendarDay> hashSet = new HashSet<>();

        for(WorkEntry entry : getAllWorkEntries()){
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(entry.getDate());
            hashSet.add(CalendarDay.from(cal));
        }

        return hashSet;
    }

    public HashSet<CalendarDay> getAllVacationDays(){

        return getIntervalDaysAsHashSet(Category.VACATION);
    }

    public HashSet<CalendarDay> getAllSickDays(){

        return getIntervalDaysAsHashSet(Category.SICK_LEAVE);
    }

    private HashSet<CalendarDay> getIntervalDaysAsHashSet(final @Category.CategoryMode int mode){

        HashSet<CalendarDay> hashSet = new HashSet<>();
        for(Interval interval : getIntervalsForMode(mode)){
            long time = interval.getStartDate();
            while(time <= interval.getEndDate()){
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(time);
                hashSet.add(CalendarDay.from(cal));
                time += TimeUnit.DAYS.toMillis(1);
            }
        }
        return hashSet;
    }
}
