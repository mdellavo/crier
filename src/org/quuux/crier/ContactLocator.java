package org.quuux.crier;

import android.content.Context;
import android.util.Config;
import android.util.Log;

import android.telephony.PhoneNumberUtils;
import android.provider.Contacts.Phones;
import android.database.Cursor;

public class ContactLocator {
    private static final String TAG = "Crier";

    private Context context;

    public ContactLocator(Context c) {
	context = c;
    }

    public String locate(String address) {
	String[] cols = new String[] {
	    Phones.NAME,
	    Phones.DISPLAY_NAME,
	    Phones.PERSON_ID,
	};
	
	String[] args = new String[] {
	    PhoneNumberUtils.toCallerIDMinMatch(address) + '%'
	};
	
	if(Config.LOGD)
	    Log.d(TAG, "looking up " + PhoneNumberUtils.toCallerIDMinMatch(address));

	Cursor cursor = context.getContentResolver().query(Phones.CONTENT_URI, cols, Phones.NUMBER_KEY + " LIKE ?", args, null);
	
	if (cursor.moveToFirst()) {
	    int id_col           = cursor.getColumnIndex(Phones.PERSON_ID); 
	    int name_col         = cursor.getColumnIndex(Phones.NAME); 
	    int display_name_col = cursor.getColumnIndex(Phones.DISPLAY_NAME); 

	    do {		
		int id              = cursor.getInt(id_col);
		String name         = cursor.getString(name_col);

		String display_name = cursor.getString(display_name_col);
		
		if(Config.LOGD)
		    Log.d(TAG, "found person : " + id + ", " + name + ", " + display_name);

		return display_name;		
	    } while (cursor.moveToNext());
	}

	return null;
    }
}