package com.a831.android.notifier.database;

import android.provider.BaseColumns;

public class NotifierDatabaseConstants implements BaseColumns{
	
    public static final String PARAMETERS_TABLE = "PARAMETERS";
    public static final String MESSAGES_TABLE = "MESSAGES";
	
	public static final String VALUE_COLUMN = "VALUE";
	
	public static final String TITLE_COLUMN = "TITLE";
	public static final String BODY_COLUMN = "BODY";
	public static final String SEVERITY_COLUMN = "SEVERITY";
	public static final String TIMESTAMP_COLUMN = "TIMESTAMP";
	
	
	public static final int STATUS_ID = 0;
	public static final int URL_ID = 1;
	public static final int INTERVAL_ID = 2;
	public static final int PROCESS_ID = 3;
	public static final int LAST_FIRE_ID = 4;
	
	public enum Statuses { STOPPED, RUNNING };

}
