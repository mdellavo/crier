package org.quuux.crier;

import android.preference.DialogPreference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.LayoutInflater;

public class TimesPreference extends DialogPreference {
    public TimesPreference(Context context, AttributeSet attrs) {
	super(context, attrs);
	setPersistent(true);
    }

    public TimesPreference(Context context, AttributeSet attrs, int def_style) {
	super(context, attrs, def_style);
	setPersistent(true);
    } 

    protected View onCreateDialogView() {
	LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	return inflater.inflate(R.layout.times_picker, null);
    }

    
}