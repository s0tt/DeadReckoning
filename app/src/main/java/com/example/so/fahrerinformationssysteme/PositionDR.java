package com.example.so.fahrerinformationssysteme;

import android.location.Location;
import android.os.Handler;
import android.provider.ContactsContract;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by so on 09.02.2018.
 */

public class PositionDR {
    private Float refBearing;
    private Float refVelocity;
    private LatLng refLatLng;
    private DataStorage mDataStorage;
    private Long refStartTime;
    private final float EARTH_RADIUS = 6371000; //Earth radius in meters
    private Handler DR = new Handler();
    private PolylineOptions lineOptions = null;

    public void start(){
        mDataStorage = DataStorage.getInstance();
        refBearing = mDataStorage.getLastValidParam(15);
        //simple velocity estimation by using last GPS velocity --> Integrate Accelerometer is very inaccurate
        refVelocity = mDataStorage.getLastValidParam(8);
        refLatLng = new LatLng(mDataStorage.getLastValidParam(3), mDataStorage.getLastValidParam(4));
        refStartTime = System.nanoTime();
        lineOptions = new PolylineOptions();
        lineOptions.add(refLatLng);

        DR.postDelayed(new Runnable() {
            public void run() {
                double bearing = mDataStorage.getCurrentParam(15);
                /*
                float distanceFactor = 1 / 6371000;


                //System.out.println("HeightDR: New:" + newHeight + " Delta: " + heightDelta);
                */
                //double deltaBearing =
                Double distancePassed = refVelocity* ((refStartTime - System.nanoTime()) / Math.pow(10, 9));
                LatLng newLocation = calcLatLng(bearing, distancePassed);
                System.out.println("PositionDR: Long:" + newLocation.longitude + " Lat: " + newLocation.latitude);
                lineOptions.add(newLocation);
                MapsHandler.getInstance().setLocation(newLocation);
                MapsHandler.getInstance().drawRoute(lineOptions);


                DR.postDelayed(this, 1000);


            }
        }, 50);





    }

    //calculates new LatLng out of old LatLng based on a bearing angle and a given distance
    private LatLng calcLatLng(Double bearing, Double distance){
        double calcLat = Math.asin( Math.sin(Math.toRadians(refLatLng.latitude))*Math.cos(distance/EARTH_RADIUS) +
                Math.cos(Math.toRadians(refLatLng.latitude))*Math.sin(distance/EARTH_RADIUS)*Math.cos(bearing) );

        double calcLng = Math.toRadians(refLatLng.longitude) + Math.atan2(Math.sin(bearing)*Math.sin(distance/EARTH_RADIUS)*
                Math.cos(Math.toRadians(refLatLng.latitude)), Math.cos(distance/EARTH_RADIUS) -
                Math.sin(Math.toRadians(refLatLng.latitude))*Math.sin(calcLat));
        return new LatLng(calcLat, calcLng);
    }
}
