package support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import listing.GeofencingState;
import ui.fragment.StatusWidgetFragment;

/**
 * Created by Florian on 23.06.2015.
 */
public class AppBroadcaster {

    public static final String BC_DATA_CACHE_UPDATED = "com.florianwolf.onthejob.broadcast.DATA_CACHE_UPDATED";
    public static final String BC_CATEGORY_CACHE_UPDATED = "com.florianwolf.onthejob.broadcast.CATEGORY_CACHE_UPDATED";
    public static final String BC_STATUS_CHANGED = "com.florianwolf.onthejob.broadcast.STATUS_CHANGED";

    public static synchronized boolean registerReceiver(final Context context, final BroadcastReceiver receiver, final IntentFilter filter) {

        if (context != null && receiver != null && filter != null) {
            getLocalBroadcastManager(context).registerReceiver(receiver, filter);
            return true;
        }
        return false;
    }

    public static synchronized boolean unregisterReceiver(final Context context, final BroadcastReceiver receiver) {

        if (context != null && receiver != null) {
            getLocalBroadcastManager(context).unregisterReceiver(receiver);
            return true;
        }
        return false;
    }

    private static LocalBroadcastManager getLocalBroadcastManager(final Context context) {
        return LocalBroadcastManager.getInstance(context);
    }

    public static void sendDataCacheUpdatedBroadcast(final Context context) {
        getLocalBroadcastManager(context).sendBroadcast(new Intent(BC_DATA_CACHE_UPDATED));
    }

    public static void sendCategoryCacheUpdatedBroadcast(final Context context) {
        getLocalBroadcastManager(context).sendBroadcast(new Intent(BC_CATEGORY_CACHE_UPDATED));
    }

    public static void sendStatusChangedBroadcast(final Context context, @GeofencingState.IGeofencingState int state) {

        Intent intent = new Intent(BC_STATUS_CHANGED);
        intent.putExtra(StatusWidgetFragment.EXTRA_STATE, state);

        getLocalBroadcastManager(context).sendBroadcast(intent);
    }
}
