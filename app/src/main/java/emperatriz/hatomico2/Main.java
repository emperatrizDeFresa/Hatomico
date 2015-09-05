package emperatriz.hatomico2;

import emperatriz.hatomico2.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class Main extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Intent svc = new Intent(this, Service.class);

			SensorManager sensorListener = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			SensorEventListener movListener = (SensorEventListener) new MovementListener(this, svc);
			Sensor mAccelerometer = sensorListener.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Sensor mOrientation = sensorListener.getDefaultSensor(Sensor.TYPE_ORIENTATION);

			Sys.init().setSvc(svc);
			Sys.init().setContext(this);

			if (!Service.running) {

				boolean screenOff = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("screenOff", true);
				if (screenOff) {
					int screenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
					Sys.init().saveScreenTimeout(screenTimeout, this);
					Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Sys.SCREEN_TIMEOUT);
				}

				Sys.init().setMovementListener(movListener);
				Sys.init().setSensorListener(sensorListener);

				startService(svc);
				Sys.init().setActivated(true);

				boolean autoOff = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autoSwitchOff", true);
				if (autoOff) {
					// sensorListener.registerListener(movListener,
					// mAccelerometer, 1000);
					sensorListener.registerListener(movListener, mOrientation, 1000);
				}

			} else {
				Sys.init().switchOff();
			}

			finish();
		} catch (Exception ex) {
			Log.e("Hatomico", "" + ex.getMessage());
		}
	}
}