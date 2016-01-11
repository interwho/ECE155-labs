package ca.uwaterloo.lab4_201_16;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.mapper.InterceptPoint;
import ca.uwaterloo.mapper.LineSegment;
import ca.uwaterloo.mapper.MapLoader;
import ca.uwaterloo.mapper.MapView;
import ca.uwaterloo.mapper.NavigationalMap;
import ca.uwaterloo.mapper.PositionListener;
import ca.uwaterloo.mapper.VectorUtils;


public class MainActivity extends ActionBarActivity {

    public static int steps = 0; // Number of steps taken
    public static float direction = 0.0f; // Direction, from Pi to -Pi
    public static float[] displacement = new float[2]; // Will hold X-Component & Y-Component of Displacement
    public static TextView dspmntx;
    public static TextView dspmnty;
    public static TextView instruct;
    public static MapView svgmap;
    public static NavigationalMap map;
    public static float caliboffset = 0.0f;
    public static float steplen = 0.4f;

    // Update Route
    public static void updateRoute(MapView source, float xdisp, float ydisp) {
        float disttoturn = 0.0f;
        float desdirection = 0.0f; //direction
        PointF first = new PointF(5,15); //from top of left-most bottom centre barrier on 3344 svg
        PointF last = new PointF(20,15); //from top of right-most bottom centre barrier on 3344 svg
        PointF newcurpt = source.getUserPoint();
        newcurpt.set(newcurpt.x + xdisp / 2.5f, newcurpt.y + ydisp / 2.5f);
        source.setUserPoint(newcurpt);
        List<PointF> route = new ArrayList<>();
        List<InterceptPoint> pts = new ArrayList<InterceptPoint>();
        /* Commence Heavy Commenting To Allow For Easy Debugging, Since we keep crashing here */
        //Add starting point
        route.add(newcurpt);
        //Calculate connecting segment starting with the two points beyond any blocks on the grid, but
        //prior to any desks.
        pts = map.calculateIntersections(first, last);
        //Note that this only works for navigation between the two sides of the room. Far corners break
        //the algorithm, as we are only looking for connecting lines at y=7 on the map grid.
        while (!pts.isEmpty()) {
            //Brute force a path through the centre until we no longer have intersections with walls
            first.y -= 1;
            last.y -= 1;
            //Recalculate Intersections to see if it meets requirements with no intersecting points
            pts = map.calculateIntersections(first, last);
        }
        //Add the found no-collision segment to the route
        route.add(new PointF(newcurpt.x, first.y));
        route.add(new PointF(source.getDestinationPoint().x, first.y));
        //Add the final point to the destination
        route.add(source.getDestinationPoint());
        //Add the lines to the graph
        source.setUserPath(route);
        //Calculate direction pointer
        disttoturn = new LineSegment(route.get(0), route.get(1)).length();
        PointF parallel = new PointF(10, 0);
        PointF start = route.get(0);
        PointF end1 = route.get(1);
        desdirection = new VectorUtils().angleBetween(start, end1, parallel);
        /* End Heavy Commenting */

        //Are we there yet?
        if (source.getUserPoint().x == source.getDestinationPoint().x && source.getUserPoint().y == source.getDestinationPoint().y) {
            instruct.setText("You have reached your destination!");
        } else {
            //Update Instructions
            instruct.setText("Proceed " + disttoturn / steplen + "step(s) " + desdirection + " degrees from North");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        //Map
        svgmap = new MapView(getApplicationContext(), 600, 600, 20, 20);
        map = MapLoader.loadMap(getExternalFilesDir(null), "E2-3344-W2015.svg");
        svgmap.setMap(map);
        registerForContextMenu(svgmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        svgmap.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item) || svgmap.onContextItemSelected(item);
    }

    // Reset Displacement
    public void resetDisplacement(View v) {
        steps = 0;
        displacement[0] = 0;
        displacement[1] = 0;
        dspmntx.setText("E/W Displacement: " + displacement[0]);
        dspmnty.setText("N/S Displacement: " + displacement[1]);
    }

    // Calibrate North
    public void calibrateNorth(View v) {
        steps = 0;
        displacement[0] = 0;
        displacement[1] = 0;
        dspmntx.setText("E/W Displacement: " + displacement[0]);
        dspmnty.setText("N/S Displacement: " + displacement[1]);
        caliboffset = direction;
    }

    // A placeholder fragment containing a simple view.
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);


            LinearLayout l = (LinearLayout) rootView.findViewById(R.id.layout1);
            l.setOrientation(LinearLayout.VERTICAL); // Stacks Views Vertically


            //Labels
            TextView info2 = new TextView(rootView.getContext());
            info2.setText("Multidirectional Mapper - W15, Lab 4, Group 201-16"); // General Header
            l.addView(info2);

            TextView sensor2 = new TextView(rootView.getContext());
            sensor2.setText("Total Steps Taken: 0"); // Steps Taken
            l.addView(sensor2);

