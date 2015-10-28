package ca.fwe.fwx;

import java.io.BufferedWriter;
import java.io.IOException;

public class FWXProperty implements FWXObject {
	
	public static final String TAG_NAME = "property" ;
	
	public static final String ATTRIBUTE_NAME = "name" ;
	
	private FWXObject parent ;
	private String name ;
	private String value ;
	
	public FWXProperty(String name, String value) {
		this.name = name ;
		this.value = value ;
	}
	
	public FWXProperty(FWXObject parent, String name) {
		this.parent = parent ;
		this.name = name ;
	}
	
	public String getName() {
		return name ;
	}
	
	public void setName(String name) {
		this.name = name ;
	}
	
	public String getValue() {
		return value ;
	}
	
	public void setValue(String value) {
		this.value = value ;
		if(parent != null)
			parent.getSAXHandler().onProperty(getName(), getValue()) ;
	}

	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		FWXUtils.indent(level, w) ;
		FWXUtils.open(this, w) ;
		w.write(" ") ;
		FWXUtils.writeAttribute(ATTRIBUTE_NAME, getName(), w) ;
		w.write(">" + value) ;
		FWXUtils.close(this, w) ;
	}

	@Override
	public FWXHandler getSAXHandler() {
		return new FWXObject.FWXHandler() {
			
			@Override
			public void onValue(String value) {
				setValue(value) ;
			}
			
			@Override
			public void onProperty(String propertyName, String value) {
				//this would be trippy
			}
			
			@Override
			public void onAttribute(String name, String value) {
				if(name.equals(ATTRIBUTE_NAME)) {
					setName(value) ;
				}
			}
			
			@Override
			public FWXObject getObject(String tagName) {
				return null;
			}
		};
	}

	@Override
	public String getTagName() {
		return TAG_NAME ;
	}
	
	
	
}
