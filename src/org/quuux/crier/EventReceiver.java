package org.quuux.crier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.telephony.PhoneStateListener;
import android.telephony.gsm.SmsMessage;

import org.quuux.crier.CrierService;

public class EventReceiver extends BroadcastReceiver {
    private static final String TAG = "Crier";

    private static final String ACTION_PHONE_STATE  = "android.intent.action.PHONE_STATE";
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String PHONE_STATE_RINGING = "RINGING";

    public void onReceive(Context context, Intent intent) {
	String action = intent.getAction();
	Bundle extras = intent.getExtras();

	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

	if(Config.LOGD)
	    Log.d(TAG, "Event: " + intent.toString());

	if(preferences.getBoolean("phone_enabled", false) && action.compareTo(ACTION_PHONE_STATE) == 0)
	    onCall(context, extras);
	else if(preferences.getBoolean("text_enabled", false) && action.compareTo(ACTION_SMS_RECEIVED) == 0)
	    onText(context, extras);
    }

    private void onCall(Context context, Bundle extras) {
	String state           = extras.getString("state");
	String incoming_number = extras.getString("incoming_number");

	if(Config.LOGD) {
	    Log.d(TAG, "state: " + state);
	    Log.d(TAG, "incoming number: " + incoming_number);
	}

	if(state.compareTo(PHONE_STATE_RINGING) == 0) {
	    notify(context, CrierService.CALL_NOTIFICATION, incoming_number);
	} else {
	    notify(context, CrierService.OFFHOOK_NOTIFICATION, incoming_number);
	}
    }

    private void onText(Context context, Bundle extras) {
	Object[]     pdus     = (Object[])extras.get("pdus");
	SmsMessage[] messages = new SmsMessage[pdus.length];

	for (int i = 0; i<pdus.length; i++) {
	    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

	    String address = messages[i].getDisplayOriginatingAddress();
	    
	    if(Config.LOGD)
		Log.d(TAG, "incoming text address: " + address);		

	    notify(context, CrierService.TEXT_NOTIFICATION, address);
	}
    }

    private void onAlarm(Context context, Bundle extras) {

    }

    private void notify(Context context, int type, String address) {
	Intent intent = new Intent(context, CrierService.class);
	intent.putExtra("type", type);
	intent.putExtra("address", address);
	
	context.startService(intent);
    }
}