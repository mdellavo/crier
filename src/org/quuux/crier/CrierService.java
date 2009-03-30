package org.quuux.crier;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;

import android.media.AudioManager;

import android.telephony.PhoneNumberUtils;
import android.provider.Contacts.Phones;
import android.database.Cursor;

import com.google.tts.TTS;

public class CrierService extends Service {
    private static final String TAG = "Crier";

    public static final int TEXT_NOTIFICATION    = 0x1;
    public static final int CALL_NOTIFICATION    = 0x2;
    public static final int OFFHOOK_NOTIFICATION = 0x3;

    private TTS     tts;
    private boolean initialized = false;
    private String  queued_message;

    private AudioManager audio_manager;

    public void onCreate() {
	super.onCreate();

	if(Config.LOGD)
	    Log.d(TAG, "CrierService::onCreate()");		

	TTS.InitListener init_listener = new TTS.InitListener() {
		public void onInit(int version) {
		    if(Config.LOGD)
			Log.d(TAG, "TTS initialized, version " + version);		
	
		    initialized = true;
		    if(queued_message != null) {
			if(Config.LOGD)
			    Log.d(TAG, "playing queued message");

 			speak(queued_message);
			queued_message = null;
		    }
		}
	    };

	tts = new TTS(this, init_listener, true);

	audio_manager = (AudioManager)getSystemService(AUDIO_SERVICE);
    }

    public void onDestroy() {
	super.onDestroy();

	if(Config.LOGD)
	    Log.d(TAG, "CrierService::onDestroy()");		

	if(tts != null && initialized)
	    tts.shutdown();
    }
  
    public void onStart(Intent intent, int start_id) {
	super.onStart(intent, start_id);
	
	if(Config.LOGD)
	    Log.d(TAG, "CrierService::onStart(start_id=" + start_id + ")");		

	int type = intent.getIntExtra("type", 0);

	if(type == TEXT_NOTIFICATION || type == CALL_NOTIFICATION) {
	    if(Config.LOGD)
		Log.d(TAG, "notifying");	

	    String message = buildMessage(intent);

	    if(Config.LOGD)
		Log.d(TAG, message);

	    speak(message);

	} else if(type == OFFHOOK_NOTIFICATION) {
	    if(Config.LOGD)
		Log.d(TAG, "silencing");		

	    silence();
	}
    }

    public IBinder onBind(Intent intent) {
	return null;
    }

    private String buildMessage(Intent intent) {
	int type = intent.getIntExtra("type", 0);

	String type_s   = type == TEXT_NOTIFICATION ? "text message" : "call";
	String address  = PhoneNumberUtils.formatNumber(intent.getStringExtra("address"));	
	String contact  = queryContacts(address);
	String location = queryLocation(address);
	
	String address_s;
	if(contact != null)
	    address_s = contact;
	else if(location != null)
	    address_s = location;
	else
	    address_s = address;
	
	return type_s + " from, " + address_s;
    }

    private void speak(String message) {
	if(tts != null && initialized) { 
	    if(audio_manager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
		if(Config.LOGD)
		    Log.d(TAG, "speaking: " + message);		

		tts.speak(message, 1, null);
	    }
	} else {
	    Log.d(TAG, "tts is null or not initialized, queuing message");
	    queued_message = message;
	}
    }

    private void silence() {
	if(tts != null && initialized && tts.isSpeaking())
	    tts.stop();
    }

    private String queryContacts(String address) {

	String[] cols = new String[] {
	    Phones.NAME,
	    Phones.DISPLAY_NAME,
	    Phones.PERSON_ID,
	};
	
	String[] args = new String[] {
	    PhoneNumberUtils.toCallerIDMinMatch(address) + '%'
	};
	
	if(Config.LOGD)
	    Log.d(TAG, "looking up " + PhoneNumberUtils.toCallerIDMinMatch(address));

	Cursor cursor = getContentResolver().query(Phones.CONTENT_URI, cols, Phones.NUMBER_KEY + " LIKE ?", args, null);
	
	if (cursor.moveToFirst()) {
	    int id_col           = cursor.getColumnIndex(Phones.PERSON_ID); 
	    int name_col         = cursor.getColumnIndex(Phones.NAME); 
	    int display_name_col = cursor.getColumnIndex(Phones.DISPLAY_NAME); 

	    do {		
		int id              = cursor.getInt(id_col);
		String name         = cursor.getString(name_col);
		String display_name = cursor.getString(display_name_col);
		
		if(Config.LOGD)
		    Log.d(TAG, "found person : " + id + ", " + name + ", " + display_name);

		return display_name;
		
	    } while (cursor.moveToNext());
	}

	return null;
    } 

    private String queryLocation(String address) {
	return null;
    }
}