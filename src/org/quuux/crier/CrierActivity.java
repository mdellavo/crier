package org.quuux.crier;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;

import org.quuux.crier.R;

public class CrierActivity extends PreferenceActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
