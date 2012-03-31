package com.a831.android.notifier.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotificationTablesHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notify.db";
    private static final int DATABASE_VERSION = 5;

    private static final String TAG = "ContentProvider";	
    
    public NotificationTablesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	
	@Override
	public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NotifierDatabaseConstants.PARAMETERS_TABLE + "(" + 
					NotifierDatabaseConstants._ID + " INTEGER PRIMARY KEY, " +
					NotifierDatabaseConstants.VALUE_COLUMN + " TEXT" +
					")");
			
			
			ContentValues values = new ContentValues();
			values.put(NotifierDatabaseConstants._ID, NotifierDatabaseConstants.STATUS_ID);
			values.put(NotifierDatabaseConstants.VALUE_COLUMN, NotifierDatabaseConstants.Statuses.STOPPED.toString());
			db.insert(NotifierDatabaseConstants.PARAMETERS_TABLE, null, values);
			
			values = new ContentValues();
			values.put(NotifierDatabaseConstants._ID, NotifierDatabaseConstants.URL_ID);
			values.put(NotifierDatabaseConstants.VALUE_COLUMN, "");
			db.insert(NotifierDatabaseConstants.PARAMETERS_TABLE, null, values);
			
			values = new ContentValues();
			values.put(NotifierDatabaseConstants._ID, NotifierDatabaseConstants.INTERVAL_ID);
			values.put(NotifierDatabaseConstants.VALUE_COLUMN, "15");
			db.insert(NotifierDatabaseConstants.PARAMETERS_TABLE, null, values);
			
			values = new ContentValues();
			values.put(NotifierDatabaseConstants._ID, NotifierDatabaseConstants.PROCESS_ID);
			values.put(NotifierDatabaseConstants.VALUE_COLUMN, "0");
			db.insert(NotifierDatabaseConstants.PARAMETERS_TABLE, null, values);			

			db.execSQL("CREATE TABLE " + NotifierDatabaseConstants.MESSAGES_TABLE + " (" + 
					NotifierDatabaseConstants._ID + " INTEGER PRIMARY KEY, " +
					NotifierDatabaseConstants.TITLE_COLUMN + " TEXT, " +
					NotifierDatabaseConstants.BODY_COLUMN  + " TEXT, " +
					NotifierDatabaseConstants.SEVERITY_COLUMN  + " TEXT, " +
					NotifierDatabaseConstants.TIMESTAMP_COLUMN	+ " INTEGER" +
			")");
			
			

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + NotifierDatabaseConstants.PARAMETERS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + NotifierDatabaseConstants.MESSAGES_TABLE);
        onCreate(db);


	}
}
