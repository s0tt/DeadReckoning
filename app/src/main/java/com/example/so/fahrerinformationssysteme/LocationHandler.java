package com.example.so.fahrerinformationssysteme;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * Created by so on 08.02.2018.
 */

// class which manages the location sensoring
public class LocationHandler {
    private int SensorSamplingRate = 100000; //in microseconds 100000 == 10Hz
    private LocationManager mLocationManager;
    private DataStorage data = DataStorage.getInstance();
    private HeightDR mHeightDR = new HeightDR();

    public LocationHandler(LocationManager _mLocationManager){
        mLocationManager = _mLocationManager;
    }

    @SuppressLint("MissingPermission")
    public void start(){
        //collect GPS data
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, SensorSamplingRate / 1000, 5, mLocationListener);
    }

    public void stop(){
        mLocationManager.removeUpdates(mLocationListener);
    }


    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            data.fillDataEntry(2, location.getAccuracy());
            data.fillDataEntry(3, location.getLatitude());
            data.fillDataEntry(4, location.getLongitude());
            //5 & 6 NMEA info
            data.fillDataEntry(7, location.getAltitude());
            data.fillDataEntry(8, location.getSpeed());
            data.fillDataEntry(9, location.getBearing());
            data.fillDataEntry(10, location.getTime());
            MapsHandler.getInstance().setLocation(location);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            if( s.equals(LocationManager.GPS_PROVIDER)){
                if(i == LocationProvider.AVAILABLE) {

                    /* Actual code for real conditions --> Does not work for simulator*/
                    data.fillDataEntry(1, 1);
                    /*
                    if(data.getCurrentParam(1) != null) {
                        mHeightDR.stop();
                    }
                    */

                }else{
                    data.fillDataEntry(1, 0);
                    /*
                     //Actual code for real conditions --> Does not work for simulator
                    if(data.getCurrentParam(1) != null) {
                        //set current gps data to null
                        setGPSDatNull();
                        //Start Heigh Dead Reckogning
                        mHeightDR.start();
                    }
                    */

                }

            }
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {

        }

        private void setGPSDatNull(){
            data.fillDataEntry(2, null);
            data.fillDataEntry(3, null);
            data.fillDataEntry(4, null);
            data.fillDataEntry(5, null);
            data.fillDataEntry(6, null);
            data.fillDataEntry(7, null);
            data.fillDataEntry(8, null);
            data.fillDataEntry(9, null);
            data.fillDataEntry(10, null);
        }


    };

}
