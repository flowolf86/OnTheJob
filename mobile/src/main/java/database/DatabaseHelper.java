package database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import cache.GeofenceDbCallback;
import data.GeofenceEvent;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 25/08/15.
 */
public class DatabaseHelper {

    /*
        Geofence
     */

    public static void getLastXGeofenceEvents(@NonNull final Context context, int maxNumberOfGeofences, @NonNull GeofenceDbCallback callback){

        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.readLastXGeofenceEventsFromDatabase(maxNumberOfGeofences, callback);
        }catch (Exception e){
            callback.onDbGeofencesFail(e.getMessage());
        }
    }

    public static void addGeofenceEvent(@NonNull final Context context, @NonNull GeofenceEvent event, @NonNull GeofenceDbCallback callback){

        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.createGeofenceInDatabase(event, callback);
        }catch (Exception e){
            callback.onDbGeofencesFail(e.getMessage());
        }
    }

    public static void getAllGeofenceEvents(@NonNull final Context context, @NonNull GeofenceDbCallback callback){

        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.readAllGeofenceEventsFromDatabase(callback);
        }catch (Exception e){
            callback.onDbGeofencesFail(e.getMessage());
        }
    }

    public static void removeGeofenceEvent(@NonNull final Context context, @NonNull GeofenceEvent event){

        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.removeGeofenceEventsFromDatabase(event, null);
        }catch (Exception e){
            Log.e("DB", "Unable to remove geofence event from db.");
        }
    }

    public static void modifyGeofenceEvent(@NonNull final Context context, @NonNull GeofenceEvent event){

        DatabaseManager databaseManager = new DatabaseManager(context);
        try {
            databaseManager.modifyGeofenceInDatabase(event, null);
        }catch (Exception e){
            Log.e("DB", "Unable to remove geofence event from db.");
        }
    }
}
