package net.fishandwhistle.ctexplorer.gps;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fishandwhistle.ctexplorer.R;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;
import ca.fwe.locations.geometry.Bounds;
import ca.fwe.locations.geometry.XY;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WaypointManager {
	
	private static final String KEY_WAYPOINT_NUMBER = "wm_newwaypoint" ;
	
	private static final String TAG = "WaypointManager" ;
	private static final String DB_NAME = "waypoints" ;
	private static final int DB_VERSION = 1 ;
	private static final long CLEAN_TIME = 5*60*1000 ;
	private static final int CLEAN_CACHE_SIZE = 25 ;

	private WaypointDatabase db ;

	private GoogleMap map ;
	private List<Marker> markers ;
	private Map<String, Waypoint> waypoints ;
	private long lastClean  ;
	private Bounds lastBounds ;
	private boolean enabled ;
	private Context context ;

	public WaypointManager(Context context, GoogleMap map) {
		db = new WaypointDatabase(context) ;
		this.map = map ;
		markers = new ArrayList<Marker>() ;
		waypoints = new HashMap<String, Waypoint>() ;
		lastClean = System.currentTimeMillis() ;
		enabled = false ;
		this.context = context ;
	}

	public String nextWaypointName() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
		int num = prefs.getInt(KEY_WAYPOINT_NUMBER, 1) ;
		String sizeString = new DecimalFormat("000").format(num) ;
		String waypointFormat = context.getString(R.string.waypoint_default_name) ;
		return String.format(waypointFormat, sizeString) ;
	}
	
	public boolean cleanTemporary() {
		int out = db.cleanTemporary() ;
		if(out > 0 && map != null) {
			this.removeAllMarkers();
			this.refreshMarkers();
		}
		return out > 0 ;
	}
	
	public long maxId() {
		return db.maxId() ;
	}
	
	public int size() {
		return db.size() ;
	}
	
	public long add(Waypoint w) {
		if(w.time == 0)
			w.time = System.currentTimeMillis() ;
		long out = db.add(w) ;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
		if(this.nextWaypointName().equals(w.name)) {
			int num = prefs.getInt(KEY_WAYPOINT_NUMBER, 1) ;
			prefs.edit().putInt(KEY_WAYPOINT_NUMBER, num+1).commit() ;
		}
		
		if(map != null && out > 0)
			refreshMarkers() ;
		Log.i(TAG, "adding waypoint " + out) ;
		return out ;
	}

	public boolean update(Waypoint w) {
		boolean out = db.update(w) > 0 ;
		if(map != null && out)
			this.removeAllMarkers();
			refreshMarkers() ;
		Log.i(TAG, "updated " + out + " waypoints") ;
		return out ;
	}

	public Waypoint get(long id) {
		return db.getWaypoint(id) ;
	}

	public boolean remove(long id) {
		boolean out = db.remove(id) > 0 ;
		if(map != null && out) {
			this.removeAllMarkers();
			refreshMarkers() ;
		}
		Log.i(TAG, "removed " + out + " waypoints") ;
		return out ;
	}
	
	public List<Waypoint> getAll() {
		return db.getAll() ;
	}

	public void setEnabled(boolean state) {
		if(state != enabled) {
			enabled = state ;
			if(enabled)
				this.refreshMarkers();
			else
				this.removeAllMarkers();
		}
	}
	
	public boolean isEnabled() {
		return enabled ;
	}
	
	public void removeAllMarkers() {
		this.cleanMarkers(new Bounds(0,0,0,0)); //lazy, but hey
	}

	public void refreshMarkers() {
		if(lastBounds != null)
			onBoundsUpdated(lastBounds) ;
	}

	public void onBoundsUpdated(Bounds b) {
		lastBounds = b ;
		if(enabled) {
			Bounds newB = extendBounds(b, 2) ;
			List<Waypoint> wpts = db.queryBounds(newB) ;
			for(Waypoint w: wpts) {
				if(!waypointLoaded(w.id)) {
					addToMap(w) ;
				}
			}
			if((System.currentTimeMillis() - lastClean > CLEAN_TIME) || markers.size() > CLEAN_CACHE_SIZE)
				cleanMarkers(newB) ;
		}
		Log.i(TAG, "updated bounds, current loaded waypoints: " + markers.size()) ;
	}

	private void cleanMarkers(Bounds b) {
		List<Marker> toRemove = new ArrayList<Marker>() ;
		for(Marker m: markers) {
			XY pos = new XY(m.getPosition().longitude, m.getPosition().latitude) ;
			if(!b.contains(pos))
				toRemove.add(m) ;
		}

		for(Marker m: toRemove) {
			removeFromMap(m) ;
		}
		lastClean = System.currentTimeMillis() ;
	}

	private Bounds extendBounds(Bounds b, double factor) {
		XY centre = b.getCentre() ;
		double cx = centre.x() ;
		double cy = centre.y();
		double height = b.height()*factor ;
		double width = b.width()*factor ;
		return new Bounds(cx-width/2, cx+width/2, cy-height/2, cy+height/2) ;
	}

	private void addToMap(Waypoint w) {
		MarkerOptions mo = new MarkerOptions() ;
		if(w.name != null && w.name.length() > 0)
			mo.title(w.name) ;
		if(w.description != null && w.description.length() > 0)
			mo.snippet(w.description) ;
		mo.position(new LatLng(w.lat, w.lon)) ;
		mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin1)) ;
		//TODO custom icons
		mo.draggable(true) ;
		Marker m = map.addMarker(mo) ;
		markers.add(m) ;
		waypoints.put(m.getId(), w) ;
	}

	private void removeFromMap(Marker m) {
		markers.remove(m) ;
		waypoints.remove(m.getId()) ;
		m.remove();
	}

	private boolean waypointLoaded(long wptId) {
		for(Marker m: markers) {
			Waypoint w = getWaypoint(m) ;
			if(w.id == wptId)
				return true ;
		}
		return false ;
	}

	public Waypoint getWaypoint(Marker m) {
		if(waypoints.containsKey(m.getId())) {
			return waypoints.get(m.getId()) ;
		} else {
			return null ;
		}
	}

	private static class WaypointDatabase extends SQLiteOpenHelper {

		public WaypointDatabase(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.i(TAG, "creating waypoint database") ;
			String sql = "CREATE TABLE waypoints (`_id` INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "`lat` REAL,"
					+ "`lon` REAL,"
					+ "`altitude` REAL,"
					+ "`name` TEXT,"
					+ "`description` TEXT,"
					+ "`icon` TEXT,"
					+ "`icon_type` TEXT,"
					+ "`temporary` INTEGER,"
					+ "`time_date` INTEGER,"
					+ "`category` TEXT)" ;
			db.execSQL(sql);
		}

		public Waypoint getWaypoint(long id) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.query("waypoints", null, "_id=?", new String[] {String.valueOf(id)}, null, null, null) ;
			Waypoint out = null ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = waypointFrom(c) ;
				}
				c.close() ;
			}

			db.close();
			return out ;
		}

		public int size() {
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.rawQuery("SELECT COUNT(*) FROM waypoints WHERE 1", new String[] {}) ;
			int out = 0 ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = c.getInt(0) ;
				}
			}
			db.close() ;
			return out ;
		}
		
		public long maxId() {
			SQLiteDatabase db = this.getReadableDatabase() ;
			Cursor c = db.rawQuery("SELECT MAX(_id) FROM waypoints WHERE 1", new String[] {}) ;
			long out = 0 ;
			if(c != null) {
				if(c.getCount() > 0) {
					c.moveToFirst() ;
					out = c.getInt(0) ;
				}
			}
			db.close() ;
			return out ;
		}
		
		public int update(Waypoint w) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			ContentValues cv = cvFrom(w) ;
			int out = db.update("waypoints", cv, "_id=?", new String[] {String.valueOf(w.id)}) ;
			db.close();
			return out ;
		}

		public long add(Waypoint w) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			ContentValues values = cvFrom(w) ;
			values.remove("_id") ;
			long out = db.insert("waypoints", "", values) ;
			db.close() ;
			return out ;
		}

		public int remove(long id) {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int out = db.delete("waypoints", "_id=?", new String[] {String.valueOf(id)}) ;
			db.close() ;
			return out ;
		}

		public List<Waypoint> queryBounds(Bounds b) {
			SQLiteDatabase db = this.getReadableDatabase() ;
			List<Waypoint> out = new ArrayList<Waypoint>() ;
			Cursor c = db.query("waypoints", null,
					"lat>=? AND lat<=? AND lon>=? AND lon<=?",
					new String[] {String.valueOf(b.getMinY()), String.valueOf(b.getMaxY()), String.valueOf(b.getMinX()), String.valueOf(b.getMaxX())},
					null, null, null) ;
			if(c != null) {
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					out.add(waypointFrom(c)) ;
				}
				c.close() ;
			}
			db.close();
			return out ;
		}
		
		public List<Waypoint> getAll() {
			SQLiteDatabase db = this.getReadableDatabase() ;
			List<Waypoint> out = new ArrayList<Waypoint>() ;
			Cursor c = db.query("waypoints", null,
					"temporary=?",
					new String[] {"0"},
					null, null, "time_date DESC") ;
			if(c != null) {
				for(int i=0; i<c.getCount(); i++) {
					c.moveToPosition(i) ;
					out.add(waypointFrom(c)) ;
				}
				c.close() ;
			}
			db.close();
			return out ;
		}
		
		public int cleanTemporary() {
			SQLiteDatabase db = this.getWritableDatabase() ;
			int out = db.delete("waypoints", "temporary=?", new String[]{"1"}) ;
			db.close() ;
			return out ;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//shouldn't be called, only one database version.
		}

		private static ContentValues cvFrom(Waypoint w) {
			ContentValues cv = new ContentValues() ;
			cv.put("_id", w.id);
			cv.put("lat", w.lat);
			cv.put("lon", w.lon);
			if(Double.isNaN(w.altitude))
				cv.put("altitude", "NaN") ;
			else
				cv.put("altitude", w.altitude);
			cv.put("name", w.name);
			cv.put("description", w.description);
			cv.put("icon", w.icon);
			cv.put("icon_type", w.iconType);
			int temp = 0 ;
			if(w.temporary)
				temp = 1 ;
			cv.put("temporary", temp);
			cv.put("time_date", w.time);
			cv.put("category", w.category);
			return cv ;
		}

		private static Waypoint waypointFrom(Cursor c) {
			Waypoint w = new Waypoint() ;
			w.id = c.getLong(0) ;
			w.lat = c.getDouble(1) ;
			w.lon = c.getDouble(2) ;
			w.altitude = c.getDouble(3) ;
			w.name = c.getString(4) ;
			w.description = c.getString(5) ;
			w.icon = c.getString(6) ;
			w.iconType = c.getString(7) ;
			w.temporary = c.getInt(8) == 1 ;
			w.time = c.getLong(9) ;
			w.category = c.getString(10) ;
			return w ;
		}

	}

	public static class Waypoint {
		public long id = -1 ;
		public double lat = Double.NaN;
		public double lon = Double.NaN;
		public double altitude = Double.NaN;
		public String name = null;
		public String description = null ;
		public String icon = null ;
		public String iconType = null ;
		public boolean temporary = false ;
		public long time = 0 ;
		public String category = null;
	}



}
