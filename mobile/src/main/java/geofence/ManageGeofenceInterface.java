package geofence;

/**
 * Created by Florian on 20.08.2015.
 */
public interface ManageGeofenceInterface {

    void onRemoveAllGeofencesRequest();
    void onRemovePrimaryGeofenceRequest();
    void onRemoveSecondaryGeofenceRequest();

    void onUpdateAllGeofencesRequest();
    void onUpdatePrimaryGeofenceRequest();
    void onUpdateSecondaryGeofenceRequest();
}
