package org.quuux.crier;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.preference.Preference;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.util.Config;
import android.util.Log;

import android.provider.MediaStore;

import org.quuux.crier.R;

public class TrainerActivity extends Activity
{
    private static final String TAG = "Crier";

    static final int RECORD_PHRASE = 1;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.trainer);
	
	final ListView phrases_list = (ListView)findViewById(R.id.trainer_phrases_list);
	
	final Button button_add_phrase = (Button)findViewById(R.id.trainer_button_add_phrase);
	button_add_phrase.setOnClickListener(new Button.OnClickListener() {
		publicx void onClick(View v) {
		    startActivityForResult(new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION), RECORD_PHRASE);
		}
	    });
    }

    protected void onActivityResult(int request_code, int result_code, Intent data) {
	if(Config.LOGD)
	    Log.d(TAG, "Result Intent: " + data);		

         if(request_code == RECORD_PHRASE) {
             if(result_code == RESULT_OK) {	 
		 
             }
         }
     }    
}