package com.example.so.fahrerinformationssysteme;

import android.graphics.Color;
import android.graphics.Paint;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by so on 09.02.2018.
 */

//class to create&interact with the graph view for the height visualisation
public class GraphHandler {
    private GraphView mGraphView;
    private boolean LAST_GPS_STATUS = true;

    //data series for estimated & real data
    LineGraphSeries<DataPoint> mLineGraphGPS;
    LineGraphSeries<DataPoint> mLineGraphDR;
    public static GraphHandler instance = null;

    public static GraphHandler getInstace() {
        if (GraphHandler.instance == null) {
            GraphHandler.instance = new GraphHandler();
        }
        return GraphHandler.instance;
    }

    public void start(GraphView _GraphView) {
        mGraphView = _GraphView;
        mLineGraphGPS = new LineGraphSeries<>();
        mLineGraphDR = new LineGraphSeries<>();

        //show data points in graph
        mLineGraphGPS.setDrawDataPoints(true);
        mLineGraphDR.setDrawDataPoints(true);
        mLineGraphDR.setColor(Color.BLACK);
        mGraphView.addSeries(mLineGraphGPS);
    }


    //add data point to the graph
    public void addDataPoint(float x, float y, Float isGps) {
        DataPoint mDataPoint = new DataPoint(x, y);
        if (isGps == null) {
            return;
        }

        //checks if GPS is available to add point to correct series
        if (isGps > 0.5) {
            //if gps changed from off -> on
            if(!LAST_GPS_STATUS){
                mLineGraphGPS = new LineGraphSeries<>();
                mLineGraphGPS.setDrawDataPoints(true);
            }
            mLineGraphGPS.appendData(mDataPoint, true, 6000);
            mGraphView.addSeries(mLineGraphGPS);
            LAST_GPS_STATUS = true;
        } else {
            //if gps changed from off -> on
            if(LAST_GPS_STATUS){
                mLineGraphDR = new LineGraphSeries<>();
                mLineGraphDR.setDrawDataPoints(true);
                mLineGraphDR.setColor(Color.BLACK);
            }

            mLineGraphDR.appendData(mDataPoint, true, 6000);
            //show data points in graph

            mGraphView.addSeries(mLineGraphDR);
            LAST_GPS_STATUS = false;
        }

        //moves the view area of the graph to the right --> keeps track of newest values
        Viewport vp = mGraphView.getViewport();
        vp.setXAxisBoundsManual(true);
        if (x > 20) {
            vp.setMinX(x - 20);
        } else {
            vp.setMinX(0);
        }

        vp.setMaxX(x + 5);

    }

    public void clearGraph() {
        mGraphView.removeAllSeries();
    }

}
