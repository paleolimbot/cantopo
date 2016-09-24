package net.fishandwhistle.ctexplorer.gps;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.fishandwhistle.ctexplorer.R;
import net.fishandwhistle.ctexplorer.gps.WaypointManager.Waypoint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import fwe.locations.geometry.LatLon;

import com.google.android.gms.maps.model.LatLng;

public class TrackManager {

	private static final String KEY_NEXT_TRACK_NUMBER = "tm_nexttrack" ;
	
	private static final String DB_NAME = "tracks" ;
	private static final int DB_VERSION = 1 ;
	
	private TrackDatabase db ;
	
	public TrackManager(Context c) {
		db = new TrackDatabase(c) ;
	}
	
	public long addPoint(double lat, double lon, double alt, double accuracy, long time) {
		return db.addPoint(lat, lon, alt, accuracy, time) ;
	}
	
	public List<LatLng> getTrackPoints(long trackId) {
		return db.getPoints(trackId) ;
	}
	
	public List<Waypoint> getTrackWaypoints(long trackId) {
		return db.getWaypoints(trackId) ;
	}
	
	public long getActiveTrackId() {
		return db.getActiveTrackId() ;
	}
	
	public boolean endCurrentTrack() {
		return db.endCurrentTrack() > 0 ;
	}
	
	public long createNewTrack() {
		return db.createNewTrack() ;
	}
	
	public TrackPoint getLastPoint() {
		return db.getLastPoint() ;
	}
	
	public List<Track> getTracks() {
		return db.getTracks() ;
	}
	
	public Track getTrack(long trackId) {
		return db.getTrack(trackId) ;
	}
	
	public boolean updateTrack(Track t) {
		return db.updateTrack(t) > 0 ;
	}
	
	public boolean removeTrack(long id) {
		return db.deleteTrack(id) > 0 ;
	}
	
	public double trackDistance(Track t) {
		if(t.points == null)
			t.points = getTrackWaypoints(t.id) ;
		if(t.points != null) {
			if(t.numPoints < 2) {
				return Double.NaN ;
			} else {
				double cumDist = 0 ;
				LatLon lastPt = null ;
				for(int i=0; i<t.points.size(); i++) {
					Waypoint w1 = t.points.get(i) ;
					LatLon p1 = new LatLon(w1.lat, w1.lon) ;
					if(lastPt != null)
						cumDist += lastPt.distanceTo(p1) ;
					lastPt = p1 ;
				}
				return cumDist * 1000 ;
			}
		} else {
			return Double.NaN ;
		}
	}
	
	private static class TrackDatabase extends SQLiteOpenHelper {
	
		
		private Context context ;
		
		public TrackDatabase(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			this.context = context ;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sqlTrackpoints = "CREATE TABLE trackpoints (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "lat REAL,"
					+ "lon REAL,"
					+ "altitude REAL,"
					+ "accuracy REAL,"
					+ "track_id INTEGER,"
					+ "time_date INTEGER)" ;
			String sqlTracks = "CREATE TABLE tracks (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "track_name TEXT,"
					+ "time_date_begin INTEGER,"
					+ "time_date_end INTEGER,"
					+ "description TEXT,"
					+ "near_location TEXT,"
					+ "num_points INTEGER,"
					+ "active_track INTEGER)" ;
			db.execSQL(sqlTrackpoints);
			db.execSQL(sqlTracks);
		}

		private String nextTrackName() {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
			int num = prefs.getInt(KEY_NEXT_TRACK_NUMBER, 1) ;
			prefs.edit().putInt(KEY_NEXT_TRACK_NUMBER, num+1).commit() ;
			String sizeString = new DecimalFormat("000").format(num) ;
			String trackFormat = context.getString(R.string.log_default_track_name) ;
			return String.format(trackFormat, sizeString) ;
		}
		
