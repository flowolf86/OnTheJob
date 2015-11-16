package cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import data.GeofenceEvent;

public interface GeofenceDbCallback {

    void onDbCreateGeofenceComplete(@NonNull GeofenceEvent createdEvent);
    void onDbReadGeofencesComplete(@NonNull List<GeofenceEvent> resultSet);
    void onDbGeofencesFail(@Nullable String msg);
}
