package emperatriz.hatomico2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;

import android.telephony.SmsMessage;
import android.util.Log;

public class SMSListener extends BroadcastReceiver {

	static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {

		
			if ((Sys.init().isActivated())&&(intent.getAction().equals(ACTION))) {
				Log.i("Hatomico", "new SMS");
				// ---get the SMS message passed in---
				Bundle bundle = intent.getExtras();
				SmsMessage[] msgs = null;
				String str = "";
				if (bundle != null) {
					// ---retrieve the SMS message received---
					Object[] pdus = (Object[]) bundle.get("pdus");
					msgs = new SmsMessage[pdus.length];

					msgs[0] = SmsMessage.createFromPdu((byte[]) pdus[0]);

					if (!PreferenceManager.getDefaultSharedPreferences(context)
							.getBoolean("smsWhiteList", false)) {
						Log.i("Hatomico", "enqueuing SMS");
						Sys.init().addSms(
								new NormalNotification(msgs[0].getOriginatingAddress(),
										msgs[0].getMessageBody().toString()));
					} else {
						if (Sys.init().isValid(msgs[0].getOriginatingAddress())) {
							Log.i("Hatomico", "enqueuing SMS");
							Sys.init()
									.addSms(new NormalNotification(msgs[0]
											.getOriginatingAddress(), msgs[0]
											.getMessageBody().toString()));
						} else {
							Log.i("Hatomico", "not in list");
						}
					}

				}
			} else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (Sys.init().isDeviceBound(device, context)) {
					try{
						Intent svc = new Intent(context, Service.class);
				        
				        SensorManager sensorListener = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
				        SensorEventListener movListener = (SensorEventListener) new MovementListener(context, svc);
				        Sensor mAccelerometer = sensorListener.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
				        Sensor mOrientation = sensorListener.getDefaultSensor(Sensor.TYPE_ORIENTATION);
				        
				        Sys.init().setSvc(svc);
				        Sys.init().setContext(context);
				        
				        
				        if ((Service.class==null)||(!Service.running)) 
				        {
				        	boolean screenOff = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("screenOff", true);
				        	if (screenOff){
					        	int screenTimeout = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
					        	Sys.init().saveScreenTimeout(screenTimeout, context);
					        	Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Sys.SCREEN_TIMEOUT);
				        	}
				            Sys.init().setMovementListener(movListener);
				            Sys.init().setSensorListener(sensorListener);
				        	
				            context.startService(svc);
				            Sys.init().setActivated(true);
				            
				            boolean autoOff = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autoSwitchOff", true);
				            if (autoOff){
				            	//sensorListener.registerListener(movListener, mAccelerometer, 1000);
				            	sensorListener.registerListener(movListener, mOrientation, 1000);
				            }
				            
				        }
					}
			        catch (Exception ex){
			        	Log.e("Hatomico",""+ex.getMessage());
			        }
					
				}
				
			} else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {

                boolean autoBTOn = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("switchOnBt", false);
                if (autoBTOn){
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                mBluetoothAdapter.enable();
                            }
                        }, 2000);
                    }
                }
                else{
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (Sys.init().isDeviceBound(device, context)) Sys.init().switchOff();
                }


            } else if (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1) == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                Sys.init().setSco(true);
            }

		}



	

}
