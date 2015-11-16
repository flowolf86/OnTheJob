package cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import data.Interval;

/**
 * Created by Florian on 22.06.2015.
 */
public interface IntervalCacheCallback {

    /**
     * Might be successful or not
     */
    void onDbOperationComplete(@Nullable String msg, int errorId);

    void onDbReadIntervalsComplete(@NonNull List<Interval> resultSet);

    void onDbWiped();
}
