package cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import data.WorkEntry;

/**
 * Created by Florian on 22.06.2015.
 */
public interface EntryCacheCallback {

    /**
     * Might be successful or not
     */
    void onDbOperationComplete(@Nullable String msg, int errorId);

    void onDbReadEntriesComplete(@NonNull List<WorkEntry> resultSet);

    void onDbWiped();
}
