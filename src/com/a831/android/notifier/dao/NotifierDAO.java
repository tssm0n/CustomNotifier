package com.a831.android.notifier.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.a831.android.notifier.NotifyEvent;
import com.a831.android.notifier.NotifyEvent.SeverityType;
import com.a831.android.notifier.database.NotificationTablesHelper;
import com.a831.android.notifier.database.NotifierDatabaseConstants;

public class NotifierDAO {

	private NotificationTablesHelper helper;
	
	private static final String TAG = "NotifierDAO";
	
	public NotifierDAO(Context context){
		helper = new NotificationTablesHelper(context);
	}
	
	public void close(){
		helper.close();
	}
	
	public NotifierDatabaseConstants.Statuses currentStatus(){
		String result = findSetting(NotifierDatabaseConstants.STATUS_ID);
		if(result != null){
			return NotifierDatabaseConstants.Statuses.valueOf(result);
		}
		return null;
	}
	
	public int updateStatus(NotifierDatabaseConstants.Statuses status){
		Log.d(TAG, "Changing status to: " + status.toString());
		return updateSetting(status.toString(), NotifierDatabaseConstants.STATUS_ID);
	}

	public int updateSetting(String value, int settingId) {
		ContentValues values = new ContentValues();
		values.put(NotifierDatabaseConstants.VALUE_COLUMN, value);
		
		return helper.getWritableDatabase().update(NotifierDatabaseConstants.PARAMETERS_TABLE, values, 
				NotifierDatabaseConstants._ID + " = ?", new String[] {Integer.toString(settingId)});
	}

	public void updateSettings(String url, int interval){
		ContentValues values = new ContentValues();
		values.put(NotifierDatabaseConstants.VALUE_COLUMN, url);
		
		helper.getWritableDatabase().update(NotifierDatabaseConstants.PARAMETERS_TABLE, values, 
				NotifierDatabaseConstants._ID + " = ?", new String[] {Integer.toString(NotifierDatabaseConstants.URL_ID)});

		values = new ContentValues();
		values.put(NotifierDatabaseConstants.VALUE_COLUMN, Integer.toString(interval));
		
		helper.getWritableDatabase().update(NotifierDatabaseConstants.PARAMETERS_TABLE, values, 
				NotifierDatabaseConstants._ID + " = ?", new String[] {Integer.toString(NotifierDatabaseConstants.INTERVAL_ID)});
	
	}
	
	public String findSetting(int id){
		String result = null;
		
		SQLiteDatabase db = helper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from " + NotifierDatabaseConstants.PARAMETERS_TABLE +
					" WHERE " + NotifierDatabaseConstants._ID + " = ?", new String[] {Integer.toString(id)});
		
		if(cursor.moveToFirst()){
			result = cursor.getString(1);
		}
		
		cursor.close();
	
		return result;
	}
	
	public long saveNotifyEvent(NotifyEvent notice){
		if(notice.getTitle() == null){
			notice.setTitle("");
		}
		if(notice.getBody() == null){
			notice.setBody("");
		}
		if(notice.getSeverity() == null){
			notice.setSeverity(SeverityType.NOTICE);
		}
		if(notice.getTimestamp() == null){
			notice.setTimestamp(new Date());
		}
		
		
		ContentValues values = new ContentValues();
		values.put(NotifierDatabaseConstants._ID, notice.getId());
		values.put(NotifierDatabaseConstants.TITLE_COLUMN, notice.getTitle());
		values.put(NotifierDatabaseConstants.BODY_COLUMN, notice.getBody());
		values.put(NotifierDatabaseConstants.SEVERITY_COLUMN, notice.getSeverity().toString());
		values.put(NotifierDatabaseConstants.TIMESTAMP_COLUMN, notice.getTimestamp().getTime());
		
		return helper.getWritableDatabase().insert(NotifierDatabaseConstants.MESSAGES_TABLE, null, values);
	}
	
	public NotifyEvent loadNotifyEvent(int id){
		NotifyEvent result = null;
		
		SQLiteDatabase db = helper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from " + NotifierDatabaseConstants.MESSAGES_TABLE +
					" WHERE " + NotifierDatabaseConstants._ID + " = ?", new String[] {Integer.toString(id)});
		
		if(cursor.moveToFirst()){
			result = new NotifyEvent(cursor.getInt(0), cursor.getString(1), cursor.getString(2), SeverityType.valueOf(cursor.getString(3)), new Date(cursor.getInt(4)));
		}
		
		cursor.close();
	
		return result;		
	}
	
	public boolean eventExists(int id){
		return loadNotifyEvent(id) != null;
	}
	
	public List<NotifyEvent> loadEvents(){
		List<NotifyEvent> result = new ArrayList<NotifyEvent>();
		
		SQLiteDatabase db = helper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select * from "
				+ NotifierDatabaseConstants.MESSAGES_TABLE + " ORDER BY "
				+ NotifierDatabaseConstants.DEFAULT_SORT_ORDER, null);
		
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			result.add(new NotifyEvent(cursor.getInt(0), cursor.getString(1),
					cursor.getString(2), SeverityType.valueOf(cursor
							.getString(3)), new Date(cursor.getInt(4))));
		}
		
		cursor.close();
	
		return result;			
	}
	
	public int countEvents(){
		SQLiteDatabase db = helper.getReadableDatabase();
		
		Cursor cursor = db.rawQuery("select count(*) from " + NotifierDatabaseConstants.MESSAGES_TABLE, null);
		
		int result = 0;
		
		if(cursor.moveToFirst()){
			result = cursor.getInt(0);
		}
		
		cursor.close();
	
		return result;	
	}
}
