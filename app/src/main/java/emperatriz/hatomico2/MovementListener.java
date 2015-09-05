package emperatriz.hatomico2;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

public class MovementListener implements SensorEventListener{

	Context context;
	Intent svc;
	boolean finished=false;

	
	public MovementListener(Context context, Intent svc){
		this.context=context;
		this.svc= svc;
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
//			Log.i("Hatomico","v0="+Math.round(event.values[0])+" v1="+Math.round(event.values[1])+" v2="+Math.round(event.values[2]));

			if ((event.values[1]>40)&&(event.values[1]<140)&&!finished){
				finished=true;
				ArrayList<Long> pattern = new ArrayList<Long>();
				pattern.add(100l);
				try{
				String patternS = PreferenceManager.getDefaultSharedPreferences(Sys.init().getContext()).getString("offPattern", "l,c,c");

				
				for (String p : patternS.split(",")){

					if (p.equalsIgnoreCase("l")) pattern.add(500l);
					if (p.equalsIgnoreCase("c")) pattern.add(100l);
					pattern.add(100l);
				}
				}catch (Exception ex){
			
					pattern.add(500l);
					pattern.add(100l);
					pattern.add(100l);
					pattern.add(100l);
					pattern.add(100l);
					pattern.add(100l);
				}
				
				long[] pat = new long[pattern.size()];
				int i=0;
				for(long l : pattern){
					pat[i++]=l;

				}
				
				((Vibrator)MovementListener.this.context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(pat,-1);

				Sys.init().switchOff();
			}
			
			
		}
	}

}
