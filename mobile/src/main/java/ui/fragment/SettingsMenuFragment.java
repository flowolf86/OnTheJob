package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import configuration.WorkConfiguration;
import data.manager.SharedPreferencesManager;
import geofence.ManageGeofenceInterface;
import listing.GeofencingState;
import support.AppBroadcaster;
import ui.base.BaseFragment;
import ui.dialog.SimpleDialogFragment;
import util.UserUtils;
import view.WeekSelectorView;

public class SettingsMenuFragment extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, WeekSelectorView.WeekSelectorViewInterface{

    public static final String FRAGMENT_TAG = "settings_menu_fragment";

    @Bind(R.id.weekly_workload_title) TextView mWeeklyWorkloadSeekbarText;
    @Bind(R.id.weekly_work_days_title) TextView mWeeklyWorkDaysSeekbarText;
    @Bind(R.id.work_radius_title) TextView mWorkRadiusSeekbarText;
    @Bind(R.id.vacation_days_title) TextView mYearlyVacationDaysSeekbarText;
    @Bind(R.id.sick_leave_title) TextView mYearlySickLeaveSeekbarText;

    @Bind(R.id.weekly_workload_seekbar) SeekBar mWeeklyWorkloadSeekbar;
    @Bind(R.id.work_radius_seekbar) SeekBar mWorkRadiusSeekbar;
    @Bind(R.id.vacation_days_seekbar) SeekBar mYearlyVacationDaysSeekbar;
    @Bind(R.id.sick_leave_seekbar) SeekBar mYearlySickLeaveSeekbar;

    @Bind(R.id.week_selector) WeekSelectorView mWeekSelectorView;

    @Bind(R.id.categories_view) RelativeLayout mCategoriesLayout;
    @Bind(R.id.notifications_view) RelativeLayout mNotificationsLayout;
    @Bind(R.id.geofencing_view) RelativeLayout mGeofencingLayout;
    @Bind(R.id.about_view) RelativeLayout mAboutLayout;
    @Bind(R.id.feedback_view) RelativeLayout mFeedbackLayout;

    @Bind(R.id.notifications_checkbox) AppCompatCheckBox mNotificationsCheckbox;
    @Bind(R.id.geofencing_checkbox) AppCompatCheckBox mGeofencingCheckbox;

    private SharedPreferencesManager mSharedPreferencesManager = null;

    public SettingsMenuFragment() { }

    public static SettingsMenuFragment newInstance() {
        return new SettingsMenuFragment();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Interface
     */

    public interface OnSettingsMenuFragmentActionListener {
        void onCategoriesSelected();
        void onAboutSelected();
        void onFeedbackSelected();
    }

    /*
        Activity communication
     */

    private void setToolbarState(){
        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarStateChange();
    }

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_menu, container, false);

        ButterKnife.bind(this, view);
        setDefaults();

