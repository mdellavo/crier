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

import java.util.Calendar;
import java.util.List;

import org.quuux.crier.R;
import org.quuux.crier.AreaCodeLocator;
import org.quuux.crier.ContactLocator;

import com.google.tts.TTS;

public class CrierService extends Service {
    private static final String TAG = "Crier";

    public static final int NOTIFICATION_TEXT    = 0x1;
    public static final int NOTIFICATION_CALL    = 0x2;
    public static final int NOTIFICATION_OFFHOOK = 0x3;
    public static final int NOTIFICATION_IDLE    = 0x4;
    public static final int NOTIFICATION_ALARM   = 0x5;

    private boolean initialized = false;
    private boolean silenced    = false;

    private Handler        handler;
    private TTS            tts;
    private String         queued_message;
    private AudioManager   audio_manager;
    private GeoLocator     geo_locator;
    private ContactLocator contact_locator;
    
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

	handler         = new Handler();
	//tts             = new TTS(this, init_listener, true);
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
	} else if(type == NOTIFICATION_OFFHOOK) {
	    event = "offhook";
	    silence();

	} else if(type == NOTIFICATION_IDLE) { 
	    event = "idle";
	    unsilence();

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

    private String buildAlarmText(String format, Intent intent, SharedPreferences preferences) {
	Calendar calendar = Calendar.getInstance();

	int hour   = calendar.get(Calendar.HOUR);
	int minute = calendar.get(Calendar.MINUTE);
	boolean am = calendar.get(Calendar.AM_PM) == Calendar.AM;

	// Alarm bit
	//   Alarm mask is a bitfield for alarms to fire, each bit is
	//   marks the next half hour with bit 0 being midnight.
	long alarm_mask = preferences.getLong("alarm_mask", 0x0L);

	if(Config.LOGD)
	    Log.d(TAG, "alarm bit: " + (((hour * 2) + (minute >= 30 ? 1 : 0)) + (!am ? 24 : 0)));

	long alarm_bit = 1 << (((hour * 2) + (minute >= 30 ? 1 : 0)) + (!am ? 24 : 0));

	// When the alarm is installed it will have a count of past
	// alarms (>1) and we allow this to trigger
	String rv = null;
	if((alarm_mask & alarm_bit)>0 || intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0)>1) { 

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