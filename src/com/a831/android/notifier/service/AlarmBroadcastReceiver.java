package com.a831.android.notifier.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.a831.android.notifier.NotifierConstants;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("AlarmBroadcastReceiver", "onReceive");
    	
        Intent startServiceIntent = new Intent(context, CustomNotifierService.class);
        startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_COMMAND, intent.getExtras().getInt(NotifierConstants.NOTIFICATION_COMMAND));
        if(intent.getExtras().containsKey(NotifierConstants.NOTIFICATION_DATA)){
        	startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_DATA, intent.getExtras().getBundle(NotifierConstants.NOTIFICATION_DATA));
        }
        
        if(!WakelockManager.hasWaitLock()){
        	WakelockManager.acquireWakeLock(context);
        }
        context.startService(startServiceIntent);
    }

}
