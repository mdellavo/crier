package org.quuux.crier;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.quuux.crier.Toaster;

public class ToggleActivity extends Activity
{
    private static final String TAG = "Crier";

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
	
	Toaster.textShort(this, "Toggle!");
	finish();
    }
}