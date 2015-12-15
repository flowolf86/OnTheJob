package support;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import data.manager.SharedPreferencesManager;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 24/08/15.
 */
public class NotificationFactory {

    public static void displayNotification(@NonNull Context context, @DrawableRes int smallIconId, @NonNull String title, @NonNull String text, @NonNull Class callbackClass){

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        if(!sharedPreferencesManager.get(SharedPreferencesManager.ID_NOTIFICATIONS, true)){
            return;
        }

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIconId)
                        .setContentTitle(title)
                        .setContentText(text);

        final Intent resultIntent = new Intent(context, callbackClass);
        final PendingIntent resultPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = 1;
        final NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
