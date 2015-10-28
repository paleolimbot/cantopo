package ca.fwe.locations.kml;

import ca.fwe.locations.geometry.LatLon;

public class ImagePlacemark extends PointPlacemark {
	private String imageSrc ;
	private int rotation ;
	
	public ImagePlacemark(String name, String description, String imageSrc, LatLon point) {
		super(name, description, point);
		this.rotation = 0 ;
		this.imageSrc = imageSrc ;
	}
	
	public ImagePlacemark(String name, String description, String relativePath, float rotation, LatLon loc) {
		super(name, description, loc) ;
		this.imageSrc = relativePath ;
		this.rotation = Math.round(rotation) ;
	}

	public String getDescription() {
		String imageTag = "<img style=\"" + this.getImageCSS() + "\" src=\"" + imageSrc + "\" />";
		return super.getDescription() + "<br />" + imageTag ;
	}
	
	public String getImageCSS() {
		if(rotation != 0) {
			return "height:400px; -webkit-transform: rotate(" + rotation + "deg); -moz-transform: rotate(" + rotation + "deg);" ;
		} else {
			return "height:400px;" ;
		}
	}

}
