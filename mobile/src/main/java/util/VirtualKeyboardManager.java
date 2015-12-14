package util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Author:  Florian Wolf
 * Email:   florian.wolf@maibornwolff.de
 * on 14/12/15.
 */
public class VirtualKeyboardManager {

    public static void hideKeyboard(@NonNull Activity activity){

        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();

        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