		private long getActiveTrackId() {
			SQLiteDatabase db = this.getWritableDatabase() ;
			Cursor c = db.query("tracks", new String[] {"_id"}, "active_track=1", new String[] {}, null, null, null) ;
			long out = -1 ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = c.getLong(0) ;
				}
				c.close();
			}
			db.close() ;
			return out ;
		}
		
		public long createNewTrack() {
			this.endCurrentTrack() ;
			
			long out = -1 ;
			SQLiteDatabase db = this.getWritableDatabase() ;
			
			Track t = new Track() ;
			t.activeTrack = true ;
			t.name = nextTrackName() ;
			out = db.insert("tracks", "", fromTrack(t)) ;
			db.close();
			return out ;
		}
		
		public long addPoint(double lat, double lon, double alt, double accuracy, long time) {
			ContentValues cv = new ContentValues() ;
			cv.put("lat", lat);
			cv.put("lon", lon) ;
			cv.put("altitude", alt) ;
			cv.put("accuracy", accuracy);
			cv.put("time_date", time) ;
			long activeTrack = this.getActiveTrackId() ;
			//don't add points if there isn't an active track
			if(activeTrack == -1)
				return -1 ;
			
			cv.put("track_id", activeTrack) ;
			
			Track t = this.getTrack(activeTrack) ;
			
			SQLiteDatabase db = this.getWritableDatabase() ;
			long out = db.insert("trackpoints", "", cv) ;
			db.close();
			
			if(out > 0) {
				t.timeDateEnd = time ;
				if(t.numPoints == 0)
					t.timeDateBegin = time ;
				t.numPoints++ ;
				this.updateTrack(t) ;
			}
			return out ;
		}
		
		private List<LatLng> getPoints(long trackId) {
			List<LatLng> out = new ArrayList<LatLng>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("trackpoints", new String[] {"lat", "lon"}, "track_id=?", new String[] {String.valueOf(trackId)}, null, null, "time_date") ;
			if(c != null) {
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					out.add(new LatLng(c.getDouble(0), c.getDouble(1))) ;
				}
				c.close() ;
			}
			db.close() ;
			return out ;
		}
		
		private List<Waypoint> getWaypoints(long trackId) {
			List<Waypoint> out = new ArrayList<Waypoint>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("trackpoints", new String[] {"lat", "lon", "altitude", "time_date"}, "track_id=?", new String[] {String.valueOf(trackId)}, null, null, "time_date") ;
			if(c != null) {
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					Waypoint w = new Waypoint() ;
					w.lat = c.getDouble(0) ;
					w.lon = c.getDouble(1) ;
					w.time = c.getLong(3) ;
					try {
						w.altitude = c.getDouble(2) ;
					} catch(NumberFormatException e) {
					}
					out.add(w) ;
				}
				c.close() ;
			}
			db.close() ;
			return out ;
		}
		
		private int endCurrentTrack() {
			//if zero or 1 points in current track, delete current track
			long current = this.getActiveTrackId() ;
			if(current != -1) {
				Track t = this.getTrack(current) ;
				if(t.numPoints < 2) {
					deleteTrack(current) ;
				}
			}
			ContentValues cv = new ContentValues() ;
			cv.put("active_track", 0);
			SQLiteDatabase db = this.getWritableDatabase() ;
			int records = db.update("tracks", cv, "1", new String[] {}) ;
			db.close();
			return records ;
		}
		
		private TrackPoint getLastPoint() {
			TrackPoint out = null ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("trackpoints", null, "1", new String[] {}, null, null, "time_date DESC", "1") ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = fromCursor(c) ;
				}
				c.close();
			}
			db.close();
			return out ;
		}
		
		public Track getTrack(long id) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			Track out = null ;
			Cursor c = db.query("tracks", null, "_id=?", new String[] {String.valueOf(id)}, null, null, null, "1") ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = trackFromCursor(c) ;
				}
				c.close() ;
			}
			db.close() ;
			return out ;
		}
		
		public List<Track> getTracks() {
			List<Track> out = new ArrayList<Track>() ;
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("tracks", null, "1", new String[] {}, null, null, "time_date_end DESC") ;
			if(c != null) {
				if(c.getCount() > 0) {
					for(int i=0; i<c.getCount(); i++) {
						c.moveToPosition(i) ;
						out.add(trackFromCursor(c)) ;
					}
				}
				c.close();
			}
			db.close();
			return out ;
		}
		
		public int updateTrack(Track t) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			ContentValues cv = fromTrack(t) ;
			int out = db.update("tracks", cv, "_id=?", new String[] {String.valueOf(t.id)});
			db.close() ;
			return out ;
		}
		
		public int deleteTrack(long id) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int out = db.delete("tracks", "_id=?", new String[] {String.valueOf(id)}) ;
			db.close() ;
			return out ;
		}
		
		private TrackPoint fromCursor(Cursor c) {
			TrackPoint out = new TrackPoint() ;
			c.moveToFirst() ;
			out.id = c.getLong(0) ;
			out.lat = c.getDouble(1) ;
			out.lon = c.getDouble(2) ;
			out.altitude = c.getDouble(3) ;
			out.accuracy = c.getDouble(4) ;
			out.trackId = c.getLong(5) ;
			out.time = c.getLong(6) ;
			return out ;
		}
		
		private Track trackFromCursor(Cursor c) {
			Track out = new Track() ;
			out.id = c.getLong(0) ;
			out.name = c.getString(1) ;
			out.timeDateBegin = c.getLong(2) ;
			out.timeDateEnd = c.getLong(3) ;
			out.description = c.getString(4) ;
			out.nearLocation = c.getString(5) ;
			out.numPoints = c.getInt(6) ;
			out.activeTrack = c.getInt(7) != 0 ;
			return out ;
		}
		
		private ContentValues fromTrack(Track t) {
			ContentValues c = new ContentValues() ;
			if(t.id != -1)
				c.put("_id", t.id);
			c.put("track_name", t.name);
			c.put("time_date_begin", t.timeDateBegin);
			c.put("time_date_end", t.timeDateEnd) ;
			c.put("description", t.description);
			c.put("near_location", t.nearLocation);
			c.put("num_points", t.numPoints) ;
			int activeInt = 0 ;
			if(t.activeTrack)
				activeInt = 1 ;
			c.put("active_track", activeInt);
			return c ;
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//shouldn't be called, only one database version.
		}
		
		
	}
	
	public static class TrackPoint {
		public double lat ;
		public double lon ;
		public long time ;
		public long trackId ;
		public long id ;
		public double altitude ;
		public double accuracy ;
	}

	public static class Track {
		public long id = -1 ;
		public String name = null ;
		public long timeDateBegin = 0 ;
		public long timeDateEnd = 0 ;
		public String description = null ;
		public String nearLocation = null ;
		public int numPoints = 0 ;
		public boolean activeTrack = false ;
		public List<Waypoint> points = null ;
		
		public String toString() {
			return this.name ;
		}
	}
	
}
