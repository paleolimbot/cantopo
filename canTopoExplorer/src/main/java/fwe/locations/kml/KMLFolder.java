package fwe.locations.kml;

import java.util.List;

public class KMLFolder extends KMLEncloser {

	public KMLFolder(String name, String description, List<Placemark> wpts) {
		super("Folder", name, description, wpts);
	}

}
