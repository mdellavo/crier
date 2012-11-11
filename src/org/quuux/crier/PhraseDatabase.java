package org.quuux.crier;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

import java.lang.Integer;

import java.util.List;
import java.util.ArrayList;

public class PhraseDatabase extends SQLiteOpenHelper {
    public final static String TAG = "Crier";
    
    public final static String DB_NAME       = "phrases";
    public final static int    DB_VERSION    = 1;
    public final static String TABLE_PHRASES = "phrases";
    public final static String INDEX_ORDER   = "text ASC";

    public final static String COL_ID   = "_id";
    public final static String COL_TEXT = "name";
    public final static String COL_PATH = "path";

    public class Phrase {
	public int    id;
	public String text;
	public String path;

	public Phrase(long id, String text, String path)
	{
	    this.id   = (int)id;
	    this.text = text;
	    this.path = path;
	}
    }
    
    public PhraseDatabase(Context context)
    {
	super(context, DB_NAME, null, DB_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db)
    {
	db.execSQL("CREATE TABLE " + TABLE_PHRASES + "(" + 
		   "_id INTEGER PRIMARY KEY," +
		   "`text` TEXT NOT NULL," + 
		   "path TEXT NOT NULL" +
		   ")");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    public void onOpen(SQLiteDatabase db)
    {
    }

    private Phrase toPhrase(Cursor c)
    {
	return new Phrase(c.getLong(c.getColumnIndexOrThrow("id")), 
			  c.getString(c.getColumnIndexOrThrow("name")),
			  c.getString(c.getColumnIndexOrThrow("path")));
    }

    private ContentValues fromPhrase(Phrase c)
    {
	ContentValues values = new ContentValues();
       
	values.put("text", c.text);
	values.put("path", c.path.toString());

	return values;
    }
    
    public Phrase getPhrase(int id) 
    {
	Phrase phrase     = null;
	SQLiteDatabase db = getReadableDatabase();	

	Cursor c = db.query(TABLE_PHRASES, null, "id=?", new String[] { String.valueOf(id) }, null, null, null);
	
	if(c != null && c.moveToFirst()) {
	    phrase = toPhrase(c);
	    c.close();
	}

	db.close();

	return phrase;
    }

    public Cursor fetchAllPhrases()
    {
	return getReadableDatabase().query(TABLE_PHRASES, null, null, null, null, null, INDEX_ORDER);
    }

    public List<Phrase> phrasesList() {

	List<Phrase> phrases = new ArrayList<Phrase>();

	Cursor cursor = fetchAllPhrases();
		
	if (cursor.moveToFirst()) {
	    int id_col   = cursor.getColumnIndex(COL_ID); 
	    int text_col = cursor.getColumnIndex(COL_TEXT); 
	    int path_col = cursor.getColumnIndex(COL_PATH); 

	    do {  	
		phrases.add(new Phrase(cursor.getInt(id_col), cursor.getString(text_col), cursor.getString(path_col)));
	    } while (cursor.moveToNext());
	}

	return phrases;
    }

    public long createPhrase(String text, String path)
    {
	SQLiteDatabase db = getWritableDatabase();

	ContentValues values = new ContentValues();
       	values.put("text", text);
	values.put("path", path);
 
	long id = db.insert(TABLE_PHRASES, null, values);
	db.close();

	return id;
    }

    public boolean updatePhrase(int id, String text, String path)
    {
	boolean rv        = false;
	SQLiteDatabase db = getWritableDatabase();

	ContentValues values = new ContentValues();
       	values.put("text", text);
	values.put("path", path);

	if(db.update(TABLE_PHRASES, values, COL_ID+"=?", new String[] { String.valueOf(id) }) > 0)
	    rv = true;
	
	db.close();

	return rv;
    }

    public boolean deletePhrase(int id) 
    {
	boolean rv        = false;
	SQLiteDatabase db = getWritableDatabase();

	if(db.delete(TABLE_PHRASES, COL_ID+"=?", new String[] { String.valueOf(id) }) > 0)
	    rv = true;

	db.close();
	
	return rv;
    }	
}
