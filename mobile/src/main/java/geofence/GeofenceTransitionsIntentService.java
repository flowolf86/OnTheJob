package geofence;

/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.florianwolf.onthejob.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import listing.GeofencingState;
import support.AppBroadcaster;
import support.NotificationFactory;
import ui.activity.MainActivity;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "geofence-service";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to LocationServices (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Transition error: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT  || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // For us it does not matter which geofence triggers. Every slide_side_enter is a "start work" and every slide_side_exit is "end work"
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            GeofenceAutoAdder geofenceAutoAdder = new GeofenceAutoAdder(getApplicationContext());

            switch (geofenceTransition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Log.d(TAG, "Entered GEOFENCE! " + triggeringGeofences.get(0).getRequestId());

                    switch (triggeringGeofences.get(0).getRequestId()){
                        case GeofenceConfiguration.PRIMARY_GEOFENCE_ID:
                            AppBroadcaster.sendStatusChangedBroadcast(getApplicationContext(), GeofencingState.AT_WORK_PRIMARY);
                            NotificationFactory.displayNotification(this, R.drawable.fw_geofencing, getString(R.string.app_name), getString(R.string.primary_geofence_entered), MainActivity.class);
                            break;
                        case GeofenceConfiguration.SECONDARY_GEOFENCE_ID:
                            AppBroadcaster.sendStatusChangedBroadcast(getApplicationContext(), GeofencingState.AT_WORK_SECONDARY);
                            NotificationFactory.displayNotification(this, R.drawable.fw_geofencing, getString(R.string.app_name), getString(R.string.secondary_geofence_entered), MainActivity.class);
                            break;
                    }

                    geofenceAutoAdder.saveGeofenceEventInDatabase(System.currentTimeMillis(), Geofence.GEOFENCE_TRANSITION_ENTER, triggeringGeofences.get(0).getRequestId());

                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    //TODO Dwelling not yet implemented (!)
                    //This will trigger after we stayed in the geofence for two minutes.
                    Log.d(TAG, "Loitering time finished (" + TimeUnit.MILLISECONDS.toMinutes(GeofenceConfiguration.GEOFENCE_LOITERING_DELAY) + " min). Entered GEOFENCE! " + triggeringGeofences.get(0).getRequestId());
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Log.d(TAG, "Exited GEOFENCE! " + triggeringGeofences.get(0).getRequestId());

                    switch (triggeringGeofences.get(0).getRequestId()){
                        case GeofenceConfiguration.PRIMARY_GEOFENCE_ID:
                            NotificationFactory.displayNotification(this, R.drawable.fw_geofencing, getString(R.string.app_name), getString(R.string.primary_geofence_left), MainActivity.class);
                            break;
                        case GeofenceConfiguration.SECONDARY_GEOFENCE_ID:
                            NotificationFactory.displayNotification(this, R.drawable.fw_geofencing, getString(R.string.app_name), getString(R.string.secondary_geofence_left), MainActivity.class);
                            break;
                    }

                    geofenceAutoAdder.saveGeofenceEventInDatabase(System.currentTimeMillis(), Geofence.GEOFENCE_TRANSITION_EXIT, triggeringGeofences.get(0).getRequestId());
                    AppBroadcaster.sendStatusChangedBroadcast(getApplicationContext(), GeofencingState.NOT_AT_WORK);

                    break;
                default:
                    break;
            }
        }
    }
}