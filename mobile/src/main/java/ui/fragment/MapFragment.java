package ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.florianwolf.onthejob.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import configuration.WorkConfiguration;
import data.manager.SharedPreferencesManager;
import listing.MapFragmentState;
import location.LocationHelper;
import ui.base.BaseFragment;

/**
 * Created by Florian on 22.06.2015.
 */
public class MapFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener{

    public static final String FRAGMENT_TAG = "map_fragment";

    private static final String ARG_ADDRESS = "arg_address";
    private static final String ARG_STATE = "arg_state";

    private static final int DEFAULT_ZOOM_LEVEL = 15;

    private GoogleMap mGoogleMap;

    private Circle mCurrentCircle;

    /*
        Interface
     */
    public interface MapFragmentInterface{
        void onMapLongClick(@NonNull final LatLng latLng);
        void onMapClick(@NonNull final LatLng latLng);
    }

    public MapFragment() { }

    public static MapFragment newInstance(@Nullable final Address address, @MapFragmentState.IMapFragmentState int fragmentState){

        final MapFragment mapFragment = new MapFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_ADDRESS, address);
        bundle.putInt(ARG_STATE, fragmentState);

        mapFragment.setArguments(bundle);

        return mapFragment;
    }

    /*
        Lifecycle
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        super.setToolbarTitle(getString(R.string.select_location));
        getMap();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        verifyActivityFulfillsRequirements(getActivity());
    }

    private void getMap() {

        final SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(supportMapFragment != null) {
            Log.d(FRAGMENT_TAG, "Found map fragment.");
            supportMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    /*
        Logic
     */

    private void verifyActivityFulfillsRequirements(Activity activity) {

        boolean verify = activity instanceof FragmentBackHandlerInterface
                && activity instanceof MapFragmentInterface;

        if(!verify){
            throw new ClassCastException(activity.toString() + " must implement all required listeners");
        }
    }

    private void setUpMap(@NonNull GoogleMap googleMap) {

        mGoogleMap = googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setTrafficEnabled(true);
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        mGoogleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mGoogleMap.getUiSettings().setRotateGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Listeners
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnMapClickListener(this);
        mGoogleMap.setOnMarkerClickListener(this);
    }

    public void locateMe() {

        final Location location = LocationHelper.getLastLocation();
        if(location != null){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            animateTo(latLng, false);
            ((MapFragmentInterface) getActivity()).onMapLongClick(latLng);
            addMarkerWithGeofence(latLng);
        } else {
            ((FragmentSnackbarInterface) getActivity()).onFragmentSnackbarRequest(getString(R.string.location_not_available), Snackbar.LENGTH_SHORT);
        }
    }

    /**
     * Change the radius of the circle shape
     * @param radiusInMeter <b>int</b> the new radius in meter
     */
    public void setCurrentCircleRadius(int radiusInMeter, boolean fillShape){

        if(mCurrentCircle != null) {
            mCurrentCircle.setRadius(radiusInMeter);

            if(fillShape){
                int shadeColor = ContextCompat.getColor(getContext(), R.color.geofence_fill);
                mCurrentCircle.setFillColor(shadeColor);
            } else {
                // Avoid flickering with fill colors. Only draw the stroke
                mCurrentCircle.setFillColor(Color.TRANSPARENT);
            }
        }
    }

    /*
        Map callbacks
     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        setUpMap(googleMap);
        setLocation((Address) getArguments().getParcelable(ARG_ADDRESS));
    }

    private void setLocation(@Nullable Address address) {

        if(address != null){
            moveTo(new LatLng(address.getLatitude(), address.getLongitude()), true);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mGoogleMap.clear();
        ((MapFragmentInterface) getActivity()).onMapClick(latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        animateTo(latLng, true);
        ((MapFragmentInterface) getActivity()).onMapLongClick(latLng);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    /*
        Map interaction
     */

    public void animateTo(@NonNull final LatLng latLng, boolean addMarker) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
        mGoogleMap.animateCamera(cameraUpdate);

        if(addMarker){
            addMarkerWithGeofence(latLng);
        }
    }

    private void addMarkerWithGeofence(@NonNull final LatLng latLng) {

        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.fw_location)));

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(getContext());
        int radius = sharedPreferencesManager.get(SharedPreferencesManager.ID_WORK_RADIUS, WorkConfiguration.DEFAULT_WORK_RADIUS);

        int strokeColor = ContextCompat.getColor(getContext(), R.color.geofence_stroke);
        int shadeColor = ContextCompat.getColor(getContext(), R.color.geofence_fill);

        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(radius).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        mCurrentCircle = mGoogleMap.addCircle(circleOptions);
    }

    public void moveTo(@NonNull final LatLng latLng, boolean addMarker) {
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
        mGoogleMap.moveCamera(cameraUpdate);

        if(addMarker){
            addMarkerWithGeofence(latLng);
        }
    }
}
