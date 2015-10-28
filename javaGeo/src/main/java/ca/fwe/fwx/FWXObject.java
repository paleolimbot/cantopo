package ca.fwe.fwx;

import java.io.BufferedWriter;
import java.io.IOException;

public interface FWXObject {
	
	public void write(int level, BufferedWriter w) throws IOException ;
	public FWXHandler getSAXHandler() ;
	public String getTagName() ;
	
	public interface FWXHandler {
		public void onAttribute(String name, String value) ;
		public void onProperty(String propertyName, String value) ;
		public void onValue(String value) ;
		public FWXObject getObject(String tagName) ;
	}
	
}
