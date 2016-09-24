package fwe.locations.kml;

import java.io.BufferedWriter;
import java.io.IOException;

import fwe.locations.geometry.RotatableBounds;

public class GroundOverlay extends Placemark {

	private RotatableBounds bounds ;
	private String relativePath ;	

	public GroundOverlay(String name, String description, RotatableBounds bounds, String relativePath) {
		super(name, description);
		this.bounds = bounds;
		this.relativePath = relativePath;
	}



	public RotatableBounds getBounds() {
		return bounds;
	}

	public String getRelativePath() {
		return relativePath;
	}

	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		//	<GroundOverlay>
		//		<name>tile1</name>
		//		<Icon>
		//			<href>chart pirate/ti-0-0.jpg</href>
		//			<viewBoundScale>0.75</viewBoundScale>
		//		</Icon>
		//		<LatLonBox>
		//			<north>48.78169552055088</north>
		//			<south>48.65408447288853</south>
		//			<east>-123.179751425456</east>
		//			<west>-123.4157001667375</west>
		//		</LatLonBox>
		//	</GroundOverlay>

		writeTag(w, level, "GroundOverlay", null, null) ;
		this.writeNameAndDescription(level, w) ;
		writeTag(w, level+1, "Icon", null, null) ;
		writeTag(w, level+2, "href", null, this.getRelativePath()) ;
		writeTag(w, level+2, "viewBoundScale", null, "0.75") ;
		closeTag(w, level+1, "Icon") ;
		
		writeTag(w, level+1, "LatLonBox", null, null) ;
		writeTag(w, level+2, "north", null, bounds.getMaxY()) ;
		writeTag(w, level+2, "south", null, bounds.getMinY()) ;
		writeTag(w, level+2, "east", null, bounds.getMaxX()) ;
		writeTag(w, level+2, "west", null, bounds.getMinX()) ;
		if(bounds.getRotation() != 0)
			writeTag(w, level+2, "rotation", null, bounds.getRotation()) ;
		closeTag(w, level+1, "LatLonBox") ;
		closeTag(w, level, "GroundOverlay") ;
	}

}