        // Set this after setting the defaults
        setOnClickListeners();
        configureSeekbars();
        configureWeekSelector();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public void onStart() {
        CURRENT_FRAGMENT_TAG = FRAGMENT_TAG;
        super.onStart();
        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarTitleChange(getString(R.string.settings));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /*
        Logic
     */

    private SharedPreferencesManager getSharedPreferencesManager(){
        if(mSharedPreferencesManager == null) {
            mSharedPreferencesManager = new SharedPreferencesManager(getContext());
        }
        return mSharedPreferencesManager;
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof OnSettingsMenuFragmentActionListener
                && activity instanceof FragmentToolbarInterface
                && activity instanceof ManageGeofenceInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private void setOnClickListeners() {
        mCategoriesLayout.setOnClickListener(this);
        mAboutLayout.setOnClickListener(this);
        mFeedbackLayout.setOnClickListener(this);

        mNotificationsLayout.setOnClickListener(this);
        mNotificationsCheckbox.setOnCheckedChangeListener(this);

        mGeofencingLayout.setOnClickListener(this);
        mGeofencingCheckbox.setOnCheckedChangeListener(this);
    }

    private void configureWeekSelector() {

        int[] weekSelectionInt = UserUtils.getUserWorkDaysArray(getSharedPreferencesManager());
        mWeekSelectorView.setInitialData(this, weekSelectionInt);
        mWeeklyWorkDaysSeekbarText.setText(getString(R.string.weekly_workdays, mWeekSelectorView.getNumberOfActiveElements()));
    }

    private void configureSeekbars() {

        mWeeklyWorkloadSeekbar.setOnSeekBarChangeListener(this);
        mWeeklyWorkloadSeekbar.setMax(WorkConfiguration.DEFAULT_WEEKLY_MAX_WORKLOAD);

        mYearlyVacationDaysSeekbar.setOnSeekBarChangeListener(this);
        mYearlyVacationDaysSeekbar.setMax(WorkConfiguration.DEFAULT_YEARLY_MAX_VACATION);

        mYearlySickLeaveSeekbar.setOnSeekBarChangeListener(this);
        mYearlySickLeaveSeekbar.setMax(WorkConfiguration.DEFAULT_YEARLY_MAX_SICK_LEAVE);

        mWorkRadiusSeekbar.setOnSeekBarChangeListener(this);
        mWorkRadiusSeekbar.setMax(WorkConfiguration.DEFAULT_WORK_MAX_RADIUS);

        // Set defaults
        int workLoad = getSharedPreferencesManager().get(SharedPreferencesManager.ID_WORKLOAD, WorkConfiguration.DEFAULT_WEEKLY_WORKLOAD);
        mWeeklyWorkloadSeekbar.setProgress(workLoad);
        mWeeklyWorkloadSeekbarText.setText(getString(R.string.weekly_workload, workLoad));

        int vacationDays = getSharedPreferencesManager().get(SharedPreferencesManager.ID_VACATION, WorkConfiguration.DEFAULT_YEARLY_VACATION);
        mYearlyVacationDaysSeekbar.setProgress(vacationDays);
        mYearlyVacationDaysSeekbarText.setText(getString(R.string.yearly_vacation_days, vacationDays));

        int sickDays = getSharedPreferencesManager().get(SharedPreferencesManager.ID_SICK_LEAVE, WorkConfiguration.DEFAULT_YEARLY_SICK_LEAVE);
        if(sickDays != WorkConfiguration.UNLIMITED_YEARLY_SICK_LEAVE) {
            mYearlySickLeaveSeekbar.setProgress(sickDays);
            mYearlySickLeaveSeekbarText.setText(getString(R.string.yearly_sick_leave, sickDays));
        } else {
            mYearlySickLeaveSeekbar.setProgress(WorkConfiguration.DEFAULT_YEARLY_MAX_SICK_LEAVE);
            mYearlySickLeaveSeekbarText.setText(getString(R.string.yearly_sick_leave, getString(R.string.unlimited)));
        }

        int workRadius = getSharedPreferencesManager().get(SharedPreferencesManager.ID_WORK_RADIUS, WorkConfiguration.DEFAULT_WORK_RADIUS);
        mWorkRadiusSeekbar.setProgress(workRadius);
        mWorkRadiusSeekbarText.setText(getString(R.string.work_radius, workRadius));
    }

    private void setDefaults() {
        boolean enableNotifications = getSharedPreferencesManager().get(SharedPreferencesManager.ID_NOTIFICATIONS, true);
        mNotificationsCheckbox.setChecked(enableNotifications);

        boolean enableGeofencing = getSharedPreferencesManager().get(SharedPreferencesManager.ID_GEOFENCING, false);
        mGeofencingCheckbox.setChecked(enableGeofencing);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(mWeeklyWorkloadSeekbar.equals(seekBar) && fromUser){
            mWeeklyWorkloadSeekbarText.setText(getString(R.string.weekly_workload, progress));
        }

        if(mYearlyVacationDaysSeekbar.equals(seekBar) && fromUser){
            mYearlyVacationDaysSeekbarText.setText(getString(R.string.yearly_vacation_days, progress));
        }

        if(mYearlySickLeaveSeekbar.equals(seekBar) && fromUser){

            String text;
            if(progress < WorkConfiguration.DEFAULT_YEARLY_MAX_SICK_LEAVE) {
                text = getString(R.string.yearly_sick_leave, Integer.toString(progress));
            } else {
                text = getString(R.string.yearly_sick_leave, getString(R.string.unlimited));
            }
            mYearlySickLeaveSeekbarText.setText(text);
        }

        if(mWorkRadiusSeekbar.equals(seekBar) && fromUser){
            mWorkRadiusSeekbarText.setText(getString(R.string.work_radius, progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mWeeklyWorkloadSeekbar.equals(seekBar)){
            getSharedPreferencesManager().set(SharedPreferencesManager.ID_WORKLOAD, seekBar.getProgress());
        }

        if(mWorkRadiusSeekbar.equals(seekBar)){
            getSharedPreferencesManager().set(SharedPreferencesManager.ID_WORK_RADIUS, seekBar.getProgress());

            // Update geofences if activated
            if(getSharedPreferencesManager().get(SharedPreferencesManager.ID_GEOFENCING, true)) {
                ((ManageGeofenceInterface) getActivity()).onUpdateAllGeofencesRequest();
            }
        }

        if(mYearlyVacationDaysSeekbar.equals(seekBar)){
            getSharedPreferencesManager().set(SharedPreferencesManager.ID_VACATION, seekBar.getProgress());
        }

        if(mYearlySickLeaveSeekbar.equals(seekBar)){

            if(seekBar.getProgress() < WorkConfiguration.DEFAULT_YEARLY_MAX_SICK_LEAVE) {
                getSharedPreferencesManager().set(SharedPreferencesManager.ID_SICK_LEAVE, seekBar.getProgress());
            } else {
                getSharedPreferencesManager().set(SharedPreferencesManager.ID_SICK_LEAVE, WorkConfiguration.UNLIMITED_YEARLY_SICK_LEAVE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.categories_view:
                ((OnSettingsMenuFragmentActionListener)getActivity()).onCategoriesSelected();
                break;
            case R.id.notifications_view:
                mNotificationsCheckbox.setChecked(!mNotificationsCheckbox.isChecked());
                break;
            case R.id.geofencing_view:
                mGeofencingCheckbox.setChecked(!mGeofencingCheckbox.isChecked());
                break;
            case R.id.about_view:
                ((OnSettingsMenuFragmentActionListener)getActivity()).onAboutSelected();
                break;
            case R.id.feedback_view:
                ((OnSettingsMenuFragmentActionListener)getActivity()).onFeedbackSelected();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(buttonView.equals(mNotificationsCheckbox)){
            getSharedPreferencesManager().set(SharedPreferencesManager.ID_NOTIFICATIONS, isChecked);
        }

        if(buttonView.equals(mGeofencingCheckbox)){
            if(isChecked){
                SimpleDialogFragment.newInstance(null, getString(R.string.geofencing_beta_warning),
                        getString(R.string.dialog_button_ok), null, SimpleDialogFragment.NO_REQUEST_CODE, null).show(getChildFragmentManager(), null);
            }

            getSharedPreferencesManager().set(SharedPreferencesManager.ID_GEOFENCING, isChecked);
            enableOrDisableGeofencing(isChecked);
        }
    }

    private void enableOrDisableGeofencing(boolean isEnabled) {

        int status = isEnabled ? GeofencingState.NOT_AT_WORK : GeofencingState.DISABLED;
        AppBroadcaster.sendStatusChangedBroadcast(getContext(), status);

        if(isEnabled) {
            ((ManageGeofenceInterface) getActivity()).onUpdateAllGeofencesRequest();
        } else {
            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
            sharedPreferencesManager.set(SharedPreferencesManager.ID_LAST_GEOFENCE_STATE, GeofencingState.DISABLED);

            ((ManageGeofenceInterface) getActivity()).onRemoveAllGeofencesRequest();
        }
    }

    /*
        Week selector
     */
    @Override
    public void onWeekdaySelectedOrDeselected(int[] selection) {

        int selectionCounter = 0;

        StringBuilder workDays = new StringBuilder();
        for(int i : selection){
            workDays.append(i);
            workDays.append(WorkConfiguration.DEFAULT_WEEKLY_WORK_DAYS_WHICH_SPLIT_CHAR);

            if(i == 1){
                selectionCounter++;
            }
        }

        getSharedPreferencesManager().set(SharedPreferencesManager.ID_WORK_DAYS, workDays.toString());
        getSharedPreferencesManager().set(SharedPreferencesManager.ID_WORK_DAYS_NUMBER, selectionCounter);
        mWeeklyWorkDaysSeekbarText.setText(getString(R.string.weekly_workdays, mWeekSelectorView.getNumberOfActiveElements()));
    }
}
