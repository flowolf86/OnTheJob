package ui.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import listing.ManualLoggingState;
import service.ManualLoggingService;
import support.UserTutorialHelper;
import ui.base.BaseFragment;
import util.DateUtils;

/**
 * Created by Florian on 22.06.2015.
 */
public class ManualLoggingWidgetFragment extends Fragment implements View.OnClickListener{

    @Bind(R.id.start_stop) ImageView mStartStop;
    @Bind(R.id.manual_logging_status) TextView mManualLoggingStatus;
    @Bind(R.id.record) ImageView mRecordIndicator;

    static Handler mHandler = new Handler(Looper.getMainLooper());

    public static final int REFRESH_DELAY = 1000;

    private boolean mBound;

    ManualLoggingService mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            ManualLoggingService.LocalBinder binder = (ManualLoggingService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            checkServiceData();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
        }
    };

    /*
        Interface
     */

    public interface ManualLoggingWidgetInterface {
    }

    /*
        Lifecycle
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_manual_logging_widget, container, false);

        ButterKnife.bind(this, view);
        connectToService();
        setOnClickListeners();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.start_stop:
                if(mBound) {
                    if(mService.getStatus() == ManualLoggingState.STOPPED) {
                        UserTutorialHelper.getInstance(getContext()).displayManualLoggingTutorialIfNeeded(getContext(), getChildFragmentManager());
                        setLoggingActive();
                        return;
                    }
                    if(mService.getStatus() == ManualLoggingState.STARTED) {
                        setLoggingInactive();
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setOnClickListeners() {
        mStartStop.setOnClickListener(this);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof BaseFragment.FragmentNavigationInterface
                && activity instanceof BaseFragment.FragmentToolbarInterface
                && activity instanceof BaseFragment.FragmentSnackbarInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private void connectToService() {

        Intent intent = new Intent(getContext(), ManualLoggingService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Refreshes the widget if the service is already in logging state
     */
    private void checkServiceData() {

        if(mService.getStatus() == ManualLoggingState.STARTED){
            mStartStop.setImageResource(R.drawable.ic_stop_black_48dp);
            mHandler.post(mRefreshTime);
        }
    }

    private void setLoggingInactive() {

        // Failsafe
        if(!DateUtils.isSameDay(mService.getLoggingStartedAt(), System.currentTimeMillis())){
            ((BaseFragment.FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.manual_logging_work_entry_not_created_same_day), Snackbar.LENGTH_SHORT);
        }

        if(mService.getTimeBetweenStartOfLoggingAndNow() < TimeUnit.MINUTES.toMillis(1)){
            ((BaseFragment.FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.manual_logging_work_entry_not_created), Snackbar.LENGTH_SHORT);
            mService.stopLogging();
        } else {
            ((BaseFragment.FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.manual_logging_work_entry_created), Snackbar.LENGTH_SHORT);
            mService.stopLoggingAndStoreEntry();
        }

        mHandler.removeCallbacksAndMessages(null);
        mStartStop.setImageResource(R.drawable.ic_play_arrow_black_48dp);
        mManualLoggingStatus.setText(getString(R.string.manual_logging_inactive));
        mRecordIndicator.setVisibility(View.GONE);
    }

    private void setLoggingActive() {
        mService.startLogging();
        mStartStop.setImageResource(R.drawable.ic_stop_black_48dp);
        mHandler.post(mRefreshTime);
    }

    private Runnable mRefreshTime = new Runnable() {

        @Override
        public void run() {

            if(mBound){
                if(mRecordIndicator != null) {
                    mRecordIndicator.setVisibility(mRecordIndicator.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                if(mManualLoggingStatus != null) {
                    mManualLoggingStatus.setText(getString(R.string.manual_logging_active, DateUtils.getHoursMinutesSeconds(mService.getTimeBetweenStartOfLoggingAndNow())));
                }
            }
            mHandler.postDelayed(this, REFRESH_DELAY);
        }
    };
}
