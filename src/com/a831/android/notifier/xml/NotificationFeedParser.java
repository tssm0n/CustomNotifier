package com.a831.android.notifier.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.util.Log;

import com.a831.android.notifier.NotifyEvent;
import com.a831.android.notifier.NotifyEvent.SeverityType;

public class NotificationFeedParser {

    final URL sourceURL;

    public NotificationFeedParser(String sourceURL) throws MalformedURLException{
        try {
            this.sourceURL = new URL(sourceURL);
        } catch (MalformedURLException e) {
            throw e;
        }
    }
    
    NotificationFeedParser(){
    	sourceURL = null;
    }
    
    public List<NotifyEvent> parse() throws IOException {
    	try {
			InputStream inputStream = sourceURL.openConnection().getInputStream();
			List<NotifyEvent> result = parse(inputStream);
			inputStream.close();
			return result;
    	} catch (IOException e) {
    		throw e;
		} catch (Exception e) {
			Log.w("NotificationFeedParser", e.getMessage());
			NotifyEvent errorEvent = new NotifyEvent(-1, "An error has occurred while loading notifications", e.getMessage(), SeverityType.ALERT,
					new Date());
			return Collections.singletonList(errorEvent);
		}
    }
    
    List<NotifyEvent> parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException{
        SAXParserFactory factory = SAXParserFactory.newInstance();
	    SAXParser parser = factory.newSAXParser();
	    XMLNotificationHandler handler = new XMLNotificationHandler();
	    parser.parse(inputStream, handler);
	    return handler.getNotices();

    }
	
}
