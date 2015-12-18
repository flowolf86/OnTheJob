package util;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.florianwolf.onthejob.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 11/12/15.
 */
public class PermissionUtils {

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    public static boolean hasStoragePermissions(@NonNull final Activity activity){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        final String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        // Check permissions in Marshmallow onwards
        int hasStorageReadPermission = ContextCompat.checkSelfPermission(activity, READ_PERMISSION);
        int hasStorageWritePermission = ContextCompat.checkSelfPermission(activity, WRITE_PERMISSION);

        List<String> permissions = new ArrayList<>();

        if (hasStorageReadPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(READ_PERMISSION);
        }
        if (hasStorageWritePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(WRITE_PERMISSION);
        }

        final String[] params = permissions.toArray(new String[permissions.size()]);

        if (!permissions.isEmpty()) {

            // If the user denied the permission via menu manually
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessageOKCancel(activity, activity.getString(R.string.storage_permission_question),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, params, STORAGE_PERMISSION_REQUEST_CODE);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(activity, params, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            return true;
        }
        return false;
    }

    public static boolean hasLocationPermissions(@NonNull final Activity activity){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
        final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

        // Check permissions in Marshmallow onwards
        int hasStorageCoarsePermission = ContextCompat.checkSelfPermission(activity, COARSE_LOCATION);
        int hasStorageFinePermission = ContextCompat.checkSelfPermission(activity, FINE_LOCATION);

        List<String> permissions = new ArrayList<>();

        if (hasStorageCoarsePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(COARSE_LOCATION);
        }
        if (hasStorageFinePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(FINE_LOCATION);
        }

        final String[] params = permissions.toArray(new String[permissions.size()]);

        if (!permissions.isEmpty()) {

            // If the user denied the permission via menu manually
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                    || !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel(activity, activity.getString(R.string.map_permission_question),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, params, LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(activity, params, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            return true;
        }
        return false;
    }

    public static void onLocationPermissionDenied(){
        //TODO
    }

    public static void onStoragePermissionDenied(){
        //TODO
    }

    private static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.dialog_button_ok), okListener)
                .setNegativeButton(activity.getString(R.string.dialog_button_cancel), null)
                .create()
                .show();
    }
}
