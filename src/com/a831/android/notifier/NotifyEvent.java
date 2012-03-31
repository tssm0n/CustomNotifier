package com.a831.android.notifier;

import java.util.Date;

public class NotifyEvent {

	public enum SeverityType  { NOTICE, WARNING, ALERT, CRITICAL };
	
	private int id;
	private String title;
	private String body;
	private SeverityType severity;
	private Date timestamp;
	
	public NotifyEvent() {
		super();
	}
	
	
	
	public NotifyEvent(int id, String title, String body,
			SeverityType severity, Date timestamp) {
		super();
		this.id = id;
		this.title = title;
		this.body = body;
		this.severity = severity;
		this.timestamp = timestamp;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public SeverityType getSeverity() {
		return severity;
	}
	public void setSeverity(SeverityType severity) {
		this.severity = severity;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
}
