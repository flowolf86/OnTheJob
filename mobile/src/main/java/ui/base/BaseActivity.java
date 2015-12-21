package ui.base;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.florianwolf.onthejob.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import data.manager.SharedPreferencesManager;
import geofence.GeofenceManager;
import geofence.ManageGeofenceInterface;
import util.VirtualKeyboardManager;

public class BaseActivity extends AppCompatActivity implements
        BaseFragment.FragmentToolbarInterface, BaseFragment.FragmentNavigationInterface,
        BaseFragment.FragmentActionModeInterface, BaseFragment.FragmentBackHandlerInterface,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ManageGeofenceInterface{

    private static final String TAG = "BaseActivity";

    public static final int ANIMATION_NONE = 0;
    public static final int ANIMATION_SLIDE_IN_OUT = 100;
    public static final int ANIMATION_SLIDE_UP_DOWN = 200;
    public static final int ANIMATION_SLIDE_DOWN_UP = 300;

    public static final int ANIMATION_DELAY_MS = 200;

    private ActionMode mActionMode = null;
    private BaseFragment mSelectedFragment;
    protected Toolbar mToolbar = null;
    public Snackbar mNoConnectionSnackbar = null;

    private GeofenceManager mGeofenceManager = null;
    private GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        }

        setUpGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    protected void setUpToolbar(int toolbar, int titleId, boolean setHomeUpAsEnabled){

        mToolbar = (Toolbar) findViewById(toolbar);
        setSupportActionBar(mToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(getString(titleId));
            getSupportActionBar().setDisplayHomeAsUpEnabled(setHomeUpAsEnabled);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        // When the fragment consumes the back press it means the fragments
        // needs to do stuff before closing. e.g. display a dialog.
        if(mSelectedFragment != null) {
            boolean fragmentNotFinished = mSelectedFragment.onBackPressed();
            if(fragmentNotFinished){
                return;
            }
        }

        onFragmentFinished();
    }

    protected void replaceFragment(final int containerId, final Fragment f, final boolean addToBackStack, int animationType, @Nullable final String tag){

        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (animationType){
            case ANIMATION_SLIDE_IN_OUT:
                transaction.setCustomAnimations(R.anim.slide_side_enter, R.anim.slide_side_exit, R.anim.slide_side_pop_enter, R.anim.slide_side_pop_exit);
                break;
            case ANIMATION_SLIDE_UP_DOWN:
                transaction.setCustomAnimations(R.anim.slide_bottom_enter, R.anim.slide_bottom_exit, R.anim.slide_bottom_pop_enter, R.anim.slide_bottom_pop_exit);
                break;
            case ANIMATION_SLIDE_DOWN_UP:
                //TODO
                //transaction.setCustomAnimations(R.anim.panel_slide_in_from_top, R.anim.panel_slide_out_from_bottom, R.anim.panel_slide_in_from_top, R.anim.panel_slide_out_from_bottom);
                break;
            default:
                break;
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                transaction.replace(containerId, f, tag);

                if(addToBackStack) {
                    transaction.addToBackStack(tag);
                }

                transaction.commit();
            }
        }, ANIMATION_DELAY_MS);
    }

    protected void setToolbarTitle(String title){
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onFragmentBack() {

    }

    @Override
    public void onFragmentUp() {

    }

    @Override
    public void onFragmentFinished() {

        VirtualKeyboardManager.hideKeyboard(this);

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(count != 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentStarted(String tag) { }

    @Override
    public void onFragmentToolbarTitleChange(String title) {
        setToolbarTitle(title);
    }

    @Override
    public void onFragmentToolbarStateChange() {

    }

    /*
        Action mode callbacks
     */

    @Override
    public void onFragmentSetActionModeRequest(ActionMode actionMode) {

        // Only if we not already have an action mode active
        if(mActionMode == null) {
            mActionMode = actionMode;
        }
    }

    @Override
    public void onFragmentStartActionModeRequest(ActionMode.Callback callback) {

        // Check if action mode is already happening
        if(mActionMode == null) {
            mActionMode = startSupportActionMode(callback);
        }
    }

    @Override
    public void onFragmentFinishActionModeRequest() {

        if(mActionMode != null) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void onFragmentSetActionModeTitleRequest(String title) {
        if(mActionMode != null) {
            mActionMode.setTitle(title);
        }
    }

    /*
        Toolbar Interface
     */
    @Override
    public void onFragmentHideToolbarRequest() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }

    @Override
    public void onFragmentShowToolbarRequest() {
        if(getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }

    /*
        BackHandlerInterface
     */

    @Override
    public void setSelectedFragment(BaseFragment selectedFragment) {
        this.mSelectedFragment = selectedFragment;
    }

    /*
        Google API Client
     */

    private synchronized void setUpGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public @Nullable GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {

        // Set last location on connect
        // LocationHelper.setLastLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        // Add or update geofence data in LocationService API
        updateGeofencing();

        if(mNoConnectionSnackbar != null){
            mNoConnectionSnackbar.dismiss();
            mNoConnectionSnackbar = null;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mNoConnectionSnackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.google_api_client_offline), Snackbar.LENGTH_INDEFINITE);
        mNoConnectionSnackbar.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mNoConnectionSnackbar = Snackbar.make(findViewById(android.R.id.content), getString(R.string.google_api_client_connection_not_successful), Snackbar.LENGTH_INDEFINITE);
        mNoConnectionSnackbar.show();
    }

    /*
        Geofencing
     */

    private void updateGeofencing() {

        mGeofenceManager = new GeofenceManager(this, mGoogleApiClient);

        updatePrimaryGeofence(false);
        updateSecondaryGeofence(false);
    }

    @Override
    public void onRemovePrimaryGeofenceRequest() {
        //TODO
    }

    @Override
    public void onRemoveSecondaryGeofenceRequest() {
        //TODO
    }

    @Override
    public void onUpdateAllGeofencesRequest() {
        updatePrimaryGeofence(true);
        updateSecondaryGeofence(true);
    }

    @Override
    public void onRemoveAllGeofencesRequest() {
        removeAllGeofences();
    }

    @Override
    public void onUpdatePrimaryGeofenceRequest() {
        updatePrimaryGeofence(true);
    }

    @Override
    public void onUpdateSecondaryGeofenceRequest() {
        updateSecondaryGeofence(true);
    }

    private void updatePrimaryGeofence(boolean forceUpdate){

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        boolean isPrimaryGeofenceSet = sharedPreferencesManager.get(SharedPreferencesManager.ID_PRIMARY_GEOFENCE_MONITOR, false);

        if(mGoogleApiClient.isConnected() && mGeofenceManager != null && (!isPrimaryGeofenceSet || forceUpdate)) {
            mGeofenceManager.addOrUpdatePrimaryGeofence();
        }
    }

    private void updateSecondaryGeofence(boolean forceUpdate){

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        boolean isSecondaryGeofenceSet = sharedPreferencesManager.get(SharedPreferencesManager.ID_SECONDARY_GEOFENCE_MONITOR, false);

        if(mGoogleApiClient.isConnected() && mGeofenceManager != null && (!isSecondaryGeofenceSet || forceUpdate)) {
            mGeofenceManager.addOrUpdateSecondaryGeofence();
        }
    }

    private void removeAllGeofences(){

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(this);
        boolean isPrimaryGeofenceSet = sharedPreferencesManager.get(SharedPreferencesManager.ID_PRIMARY_GEOFENCE_MONITOR, false);
        boolean isSecondaryGeofenceSet = sharedPreferencesManager.get(SharedPreferencesManager.ID_SECONDARY_GEOFENCE_MONITOR, false);

        if(mGoogleApiClient.isConnected() && mGeofenceManager != null && (isSecondaryGeofenceSet || isPrimaryGeofenceSet)) {
            mGeofenceManager.removeAllGeofences();
        } else {
            if(!isPrimaryGeofenceSet && !isSecondaryGeofenceSet){
                Log.d(TAG, "Unable to remove geofences. No geofence active.");
            } else {
                Log.w(TAG, "Google API Client not connected.");
            }
        }
    }
}
