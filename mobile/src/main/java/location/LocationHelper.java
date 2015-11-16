package location;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Florian on 19.08.2015.
 */
public class LocationHelper {

    private static Location mLastLocation = null;

    public static @Nullable Location getLastLocation(){
        return mLastLocation;
    }

    public static void setLastLocation(@NonNull Location location){
        mLastLocation = location;
    }
}
