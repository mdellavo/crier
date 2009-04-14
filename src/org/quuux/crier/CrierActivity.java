package org.quuux.crier;

import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.app.Dialog;
import android.app.AlertDialog;

import org.quuux.crier.R;
import org.quuux.crier.TrainerActivity;

public class CrierActivity extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    final static int DIALOG_ABOUT = 0x1;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

	SharedPreferences preferences;
	preferences = PreferenceManager.getDefaultSharedPreferences(this);
	preferences.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	if(key.compareTo("enabled") == 0 || key.compareTo("time_enabled") == 0)
	    if(sharedPreferences.getBoolean("enabled", false) && sharedPreferences.getBoolean("time_enabled", false))
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
	    showDialog(DIALOG_ABOUT);
	    rv = true;
	    break;

	default:
	    rv = false;
	    break;
	}

	return rv;
    }

    protected Dialog onCreateDialog(int id)
    {
	Dialog rv = null;

	switch(id) { 
	case DIALOG_ABOUT:
	    rv = new AlertDialog.Builder(this)
		.setTitle(R.string.about_title)
		.setView(getLayoutInflater().inflate(R.layout.dialog_about, null))
		.setPositiveButton(R.string.about_ok, null)
		.create();
	    break;


	default:
	    break;
	}

	return rv;
    }

}
