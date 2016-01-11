package ca.uwaterloo.lab1_201_16;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.LinearLayout;
import ca.uwaterloo.sensortoy.LineGraphView;
import java.util.Arrays;

public class MainActivity extends ActionBarActivity {
  static  LineGraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        //LinearLayout layout = ((LinearLayout)findViewById(R.id.layout1));
        graph = new LineGraphView(getApplicationContext(),
                                  100,
                                  Arrays.asList("x","y","z"));
        graph.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            LinearLayout layout1 = (LinearLayout) rootView.findViewById(R.id.layout1);
            TextView title = new TextView(rootView.getContext());
            title.setText("Sensor Reader - Lab 1, Section 201, Group 16");
            layout1.addView(title);
            TextView tmax1 = new TextView(rootView.getContext());
            tmax1.setText("Maximum Accelerometer Values");
            layout1.addView(tmax1);
            TextView max1 = new TextView(rootView.getContext());
            max1.setText("0,0,0");
            layout1.addView(max1);
            TextView tmax2 = new TextView(rootView.getContext());
            tmax2.setText("Current Accelerometer Values");
            layout1.addView(tmax2);
            TextView max2 = new TextView(rootView.getContext());
            max2.setText("0,0,0");
            layout1.addView(max2);
            TextView tmax3 = new TextView(rootView.getContext());
            tmax3.setText("Maximum Light Sensor Values");
            layout1.addView(tmax3);
            TextView max3 = new TextView(rootView.getContext());
            max3.setText("0,0,0");
            layout1.addView(max3);
            TextView tmax4 = new TextView(rootView.getContext());
            tmax4.setText("Current Light Sensor Values");
            layout1.addView(tmax4);
            TextView max4 = new TextView(rootView.getContext());
            max4.setText("0,0,0");
            layout1.addView(max4);
            TextView tmax5 = new TextView(rootView.getContext());
            tmax5.setText("Maximum Magnetic Sensor Values");
            layout1.addView(tmax5);
            TextView max5 = new TextView(rootView.getContext());
            max5.setText("0,0,0");
            layout1.addView(max5);
            TextView tmax6 = new TextView(rootView.getContext());
            tmax6.setText("Current Magnetic Sensor Values");
            layout1.addView(tmax6);
            TextView max6 = new TextView(rootView.getContext());
            max6.setText("0,0,0");
            layout1.addView(max6);
            TextView tmax7 = new TextView(rootView.getContext());
            tmax7.setText("Maximum Rotation Vector Values");
            layout1.addView(tmax7);
            TextView max7 = new TextView(rootView.getContext());
            max7.setText("0,0,0");
            layout1.addView(max7);
            TextView tmax8 = new TextView(rootView.getContext());
            tmax8.setText("Current Rotation Vector Values");
            layout1.addView(tmax8);
            TextView max8 = new TextView(rootView.getContext());
            max8.setText("0,0,0");
            layout1.addView(max8);

            layout1.addView(graph);

            SensorManager sensorManager = (SensorManager)
                    rootView.getContext().getSystemService(SENSOR_SERVICE);
            Sensor lightSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            SensorEventListener one = new LightSensorEventListener(max4, max3);
            sensorManager.registerListener(one, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Sensor magnetSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            SensorEventListener two = new MagneticFieldEventListener(max6, max5);
            sensorManager.registerListener(two, magnetSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Sensor accelerometer =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            SensorEventListener three = new AccelerometerEventListener(max2, graph, max1);
            sensorManager.registerListener(three, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Sensor rotationVector =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            SensorEventListener four = new RotationVectorEventListener(max8, max7);
            sensorManager.registerListener(four, rotationVector, SensorManager.SENSOR_DELAY_NORMAL);
            return rootView;
        }

    }
}

/**
 * All Sensor Listeners
 */
class LightSensorEventListener implements SensorEventListener {
    TextView output;
    TextView max;
    public LightSensorEventListener(TextView outputView, TextView maximum){
        output = outputView;
        max = maximum;
    }

    public void onAccuracyChanged(Sensor s, int i) {}

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LIGHT) {
            // the variable se.values is an array of type int[] or double[].
            // the first value (se.values[0]) contains the value
            // of the light sensor. Store it somewhere useful.
            // String.format("(%f, %f, %f)", x, y, z);
            //if(max.)
            output.setText(Float.toString(se.values[0]));
        }
    }
}

class AccelerometerEventListener implements SensorEventListener {
    TextView output;
    LineGraphView outputGraph;
    TextView max;
    public AccelerometerEventListener(TextView outputView, LineGraphView outputGraphView,  TextView maximum){
        output = outputView;
        outputGraph = outputGraphView ;
        max = maximum;
    }

    public void onAccuracyChanged(Sensor s, int i) {}

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // the variable se.values is an array of type int[] or double[].
            // the first value (se.values[0]) contains the value
            // of the light sensor. Store it somewhere useful.
            //return se.values[0];
            output.setText(String.format("(%f, %f, %f)", Float.toString(se.values[0]), Float.toString(se.values[1]), Float.toString(se.values[2])));
            outputGraph.addPoint(se.values);
        }
    }
}

class MagneticFieldEventListener implements SensorEventListener {
    TextView output;
    TextView max;
    public MagneticFieldEventListener(TextView outputView,  TextView maximum){
        output = outputView;
        max = maximum;
    }

    public void onAccuracyChanged(Sensor s, int i) {}

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // the variable se.values is an array of type int[] or double[].
            // the first value (se.values[0]) contains the value
            // of the light sensor. Store it somewhere useful.
            //return se.values[0];
            output.setText(Float.toString(se.values[0]));
        }
    }
}

class RotationVectorEventListener implements SensorEventListener {
    TextView output;
    TextView max;
    public RotationVectorEventListener(TextView outputView,  TextView maximum){
        output = outputView;
        max = maximum;
    }

    public void onAccuracyChanged(Sensor s, int i) {}

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // the variable se.values is an array of type int[] or double[].
            // the first value (se.values[0]) contains the value
            // of the light sensor. Store it somewhere useful.
            //return se.values[0];
            output.setText(Float.toString(se.values[0]));
        }
    }
}
