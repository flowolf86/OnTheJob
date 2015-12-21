package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.WorkEntry;
import data.factory.WorkEntryFactory;
import ui.base.BaseFragment;
import ui.dialog.DatePickerFragment;
import ui.dialog.SimpleDialogFragment;
import util.DateUtils;
import util.TextUtils;


public class WorkEntryDetailsFragment extends BaseFragment implements View.OnClickListener, View.OnFocusChangeListener, DatePickerFragment.DatePickerCallback, SimpleDialogFragment.SimpleDialogCallback{

    /*
        IntDefs
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INITIAL_STATE, CREATE_STATE, EDIT_STATE, RETAIN_STATE})
    public @interface FragmentState{ }

    public static final int INITIAL_STATE = 10;
    public static final int CREATE_STATE = 20;
    public static final int EDIT_STATE = 30;
    public static final int RETAIN_STATE = 40;

    /*
        Variables & Statics
     */
    @Bind(R.id.store_entry) FloatingActionButton mStoreEntryButton;

    @Bind(R.id.root) CoordinatorLayout mRootLayout;

    @Bind(R.id.title) EditText mTitleText;
    @Bind(R.id.title_counter) TextView mTitleCounter;

    @Bind(R.id.description) EditText  mDescriptionText;
    @Bind(R.id.description_counter) TextView mDescriptionCounter;

    @Bind(R.id.date) EditText mDateText;
    @Bind(R.id.blocks_subtitle) TextView  mBlocksSubtitle;

    @Bind(R.id.blocks_view) RelativeLayout mBlocksView;

    public static final String FRAGMENT_TAG = "work_entry_details_fragment";

    private static final String ARG_WORK_ENTRY = "work_entry";
    private static final String ARG_MODE = "fragment_mode";
    private static final String ARG_TOOLBAR_TITLE = "toolbar_title";

    private static int TITLE_LENGTH = 0;
    private static int DESCRIPTION_LENGTH = 0;
    private static int REQUEST_CODE = 100;

    private WorkEntry mArgWorkEntry = null;
    private @FragmentState int mArgState = INITIAL_STATE;

    /*
        Interface
     */

    public interface WorkEntryDetailsInterface {
        void onWorkBlockListRequest(@NonNull WorkEntry entry);
    }

    public WorkEntryDetailsFragment() { }

    public static WorkEntryDetailsFragment newInstance(@Nullable WorkEntry workEntry, @FragmentState int state, @Nullable String toolbarTitle) {

        if(workEntry == null && (state == EDIT_STATE || state == RETAIN_STATE)){
            throw new IllegalArgumentException("WorkEntry can not be null when trying to display EDIT_STATE / RETAIN_STATE.");
        }

        WorkEntryDetailsFragment f = new WorkEntryDetailsFragment();

        Bundle args = new Bundle();

        // A default one will be created later
        if(workEntry != null) {
            args.putParcelable(ARG_WORK_ENTRY, workEntry);
        }

        if(toolbarTitle != null){
            args.putString(ARG_TOOLBAR_TITLE, toolbarTitle);
        }

        args.putInt(ARG_MODE, state);
        f.setArguments(args);

        return f;
    }

    /*
        TextWatcher
     */

