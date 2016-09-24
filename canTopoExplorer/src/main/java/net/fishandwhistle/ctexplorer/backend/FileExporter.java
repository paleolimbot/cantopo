package net.fishandwhistle.ctexplorer.backend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.fishandwhistle.ctexplorer.gps.TrackManager;
import net.fishandwhistle.ctexplorer.gps.TrackManager.Track;
import net.fishandwhistle.ctexplorer.gps.WaypointManager.Waypoint;
import android.content.Context;
import android.util.Log;
import fwe.locations.geometry.LatLon;
import fwe.locations.gpx.GPXFile;
import fwe.locations.gpx.GPXTrack;
import fwe.locations.gpx.GPXTrackpoint;
import fwe.locations.gpx.GPXWaypoint;
import fwe.locations.kml.KMLDocument;
import fwe.locations.kml.KMLFolder;
import fwe.locations.kml.LinePlacemark;
import fwe.locations.kml.Placemark;
import fwe.locations.kml.PointPlacemark;

public class FileExporter {
	
	private List<Waypoint> waypoints ;
	private List<Track> tracks ;
	private TrackManager trackManager ;
	
	public FileExporter(Context context, List<Waypoint> waypoints, List<Track> tracks) {
		if(waypoints != null) {
			this.waypoints = waypoints ;
		} else {
			this.waypoints = new ArrayList<Waypoint>() ;
		}
		
		if(tracks != null) {
			this.tracks = tracks ;
			this.trackManager = new TrackManager(context) ;
		} else {
			this.tracks = new ArrayList<Track>() ;
		}
	}
	
	public void writeKML(File file) throws IOException {
		KMLDocument d = this.makeKML() ;
		d.writeFile(file);
		Log.i("FileExporter", "wrote KML to file " + file) ;
	}
	
	public void writeGPX(File file) throws IOException {
		GPXFile f = this.makeGPX() ;
		f.writeFile(file);
		Log.i("FileExporter", "wrote GPX to file " + file + " (" + file.length() + " bytes)") ;
	}
	
	private KMLDocument makeKML() {
		KMLDocument k = new KMLDocument() ;
		List<Placemark> wpts = new ArrayList<Placemark>() ;
		for(Waypoint w: waypoints) {
			PointPlacemark pp = new PointPlacemark(w.name, w.description, new LatLon(w.lat, w.lon)) ;
			wpts.add(pp) ;
		}
		List<Placemark> trks = new ArrayList<Placemark>() ;
		for(Track t: tracks) {
			t.points = trackManager.getTrackWaypoints(t.id) ;
			LinePlacemark lp = new LinePlacemark() ;
			lp.setName(t.name);
			lp.setDescription(t.description);
			lp.setPoints(fromWaypoints(t.points)) ;
			trks.add(lp) ;
		}
		if(wpts.size() > 0)
			k.add(new KMLFolder("Waypoints", null, wpts));
		if(trks.size() > 0)
			k.add(new KMLFolder("Tracks", null, trks));
		return k ;
	}

	private GPXFile makeGPX() {
		GPXFile g = new GPXFile() ;
		for(Waypoint w: waypoints) {
			GPXWaypoint wpt = new GPXWaypoint(fromWaypoint(w), w.time) ;
			wpt.name = w.name ;
			wpt.description = w.description ;
			g.content.add(wpt) ;
		}
		for(Track t: tracks) {
			GPXTrack trk = new GPXTrack() ;
			trk.name = t.name ;
			trk.description = t.description ;
			t.points = trackManager.getTrackWaypoints(t.id) ; 
			if(t.points != null) {
				for(Waypoint w: t.points) {
					trk.points.add(new GPXTrackpoint(fromWaypoint(w), w.time)) ;
				}
			}
			g.content.add(trk) ;
		}

		return g ;
	}
	
	private List<LatLon> fromWaypoints(List<Waypoint> list) {
		List<LatLon> out = new ArrayList<LatLon>() ;
		for(Waypoint w: list)
			out.add(fromWaypoint(w)) ;
		return out ;
	}
	
	private LatLon fromWaypoint(Waypoint w) {
		if(!Double.isNaN(w.altitude))
			return new LatLon(w.lat, w.lon, w.altitude) ;
		else
			return new LatLon(w.lat, w.lon) ;
	}
	
}
