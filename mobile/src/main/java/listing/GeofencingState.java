package listing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface GeofencingState {

    int DISABLED = 0;
    int AT_WORK_PRIMARY = 10;
    int AT_WORK_SECONDARY = 15;
    int NOT_AT_WORK = 20;
    int UNKNOWN = 90;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISABLED, AT_WORK_PRIMARY, AT_WORK_SECONDARY, NOT_AT_WORK, UNKNOWN})
    @interface IGeofencingState { }

}
