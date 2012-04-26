package com.a831.android.notifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NoticeDetailsActivity extends Activity {

	private static final String TAG = "NoticeDetailsActivity";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	       super.onCreate(savedInstanceState);
	        setContentView(R.layout.details);
	        
	        Intent intent = getIntent();
	        
	        displayMessage(intent);
	}

	private void displayMessage(Intent intent) {
		TextView tv = (TextView) findViewById(R.id.messageDetails);
		
		String text = intent.getStringExtra(NotifierConstants.NOTIFICATION_TEXT);

		tv.setText(text);
		
		Log.d(TAG, text);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.d(TAG, "New Intent");
		displayMessage(intent);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(!hasFocus){
			finish();
		}
	}

	
		
}
