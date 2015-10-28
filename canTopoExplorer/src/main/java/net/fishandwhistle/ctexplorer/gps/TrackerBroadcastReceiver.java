package net.fishandwhistle.ctexplorer.gps;

import java.text.DecimalFormat;

import net.fishandwhistle.ctexplorer.MapActivity;
import net.fishandwhistle.ctexplorer.R;
import net.fishandwhistle.ctexplorer.ScaleBarView;
import net.fishandwhistle.ctexplorer.backend.Units;
import net.fishandwhistle.ctexplorer.gps.TrackManager.Track;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class TrackerBroadcastReceiver extends WakefulBroadcastReceiver {

	private static final String PREF_LAST_POINT_LOGGED = "last_point_logged" ;

	private static final String TAG = "TrackerBroadcastReceiver" ;

	public static final String ACTION_LOG_POINT = "net.fishandwhistle.ctexplorer.LOG_POINT" ;
	public static final String ACTION_START_LOGGING = "net.fishandwhistle.ctexplorer.START_LOGGING" ;
	public static final String ACTION_STOP_LOGGING = "net.fishandwhistle.ctexplorer.STOP_LOGGING" ;

	public static final String ACTION_NEW_POINT = "net.fishandwhistle.ctexplorer.NEW_TRACK_POINT" ;
	public static final String ACTION_POINT_ACQUIRED = "net.fishandwhistle.ctexplorer.POINT_ACQUIRED" ;
	public static final String ACTION_LOGGING_STATE_CHANGED = "net.fishandwhistle.ctexplorer.LOGGING_STATE_CHANGED" ;

	public static final String EXTRA_POINTS = "points" ;

	public static final String ACTION_ENSURE_STATE = "net.fishandwhistle.ctexplorer.ENSURE_STATE" ;

	private static final int TRACKING_NOTIFICATION_ID = 11 ;

	private TrackManager tracks ;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction() ;
		Log.i(TAG, "receiving intent with action " + action) ;
		if(action.equals(ACTION_LOG_POINT)) {
			this.logPoint(context);
		} else if(action.equals(ACTION_STOP_LOGGING)) {
			this.setTracking(context, false);
		} else if(action.equals(ACTION_START_LOGGING)) {
			this.setTracking(context, true);
		} else if(action.equals(ACTION_ENSURE_STATE)) {
			ensureNotificationState(context) ;
			ensureAlarmState(context) ;
		} else if(action.equals(ACTION_POINT_ACQUIRED)) {
			onPointAcquired(context, intent) ;
		} else if(action.equals("android.intent.action.BOOT_COMPLETED")) {
			//on boot stop tracking
			this.setupTrackManager(context);
			tracks.endCurrentTrack() ;
			ensureNotificationState(context) ;
			ensureAlarmState(context) ;
		} else {
			Log.i(TAG, "request not handled by this broadcast receiver") ;
		}
	}

	private void setupTrackManager(Context context) {
		if(tracks == null)
			tracks = new TrackManager(context) ;
	}

	private void setTracking(Context context, boolean state) {
		this.setupTrackManager(context);
		boolean currentState = tracks.getActiveTrackId() != -1 ;
		if(state != currentState) {
			if(state) {
				tracks.createNewTrack() ;
			} else {
				tracks.endCurrentTrack() ;
			}
		}

		ensureNotificationState(context) ;
		ensureAlarmState(context) ;
		Intent i = new Intent(ACTION_LOGGING_STATE_CHANGED) ;
		context.sendBroadcast(i);
	}

	private void ensureNotificationState(Context context) {
		this.setupTrackManager(context) ;
		long trackId = tracks.getActiveTrackId() ;
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;
		Log.i(TAG, "ensuring notification state for track id: " + trackId) ;
		
		if(trackId != -1) {
			Track t = tracks.getTrack(trackId) ;
			double distanceM = tracks.trackDistance(t) ;
			if(Double.isNaN(distanceM))
				distanceM = 0 ;
			String[] units = ScaleBarView.UNITS_LG ;
			String unit = units[Units.getUnitCategoryConstant(context)] ;
			double valueU = Units.fromSI(distanceM, unit) ;
			
			String num = new DecimalFormat("0.0").format(valueU) ;
			String distanceText = String.format(context.getString(R.string.map_measure_distance), num, unit) ;
			
			Notification.Builder b = new Notification.Builder(context) ;
			b.setContentTitle(context.getString(R.string.app_name)) ;
			b.setContentText(String.format(context.getString(R.string.log_notification_message), t.numPoints, distanceText)) ;
			b.setOngoing(true) ;
			b.setSmallIcon(R.drawable.ic_stat_tracking) ;
			Intent i = new Intent(context, MapActivity.class) ;
			b.setContentIntent(PendingIntent.getActivity(context, 8, i, PendingIntent.FLAG_UPDATE_CURRENT)) ;
			nm.notify(TRACKING_NOTIFICATION_ID, b.getNotification());
		} else {
			nm.cancel(TRACKING_NOTIFICATION_ID);
		}
	}

	private void ensureAlarmState(Context context) {
		this.setupTrackManager(context) ;
		long trackId = tracks.getActiveTrackId() ;

		PendingIntent pi = this.getLogPendingIntent(context) ;
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE) ;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
		Log.i(TAG, "ensuring alarm state for track id: " + trackId) ;
		if(trackId != -1) {
			long lastLogged = prefs.getLong(PREF_LAST_POINT_LOGGED, 0) ;
			int minutes = Integer.valueOf(prefs.getString("xml_log_point_duration", "3")) ;
			long updateI = minutes*60*1000 ;
			am.setRepeating(AlarmManager.RTC_WAKEUP, lastLogged+updateI, updateI, pi);
			prefs.edit()
				.putLong(PREF_LAST_POINT_LOGGED, System.currentTimeMillis())
				.commit() ;
			Log.i(TAG, "set alarm to log point at update interval") ;
		} else {
			am.cancel(pi);
			prefs.edit().putLong(PREF_LAST_POINT_LOGGED, 0).commit() ;
			LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE) ;
			lm.removeUpdates(getAcquiredPendingIntent(context));
		}
	}

	private void onPointAcquired(Context context, Intent intent) {
		this.setupTrackManager(context);
		Location l = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED) ;
		if(l != null) {
			Log.i(TAG, "point received: lat:" + l.getLatitude() + " + lon:" + l.getLongitude() + " accuracy:" + l.getAccuracy()) ;
			long id = tracks.addPoint(l.getLatitude(),
					l.getLongitude(),
					l.getAltitude(),
					l.getAccuracy(),
					l.getTime()) ;
			Log.i(TAG, "added point with id " + id) ;
			Intent i = new Intent(TrackerBroadcastReceiver.ACTION_NEW_POINT) ;
			context.sendBroadcast(i) ;
			this.ensureNotificationState(context);
		} else {
			Log.e(TAG, "no location extra on point acquired intent!") ;
		}
	}
	
	private void logPoint(Context context) {
		Log.i(TAG, "log point started") ;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
		prefs.edit()
			.putLong(PREF_LAST_POINT_LOGGED, System.currentTimeMillis())
			.commit() ;
		
		PendingIntent pi = getAcquiredPendingIntent(context) ;
		LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE) ;
		Criteria criteria = new Criteria() ;
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		lm.requestSingleUpdate(criteria, pi);
	}

	private PendingIntent getAcquiredPendingIntent(Context context) {
		Intent newPoint = new Intent(ACTION_POINT_ACQUIRED) ;
		return PendingIntent.getBroadcast(context, 0, newPoint, PendingIntent.FLAG_UPDATE_CURRENT) ;
	}
	
	private PendingIntent getLogPendingIntent(Context context) {
		Intent i = new Intent(context, TrackerBroadcastReceiver.class) ;
		i.setAction(ACTION_LOG_POINT) ;
		return PendingIntent.getBroadcast(context, 11, i, PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
