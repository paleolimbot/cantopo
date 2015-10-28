package ca.fwe.fwx;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FWXSiteList implements FWXObject {

	public static final String TAG_NAME = "sitelist" ;
	
	public static final String ATTRIBUTE_CONTEXT = "context" ;
	
	public List<FWXSite> sites ;
	private String context ;
	private FWXSiteFactory factory ;
	
	public FWXSiteList(FWXSiteFactory factory, String context) {
		this.factory = factory ;
		sites = new ArrayList<FWXSite>() ;
	}
	
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public FWXSite getSite(String slug) {
		for(FWXSite s: sites) {
			if(slug.equals(s.getSlug()))
				return s ;
		}
		return null ;
	}
	
	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		FWXUtils.indent(level, w) ;
		FWXUtils.open(this, w) ;
		w.write(">\n") ;
		for(FWXSite s: sites) {
			s.write(level+1, w) ;
		}
		FWXUtils.close(this, w) ;
	}

	@Override
	public FWXHandler getSAXHandler() {
		return new FWXObject.FWXHandler() {
			
			@Override
			public void onValue(String value) {
				//shouldn't happen
			}
			
			@Override
			public void onProperty(String propertyName, String value) {
				//none
			}
			
			@Override
			public void onAttribute(String name, String value) {
				if(name.equals(ATTRIBUTE_CONTEXT)) {
					setContext(value) ;
				}
			}
			
			@Override
			public FWXObject getObject(String tagName) {
				if(tagName.equals(FWXSite.TAG_NAME)) {
					FWXSite s = factory.getSite() ;
					sites.add(s) ;
					return s ;
				} else {
					return null ;
				}
			}
		};
	}

	@Override
	public String getTagName() {
		return TAG_NAME ;
	}

	
	
}
