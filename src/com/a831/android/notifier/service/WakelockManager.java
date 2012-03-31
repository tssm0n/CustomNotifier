package com.a831.android.notifier.service;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class WakelockManager {
	   private static final String TAG = "WakelockManager";
	   
	   private static PowerManager.WakeLock wakeLock;

	   public static void acquireWakeLock(Context context) {

	        if (wakeLock != null) {
	            return;
	        }
	        
	        Log.d(TAG, "Acquiring wake lock");
	        
	        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);


	        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG);
	        wakeLock.acquire();
	    }

	    public static void releaseWaitLock() {
	        if (wakeLock != null) {
	        	Log.d(TAG, "Releasing Wake Lock");
	        	wakeLock.release();
	        	wakeLock = null;
	        }
	    }
	    
	    
	    public static boolean hasWaitLock() {
	    	return wakeLock != null;
	    }

}
