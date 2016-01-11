package ca.uwaterloo.lab3_201_16;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.uwaterloo.mapper.MapView;
import ca.uwaterloo.mapper.NavigationalMap;
import ca.uwaterloo.mapper.MapLoader;

import ca.uwaterloo.lab3_201_16.R;



public class MainActivity extends ActionBarActivity {

    public static int steps = 0; // Number of steps taken
    public static float direction = 0.0f; // Direction, from Pi to -Pi
    public static float[] displacement = new float[2]; // Will hold X-Component & Y-Component of Displacement
    public static TextView dspmntx;
    public static TextView dspmnty;
    public static MapView svgmap;
    public static float caliboffset = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        //Map
        svgmap = new MapView(getApplicationContext(), 400, 400, 60, 60);
        NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null), "Lab-room-peninsula.svg");
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu , View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu , v, menuInfo);
        svgmap.onCreateContextMenu(menu , v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item) || svgmap.onContextItemSelected(item);
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


            LinearLayout l = (LinearLayout)rootView.findViewById(R.id.layout1);
            l.setOrientation(LinearLayout.VERTICAL); // Stacks Views Vertically


            //Labels
            TextView info2 = new TextView(rootView.getContext());
            info2.setText("Multidirectional Mapper - W15, Lab 3, Group 201-16"); // General Header
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


            return rootView;
        }


        class RotationEventListener implements SensorEventListener
        {
            TextView output2;
            float[] orVals = new float[3]; // Orientation Values

            float[] I = new float[9];
            float[] mGravity = new float[9];
            float[] mGeomagnetic = new float[9];
            // Arrays Required By
            public RotationEventListener(TextView outputView){
                output2 = outputView;
            }
            public void onAccuracyChanged(Sensor s, int i) {}
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

        class AccelerometerEventListener implements SensorEventListener
        {
            TextView output;
            //steps = 0;
            double smootheAccel = 0;
            double[] zVals = new double[15];//holds the previous 15 values for Z
            int counter = 0;
            public AccelerometerEventListener(TextView outputView){
                output = outputView;

            }
            public void onAccuracyChanged(Sensor s, int i) {}
            public void onSensorChanged(SensorEvent se) {
                if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {


                    double zVal =  se.values[2];//stores the z value
                    zVals[counter] = zVal;
                    counter++;
                    if(counter == 15)// 15 samples are taken
                    {
                        counter = 0;
                        steps += updateStep(zVals);//steps are sent for evaluation
                    }

                    output.setText("Total Steps Taken: " + steps + "          " + se.values[2]);//displays steps and accel info

                }
            }
        }

        public int updateStep(double[] zVal)
        {

            boolean rise = false;
            boolean fall = false;
            double[] sVal = new double[15];//smoothed values
            //lowpass filter
            sVal[0] = 0.8*zVal[0];
            for(int counter = 1; counter < 15; counter++)
            {
                sVal[counter] = 0.8*zVal[counter] + 0.2*sVal[counter-1];
            }


            for(int i = 1; i < 15; i++)
            {
                if(sVal[i] > sVal[i-1] && (sVal[i]-sVal[i-1]) >= 0.4 && (sVal[i]-sVal[i-1]) < 0.62)
                {//there has been a change in the z values that can falls within the bounds of a step
                    rise = true;
                }
                else if(sVal[i] < sVal[i-1] && (sVal[i-1]-sVal[i]) >= 0.4 && (sVal[i-1]-sVal[i]) < 0.62)
                {//a similar drop has been detected
                    fall = true;
                }
            }
            if(rise & fall)//when a rise and fall occurs, there must be a step
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
            float stepSize = 1;//the size of a typical step

            xComp = (float)(stepSize*Math.cos(direction));
            yComp = (float)(stepSize*Math.sin(direction));


            displacement[0] = displacement[0] + xComp;
            displacement[1] = displacement[1] + yComp;

            dspmntx.setText("E/W Displacement: " + displacement[0]);
            dspmnty.setText("N/S Displacement: " + displacement[1]);

        }
    }

    // Reset Displacement
    public void resetDisplacement(View v)
    {
        steps = 0;
        displacement[0] = 0;
        displacement[1] = 0;
        dspmntx.setText("E/W Displacement: " + displacement[0]);
        dspmnty.setText("N/S Displacement: " + displacement[1]);
    }

    // Calibrate North
    public void calibrateNorth(View v)
    {
        steps = 0;
        displacement[0] = 0;
        displacement[1] = 0;
        dspmntx.setText("E/W Displacement: " + displacement[0]);
        dspmnty.setText("N/S Displacement: " + displacement[1]);
        caliboffset = direction;
    }
}
