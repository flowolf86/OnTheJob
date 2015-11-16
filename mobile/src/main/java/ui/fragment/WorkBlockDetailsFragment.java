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
import android.support.v7.widget.CardView;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.WorkBlock;
import data.WorkEntry;
import data.factory.WorkBlockFactory;
import ui.base.BaseFragment;
import ui.dialog.SimpleDialogFragment;
import ui.dialog.TimePickerFragment;
import util.DateUtils;
import util.TextUtils;

public class WorkBlockDetailsFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener, TimePickerFragment.TimePickerCallback, SimpleDialogFragment.SimpleDialogCallback{

    public static final String FRAGMENT_TAG = "work_block_details_fragment";

    /*
        IntDefs
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INITIAL_STATE, CREATE_STATE, EDIT_STATE})
    public @interface FragmentState{ }

    public static final int INITIAL_STATE = 10;
    public static final int CREATE_STATE = 20;
    public static final int EDIT_STATE = 30;

    private static final int START_TIME_REQUEST_CODE = 200;
    private static final int END_TIME_REQUEST_CODE = 201;

    /*
        Variables & Statics
     */
    @Bind(R.id.store_block) FloatingActionButton mStoreBlockButton;

    @Bind(R.id.root) CoordinatorLayout mRootLayout;

    @Bind(R.id.title) EditText mTitleText;
    @Bind(R.id.title_counter) TextView mTitleCounter;

    @Bind(R.id.description) EditText  mDescriptionText;
    @Bind(R.id.description_counter) TextView mDescriptionCounter;

    @Bind(R.id.start_time) EditText mStartTime;
    @Bind(R.id.end_time) EditText mEndTime;

    @Bind(R.id.category_view) CardView mCategoryView;
    @Bind(R.id.category_icon) ImageView mCategoryIcon;
    @Bind(R.id.category_selector) ImageView mCategorySelector;
    @Bind(R.id.category_title) TextView mCategoryTitle;
    @Bind(R.id.category_subtitle) TextView mCategorySubtitle;

    private boolean mHasDataChanged = false;

    private static final String ARG_WORK_BLOCK = "work_block";
    private static final String ARG_WORK_ENTRY = "work_entry";
    private static final String ARG_MODE = "fragment_mode";

    private static int TITLE_LENGTH = 0;
    private static int DESCRIPTION_LENGTH = 0;

    private WorkBlock mArgWorkBlock = null;
    private @FragmentState int mArgState = INITIAL_STATE;

    /*
        Interface
     */

    public interface WorkBlockDetailsInterface {
        void onWorkBlockCreated(@NonNull WorkBlock block);
        void onSelectCategory(@NonNull WorkBlock block);
    }

    public WorkBlockDetailsFragment() { }

    public static WorkBlockDetailsFragment newInstance(@NonNull WorkEntry workEntry, @Nullable WorkBlock workBlock, @FragmentState int state) {

        if(workBlock == null && state == EDIT_STATE){
            throw new IllegalArgumentException("WorkBlock can not be null when trying to display EDIT_STATE / RETAIN_STATE.");
        }

        WorkBlockDetailsFragment f = new WorkBlockDetailsFragment();

        Bundle args = new Bundle();

        args.putParcelable(ARG_WORK_ENTRY, workEntry);
        // A default one will be created later
        if(workBlock != null) {
            args.putParcelable(ARG_WORK_BLOCK, workBlock);
        }

        args.putInt(ARG_MODE, state);
        f.setArguments(args);

        return f;
    }

    /*
        Fragment data
     */

    private WorkBlock getWorkBlock(){

        if(mArgWorkBlock == null){
            mArgWorkBlock = getArguments().getParcelable(ARG_WORK_BLOCK);

            if(mArgWorkBlock == null){
                WorkEntry parent = getArguments().getParcelable(ARG_WORK_ENTRY);

                if(parent != null) {
                    setWorkBlock(parent , null);
                } else {
                    throw new IllegalArgumentException("A work block cannot exist without a parent work entry.");
                }
            }
        }
        return mArgWorkBlock;
    }

    private void setWorkBlock(@NonNull WorkEntry parent, @Nullable WorkBlock block){

        mArgWorkBlock = block == null ? WorkBlockFactory.buildNewEmptyWorkBlock(parent) : block;

    }

    private WorkEntry getWorkEntry(){

        return getArguments().getParcelable(ARG_WORK_ENTRY);
    }

    private @FragmentState int getViewState(){

        if(mArgState == INITIAL_STATE){

            @FragmentState int state = getArguments().getInt(ARG_MODE);
            setViewState(state);
        }
        return mArgState;
    }

