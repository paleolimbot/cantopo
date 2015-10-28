package ca.fwe.fwx;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.fwe.locations.geometry.Bounds;
import ca.fwe.locations.geometry.LatLon;

public abstract class FWXSite implements FWXObject {

	public static final String TAG_NAME = "site" ;
	public static final String ATTRIBUTE_SLUG = "slug" ;
	public static final String ATTRIBUTE_CREATED = "created" ;
	public static final String ATTRIBUTE_LAST_UPDATED = "updated" ;
	public static final String PROPERTY_NAME = "name" ;
	
	private String slug ;
	private String name ;
	private long created ;
	private long lastUpdated ;
	private Bounds location ;
	public List<FWXObject> objects ;

	public FWXSite() {
		lastUpdated = -1 ;
		objects = new ArrayList<FWXObject>() ;
	}

	public String getSlug() {
		return slug;
	}

	public String getName() {
		return name;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Bounds getLocation() {
		return location;
	}

	public void setLocation(Bounds location) {
		this.location = location;
	}

	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		FWXUtils.indent(level, w) ;
		FWXUtils.open(this, w) ;
		if(getSlug() != null) {
			w.write(" ") ;
			FWXUtils.writeAttribute(ATTRIBUTE_SLUG, getSlug(), w) ;
		}
		if(getLastUpdated() != -1) {
			w.write(" ") ;
			FWXUtils.writeAttribute(ATTRIBUTE_LAST_UPDATED, getLastUpdated(), w) ;
		}
		if(getCreated() != -1) {
			w.write(" ") ;
			FWXUtils.writeAttribute(ATTRIBUTE_CREATED, getCreated(), w) ;
		}
		w.write(">\n") ;
		if(getName() != null)
			FWXUtils.writeProperty(level+1, PROPERTY_NAME, getName(), w) ;

		if(objects != null) {
			for(FWXObject o: objects) {
				o.write(level+1, w) ;
			}
		}
		FWXUtils.indent(level, w) ;
		FWXUtils.close(this, w) ;
	}

	@Override
	public FWXHandler getSAXHandler() {
		return new FWXObject.FWXHandler() {

			@Override
			public void onValue(String value) {
				//no value
			}

			@Override
			public void onProperty(String propertyName, String value) {
				if(propertyName.equals(PROPERTY_NAME)) {
					setName(value) ;
				}
			}

			@Override
			public void onAttribute(String name, String value) {
				if(name.equals(ATTRIBUTE_SLUG)) {
					setSlug(value) ;
				} else if(name.equals(ATTRIBUTE_LAST_UPDATED)) {
					setLastUpdated(Long.valueOf(value)) ;
				} else if(name.equals(ATTRIBUTE_CREATED)) {
					setCreated(Long.valueOf(value)) ;
				}
			}

			@Override
			public FWXObject getObject(String tagName) {
				if(tagName.equals(LatLon.TAG_NAME)) {
					location = new Bounds() ; 
					return location ;
				} else {
					FWXObject o = FWXSite.this.getObject(tagName) ;
					objects.add(o) ;
					return o ;
				}
			}
		};
	}

	@Override
	public String getTagName() {
		return TAG_NAME ;
	}

	public abstract FWXObject getObject(String tagName) ;

	public static String slugFromName(String name) {
		String in = name.trim().toLowerCase(Locale.CANADA) ;
		char[] newString = new char[in.length()] ;
		for(int i=0; i<in.length(); i++) {
			if(acceptChar(in.charAt(i))) {
				newString[i] = in.charAt(i) ;
			} else {
				newString[i] = '-' ;
			}
		}
		return new String(newString) ;
	}

	private static boolean acceptChar(char in) {
		switch(in) {
		case '/':
			return false ;
		case '\\':
			return false ;
		case '?':
			return false ;
		case '%' :
			return false ;
		case '*':
			return false ;
		case ':':
			return false ;
		case '|':
			return false ;
		case '"':
			return false ;
		case '>':
			return false ;
		case '<':
			return false ;
		case ' ':
			return false ;
		case '\'':
			return false ;
		default:
			return true ;

		}
	}
	
	public boolean equals(Object o) {
		if(o != null) {
			if(o instanceof FWXSite) {
				if(((FWXSite) o).getSlug() == this.getSlug()) 
					return true ;
			}
		}
		return false ;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}
	
}
