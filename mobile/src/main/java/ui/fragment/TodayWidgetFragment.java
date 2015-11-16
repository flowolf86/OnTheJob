package ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.WorkEntry;
import support.AppBroadcaster;

/**
 * Created by Florian on 22.06.2015.
 */
public class TodayWidgetFragment extends Fragment implements View.OnClickListener{

    @Bind(R.id.work_today_hours) TextView mWorkTodayHours;
    @Bind(R.id.root) LinearLayout mRootLayout = null;

    private boolean mIsRegistered = false;
    private final BroadcastReceiver mCacheUpdateReceiver = new DataCacheUpdateReceiver();
    private final IntentFilter mCacheUpdateFilter = new IntentFilter(AppBroadcaster.BC_DATA_CACHE_UPDATED);

    DataCacheHelper mDataCacheHelper = null;

    /*
        Interface
     */

    public interface TodayWidgetInterface {
        void onTodayWidgetInteraction();
    }

    /*
        Lifecycle
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_today_widget, container, false);

        ButterKnife.bind(this, view);
        setOnClickListeners();
        refreshData();

        return view;
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

    private void setOnClickListeners() {
        mRootLayout.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        ((TodayWidgetInterface)getActivity()).onTodayWidgetInteraction();
    }

    private void refreshData() {

        if(mDataCacheHelper == null){
            mDataCacheHelper = new DataCacheHelper(getContext());
        }

        WorkEntry todayEntry = mDataCacheHelper.getWorkEntryForTimestampDay(System.currentTimeMillis());

        if (todayEntry != null) {
            int[] todayDuration = todayEntry.getTotalWorkBlockDuration();
            mWorkTodayHours.setText(getString(R.string.hours_minutes, todayDuration[0], todayDuration[1]));
        } else {
            mWorkTodayHours.setText(getString(R.string.dash));
        }
    }

    private class DataCacheUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            if(getActivity() == null){
                return;
            }

            //TODO BUGGED
            //refreshData();
        }
    }
}
