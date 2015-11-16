package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import data.Category;
import data.Interval;
import data.WorkEntry;
import data.factory.CategoryFactory;
import ui.base.BaseFragment;
import ui.dialog.DatePickerFragment;
import ui.dialog.SimpleDialogFragment;
import util.DateUtils;
import util.TextUtils;

public class IntervalDetailsFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener, DatePickerFragment.DatePickerCallback, SimpleDialogFragment.SimpleDialogCallback{

    public static final String FRAGMENT_TAG = "day_off_planer_fragment";

    /*
        IntDefs
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, VACATION, SICK_LEAVE})
    public @interface DayOffType{ }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CREATE, EDIT})
    public @interface DayOffState{ }

    public static final int NONE = 0;
    public static final int VACATION = 10;
    public static final int SICK_LEAVE = 20;

    public static final int CREATE = 110;
    public static final int EDIT = 120;

    private static final int START_DATE_REQUEST_CODE = 208;
    private static final int END_DATE_REQUEST_CODE = 209;

    /*
        Variables & Statics
     */
    @Bind(R.id.store_container) FloatingActionButton mStoreContainerButton;
    @Bind(R.id.root) CoordinatorLayout mRootLayout;
    @Bind(R.id.title) EditText mTitleText;
    @Bind(R.id.title_counter) TextView mTitleCounter;
    @Bind(R.id.description) EditText  mDescriptionText;
    @Bind(R.id.description_counter) TextView mDescriptionCounter;
    @Bind(R.id.start_date) EditText mStartDate;
    @Bind(R.id.end_date) EditText mEndDate;
    @Bind(R.id.category_icon) ImageView mCategoryIcon;
    @Bind(R.id.category_selector) ImageView mCategorySelector;

    private boolean mHasDataChanged = false;

    private static final String ARG_INTERVAL = "arg_interval";
    private static final String ARG_TYPE = "arg_fragment_type";
    private static final String ARG_STATE = "arg_fragment_state";

    private static int TITLE_LENGTH = 0;
    private static int DESCRIPTION_LENGTH = 0;

    private Interval mInterval = null;

    /*
        Interface
     */

    public interface IntervalDetailsFragmentInterface {
        void onStore(@NonNull Interval interval);
    }

    public IntervalDetailsFragment() { }

    public static IntervalDetailsFragment newInstance(@Nullable Interval interval, @DayOffType int type) {

        IntervalDetailsFragment f = new IntervalDetailsFragment();

        Bundle args = new Bundle();
        if(interval != null) {
            args.putParcelable(ARG_INTERVAL, interval);
        }
        args.putInt(ARG_TYPE, type);
        args.putInt(ARG_STATE, interval == null ? CREATE : EDIT);
        f.setArguments(args);

        return f;
    }

    /*
        Fragment data
     */

    private @NonNull Interval getInterval(){

        if(mInterval == null){
            mInterval = getArguments().getParcelable(ARG_INTERVAL);

            if(mInterval == null){

                Category category;
                switch (getViewType()){
                    case VACATION:
                        category = CategoryFactory.getVacationCategory();
                        break;
                    case SICK_LEAVE:
                        category = CategoryFactory.getSickLeaveCategory();
                        break;
                    case NONE:
                    default:
                        throw new IllegalArgumentException("Unable to handle type NONE / UNKNOWN in DayOffPlanerFragment...");
                }
                mInterval = new Interval("DEFAULT", "DEFAULT", System.currentTimeMillis(), System.currentTimeMillis() + TimeUnit.DAYS.toHours(1), category);
            }
        }
        return mInterval;
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
        View view = inflater.inflate(R.layout.fragment_interval_details, container, false);

        ButterKnife.bind(this, view);
        getDefaultIntegers();
        refreshFragmentData(getViewState());
        setOnClickListeners();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements(getActivity());
    }

