package ui.base;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;

import com.florianwolf.onthejob.R;

/**
 * Created by Florian on 25.06.2015.
 */
public abstract class BaseFragment extends Fragment {

    protected static String CURRENT_FRAGMENT_TAG = "base_fragment";

    public abstract boolean onBackPressed();

    @Override
    public void onStart() {
        super.onStart();
        ((FragmentNavigationInterface)getActivity()).onFragmentStarted(CURRENT_FRAGMENT_TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((FragmentBackHandlerInterface)getActivity()).setSelectedFragment(this);
    }

    public interface FragmentBackHandlerInterface {
        void setSelectedFragment(BaseFragment selectedFragment);
    }

    public interface FragmentNavigationInterface {
        void onFragmentBack();
        void onFragmentUp();
        void onFragmentFinished();
        void onFragmentStarted(String tag);
    }

    public interface FragmentToolbarInterface {
        void onFragmentToolbarTitleChange(String title);
        void onFragmentToolbarStateChange();
    }

    public interface FragmentSnackbarInterface {
        void onFragmentSnackbarRequest(String text, int duration);
    }

    public interface FragmentActionModeInterface {
        void onFragmentSetActionModeRequest(ActionMode actionMode);
        void onFragmentStartActionModeRequest(ActionMode.Callback callback);
        void onFragmentFinishActionModeRequest();
        void onFragmentSetActionModeTitleRequest(String title);
        void onFragmentHideToolbarRequest();
        void onFragmentShowToolbarRequest();
    }

    /**
     * Actvity requests to finish the fragment (back-button, up navigation etc.)
     * @return true if fragment can be finished safely, false if fragment shall not be finished
     */

    public boolean onActivityFinishFragmentRequest(){
        return true;
    }

    /*
        Convenience
     */

    protected void setToolbarTitle(@Nullable String customTitle) {

        if(customTitle == null){
            return;
        }

        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarTitleChange(customTitle);
    }

    protected void setToolbarTitle(@StringRes int customTitleId) {

        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarTitleChange(getString(customTitleId));
    }
}