            TextView oView = new TextView(rootView.getContext());
            oView.setText("Orientation Value: 0"); // Orientation View
            l.addView(oView);

            dspmntx = new TextView(rootView.getContext());
            dspmntx.setText("E/W Displacement: 0"); // N/S Displacement View
            l.addView(dspmntx);

            dspmnty = new TextView(rootView.getContext());
            dspmnty.setText("N/S Displacement: 0"); // E/W Displacement View
            l.addView(dspmnty);

            instruct = new TextView(rootView.getContext());
            instruct.setText(""); // Instruction View
            l.addView(instruct);

            l.addView(svgmap);

            //Sensors
            SensorManager sensorManager = (SensorManager)
                    rootView.getContext().getSystemService(SENSOR_SERVICE);

            Sensor accelSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Used for Counting Steps
            SensorEventListener accelListen = new AccelerometerEventListener(sensor2);
            sensorManager.registerListener(accelListen, accelSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);

            Sensor rotSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); // Used for Finding Orientation
            SensorEventListener rotListen = new RotationEventListener(oView);
            sensorManager.registerListener(rotListen, rotSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);

            svgmap.addListener(new PositionListener() {
                @Override
                public void originChanged(MapView source, PointF loc) {
                    source.setUserPoint(loc);
                }

                @Override
                public void destinationChanged(MapView source, PointF dest) {
                    source.setDestinationPoint(dest);
                }
            });

            return rootView;
        }

        public int updateStep(double[] zVal) {

            boolean rise = false;
            boolean fall = false;
            double[] sVal = new double[15];//smoothed values
            //lowpass filter
            sVal[0] = 0.8 * zVal[0];
            for (int counter = 1; counter < 15; counter++) {
                sVal[counter] = 0.8 * zVal[counter] + 0.2 * sVal[counter - 1];
            }


            for (int i = 1; i < 15; i++) {
                if (sVal[i] > sVal[i - 1] && (sVal[i] - sVal[i - 1]) >= 0.4 && (sVal[i] - sVal[i - 1]) < 0.62) {//there has been a change in the z values that can falls within the bounds of a step
                    rise = true;
                } else if (sVal[i] < sVal[i - 1] && (sVal[i - 1] - sVal[i]) >= 0.4 && (sVal[i - 1] - sVal[i]) < 0.62) {//a similar drop has been detected
                    fall = true;
                }
            }
            if (rise & fall)//when a rise and fall occurs, there must be a step
            {
                calcDisplacement();
                return 1;

            }
            return 0;//no step detected
        }

        public void calcDisplacement()//calculates the user's displacement from the original point
        {
            float xComp;
            float yComp;
            float stepSize = 1.0f;//the size of a typical step

            xComp = (float) (stepSize * Math.cos(direction));
            yComp = (float) (stepSize * Math.sin(direction));


            displacement[0] = xComp;
            displacement[1] = yComp;

            dspmntx.setText("E/W Displacement: " + displacement[0]);
            dspmnty.setText("N/S Displacement: " + displacement[1]);

            updateRoute(svgmap, displacement[0], displacement[1]);

        }

        class RotationEventListener implements SensorEventListener {
            TextView output2;
            float[] orVals = new float[3]; // Orientation Values

            float[] I = new float[9];
            float[] mGravity = new float[9];
            float[] mGeomagnetic = new float[9];

            // Arrays Required By
            public RotationEventListener(TextView outputView) {
                output2 = outputView;
            }

            public void onAccuracyChanged(Sensor s, int i) {
            }

            public void onSensorChanged(SensorEvent se) {
                if (se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    float[] R = new float[9];//rotation matrix
                    SensorManager.getRotationMatrixFromVector(R, se.values);//outputs values to R based on current rotation values
                    SensorManager.getOrientation(R, orVals);//outputs orientation based on rotation matrix
                    output2.setText("Orientation Value: " + orVals[0]);//displays orientation form -Pi to Pi
                    direction = orVals[0];
                }
            }
        }

        class AccelerometerEventListener implements SensorEventListener {
            TextView output;
            //steps = 0;
            double smootheAccel = 0;
            double[] zVals = new double[15];//holds the previous 15 values for Z
            int counter = 0;

            public AccelerometerEventListener(TextView outputView) {
                output = outputView;

            }

            public void onAccuracyChanged(Sensor s, int i) {
            }

            public void onSensorChanged(SensorEvent se) {
                if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {


                    double zVal = se.values[2];//stores the z value
                    zVals[counter] = zVal;
                    counter++;
                    if (counter == 15)// 15 samples are taken
                    {
                        counter = 0;
                        steps += updateStep(zVals);//steps are sent for evaluation
                    }

                    output.setText("Total Steps Taken: " + steps + "          " + se.values[2]);//displays steps and accel info

                }
            }
        }
    }
}
