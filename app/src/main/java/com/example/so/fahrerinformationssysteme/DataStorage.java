package com.example.so.fahrerinformationssysteme;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 * Created by so on 07.02.2018.
 */


//class which holds&manages all the sensor data
public class DataStorage {
    //Singleton
    private static DataStorage instance = null;
    //Array List for the sensor data lines over time
    private ArrayList<Float[]> SensorData = new ArrayList<>();
    //Array for the current sensor data
    private Float[] SensorArray = new Float[19];
    DecimalFormat df = new DecimalFormat("#.000");

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //Singleton
    public static DataStorage getInstance() {
        if (DataStorage.instance == null) {
            DataStorage.instance = new DataStorage();
        }
        return DataStorage.instance;
    }

    public void clearSensorData() {
        SensorData.clear();
    }

    public void addDataLine(Float[] externalLine) {
        Float[] buffer = externalLine.clone();
        SensorData.add(buffer);
    }

    public void addDataLine() {
        //writes current sensor data to sensor log array list
        Float[] buffer = SensorArray.clone();
        SensorData.add(buffer);
    }

    public void fillDataEntry(int index, Object entry) {
        if (entry != null) {
            Float buffer = Float.valueOf(String.valueOf(entry));
            SensorArray[index] = Float.parseFloat(df.format(buffer));

            //print height to graph
            if (index == 7) {
                if (SensorArray[0] != null && SensorArray[7] != null) {
                    GraphHandler.getInstace().addDataPoint(SensorArray[0], SensorArray[7], SensorArray[1]);
                }
            }
        } else {
            SensorArray[index] = null;
        }
    }

    //OpenCSV for generating & reading CSV files
    public int exportCSV(Activity activity) {
        try {
            verifyStoragePermissions(activity);
            //writes to external storage of simulator
            String path = Environment.getExternalStorageDirectory() + "/sensor_data.csv";
            CSVWriter writer = new CSVWriter(new FileWriter(path), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
            for (Float[] line : SensorData) {
                String[] arr = float2string(line);
                writer.writeNext(arr);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    public int importCSV(Activity activity, String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory() + path), ',');
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                //write the lines to the sensor data log array lists
                addDataLine(string2float(nextLine));
            }
            return 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Float getLastValidParam(int idx) {
        //returns the most recent sensor data which is not null from the log
        ListIterator li = SensorData.listIterator(SensorData.size());
        Float rtnVal = null;
        while (li.hasPrevious() && rtnVal == null) {
            Float[] line = (Float[]) li.previous();
            rtnVal = line[idx];
        }
        return rtnVal;
    }

    //float & string conversions
    private String[] float2string(Float[] in) {

        String[] out = new String[in.length];
        for (int i = 0; i < in.length; i++) {
            if (in[i] == null) {
                out[i] = String.valueOf(0);
            } else {
                out[i] = String.valueOf(in[i]);
            }
        }
        return out;
    }

    private Float[] string2float(String[] in) {
        Float[] out = new Float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = Float.parseFloat(in[i]);
        }
        return out;
    }

    //request permission
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    public Float getCurrentParam(int idx) {
        return SensorArray[idx];
    }

    public Float[] getSensorDataLine(int offset_end) {
        return SensorData.get(SensorData.size() - offset_end - 1);
    }


}
