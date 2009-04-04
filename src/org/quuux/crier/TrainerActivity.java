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
import android.widget.EditText;
import android.util.Config;
import android.util.Log;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import android.provider.MediaStore;

import org.quuux.crier.R;

public class TrainerActivity extends Activity
{
    private static final String TAG = "Crier";

    static final int RESULT_RECORD_PHRASE = 1;

    static final int DIALOG_PHRASE_TEXT = 1;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.trainer);
	
	final ListView phrases_list = (ListView)findViewById(R.id.trainer_phrases_list);
	
	final Button button_add_phrase = (Button)findViewById(R.id.trainer_button_add_phrase);
	button_add_phrase.setOnClickListener(new Button.OnClickListener() {
		public void onClick(View v) {
		    startActivityForResult(new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION), RESULT_RECORD_PHRASE);
		}
	    });
    }

    protected void onActivityResult(int request_code, int result_code, Intent data) {
	if(Config.LOGD)
	    Log.d(TAG, "Result Intent: " + data);		

         if(request_code == RESULT_RECORD_PHRASE) {
             if(result_code == RESULT_OK) {	 
		 showDialog(DIALOG_PHRASE_TEXT);
             }
         }
     }

    protected Dialog onCreateDialog(int id) {
	Dialog rv = null;

	final View dialog_view = LayoutInflater.from(this).inflate(R.layout.dialog_phrase_text, null);
	
	if(id == DIALOG_PHRASE_TEXT) {
            rv = new AlertDialog.Builder(this)
                .setIcon(R.drawable.icon)
                .setTitle(R.string.dialog_phrase_title)
                .setView(dialog_view)
                .setPositiveButton(R.string.dialog_phrase_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
			EditText phrase_text = (EditText)dialog_view.findViewById(R.id.phrase_text);
			String text          = phrase_text.getText().toString();
			
			if(Config.LOGD)
			    Log.d(TAG, "Phrase Text: " + text);		
                    }
                })
                .setNegativeButton(R.string.dialog_phrase_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

			if(Config.LOGD)
			    Log.d(TAG, "Dialog cancel");	     
                    }
                })
                .create();
	}
      
	return rv;
    }
}