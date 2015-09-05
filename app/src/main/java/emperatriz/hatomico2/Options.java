package emperatriz.hatomico2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class Options extends PreferenceActivity {

	boolean autoAnswer;
	String answerDelay;
	Preference autoAnswerP,screenOff;
	Preference answerDelayP, smsOnP, smsTypeP, smsTestP, smsMaxP, sendEmailP, switchOff, autoSwitchOff, about, whiteList, switchOffBt, switchOnBt;
	EditTextPreference offPattern;
	MultiSelectListPreference activatedApps;
	SharedPreferences prefs;
	int aboutTimes;
	int count = 0;
	boolean doNotFinish=false, switchedOff=false;

	@Override
	public void onStart() {
		super.onStart();
		boolean screenOff = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("screenOff", true);
    	if (screenOff){
    		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Sys.init().retrieveScreenTimeout(this));
    	}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (!doNotFinish) {
			int screenTimeout;
			try {
				if (!switchedOff){
		        	boolean screenOff = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("screenOff", true);
		        	if (screenOff){
		        		screenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);		
			        	Sys.init().saveScreenTimeout(screenTimeout, this);
			        	Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Sys.SCREEN_TIMEOUT);
		        	}
					
				}
			} catch (SettingNotFoundException e) {				
			}
			finish();
		}		
		else {
			doNotFinish=false;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		
		
		if (Build.VERSION.SDK_INT < 14) {
			super.setTheme(android.R.style.Theme_DeviceDefault_NoActionBar);
		} else if (Build.VERSION.SDK_INT >= 20){
            super.setTheme(android.R.style.Theme_Material_Light_DarkActionBar);
            getActionBar().setIcon(null);
        }




		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		aboutTimes = r.nextInt(11) + 3;

		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle("Hatomico");
		addPreferencesFromResource(R.xml.preferences);
		autoAnswerP = (Preference) findPreference("autoAnswer");
		answerDelayP = (Preference) findPreference("answerDelay");

		autoAnswerP.setOnPreferenceChangeListener(autoAnswer_click);

		smsOnP = (Preference) findPreference("smsOn");
		smsMaxP = (Preference) findPreference("smsMax");
		smsTypeP = (Preference) findPreference("smsType");
		smsTestP = (Preference) findPreference("smsTest");

		smsOnP.setOnPreferenceChangeListener(smsOn_click);
		smsTestP.setOnPreferenceClickListener(smsTest_click);

		sendEmailP = (Preference) findPreference("sendMail");
		sendEmailP.setOnPreferenceClickListener(sendEmail_click);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

        switchOnBt = (Preference) findPreference("switchOnBt");


		switchOffBt = (Preference) findPreference("switchOffBt");
		switchOffBt.setOnPreferenceClickListener(switchOffBt_click);
		
		switchOff = (Preference) findPreference("switchOff");
		switchOff.setOnPreferenceClickListener(switchOff_click);
		
		screenOff = (Preference) findPreference("screenOff");
		screenOff.setOnPreferenceChangeListener(screenOff_click);

		autoSwitchOff = (Preference) findPreference("autoSwitchOff");
		autoSwitchOff.setOnPreferenceClickListener(autoSwitchOff_click);
		
		

		offPattern = (EditTextPreference) findPreference("offPattern");

		about = (Preference) findPreference("version");
		about.setOnPreferenceClickListener(about_click);


		getPrefs();

		if (Build.VERSION.SDK_INT>20){
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("autoAnswer", false);
			editor.apply();
			autoAnswerP.setEnabled(false);
			answerDelayP.setEnabled(false);
			autoAnswerP.setSummary(getString(R.string.info50));
			((Preference) findPreference("speaker")).setEnabled(false);
			((Preference) findPreference("callMax")).setEnabled(false);
			((Preference) findPreference("callWhiteList")).setEnabled(false);
			((Preference) findPreference("whiteList")).setEnabled(false);
		}
		
		activatedApps  = (MultiSelectListPreference) findPreference("smsApps");
		activatedApps.setOnPreferenceChangeListener(activatedApps_click);
		Set<String> smsApps = prefs.getStringSet("smsApps", null);
		if ((smsApps==null)||(smsApps.size()==0))
			activatedApps.setSummary(getString(R.string.noApps));
		
		
		try {
			about.setTitle("Hatomico v"+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {

		}

		whiteList = (Preference) findPreference("whiteList");
		whiteList.setOnPreferenceClickListener(whiteList_click);

		
		String off = prefs.getString("offPattern", "l,c,c");
		offPattern.setText(off);
	}

	
	private OnPreferenceChangeListener activatedApps_click = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			activatedApps.setSummary("");
//			Set<String> smsApps = prefs.getStringSet("smsApps", null);
//			if ((smsApps!=null)&&(smsApps.size()!=0))
//				activatedApps.setSummary(smsApps.size() + " " + getString(R.string.activatedApps));
//			else	
//				activatedApps.setSummary(getString(R.string.noApps));
			return true;
		}
	};
	
	private OnPreferenceChangeListener autoAnswer_click = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {



				answerDelayP.setSelectable(!prefs.getBoolean("autoAnswer", true));




			return true;
		}
	};
	
	private OnPreferenceChangeListener screenOff_click = new OnPreferenceChangeListener() {
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(Options.this);
			myAlertDialog.setMessage(getString(R.string.reset));
			myAlertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			myAlertDialog.show();
			return true;
		}
	};

	private OnPreferenceChangeListener smsOn_click = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference arg0, Object arg1) {
			smsTypeP.setSelectable(!prefs.getBoolean("smsOn", false));
			smsMaxP.setSelectable(!prefs.getBoolean("smsOn", false));
			if (!prefs.getBoolean("smsOn", false)) {

				AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(Options.this);
				myAlertDialog.setMessage(getString(R.string.notificationSettings));
				myAlertDialog.setPositiveButton(getString(R.string.goToSettings), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						doNotFinish=true;
						Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
						startActivity(intent);
					}
				});
				myAlertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}
				});
				myAlertDialog.show();

			}
			return true;
		}
	};

	private OnPreferenceClickListener about_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			if (count < aboutTimes)
				count++;
			else {
				count = 0;
				Intent aboutIntent = new Intent(getApplicationContext(), About.class);
				aboutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getApplicationContext().startActivity(aboutIntent);
			}
			return true;
		}
	};

	private OnPreferenceClickListener whiteList_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			doNotFinish=true;
			Intent aboutIntent = new Intent(getApplicationContext(), ContactList.class);
			aboutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(aboutIntent);
			return true;
		}
	};

	private OnPreferenceClickListener sendEmail_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			try {
				doNotFinish=true; 
				Intent emailIntent = new Intent(Intent.ACTION_SEND);
				emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				emailIntent.setType("text/html");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.suggestion));
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "emperatriz.de.fresa@gmail.com" });
				startActivity(emailIntent);
			} catch (Exception ex) {

			}
			return true;
		}
	};

	private OnPreferenceClickListener smsTest_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			doNotFinish=true;
			Intent readIntent = new Intent(getApplicationContext(), ReadingSMS.class);
			NormalNotification normalNotification = new NormalNotification(getString(R.string.unknown), getString(R.string.testMessage), BitmapFactory.decodeResource(getResources(), R.drawable.hatomico), "Hatomico",getApplicationContext());
			Sys.init().currentNormalNotification = normalNotification;
			readIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(readIntent);
			return true;
		}
	};

	private OnPreferenceClickListener switchOffBt_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
			if (bta==null){
				 AlertDialog.Builder builder = new AlertDialog.Builder(Options.this);
				 builder.setMessage(getString(R.string.noBt));
				 builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int id) {
			           dialog.dismiss();
			        }
			    });
				 builder.show();
			}
			else if (bta.isEnabled()){
				Set<BluetoothDevice> pairedDevices = bta.getBondedDevices();
				if (pairedDevices.size()>0){
					final CharSequence[] items = new String[pairedDevices.size()];
					final ArrayList<String> macs = new ArrayList<String>();
					final boolean[] states = new boolean[pairedDevices.size()];
					int i=0;
					for (BluetoothDevice device : pairedDevices){
						states[i]=Sys.init().isDeviceBound(device, Options.this);
						macs.add(device.getAddress());
						items[i++]=device.getName();
					}
				    
				    AlertDialog.Builder builder = new AlertDialog.Builder(Options.this);
				    builder.setTitle(getString(R.string.devicesBound));
				    builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
				        public void onClick(DialogInterface dialogInterface, int item, boolean state) {
				        }
				    });
				    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int id) {
				            long[] CheCked = ((AlertDialog)dialog).getListView().getCheckItemIds();
				            ArrayList<String> btMacs = new ArrayList<String>();
				            for (int i=0;i<CheCked.length;i++){
				            	btMacs.add(macs.get((int)CheCked[i]));
				            }
				           Sys.init().saveBluetoothMac(btMacs, Options.this);
				        }
				    });
				    builder.show();
				}
				else{
					 AlertDialog.Builder builder = new AlertDialog.Builder(Options.this);
					 builder.setMessage(getString(R.string.noBtPaired));
					 builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int id) {
				           dialog.dismiss();
				        }
				    });
					builder.show();
				}
				
			} else{
				 AlertDialog.Builder builder = new AlertDialog.Builder(Options.this);
				 builder.setMessage(getString(R.string.enableBt));
				 builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int id) {
			           dialog.dismiss();
			        }
			    });
				 builder.setNeutralButton(getString(R.string.goToSettings), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int id) {
				        	Intent intentOpenBluetoothSettings = new Intent();
				        	intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS); 
				        	startActivity(intentOpenBluetoothSettings); 
				        }
				    });
				 builder.show();
			}
			

			return true;
		}
	};



	private OnPreferenceClickListener switchOff_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			
			switchedOff=true;
			Sys.init().switchOff();

			return true;
		}
	};

	private OnPreferenceClickListener autoSwitchOff_click = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			offPattern.setSelectable(prefs.getBoolean("autoSwitchOff", true));

			if (prefs.getBoolean("autoSwitchOff", true)) {
				Sys.init().getSensorListener().registerListener(Sys.init().getMovementListener(), Sys.init().getSensorListener().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 1000);
			} else {
				Sys.init().getSensorListener().unregisterListener(Sys.init().getMovementListener());
			}

			return true;
		}
	};

	private void getPrefs() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		autoAnswer = prefs.getBoolean("autoAnswer", true);
		answerDelay = prefs.getString("answerDelay", "4");
		prefs.getBoolean("smsOn", true);
		prefs.getString("smsType", "0");
		smsTypeP.setSelectable(prefs.getBoolean("smsOn", true));
		answerDelayP.setSelectable(prefs.getBoolean("autoAnswer", true));
	}
}
