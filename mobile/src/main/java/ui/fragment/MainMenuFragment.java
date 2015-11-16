package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import ui.base.BaseFragment;

/**
 * Created by Florian on 25.06.2015.
 */
public class MainMenuFragment extends BaseFragment implements View.OnClickListener{

    public static final String FRAGMENT_TAG = "main_menu_fragment";

    @Bind(R.id.record_view) CardView mNewEntryLayout;
    @Bind(R.id.history_view) CardView mHistoryLayout;
    @Bind(R.id.day_off_view) CardView mDayOffLayout;

    public MainMenuFragment() { }

    public static MainMenuFragment newInstance() {
        return new MainMenuFragment();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Interface
     */

    public interface OnMainMenuFragmentActionListener {
        void onAddEntrySelected();
        void onHistorySelected();
        void onDayOffSelected();
    }

    /*
        Activity communication
     */

    private void setToolbarState(){
        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarStateChange(); //TODO
    }

    /*
        Lifecycle
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        ButterKnife.bind(this, view);
        setOnClickListeners();
        setChildFragments();

        return view;
    }

    private void setChildFragments() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.status_fragment_wrapper, StatusWidgetFragment.newInstance());
        transaction.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onStart() {
        CURRENT_FRAGMENT_TAG = FRAGMENT_TAG;
        super.onStart();
        ((FragmentToolbarInterface)getActivity()).onFragmentToolbarTitleChange(getString(R.string.app_name));
    }

    /*
        Logic
     */

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof OnMainMenuFragmentActionListener
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private void setOnClickListeners() {
        mNewEntryLayout.setOnClickListener(this);
        mHistoryLayout.setOnClickListener(this);
        mDayOffLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_view:
                ((OnMainMenuFragmentActionListener)getActivity()).onAddEntrySelected();
                break;
            case R.id.history_view:
                ((OnMainMenuFragmentActionListener)getActivity()).onHistorySelected();
                break;
            case R.id.day_off_view:
                ((OnMainMenuFragmentActionListener)getActivity()).onDayOffSelected();
                break;
            default:
                break;
        }
    }
}
