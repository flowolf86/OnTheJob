package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.florianwolf.onthejob.R;

import ui.base.BaseFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class AboutFragment extends BaseFragment{

    public static final String FRAGMENT_TAG = "about_fragment";

    public AboutFragment() { }

    public static AboutFragment newInstance(){

        return new AboutFragment();
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        super.setToolbarTitle(R.string.about);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Logic
     */

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentBackHandlerInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }
}
