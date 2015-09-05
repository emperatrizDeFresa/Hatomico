package emperatriz.hatomico2;



import java.util.HashMap;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReadingSMS extends Activity implements OnClickListener, OnInitListener, SensorEventListener, OnTouchListener{

	private int MY_DATA_CHECK_CODE = 0;
	
	static final int  LEFT=1, RIGHT=2, TOP=3, BOTTOM=4, NONE=5;

	private TextToSpeech tts;
    
    private String sender, text, contactName="",photoId="";
    
    private SensorManager  mSensorManager;
    
    private boolean click;
    private PowerManager.WakeLock wl;
    
    private KeyguardManager mKeyguardManager;
    private KeyguardManager.KeyguardLock mKeyguardLock1;
    
    private boolean closing=false;
    private LinearLayout background;
    private FrameLayout frame;
    ImageView imgSender;

    
    private boolean resumed=false;
    
    private boolean halt = false;
    
    boolean spoken=false;
    
    AudioManager audioManager;

    
    private final Runnable mUpdateUITimerTask = new Runnable() {
	    public void run() {
	        readAndClose();
	    }
	};
	private final Handler mHandler = new Handler();
    
	Notification notification;
	Boolean initOk;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Sys.init().setTreatingSms(true);
		
		initOk=false;
		
		try{
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
				
		 PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Hatomico");
		 
		 
		 
		
		mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock1 = mKeyguardManager.newKeyguardLock("Hatomico"); 
		mKeyguardLock1.disableKeyguard(); 
		


		wl.acquire();
		
		setContentView(R.layout.sms);

		
		notification = Sys.init().currentNormalNotification;
		imgSender =  (ImageView) findViewById(R.id.imgSender);
		TextView txvSender =  (TextView) findViewById(R.id.txvSender);
		TextView txvApp =  (TextView) findViewById(R.id.textView2);
		
		
		if (notification.getIcon()!=null){
        	imgSender.setImageBitmap(notification.getIcon());
		}

		txvSender.setText(notification.getSender().toUpperCase());
		txvApp.setText(notification.getApp().toUpperCase());
			
	
		background =  (LinearLayout) findViewById(R.id.linearLayout1);
		background.setOnClickListener(this); 
		background.setOnTouchListener(this);
		
		frame =  (FrameLayout) findViewById(R.id.smsFrame);
		if (notification.getApp().toUpperCase().equals("HANGOUTS")){
			frame.setBackgroundColor(0xff3F992C);
		}
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
		
		int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("smsType", "0"));
        if (type==2){      	
        	
        	mHandler.postDelayed(mUpdateUITimerTask, 3 * 1000);       	
        }
		}catch (Exception ex){
			finish();
		}
	}
	
	
	

	
	
	
	int musicVolume = -999;
	
	
	private void readAndClose(){
		

		
		musicVolume = -999;
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		try{
			Log.i("Hatomico","reading");
			closing=true;
			
			musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			
			if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("smsMax", true)){
				
				audioManager.setSpeakerphoneOn(true);
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
				
			}
			
			if (tts==null) tts = new TextToSpeech(this, this);


//            audioManager.setMode(AudioManager.MODE_IN_CALL);


			int i=0;
			while (i<5&&audioManager.requestAudioFocus(null, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
				i++;
				Log.i("Hatomico","audiofocus error");
			}
			
//			synchronized (this) {
	
				sender = notification.getSender();
				if (sender.toUpperCase().equals("WHATSAPP")) sender = getString(R.string.whatsapp);
				if (sender.toUpperCase().equals("GOOGLE NOW")) sender = getString(R.string.googlenow);
				if (sender.toUpperCase().equals("HANGOUTS")) sender = getString(R.string.hangouts);
				if (sender.toUpperCase().equals("TWITTER")) sender = getString(R.string.twitter);
				if (sender.toUpperCase().equals("GMAIL")) sender = getString(R.string.gmail);
				
				if (!Sys.init().isKnown(sender))  sender = getString(R.string.unknown);
				
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "HTMC Id");
//                params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_VOICE_CALL));
                tts.speak(sender + " " + getString(R.string.says) + ". " + Sys.init().translateEmoji(notification.getText()), TextToSpeech.QUEUE_ADD, params);

            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
					
					@Override
					public void onStart(String utteranceId) {
						
					}
					
					@Override
					public void onError(String utteranceId) {
						Sys.init().addSms(Sys.init().currentNormalNotification);
						endSpeech(NONE);
						
					}
					
					@Override
					public void onDone(String utteranceId) {
						if (!alreadyEnding) endSpeech(NONE);
						
					}
				});
				
	
		}
		catch (Exception ex){
			Sys.init().addSms(Sys.init().currentNormalNotification);
			finish();
		}


	}
	
	boolean alreadyEnding=false;
	
	private void endSpeech(int animation){
		alreadyEnding=true;
		if ((wl!=null)&&(wl.isHeld())) wl.release();
		if (audioManager!=null) audioManager.abandonAudioFocus(null);
		tts.shutdown();
					
		mKeyguardLock1.reenableKeyguard();
		
		if (musicVolume!=-999) if (audioManager!=null) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0);
		Sys.init().currentNormalNotification =null;
		Sys.init().setTreatingSms(false);
		finish();
		switch (animation){
		case TOP:
			overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_top);
			break;
		case BOTTOM:
			overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_bottom);
			break;
		case LEFT:
			overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left);
			break;
		case RIGHT:
			overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_right);
			break;
		}
	}


	 public void onBackPressed() {
		 if ((wl!=null)&&(wl.isHeld())) wl.release();
			
			tts.shutdown();
						
			mKeyguardLock1.reenableKeyguard();
			
			
			Sys.init().currentNormalNotification =null;
			Sys.init().setTreatingSms(false);
			finish();
	 }
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.ERROR) {
            Toast.makeText(this,"Oooops, no se pudo inicializar Text to speech. ", Toast.LENGTH_LONG).show();
            Sys.init().addSms(Sys.init().currentNormalNotification);
            initOk=false;
            Sys.init().setTreatingSms(false);
            finish();
        } else if (status == TextToSpeech.SUCCESS){
        	initOk=true;
        }
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);
                
                
            }
            else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
 
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	@Override 
	public void onSensorChanged(SensorEvent event) {
		if (!closing){
			int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("smsType", "0"));
			Log.i("Hatomico","sensor="+event.values[0]);
			if (event.values[0]<1){
				if (type==1) 
					readAndClose();
			}
		}
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.i("Hatomico","onPause");
		mKeyguardLock1.reenableKeyguard();
		mSensorManager.unregisterListener(this);
		if (!resumed) Sys.init().setTreatingSms(false);
		resumed=false;
		if ((wl!=null)&&(wl.isHeld())) wl.release();
		

	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		resumed=true;
		Log.i("Hatomico","onResume");
		mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
		

	}


	float firstY, firstX, gap=50;

	
	
	@Override
	public void onClick(View v) {
		
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		alreadyEnding=true;
	    switch(event.getAction()) {
	        case (MotionEvent.ACTION_DOWN) :       		      
	        	firstY = event.getAxisValue(MotionEvent.AXIS_Y);
	        	firstX = event.getAxisValue(MotionEvent.AXIS_X);
	        	return false;
	        case (MotionEvent.ACTION_MOVE) :
	        	tts.stop(); 
		        float y = event.getAxisValue(MotionEvent.AXIS_Y); 
	        	float x = event.getAxisValue(MotionEvent.AXIS_X);     
	        	
	        	if (x-firstX>gap) // Hacia la derecha
	        		endSpeech(RIGHT);
	        	else if (y-firstY>gap) // Hacia abajo
	        		endSpeech(BOTTOM);
	         	else if (x-firstX<-gap) // Hacia la izquierda  
	        		endSpeech(LEFT);
	         	else if (y-firstY<-gap) // Hacia arriba
	        		endSpeech(TOP);


		        return false;
	            
	        case (MotionEvent.ACTION_UP) :
	        	if (!closing){
	    			int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("smsType", "0"));
	    			Log.i("Hatomico","smsType="+type);
	    			if ((type==0)) 
	    				alreadyEnding=false;
	    				readAndClose();
	    		}
	        	return true;
	            
	        case (MotionEvent.ACTION_CANCEL) :
	        	return false;
	            
	        case (MotionEvent.ACTION_OUTSIDE) :
	        	return false;
	                  
	        
	            
	    }   
	    return super.onTouchEvent(event);
	}
}
