package listing;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface ManualLoggingState {

    int STARTED = 0;
    int STOPPED = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STARTED, STOPPED})
    @interface IManualLoggingState { }

}
