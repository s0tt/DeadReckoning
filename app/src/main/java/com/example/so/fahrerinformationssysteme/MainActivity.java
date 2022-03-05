package com.example.so.fahrerinformationssysteme;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.opencsv.CSVWriter;

public class MainActivity extends AppCompatActivity {
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private SensorHandler mSensorHandler;
    private LocationHandler mLocationHandler;

    private Handler mDataHandler;
    private int CSVdelay = 100; //1000/Hz
    private float timestamp = 0.000f;
    private long starttime;

    private boolean GPS_ONLINE_SIMULATED = true;
    HeightDR mHeightDR = null;

    private boolean CAPTURING_SENSORS = false;
    private CSVWriter writer;
    private DataStorage data = DataStorage.getInstance();


    private Activity currentActivity;
    public static final int PERM = 99;

    //Graphics
    private Button Btn_export;
    private Button Btn_import;
    private Uri file_path;
    private Button Btn_GPSoff;
    //Maps
    private MapsHandler mMapsHandler;
    //Chart
    private GraphHandler mGraphHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Permission Request
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS}, PERM);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentActivity = this;

        //init managers
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mSensorHandler = new SensorHandler(mSensorManager);
        mLocationHandler = new LocationHandler(mLocationManager);

        //init GPS with availbable
        data.fillDataEntry(1, 1);

        //get buttons from view
        Btn_export = findViewById(R.id.csv_export);
        Btn_import = findViewById(R.id.csv_import);
        Btn_GPSoff = findViewById(R.id.btn_gpsoff);

        //start graph handler by passing the graph object
        GraphHandler.getInstace().start((GraphView) findViewById(R.id.graph));
        mMapsHandler = MapsHandler.getInstance();
        mMapsHandler.start(savedInstanceState, currentActivity);
        mDataHandler = new Handler();

        //CSV collection button
        Btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CAPTURING_SENSORS) {
                    stopSensorCapture();
                    CAPTURING_SENSORS = false;
                    Btn_export.setText("Collect CSV");
                } else {
                    startSensorCapture();
                    CAPTURING_SENSORS = true;
                    Btn_export.setText("Collects...");
                }


            }
        });

        //btn click calls file dialog to import CSV
        Btn_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent()
                        .setType("*/*")
                        .setAction(Intent.ACTION_GET_CONTENT);

                //startActivityForResult(Intent.createChooser(intent, "Select a file"), 987);
                data.importCSV(currentActivity, "sensor_data.csv");
            }
        });

        //simulates a GPS Offline scenario
        //needed for testing
        Btn_GPSoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(GPS_ONLINE_SIMULATED) {
                    data.fillDataEntry(1, 0);
                    data.fillDataEntry(2, null);
                    data.fillDataEntry(3, null);
                    data.fillDataEntry(4, null);
                    data.fillDataEntry(5, null);
                    data.fillDataEntry(6, null);
                    data.fillDataEntry(7, null);
                    data.fillDataEntry(8, null);
                    data.fillDataEntry(9, null);
                    data.fillDataEntry(10, null);
                    mHeightDR = new HeightDR();
                    mHeightDR.start();
                    Btn_GPSoff.setText("GPS: Off");
                    Btn_GPSoff.setBackgroundColor(Color.parseColor("#e47d7d"));
                    mLocationHandler.stop();
                    GPS_ONLINE_SIMULATED = false;
                }else{
                    mLocationHandler.start();
                    Btn_GPSoff.setText("GPS: On");
                    Btn_GPSoff.setBackgroundColor(Color.parseColor("#85f1a9"));
                    mHeightDR.stop();
                    data.fillDataEntry(1, 1);
                    GPS_ONLINE_SIMULATED = true;
                }
            }
        });
    }

    //file dialog logic passes file path to CSV import function
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dat) {
        super.onActivityResult(requestCode, resultCode, dat);
        if (requestCode == 987 && resultCode == RESULT_OK) {
            file_path = dat.getData(); //The uri with the location of the file
            data.importCSV(currentActivity, file_path.getPath());
        }
    }

    //starts the handler which continously writes sensor data to the storage
    private void startSensorCapture() {
        GraphHandler.getInstace().clearGraph();
        data.clearSensorData();
        starttime = System.nanoTime();

        mDataHandler.postDelayed(new Runnable() {
            public void run() {
                //generate and write time stamp
                long elapsedTime = System.nanoTime() - starttime;
                data.fillDataEntry(0, elapsedTime / Math.pow(10, 9));

                //add data line
                data.addDataLine();
                mDataHandler.postDelayed(this, CSVdelay);
            }
        }, CSVdelay);
    }

    //stops the sensor capturing
    private void stopSensorCapture() {
        mDataHandler.removeCallbacksAndMessages(null);
        data.exportCSV(currentActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapsHandler.onResume();
        mSensorHandler.startListeners();
        mLocationHandler.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //mLocationManager.addNmeaListener(mNmeaListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mMapsHandler.onPause();
        //mSensorHandler.stopListeners();
        //mLocationHandler.stop();
    }

 /*    private OnNmeaMessageListener mNmeaListener = new OnNmeaMessageListener() {

       @Override
      public void onNmeaMessage(String nmeaSentence, long l) {
            if (nmeaSentence.isEmpty()) {
                return;
            }
            String[] nmeaParts = nmeaSentence.split(",");

            if (nmeaParts[0].equalsIgnoreCase("$GPGGA")) {
                if (nmeaParts.length > 8 && !(nmeaParts[8].isEmpty())) {
                    SensorData[6] = Float.parseFloat(nmeaParts[8]); //HDOP Parameter
                }

                if (nmeaParts.length > 7 && !(nmeaParts[7].isEmpty())) {
                    SensorData[5] = Float.parseFloat(nmeaParts[7]); //Number of satellites
                }
            }
        }
    };*/

}
