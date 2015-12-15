package util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.florianwolf.onthejob.R;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 11/12/15.
 */
public class MailUtils {

    public static void startMailApplication(@NonNull Context context, @Nullable String mailAddress, @Nullable String subject, @Nullable String text, @Nullable String dialogTitle, @Nullable String filePath) {

        final Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("plain/text");

        if(TextUtils.isValidText(mailAddress)) {
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailAddress});
        }

        if(TextUtils.isValidText(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        if(TextUtils.isValidText(text)) {
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }

        if(filePath != null){
            Uri uri = Uri.parse("file://" + filePath);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        context.startActivity(Intent.createChooser(intent, TextUtils.isValidText(dialogTitle) ? dialogTitle  : context.getString(R.string.dialog_button_select)));
    }
}