    private TextWatcher mTitleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            getWorkEntry().setTitle(s.toString());
            mTitleCounter.setText(getString(R.string.default_text_counter, s.length(), TITLE_LENGTH));
        }
    };

    private TextWatcher mDescriptionTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            getWorkEntry().setText(s.toString());
            mDescriptionCounter.setText(getString(R.string.default_text_counter, s.length(), DESCRIPTION_LENGTH));
        }
    };

    /*
        Fragment data
     */

    private @Nullable WorkEntry getWorkEntry(){

        if(mArgWorkEntry == null){
            mArgWorkEntry = getArguments().getParcelable(ARG_WORK_ENTRY);

            if(mArgWorkEntry == null){
                setWorkEntry(null);
            }
        }
        return mArgWorkEntry;
    }

    private void setWorkEntry(@Nullable WorkEntry entry){

        mArgWorkEntry = entry == null ? WorkEntryFactory.buildNewEmptyManualWorkEntry(): entry;
    }

    private @FragmentState int getViewState(){

        if(mArgState == INITIAL_STATE){

            @FragmentState int state = getArguments().getInt(ARG_MODE);
            mArgState = state;
        }
        return mArgState;
    }

    private void setViewState(@FragmentState int state){
        mArgState = state;
    }

    /*
        Fragment to fragment communication
     */

    // This happens when we are in create mode and move to blocks and back, otherwise it would set the edit texts to blank
    public void setRetainState(){
        setViewState(RETAIN_STATE);
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
        View view = inflater.inflate(R.layout.fragment_work_entry_details, container, false);

        ButterKnife.bind(this, view);
        getDefaultIntegers();
        refreshFragmentData(getViewState());
        setOnClickListeners();

        return view;
    }

    private void restoreData() {

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        for(WorkEntry entry : dataCacheHelper.getCacheDbRestoreData()){
            // Check for ID from db entries and creation time, which is unique too, for entries in cache
            if(entry.getId() == getWorkEntry().getId() || entry.creation_time == getWorkEntry().creation_time){
                getWorkEntry().restore(entry);
                break;
            }
        }
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
        unsetOnFocusAndTextChangedListeners();
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.delete, menu);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface
                && activity instanceof FragmentBackHandlerInterface
                && activity instanceof WorkEntryDetailsInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    /*
        Logic
     */

    private void getDefaultIntegers() {
        TITLE_LENGTH = getResources().getInteger(R.integer.title_length);
        DESCRIPTION_LENGTH = getResources().getInteger(R.integer.description_length);
    }

    private boolean hasDataChanged(){
        return getWorkEntry().hasChanged();
    }

    private void refreshFragmentData(int viewState) {

        // Set custom toolbar title
        if(getArguments().getString(ARG_TOOLBAR_TITLE) != null){
            super.setToolbarTitle(getArguments().getString(ARG_TOOLBAR_TITLE));
        }

        if(viewState == EDIT_STATE || viewState == RETAIN_STATE){

            setHasOptionsMenu(true);

            // Restore work entry
            if(getWorkEntry().getWorkBlocks().size() > 0) {
                int[] duration = getWorkEntry().getTotalWorkBlockDuration();
                mBlocksSubtitle.setText(getString(R.string.block_subtitle, duration[0], duration[1], getWorkEntry().getWorkBlocks().size()));
            }

            mTitleText.setText(getWorkEntry().getTitle());
            mTitleCounter.setText(getString(R.string.default_text_counter, getWorkEntry().getTitle().length(), TITLE_LENGTH));

            mDescriptionText.setText(getWorkEntry().getText());
            mDescriptionCounter.setText(getString(R.string.default_text_counter, getWorkEntry().getText().length(), DESCRIPTION_LENGTH));

            mDateText.setText(DateUtils.getDate(getWorkEntry().getDate()));

            int[] duration = getWorkEntry().getTotalWorkBlockDuration();

            if(getWorkEntry().getWorkBlocks().size() > 0) {
                mBlocksSubtitle.setText(getString(R.string.block_subtitle, duration[0], duration[1], getWorkEntry().getWorkBlocks().size()));
            }else{
                mBlocksSubtitle.setText(getString(R.string.block_subtitle_empty));
            }

        } else if(viewState == CREATE_STATE){

            setHasOptionsMenu(false);

            mTitleText.setText("");
            mTitleCounter.setText(getString(R.string.default_text_counter, 0, TITLE_LENGTH));
            mDescriptionText.setText("");
            mDescriptionCounter.setText(getString(R.string.default_text_counter, 0, DESCRIPTION_LENGTH));
            mDateText.setText("");
            mBlocksSubtitle.setText(getString(R.string.block_subtitle_empty));
        }
    }

    private void setOnClickListeners() {
        mStoreEntryButton.setOnClickListener(this);

        mTitleText.setOnFocusChangeListener(this);
        mTitleText.addTextChangedListener(mTitleTextWatcher);

        mDescriptionText.setOnFocusChangeListener(this);
        mDescriptionText.addTextChangedListener(mDescriptionTextWatcher);

        mDateText.setOnClickListener(this);
        mBlocksView.setOnClickListener(this);
    }

    private void unsetOnFocusAndTextChangedListeners() {

        mTitleText.setOnFocusChangeListener(null);
        mDescriptionText.setOnFocusChangeListener(null);

        mTitleText.removeTextChangedListener(mTitleTextWatcher);
        mDescriptionText.removeTextChangedListener(mDescriptionTextWatcher);
    }

    /**
     *
     */
    public void switchDataCreateState(){

        setWorkEntry(null);
        setViewState(CREATE_STATE);
        getArguments().putString(ARG_TOOLBAR_TITLE, getString(R.string.title_new_work_day));
        refreshFragmentData(getViewState());
    }

    public void switchDataEditState(WorkEntry workEntry){

        setWorkEntry(workEntry);
        setViewState(EDIT_STATE);
        getArguments().putString(ARG_TOOLBAR_TITLE, getString(R.string.title_edit_work_day));
        refreshFragmentData(getViewState());
    }

    public void switchDataRetainState(WorkEntry workEntry){

        setWorkEntry(workEntry);
        refreshFragmentData(RETAIN_STATE);
    }

    public void storeEntry(){

        if(!getWorkEntry().hasValidDate()){
            showEnterDateSnackbar();
            return;
        }

        //TODO Put this elsewhere
        String title = DateUtils.getDayOfWeekString(getWorkEntry().getDate());
        String description = getString(R.string.default_description);

        if(TextUtils.isValidText(mTitleText.getText().toString())){
            title = mTitleText.getText().toString();
        }

        if(TextUtils.isValidText(mDescriptionText.getText().toString())){
            description = mDescriptionText.getText().toString();
        }

        getWorkEntry().setTitle(title);
        getWorkEntry().setText(description);

        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());

        if(getViewState() == CREATE_STATE) {
            dataCacheHelper.addNewEntry(getWorkEntry(), null);
        }else if(getViewState() == EDIT_STATE  || getViewState() == RETAIN_STATE){
            dataCacheHelper.modifyWorkEntry(getWorkEntry(), null);
        }

        ((FragmentSnackbarInterface)getActivity()).onFragmentSnackbarRequest(getString(R.string.work_entry_stored), Snackbar.LENGTH_SHORT);
        ((BaseFragment.FragmentNavigationInterface)getActivity()).onFragmentFinished();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.store_entry:
                storeEntry();
                break;
            case R.id.date:
                DialogFragment newFragment = DatePickerFragment.newInstance(getWorkEntry().getDate() == WorkEntry.INVALID_LONG ? System.currentTimeMillis() : getWorkEntry().getDate());
                newFragment.setTargetFragment(this, 0);
                newFragment.show(getChildFragmentManager(), "datePicker");
                break;
            case R.id.blocks_view:

                if(!getWorkEntry().hasValidDate()){
                    showEnterDateSnackbar();
                    return;
                }

                ((WorkEntryDetailsInterface) getActivity()).onWorkBlockListRequest(getWorkEntry());

                // Expect that the user navigates back to this fragment after a while.
                // If so, we cannot be in CREATE_STATE anymore because that would wipe
                // the input fields.

                //if(getViewState() == EDIT_STATE) {
                //    getArguments().putString(ARG_TOOLBAR_TITLE, getString(R.string.title_edit_work_day));
                //}
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
    public void onDatePickerComplete(long timestamp, int targetRequestCode) {

        // Check if an entry for today already exists
        DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        WorkEntry todayEntry = dataCacheHelper.getWorkEntryForTimestampDay(timestamp);

        if(todayEntry != null){
            showEditWorkEntryDialog(todayEntry);
            return;
        }

        // Retain means, we're editing a history item! Therefore
        // We do not want do discard all data but keep it and only
        // change the date.
        if(getViewState() != RETAIN_STATE) {
            switchDataCreateState();
        }

        // Set work day reference time to new day
        getWorkEntry().setDate(timestamp);

        // Set date text view to selected date
        mDateText.setText(DateUtils.getDate(timestamp));
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

    public void showEditWorkEntryDialog(WorkEntry todayEntry){
        SimpleDialogFragment newFragment = SimpleDialogFragment.newInstance(
                getString(R.string.dialog_title_already_logged),
                getString(R.string.dialog_text_edit_existing_work_day, DateUtils.getDateShort(todayEntry.getDate())),
                getString(R.string.dialog_button_edit).toUpperCase(Locale.getDefault()),
                getString(R.string.dialog_button_cancel).toUpperCase(Locale.getDefault()),
                SimpleDialogFragment.EDIT_WORK_DAY_DIALOG,
                todayEntry);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getChildFragmentManager(), "dialog");
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

    public void showDeleteEntryDialog(){
        SimpleDialogFragment newFragment = SimpleDialogFragment.newInstance(
                null,
                getString(R.string.dialog_text_delete_work_day),
                getString(R.string.dialog_button_yes).toUpperCase(Locale.getDefault()),
                getString(R.string.dialog_button_no).toUpperCase(Locale.getDefault()),
                SimpleDialogFragment.DELETE_ENTRY_DIALOG,
                null);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    public void showEnterDateSnackbar(){
        Snackbar snackbar = Snackbar.make(mRootLayout, getString(R.string.please_choose_date), Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    public void onPositiveDialogResponse(int responseCode, Parcelable data) {

        switch (responseCode){
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:

                // Restore work entry in cache if we don't want to save...
                // This serves performance reasons so that we don't have to refresh the cache from db
                restoreData();

                ((BaseFragment.FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            case SimpleDialogFragment.EDIT_WORK_DAY_DIALOG:
                switchDataEditState((WorkEntry)data);
                break;
            case SimpleDialogFragment.DELETE_ENTRY_DIALOG:
                deleteWorkEntry();
                ((FragmentNavigationInterface)getActivity()).onFragmentFinished();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeDialogResponse(int responseCode) {
        switch (responseCode){
            case SimpleDialogFragment.EDIT_WORK_DAY_DIALOG:
                break;
            case SimpleDialogFragment.EXIT_WITHOUT_SAVING_DIALOG:
            default:
                break;
        }
    }

    /*
        OptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteEntryDialog();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteWorkEntry() {
        final DataCacheHelper dataCacheHelper = new DataCacheHelper(getContext());
        dataCacheHelper.deleteWorkEntry(getWorkEntry(), null);
    }
}
