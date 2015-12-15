package support;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 25/08/15.
 */
public class AppToaster {

    public static void toast(@NonNull final Context context, @NonNull final String text){

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