    private void setViewState(@FragmentState int state){
        mArgState = state;
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
        View view = inflater.inflate(R.layout.fragment_work_block_details, container, false);

        ButterKnife.bind(this, view);
        getDefaultIntegers();
        refreshFragmentData(getViewState());
        setOnClickListeners();

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
                && activity instanceof FragmentBackHandlerInterface;

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

    private void refreshFragmentData(int viewState) {

        if(viewState == EDIT_STATE){

            setToolbarTitle(R.string.title_edit_work_block);

            mTitleText.setText(getWorkBlock().getTitle());
            mTitleCounter.setText(getString(R.string.default_text_counter, TextUtils.isValidText(getWorkBlock().getTitle())? getWorkBlock().getTitle().length() : 0, TITLE_LENGTH));

            mDescriptionText.setText(getWorkBlock().getText());
            mDescriptionCounter.setText(getString(R.string.default_text_counter, TextUtils.isValidText(getWorkBlock().getText())? getWorkBlock().getText().length() : 0, DESCRIPTION_LENGTH));

            if(getWorkBlock().work_start != WorkBlock.INVALID_LONG) {
                mStartTime.setText(DateUtils.getTime(getWorkBlock().work_start));
            }

            if(getWorkBlock().work_end != WorkBlock.INVALID_LONG) {
                mEndTime.setText(DateUtils.getTime(getWorkBlock().work_end));
            }

            setCategory();

        } else if(viewState == CREATE_STATE){

            setToolbarTitle(R.string.title_new_work_block);

            mTitleText.setText("");
            mTitleCounter.setText(getString(R.string.default_text_counter, 0, TITLE_LENGTH));
            mDescriptionText.setText("");
            mDescriptionCounter.setText(getString(R.string.default_text_counter, 0, DESCRIPTION_LENGTH));

            setCategory();
        }
    }

    private void setCategory(){

        mCategorySelector.setColorFilter(getWorkBlock().getCategory().color, PorterDuff.Mode.ADD);
        mCategoryIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), getWorkBlock().getIconId()));
        mCategoryTitle.setText(getWorkBlock().getCategory().name);
        mCategorySubtitle.setText(getWorkBlock().getCategory().description);
    }

    private void restoreData() {

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        outer : for(WorkEntry entry : dataCacheHelper.getCacheDbRestoreData()){
            if(entry.getId() == getWorkEntry().getId()){
                for(WorkBlock block : entry.getWorkBlocks()){
                    if(block._id == getWorkBlock()._id){
                        getWorkBlock().restore(block);
                        break outer;
                    }
                }
            }
        }
    }

    /*
        Listeners
     */

    private void setOnClickListeners() {
        mCategoryView.setOnClickListener(this);

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
                getWorkBlock().setTitle(s.toString());
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
                getWorkBlock().setText(s.toString());
                mDescriptionCounter.setText(getString(R.string.default_text_counter, s.length(), DESCRIPTION_LENGTH));
                setDataChanged();
            }
        });

        mStartTime.setOnClickListener(this);
        mEndTime.setOnClickListener(this);
        mStoreBlockButton.setOnClickListener(this);
    }

    private void unsetOnFocusChangedListeners() {

        mTitleText.setOnFocusChangeListener(null);
        mDescriptionText.setOnFocusChangeListener(null);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.store_block:

                if(!getWorkBlock().isComplete()){
                    showIncompleteWorkBlockSnackbar();
                    return;
                }

                if(!getWorkBlock().hasValidDates()){
                    showInvalidDatesWorkBlockSnackbar();
                    return;
                }

                if(!TextUtils.isValidText(getWorkBlock().getTitle())){
                    getWorkBlock().setTitle(getString(R.string.work_block));
                }

                ((WorkBlockDetailsInterface)getActivity()).onWorkBlockCreated(getWorkBlock());
                ((FragmentSnackbarInterface) getActivity()).onFragmentSnackbarRequest(getString(R.string.work_block_added), Snackbar.LENGTH_SHORT);
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            case R.id.start_time:

                TimePickerFragment startPicker = TimePickerFragment.newInstance(getWorkBlock().work_start == WorkEntry.INVALID_LONG ? System.currentTimeMillis() : getWorkBlock().work_start);
                startPicker.setTargetFragment(this, START_TIME_REQUEST_CODE);
                startPicker.show(getChildFragmentManager(), "startTimePicker");
                break;
            case R.id.end_time:

                TimePickerFragment endPicker = TimePickerFragment.newInstance(getWorkBlock().work_end == WorkEntry.INVALID_LONG ? System.currentTimeMillis() : getWorkBlock().work_end);
                endPicker.setTargetFragment(this, END_TIME_REQUEST_CODE);
                endPicker.show(getChildFragmentManager(), "endTimePicker");
                break;
            case R.id.category_view:

                ((WorkBlockDetailsInterface)getActivity()).onSelectCategory(getWorkBlock());
                break;
            default:
                break;
        }
    }

    @Override
    public void onTimePickerComplete(long timestamp, int targetRequestCode) {

        switch (targetRequestCode){
            case START_TIME_REQUEST_CODE:
                getWorkBlock().work_start = timestamp;
                mStartTime.setText(DateUtils.getTime(timestamp));
                setDataChanged();
                break;
            case END_TIME_REQUEST_CODE:
                getWorkBlock().work_end = timestamp;
                mEndTime.setText(DateUtils.getTime(timestamp));
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

    public void showIncompleteWorkBlockSnackbar(){
        Snackbar.make(mRootLayout, getString(R.string.block_is_incomplete), Snackbar.LENGTH_SHORT).show();
    }

    public void showInvalidDatesWorkBlockSnackbar(){
        Snackbar.make(mRootLayout, getString(R.string.block_invalid_times), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onPositiveDialogResponse(int responseCode, Parcelable data) {

        switch (responseCode){
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:

                // Restore work block in cache if we don't want to save...
                // This serves performance reasons so that we don't have to refresh the cache from db
                restoreData();

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
