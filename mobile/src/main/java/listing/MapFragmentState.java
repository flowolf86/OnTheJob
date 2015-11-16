package listing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Florian on 19.08.2015.
 */
public interface MapFragmentState {

    int UNKNOWN = -1;
    int PRIMARY_WORK_ADDRESS = 0;
    int SECONDARY_WORK_ADDRESS = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNKNOWN, PRIMARY_WORK_ADDRESS, SECONDARY_WORK_ADDRESS})
    @interface IMapFragmentState {}

}
