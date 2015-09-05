package emperatriz.hatomico2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class Service extends android.app.Service {

	private static final int NOTIFY_ID = 181818;
	
	public static boolean running=false;
	
	private NotificationManager mNotificationManager;
	private TelephonyManager telephony;
	private MyPhoneStateListener phoneListener;
	private Handler smsHandler;
	private PowerManager.WakeLock wl;
	private Sys sys;
	private Runnable r;
    SMSListener smsListener;
	
	// This is the old onStart method that will be called on the pre-2.0
	// platform.  On 2.0 or later we override onStartCommand() so this
	// method will not be called.
	@Override
	public void onStart(Intent intent, int startId) {
	   
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    
		final int myID=23523; 
		
        sys = Sys.init();
        
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Hatomico");
		wl.acquire();
        
        int icon = (Build.VERSION.SDK_INT<11)? R.drawable.icon2 : R.drawable.icon_ics;
        CharSequence tickerText = getString(R.string.activated);
        long when = System.currentTimeMillis();

//        Notification notification = new Notification(icon, tickerText, when);
//        notification.flags |= Notification.FLAG_ONGOING_EVENT;
//        notification.flags |= Notification.FLAG_NO_CLEAR;
        
        

        
        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.activated);
        CharSequence contentText = getString(R.string.pressOptions); 
        Intent notificationIntent = new Intent(this, Options.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

//        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        
        Notification notif = new Notification.Builder(this)
        .setContentTitle("HATOMICO")
        .setContentText(contentText)
        .setSmallIcon(icon)
        .setTicker(tickerText)
        .setOngoing(true)
        .setWhen(when)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.icony))  
        .setContentIntent(contentIntent)
        .build();
        
        running = true;
        
        
        startService(new Intent(this, NotificationService.class));
        
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        audiomanager.setSpeakerphoneOn(true);
        
        phoneListener = new MyPhoneStateListener(audiomanager, getApplicationContext());
        telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
     
        
        
        smsHandler=new Handler();

        if (r==null) r = new Runnable()
        {
            public void run() 
            {
                checkSms();
                if (running) smsHandler.postDelayed(this, 3000);
            }
        };

        smsHandler.postDelayed(r, 3000);
		startForeground(myID, notif);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean smsOn = prefs.getBoolean("smsOn", false);
        if (smsOn){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

            smsListener = new SMSListener();
            this.registerReceiver(smsListener, intentFilter);


            //audiomanager.startBluetoothSco();
       }


	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return Service.START_STICKY;
	}
	
	
	
//	 @Override
//	    public void onCreate() {
//	        super.onCreate();
//	        
//
//
//	    }
	
	
	 private void checkSms(){

//		 Log.i("Hatomico","CheckingSms "+Sys.init().smsSize());
			if (!Sys.init().isCallOngoing()&&!Sys.init().isTreatingSms()){ 
				emperatriz.hatomico2.Notification normalNotification = Sys.init().getSms();
				if ((normalNotification !=null)&&(Sys.init().isSco())){
					Sys.init().currentNormalNotification = normalNotification;
					Intent readIntent = new Intent(getBaseContext(), ReadingSMS.class);
					readIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getBaseContext().startActivity(readIntent);
				}
			}

	 }
	 
	 @Override
	    public void onDestroy() {
		 super.onDestroy();
		 
		 telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
		 
		 smsHandler.removeCallbacks(r);
		 
		 running = false;
		 
		 mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		 
		 mNotificationManager.cancelAll();

         AudioManager audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         //audiomanager.stopBluetoothSco();
         audiomanager.setMode(AudioManager.MODE_NORMAL);
         Sys.init().setSco(false);

         if (smsListener!=null) this.unregisterReceiver(smsListener);

		 stopService(new Intent(this, NotificationService.class));
		 
			if ((wl!=null)&&(wl.isHeld())) wl.release();
	 }
	 
	 
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
