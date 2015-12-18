package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.florianwolf.onthejob.BuildConfig;
import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ui.base.BaseFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class AboutFragment extends BaseFragment{

    @Bind(R.id.copyright) TextView mCopyright;

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
        ButterKnife.bind(this, view);
        super.setToolbarTitle(R.string.about);

        mCopyright.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
