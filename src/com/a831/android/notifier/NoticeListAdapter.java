package com.a831.android.notifier;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.a831.android.notifier.dao.NotifierDAO;

public class NoticeListAdapter extends ArrayAdapter<String> {

	private Activity listActivity;
	private NotifierDAO dao;
	private List<NotifyEvent> events;
	
	public NoticeListAdapter(Activity listActivity, int resource, int resourceId) {
		super(listActivity, resource, resourceId);
		this.listActivity = listActivity;
		this.dao = new NotifierDAO(listActivity);
	}
	
	public View getView(int arg0, View convertView, ViewGroup arg2) {
		TextView rowView = (TextView)convertView;
		if (rowView == null) {
			rowView = new TextView(listActivity);
		}
		
		NotifyEvent event = getEvents().get(arg0);
		rowView.setText(event.getTitle());
		rowView.setTag(event);

		return rowView;
	}

	private synchronized List<NotifyEvent> getEvents() {
		if(events == null){
			events = dao.loadEvents();
		}
		return events;
	}

	@Override
	public int getCount() {
		return getEvents().size();
	}


}
