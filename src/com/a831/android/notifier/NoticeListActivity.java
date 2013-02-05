package com.a831.android.notifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class NoticeListActivity extends Activity {

	private NoticeListAdapter noticeListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_notice_list);
		
		ListView listView = (ListView)findViewById(R.id.noticeListView);
		noticeListAdapter = new NoticeListAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1);
		listView.setAdapter(noticeListAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
					long arg3) {
				Intent notificationIntent = new Intent(NoticeListActivity.this, NoticeDetailsActivity.class);
				NotifyEvent event = (NotifyEvent) v.getTag();
				notificationIntent.putExtra(NotifierConstants.NOTIFICATION_ID,  event.getId());
				notificationIntent.putExtra(NotifierConstants.NOTIFICATION_TEXT,  event.getBody());
				notificationIntent.putExtra(NotifierConstants.NOTIFICATION_TIME, event.getTimestamp());
				startActivity(notificationIntent);
			}
		});
		
		listView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus){
					noticeListAdapter.refreshEvents();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_notice_list, menu);
		return true;
	}

}
