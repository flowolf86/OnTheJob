package util;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.Locale;

import data.manager.SharedPreferencesManager;
import listing.MapFragmentState;
import location.LocationHelper;
import ui.activity.MapActivity;

public class AddressUtils {

    public static boolean isValidAddress(@Nullable final Address address){

        if(address == null){
            return false;
        }

        if(address.getMaxAddressLineIndex() <= 0){
            return false;
        }

        return true;
    }

    public static @Nullable Address getAddress(@NonNull Context context, @NonNull final String sharedPreferencesAddressId) {

        final SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        Address address = sharedPreferencesManager.getComplex(sharedPreferencesAddressId, Address.class);

        if(address == null){
            final Location location = LocationHelper.getLastLocation();

            if(location != null){
                address = new Address(Locale.getDefault());
                address.setLatitude(location.getLatitude());
                address.setLongitude(location.getLongitude());
            }
        }

        return address;
    }

    public static void storeAddressInSharedPreferences(@NonNull Context context, @Nullable final Address address, @MapFragmentState.IMapFragmentState int mapFragmentState) {

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        switch (mapFragmentState) {
            case MapFragmentState.PRIMARY_WORK_ADDRESS:
                sharedPreferencesManager.putComplex(SharedPreferencesManager.ID_PRIMARY_WORK_ADDRESS, address);
                break;
            case MapFragmentState.SECONDARY_WORK_ADDRESS:
                sharedPreferencesManager.putComplex(SharedPreferencesManager.ID_SECONDARY_WORK_ADDRESS, address);
                break;
            default:
                break;
        }
    }

    public static @Nullable String getAddressStringFormatted(@Nullable final Address address) {

        if(!AddressUtils.isValidAddress(address)){
            return null;
        }

        final StringBuilder sb = new StringBuilder(address.getAddressLine(0));
        if(address.getMaxAddressLineIndex() >= 1) {
            for (int i = 1; i <= address.getMaxAddressLineIndex(); i++) {

                if(address.getAddressLine(i).equalsIgnoreCase(address.getCountryName())){
                    continue;
                }

                if(i == 1){
                    sb.append('\n');
                } else {
                    sb.append(',');
                    sb.append(' ');
                }
                sb.append(address.getAddressLine(i));
            }
        }

        return sb.toString();
    }

    public static @Nullable String getAddressStringOneLine(@Nullable final Address address) {

        if(address == null){
            return null;
        }

        final StringBuilder sb = new StringBuilder(address.getAddressLine(0));
        if(address.getMaxAddressLineIndex() >= 1) {
            for (int i = 1; i <= address.getMaxAddressLineIndex(); i++) {

                if(address.getAddressLine(i).equalsIgnoreCase(address.getCountryName())){
                    continue;
                }

                sb.append(',');
                sb.append(' ');
                sb.append(address.getAddressLine(i));
            }
        }

        return sb.toString();
    }
}
