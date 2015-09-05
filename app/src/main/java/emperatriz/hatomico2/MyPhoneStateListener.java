package emperatriz.hatomico2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MyPhoneStateListener extends PhoneStateListener {

	private AudioManager audioManager;
	private Context context;
	public MyPhoneStateListener (AudioManager audioManager, Context context){
		this.audioManager = audioManager;
		this.context = context;
	}
	
	@Override
	   public void onCallStateChanged(int state,String incomingNumber){
	        switch(state){
	        case TelephonyManager.CALL_STATE_IDLE:	        	
		         	Sys.init().setCallOngoing(false);
	              break;
	        case TelephonyManager.CALL_STATE_OFFHOOK:	 
	        		Sys.init().setCallOngoing(true);
	        			
	        			boolean speakerMax = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("callMax", true);
	        			if (speakerMax) {
	        				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
	        			}
	        			

	        		
	              break;
	        case TelephonyManager.CALL_STATE_RINGING:
	        	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        		Log.i("Hatomico","Incoming call: "+incomingNumber);
        		if ((!prefs.getBoolean("callWhiteList", false))||(prefs.getBoolean("callWhiteList", false) && Sys.init().isValid(incomingNumber))){
        			Log.i("Hatomico",incomingNumber+" is in list");
	        		context.startService(new Intent(context, AutoAnswerIntentService.class));
        		}else{
        			Log.i("Hatomico",incomingNumber+" not in list");
        		}
	              break;
	        default:
	           
	           break;
	        }
	

	}
}
