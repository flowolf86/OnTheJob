package geofence;

import com.google.android.gms.location.Geofence;

import java.util.concurrent.TimeUnit;


public class GeofenceConfiguration {

    public static final int DEFAULT_GEOFENCE_TRIGGER = Geofence.GEOFENCE_TRANSITION_ENTER;
    public static final int GEOFENCE_LOITERING_DELAY = (int) TimeUnit.MINUTES.toMillis(2);

    public static final String PRIMARY_GEOFENCE_ID = "onthejob_primary_geofence_4711";
    public static final String SECONDARY_GEOFENCE_ID = "onthejob_secondary_geofence_4711";
}
