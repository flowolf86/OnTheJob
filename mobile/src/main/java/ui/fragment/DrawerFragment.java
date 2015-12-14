package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.BuildConfig;
import com.florianwolf.onthejob.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import data.manager.SharedPreferencesManager;
import ui.base.BaseFragment;
import util.AddressUtils;

public class DrawerFragment extends BaseFragment implements View.OnClickListener{

    public static final String FRAGMENT_TAG = "drawer_fragment";

    @Bind(R.id.settings_view) RelativeLayout mSettingsLayout;
    @Bind(R.id.primary_work_view) RelativeLayout mPrimaryWorkLayout;
    @Bind(R.id.secondary_work_view) RelativeLayout mSecondaryWorkLayout;
    @Bind(R.id.export_work_data_view) RelativeLayout mExportWorkDataLayout;
    @Bind(R.id.primary_work_subtitle) TextView mPrimaryWorkSubtitle;
    @Bind(R.id.secondary_work_subtitle) TextView mSecondaryWorkSubtitle;
    @Bind(R.id.copyright) TextView mCopyright;

    public DrawerFragment() { }

    public static DrawerFragment newInstance() {
        return new DrawerFragment();
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Interface
     */

    public interface OnDrawerFragmentActionListener {
        void onSettingsEntrySelected();
        void onPrimaryWorkAddressSelected();
        void onSecondaryWorkAddressSelected();
        void onExportSelected();
    }

    /*
        Lifecycle
     */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawer, container, false);

        ButterKnife.bind(this, view);
        setOnClickListeners();
        setDefaults();

        return view;
    }

    private void setDefaults() {

        final SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        final Address primaryAddress = sharedPreferencesManager.getComplex(SharedPreferencesManager.ID_PRIMARY_WORK_ADDRESS, Address.class);
        final Address secondaryAddress = sharedPreferencesManager.getComplex(SharedPreferencesManager.ID_SECONDARY_WORK_ADDRESS, Address.class);

        if(primaryAddress != null){
            setPrimaryWorkAddress(primaryAddress);
        }

        if(secondaryAddress != null){
            setSecondaryWorkAddress(secondaryAddress);
        }

        mCopyright.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements((Activity) context);
    }

    @Override
    public void onStart() {
        CURRENT_FRAGMENT_TAG = FRAGMENT_TAG;
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    /*
        Logic
     */

    private void setOnClickListeners() {

        mSettingsLayout.setOnClickListener(this);
        mPrimaryWorkLayout.setOnClickListener(this);
        mSecondaryWorkLayout.setOnClickListener(this);
        mExportWorkDataLayout.setOnClickListener(this);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentNavigationInterface
                && activity instanceof OnDrawerFragmentActionListener
                && activity instanceof FragmentToolbarInterface
                && activity instanceof FragmentSnackbarInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settings_view:
                ((OnDrawerFragmentActionListener)getActivity()).onSettingsEntrySelected();
                break;
            case R.id.primary_work_view:
                ((OnDrawerFragmentActionListener)getActivity()).onPrimaryWorkAddressSelected();
                break;
            case R.id.secondary_work_view:
                ((OnDrawerFragmentActionListener)getActivity()).onSecondaryWorkAddressSelected();
                break;
            case R.id.export_work_data_view:
                ((OnDrawerFragmentActionListener)getActivity()).onExportSelected();
                break;
            default:
                break;
        }
    }

    /*
        Communication
     */

    public void setPrimaryWorkAddress(@NonNull Address address){
        mPrimaryWorkSubtitle.setText(AddressUtils.getAddressStringFormatted(address));
    }

    public void setSecondaryWorkAddress(@NonNull Address address){
        mSecondaryWorkSubtitle.setText(AddressUtils.getAddressStringFormatted(address));
    }
}
