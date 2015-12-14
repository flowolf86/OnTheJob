package ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import listing.ManualLoggingState;

/**
 * Created by Florian on 22.06.2015.
 */
public class ManualLoggingWidgetFragment extends Fragment implements View.OnClickListener{

    @Bind(R.id.start_stop) ImageView mStartStop;
    @Bind(R.id.time_logging) TextView mTimeLogging;

    static Handler mHandler = new Handler(Looper.getMainLooper());

    // TODO
    static long start_timestamp = 0L;
    static long stop_timestamp = 0L;

    @ManualLoggingState.IManualLoggingState static int mStatus = ManualLoggingState.STOPPED;

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
        setOnClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void setOnClickListeners() {
        mStartStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.start_stop:
                switch (mStatus){
                    case ManualLoggingState.STOPPED:
                        mStartStop.setImageResource(R.drawable.ic_stop_black_48dp);
                        start_timestamp = System.currentTimeMillis();
                        mStatus = ManualLoggingState.STARTED;
                        mHandler.postDelayed(mRefreshTime, 100);
                        break;
                    case ManualLoggingState.STARTED:
                        mStartStop.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                        stop_timestamp = System.currentTimeMillis();
                        mStatus = ManualLoggingState.STOPPED;
                        mHandler.removeCallbacksAndMessages(null);
                        mTimeLogging.setText("Not logging");
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private Runnable mRefreshTime = new Runnable() {

        @Override
        public void run() {
            if(mTimeLogging != null) {
                mTimeLogging.setText(String.valueOf(System.currentTimeMillis() - start_timestamp));
            }
            mHandler.postDelayed(this, 100);
        }
    };
}
