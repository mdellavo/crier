package org.quuux.crier;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import org.quuux.crier.R;
import org.quuux.crier.TrainerActivity;

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

    public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.options, menu);
	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	boolean rv;
	
	switch (item.getItemId()) {
	case R.id.option_speech_trainer:
	    startActivity(new Intent(this, TrainerActivity.class));
	    rv = true;
	    break;

	case R.id.option_about:
	    rv = true;
	    break;

	default:
	    rv = false;
	    break;
	}

	return rv;
    }
}
