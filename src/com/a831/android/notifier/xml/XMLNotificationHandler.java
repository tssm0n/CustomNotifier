package com.a831.android.notifier.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.a831.android.notifier.NotifyEvent;
import com.a831.android.notifier.NotifyEvent.SeverityType;

public class XMLNotificationHandler extends DefaultHandler{
    private List<NotifyEvent> notices;
    private NotifyEvent current;
    private StringBuilder builder;
    
    private static final String NOTIFY = "notify";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String BODY = "body";
    private static final String SEVERITY = "severity";
    private static final String TIMESTAMP = "timestamp";
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    private static final String TAG = "XMLNotificationHandler";
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        builder.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {
        super.endElement(uri, localName, name);
        try {
			if (this.current != null){
			    if (localName.equalsIgnoreCase(TITLE)){
			        current.setTitle(builder.toString());
			    } else if (localName.equalsIgnoreCase(ID)){
			    	current.setId(Integer.parseInt(builder.toString().trim()));
			    } else if (localName.equalsIgnoreCase(BODY)){
			    	current.setBody(builder.toString());
			    } else if (localName.equalsIgnoreCase(SEVERITY)){
			    	current.setSeverity(NotifyEvent.SeverityType.valueOf(builder.toString().trim().toUpperCase()));
			    } else if (localName.equalsIgnoreCase(TIMESTAMP)){
			    	current.setTimestamp(parseDate(builder.toString()));
			    } else if (localName.equalsIgnoreCase(NOTIFY)){
			        notices.add(current);
			    }
			    builder.setLength(0);    
			}
		} catch (Exception e) {
			Log.w("XMLNotificationHandler", e.getMessage());
			NotifyEvent errorEvent = new NotifyEvent(-1, "An error has occurred while loading notifications", e.getMessage(), SeverityType.ALERT,
					new Date());
			notices.add(errorEvent);
		}
    }

    Date parseDate(String input)  {
    	try {
			return DATE_FORMAT.parse(input);
		} catch (ParseException e) {
			Log.d(TAG, "Parse exception handling date: " + input + " " + e.getMessage());
			return new Date();
		}
	}

	@Override
    public void startDocument() throws SAXException {
        super.startDocument();
        notices = new ArrayList<NotifyEvent>();
        builder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase(NOTIFY)){
            this.current = new NotifyEvent();
        }
    }

	public List<NotifyEvent> getNotices() {
		return this.notices;
	}

}
