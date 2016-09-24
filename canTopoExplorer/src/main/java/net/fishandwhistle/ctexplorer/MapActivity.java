package net.fishandwhistle.ctexplorer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import net.fishandwhistle.ctexplorer.SearchDialog.OnAddressSelectedListener;
import net.fishandwhistle.ctexplorer.backend.HorizontalProgressDialog;
import net.fishandwhistle.ctexplorer.backend.LocationFormat;
import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import net.fishandwhistle.ctexplorer.backend.NTSTileNameSearcher;
import net.fishandwhistle.ctexplorer.backend.ToporamaLoader;
import net.fishandwhistle.ctexplorer.backend.ToporamaLoader.ErrorClass;
import net.fishandwhistle.ctexplorer.backend.ToporamaLoader.ProgressType;
import net.fishandwhistle.ctexplorer.backend.Units;
import net.fishandwhistle.ctexplorer.gps.TrackManager;
import net.fishandwhistle.ctexplorer.gps.TrackManager.Track;
import net.fishandwhistle.ctexplorer.gps.TrackerBroadcastReceiver;
import net.fishandwhistle.ctexplorer.gps.WaypointManager;
import net.fishandwhistle.ctexplorer.gps.WaypointManager.Waypoint;
import net.fishandwhistle.ctexplorer.maptools.MapTool;
import net.fishandwhistle.ctexplorer.maptools.MeasureAreaTool;
import net.fishandwhistle.ctexplorer.maptools.MeasureAreaTool.OnNewMeasuredAreaListener;
import net.fishandwhistle.ctexplorer.maptools.MeasureTool;
import net.fishandwhistle.ctexplorer.maptools.MeasureTool.OnNewMeasuredDistanceListener;
import net.fishandwhistle.ctexplorer.tiles.NTSDownloadedImagesGrid;
import net.fishandwhistle.ctexplorer.tiles.NTSGrid;
import net.fishandwhistle.ctexplorer.tiles.NTSGridMajor;
import net.fishandwhistle.ctexplorer.tiles.NTSImageTile;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import fwe.locations.geometry.Bounds;
import fwe.locations.geometry.LatLon;
import fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends Activity implements GoogleMap.OnCameraChangeListener, 
ToporamaLoader.OnToporamaLoadListener, 
GoogleMap.OnMapClickListener,
GoogleMap.OnMapLongClickListener,
        OnMapReadyCallback {

	private static final String TAG = "MapActivity" ;
	private static final String PREF_SAVED_CAMERA_POSITION = "camerapos" ;
	private static final String PREF_GRID_ON = "grid_state" ;
	private static final String PREF_TOPO_ON = "topo_state" ;
	private static final String PREF_MAPTYPE = "maptype" ;
	private static final String PREF_MAP_TRANSPARENCY = "topo_transparency" ;
	private static final String PREF_MAP_MYLOCATION = "location_enabled" ;
    private static final int PERMISSION_REQUEST_SHOW_ON_MAP = 1 ;
    private static final int PERMISSION_REQUEST_TRACKME = 2 ;

	private GoogleMap map ;
	private NTSGrid grid ;
	private NTSGridMajor gridMajor ; 
	private NTSImageTile images ;
	private NTSDownloadedImagesGrid downloadedImages ;
	private FrameLayout layout ;
	private LatLon longPressLatLon ;
	private NTSMapSheet longPressMapSheet ;

	private ToporamaLoader loader ;
	private HorizontalProgressDialog loaderProgress ;
	private boolean loaderProgressShowing ;
	private ToporamaLoader.ProgressType currentLoaderTask ;

	private TransparencyDialog transparencyDialog ;
	private boolean transparencyDialogShowing ;

	private WaypointDialog waypointDialog ;
	private boolean waypointDialogShowing ;

	private SearchDialog searchDialog ;
	private boolean searchDialogShowing ;
	private long searchWaypointId ;

	private NTSTileNameSearcher names ;

	private double screenHeight ; //in metres
	private int screenHeightPx ;

	private MapCacheManager cache ;

	private MapTool currentTool ;
	private MeasureTool measureDistanceTool ;
	private MeasureAreaTool measureAreaTool ;

	private TrackManager trackManager ;
	private Polyline trackPolyline ;
	private Polyline trackViewPolyline ;
	private Marker trackViewStartMarker ;
	private long trackViewId ;

	private WaypointManager waypoints ;

	private BroadcastReceiver refreshReceiver ;
	private IntentFilter refreshIntentFilter ;
	private IntentFilter logPointFilter ;

	private ScaleBarView scaleBar ;
	private TextView statusText ;
	
	private Uri pendingUri ;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		boolean upgraded = Upgrader.upgrade(this) ; //also calls onApplicationStart in Upgrader
		screenHeight = 0 ;
		screenHeightPx = 0 ;
		layout = (FrameLayout)findViewById(R.id.frame_layout) ;
		//check available memory
		ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE) ;
		int availableMemmory = am.getMemoryClass() ;
		Log.i(TAG, "Checking memmory: " + availableMemmory + " MB free.") ;

		loaderProgressShowing = false ;
		loaderProgress = new HorizontalProgressDialog(this) ;
		loaderProgress.setMax(100);
		loaderProgress.setTitle(getString(R.string.map_prog_title));
		loaderProgress.setMessage(getString(R.string.map_prog_message));
		loaderProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if(loader != null)
					loader.cancel();
				loaderProgressShowing = false ;
			}
		});

		names = new NTSTileNameSearcher(this) ;
		cache = new MapCacheManager(this) ;
		trackManager = new TrackManager(this) ;
		trackViewId = -1 ;

		refreshReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "receiving intent with action " + intent.getAction()) ;
				if(intent.getAction().equals(ToporamaLoaderService.ACTION_DOWNLOAD_COMPLETE)) {
					if(map != null && downloadedImages != null && images != null) {
						String result = intent.getStringExtra(ToporamaLoaderService.EXTRA_DOWNLOAD_ERROR) ;
						ErrorClass res = ErrorClass.valueOf(result) ;
						Uri data = intent.getData() ;
						if(data != null) {
							String sheetId = data.getSchemeSpecificPart() ;
							NTSMapSheet sheet = NTSMapSheet.getSheetById(sheetId) ;
							if(sheet != null) {
								onToporamaLoad(sheet, res, 0) ;
								return ;
							}
						}
					}
				} else if(intent.getAction().equals(ToporamaLoaderService.ACTION_DOWNLOAD_STARTED)) {
					//do nothing, but this does get called
					return ;
				} else if(intent.getAction().equals(TrackerBroadcastReceiver.ACTION_NEW_POINT)) {
					if(map != null) {
						setupTrackingPolyline();
						return ;
					}
				} else if(intent.getAction().equals(TrackerBroadcastReceiver.ACTION_LOGGING_STATE_CHANGED)) {
					if(map != null) {
						setupTrackingPolyline();
						invalidateOptionsMenu() ;
						return ;
					}
				}
				Log.e(TAG, "could not process data from refreshReceiver " + intent.getData()) ;
			}

		} ;
		refreshIntentFilter = new IntentFilter(ToporamaLoaderService.ACTION_DOWNLOAD_COMPLETE) ;
		refreshIntentFilter.addAction(ToporamaLoaderService.ACTION_DOWNLOAD_STARTED);
		refreshIntentFilter.addDataScheme("nts");
		logPointFilter = new IntentFilter(TrackerBroadcastReceiver.ACTION_NEW_POINT) ;
		logPointFilter.addAction(TrackerBroadcastReceiver.ACTION_LOGGING_STATE_CHANGED);

		Intent ensureLoggingState = new Intent(this, TrackerBroadcastReceiver.class) ;
		ensureLoggingState.setAction(TrackerBroadcastReceiver.ACTION_ENSURE_STATE) ;
		sendBroadcast(ensureLoggingState) ;

		scaleBar = (ScaleBarView)findViewById(R.id.map_scale_bar) ;
		statusText = (TextView)findViewById(R.id.map_statustext) ;

		GoogleMapOptions options = new GoogleMapOptions() ;
		options.tiltGesturesEnabled(false) ;
		CameraPosition camera = this.getStoredCameraPosition() ;
		if(camera != null)
			options.camera(camera) ;
		MapFragment fragment = MapFragment.newInstance(options) ;
		getFragmentManager().beginTransaction()
		.replace(R.id.map, fragment)
		.commit();

		//if upgraded, show about dialog
		if(upgraded)
			this.showAboutDialog();
		
		if(this.getIntent().getData() != null) {
			this.onNewIntent(this.getIntent());
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.i(TAG, "processing new intent: " + intent.getData()) ;
		if(intent != null) {
			Uri data = intent.getData() ;
			if(data != null) {
				if(data.getScheme().equals("geo")) {
					if(map != null) {
						this.loadGeoUri(data);
					} else {
						pendingUri = data ;
					}
				}
			}
		}
	}

	public void onResume() {
		super.onResume();
        MapFragment fragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)) ;
        fragment.getMapAsync(this);

		if(loaderProgressShowing)
			loaderProgress.show();
		if(transparencyDialogShowing)
			transparencyDialog.show();
		if(searchDialogShowing)
			searchDialog.show();
		if(waypointDialogShowing)
			waypointDialog.show();
		this.registerReceiver(refreshReceiver, refreshIntentFilter) ;
		this.registerReceiver(refreshReceiver, logPointFilter) ;
		this.setupTrackingPolyline();
		//set scale unit category in case preference changed
		if(scaleBar != null)
			scaleBar.refreshUnitCategory();
		//make sure currently displayed track still exists
		if(trackViewId != -1) {
			Track t = trackManager.getTrack(trackViewId) ;
			if(t == null)
				this.setViewTrackPolyline(-1);
		}
		//refresh waypoints in case waypoints were deleted
		if(map != null) {
			waypoints.removeAllMarkers();
			waypoints.refreshMarkers();
		}

	}

	public void onPause() {
		if(map != null)
			this.saveCameraPosition(map.getCameraPosition());
		if(loaderProgressShowing)
			loaderProgress.dismiss();
		if(transparencyDialogShowing)
			transparencyDialog.dismiss();
		if(searchDialogShowing)
			searchDialog.dismiss();
		if(waypointDialogShowing)
			waypointDialog.dismiss();
		this.unregisterReceiver(refreshReceiver);
		super.onPause();
	}

	public void onMapReady(final GoogleMap map) {
		// Do a null check to confirm that we have not already instantiated the map.
		if (this.map == null) {
            this.map = map;

            // The Map is verified. It is now safe to manipulate the map.
            Log.e(TAG, "setting up map! plus error to open log.") ;

            SharedPreferences prefs = this.getPreferences(MODE_PRIVATE) ;

            this.setMapType(prefs.getInt(PREF_MAPTYPE, GoogleMap.MAP_TYPE_NORMAL));

            grid = new NTSGrid(this, map) ;
            gridMajor = new NTSGridMajor(this, map) ;
            downloadedImages = new NTSDownloadedImagesGrid(this, map) ;

            this.setGridState(prefs.getBoolean(PREF_GRID_ON, true));

            images = new NTSImageTile(this, map) ;
            this.setImagesState(prefs.getBoolean(PREF_TOPO_ON, true));

            this.registerForContextMenu(layout);

            transparencyDialog = new TransparencyDialog(this) ;
            transparencyDialog.setMax(100);
            transparencyDialog.setProgress(100);

            float savedTransparency = prefs.getFloat(PREF_MAP_TRANSPARENCY, 0) ;
            int progress = Math.round((1 - savedTransparency) * 100) ;
            transparencyDialog.setProgress(progress);
            images.setTransparency(savedTransparency);

            transparencyDialog.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //do nothing
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //do nothing
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                        boolean fromUser) {
                    float transparency = 1 - (float)(progress / 100.0) ;
                    images.setTransparency(transparency);
                    SharedPreferences.Editor edit = getPreferences(MODE_PRIVATE).edit() ;
                    edit.putFloat(PREF_MAP_TRANSPARENCY, transparency) ;
                    edit.commit() ;
                }
            });
            transparencyDialog.setChecked(images.isEnabled());
            transparencyDialog.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setImagesState(isChecked) ;
                    if(!isChecked)
                        transparencyDialog.cancel();
                }
            });
            transparencyDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    transparencyDialogShowing = false ;
                }
            });
            transparencyDialogShowing = false ;

            searchDialog = new SearchDialog(this) ;
            searchDialogShowing = false ;
            searchWaypointId = 0 ;
            searchDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    searchDialogShowing = false ;
                }
            });
            searchDialog.setOnAddressSelectedListener(new OnAddressSelectedListener() {

                @Override
                public void onAddressSelected(Address a, Bounds b) {
                    CameraUpdate ud ;
                    if(b != null) {
                        LatLng sw = new LatLng(b.getMinY(), b.getMinX()) ;
                        LatLng ne = new LatLng(b.getMaxY(), b.getMaxX()) ;

                        ud = CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sw, ne), 10) ;
                    } else {
                        LatLng ll = new LatLng(a.getLatitude(), a.getLongitude()) ;
                        ud = CameraUpdateFactory.newLatLng(ll) ;
                    }
                    String title = a.getAddressLine(0) ;
                    String desc = a.getAddressLine(1) ;
                    Waypoint w = new Waypoint() ;
                    w.lat = a.getLatitude() ;
                    w.lon = a.getLongitude() ;
                    if(title != null)
                        w.name = title ;
                    if(desc != null)
                        w.description = desc ;
                    w.temporary = true ;
                    waypoints.remove(searchWaypointId) ; //if exists
                    searchWaypointId = waypoints.add(w) ;
                    map.animateCamera(ud);
                }

            });

            measureDistanceTool = new MeasureTool(this, map) ;
            measureDistanceTool.getPolylineOptions().width(8).color(Color.argb(127, 0, 0, 255)) ; //half transparent blue
            measureDistanceTool.getStartMarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.green_circle))
            .anchor(0.5f, 0.5f);
            measureDistanceTool.setOnNewMeasuredDistanceListener(new OnNewMeasuredDistanceListener() {

                @Override
                public void onNewMeasuredDistance(double distanceKm,
                        List<LatLng> points) {
                    if(distanceKm != 0) {
                        double distanceM = distanceKm * 1000 ;
                        String[] units ;
                        if(distanceM < 500) {
                            units = ScaleBarView.UNITS_MED ;
                        } else {
                            units = ScaleBarView.UNITS_LG ;
                        }
                        String unit = units[Units.getUnitCategoryConstant(MapActivity.this)] ;
                        double valueU = Units.fromSI(distanceM, unit) ;

                        String num = new DecimalFormat("0.0").format(valueU) ;
                        String text = String.format(getString(R.string.map_measure_distance), num, unit) ;
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText(text) ;
                    } else {
                        statusText.setVisibility(View.GONE);
                    }
                }

            });

            measureAreaTool = new MeasureAreaTool(this, map) ;
            measureAreaTool.getPolygonOptions().strokeWidth(1).fillColor(Color.argb(127, 0, 0, 255)) ; //half transparent blue
            measureAreaTool.getStartMarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.green_circle)).anchor(0.5f, 0.5f); ;
            measureAreaTool.setOnNewMeasuredAreaListener(new OnNewMeasuredAreaListener() {

                @Override
                public void onNewMeasuredArea(double areaKm2,
                        List<LatLng> points) {
                    if(areaKm2 != 0) {
                        String[] units ;
                        if(areaKm2 < 2.5) {
                            units = new String[] {"ha", "acres"} ;
                        } else {
                            units = new String[] {"km2", "mi2"} ;
                        }
                        String unit = units[Units.getUnitCategoryConstant(MapActivity.this)] ;
                        double valueU = Units.fromSI(1e6*areaKm2, unit) ;

                        String num = new DecimalFormat("0.0").format(valueU) ;
                        String label = unit.replace("2", "<sup>2</sup>") ;

                        Spanned text = Html.fromHtml(String.format(getString(R.string.map_measure_area), num, label)) ;
                        statusText.setVisibility(View.VISIBLE);
                        statusText.setText(text) ;
                    } else {
                        statusText.setVisibility(View.GONE);
                    }
                }

            });
            setCurrentTool(null) ;

            this.setupTrackingPolyline();
            waypoints = new WaypointManager(this, map) ;
            waypoints.cleanTemporary() ;
            waypoints.setEnabled(true);

            waypointDialog = new WaypointDialog(this, waypoints) ;
            waypointDialogShowing = false ;

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker m) {
                    Waypoint w = waypoints.getWaypoint(m) ;
                    if(w != null)
                        launchWaypointEditor(w) ;
                }
            });
            map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                @Override
                public void onMarkerDrag(Marker arg0) {
                    //do nothing
                }

                @Override
                public void onMarkerDragEnd(Marker m) {
                    //update position
                    Log.i(TAG, "end marker drag") ;
                    Waypoint w = waypoints.getWaypoint(m) ;
                    if(w != null) {
                        LatLng pos = m.getPosition() ;
                        w.lat = pos.latitude ;
                        w.lon = pos.longitude ;
                        if(!w.temporary)
                            waypoints.update(w) ;
                        else
                            launchWaypointEditor(w) ;
                    } else {
                        Log.e(TAG, "could not find waypoint to go with marker") ;
                    }
                }

                @Override
                public void onMarkerDragStart(Marker arg0) {
                    //do nothing
                }
            });

            // show on map true by default
            boolean showMeOnMap = getPreferences(MODE_PRIVATE).getBoolean(PREF_MAP_MYLOCATION, true) ;
            if(showMeOnMap) {
                if(checkLocationPermission()) {
                    map.setMyLocationEnabled(true);
                } else {
                    Log.i("MapActivity", "requesting location permission...");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_SHOW_ON_MAP);
                }
            } else {
                map.setMyLocationEnabled(false);
            }

            map.setOnCameraChangeListener(this);
            this.onCameraChange(map.getCameraPosition());

            this.invalidateOptionsMenu();

            if(pendingUri != null) {
                this.loadGeoUri(pendingUri);
            }
        }

	}

	private void setupTrackingPolyline() {
		long trackId = trackManager.getActiveTrackId() ;
		Log.i(TAG, "current track id:" + trackId) ;

		if(trackId != -1) {
			List<LatLng> points = trackManager.getTrackPoints(trackId) ;
			if(points.size() > 1) {
				if(trackPolyline == null) {
					PolylineOptions plo = new PolylineOptions() ;
					plo.width(5) ;
					plo.color(Color.argb(127, 0, 0, 0)) ;
					plo.addAll(points) ;
					trackPolyline = map.addPolyline(plo) ;
				} else {
					trackPolyline.setPoints(points);
				}
			}
		} else {
			if(trackPolyline != null) {
				trackPolyline.remove();
				trackPolyline = null ;
			}
		}
	}


	private void getDialogViewTrack() {
		AlertDialog.Builder b = new AlertDialog.Builder(this) ;
		b.setTitle(R.string.map_trackview_title) ;
		final List<Track> trackList = trackManager.getTracks() ;

		int position = -1 ;
		for(int i=0; i<trackList.size(); i++) {
			if(trackList.get(i).id == trackViewId) {
				position = i;
				break ;
			}
		}

		b.setSingleChoiceItems(new ArrayAdapter<Track>(this, android.R.layout.select_dialog_singlechoice, trackList), 
				position, 
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setViewTrackPolyline(trackList.get(which).id) ;
				dialog.cancel();
			}
		}) ;
		b.setNegativeButton(R.string.map_trackview_cancel, null) ;
		if(position != -1) {
			b.setPositiveButton(R.string.map_trackview_cleartrack, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					setViewTrackPolyline(-1) ;
					dialog.dismiss();
				}
			}) ;
		}
		b.show();
	}

	private void setViewTrackPolyline(long trackId) {
		trackViewId = trackId ;
		if(trackId != -1) {
			if(trackId != trackManager.getActiveTrackId()) {
				List<LatLng> points = trackManager.getTrackPoints(trackId) ;
				if(points.size() > 0) {
					if(trackViewPolyline == null) {
						PolylineOptions plo = new PolylineOptions() ;
						plo.width(5) ;
						plo.color(Color.argb(127, 255, 0, 0)) ;
						plo.addAll(points) ;
						trackViewPolyline = map.addPolyline(plo) ;
					} else {
						trackViewPolyline.setPoints(points);
					}

					//start marker
					LatLng start = points.get(0) ;
					if(trackViewStartMarker != null) {
						trackViewStartMarker.setPosition(start);
					} else {
						MarkerOptions mo = new MarkerOptions() ;
						mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_circle)) ;
						mo.position(start) ;
						trackViewStartMarker = map.addMarker(mo) ;
					}

					//zoom
					CameraUpdate cu ;
					if(points.size() >= 2) {
						LatLngBounds.Builder bb = new LatLngBounds.Builder() ;
						for(LatLng l: points)
							bb.include(l) ;
						cu = CameraUpdateFactory.newLatLngBounds(bb.build(), 20) ;
					} else {
						cu = CameraUpdateFactory.newLatLng(start) ;
					}

					map.animateCamera(cu);
				} else {
					setViewTrackPolyline(-1) ;
					Toast.makeText(this, R.string.map_trackview_error_nopoints, Toast.LENGTH_SHORT).show() ;
				}
			} else {
				//user is viewing active track, which is already on the map
				//zoom to map
				List<LatLng> points = trackManager.getTrackPoints(trackId) ;
				CameraUpdate cu ;
				if(points.size() >= 2) {
					LatLngBounds.Builder bb = new LatLngBounds.Builder() ;
					for(LatLng l: points)
						bb.include(l) ;
					cu = CameraUpdateFactory.newLatLngBounds(bb.build(), 20) ;
				} else if(points.size() == 1) {
					cu = CameraUpdateFactory.newLatLng(points.get(0)) ;
				} else {
					Toast.makeText(this, R.string.map_trackview_error_nopoints, Toast.LENGTH_SHORT).show();
					cu = null ;
				}
				if(cu != null)
					map.animateCamera(cu);
			}
		} else {
			if(trackViewPolyline != null) {
				trackViewPolyline.remove();
				trackViewPolyline = null ;
			}
			if(trackViewStartMarker != null) {
				trackViewStartMarker.remove() ;
				trackViewStartMarker = null ;
			}
		}
	}

	@Override
	public void onMapClick(LatLng arg0) {
		//do nothing
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		longPressLatLon = new LatLon(arg0.latitude, arg0.longitude) ;
		MapActivity.this.openContextMenu(layout);
		Vibrator v = (Vibrator)getSystemService(VIBRATOR_SERVICE) ;
		v.vibrate(100);
	}

	private void setCurrentTool(MapTool t) {
		if(t != currentTool && currentTool != null)
			currentTool.setEnabled(false);
		currentTool = t ;
		if(t != null) {
			t.setEnabled(true);
			map.setOnMapLongClickListener(t);
			map.setOnMapClickListener(t);
			map.setOnMarkerClickListener(t);
		} else {
			map.setOnMapClickListener(this);
			map.setOnMapLongClickListener(this) ;
			map.setOnMarkerClickListener(null);
		}
	}

	private void saveCameraPosition(CameraPosition cp) {
		SharedPreferences.Editor edit = this.getPreferences(MODE_PRIVATE).edit() ;
		edit.putString(PREF_SAVED_CAMERA_POSITION, String.format("%s,%s,%s", cp.target.latitude, 
				cp.target.longitude, cp.zoom)) ;
		edit.apply() ;
	}

	private CameraPosition getStoredCameraPosition() {
		String value = this.getPreferences(MODE_PRIVATE).getString(PREF_SAVED_CAMERA_POSITION, null) ;
		if(value != null) {
			try {
				String[] values = value.split(",") ;
				if(values.length == 3) {
					return CameraPosition.fromLatLngZoom(
							new LatLng(Double.valueOf(values[0]), Double.valueOf(values[1])), 
							Float.valueOf(values[2])) ;
				} else {
					throw new NumberFormatException() ;
					//gets the point across, anyway
				}
			} catch(NumberFormatException e) {
				//error in saved data
				Log.e(TAG, "error in saved camera position data", e) ;
				return null ;
			}
		} else {
			//default camera position, over eastern canada
			return CameraPosition.fromLatLngZoom(new LatLng(48.2,-70.0), 4) ;
		}
	}

	private void setGridState(boolean state) {
		grid.setEnabled(state);
		gridMajor.setEnabled(state);
		downloadedImages.setEnabled(state);
		this.invalidateOptionsMenu();
		SharedPreferences.Editor edit = this.getPreferences(MODE_PRIVATE).edit() ;
		edit.putBoolean(PREF_GRID_ON, state) ;
		edit.apply() ;
	}

	private void setImagesState(boolean state) {
		images.setEnabled(state);
		this.invalidateOptionsMenu();
		SharedPreferences.Editor edit = this.getPreferences(MODE_PRIVATE).edit() ;
		edit.putBoolean(PREF_TOPO_ON, state) ;
		edit.apply() ;
	}

	private void setMapType(int mapType) {
		map.setMapType(mapType);
		SharedPreferences.Editor edit = this.getPreferences(MODE_PRIVATE).edit() ;
		edit.putInt(PREF_MAPTYPE, mapType) ;
		edit.apply() ;
		this.invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);

		if(map != null && grid != null && measureAreaTool != null) {

			MenuItem gridItem = menu.findItem(R.id.action_grid) ;
			int title ;
			int icon ;
			if(grid.isEnabled()) {
				title = R.string.action_grid_off ;
				icon = R.drawable.ic_action_grid_off ;
			} else {
				title = R.string.action_grid_on ;
				icon = R.drawable.ic_action_grid_on ;
			}
			gridItem.setTitle(title) ;
			gridItem.setIcon(icon) ;

			if(map.isMyLocationEnabled()) {
				title = R.string.action_mylocation_off ;
				icon = R.drawable.ic_action_location_off ;
			} else {
				title = R.string.action_mylocation_on ;
				icon = R.drawable.ic_action_location_on ;
			}

			MenuItem locItem = menu.findItem(R.id.action_mylocation) ;
			locItem.setTitle(title) ;
			locItem.setIcon(icon) ;

			MenuItem measureItem = menu.findItem(R.id.action_measure) ;
			if(measureDistanceTool.isEnabled() || measureAreaTool.isEnabled()) {
				title = R.string.action_measure_stop ;
				icon = R.drawable.ic_action_measure_stop ;
			} else {
				title = R.string.action_measure ;
				icon = R.drawable.ic_action_measure_distance ;
			}
			measureItem.setTitle(title) ;
			measureItem.setIcon(icon) ;

			MenuItem trackingItem = menu.findItem(R.id.action_tracking) ;
			if(trackManager.getActiveTrackId() == -1) {
				title = R.string.action_tracking_on ;
				icon = R.drawable.ic_action_tracking_on ;
			} else {
				title = R.string.action_tracking_off ;
				icon = R.drawable.ic_action_tracking_off ;
			}
			trackingItem.setTitle(title) ;
			trackingItem.setIcon(icon) ;

			MenuItem waypointsItem = menu.findItem(R.id.action_waypoints) ;
			if(waypoints.isEnabled()) {
				title = R.string.action_waypoints_off ;
				//icon = R.drawable.ic_action_tracking_on ;
			} else {
				title = R.string.action_waypoints_on ;
				//icon = R.drawable.ic_action_tracking_off ;
			}
			waypointsItem.setTitle(title) ;
			//trackingItem.setIcon(icon) ;

			MenuItem markMe = menu.findItem(R.id.action_mark_my_location) ;
			if(map.isMyLocationEnabled())
				markMe.setVisible(true) ;
			else
				markMe.setVisible(false) ;

			int mapType = map.getMapType() ;

			int iconId ;
			switch(mapType) {
			case GoogleMap.MAP_TYPE_NONE:
				iconId = R.drawable.ic_maptype_none ;
				break ;
			case GoogleMap.MAP_TYPE_NORMAL:
				iconId = R.drawable.ic_maptype_map ;
				break ;
			case GoogleMap.MAP_TYPE_SATELLITE:
				iconId = R.drawable.ic_maptype_satellite ;
				break ;
			case GoogleMap.MAP_TYPE_TERRAIN:
				iconId = R.drawable.ic_maptype_terrain ;
				break ;
			case GoogleMap.MAP_TYPE_HYBRID:
				iconId = R.drawable.ic_maptype_hybrid ;
				break ;
			default:
				iconId = R.drawable.ic_maptype_none ;
			}

			MenuItem typeItem = menu.findItem(R.id.action_maptype) ;
			typeItem.setIcon(iconId) ;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, Preferences.class) ;
			startActivity(i) ;
			return true;
		} else if(id == R.id.action_grid) {
			this.setGridState(!grid.isEnabled());
			return true ;
		} else if(id == R.id.action_topo) {
			transparencyDialog.show();
			transparencyDialogShowing = true ;
			return true ;
		} else if(id == R.id.action_maptype_none) {
			this.setMapType(GoogleMap.MAP_TYPE_NONE);
			return true ;
		} else if(id == R.id.action_maptype_map) {
			this.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			return true ;
		} else if(id == R.id.action_maptype_satellite) {
			this.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			return true ;
		} else if(id == R.id.action_maptype_hybrid) {
			this.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			return true ;
		} else if(id == R.id.action_maptype_terrain) {
			this.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			return true ;
		} else if(id == R.id.action_search) {
			searchDialog.clearText();
			searchDialogShowing = true ;
			searchDialog.show();
			return true ;
		} else if(id == R.id.action_measure) {
			if(measureDistanceTool.isEnabled() || measureAreaTool.isEnabled()) {
				setCurrentTool(null) ;
				statusText.setVisibility(View.GONE);
				invalidateOptionsMenu();
				return true ;
			} else {
				return false ;
			}
		} else if(id == R.id.action_measure_distance) {

			setCurrentTool(measureDistanceTool) ;
			Toast.makeText(this, R.string.map_measure_helptext, Toast.LENGTH_SHORT).show();

			invalidateOptionsMenu();
			return true ;
		} else if(id == R.id.action_measure_area) {

			setCurrentTool(measureAreaTool) ;
			Toast.makeText(this, R.string.map_measure_helptext, Toast.LENGTH_SHORT).show();

			invalidateOptionsMenu();
			return true ;
		} else if(id == R.id.action_tracking) {
			if(trackManager.getActiveTrackId() != -1) {
				Intent i = new Intent(this, TrackerBroadcastReceiver.class) ;
				i.setAction(TrackerBroadcastReceiver.ACTION_STOP_LOGGING) ;
				sendBroadcast(i) ;
			} else {
                if(!checkLocationPermission()) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_TRACKME);
                } else {
                    Intent i = new Intent(this, TrackerBroadcastReceiver.class);
                    i.setAction(TrackerBroadcastReceiver.ACTION_START_LOGGING);
                    sendBroadcast(i);
                }
			}
			return true ;
		} else if(id == R.id.action_mylocation) {
            if(!map.isMyLocationEnabled()) {
                if(checkLocationPermission()) {
                    map.setMyLocationEnabled(true);
                } else {
                    Log.i("MapActivity", "requesting location permission...");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_SHOW_ON_MAP);
                }
            } else {
                map.setMyLocationEnabled(false);
            }
            getPreferences(MODE_PRIVATE).edit()
                    .putBoolean(PREF_MAP_MYLOCATION, map.isMyLocationEnabled())
                    .apply();
			this.invalidateOptionsMenu();
			return true ;			
		} else if(id == R.id.action_mark_my_location) {
			Toast.makeText(MapActivity.this, R.string.map_location_waiting, Toast.LENGTH_SHORT).show();
			map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

				@Override
				public void onMyLocationChange(Location l) {
					Waypoint w = new Waypoint() ;
					if(l != null) {
						w.lat = l.getLatitude() ;
						w.lon = l.getLongitude() ;
						w.altitude = l.getAltitude() ;
						w.time = l.getTime() ;
						launchWaypointEditor(w) ;
					} else {
						Toast.makeText(MapActivity.this, R.string.map_location_not_available, Toast.LENGTH_SHORT).show();
					}
					map.setOnMyLocationChangeListener(null);
				}
			});
			return true ;
		} else if(id == R.id.action_about) {
			this.showAboutDialog() ;
			return true ;
		} else if(id == R.id.action_location_manager) {
			Intent i = new Intent(this, LocationManagerActivity.class) ;
			startActivity(i) ;
			return true ;
		} else if(id == R.id.action_trackview) {
			getDialogViewTrack() ;
			return true ;
		} else if(id == R.id.action_waypoints) {
			waypoints.setEnabled(!waypoints.isEnabled());
			invalidateOptionsMenu();
			return true ;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if(longPressLatLon != null) {			
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.map_context, menu);

			//position first

			MenuItem location = menu.findItem(R.id.action_location) ;
			location.setTitle(LocationFormat.formatLocation(this, longPressLatLon.getLat(), longPressLatLon.getLon())) ;

			int contextScale = downloadedImages.getCurrentScaleConstant() ;
			if(contextScale == -1)
				contextScale = images.getCurrentScaleConstant() ;

			if(contextScale == NTSMapSheet.SCALE_BLOCK)
				contextScale = NTSMapSheet.SCALE_50K ;

			MenuItem mapsheet = menu.findItem(R.id.action_download_sheet) ;
			MenuItem remove = menu.findItem(R.id.action_remove_mapsheets) ;
			MenuItem reload = menu.findItem(R.id.action_reload_mapsheets) ;

			if(contextScale != -1) {
				longPressMapSheet = NTSMapSheet.getSheetByLatLon(contextScale, longPressLatLon) ;

				if(longPressMapSheet != null) {

					boolean anyFiles = cache.hasAnyFiles(longPressMapSheet) ;
					boolean allFiles = cache.hasAllFiles(longPressMapSheet) ;

					if(!anyFiles && contextScale == NTSMapSheet.SCALE_50K) {
						String addendum = "" ;
						String name = names.getName(longPressMapSheet) ;
						if(name != null)
							addendum = " (" + name + ")" ;
						mapsheet.setTitle(String.format(getString(R.string.action_download_mapsheet) + addendum, longPressMapSheet.getNtsId())) ;
						mapsheet.setEnabled(true) ;
						mapsheet.setVisible(true) ;
					} else if(allFiles) {
						String format = getString(R.string.action_download_mapsheet_exists) ;
						mapsheet.setTitle(String.format(format, longPressMapSheet.getNtsId())) ;
						mapsheet.setEnabled(false) ;
						mapsheet.setVisible(true) ;
					} else {
						mapsheet.setVisible(true) ;
						mapsheet.setEnabled(false) ;
						mapsheet.setTitle(R.string.action_zoomin_to_download) ;
					}

					if(anyFiles) {
						String format = getString(R.string.action_remove_mapsheets) ;
						if(contextScale == NTSMapSheet.SCALE_50K)
							format = getString(R.string.action_remove_mapsheets_50k) ;
						remove.setTitle(String.format(format, longPressMapSheet.getNtsId())) ;
						remove.setVisible(true) ;
					} else {
						remove.setVisible(false) ;
					}

					if(anyFiles && contextScale == NTSMapSheet.SCALE_50K) {
						String format = getString(R.string.action_reload_mapsheets) ;
						reload.setTitle(String.format(format, longPressMapSheet.getNtsId())) ;
						reload.setVisible(true) ;
					} else {
						reload.setVisible(false) ;
					}

				} else {
					mapsheet.setEnabled(false) ;
					mapsheet.setTitle(R.string.action_download_mapsheet_nomapsheets) ;
					remove.setVisible(false) ;
					reload.setVisible(false) ;
				}
			} else {
				mapsheet.setEnabled(false) ;
				mapsheet.setTitle(R.string.action_download_mapsheet_nomapsheets) ;
				remove.setVisible(false) ;
				reload.setVisible(false) ;
			}
		}
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.i(TAG, "context item selected! " + item.getTitle() + " and info " + item.getMenuInfo()) ;
		int id = item.getItemId() ;
		if(id == R.id.action_download_sheet) {
			if(longPressMapSheet != null) {
				this.startToporamaInfoLoader(longPressMapSheet);
			} else {
				Log.e(TAG, "no mapsheet on context item selected") ;
			}
			return true ;
		} else if(id == R.id.action_reload_mapsheets) {
			if(longPressMapSheet != null) {
				this.startToporamaInfoLoader(longPressMapSheet);
			} else {
				Log.e(TAG, "no mapsheet on context item selected") ;
			}
			return true ;
		} else if(id == R.id.action_remove_mapsheets) {
			if(longPressMapSheet != null) {
				AlertDialog.Builder b = new AlertDialog.Builder(this) ;
				b.setTitle(R.string.map_removesheets_title) ;
				String format = getString(R.string.map_removesheets_message) ;
				String nameAd = sheetNameFormatted(longPressMapSheet, " (%s)") ;
				//TODO may need to do this asynchronously
				long[] sizes = cache.countFiles(longPressMapSheet) ;
				String mbString = megabyteString(sizes[1]) ;
				b.setMessage(String.format(format, longPressMapSheet.getNtsId(), nameAd, sizes[0], mbString)) ;
				b.setPositiveButton(getString(R.string.map_removesheets_delete), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cache.removeAllFiles(longPressMapSheet) ;
						downloadedImages.refreshSheets();
						images.refreshSheets();
					}
				}) ;
				b.setNegativeButton(R.string.map_removesheets_cancel, null) ;
				b.create().show();
			} else {
				Log.e(TAG, "no mapsheet on context item selected") ;
			}
			return true ;
		} else if(id == R.id.action_mark_location) {
			Waypoint w = new Waypoint() ;
			w.lat = longPressLatLon.getLat() ;
			w.lon = longPressLatLon.getLon() ;
			w.time = System.currentTimeMillis() ;
			launchWaypointEditor(w) ;
			return true ;
		} else if(id == R.id.action_location) {
			return false ;
		} else {
			return super.onContextItemSelected(item);
		}
	}

	private void showAboutDialog() {
		AlertDialog.Builder b = new AlertDialog.Builder(this) ;
		String title = "About Canada Topo" ;
        String updateMes = "Thanks for holding tight on the NO_FILE_ON_SERVER error (if you don't know what " +
                "I'm talking about skip this paragraph)! And thanks to Rich for giving me the $5 that kept me " +
                "up at night feeling guilty that I hadn't fixed the app yet. I know there are more " +
                "improvements that are needed, but at least you're back to functional now! Cheers!" ;
		String mes = "Canada Topo is designed to display the National Topographic System (NTS) "
				+ "grid for Canada and display Topo maps published for free by the government. "
				+ "It also does other things, but maybe not as well as other apps out there. "
				+ "Please email me with any problems you have to make this app better! You can find "
				+ "me at apps@fishandwhistle.net." ;
		String message = "Toporama topographic maps are published by the Government of Canada,"
				+ " but the government (or me) makes no guarantees to the accuracy of this information. "
				+ "In many cases the information is outdated, but provides a useful addition to information "
				+ "provied by Google Maps and offers the additional advantage of being able to be downloaded "
				+ "and kept indefinitely on your phone should you need to use it while out of reception "
				+ "(something that happens to me all the time when I hike/canoe/kayak). In short...don't "
				+ "be an idiot! But more information means better decisions, so use and enjoy!" ;
		b.setTitle(title) ;
		b.setMessage(updateMes + "\n\n" + mes + "\n\n" + message) ;
		b.setNeutralButton("Rate This App!", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_VIEW) ;
				i.setData(Uri.parse("http://play.google.com/store/apps/details?id=net.fishandwhistle.ctexplorer")) ;
				try {
					startActivity(i) ;
				} catch(ActivityNotFoundException e) {
					Toast.makeText(MapActivity.this, "No internet browser found!", Toast.LENGTH_SHORT).show() ;
				}
			}
		}) ;
		b.setPositiveButton(R.string.map_about_tour, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				IntroductionTour t = new IntroductionTour(MapActivity.this) ;
				t.startTour();
			}
		}) ;
		b.setNegativeButton(R.string.map_about_cancel, null) ;
		b.create().show();
	}

	private void launchWaypointEditor(Waypoint w) {
		waypointDialog.setWaypoint(w) ;
		waypointDialog.show();
	}

	private void startToporamaInfoLoader(NTSMapSheet sheet) {
		if(loader != null)
			loader.cancel();
		loader = new ToporamaLoader(MapActivity.this, longPressMapSheet, false) ;
		loader.loadAsync(this);
	}


	@Override
	public void onCameraChange(CameraPosition cp) {
		LatLngBounds bds = map.getProjection().getVisibleRegion().latLngBounds ;
		Bounds bounds = new Bounds(bds.southwest.longitude, bds.northeast.longitude, 
				bds.southwest.latitude, bds.northeast.latitude) ;

		if(screenHeight == 0) {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int dpi = displaymetrics.densityDpi ;
			//FIXME switching from landscape to portrait sometimes gets nullpointerexception here
			int heightPx = layout.getHeight() ;
			screenHeight = (double)heightPx / (double)dpi / 39.3701 ;
			screenHeightPx = heightPx ;
		}

		double mapHeightMetres = bounds.height() * 60.0 * 1852.0 ;
		double invScale = mapHeightMetres / screenHeight ;
		grid.onBoundsUpdated(bounds, invScale);
		gridMajor.onBoundsUpdated(bounds, invScale);
		images.onBoundsUpdated(bounds, invScale);
		downloadedImages.onBoundsUpdated(bounds, invScale);
		waypoints.onBoundsUpdated(bounds);
		scaleBar.refreshScale(mapHeightMetres / screenHeightPx);
	}

	@Override
	public void onToporamaLoad(final NTSMapSheet sheet, ErrorClass error,
			int contentLength) {
		if(error == ErrorClass.INFORMATION_ONLY) {
			String message = getString(R.string.map_topopreview_message) ;
			String addendum = sheetNameFormatted(sheet, " (%s)") ;

			String lengthString = megabyteString(contentLength) ;

			AlertDialog.Builder builder = new AlertDialog.Builder(this) ;
			builder.setTitle(R.string.map_topopreview_title) ;
			builder.setMessage(String.format(message, lengthString, sheet.getNtsId(), addendum)) ;
			builder.setNegativeButton(R.string.map_topopreview_cancel, null) ;
			builder.setPositiveButton(R.string.map_topopreview_download, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(MapActivity.this, ToporamaLoaderService.class) ;
					i.setAction(ToporamaLoaderService.ACTION_DOWNLOAD_TOPORAMA) ;
					i.setData(Uri.parse("nts:"+sheet.getNtsId())) ;
					Log.i(TAG, "Starting loader service") ;
					startService(i) ;
					//create dummy file so user knows which maps they've tried to download
					File mapDummy = cache.getMapFile(sheet) ;
					try {
						mapDummy.createNewFile() ;
						downloadedImages.refreshSheets();
					} catch(IOException e) {
						//do nothing
					}

					Toast.makeText(MapActivity.this, R.string.map_topopreview_starting_download, Toast.LENGTH_SHORT).show();
				}
			}) ;
			builder.create().show();

		} else if(error == ErrorClass.NO_ERROR || error == ErrorClass.CANCELLED_ERROR) {
			images.refreshSheets();
			downloadedImages.refreshSheets();
		} else {
			Toast.makeText(this, String.format(getString(R.string.map_topopreview_error), error), Toast.LENGTH_SHORT).show();
		}
	}

	private String sheetNameFormatted(NTSMapSheet sheet, String format) {
		String name = names.getName(sheet) ;
		if(name != null)
			return String.format(format, name) ;
		else
			return "" ;
	}

	private String megabyteString(long bytes) {
		double lengthMb = bytes / 1048576.0 ;
		return new DecimalFormat("0.0").format(lengthMb) ;
	}

	@Override
	public void onToporamaProgress(NTSMapSheet sheet, ProgressType type,
			int value) {
		if(type != currentLoaderTask) {
			currentLoaderTask = type ;
			switch(type) {
			case COMPLETED:
				loaderProgress.hide();
				loaderProgressShowing = false ;
				break;
			case DOWNLOAD_HIRES:
				loaderProgress.setMessage(getString(R.string.loader_hires));
				loaderProgress.setIndeterminate(false);
				break;
			case DOWNLOAD_LOWRES:
				loaderProgress.setMessage(getString(R.string.loader_lowres));
				loaderProgress.setIndeterminate(false);
				break;
			case INITIALIZE:
				loaderProgress.show();
				loaderProgressShowing = true ;
				break;
			case PROCESSING:
				loaderProgress.setMessage(getString(R.string.loader_processing));
				loaderProgress.setIndeterminate(true);
				break;		
			}
		}

		if(type == ProgressType.DOWNLOAD_HIRES || type == ProgressType.DOWNLOAD_LOWRES) {
			loaderProgress.setProgress(value);
		}

	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_SHOW_ON_MAP:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do the showing on map thing
                    try {
                        map.setMyLocationEnabled(true);
                    } catch(SecurityException e) {
                        //will never happen
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Cannot display GPS location without GPS permission!",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case PERMISSION_REQUEST_TRACKME:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // start tracking
                    Intent i = new Intent(this, TrackerBroadcastReceiver.class);
                    i.setAction(TrackerBroadcastReceiver.ACTION_START_LOGGING);
                    sendBroadcast(i);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Cannot track using GPS location without GPS permission!",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = ContextCompat.checkSelfPermission(this, permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

	private void loadGeoUri(Uri uri) {
		Waypoint w = new Waypoint() ;
		String wholeUri = uri.getSchemeSpecificPart() ;
		int questionMarkPos = wholeUri.indexOf('?') ;
		String coords = wholeUri ;
		String alternateUriString = "http://joe_shmo.net/fish" ;
		
		if(questionMarkPos != -1) {
			coords = wholeUri.substring(0, questionMarkPos) ;
			String uriString = uri.toString() ;
			questionMarkPos = uriString.indexOf('?') ;
			
			alternateUriString = "http://joe_shmo.net/fish" + uriString.substring(questionMarkPos) ;
		}
		Uri alternateUri = Uri.parse(alternateUriString) ;

		double[] latlons = parseCoords(coords) ;
		if(latlons != null) {			
			try {
				w.lat = latlons[0] ;
				w.lon = latlons[1] ;

				if(w.name == null) {
					String queryQ = alternateUri.getQueryParameter("q") ;
					if(queryQ != null) {
						latlons = parseCoords(queryQ) ;
						if(latlons != null) {
							w.lat = latlons[0] ;
							w.lon = latlons[1] ;
						} else {
							queryQ = queryQ.trim();
							if(queryQ.length() > 0) {
								int para1Pos = queryQ.indexOf('(') ;
								if(para1Pos != -1) {
									w.name = queryQ.substring(para1Pos).replace("(", "").replace(")", "") ;
									coords = queryQ.substring(0, para1Pos) ;
									latlons = parseCoords(coords) ;
									if(latlons != null) {
										w.lat = latlons[0] ;
										w.lon = latlons[1] ;
									}
								} else {
									w.name = queryQ ;
								}
							}
						}
					}
				}

				if(w.name == null) {
					w.name = String.format("%s, %s", w.lat, w.lon) ;
				}

				float zoom = -1 ;
				try {
					String zoomString = alternateUri.getQueryParameter("z") ;
					if(zoomString != null)
						zoom = Float.valueOf(zoomString) ;
				} catch(NumberFormatException e) {
					//do nothing, zoom is lame
				}

				w.temporary = true ;
				waypoints.add(w) ;

				if(zoom != -1) {
					CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(w.lat, w.lon), zoom) ;
					map.animateCamera(cu);
				} else {
					CameraUpdate cu = CameraUpdateFactory.newLatLng(new LatLng(w.lat, w.lon)) ;
					map.animateCamera(cu);
				}

			} catch(NumberFormatException e) {
				Toast.makeText(this, "Error parsing geo: uri!", Toast.LENGTH_SHORT).show() ;
			}
		} else {
			Toast.makeText(this, "Error parsing geo: uri!", Toast.LENGTH_SHORT).show() ;
		}		
	}

	private static double[] parseCoords(String coords) {
		String[] parts = coords.split(",") ;
		if(parts.length == 2) {			
			try {
				double[] out = new double[2] ;
				out[0] = Double.valueOf(parts[0]) ;
				out[1] = Double.valueOf(parts[1]) ;
				return out ;
			} catch(NumberFormatException e) {
				return null ;
			}
		} else {
			return null ;
		}
	}

}
