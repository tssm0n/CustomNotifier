package com.a831.android.notifier.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.a831.android.notifier.NoticeDetailsActivity;
import com.a831.android.notifier.NotifierConstants;
import com.a831.android.notifier.NotifyEvent;
import com.a831.android.notifier.NotifyEvent.SeverityType;
import com.a831.android.notifier.R;
import com.a831.android.notifier.dao.NotifierDAO;
import com.a831.android.notifier.database.NotifierDatabaseConstants;
import com.a831.android.notifier.database.NotifierDatabaseConstants.Statuses;
import com.a831.android.notifier.xml.NotificationFeedParser;

public class CustomNotifierService extends Service {

	private static final String TAG = "CustomNotifierService";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm" );
	
	private NotifierDAO notifierDAO = null;
	
	private ScheduledFuture scheduledFuture = null; 
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d(TAG, "onBind");
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		if(notifierDAO != null){
			notifierDAO.close();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart");
		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		
		try {
			if(notifierDAO == null){
				notifierDAO = new NotifierDAO(getApplicationContext());
			}
	
			if(intent.getExtras().containsKey(NotifierConstants.NOTIFICATION_COMMAND)){
				int command = intent.getExtras().getInt(NotifierConstants.NOTIFICATION_COMMAND);
				Bundle bundle = intent.getExtras().getBundle(NotifierConstants.NOTIFICATION_DATA);
				handleCommand(command, bundle);
			}  else {
				Log.w(TAG, "No command found when starting service");
			}
		} catch (Exception e){
			Log.e(TAG, e.getMessage());
			notify(new NotifyEvent(-1, "An Unknown Error Occurred!", e.getMessage(), SeverityType.CRITICAL, new Date()));
		} finally {
	        if(WakelockManager.hasWaitLock()){
	        	WakelockManager.releaseWaitLock();
	        }
		}
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void handleCommand(int command, Bundle bundle) {
		switch(command){
			case NotifierConstants.BOOT:
				Log.d(TAG, "Booting");
				markAsRunning();
				scheduleNext(1);
				break;
			case NotifierConstants.START_SCHEDULE:
				if(!isAlreadyRunning()){
					markAsRunning();
					scheduleNext(1);
				}
				break;
			case NotifierConstants.CLOCK_TICK:
				if(shouldContinueRunning(bundle)){
					checkForUpdates();
					scheduleNextAlarm(timerDelay());
				}
				break;
			case NotifierConstants.STOP:
				markAsStopped();
				if(scheduledFuture != null){
					scheduledFuture.cancel(false);
				}
				break;
			case NotifierConstants.UPDATE_SETTINGS:
				saveSettings(bundle);
				if(isAlreadyRunning()){
					if(scheduledFuture != null){
						scheduledFuture.cancel(false);
						scheduleNext(1);
					}
				}
				break;
			default:
				Log.w(TAG, "Invalid command found");
		}
	}

	private void saveSettings(Bundle bundle) {
		String url = bundle.getString(NotifierConstants.URL);
		int interval = bundle.getInt(NotifierConstants.INTERVAL);
		
		notifierDAO.updateSettings(url, interval);
	}

	private boolean shouldContinueRunning(Bundle data) {
		int expectedProcess = data.getInt(NotifierConstants.PROCESS_ID);
		int currentProcess = Integer.parseInt(notifierDAO.findSetting(NotifierDatabaseConstants.PROCESS_ID));
		
		if(expectedProcess != currentProcess){
			Log.d(TAG, "Process IDs don't match, not executing");
			return false;
		}
		
		Log.d(TAG, "Process IDs match, continuing: " + currentProcess);
		
		return notifierDAO.currentStatus() == Statuses.RUNNING;
	}

	private boolean isAlreadyRunning() {
		return notifierDAO.currentStatus() == Statuses.RUNNING;
	}

	private void markAsRunning() {
		int currentProcess = Integer.parseInt(notifierDAO.findSetting(NotifierDatabaseConstants.PROCESS_ID));
		currentProcess++;
		
		notifierDAO.updateSetting(Integer.toString(currentProcess), NotifierDatabaseConstants.PROCESS_ID); 
		
		notifierDAO.updateStatus(Statuses.RUNNING);
	}
	
	private void markAsStopped() {
		notifierDAO.updateStatus(Statuses.STOPPED);
	}

	private void checkForUpdates() {
		Log.d(TAG, "Checking for notifications");
		
		String sourceURL = findSourceUrl();
		
		List<NotifyEvent> notices = null;
		
		try {
			NotificationFeedParser parser = new NotificationFeedParser(sourceURL);
			
			notices = parser.parse();
		} catch (MalformedURLException e) {
			if(sourceURL != null && sourceURL.trim().length() > 0){
				notify(new NotifyEvent(-1, "Invalid URL", "URL Is Invalid: " + sourceURL, SeverityType.WARNING, new Date()));
			}
		} catch (IOException e){
			// TODO: This should be handled, perhaps display a warning every 5 error messages or so...
		} catch (Exception e){
            Log.e(TAG, e.getMessage());
            notify(new NotifyEvent(-1, "An Error Occurred!", e.getMessage(), SeverityType.CRITICAL, new Date()));
        }
		
		if(notices == null || notices.size() == 0){
			return;
		}
		
		for(NotifyEvent notice : notices){
			if(notice.getId() == -1){
				notify(notice);
			} else {
				if(!notifierDAO.eventExists(notice.getId())){
					notifierDAO.saveNotifyEvent(notice);
					notify(notice);
				} else {
					Log.d(TAG, "Not notifying of existing event: " + notice.getId());
				}
			}
		}
		
		notifierDAO.updateSetting(DATE_FORMAT.format(new Date()), NotifierDatabaseConstants.LAST_FIRE_ID);
		
	}
	
	private String findSourceUrl(){
		return notifierDAO.findSetting(NotifierDatabaseConstants.URL_ID);
	}
	
	private long timerDelay() {
		return Integer.parseInt(notifierDAO.findSetting(NotifierDatabaseConstants.INTERVAL_ID)) * 60;
	}
	
	private void notify(NotifyEvent notice){
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
		
		int icon = R.drawable.notice;
		
		switch(notice.getSeverity()){
			case ALERT:
				icon = R.drawable.alert;
				break;
			case CRITICAL:
				icon = R.drawable.critical;
				break;
			case WARNING:
				icon = R.drawable.warning;
				break;
			case NOTICE:
			default:
				icon = R.drawable.notice;
				
		}
		
		CharSequence tickerText = notice.getTitle();
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = notice.getTitle();
		CharSequence contentText = notice.getBody();
		Intent notificationIntent = new Intent(this, NoticeDetailsActivity.class);
		notificationIntent.putExtra(NotifierConstants.NOTIFICATION_ID,  notice.getId());
		notificationIntent.putExtra(NotifierConstants.NOTIFICATION_TEXT,  notice.getBody());
		PendingIntent contentIntent = PendingIntent.getActivity(this, notice.getId(), notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.defaults = Notification.DEFAULT_ALL;
		
		notificationManager.notify(notice.getId(), notification);
	}
	
	private void scheduleNext(long delay){
        final Context context = this;
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        final int currentProcess = Integer.parseInt(notifierDAO.findSetting(NotifierDatabaseConstants.PROCESS_ID));

		final Runnable clockTick = new Runnable() {
			public void run() {
				Bundle bundle = new Bundle();
				bundle.putInt(NotifierConstants.PROCESS_ID, currentProcess);
				
		        Intent startServiceIntent = new Intent(context, CustomNotifierService.class);
		        startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_COMMAND, NotifierConstants.CLOCK_TICK);
		        startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_DATA, bundle);
		        context.startService(startServiceIntent);
			}
		};
		
		scheduledFuture = scheduler.schedule(clockTick, delay, SECONDS);
	}
	
	private void scheduleNextAlarm(long delay){
		final Context context = this;
		final int currentProcess = Integer.parseInt(notifierDAO.findSetting(NotifierDatabaseConstants.PROCESS_ID));
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, (int)delay);
		
		Bundle bundle = new Bundle();
		bundle.putInt(NotifierConstants.PROCESS_ID, currentProcess);
		
		Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
		intent.putExtra(NotifierConstants.NOTIFICATION_COMMAND, NotifierConstants.CLOCK_TICK);
		intent.putExtra(NotifierConstants.NOTIFICATION_DATA, bundle);
		PendingIntent sender = PendingIntent.getBroadcast(this, 867, intent,PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}
	
	

}
