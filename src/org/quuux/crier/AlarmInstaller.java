package org.quuux.crier;

import android.content.Context;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Config;
import android.util.Log;

import org.quuux.crier.CrierService;

public class AlarmInstaller {
    private static final String TAG       = "Crier";
    private static final int ALARM_PERIOD = 1800000;

    public static PendingIntent buildIntent(Context context) {
	Intent intent = new Intent(context, CrierService.class);
	intent.putExtra("type", CrierService.NOTIFICATION_ALARM);
	return PendingIntent.getService(context, 0, intent, 0);
    }

    public static void install(Context context) {

	if(Config.LOGD)
	    Log.d(TAG, "Installing alarm");

	AlarmManager alarm_manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);	
	alarm_manager.setRepeating(AlarmManager.RTC, 0, ALARM_PERIOD, buildIntent(context));
    }

    public static void cancel(Context context) {

	if(Config.LOGD)
	    Log.d(TAG, "Cancelling alarm");

	AlarmManager alarm_manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	alarm_manager.cancel(buildIntent(context));	
    }
}