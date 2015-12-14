package util;

import android.support.annotation.Nullable;

/**
 * Author:  Florian Wolf
 * Email:   florian.wolf@maibornwolff.de
 * on 24/08/15.
 */
public class TextUtils {

    public static boolean isValidText(@Nullable String text){
        return !(text == null || text.length() == 0 || text.equals("") || text.equals(" "));
    }
}