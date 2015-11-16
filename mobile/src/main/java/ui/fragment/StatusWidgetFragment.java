package ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.florianwolf.onthejob.R;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import data.manager.SharedPreferencesManager;
import geofence.ManageGeofenceInterface;
import listing.GeofencingState;
import support.AppBroadcaster;
import ui.dialog.SimpleDialogFragment;

public class StatusWidgetFragment extends Fragment implements View.OnClickListener, SimpleDialogFragment.SimpleDialogCallback{

    @Bind(R.id.current_status) TextView mStatusText = null;

    public static final String EXTRA_STATE = "extra_state";

    private boolean mIsRegistered = false;
    private final BroadcastReceiver mStatusUpdateReceiver = new StatusUpdateReceiver();
    private final IntentFilter mStatusUpdateFilter = new IntentFilter(AppBroadcaster.BC_STATUS_CHANGED);

    public StatusWidgetFragment() { }

    public static StatusWidgetFragment newInstance() {
        return new StatusWidgetFragment();
    }
    /*
        Lifecycle
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_status_widget, container, false);

        ButterKnife.bind(this, view);
        restoreLastState();
        setOnClickListeners();

        return view;
    }

    private void setOnClickListeners() {

        mStatusText.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceivers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcastReceivers();
        ButterKnife.unbind(this);
    }

    /*
        Logic
     */

    private void restoreLastState() {

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        @GeofencingState.IGeofencingState int state = sharedPreferencesManager.get(SharedPreferencesManager.ID_LAST_GEOFENCE_STATE, GeofencingState.UNKNOWN);
        setState(state);
    }

    private void setState(@GeofencingState.IGeofencingState int state) {

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        sharedPreferencesManager.set(SharedPreferencesManager.ID_LAST_GEOFENCE_STATE, state);

        switch (state){
            case GeofencingState.DISABLED:
                setCompoundDrawable(R.drawable.oval_red);
                mStatusText.setText(getString(R.string.status_disabled));
                break;
            case GeofencingState.AT_WORK_PRIMARY:
                setCompoundDrawable(R.drawable.oval_green);
                mStatusText.setText(getString(R.string.status_at_work_primary));
                break;
            case GeofencingState.AT_WORK_SECONDARY:
                setCompoundDrawable(R.drawable.oval_green);
                mStatusText.setText(getString(R.string.status_at_work_secondary));
                break;
            case GeofencingState.NOT_AT_WORK:
                setCompoundDrawable(R.drawable.oval_yellow);
                mStatusText.setText(getString(R.string.status_not_at_work));
                break;
            case GeofencingState.UNKNOWN:
                setCompoundDrawable(R.drawable.oval_red);
                mStatusText.setText(getString(R.string.status_unknown));
                break;
        }
    }

    private void setCompoundDrawable(@DrawableRes int drawableId){
        mStatusText.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), drawableId), null);
    }

    private synchronized void registerBroadcastReceivers() {

        if(!mIsRegistered){
            AppBroadcaster.registerReceiver(getContext(), mStatusUpdateReceiver, mStatusUpdateFilter);
            mIsRegistered = true;
        }
    }

    private synchronized void unregisterBroadcastReceivers() {

        if(mIsRegistered){
            AppBroadcaster.unregisterReceiver(getContext(), mStatusUpdateReceiver);
            mIsRegistered = false;
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.current_status:

                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
                boolean isGeofencingEnabled = sharedPreferencesManager.get(SharedPreferencesManager.ID_GEOFENCING, true);

                SimpleDialogFragment newFragment;

                if(isGeofencingEnabled){

                    newFragment = SimpleDialogFragment.newInstance(
                            null,
                            getString(R.string.disable_geofencing_dialog),
                            getString(R.string.dialog_button_disable).toUpperCase(Locale.getDefault()),
                            getString(R.string.dialog_button_cancel).toUpperCase(Locale.getDefault()),
                            SimpleDialogFragment.DISABLE_GEOFENCING_DIALOG,
                            null);
                } else {

                    newFragment = SimpleDialogFragment.newInstance(
                            null,
                            getString(R.string.enable_geofencing_dialog),
                            getString(R.string.dialog_button_enable).toUpperCase(Locale.getDefault()),
                            getString(R.string.dialog_button_cancel).toUpperCase(Locale.getDefault()),
                            SimpleDialogFragment.ENABLE_GEOFENCING_DIALOG,
                            null);
                }

                newFragment.setTargetFragment(this, 0);
                newFragment.show(getChildFragmentManager(), "dialog");
                break;
            default:
                break;
        }
    }

    @Override
    public void onPositiveDialogResponse(int responseCode, Parcelable data) {

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());

        switch (responseCode){
            case SimpleDialogFragment.ENABLE_GEOFENCING_DIALOG:
                setState(GeofencingState.UNKNOWN);
                sharedPreferencesManager.set(SharedPreferencesManager.ID_GEOFENCING, true);
                ((ManageGeofenceInterface)getActivity()).onUpdateAllGeofencesRequest();
                break;
            case SimpleDialogFragment.DISABLE_GEOFENCING_DIALOG:
                setState(GeofencingState.DISABLED);
                sharedPreferencesManager.set(SharedPreferencesManager.ID_GEOFENCING, false);
                ((ManageGeofenceInterface)getActivity()).onRemoveAllGeofencesRequest();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegativeDialogResponse(int responseCode) {

    }

    /*
        Broadcast receivers
     */

    private class StatusUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            @GeofencingState.IGeofencingState int status = intent.getIntExtra(EXTRA_STATE, GeofencingState.UNKNOWN);
            setState(status);
        }
    }
}
