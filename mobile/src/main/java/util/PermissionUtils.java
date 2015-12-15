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

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 11/12/15.
 */
public class PermissionUtils {

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 200;

    public static boolean checkStoragePermissions(@NonNull final Activity activity){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        // Check permissions in Marshmallow onwards
        int hasStoragePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission_group.STORAGE);

        if (hasStoragePermission != PackageManager.PERMISSION_GRANTED) {

            // If the user denied the permission via menu manually
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission_group.STORAGE)) {
                showMessageOKCancel(activity,
                        activity.getString(R.string.storage_permission_question),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission_group.STORAGE }, STORAGE_PERMISSION_REQUEST_CODE);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission_group.STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
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