    @Override
    public void onStart() {
        CURRENT_FRAGMENT_TAG = FRAGMENT_TAG;
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        unsetOnFocusChangedListeners();
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof FragmentBackHandlerInterface
                && activity instanceof IntervalDetailsFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    /*
        Logic
     */

    private void getDefaultIntegers() {
        TITLE_LENGTH = getResources().getInteger(R.integer.title_length);
        DESCRIPTION_LENGTH = getResources().getInteger(R.integer.block_description_length);
    }

    private void setDataChanged() {
        mHasDataChanged = true;
    }

    private boolean hasDataChanged(){
        return mHasDataChanged;
    }

    private void refreshFragmentData(@DayOffState int viewState) {

        if(viewState == EDIT){

            setToolbarTitle(R.string.title_edit);

            mTitleText.setText(getInterval().getTitle());
            mTitleCounter.setText(getString(R.string.default_text_counter, TextUtils.isValidText(getInterval().getTitle()) ? getInterval().getTitle().length() : 0, TITLE_LENGTH));

            mDescriptionText.setText(getInterval().getDescription());
            mDescriptionCounter.setText(getString(R.string.default_text_counter, TextUtils.isValidText(getInterval().getDescription()) ? getInterval().getDescription().length() : 0, DESCRIPTION_LENGTH));

        } else if(viewState == CREATE){

            setToolbarTitle(R.string.title_new);

            mTitleText.setText("");
            mTitleCounter.setText(getString(R.string.default_text_counter, 0, TITLE_LENGTH));
            mDescriptionText.setText("");
            mDescriptionCounter.setText(getString(R.string.default_text_counter, 0, DESCRIPTION_LENGTH));
        }

        mStartDate.setText(DateUtils.getDate(getInterval().getStartDate()));
        mEndDate.setText(DateUtils.getDate(getInterval().getEndDate()));

        setCategory();
    }

    private @DayOffState int getViewState(){
        @DayOffState int state = getArguments().getInt(ARG_STATE, CREATE);
        return state;
    }

    private @DayOffType int getViewType(){
        @DayOffType int type = getArguments().getInt(ARG_TYPE, NONE);
        return type;
    }

    private void setCategory(){

        mCategorySelector.setColorFilter(getInterval().getCategory().color, PorterDuff.Mode.ADD);

        switch (getViewType()){
            case VACATION:
                mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flight_takeoff_black_24dp));
                break;
            case SICK_LEAVE:
                mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_airline_seat_individual_suite_black_24dp));
                break;
            default:
                break;
        }
    }

    /*
        Listeners
     */

    private void setOnClickListeners() {

        mTitleText.setOnFocusChangeListener(this);
        mTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getInterval().setTitle(s.toString());
                mTitleCounter.setText(getString(R.string.default_text_counter, s.length(), TITLE_LENGTH));
                setDataChanged();
            }
        });

        mDescriptionText.setOnFocusChangeListener(this);
        mDescriptionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getInterval().setDescription(s.toString());
                mDescriptionCounter.setText(getString(R.string.default_text_counter, s.length(), DESCRIPTION_LENGTH));
                setDataChanged();
            }
        });

        mStartDate.setOnClickListener(this);
        mEndDate.setOnClickListener(this);
        mStoreContainerButton.setOnClickListener(this);
    }

    private void unsetOnFocusChangedListeners() {

        mTitleText.setOnFocusChangeListener(null);
        mDescriptionText.setOnFocusChangeListener(null);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.store_container:

                if(!getInterval().hasValidTitle()){
                    showNoTitleSnackbar();
                    return;
                }

                if(!getInterval().hasValidDates()){
                    showEndBeforeStartSnackbar();
                    return;
                }

                ((IntervalDetailsFragmentInterface)getActivity()).onStore(getInterval());

                String snackbarMessage = "";
                switch (getViewType()){
                    case VACATION:
                        snackbarMessage = getString(R.string.vacation_created);
                        break;
                    case SICK_LEAVE:
                        snackbarMessage = getString(R.string.sick_leave_created);
                        break;
                    default:
                        break;
                }

                ((FragmentSnackbarInterface) getActivity()).onFragmentSnackbarRequest(snackbarMessage, Snackbar.LENGTH_SHORT);
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            case R.id.start_date:

                DatePickerFragment startPicker = DatePickerFragment.newInstance(getInterval().getStartDate() == WorkEntry.INVALID_LONG ? System.currentTimeMillis() : getInterval().getStartDate());
                startPicker.setTargetFragment(this, START_DATE_REQUEST_CODE);
                startPicker.show(getChildFragmentManager(), "startDatePicker");
                break;
            case R.id.end_date:

                DatePickerFragment endPicker = DatePickerFragment.newInstance(getInterval().getEndDate() == WorkEntry.INVALID_LONG ? System.currentTimeMillis() : getInterval().getEndDate());
                endPicker.setTargetFragment(this, END_DATE_REQUEST_CODE);
                endPicker.show(getChildFragmentManager(), "endDatePicker");
                break;
            default:
                break;
        }
    }

    @Override
    public void onDatePickerComplete(long timestamp, int targetRequestCode) {

        switch (targetRequestCode){
            case START_DATE_REQUEST_CODE:
                getInterval().setStartDate(timestamp);
                mStartDate.setText(DateUtils.getDate(timestamp));
                setDataChanged();
                break;
            case END_DATE_REQUEST_CODE:
                getInterval().setEndDate(timestamp);
                mEndDate.setText(DateUtils.getDate(timestamp));
                setDataChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

        switch(v.getId()){
            case R.id.title:
                if(mTitleCounter != null) {
                    mTitleCounter.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
                break;
            case R.id.description:
                if(mTitleCounter != null) {
                    mDescriptionCounter.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onBackPressed() {

        return onActivityFinishFragmentRequest();
    }

    @Override
    public boolean onActivityFinishFragmentRequest() {

        if(!hasDataChanged()){
            return false;
        }

        showExitWithoutSavingDialog();
        return true;
    }

    public void showExitWithoutSavingDialog(){
        SimpleDialogFragment newFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.dialog_text_exit_without_saving),
                getString(R.string.dialog_button_exit).toUpperCase(Locale.getDefault()),
                getString(R.string.dialog_button_stay).toUpperCase(Locale.getDefault()),
                SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG,
                null);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    public void showNoTitleSnackbar(){
        Snackbar.make(mRootLayout, getString(R.string.please_enter_title), Snackbar.LENGTH_SHORT).show();
    }

    public void showEndBeforeStartSnackbar(){
        Snackbar.make(mRootLayout, getString(R.string.block_invalid_dates), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveDialogResponse(int responseCode, Parcelable data) {

        switch (responseCode){
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:

                // TODO Did we modify the old data?

                ((FragmentNavigationInterface) getActivity()).onFragmentFinished();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeDialogResponse(int responseCode) {
        switch (responseCode){
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:
            default:
                break;
        }
    }
}
