package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import configuration.WorkConfiguration;
import ui.base.BaseFragment;

/**
 * Author:  Florian Wolf
 * Email:   florian.wolf@maibornwolff.de
 * on 09/11/15.
 */
public class SlideInSeekbarPanelFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener{

    public static final String FRAGMENT_TAG = "SlideInSeekbarPanel";

    private static final String ARG_MAX_VALUE = "arg_max_value";
    private static final String ARG_VALUE = "arg_value";

    @Bind(R.id.seekbar) SeekBar mSeekbar;

    private boolean mIsShown = false;
    private static final int ANIMATION_DURATION = 200;

    SlideInSeekbarPanelFragmentInterface mCallback;

    public interface SlideInSeekbarPanelFragmentInterface {
        void onValueChanged(int value);
        void onValueSelected(int value);
    }

    /*
        Fragment creation
     */
    public SlideInSeekbarPanelFragment(){ }

    public static SlideInSeekbarPanelFragment newInstance(int max, int progress){

        final SlideInSeekbarPanelFragment slideInMapPanelFragment = new SlideInSeekbarPanelFragment();
        final Bundle bundle = new Bundle();
        bundle.putInt(ARG_MAX_VALUE, max);
        bundle.putInt(ARG_VALUE, progress);

        slideInMapPanelFragment.setArguments(bundle);

        return slideInMapPanelFragment;
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.slide_in_seekbar_panel, container, false);

        ButterKnife.bind(this, view);
        configureSeekbar();

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
    public boolean onBackPressed() {
        return false;
    }

    /*
        Logic
     */

    public void setCallback(@NonNull final SlideInSeekbarPanelFragmentInterface callback){
        mCallback = callback;
    }

    public void setValue(int value){
        mSeekbar.setProgress(value);
    }

    public void setMaxValue(int maxValue){
        mSeekbar.setMax(maxValue);
    }

    private void configureSeekbar() {

        if(getArguments() != null){
            mSeekbar.setMax(getArguments().getInt(ARG_MAX_VALUE, WorkConfiguration.DEFAULT_WORK_MAX_RADIUS));
            mSeekbar.setProgress(getArguments().getInt(ARG_VALUE, WorkConfiguration.DEFAULT_WORK_RADIUS));
        }

        mSeekbar.setOnSeekBarChangeListener(this);

        // Verticalize seekbar
        /*WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        setRotation(270.0f);
        v.setPadding(0, point.y / 2, 0, 0);*/
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof SlideInSeekbarPanelFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    /*
        Seekbar
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(mCallback != null){
            mCallback.onValueChanged(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        if(mCallback != null){
            mCallback.onValueSelected(seekBar.getProgress());
        }
    }
}
