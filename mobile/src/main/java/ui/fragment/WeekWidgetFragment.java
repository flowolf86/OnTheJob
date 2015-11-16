package ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import org.joda.time.DateTime;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.WorkEntry;
import support.AppBroadcaster;
import util.DateUtils;

/**
 * Created by Florian on 22.06.2015.
 */
public class WeekWidgetFragment extends Fragment implements View.OnClickListener{

    private View mRootView = null;

    @Bind(R.id.week_fragment_root) LinearLayout mRootLayout;

    @Bind(R.id.work_week_title) TextView mWorkWeekTitle;
    @Bind(R.id.work_week_hours) TextView mWorkWeekHours;

    @Bind(R.id.monday_subtitle) TextView mHoursMonday;
    @Bind(R.id.tuesday_subtitle) TextView mHoursTuesday;
    @Bind(R.id.wednesday_subtitle) TextView mHoursWednesday;
    @Bind(R.id.thursday_subtitle) TextView mHoursThursday;
    @Bind(R.id.friday_subtitle) TextView mHoursFriday;
    @Bind(R.id.saturday_subtitle) TextView mHoursSaturday;
    @Bind(R.id.sunday_subtitle) TextView mHoursSunday;

    @Bind(R.id.monday_title) TextView mMondayTitle;
    @Bind(R.id.tuesday_title) TextView mTuesdayTitle;
    @Bind(R.id.wednesday_title) TextView mWednesdayTitle;
    @Bind(R.id.thursday_title) TextView mThursdayTitle;
    @Bind(R.id.friday_title) TextView mFridayTitle;
    @Bind(R.id.saturday_title) TextView mSaturdayTitle;
    @Bind(R.id.sunday_title) TextView mSundayTitle;

    private boolean mIsRegistered = false;
    private final BroadcastReceiver mCacheUpdateReceiver = new DataCacheUpdateReceiver();
    private final IntentFilter mCacheUpdateFilter = new IntentFilter(AppBroadcaster.BC_DATA_CACHE_UPDATED);

    private int mWeekOfYear = 0;

    DataCacheHelper mDataCacheHelper = null;

    /*
        Interface
     */

    public interface WeekWidgetInterface {
        void onWeekWidgetInteraction(@NonNull ArrayList<WorkEntry> workEntryList, @Nullable String toolbarTitle);
    }

    /*
        Lifecycle
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_widget, container, false);

        ButterKnife.bind(this, view);
        mRootView = view;
        setOnClickListeners();
        refreshData();

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceivers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcastReceivers();
        ButterKnife.unbind(this);
    }

    /*
        Logic
     */

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof WeekWidgetInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private synchronized void registerBroadcastReceivers() {

        if(!mIsRegistered){
            AppBroadcaster.registerReceiver(getContext(), mCacheUpdateReceiver, mCacheUpdateFilter);
            mIsRegistered = true;
        }
    }

    private synchronized void unregisterBroadcastReceivers() {

        if(mIsRegistered){
            AppBroadcaster.unregisterReceiver(getContext(), mCacheUpdateReceiver);
            mIsRegistered = false;
        }
    }

    private void setOnClickListeners() {

        mRootLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        ArrayList<WorkEntry> currentWeekWorkEntries = new ArrayList<>(mDataCacheHelper.getAllWorkEntriesForCurrentWeek());
        String toolbarTitle = getString(R.string.work_week_number, mWeekOfYear);

        ((WeekWidgetInterface)getActivity()).onWeekWidgetInteraction(currentWeekWorkEntries, toolbarTitle);
    }

    private void refreshData() {

        if(mDataCacheHelper == null){
            mDataCacheHelper = new DataCacheHelper(getContext());
        }

        int[] weekWorked = mDataCacheHelper.getHoursAndMinutesWorkedCurrentWeek();
        mWorkWeekHours.setText(getString(R.string.hours_minutes, weekWorked[0], weekWorked[1]));


        int[][] weekWorkedPerDay = mDataCacheHelper.getHoursAndMinutesWorkedForEachDayOfTheCurrentWeek();
        TextView[] hoursArray = new TextView[] { mHoursMonday, mHoursTuesday, mHoursWednesday, mHoursThursday, mHoursFriday, mHoursSaturday, mHoursSunday };

        for(int i = 0; i < weekWorkedPerDay.length; i++){
            hoursArray[i].setText(getString(R.string.hours_minutes_short, weekWorkedPerDay[i][0], weekWorkedPerDay[i][1]));
        }

        TextView[] titleArray = new TextView[] { mMondayTitle, mTuesdayTitle, mWednesdayTitle, mThursdayTitle, mFridayTitle, mSaturdayTitle, mSundayTitle };
        titleArray[DateUtils.getTodayDayOfWeekIndexStartingWithZero()].setTextColor(ContextCompat.getColor(getContext(), R.color.accent));

        DateTime dt = new DateTime();
        mWeekOfYear = dt.getWeekOfWeekyear();
        mWorkWeekTitle.setText(getString(R.string.work_week_number, mWeekOfYear));
    }

    private class DataCacheUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(getActivity() == null){
                return;
            }

            //TODOD BUGGED
            //refreshData();
        }
    }
}
