package ca.uwaterloo.Lab2_201_16;

import com.example.lab1_practice.R;

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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	public static int steps = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			
			
			LinearLayout l = (LinearLayout)rootView.findViewById(R.id.layout1);
			l.setOrientation(LinearLayout.VERTICAL);//stacks views vertically
			
			//textviews
			
			
			TextView info2 = new TextView(rootView.getContext());
			info2.setText("Acceleration Sensor Info");
			l.addView(info2);
			
			TextView sensor2 = new TextView(rootView.getContext());
			sensor2.setText("sensor2");
			l.addView(sensor2);
			

			
			
			//sensors
			SensorManager sensorManager = (SensorManager)
					rootView.getContext().getSystemService(SENSOR_SERVICE);
		
			
			Sensor accelSensor =
					sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			SensorEventListener accelListen = new AccelerometerEventListener(sensor2);
			sensorManager.registerListener(accelListen, accelSensor,
			SensorManager.SENSOR_DELAY_FASTEST);
			
			

			
			return rootView;
		}
		
		
		
		
		class AccelerometerEventListener implements SensorEventListener 
		{
			TextView output;
		    //steps = 0;
			double smootheAccel = 0;
			double[] zVals = new double[15];//holds the previous 5 values for Z
			int counter = 0;
			public AccelerometerEventListener(TextView outputView){
			output = outputView;
			
			}
			public void onAccuracyChanged(Sensor s, int i) {}
			public void onSensorChanged(SensorEvent se) {
			if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				
				
				double zVal =  se.values[2];
				zVals[counter] = zVal;
				counter++;
				if(counter == 15)
				{
					counter = 0;
					steps += updateStep(zVals);
				}
				
				output.setText("Steps taken: " + steps + "          " + se.values[2]);
				
			}
			}
			}
		
		public int updateStep(double[] zVal)
		{
			
			String state = "rising";
			boolean rise = false;
			boolean stable = false;
			boolean fall = false;
			double[] sVal = new double[15];
			//lowpass filter
			sVal[0] = 0.8*zVal[0];
			for(int counter = 1; counter < 15; counter++)
			{
				sVal[counter] = 0.8*zVal[counter] + 0.2*sVal[counter-1];
			}
		
			
			for(int i = 1; i < 15; i++)
			{
				if(sVal[i] > sVal[i-1] && (sVal[i]-sVal[i-1]) >= 0.4 && (sVal[i]-sVal[i-1]) < 0.62)
				{
					state = "rising";
					rise = true;
				}
				else if(sVal[i] < sVal[i-1] && (sVal[i-1]-sVal[i]) >= 0.4 && (sVal[i-1]-sVal[i]) < 0.62)
				{
					state = "falling";
					fall = true;
				}
				else
				{
					state = "stable";
				}
			}
			if(rise & fall)
			{
				return 1;
			}
			return 0;
		}
	}
	
	public void resetSteps(View v)
	{
		steps = 0;
	}
}
