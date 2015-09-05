package emperatriz.hatomico2;

import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {

	static String HANGOUTS = "com.google.android.talk";
	static String WHATSAPP = "com.whatsapp";
	static String GOOGLE_NOW = "com.google.android.googlequicksearchbox";
	static String TELEGRAM = "org.telegram.messenger";
	static String TWITTER = "com.twitter.android";
	static String GMAIL = "com.google.android.gm ---> no está funcionando así que lo desactivo";
	static String LINE = "jp.naver.line.android";




	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
//		try {
			if (Sys.init().isActivated()) {
				Notification mNotification = sbn.getNotification(); 
				

				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				
				Set<String> smsApps = prefs.getStringSet("smsApps", null);
				
				
				
				if (mNotification != null) {
					String packageName = sbn.getPackageName();

					String app = HANGOUTS;///////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("Hangouts")&&packageName.equalsIgnoreCase(app)) {
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON));
						String notificationText = "";
						CharSequence[] csa;
						if (extras.getCharSequence(Notification.EXTRA_TEXT)!=null){
							notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
							if (notificationText.contains("\n")) {
								String[] notifications = notificationText.split("\n");
								notificationText = notifications[notifications.length-1];
							}
						}
						

						if (Sys.init().isSenderAllowed(notificationTitle, this)) {
							Sys.init().addSms(new NormalNotification(notificationTitle, notificationText, notificationLargeIcon, "Hangouts",getApplicationContext()));
//							NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//						    notificationManager.cancel(sbn.getId());
						}
							
					}
					
					app = GOOGLE_NOW;///////////////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("GoogleNow")&&packageName.equalsIgnoreCase(app)) {
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.googlenow);
						String notificationText = "";
						CharSequence[] csa;
						if (extras.getCharSequence(Notification.EXTRA_TEXT)!=null){
							notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString(); 
//							if (notificationText.contains("\n")) {
//								String[] notifications = notificationText.split("\n");
//								notificationText = notifications[notifications.length-1];
//							}
						}
						
						notificationTitle = notificationTitle.replace("°", getString(R.string.degrees));
						
						//if (Sys.init().isSenderAllowed(notificationTitle, this)) En google now no se filtra por remitente porque no tiene sentido
							Sys.init().addSms(new NormalNotification("Google now", notificationTitle, notificationLargeIcon, "Google now",getApplicationContext()));
