package org.quuux.crier;

import android.preference.ListPreference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.util.Config;
import android.util.Log;

import org.quuux.crier.R;

public class TimesPreference extends ListPreference {
    private static final String TAG = "Crier";

    private long times = 0x0;

    public TimesPreference(Context context) {
	super(context);
	setPersistent(true);
    }

    public TimesPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(true);
    } 

    protected void onSetInitialValue(boolean restore_value, Object default_value)
    {
	if(Config.LOGD)
	    Log.d(TAG, "onSetInitialalue: restore=" + restore_value + ", default=" + default_value);		

	if(restore_value) 
	    times = Long.parseLong(getSharedPreferences().getString(getKey(), "0"));
	else
	    times = Long.parseLong(default_value.toString());	    
    }

    private boolean[] buildTimes() {
	boolean[] t = new boolean[48];

	for(int i=0; i<48; i++)
	    t[i] = (times & 1<<i) != 0;

	return t;
    }

    private void setTime(int which, boolean enabled) {
	if(enabled)
	    times |= (1<<which);
	else
	    times &= ~(1<<which);

	getEditor().putString(getKey(), Long.toString(times)).commit();

	if(Config.LOGD)
	    Log.d(TAG, String.format("times: 0x%012x", times));
    }

    protected void onPrepareDialogBuilder(Builder builder) {
	builder.setMultiChoiceItems(R.array.times, buildTimes(), 
				    new DialogInterface.OnMultiChoiceClickListener() {					
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {

					    if(Config.LOGD)
						Log.d(TAG, "Time checked: " + which + "=" + isChecked);		

					    setTime(which, isChecked);
					}
				    });

    }
}