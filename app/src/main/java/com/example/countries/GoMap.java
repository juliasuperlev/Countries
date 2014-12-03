package com.example.countries;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Юлька on 03.12.2014.
 */
public class GoMap extends Fragment {

    SharedPreferences sharedPreferences;

    private GoogleMap myMap;
    private LocationManager locationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String latitude = sharedPreferences.getString(Constants.LATITUDE, "");
        String longitude = sharedPreferences.getString(Constants.LONGITUDE, "");
        Log.d(Constants.LOG_TAG, "Latitude: " + latitude + ", longitude: " + longitude);

        myMap = ((MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map)).getMap();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100 * 3, 5, locationListener);

    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void showLocation(Location location) {
        if (location == null) {
            return;
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            myMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title("My location"));
        }
    }
}
