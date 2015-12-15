package ui.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.florianwolf.onthejob.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cache.DataCacheHelper;
import data.Category;
import data.Interval;
import data.WorkBlock;
import data.WorkEntry;
import data.manager.SharedPreferencesManager;
import listing.MapFragmentState;
import ui.base.BaseActivity;
import ui.base.BaseFragment;
import ui.fragment.CalendarFragment;
import ui.fragment.CategoryDetailsFragment;
import ui.fragment.CategoryListFragment;
import ui.fragment.DayOffFragment;
import ui.fragment.DrawerFragment;
import ui.fragment.IntervalDetailsFragment;
import ui.fragment.IntervalListFragment;
import ui.fragment.MainMenuFragment;
import ui.fragment.TodayWidgetFragment;
import ui.fragment.WeekWidgetFragment;
import ui.fragment.WorkBlockDetailsFragment;
import ui.fragment.WorkBlockListFragment;
import ui.fragment.WorkEntryDetailsFragment;
import ui.fragment.WorkEntryListFragment;
import util.AddressUtils;
import util.CSVUtils;
import util.PermissionUtils;

public class MainActivity extends BaseActivity implements
        BaseFragment.FragmentSnackbarInterface,
        DrawerFragment.OnDrawerFragmentActionListener,
        MainMenuFragment.OnMainMenuFragmentActionListener,
        WorkEntryDetailsFragment.WorkEntryDetailsInterface,
        WorkBlockDetailsFragment.WorkBlockDetailsInterface,
        WorkBlockListFragment.WorkBlockListFragmentInterface,
        WorkEntryListFragment.WorkEntryListFragmentInterface,
        DayOffFragment.DayOffFragmentInterface,
        WeekWidgetFragment.WeekWidgetInterface,
        TodayWidgetFragment.TodayWidgetInterface,
        CategoryListFragment.CategoryListFragmentInterface,
        IntervalDetailsFragment.IntervalDetailsFragmentInterface,
        IntervalListFragment.IntervalListFragmentInterface,
        CalendarFragment.CalendarFragmentInterface{

    private BaseFragment mSelectedFragment;

    @Bind(R.id.root_drawer) DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private final static int ACTIVITY_TRANSITION_DELAY = 200;

    String mLastFragmentTag = null;

    /*
        Lifecycle
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        super.setUpToolbar(R.id.toolbar, R.string.app_name, true);

        setUpNavigationDrawer();
        setUpFragments();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode){
            case RESULT_OK:
                if(data != null && requestCode == MapActivity.REQUEST_ADDRESS){

                    final Address address = data.getParcelableExtra(MapActivity.EXTRA_ADDRESS);
                    @MapFragmentState.IMapFragmentState int state = data.getIntExtra(MapActivity.EXTRA_STATE, MapFragmentState.UNKNOWN);
                    updateAddressData(address, state);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.STORAGE_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onExportSelected();
                } else {
                    // Permission Denied
                    // TODO
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*
        Logic
     */

    private void setUpFragments() {
        displayDrawerFragment();
        displayMenuFragment();
    }

    public void displayMenuFragment(){
        MainMenuFragment mainMenuFragment = MainMenuFragment.newInstance();
        super.replaceFragment(R.id.fragment_content_wrapper, mainMenuFragment, false, BaseActivity.ANIMATION_NONE, MainMenuFragment.FRAGMENT_TAG);
    }

    public void displayDrawerFragment(){
        DrawerFragment drawerFragment = DrawerFragment.newInstance();
        super.replaceFragment(R.id.fragment_drawer_wrapper, drawerFragment, false, BaseActivity.ANIMATION_NONE, DrawerFragment.FRAGMENT_TAG);
    }

    public void displayWorkEntryDetailsFragment(@Nullable WorkEntry workEntry, @Nullable String customToolbarTitle, int animation){
        WorkEntryDetailsFragment workEntryDetailsFragment = WorkEntryDetailsFragment.newInstance(workEntry, workEntry == null ? WorkEntryDetailsFragment.CREATE_STATE : WorkEntryDetailsFragment.RETAIN_STATE, customToolbarTitle);
        super.replaceFragment(R.id.fragment_content_wrapper, workEntryDetailsFragment, true, animation, WorkEntryDetailsFragment.FRAGMENT_TAG);
    }

    public void displayIntervalDetailsFragment(@Nullable Interval interval, @IntervalDetailsFragment.DayOffType int type){
        IntervalDetailsFragment dayOffPlanerFragment = IntervalDetailsFragment.newInstance(interval, type);
        super.replaceFragment(R.id.fragment_content_wrapper, dayOffPlanerFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, IntervalDetailsFragment.FRAGMENT_TAG);
    }

    public void displayWorkEntryListFragment(@Nullable ArrayList<WorkEntry> entryList, @Nullable String customToolbarTitle, int animation){
        WorkEntryListFragment workEntryListFragment = WorkEntryListFragment.newInstance(entryList, customToolbarTitle);
        super.replaceFragment(R.id.fragment_content_wrapper, workEntryListFragment, true, animation, WorkEntryListFragment.FRAGMENT_TAG);
    }

    public void displayWorkEntryCalendarFragment(@Nullable String customToolbarTitle){
        CalendarFragment calendarFragment = CalendarFragment.newInstance(customToolbarTitle);
        super.replaceFragment(R.id.fragment_content_wrapper, calendarFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, CalendarFragment.FRAGMENT_TAG);
    }

    public void displayWorkBlockListFragment(WorkEntry workEntry, @Nullable String customToolbarTitle){
        WorkBlockListFragment workBlockListFragment = WorkBlockListFragment.newInstance(workEntry, customToolbarTitle);
        super.replaceFragment(R.id.fragment_content_wrapper, workBlockListFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, WorkBlockListFragment.FRAGMENT_TAG);
    }

    public void displayWorkBlockDetailsFragment(@NonNull WorkEntry workEntry, @Nullable WorkBlock workBlock, int animation){
        WorkBlockDetailsFragment workBlockDetailsFragment = WorkBlockDetailsFragment.newInstance(workEntry, workBlock, workBlock == null ? WorkBlockDetailsFragment.CREATE_STATE : WorkBlockDetailsFragment.EDIT_STATE);
        super.replaceFragment(R.id.fragment_content_wrapper, workBlockDetailsFragment, true, animation, WorkBlockDetailsFragment.FRAGMENT_TAG);
    }

    public void displayCategoriesFragment(@Nullable WorkBlock workBlock){
        CategoryListFragment categoryListFragment = CategoryListFragment.newInstance(workBlock);
        super.replaceFragment(R.id.fragment_content_wrapper, categoryListFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, CategoryListFragment.FRAGMENT_TAG);
    }

    public void displayDayOffFragment(){
        DayOffFragment dayOffFragment = DayOffFragment.newInstance();
        super.replaceFragment(R.id.fragment_content_wrapper, dayOffFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, DayOffFragment.FRAGMENT_TAG);
    }

    public void displayCreateOrEditCategoryFragment(@Nullable Category category){
        CategoryDetailsFragment categoryDetailsFragment = CategoryDetailsFragment.newInstance(category);
        super.replaceFragment(R.id.fragment_content_wrapper, categoryDetailsFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, CategoryDetailsFragment.FRAGMENT_TAG);
    }

    public void displayIntervalListFragment(@Category.CategoryMode int mode){

        String toolbarTitle;
        switch (mode){
            case Category.VACATION:
                toolbarTitle = getString(R.string.manage_vacation_title);
                break;
            case Category.SICK_LEAVE:
                toolbarTitle = getString(R.string.manage_sick_leave_title);
                break;
            default:
                toolbarTitle = getString(R.string.manage);
        }

        IntervalListFragment intervalsListFragment = IntervalListFragment.newInstance(mode, toolbarTitle);
        super.replaceFragment(R.id.fragment_content_wrapper, intervalsListFragment, true, BaseActivity.ANIMATION_SLIDE_IN_OUT, CategoryListFragment.FRAGMENT_TAG);
    }

    private void updateAddressData(@NonNull final Address address, @MapFragmentState.IMapFragmentState int state) {

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        AddressUtils.storeAddressInSharedPreferences(this, address, state);

        // Update geofences if enabled in settings
        if(sharedPreferencesManager.get(SharedPreferencesManager.ID_GEOFENCING, true)){
            if(state == MapFragmentState.PRIMARY_WORK_ADDRESS) {
                super.onUpdatePrimaryGeofenceRequest();
            }else if(state == MapFragmentState.SECONDARY_WORK_ADDRESS){
                super.onUpdateSecondaryGeofenceRequest();
            }
        }

        refreshDrawerFragment(address, state);
    }

    /*
        Drawer
     */

    private void setUpNavigationDrawer() {

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_closed) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void refreshDrawerFragment(@NonNull final Address address, @MapFragmentState.IMapFragmentState int state) {

        final DrawerFragment drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentByTag(DrawerFragment.FRAGMENT_TAG);
        if(drawerFragment != null){
            switch (state){
                case MapFragmentState.PRIMARY_WORK_ADDRESS:
                    drawerFragment.setPrimaryWorkAddress(address);
                    break;
                case MapFragmentState.SECONDARY_WORK_ADDRESS:
                    drawerFragment.setSecondaryWorkAddress(address);
                    break;
                default:
                    break;
            }
        }
    }

    /*
        Fragment communication
     */

    @Override
    public void onFragmentStarted(String tag) {
        mLastFragmentTag = tag;
        handleDrawerIconState(tag);
    }

    public void handleDrawerIconState(String tag){

        // Ignore the drawer fragment which always gets loaded invisibly
        if(tag != null && tag.equals(DrawerFragment.FRAGMENT_TAG)){
            return;
        }

        if(tag == null || tag.equals(MainMenuFragment.FRAGMENT_TAG)){
            animateDrawerIconManually(false);
        }else{
            animateDrawerIconManually(true);
        }
    }

    /*
        Animation
     */

    private float mLastDrawerToogleAnimationValue = 0f;

    private void animateDrawerIconManually(final boolean toArrow){
        
        if(!toArrow) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

        final float end = toArrow ? 1f : 0f;

        // Skip the animation if we're already in the desired state
        if(mLastDrawerToogleAnimationValue == end){
            return;
        }

        ValueAnimator anim = ValueAnimator.ofFloat(mLastDrawerToogleAnimationValue, end);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                mDrawerToggle.onDrawerSlide(mDrawerLayout, slideOffset);
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                mLastDrawerToogleAnimationValue = end;
                if (toArrow) {

                    mDrawerToggle.setDrawerIndicatorEnabled(false);
                    mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }

    /*
        Fragment communication
     */

    @Override
    public void onFragmentSnackbarRequest(String text, int duration) {
        Snackbar.make(findViewById(R.id.root_drawer), text, duration).show();
    }

    /*
        Drawer interface
     */
    @Override
    public void onSettingsEntrySelected() {
        mDrawerLayout.closeDrawer(Gravity.LEFT);

        // Assure smooth animation and transition
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        }, ACTIVITY_TRANSITION_DELAY);
    }

    @Override
    @UiThread
    public void onPrimaryWorkAddressSelected() {

        final Address address = AddressUtils.getAddress(this, SharedPreferencesManager.ID_PRIMARY_WORK_ADDRESS);

        final Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_STATE, MapFragmentState.PRIMARY_WORK_ADDRESS);
        intent.putExtra(MapActivity.EXTRA_ADDRESS, address);
        //intent.putExtra(MapActivity.EXTRA_TOOLBAR_TITLE, /* title */);

        startActivityForResult(intent, MapActivity.REQUEST_ADDRESS);
    }

    @Override
    @UiThread
    public void onSecondaryWorkAddressSelected() {

        final Address address = AddressUtils.getAddress(this, SharedPreferencesManager.ID_SECONDARY_WORK_ADDRESS);

        final Intent intent = new Intent(MainActivity.this, MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_STATE, MapFragmentState.SECONDARY_WORK_ADDRESS);
        intent.putExtra(MapActivity.EXTRA_ADDRESS, address);
        //intent.putExtra(MapActivity.EXTRA_TOOLBAR_TITLE, /* title */);

        startActivityForResult(intent, MapActivity.REQUEST_ADDRESS);
    }

    @Override
    @UiThread
    public void onExportSelected(){

        boolean hasStoragePermission = PermissionUtils.checkStoragePermissions(this);
        if(!hasStoragePermission){
            // If permission is granted later, we get notified via callback
            return;
        }
        CSVUtils.exportData(this, getSupportFragmentManager());
    }

    @Override
    public void onStatisticsSelected(){
        mDrawerLayout.closeDrawer(Gravity.LEFT);

        // Assure smooth animation and transition
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            }
        }, ACTIVITY_TRANSITION_DELAY);
    }

    @Override
    public void onAddEntrySelected() {
        displayWorkEntryDetailsFragment(null, getString(R.string.title_new_work_day), BaseActivity.ANIMATION_SLIDE_IN_OUT);
    }

    @Override
    public void onHistorySelected() {
        displayWorkEntryCalendarFragment(getString(R.string.title_history));
    }

    @Override
    public void onDayOffSelected() {
        displayDayOffFragment();
    }

    /*
        WorkEntryListFragment
     */

    @Override
    public void onListEntryItemSelected(WorkEntry entry) {
        displayWorkEntryDetailsFragment(entry, getString(R.string.title_edit_work_day), BaseActivity.ANIMATION_SLIDE_IN_OUT);
    }

    /*
        WorkBlockListFragment
    */

    @Override
    public void onListBlockItemSelected(@NonNull WorkEntry workEntry, @NonNull WorkBlock workBlock) {
        displayWorkBlockDetailsFragment(workEntry, workBlock, BaseActivity.ANIMATION_SLIDE_IN_OUT);
    }

    @Override
    public void onAddWorkBlockRequest(@NonNull WorkEntry workEntry) {
        displayWorkBlockDetailsFragment(workEntry, null, BaseActivity.ANIMATION_SLIDE_UP_DOWN);
    }

    /*
        WorkEntryDetailsFragment
     */

    @Override
    public void onWorkBlockListRequest(@NonNull WorkEntry entry) {
        displayWorkBlockListFragment(entry, null);
    }

    /*
        WorkBlockDetailsFragment
     */

    @Override
    public void onWorkBlockCreated(@NonNull WorkBlock block) {

        WorkBlockListFragment workBlockListFragment = (WorkBlockListFragment) getSupportFragmentManager().findFragmentByTag(WorkBlockListFragment.FRAGMENT_TAG);
        if(workBlockListFragment != null) {
            workBlockListFragment.addWorkBlock(block);
        }
    }

    @Override
    public void onSelectCategory(@NonNull WorkBlock block) {
        displayCategoriesFragment(block);
    }

    /*
        WeekWidget
     */

    @Override
    public void onWeekWidgetInteraction(@NonNull ArrayList<WorkEntry> workEntryList, @Nullable String toolbarTitle) {
        displayWorkEntryListFragment(workEntryList, toolbarTitle, BaseActivity.ANIMATION_SLIDE_UP_DOWN);
    }

    /*
        TodayWidget
     */
    @Override
    public void onTodayWidgetInteraction() {

        final DataCacheHelper dataCacheHelper = new DataCacheHelper(this);
        final WorkEntry workEntry = dataCacheHelper.getWorkEntryForTimestampDay(System.currentTimeMillis());
        displayWorkEntryDetailsFragment(workEntry, workEntry == null ? getString(R.string.title_new_work_day) : getString(R.string.title_edit_work_day), BaseActivity.ANIMATION_SLIDE_UP_DOWN);
    }

    /*
        DayOffFragment#DayOffFragmentInterface
     */
    @Override
    public void onManageSickDays() {
        displayIntervalListFragment(Category.SICK_LEAVE);
    }

    @Override
    public void onManageVacationDays() {
        displayIntervalListFragment(Category.VACATION);
    }

    /*
        CategoryListFragment#CategoryListFragmentInterface
     */

    @Override
    public void onCategoryCreate() {
        displayCreateOrEditCategoryFragment(null);
    }

    @Override
    public void onCategoryEdit(@NonNull Category category) {
        displayCreateOrEditCategoryFragment(category);
    }

    /*
        IntervalListFragment#IntervalListFragmentInterface
     */

    @Override
    public void onListIntervalItemSelected(@NonNull Interval interval) {
        displayIntervalDetailsFragment(interval, interval.getCategory().getId() == Category.VACATION ? IntervalDetailsFragment.VACATION : IntervalDetailsFragment.SICK_LEAVE);
    }

    @Override
    public void onPlanDaysOff(@Category.CategoryMode int mode) {
        displayIntervalDetailsFragment(null, mode == Category.VACATION ? IntervalDetailsFragment.VACATION : IntervalDetailsFragment.SICK_LEAVE);
    }

    /*
       IntervalDetailsFragment#IntervalDetailsFragmentInterface
     */

    @Override
    public void onStore(@NonNull Interval interval) {
        interval.store(this, null);
    }

    /*
        CalendarFragment#CalendarFragmentInterface
     */
    @Override
    public void onDateSelected(WorkEntry workEntry) {
        displayWorkEntryDetailsFragment(workEntry, getString(R.string.title_edit_work_day), BaseActivity.ANIMATION_SLIDE_IN_OUT);
    }

    @Override
    public void onDisplayList() {
        final DataCacheHelper dataCacheHelper = new DataCacheHelper(this);
        displayWorkEntryListFragment(new ArrayList<>(dataCacheHelper.getAllWorkEntries()), getString(R.string.title_history), BaseActivity.ANIMATION_SLIDE_IN_OUT);
    }
}
