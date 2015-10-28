package ca.fwe.fwx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class FWXDocument {

	public static final String TAG_PROPERTY = "property" ;
	public static final String ATTRIBUTE_PROPERTY_NAME = "name" ;
	public static final String ATTRIBUTE_PROPERTY_VALUE = "value" ;

	private FWXObject rootElement ;

	public FWXDocument(FWXObject rootElement) {
		this.rootElement = rootElement ;
	}	

	public void write(File file) throws IOException {
		FileOutputStream fo = new FileOutputStream(file) ;
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(fo, "UTF-8")) ;
		w.write("<?xml version=\"1.0\" ?>\n") ;
		rootElement.write(0, w) ;
		w.flush() ;
		w.close() ;
	}

	public boolean readFrom(File file) throws IOException, SAXException {
		try {
			SAXParser sax = SAXParserFactory.newInstance().newSAXParser() ;
			sax.parse(file, new SiteViewXMLHandler()) ;
			return true ;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false ;
		}
	}


	public class SiteViewXMLHandler extends DefaultHandler {

		private List<FWXObject> objectChain ;
		private String skipTag ;
		private String currentCharsString ;

		public SiteViewXMLHandler() {
			objectChain = new ArrayList<FWXObject>() ;
			skipTag = null ;
		}

		private FWXObject current() {
			if(objectChain.size() > 0)
				return objectChain.get(objectChain.size() - 1) ;
			else
				return null ;
		}

		@Override
		public void startDocument() throws SAXException {

		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

			currentCharsString = "" ;
			
			if(skipTag != null) {
				//do nothing
			} else if(qName.equals(rootElement.getTagName())) {
				if(current() == null) {
					objectChain.add(rootElement) ;
					readAttributes(rootElement, attributes) ;
				} else {
					throw new InvalidDocumentStructureException() ;
				}
			} else if(qName.equals(FWXProperty.TAG_NAME)) {
				String name = attributes.getValue(FWXProperty.ATTRIBUTE_NAME) ;
				if(name != null && current() != null) {
					FWXProperty p = new FWXProperty(current(), name) ;
					objectChain.add(p) ;
				} else {
					throw new InvalidDocumentStructureException() ;
				}
			} else {
				FWXObject obj = current().getSAXHandler().getObject(qName) ;
				if(obj != null) {
					this.readAttributes(obj, attributes) ;
					objectChain.add(obj) ;
				} else {
					skipTag = qName ;
				}
			}

		}
		
		private void readAttributes(FWXObject obj, Attributes attributes) {
			for(int i=0; i<attributes.getLength(); i++) {
				String name = attributes.getQName(i) ;
				String value = attributes.getValue(i) ;
				obj.getSAXHandler().onAttribute(name, value) ;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if(skipTag == null) {
				currentCharsString = new String(ch, start, length) ;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if(qName.equals(skipTag)) {
				skipTag = null ;
			} else if(qName.equals(current().getTagName())) {
				current().getSAXHandler().onValue(currentCharsString) ;
				objectChain.remove(current()) ;
			}
		}

		@Override
		public void endDocument() throws SAXException {

		}
	}

	public class InvalidDocumentStructureException extends SAXException {
		private static final long serialVersionUID = -5305919990746191292L;
	}
}
