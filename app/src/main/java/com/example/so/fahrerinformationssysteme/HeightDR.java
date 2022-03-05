package com.example.so.fahrerinformationssysteme;

import android.os.Handler;

/**
 * Created by so on 09.02.2018.
 */

//class which calculates the dead reckoning for the altitude
public class HeightDR {
    private DataStorage mDataStorage;
    private Float referenceHeight;
    private Float referencePressure;
    private Handler DR = new Handler();

    public HeightDR() {
        mDataStorage = DataStorage.getInstance();
    }

    public void start() {
        //get last known values from the log before GPS failed
        referencePressure = mDataStorage.getLastValidParam(17);
        referenceHeight = mDataStorage.getLastValidParam(7);

        //handler which continously estimates new height based on pressure sensor
        DR.postDelayed(new Runnable() {
            public void run() {
                Float currentPressure = mDataStorage.getCurrentParam(17);
                Float pressureDelta = currentPressure - referencePressure;

                //Calculate height delta based on pressure delta: https://de.wikipedia.org/wiki/Barometrische_H%C3%B6henformel
                Float density = (101325 / (287.058f * ((mDataStorage.getCurrentParam(18) + 273.15f))));
                Float heightDelta = (pressureDelta / -density * 9.810f);
                Float newHeight = referenceHeight + heightDelta;

                mDataStorage.fillDataEntry(7, newHeight);
                //System.out.println("HeightDR: New:" + newHeight + " Delta: " + heightDelta);

                DR.postDelayed(this, 1000);

            }
        }, 50);
    }

    //stops dead reckoning when GPS is avilable again
    public void stop() {
        DR.removeCallbacksAndMessages(null);
    }



        /*
        while(mDataStorage.getCurrentParam(1) == 0){
            Float[] recentLine = mDataStorage.getSensorDataLine(0);
            Float[] secondRecentLine = mDataStorage.getSensorDataLine(1);
            if(recentLine[17] != pastHeight && secondPastHeight != secondRecentLine[17]) {
                Float pressureDelta = recentLine[17] - secondRecentLine[17];

                //Calculate height delta based on pressure delta: https://de.wikipedia.org/wiki/Barometrische_H%C3%B6henformel
                Double density = 101325 / 287.058 * (recentLine[18] + 273.15);
                Double heightDelta = pressureDelta / density * 9.81;
                Double newHeight = mDataStorage.getLastValidParam(7) + heightDelta;
                mDataStorage.fillDataEntry(7, newHeight);
            }
        }
        */


}
