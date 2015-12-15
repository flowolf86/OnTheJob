package support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.florianwolf.onthejob.R;

import data.manager.SharedPreferencesManager;
import ui.dialog.SimpleDialogFragment;

/**
 * Author:  Florian Wolf
 * Email:   flowolf86@gmail.com
 * on 15/12/15.
 */
public class UserTutorialHelper {

    public static final String PREF_USER_HAS_SEEN_MANUAL_LOGGING_DESCRIPTION = "tutorial_manual_logging";

    public static UserTutorialHelper INSTANCE;
    private SharedPreferencesManager mSharedPreferencesManager;

    private UserTutorialHelper(Context context){
        mSharedPreferencesManager = new SharedPreferencesManager(context);
    }

    public static UserTutorialHelper getInstance(Context context){
        if(INSTANCE == null){
            INSTANCE = new UserTutorialHelper(context);
        }
        return INSTANCE;
    }

    /*
        ManualLoggingWidgetFragment
     */
    private boolean userHasSeenManualLoggingTutorial(){
        return mSharedPreferencesManager.get(PREF_USER_HAS_SEEN_MANUAL_LOGGING_DESCRIPTION, false);
    }

    private void setUserHasSeenManualLoggingTutorial(boolean hasSeenIt){
        mSharedPreferencesManager.set(PREF_USER_HAS_SEEN_MANUAL_LOGGING_DESCRIPTION, hasSeenIt);
    }

    public void displayManualLoggingTutorialIfNeeded(@NonNull Context context, @NonNull FragmentManager fragmentManager){
        if(!userHasSeenManualLoggingTutorial()) {
            SimpleDialogFragment.newInstance(context.getString(R.string.manual_logging), context.getString(R.string.manual_logging_tutorial),
                   context.getString(R.string.dialog_button_ok), null, SimpleDialogFragment.NO_REQUEST_CODE, null).show(fragmentManager, null);
            setUserHasSeenManualLoggingTutorial(true);
        }
    }
}
