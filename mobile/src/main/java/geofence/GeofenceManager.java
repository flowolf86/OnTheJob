package geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import configuration.WorkConfiguration;
import data.manager.SharedPreferencesManager;
import listing.GeofencingState;
import support.AppBroadcaster;

public class GeofenceManager {

    private static final String TAG = "GEOFENCE MANAGER";

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;

    public GeofenceManager(@NonNull Context context, @NonNull GoogleApiClient googleApiClient){
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mGeofencePendingIntent = null;
    }

    private void setNotAtWorkIfStateIs(int... states) {

        // Update status widget fragment if we are currently at work
        @GeofencingState.IGeofencingState int state = getCurrentGeofenceState();

        int i = 0;
        for(int stateParam : states){
            if(state == stateParam){
                AppBroadcaster.sendStatusChangedBroadcast(mContext, GeofencingState.NOT_AT_WORK);
                //TODO If we change the geofence while active, save a work block.
            }
        }
    }

    /*
        All geofences
     */
    private GeofencingRequest getAddAllGeofencesRequest() {

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofenceConfiguration.DEFAULT_GEOFENCE_TRIGGER);
        builder.addGeofences(getAppGeofences());
        return builder.build();
    }

    public void addAllGeofences(){

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getAddAllGeofencesRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {

                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_PRIMARY_GEOFENCE_MONITOR, true);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_SECONDARY_GEOFENCE_MONITOR, true);

                    setNotAtWorkIfStateIs(GeofencingState.AT_WORK_PRIMARY, GeofencingState.AT_WORK_SECONDARY);

                    Log.d(TAG, "All geofences have been added/updated.");
                } else {
                    Log.d(TAG, "All geofences have NOT been added/updated.");
                }
            }
        });
    }

    public void removeAllGeofences(){

        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {

                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_PRIMARY_GEOFENCE_MONITOR, false);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_SECONDARY_GEOFENCE_MONITOR, false);

                    setNotAtWorkIfStateIs(GeofencingState.AT_WORK_PRIMARY, GeofencingState.AT_WORK_SECONDARY);

                    Log.d(TAG, "All geofences have been removed.");
                } else {
                    Log.d(TAG, "All geofence have NOT been removed.");
                }
            }
        });
    }

    public void getActiveGeofences(){
        //TODO
    }

    /*
        Primary geofence
     */

    private @Nullable GeofencingRequest getPrimaryGeofencesRequest() {

        Geofence primaryGeofence = getPrimaryGeofence();
        if(primaryGeofence != null) {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofenceConfiguration.DEFAULT_GEOFENCE_TRIGGER);
            builder.addGeofence(primaryGeofence);
            return builder.build();
        }
        return null;
    }

    public void addOrUpdatePrimaryGeofence(){

        final @GeofencingState.IGeofencingState int state = getCurrentGeofenceState();
        if(state == GeofencingState.DISABLED){
            Log.d(TAG, "Not updating primary geofence. Status is disabled.");
            return;
        }

        final GeofencingRequest geofencingRequest = getPrimaryGeofencesRequest();
        if(geofencingRequest == null){
            Log.d(TAG, "No primary geofence to set.");
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                geofencingRequest,
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {

                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_PRIMARY_GEOFENCE_MONITOR, true);

                    setNotAtWorkIfStateIs(GeofencingState.AT_WORK_PRIMARY);

                    Log.d(TAG, "Primary geofence has been added/updated.");
                } else {
                    Log.d(TAG, "Primary geofence has NOT been added/updated.");
                }
            }
        });
    }

    public void removePrimaryGeofence(){

        List<String> geofenceId = new ArrayList<>();
        geofenceId.add(GeofenceConfiguration.PRIMARY_GEOFENCE_ID);

        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                geofenceId
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {

                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_PRIMARY_GEOFENCE_MONITOR, false);

                    setNotAtWorkIfStateIs(GeofencingState.AT_WORK_PRIMARY);

                    Log.d(TAG, "Primary geofence has been removed.");
                } else {
                    Log.d(TAG, "Primary geofence has NOT been removed.");
                }
            }
        });
    }

    /*
        Secondary geofence
     */

    private @Nullable GeofencingRequest getSecondaryGeofencesRequest() {

        Geofence secondaryGeofence = getSecondaryGeofence();
        if(secondaryGeofence != null) {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofenceConfiguration.DEFAULT_GEOFENCE_TRIGGER);
            builder.addGeofence(secondaryGeofence);
            return builder.build();
        }
        return null;
    }

    public void addOrUpdateSecondaryGeofence(){

        final @GeofencingState.IGeofencingState int state = getCurrentGeofenceState();
        if(state == GeofencingState.DISABLED){
            Log.d(TAG, "Not updating secondary geofence. Status is disabled.");
            return;
        }

        GeofencingRequest geofencingRequest = getSecondaryGeofencesRequest();
        if(geofencingRequest == null){
            Log.d(TAG, "No secondary geofence to set.");
            return;
        }

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                geofencingRequest,
                getGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {

                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_SECONDARY_GEOFENCE_MONITOR, true);

                    setNotAtWorkIfStateIs(GeofencingState.AT_WORK_SECONDARY);

                    Log.d(TAG, "Secondary geofence has been added/updated.");
                } else {
                    Log.d(TAG, "Secondary geofence has NOT been added/updated.");
                }
            }
        });
    }

    public void removeSecondaryGeofence(){

        List<String> geofenceId = new ArrayList<>();
        geofenceId.add(GeofenceConfiguration.SECONDARY_GEOFENCE_ID);

        LocationServices.GeofencingApi.removeGeofences(
                mGoogleApiClient,
                geofenceId
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {

                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    sharedPreferencesManager.set(SharedPreferencesManager.ID_SECONDARY_GEOFENCE_MONITOR, false);

                    setNotAtWorkIfStateIs(GeofencingState.AT_WORK_SECONDARY);

                    Log.d(TAG, "Secondary geofence has been removed.");
                } else {
                    Log.d(TAG, "Secondary geofence has NOT been removed.");
                }
            }
        });
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {

        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public List<Geofence> getAppGeofences() {

        List<Geofence> geofences = new ArrayList<>();

        final Geofence primaryGeofence = getPrimaryGeofence();
        if(primaryGeofence != null){
            geofences.add(primaryGeofence);
        }

        final Geofence secondaryGeofence = getPrimaryGeofence();
        if(secondaryGeofence != null){
            geofences.add(secondaryGeofence);
        }

        return geofences;
    }

    private @Nullable Geofence getPrimaryGeofence(){

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
        int radius = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_RADIUS, WorkConfiguration.DEFAULT_WORK_RADIUS);

        Address primaryWorkAddress = sharedPreferencesManager.getComplex(SharedPreferencesManager.ID_PRIMARY_WORK_ADDRESS, Address.class);

        Geofence primaryGeofence = null;
        if(primaryWorkAddress != null) {
            primaryGeofence = new Geofence.Builder()
                    .setRequestId(GeofenceConfiguration.PRIMARY_GEOFENCE_ID)
                    .setCircularRegion(primaryWorkAddress.getLatitude(), primaryWorkAddress.getLongitude(), radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(GeofenceConfiguration.GEOFENCE_LOITERING_DELAY)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build();

            Log.d(TAG, "Building primary geofence. Radius: " + radius + "m, LatLng: " + primaryWorkAddress.getLatitude() + ", " + primaryWorkAddress.getLongitude());
        }

        return primaryGeofence;
    }

    private @Nullable Geofence getSecondaryGeofence(){

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
        int radius = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_RADIUS, WorkConfiguration.DEFAULT_WORK_RADIUS);

        Address secondaryWorkAddress = sharedPreferencesManager.getComplex(SharedPreferencesManager.ID_SECONDARY_WORK_ADDRESS, Address.class);

        Geofence secondaryGeofence = null;
        if(secondaryWorkAddress != null) {
            secondaryGeofence = new Geofence.Builder()
                    .setRequestId(GeofenceConfiguration.SECONDARY_GEOFENCE_ID)
                    .setCircularRegion(secondaryWorkAddress.getLatitude(), secondaryWorkAddress.getLongitude(), radius)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(GeofenceConfiguration.GEOFENCE_LOITERING_DELAY)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build();

            Log.d(TAG, "Building secondary geofence. Radius: "+radius+"m, LatLng: "+secondaryWorkAddress.getLatitude()+", "+secondaryWorkAddress.getLongitude());
        }

        return secondaryGeofence;
    }

    /**
     * Get current user geofence state
     */
    private @GeofencingState.IGeofencingState int getCurrentGeofenceState() {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
        @GeofencingState.IGeofencingState int state = sharedPreferencesManager.get(SharedPreferencesManager.ID_LAST_GEOFENCE_STATE, GeofencingState.UNKNOWN);
        return state;
    }
}
