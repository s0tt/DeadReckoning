package com.example.so.fahrerinformationssysteme;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.os.Handler;
import android.util.Log;

/**
 * Created by so on 08.02.2018.
 */

//class handles sensor data capturing and its listeners
public class SensorHandler {
    private int SensorSamplingRate = 100000; //in Microseconds 100000 != 10Hz


    private DataStorage data =  DataStorage.getInstance();

    private SensorManager mSensorManager;
    private Sensor mACCELEROMETER;
    private Sensor mGYROSCOPE;
    private Sensor mPRESSURE;
    private Sensor mAMBIENT_TEMPERATURE;
    private Sensor mROTATION;
    private Sensor mGRAVITY;
    private Sensor mMAGNETIC;

    private Rotation calibrate = null;

    private float[] gravityValues = null;
    private float[] magneticValues = null;
    private float[] orientation = new float[3];
    private float[] rMat = new float[9];


    public SensorHandler(SensorManager _mSensorManager){
        mSensorManager = _mSensorManager;
        mACCELEROMETER = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGYROSCOPE = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mPRESSURE = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mAMBIENT_TEMPERATURE = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mROTATION = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGRAVITY = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mMAGNETIC = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void startListeners(){
        mSensorManager.registerListener(mSensorListener, mACCELEROMETER, SensorSamplingRate);
        mSensorManager.registerListener(mSensorListener, mGYROSCOPE, SensorSamplingRate);
        mSensorManager.registerListener(mSensorListener, mPRESSURE, SensorSamplingRate);
        mSensorManager.registerListener(mSensorListener, mAMBIENT_TEMPERATURE, SensorSamplingRate);
        mSensorManager.registerListener(mSensorListener, mROTATION, SensorSamplingRate);
        mSensorManager.registerListener(mSensorListener, mGRAVITY, SensorSamplingRate);
        mSensorManager.registerListener(mSensorListener, mMAGNETIC, SensorSamplingRate);


    }

    public void stopListeners(){
        mSensorManager.unregisterListener(mSensorListener);
    }


    private SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged (Sensor sensor,int accuracy){
        }

        @Override
        public void onSensorChanged (SensorEvent event){

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    data.fillDataEntry(11, event.values[0]);//ACCELEROMETER X
                    data.fillDataEntry(12, event.values[1]); //ACCELEROMETER Y
                    data.fillDataEntry(13, event.values[2]);
                    ; //ACCELEROMETER Z
                    gravityValues = event.values;

                    if (gravityValues != null && magneticValues != null) {
                        float[] deviceRelativeAcceleration = new float[4];
                        deviceRelativeAcceleration[0] = event.values[0];
                        deviceRelativeAcceleration[1] = event.values[1];
                        deviceRelativeAcceleration[2] = event.values[2];
                        deviceRelativeAcceleration[3] = 0;

                        float[] R = new float[16], I = new float[16], earthAcc = new float[16];

                        SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);

                        float[] inv = new float[16];

                        android.opengl.Matrix.invertM(inv, 0, R, 0);
                        android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                        //Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");
                    }

                    break;

                case Sensor.TYPE_GYROSCOPE:
                    data.fillDataEntry(14, event.values[0]); //GYROSCOPE X
                    data.fillDataEntry(15, event.values[1]); //GYROSCOPE Y
                    data.fillDataEntry(16, event.values[2]); //GYROSCOPE Z
                    break;

                case Sensor.TYPE_PRESSURE:
                    data.fillDataEntry(17, event.values[0]); // PRESSURE in hPa
                    break;

                case Sensor.TYPE_GRAVITY:
                    //gravityValues = event.values;
                    break;

                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticValues = event.values;
                    break;


                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    data.fillDataEntry(18, event.values[0]);
                    break;

                case Sensor.TYPE_ROTATION_VECTOR:
                    Rotation direction;

                    Rotation rotation = new Rotation(
                            (double) event.values[3], // quaternion scalar
                            (double) event.values[0], // quaternion x
                            (double) event.values[1], // quaternion y
                            (double) event.values[2], // quaternion z
                            false);

                    if (calibrate == null) {
                        calibrate = rotation;
                    } else {
                        direction = calibrate.applyInverseTo(rotation);
                        //System.out.println("Matrix: "+ direction.getMatrix()[2][0]+","+ direction.getMatrix()[2][1]+","+ direction.getMatrix()[2][2]);
                    }

                    SensorManager.getRotationMatrixFromVector( rMat, event.values );
                    // get the azimuth value (orientation[0]) in degree
                    int mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
                    //Log.d("Azimuth", "Value:" + mAzimuth);


                    break;
            }
                if (gravityValues != null && magneticValues != null) {
                    float R[] = new float[9];
                    float I[] = new float[9];

                    boolean success = SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);
                    if (success) {
                        float orientation[] = new float[3];
                        SensorManager.getOrientation(R, orientation);
                        float azimut = orientation[0];
                        float pitch = orientation[1];
                        float roll = orientation[2];
                        //Log.d("Orientation", "Azimut: "+azimut + " Pitch: " + pitch + "Roll: "+ roll);
                    }
                }


                    //System.out.println("Rotation: " + event.values[0] + "\t"+event.values[1] + "\t" + event.values[2] + "\t" + event.values[3]);
            }
    };
}
