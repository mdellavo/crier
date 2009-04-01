package org.quuux.crier;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.quuux.crier.R;

public class CrierActivity extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener
{

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	SharedPreferences preferences;
	preferences = PreferenceManager.getDefaultSharedPreferences(this);
	preferences.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	if(key.compareTo("time_enabled") == 0)
	    if(sharedPreferences.getBoolean("time_enabled", false))
		AlarmInstaller.install(this);
	    else
		AlarmInstaller.cancel(this);
    }
}
