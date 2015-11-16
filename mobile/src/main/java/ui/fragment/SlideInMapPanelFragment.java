package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.florianwolf.onthejob.R;
import com.google.android.gms.maps.model.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import listing.MapFragmentState;
import location.LocationHelper;
import ui.base.BaseFragment;
import util.AddressUtils;
import util.ReverseGeocoder;

public class SlideInMapPanelFragment extends BaseFragment implements ReverseGeocoder.ReverseGeocoderCallback, View.OnClickListener{

    public static final String FRAGMENT_TAG = "SlideInMapPanel";

    /*
        Fragments states
     */
    final static int LOADING = 0;
    final static int CONTENT = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ LOADING, CONTENT })
    private @interface SlideInMapPanelState{

    }

    /*
        ButterKnife view binding
     */
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.street) TextView mStreet;
    @Bind(R.id.city) TextView mCity;
    @Bind(R.id.root) RelativeLayout mRootLayout;

    private static final String ARG_ADDRESS = "arg_address";

    /*
        Interface
     */
    public interface SlideInMapPanelInterface{
        void onPanelClicked(@NonNull Address address);
        void onPanelDismissRequest();
    }

    /*
        Fragment creation
     */
    public SlideInMapPanelFragment(){ }

    public static SlideInMapPanelFragment newInstance(@Nullable Address address){

        final SlideInMapPanelFragment slideInMapPanelFragment = new SlideInMapPanelFragment();
        final Bundle bundle = new Bundle();

        if(address != null) {
            bundle.putParcelable(ARG_ADDRESS, address);
        }

        slideInMapPanelFragment.setArguments(bundle);

        return slideInMapPanelFragment;
    }

    public static SlideInMapPanelFragment newInstance(@Nullable LatLng latLng){

        Address address = null;
        if(latLng != null){
            address = new Address(Locale.getDefault());
            address.setLatitude(latLng.latitude);
            address.setLongitude(latLng.longitude);
        }

        return newInstance(address);
    }

    public static SlideInMapPanelFragment newInstance(@Nullable Location location){

        Address address = null;
        if(location != null){
            address = new Address(Locale.getDefault());
            address.setLatitude(location.getLatitude());
            address.setLongitude(location.getLongitude());
        }

        return newInstance(address);
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.slide_in_map_panel, container, false);

        ButterKnife.bind(this, view);
        setState(AddressUtils.isValidAddress((Address) getArguments().getParcelable(ARG_ADDRESS)) ? CONTENT : LOADING);

        return view;
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

    /*
        Logic
     */

    @UiThread
    private void setState(final @SlideInMapPanelState int state) {

        if(state == LOADING){

            mRootLayout.setOnClickListener(null);

            mProgressBar.setVisibility(View.VISIBLE);
            mCity.setVisibility(View.GONE);
            mStreet.setVisibility(View.GONE);

            final ReverseGeocoder reverseGeocoder = new ReverseGeocoder(getContext());
            reverseGeocoder.reverseGeocode(getAddress(), this, 1);

        } else if(state == CONTENT){

            mRootLayout.setOnClickListener(this);

            final Address address = getArguments().getParcelable(ARG_ADDRESS);

            mProgressBar.setVisibility(View.GONE);

            if(address != null && address.getMaxAddressLineIndex() >= 0){
                mStreet.setText(address.getAddressLine(0));
                mStreet.setVisibility(View.VISIBLE);

                if(address.getMaxAddressLineIndex() >= 1) {
                    StringBuilder sb = new StringBuilder(address.getAddressLine(1));
                    for (int i = 2; i <= address.getMaxAddressLineIndex(); i++) {

                        if(address.getAddressLine(i).equalsIgnoreCase(address.getCountryName())){
                            continue;
                        }

                        sb.append(',');
                        sb.append(' ');
                        sb.append(address.getAddressLine(i));
                    }
                    mCity.setText(sb.toString());
                    mCity.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setAddress(@NonNull Address address){

        getArguments().putParcelable(ARG_ADDRESS, address);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setState(CONTENT);
            }
        });
    }

    private @Nullable Address getAddress(){

        return getArguments().getParcelable(ARG_ADDRESS);
    }

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof SlideInMapPanelInterface
                && activity instanceof FragmentSnackbarInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    @Override
    public void onReverseGeocodingSuccess(@NonNull Address address) {

        // In case the panel got destroyed already
        if(getActivity() != null) {
            setAddress(address);
        }
    }

    @Override
    public void onReverseGeocodingFail(@Nullable String msg) {

        // In case the panel got destroyed already
        if(getActivity() != null) {
            ((SlideInMapPanelInterface) getActivity()).onPanelDismissRequest();
            ((FragmentSnackbarInterface) getActivity()).onFragmentSnackbarRequest(getString(R.string.no_address_please_try_again), Snackbar.LENGTH_SHORT);
        }
    }

    /*
        ButterKnife listener binding
     */

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.root:
                if(AddressUtils.isValidAddress(getAddress())) {
                    ((SlideInMapPanelInterface) getActivity()).onPanelClicked(getAddress());
                }
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.root) void onPanelClick(){
        if(getAddress() != null) {
            ((SlideInMapPanelInterface) getActivity()).onPanelClicked(getAddress());
        }
    }

    @Override
    public boolean onBackPressed() {

        if(getActivity() != null) {
            ((SlideInMapPanelInterface) getActivity()).onPanelDismissRequest();
            return true;
        }
        return false;
    }
}
