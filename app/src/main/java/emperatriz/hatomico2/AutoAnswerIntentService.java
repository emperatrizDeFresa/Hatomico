package emperatriz.hatomico2;


import java.io.IOException;
import java.lang.reflect.Method;
//import emperatriz.hatomico2.ITelephony;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

public class AutoAnswerIntentService extends IntentService {

        public AutoAnswerIntentService() {
                super("AutoAnswerIntentService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
                Context context = getBaseContext();

                try {
                        if (!PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("autoAnswer", false)){
                                Process p = Runtime.getRuntime().exec("su");
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }

                // Load preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                if ( (prefs.getBoolean("autoAnswer", false)) && (!Sys.init().isCallOngoing()) ) {
                 
	                // Let the phone ring for a set delay
	                try {
	                        Thread.sleep(Integer.parseInt(prefs.getString("answerDelay", "4")) * 1000);
	                } catch (InterruptedException e) {
	                        // We don't really care
	                }
	
	
	
	                // Make sure the phone is still ringing
	                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	                if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
	                        return;
	                }
	
	                // Answer the phone
//	                try {
//	                        answerPhoneAidl(context);
//	                }
//	                catch (Exception e) {
//	                        e.printStackTrace();
	                        answerPhoneHeadsethook(context);
//	                }
                }
               
                //Sys.init().setSpeakerState();
                
                return;
               
        }

        

        private void answerPhoneHeadsethook(Context context) {
                // Simulate a press of the headset button to pick up the call
                Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);            
                buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
                context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

                // froyo and beyond trigger on buttonUp instead of buttonDown
                Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);              
                buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
                context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
        }

        @SuppressWarnings("unchecked")
        private void answerPhoneAidl(Context context) throws Exception {
                // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                Class c = Class.forName(tm.getClass().getName());
                Method m = c.getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                com.android.internal.telephony.ITelephony telephonyService;
                telephonyService = (com.android.internal.telephony.ITelephony)m.invoke(tm);

                // Silence the ringer and answer the call!
                telephonyService.silenceRinger();
                telephonyService.answerRingingCall();
        }
}


