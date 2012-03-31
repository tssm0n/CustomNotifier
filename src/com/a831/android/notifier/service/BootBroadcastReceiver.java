package com.a831.android.notifier.service;

import com.a831.android.notifier.NotifierConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d("BootBroadcastReceiver", "onReceive");
        Intent startServiceIntent = new Intent(context, CustomNotifierService.class);
        startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_COMMAND, NotifierConstants.BOOT);
        context.startService(startServiceIntent);
    }

}
