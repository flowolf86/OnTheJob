package ui.activity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.florianwolf.onthejob.R;
import com.google.android.gms.maps.model.LatLng;

import butterknife.Bind;
import butterknife.ButterKnife;
import configuration.WorkConfiguration;
import data.manager.SharedPreferencesManager;
import listing.MapFragmentState;
import ui.base.BaseActivity;
import ui.base.BaseFragment;
import ui.fragment.MapFragment;
import ui.fragment.SlideInMapPanelFragment;
import ui.fragment.SlideInSeekbarPanelFragment;

public class MapActivity extends BaseActivity implements BaseFragment.FragmentSnackbarInterface,
        SlideInMapPanelFragment.SlideInMapPanelInterface, SlideInSeekbarPanelFragment.SlideInSeekbarPanelFragmentInterface,
        MapFragment.MapFragmentInterface {

    public static final int REQUEST_ADDRESS = 100;

    public static final String EXTRA_TOOLBAR_TITLE = "extra_title";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_STATE = "extra_state";

    @Bind(R.id.root) FrameLayout mRootLayout;

    /*
        Lifecycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        super.setUpToolbar(R.id.toolbar, R.string.select_location, true);
        setUpCustomToolbarTitle(getIntent());
        setUpMapFragment(getIntent());
        configureSeekbarExtension();
    }

    private void configureSeekbarExtension() {

        SlideInSeekbarPanelFragment slideInSeekbarPanelFragment = getSlideInSeekbarPanelFragment();
        if(slideInSeekbarPanelFragment != null){
            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
            slideInSeekbarPanelFragment.setMaxValue(WorkConfiguration.DEFAULT_WORK_MAX_RADIUS);
            slideInSeekbarPanelFragment.setValue(sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_RADIUS, WorkConfiguration.DEFAULT_WORK_RADIUS));
            slideInSeekbarPanelFragment.setCallback(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_locate_me:
                final MapFragment mapFragment = getMapFragment();
                if(mapFragment != null) {
                    mapFragment.locateMe();
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
        Logic
     */

    private void setUpCustomToolbarTitle(@Nullable Intent intent) {

        if(intent != null){

            final String title = intent.getStringExtra(EXTRA_TOOLBAR_TITLE);
            if(title != null){
                super.setToolbarTitle(title);
            }
        }
    }

    private void setUpMapFragment(@Nullable Intent intent) {

        if(intent != null){

            final Address address = intent.getParcelableExtra(EXTRA_ADDRESS);
            @MapFragmentState.IMapFragmentState int state = intent.getIntExtra(EXTRA_STATE, MapFragmentState.UNKNOWN);

            displayMapFragment(address, state);
        } else {
            //TODO
        }
    }

    public void displayMapFragment(@Nullable final Address address, @MapFragmentState.IMapFragmentState int fragmentState){

        if(fragmentState == MapFragmentState.UNKNOWN){
            throw new IllegalArgumentException("Map fragment state must not be MapFragmentState.UNKNOWN");
        }

        final MapFragment mapFragment = MapFragment.newInstance(address, fragmentState);
        super.replaceFragment(R.id.fragment_content_wrapper, mapFragment, false, BaseActivity.ANIMATION_NONE, MapFragment.FRAGMENT_TAG);
    }

    public @Nullable MapFragment getMapFragment(){
        final FragmentManager fragmentManager = getSupportFragmentManager();
        return (MapFragment) fragmentManager.findFragmentByTag(MapFragment.FRAGMENT_TAG);
    }

    /*
        Snackbar interface
     */

    @Override
    public void onFragmentSnackbarRequest(String text, int duration) {

        hideSlideInPanelIfPossible(null);
        Snackbar.make(mRootLayout, text, duration).show();
    }

    /*
        Slide in map panel interface
     */

    @Override
    public void onPanelClicked(@NonNull Address address) {

        final Intent intent = new Intent();
        intent.putExtra(MapActivity.EXTRA_ADDRESS, address);

        if(getIntent() != null){
            intent.putExtra(MapActivity.EXTRA_STATE, getIntent().getIntExtra(EXTRA_STATE, MapFragmentState.UNKNOWN));
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPanelDismissRequest() {
        hideSlideInPanelIfPossible(null);
    }

    private void hideSlideInPanelIfPossible(@Nullable FragmentManager fm) {

        final FragmentManager fragmentManager = fm != null ? fm : getSupportFragmentManager();
        fragmentManager.popBackStack(SlideInMapPanelFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    /*
        Map fragment interface
     */

    @Override
    public void onMapLongClick(@NonNull final LatLng latLng) {

        hideSlideInPanelIfPossible(null);
        super.replaceFragment(R.id.slide_in_panel_container, SlideInMapPanelFragment.newInstance(latLng), true, ANIMATION_SLIDE_UP_DOWN, SlideInMapPanelFragment.FRAGMENT_TAG);
    }

    @Override
    public void onMapClick(@NonNull final LatLng latLng) {
        hideSlideInPanelIfPossible(null);
    }

    /*
        VerticalSeekbarView.VerticalSeekbarViewInterface
     */

    @Override
    public void onValueChanged(int value) {

        //TODO This can be performance improved with a short ref to the fragment...
        MapFragment mapFragment = getMapFragment();
        if(mapFragment != null){
            mapFragment.setCurrentCircleRadius(value, false);
        }
    }

    @Override
    public void onValueSelected(int value) {

        // Store the new work radius in shared prefs
        final SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        sharedPreferencesManager.set(SharedPreferencesManager.ID_WORK_RADIUS, value);

        MapFragment mapFragment = getMapFragment();
        if(mapFragment != null){
            mapFragment.setCurrentCircleRadius(value, true);
        }
    }

    public @Nullable SlideInSeekbarPanelFragment getSlideInSeekbarPanelFragment(){
        final FragmentManager fragmentManager = getSupportFragmentManager();
        return (SlideInSeekbarPanelFragment) fragmentManager.findFragmentById(R.id.fragment_seekbar);
    }
}
