package emperatriz.hatomico2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.util.Log;

public class Sys {

	public static int SCREEN_TIMEOUT = 15000;
    public static int SMS_CACHE = 1000;
	
	private Sys(){
		callOngoing=false;
		treatingSms=false;
		notificationQueue = new LinkedList<Notification>();
		notificationCache = new LinkedList<Notification>();
		
	}
	
	private static Sys instance; 
	
	public static Sys init(){
		if (instance==null)
			instance = new Sys();
		return instance;
	}
	
	private boolean callOngoing;

    public boolean isSco() {
        return true||sco;
    }

    public void setSco(boolean sco) {
        this.sco = sco;
    }

    private boolean sco=false;

	private boolean treatingSms;
	
	private boolean activated;
	
	private SensorEventListener sListener;
	
	private SensorManager sensorListener;
	
	private Context context;
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
		DB.init(context);
		purgeSms();
	}

	public Intent getSvc() {
		return svc;
	}

	public void setSvc(Intent svc) { 
		this.svc = svc;
	}

	private Intent svc;
	
	public Notification currentNormalNotification;
	
	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activaded) {
		this.activated = activaded;
        boolean autoBTOn = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("switchOnBt", false);
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        if (autoBTOn){
            if (activated){

                if (bta==null){

                }
                else if (!bta.isEnabled()){
                    bta.enable();
                }
            }
            else{

                if (bta==null){

                }
                else if (bta.isEnabled()){
                    bta.disable();
                }
            }
        }
	}

	private Queue<Notification> notificationQueue;
	private Queue<Notification> notificationCache; // Cola de mensajes para eliminar duplicados raros en twitter

	public boolean isCallOngoing() {
		return callOngoing;
	}

	public void setCallOngoing(boolean callOngoing) {
		Log.i("Hatomico","CallOngoing "+callOngoing);
		this.callOngoing = callOngoing;
		setSpeakerState();
	}

	public boolean isTreatingSms() {
		return treatingSms;
	}

	public void setTreatingSms(boolean treatingSms) {
		Log.i("Hatomico","TreatingSms "+treatingSms);
		this.treatingSms = treatingSms;
	}
	
	public Notification getSms(){
		return notificationQueue.poll();
	}
	
	public void addSms(Notification normalNotification){
        Log.i("HATOMIC0","Adding -> "+ normalNotification.getSender()+" | "+ normalNotification.getText());
        if (!notificationCache.contains(normalNotification)&&(!checkInfoNotification(normalNotification))){
            Log.i("HATOMIC0","Added -> "+ normalNotification.getSender()+" | "+ normalNotification.getText());
            notificationQueue.add(normalNotification);
            cache(normalNotification);

            if (notificationCache.size()>=SMS_CACHE) notificationCache.poll();
        }

	}

    public void cache(Notification normalNotification){
        notificationCache.add(normalNotification);
        Log.i("HATOMIC0","Cached -> "+ normalNotification.toString());
        if (normalNotification instanceof WhatsappNotification){
            notificationCache.add(((WhatsappNotification)normalNotification).toGroupForm());
            Log.i("HATOMIC0","Cached -> "+ ((WhatsappNotification)normalNotification).toGroupForm().toString());
        }


        if (notificationCache.size()>=SMS_CACHE) notificationCache.poll();
    }

    public int whatsappCount;

	public void purgeSms(){
		notificationQueue.clear();
	}

    private boolean checkInfoNotification(Notification normalNotification){ // Comprobamos si es una notificacioń resumen: '5 mensajes nuevos'
        boolean esp = (normalNotification.getText().toUpperCase().contains("NUEVO"))&&(normalNotification.getText().toUpperCase().contains("MENSAJE"));
        boolean eng = (normalNotification.getText().toUpperCase().contains("NEW"))&&(normalNotification.getText().toUpperCase().contains("MESSAGE"));

        return esp||eng;
    }
	
	public int smsSize(){
		return notificationQueue.size();
	}
	


	public void setMovementListener(SensorEventListener sListener) {
		this.sListener = sListener;
	}

	public SensorEventListener getMovementListener() {
		return sListener;
	}

	public void setSensorListener(SensorManager sensorListener) {
		this.sensorListener = sensorListener;
	}

	public SensorManager getSensorListener() {  
		return sensorListener; 
	}

    public String checkNumber(String text, Context ctx){
		String ret=text;
       try{
           Double.parseDouble(text.trim().replace(" ","").replace("+","").replace("(","").replace(")",""));
		   ret = ctx.getString(R.string.unknown);
		   String at = " "+ctx.getString(R.string.at)+" ";
		   Double.parseDouble(text.split(at)[0].trim().replace(" ","").replace("+","").replace("(","").replace(")",""));
           ret =  ctx.getString(R.string.unknown)+" "+ctx.getString(R.string.at)+" "+text.split(at)[1];
       }catch (Exception ex){

       }
		return ret;
    }
	

	public void switchOff(){ 
		try{
			AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	    	audiomanager.setSpeakerphoneOn(false);
	    	context.stopService(svc);
	    	setActivated(false);
	    	getSensorListener().unregisterListener(getMovementListener());
	    	boolean screenOff = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("screenOff", true);
        	if (screenOff){
        		Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, retrieveScreenTimeout(context));
        	}
	    	
		}catch (Exception ex){}
	}
	
	public void setSpeakerState(){
		try{
			boolean speaker = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("speaker", true);		
			AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	    	audiomanager.setSpeakerphoneOn(speaker);
		}catch (Exception ex){}
    	
	}
	
	public void setWhiteList(long[] ids){
		DB.init().createNewWhiteList(ids);
	}
	
	public long[] getWhiteList(){
		return DB.init().getWhiteList(); 
		
	}

	public static void infoDialog(String text, Context ctx) {
		AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
		alertDialog.setMessage(text);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});
		alertDialog.show();
	}

	public boolean isSenderAllowed(String sender, Context ctx){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);		
		String bList = prefs.getString("notificationsBlackList", "");
		
		if (bList.trim().length()==0) return true;
		
		String[] blacklist = bList.split(",");
		
		for (int i=0; i<blacklist.length;i++){
			if (sender.toUpperCase().contains(blacklist[i].trim().toUpperCase())) return false;
		}
		return true;
		
	}
	
	private boolean isInWhiteList(long id){
		long[] list = getWhiteList();
		for (int i=0; i<list.length;i++){
			if (list[i]==id) return true;
		}
		return false;
	}
	
	private String getPhoneNumbers(String id) 
	{
	    ArrayList<String> phones = new ArrayList<String>();

	    Cursor cursor = context.getContentResolver().query(
	            CommonDataKinds.Phone.CONTENT_URI, 
	            null, 
	            CommonDataKinds.Phone.CONTACT_ID +" = ?", 
	            new String[]{id}, null);


	    String ret="";
	    while (cursor.moveToNext()) 
	    {
	        ret += cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
	    } 

	    cursor.close();
	    return(ret);
		//return "";
	}

	public boolean isValid(String phoneNumber){
		if (phoneNumber.trim().length()==0) return false;
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[] { ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = null;
        long id=-1;
		Cursor c = context.getContentResolver().query(uri, projection, selection, selectionArgs,sortOrder);
		if (c.moveToFirst()){
			do{
				id = c.getLong(c.getColumnIndex(ContactsContract.Contacts._ID));
			}
			while (c.moveToNext());
			return isInWhiteList(id);
		}
		
		return false;
	}
	
	public Cursor getAllContacts(){
		try{
	        Uri uri = ContactsContract.Contacts.CONTENT_URI;
	        String[] projection = new String[] { ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME };
	        String selection = Contacts.IN_VISIBLE_GROUP + " = 1 AND "+Contacts.HAS_PHONE_NUMBER+"= 1";
	        String[] selectionArgs = null;
	        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME+ " COLLATE LOCALIZED ASC";
	
	        return context.getContentResolver().query(uri, projection, selection, selectionArgs,sortOrder);
		} catch (Exception ex){
			return null;
		}

	}
	
	public void saveScreenTimeout(int millis, Context ctx) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		editor.putInt("screenTimeout", millis);
		editor.commit();
	}

	public int retrieveScreenTimeout(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getInt("screenTimeout", 120000);
	}
	
	
	public void saveBluetoothMac(ArrayList<String> names, Context ctx) {
		Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit(); 
		String namesS="";
		for(String name:names){
			namesS+=name+"%";
		}
		if (namesS.length()>0) namesS = namesS.substring(0, namesS.length()-1);
		editor.putString("bluetoothMac", namesS);
		editor.commit();
	}

	public String[] retrieveBluetoothMac(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString("bluetoothMac", "").split("%");
	}
	
	public boolean isDeviceBound(BluetoothDevice device, Context ctx){
		String[] macs = retrieveBluetoothMac(ctx);
		for (String mac: macs){
			if (mac.equals(device.getAddress())) return true;
		}
		return false;
	}
	
	public int whatsappCounter=0;
	
	public String translateEmoji(String text){
		
		boolean esp = Locale.getDefault().getDisplayLanguage().toUpperCase().equals("ESPAÑOL");
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		//                      http://apps.timwhitlock.info/emoji/tables/unicode                         //
		////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		// 1. Emoticons ( 1F601 - 1F64F )
		text = text.replaceAll("😁", esp?"risa ":"smile ");
		text = text.replaceAll("😂", esp?"llorar de risa ":"tears of joy ");
		text = text.replaceAll("😃", esp?"sonrisa ":"smile ");
		text = text.replaceAll("😄", esp?"sonrisa ":"smile ");
		text = text.replaceAll("😅", esp?"sonrisa con gota de sudor ":"smile with cold sweat ");
		text = text.replaceAll("😆", esp?"risa ":"smile ");
		text = text.replaceAll("😉", esp?"guiño de ojo ":"wink ");
		text = text.replaceAll("😊", esp?"sonrojado ":"smile ");
		text = text.replaceAll("😋", esp?"rico ":"tasty ");
		text = text.replaceAll("😌", esp?"aliviado ":"relieved ");
		text = text.replaceAll("😍", esp?"ojos corazón ":"heart eyed ");
		text = text.replaceAll("😏", esp?"pícaro ":"smirking face ");
		text = text.replaceAll("😒", esp?"impasible ":"unamused ");
		text = text.replaceAll("😓", esp?"sudor frío ":"face with cold sweat ");
		text = text.replaceAll("😔", esp?"triste ":"pensive ");
		text = text.replaceAll("😖", esp?"aturdido ":"confounded ");
		text = text.replaceAll("😘", esp?"beso ":"kiss ");
		text = text.replaceAll("😚", esp?"beso ":"kiss ");
		text = text.replaceAll("😜", esp?"cara divertida ":"funny face ");
		text = text.replaceAll("😝", esp?"cara divertida ":"funny face ");
		text = text.replaceAll("😞", esp?"decepción ":"dissapointed ");
		text = text.replaceAll("😠", esp?"enfadado ":"angry ");
		text = text.replaceAll("😡", esp?"enfadado ":"angry ");
		text = text.replaceAll("😢", esp?"llorar ":"crying ");
		text = text.replaceAll("😣", esp?"aturdido ":"confounded ");
		text = text.replaceAll("😤", esp?"triunfador ":"triumph ");
		text = text.replaceAll("😥", esp?"sudor ":"sweat ");
		text = text.replaceAll("😨", esp?"miedo ":"fear ");
		text = text.replaceAll("😩", esp?"abatido ":"weary ");
		text = text.replaceAll("😪", esp?"sueño ":"sleepy ");
		text = text.replaceAll("😫", esp?"cansado ":"tired ");
		text = text.replaceAll("😭", esp?"llorar ":"crying ");
		text = text.replaceAll("😰", esp?"miedo ":"fear ");
		text = text.replaceAll("😱", esp?"miedo ":"fear ");
		text = text.replaceAll("😲", esp?"sorprendido ":"astonished ");
		text = text.replaceAll("😳", esp?"sorprendido ":"astonished ");
		text = text.replaceAll("😵", esp?"sorprendido ":"astonished ");
		text = text.replaceAll("😷", esp?"mascarilla ":"medical mask ");
		text = text.replaceAll("😸", esp?"risa ":"smile ");
		text = text.replaceAll("😹", esp?"llorar de risa ":"tears of joy ");
		text = text.replaceAll("😺", esp?"sonrisa ":"smile ");
		text = text.replaceAll("😻", esp?"ojos corazón ":"heart eyed ");
		text = text.replaceAll("😼", esp?"pícaro ":"smirking face ");
		text = text.replaceAll("😽", esp?"beso ":"kiss ");
		text = text.replaceAll("😾", esp?"enfadado ":"angry ");
		text = text.replaceAll("😿", esp?"llorar ":"crying ");
		text = text.replaceAll("🙀", esp?"miedo ":"fear ");
		text = text.replaceAll("🙈", esp?"mono ciego ":"see-no-evil monkey ");
		text = text.replaceAll("🙉", esp?"mono sordo ":"hear-no-evil monkey ");
		text = text.replaceAll("🙊", esp?"mono mudo ":"speak-no-evil monkey ");
		
		// 2. Dingbats ( 2702 - 27B0 )
		text = text.replaceAll("✊", esp?"puño ":"fist ");
		text = text.replaceAll("✋", esp?"mano ":"hand ");
		text = text.replaceAll("✌", esp?"victoria ":"victory ");
		text = text.replaceAll("❤", esp?"corazón ":"heart ");
		
		// 5. Uncategorized
		text = text.replaceAll("☺", esp?"sonrojado ":"smile ");
		text = text.replaceAll("👏", esp?"aplauso ":"clapping ");
		text = text.replaceAll("👏", esp?"aplauso ":"clapping ");
		text = text.replaceAll("👻", esp?"fantasma ":"ghost ");
		text = text.replaceAll("👼", esp?"angel ":"angel ");
		text = text.replaceAll("👽", esp?"alien ":"alien ");
		text = text.replaceAll("👿", esp?"malo ":"evil ");
		text = text.replaceAll("💀", esp?"calavera ":"skull ");
		text = text.replaceAll("💃", esp?"bailarina ":"dancer ");
		text = text.replaceAll("💋", esp?"beso ":"kiss ");
		text = text.replaceAll("💏", esp?"beso ":"kiss ");
		text = text.replaceAll("💑", esp?"amor ":"love ");
		text = text.replaceAll("💩", esp?"caca ":"poo ");
		text = text.replaceAll("💪", esp?"fuerte ":"strong ");
		text = text.replaceAll("💰", esp?"dinero ":"money ");
		text = text.replaceAll("💲", esp?"dinero ":"money ");
		text = text.replaceAll("💴", esp?"dinero ":"money ");
		text = text.replaceAll("💵", esp?"dinero ":"money ");
		text = text.replaceAll("👊", esp?"puño ":"fist ");
		text = text.replaceAll("🔥", esp?"fuego ":"fire ");
		text = text.replaceAll("🔫", esp?"pistola ":"pistol ");
		text = text.replaceAll("🔪", esp?"cuchillo ":"knife ");
		text = text.replaceAll("👆", esp?"apuntar hacia arriba ":"pointing upwards ");
		text = text.replaceAll("👇", esp?"apuntar hacia abajo ":"pointing downwards ");
		text = text.replaceAll("👈", esp?"apuntar hacia la izquierda ":"pointing left ");
		text = text.replaceAll("👉", esp?"apuntar hacia la derecha ":"pointing right ");
		text = text.replaceAll("👋", esp?"adiós ":"waving ");
		text = text.replaceAll("👌", esp?"oquéi ":"ok ");
		text = text.replaceAll("👍", esp?"oquéi ":"thumbs up ");
		text = text.replaceAll("👎", esp?"negativo ":"thumbs down ");
		text = text.replaceAll("👏", esp?"aplauso ":"clapping ");
		text = text.replaceAll("👐", esp?"manos abiertas ":"open hands ");

		
		// 6a. Additional emoticons ( 1F600 - 1F636 )
		text = text.replaceAll("😀", esp?"sonrisa ":"smile ");
		text = text.replaceAll("😇", esp?"santo ":"saint ");
		text = text.replaceAll("😈", esp?"malo ":"evil ");
		text = text.replaceAll("😎", esp?"gafas de sol ":"sunglasses ");
		text = text.replaceAll("😐", esp?"impasible ":"neutral ");
		text = text.replaceAll("😑", esp?"impasible ":"neutral ");
		text = text.replaceAll("😕", esp?"confundido ":"confused ");
		text = text.replaceAll("😗", esp?"beso ":"kiss ");
		text = text.replaceAll("😙", esp?"beso ":"kiss ");
		text = text.replaceAll("😛", esp?"echar la lengua ":" stuck-out tongue ");
		text = text.replaceAll("😟", esp?"preocupado ":"worried ");
		text = text.replaceAll("😦", esp?"sorprendido ":"surprised ");
		text = text.replaceAll("😧", esp?"sorprendido ":"surprised ");
		text = text.replaceAll("😬", esp?"risa ":"smile ");
		text = text.replaceAll("😮", esp?"sorprendido ":"surprised ");
		text = text.replaceAll("😯", esp?"sorprendido ":"surprised ");
		text = text.replaceAll("😴", esp?"dormido ":"sleeping ");
		
		// 6c. Other additional symbols ( 1F30D - 1F567 )
		text = text.replaceAll("💶", esp?"dinero ":"money ");
		text = text.replaceAll("💷", esp?"dinero ":"money ");


		
		return text;
	}
	
	public boolean isKnown(String number){
		try{
			Long.parseLong(number.replace("+", "").replace(" ", ""));
		}
		catch (Exception ex){
			return true;
		}
		return false;
		
	}
	

	
}
