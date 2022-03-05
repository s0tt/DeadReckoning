package com.example.so.fahrerinformationssysteme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by so on 09.02.2018.
 */

public class MapsHandler implements OnMapReadyCallback {
    public static MapsHandler instance = null;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Activity activity;

    public static MapsHandler getInstance() {
        if (MapsHandler.instance == null) {
            MapsHandler.instance = new MapsHandler();
        }
        return MapsHandler.instance;
    }

    public void start(Bundle savedInstance, Activity _activity) {
        activity = _activity;
        mMapView = (MapView) activity.findViewById(R.id.maps);
        mMapView.onCreate(savedInstance);
        mMapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

       /*
       //in old Api Needs to call MapsInitializer before doing any CameraUpdateFactory call
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
       */

        // Updates the location and zoom of the MapView
        /*CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
        map.animateCamera(cameraUpdate);*/
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.3584590, 9.1205380), 10));
        mGoogleMap.setMyLocationEnabled(true);
        //mMapView.setCameraDistance(5.0f);

    }

    public void setLocation(Location newLoc) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(newLoc.getLatitude(), newLoc.getLongitude()), 40));
    }

    public void setLocation(LatLng newLoc) {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLoc, 40));
    }

    public void drawRoute(PolylineOptions route) {
        mGoogleMap.addPolyline(route);
    }

    public void onResume() {
        mMapView.onResume();
    }


    public void onPause() {
        mMapView.onPause();
    }

    public void onDestroy() {
        mMapView.onDestroy();
    }

    public void onLowMemory() {
        mMapView.onLowMemory();
    }

}