//							NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//						    notificationManager.cancel(sbn.getId());
					}

					app = WHATSAPP;///////////////////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("Whatsapp")&&packageName.equalsIgnoreCase(app)) {
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON));
						String notificationText = "";
                        String group = null;


						if (extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)==null){

							notificationText=extras.getCharSequence(Notification.EXTRA_TEXT).toString();
                            if (Sys.init().isSenderAllowed(notificationTitle, this)) 	{
                                if (notificationText.contains(" @ ")){
                                    notificationText = notificationText.split(" @ ")[0];
                                    group = notificationText.split(" @ ")[1];
                                }
                                if (notificationTitle.contains(" @ ")){
                                    Sys.init().addSms(new WhatsappNotification(notificationTitle.split(" @ ")[0], notificationTitle.split(" @ ")[1], notificationText, notificationLargeIcon, getApplicationContext()));
                                }
                                else{
                                    Sys.init().addSms(new WhatsappNotification(notificationTitle, group, notificationText, notificationLargeIcon, getApplicationContext()));
                                }


                                if (notificationText.contains(": ")){
                                    WhatsappNotification whatsappNotification2 = new WhatsappNotification(notificationText.split(": ")[0], notificationTitle, notificationText.split(": ")[1],null,getApplicationContext());
                                    Sys.init().cache(whatsappNotification2);

                                }
                            }
						}
						else{
                            if (notificationTitle.equalsIgnoreCase("Whatsapp")){
                                CharSequence[] csa = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);


                                for (CharSequence cs : csa){

                                        notificationText = cs.toString();


                                        notificationTitle= notificationText.split(": ")[0];
                                        notificationText = notificationText.split(": ")[1];

                                        notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.whatsapp);

                                        if (Sys.init().isSenderAllowed(notificationTitle, this)){
                                            String[] parts = notificationTitle.split(" @ " );
                                            Sys.init().addSms(new WhatsappNotification(parts[0], parts.length==2?parts[1]:null, notificationText, notificationLargeIcon, getApplicationContext()));
                                        }
                                    }





                            }
                            else{
                                CharSequence[] csa = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);



                                for (CharSequence cs : csa){
                                    notificationText = cs.toString();

                                    if (notificationLargeIcon==null){
                                        notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.whatsapp);
                                    }

                                    if (Sys.init().isSenderAllowed(notificationTitle, this)){

                                        if (notificationText.contains(": ")){
                                            WhatsappNotification normalNotification2 = new WhatsappNotification(notificationText.split(": ")[0],notificationTitle, notificationText.split(": ")[1],notificationLargeIcon,getApplicationContext());
                                            Sys.init().cache(normalNotification2);
                                        }
                                        String[] parts = notificationTitle.split(" @ ");
                                        Sys.init().addSms(new WhatsappNotification(notificationTitle.split(" @ ")[0], parts.length==2?parts[1]:null,notificationText, notificationLargeIcon,getApplicationContext()));



                                    }

                                }
                            }

						}

							
					}
					
					app = TELEGRAM;///////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("Telegram")&&packageName.equalsIgnoreCase(app)) {
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON)); 
						String notificationText = "";
						if (extras.getCharSequence(Notification.EXTRA_TEXT)!=null){
							notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
							if (notificationText.contains(": ")){
								notificationTitle= notificationText.split(": ")[0].replace("@", getString(R.string.at));
								notificationText= notificationText.split(": ")[1];
							}
							
						}
						else{
							CharSequence[] csa = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
							notificationText = csa[0].toString();
						}
						
						if (notificationTitle.toUpperCase().equals("TELEGRAM")){ //Varios mensajes de varios remitentes
							notificationTitle= notificationText.split(": ")[0].replace("@", getString(R.string.at));
							notificationText = notificationText.substring(notificationText.indexOf(": "));
							notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.telegram);
						}
						
						if (notificationLargeIcon==null){
							notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.telegram);
						}
						
						if (Sys.init().isSenderAllowed(notificationTitle, this)) {
							Sys.init().addSms(new NormalNotification(notificationTitle, notificationText, notificationLargeIcon, "Telegram",getApplicationContext()));
//							NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//						    notificationManager.cancel(sbn.getId());
						}
							
					}
					
					app = TWITTER;///////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("Twitter")&&packageName.equalsIgnoreCase(app)) {
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON)); 
						String notificationText = "";
						CharSequence[] csa;
						if (extras.getCharSequence(Notification.EXTRA_TEXT)!=null){
							notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
//							notificationTitle= notificationText.split(":")[0].replace("@", getString(R.string.at));
//							notificationText= notificationText.split(":")[1];
						}
						
						if (notificationLargeIcon==null){
							notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.twitter);
						}
						
						if (Sys.init().isSenderAllowed(notificationTitle, this)) {
							NormalNotification normalNotification = new NormalNotification(notificationTitle, notificationText, notificationLargeIcon, "Twitter",getApplicationContext());

                            Sys.init().addSms(normalNotification);
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(sbn.getId());

						}
							
					}
					
					app = GMAIL;///////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("Gmail")&&packageName.equalsIgnoreCase(app)) { 
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON)); 
						String notificationText = "";
						CharSequence[] csa;
						if (extras.getCharSequence(Notification.EXTRA_TEXT)!=null){
							notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
						}
						
						if (notificationLargeIcon==null){
							notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.gmail);
						}
						
						if (Sys.init().isSenderAllowed(notificationTitle, this)) {
								Sys.init().addSms(new NormalNotification(notificationTitle, notificationText, notificationLargeIcon, "Gmail",getApplicationContext()));
								NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
							    notificationManager.cancel(sbn.getId());
						}
						
						
							
					}
					
					app = LINE;///////////////////////////////////////////////////////////////////////////////////////////////////
					if (smsApps.contains("LINE")&&packageName.equalsIgnoreCase(app)) {
						Bundle extras = mNotification.extras;

						String notificationTitle = extras.getString(Notification.EXTRA_TITLE);
						Bitmap notificationLargeIcon = ((Bitmap) extras.getParcelable(Notification.EXTRA_LARGE_ICON)); 
						String notificationText = "";
						CharSequence[] csa;
						if (extras.getCharSequence(Notification.EXTRA_TEXT)!=null){
							notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
							notificationTitle= notificationText.split(":")[0].replace("@", getString(R.string.at));
							notificationText= notificationText.split(":")[1];
						}
						
						if (notificationLargeIcon==null){
							notificationLargeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.telegram);
						}
						
						if (Sys.init().isSenderAllowed(notificationTitle, this)) {
							Sys.init().addSms(new NormalNotification(notificationTitle, notificationText, notificationLargeIcon, "LINE",getApplicationContext()));
//							NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//						    notificationManager.cancel(sbn.getId());
						}
							
					}
					
					


				}
			}
//		} catch (Exception ex) {
//            for (StackTraceElement st : ex.getStackTrace())
//			Log.i("Hatomico","Error: "+st.toString());
//		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {

	}

}
