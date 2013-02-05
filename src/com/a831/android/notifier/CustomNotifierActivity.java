package com.a831.android.notifier;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.a831.android.notifier.dao.NotifierDAO;
import com.a831.android.notifier.database.NotifierDatabaseConstants;
import com.a831.android.notifier.database.NotifierDatabaseConstants.Statuses;
import com.a831.android.notifier.service.CustomNotifierService;


public class CustomNotifierActivity extends Activity {
	
	private static final String TAG = "CustomNotifierActivity";
	
	private static final int STATUS_UPDATE_DELAY = 60;
	
	private static final TimerInterval[] TIMER_INTERVALS = new TimerInterval[] {   
		        new TimerInterval("1 Minute", 1),
		        new TimerInterval("5 Minutes", 5),
		        new TimerInterval("10 Minutes", 10),
		        new TimerInterval("15 Minutes", 15),
		        new TimerInterval("30 Minutes", 30),
		        new TimerInterval("1 Hour", 60),
		        new TimerInterval("2 Hours", 120),
		        new TimerInterval("4 Hours", 240)};

	private NotifierDAO dao;
	
	private ScheduledFuture scheduledFuture = null; 
	
	private OnClickListener startListener = new OnClickListener() {
	    public void onClick(View v) {
	      startService(NotifierConstants.START_SCHEDULE, null);
	    }
	};

	private OnClickListener stopListener = new OnClickListener() {
	    public void onClick(View v) {
	    	startService(NotifierConstants.STOP, null);
	    }
	};
	
	private OnClickListener saveListener = new OnClickListener() {
	    public void onClick(View v) {
	    	startService(NotifierConstants.UPDATE_SETTINGS, settingsBundle());
	    }
	};	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(dao == null){
        	dao = new NotifierDAO(this);
        }
        
        Button startButton = (Button)findViewById(R.id.startButton);
        Button stopButton = (Button)findViewById(R.id.stopButton);
        Button saveButton = (Button)findViewById(R.id.saveButton);
        
        Spinner spinner = (Spinner) findViewById(R.id.spinnerInterval);
        /*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.interval_array, android.R.layout.simple_spinner_item);*/
        ArrayAdapter<TimerInterval> adapter = new ArrayAdapter<TimerInterval>(this,
                android.R.layout.simple_spinner_item, TIMER_INTERVALS);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        updateStatus(null);
        loadSettings();

        startButton.setOnClickListener(startListener);
        stopButton.setOnClickListener(stopListener);
        saveButton.setOnClickListener(saveListener);
        
        scheduleStatusUpdates();
        
        Log.d(TAG, "Starting...");

        //startService(NotifierConstants.START_SCHEDULE, null);
    }

	private void scheduleStatusUpdates() {
		Log.d(TAG, "scheduleStatusUpdates");
		if(!(scheduledFuture == null || scheduledFuture.isCancelled() || scheduledFuture.isDone())){
			Log.d(TAG, "already scheduled");
			return;
		}
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable(){
			public void run() {
				Log.d(TAG, "Updating Status");
				try {
					runOnUiThread(new Runnable(){
						public void run() {
							updateStatus(null);
						}
					});
				} catch(Exception e){
					Log.e(TAG, e.getMessage());
				}
			}
        }, 1, STATUS_UPDATE_DELAY, SECONDS);
	}

	private void updateStatus(String inStatus) {
		TextView statusView = (TextView)findViewById(R.id.textViewStatus);
		if(inStatus == null){
			if(dao != null){
				String status = dao.currentStatus().toString();
				statusView.setText("   " + status);
			}
		} else {
			statusView.setText("   " + inStatus);
		}
		
		TextView lastRun = (TextView)findViewById(R.id.textViewUpdateStatus);
		if(dao != null){
			String update = dao.findSetting(NotifierDatabaseConstants.LAST_FIRE_ID);
			lastRun.setText("   " + update);
		}
	}
    

	private void loadSettings() {
		if(dao != null){
			String url = dao.findSetting(NotifierDatabaseConstants.URL_ID);
			String interval = dao.findSetting(NotifierDatabaseConstants.INTERVAL_ID);
		
			Spinner spinner = (Spinner) findViewById(R.id.spinnerInterval);
			EditText urlBox = (EditText) findViewById(R.id.editTextUrl);
			
			urlBox.getEditableText().clear();
			urlBox.getEditableText().append(url);
			
			spinner.setSelection(findIntervalPosition(interval));
		}
	}	
	
    private int findIntervalPosition(String interval) {
		int intInterval = Integer.parseInt(interval);
    	for(int index = 0; index < TIMER_INTERVALS.length; index++){
			if(TIMER_INTERVALS[index].getMinutes() == intInterval){
				return index;
			}
		}
    	return 0;
	}

	private void startService(int command, Bundle data){
        Intent startServiceIntent = new Intent(this, CustomNotifierService.class);
        startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_COMMAND, command);
        if(data != null){
        	startServiceIntent.putExtra(NotifierConstants.NOTIFICATION_DATA, data);
        }
        this.startService(startServiceIntent);  
        if(command == NotifierConstants.STOP || command == NotifierConstants.START_SCHEDULE){
	        if(command == NotifierConstants.STOP){
	        	updateStatus(Statuses.STOPPED.toString());
	        } else {
	        	updateStatus(Statuses.RUNNING.toString());
	        }
        }
    }
    
	private Bundle settingsBundle() {
		Bundle bundle = new Bundle();
		
		Spinner spinner = (Spinner) findViewById(R.id.spinnerInterval);
		int timerInterval = 0;
		if(spinner.getSelectedItem() != null){
			timerInterval = ((TimerInterval) spinner.getSelectedItem()).getMinutes();
		}
		
		EditText urlBox = (EditText) findViewById(R.id.editTextUrl);
		String url = urlBox.getEditableText().toString();
		
		bundle.putString(NotifierConstants.URL, url);
		bundle.putInt(NotifierConstants.INTERVAL, timerInterval);
		
		Log.d(TAG, "Timer interval: " + timerInterval);
		Log.d(TAG, "URL: " + url);
		
		return bundle;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelStatusUpdates();
		if(dao != null){
			dao.close();
			dao = null;
		}
		
	}
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		cancelStatusUpdates();
		if(hasFocus){
			updateStatus(null);
			scheduleStatusUpdates();
		}
	}
	
	public void showHistory(View view){
		Intent intent = new Intent(this, NoticeListActivity.class); 
		startActivity(intent);
	}

	private void cancelStatusUpdates() {
		if(scheduledFuture != null){
			scheduledFuture.cancel(false);
			scheduledFuture = null;
		}
	}
	
	private static class TimerInterval {
		private String name;
		private int minutes;
		
		public TimerInterval(String name, int minutes) {
			super();
			this.name = name;
			this.minutes = minutes;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getMinutes() {
			return minutes;
		}
		public void setMinutes(int minutes) {
			this.minutes = minutes;
		}
		@Override
		public String toString() {
			return name;
		}
		
		
		
		
	}
    
    
}