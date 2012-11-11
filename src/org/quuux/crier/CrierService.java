package org.quuux.crier;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Config;
import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.quuux.crier.R;
import org.quuux.crier.GeoLocator;
import org.quuux.crier.ContactLocator;

import android.speech.tts.TextToSpeech;

public class CrierService extends Service {
    private static final String TAG = "Crier";

    public static final int NOTIFICATION_TEXT    = 0x1;
    public static final int NOTIFICATION_CALL    = 0x2;
    public static final int NOTIFICATION_OFFHOOK = 0x3;
    public static final int NOTIFICATION_IDLE    = 0x4;
    public static final int NOTIFICATION_ALARM   = 0x5;

    public static final String ACTION_CALL_UPDATE = "org.quuux.crier.CALL_UPDATE";
    
    private boolean initialized = false;
    private boolean silenced    = false;

    private Handler        handler;
    private TextToSpeech   tts;
    private String         queued_message;
    private AudioManager   audio_manager;
    private GeoLocator     geo_locator;
    private ContactLocator contact_locator;
    
    public void onCreate() {
	super.onCreate();

	if(Config.LOGD)
	    Log.d(TAG, "CrierService::onCreate()");		

	TextToSpeech.OnInitListener init_listener = new TextToSpeech.OnInitListener() {
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

	handler         = new Handler();
	tts             = new TextToSpeech(this, init_listener);
        tts.setLanguage(Locale.US);
	geo_locator     = new GeoLocator();
	contact_locator = new ContactLocator(this);
	audio_manager   = (AudioManager)getSystemService(AUDIO_SERVICE);
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

	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

	int type = intent.getIntExtra("type", 0);

	String event   = null;
	String message = null;
	if(type == NOTIFICATION_TEXT) {
	    event   = "notification";
	    message = buildNotificationText(preferences.getString("text_format", getString(R.string.text_format_default)),
					    intent, preferences);

	} else if(type == NOTIFICATION_CALL) {
	    event   = "notification";
	    message = buildNotificationText(preferences.getString("phone_format", getString(R.string.phone_format_default)), 
					    intent, preferences);

            startIncomingCallActivity(intent);
    
	} else if(type == NOTIFICATION_OFFHOOK) {
	    event = "offhook";
	    silence();

            updateIncomingCallActivity(intent);

	} else if(type == NOTIFICATION_IDLE) { 
	    event = "idle";
	    unsilence();

            updateIncomingCallActivity(intent);

	} else if(type == NOTIFICATION_ALARM) {
	    event   = "alarm";
	    message = buildAlarmText(preferences.getString("time_format", getString(R.string.time_format_default)), 
				     intent, preferences);
	}
        
	if(Config.LOGD)
	    Log.d(TAG, "event: " + event);
        
	if(message != null)
	    speak(message);	    
    }
    
    public IBinder onBind(Intent intent) {
	return null;
    }
    
    private void startIncomingCallActivity(Intent intent) {
        Intent i = new Intent(this, IncomingCallActivity.class);
        i.putExtras(intent);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void updateIncomingCallActivity(Intent intent) {
        Intent i = new Intent();
        i.setAction(ACTION_CALL_UPDATE);
        i.putExtras(intent);
        sendBroadcast(i);
    }

    private String buildAlarmText(String format, Intent intent, SharedPreferences preferences) {
	Calendar calendar = Calendar.getInstance();

	int hour   = calendar.get(Calendar.HOUR);
	int minute = calendar.get(Calendar.MINUTE);
	boolean am = calendar.get(Calendar.AM_PM) == Calendar.AM;

	// Alarm bit
	//   Alarm mask is a bitfield for alarms to fire, each bit is
	//   marks the next half hour with bit 0 being midnight.
	long alarm_mask = Long.parseLong(preferences.getString("alarm_mask", "0"));
	long alarm_bit  = 1L << (((hour * 2) + (minute >= 30 ? 1 : 0)) + (!am ? 24 : 0));

	if(Config.LOGD)
	    Log.d(TAG, "alarm bit: " + String.format("0x012", alarm_bit));

	String rv = null;
	if((alarm_mask & alarm_bit) != 0 || intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0)==1) { 

	    if(hour == 0)
		hour = 12;

	    String time_s; 
	    if(hour == 12 && minute == 0 && am)
		time_s = "midnight";
	    else if(hour == 12 && minute == 0 && !am)
		time_s = "noon";
	    else if(minute == 0)
		time_s = String.format("%d %s", hour, am ? "AM" : "PM");
	    else
		time_s = String.format("%d %d %s", hour, minute, am ? "AM" : "PM");

	    rv = String.format(format, time_s);
	}

	return rv;
    }

    private String buildNotificationText(String format, Intent intent, SharedPreferences preferences) {
	String address   = intent.getStringExtra("address");	
	String address_s = contact_locator.locate(address);

	if(Config.LOGD && address_s != null)
	    Log.d(TAG, "contact: " + address_s);

	if(address_s == null) {
	    address_s = geo_locator.locate(address);

	    if(Config.LOGD && address_s != null)
		Log.d(TAG, "location: " + address_s);
	}

	return String.format(format, address_s);
    }

    private void speak(String message) {
	if(tts != null && initialized) { 
	    if(!isSilenced()) {
		if(Config.LOGD)
		    Log.d(TAG, "speaking: " + message);		
	    
		tts.speak(message, 1, null);
	    } else {
		if(Config.LOGD)
		    Log.d(TAG, "muted: " + message);
	    }
	} else {
	    Log.d(TAG, "tts is null or not initialized, queuing message");
	    queued_message = message;
	}
    }

    private boolean isSilenced() {
	return silenced || audio_manager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
    }

    private void silence() {
	if(Config.LOGD)
	    Log.d(TAG, "silencing");		

	if(tts != null && initialized && tts.isSpeaking())
	    tts.stop();
	
	silenced = true;
    }

    private void unsilence() {
	if(Config.LOGD)
	    Log.d(TAG, "unsilencing");		
	
	silenced = false;
    }
}
