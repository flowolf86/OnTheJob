package util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by Florian on 20.08.2015.
 */
public class ReverseGeocoder {

    public interface ReverseGeocoderCallback{
        void onReverseGeocodingSuccess(@NonNull final Address address);
        void onReverseGeocodingFail(@Nullable final String msg);
    }

    Context mContext = null;

    private ReverseGeocoder() { }

    public ReverseGeocoder(@NonNull Context context){
        mContext = context;
    }

    public void reverseGeocode(@Nullable final LatLng latLng, @NonNull final ReverseGeocoderCallback callback, final int maxNumberOfResults){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    final Geocoder geoCoder = new Geocoder(mContext);
                    final List<Address> matches = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, maxNumberOfResults);
                    final Address bestMatch = (matches.isEmpty() ? null : matches.get(0));

                    if(bestMatch != null) {
                        callback.onReverseGeocodingSuccess(bestMatch);
                    }else{
                        callback.onReverseGeocodingFail("No reverse geocoding match...");
                    }
                }catch (IOException e){
                    callback.onReverseGeocodingFail("Exception while reverse geocoding...");
                }
            }
        }).start();
    }

    public void reverseGeocode(@Nullable final Address address, @NonNull final ReverseGeocoderCallback callback, final int maxNumberOfResults){

        if(address == null){
            callback.onReverseGeocodingFail("No address to reverse geocode");
            return;
        }

        reverseGeocode(new LatLng(address.getLatitude(), address.getLongitude()), callback, maxNumberOfResults);
    }

    public void reverseGeocode(@Nullable final Location location, @NonNull final ReverseGeocoderCallback callback, final int maxNumberOfResults){

        if(location == null){
            callback.onReverseGeocodingFail("No location to reverse geocode");
            return;
        }

        reverseGeocode(new LatLng(location.getLatitude(), location.getLongitude()), callback, maxNumberOfResults);
    }
}
