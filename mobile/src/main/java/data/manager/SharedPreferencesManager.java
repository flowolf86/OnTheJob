package data.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SharedPreferencesManager {

    public static final String ID_WORKLOAD = "id_workload";
    public static final String ID_WORK_DAYS = "id_workdays";
    public static final String ID_WORK_DAYS_NUMBER = "id_workdays_number";
    public static final String ID_VACATION = "id_vacation";
    public static final String ID_SICK_LEAVE = "id_sick_leave";
    public static final String ID_WORK_RADIUS = "id_radius";
    public static final String ID_NOTIFICATIONS = "id_notifications";
    public static final String ID_GEOFENCING = "id_geofencing";
    public static final String ID_PRIMARY_WORK_ADDRESS = "id_primary_work";
    public static final String ID_SECONDARY_WORK_ADDRESS = "id_secondary_work";

    public static final String ID_PRIMARY_GEOFENCE_MONITOR = "id_primary_geofence_monitor";
    public static final String ID_SECONDARY_GEOFENCE_MONITOR = "id_secondary_geofence_monitor";
    public static final String ID_LAST_GEOFENCE_STATE = "id_last_geofence_state";

    private Context context = null;

    public SharedPreferencesManager(Context context){
        this.context = context;
    }

    public void set(final String key, final String value) {
        final Editor editor = getEditor();
        editor.putString(key, value);
        editor.commit();
    }

    public void set(final String key, final boolean value) {
        final Editor editor = getEditor();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void set(final String key, final int value) {
        final Editor editor = getEditor();
        editor.putInt(key, value);
        editor.commit();
    }

    public void set(final String key, final long value) {
        final Editor editor = getEditor();
        editor.putLong(key, value);
        editor.commit();
    }

    public void set(final String key, final float value) {
        final Editor editor = getEditor();
        editor.putFloat(key, value);
        editor.commit();
    }

    public String get(final String key, final String defaultValue) {
        return getPreferences().getString(key, defaultValue);
    }

    public int get(final String key, final int defaultValue) {
        return getPreferences().getInt(key, defaultValue);
    }

    public float get(final String key, final float defaultValue) {
        return getPreferences().getFloat(key, defaultValue);
    }

    public boolean get(final String key, final boolean defaultValue) {
        return getPreferences().getBoolean(key, defaultValue);
    }

    public long get(final String key, final long defaultValue) {
        return getPreferences().getLong(key, defaultValue);
    }

    public boolean remove(final String key) {
        return getEditor().remove(key).commit();
    }

    /*
     * Helper Methods
     */

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private Editor getEditor() {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.edit();
    }

    /*
        Complex
     */

    public <T> void putComplex(@NonNull String key, T value){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String data = gson.toJson(value);
        set(key, data);
    }

    public @Nullable <T> T getComplex(@NonNull String key, @NonNull Class<T> type){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String data = get(key, "");
        if (data.equals("")){
            return null;
        }
        return gson.fromJson(data, type);
    }
}