package net.fishandwhistle.ctexplorer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fishandwhistle.ctexplorer.backend.CustomAlertDialog;
import net.fishandwhistle.ctexplorer.backend.DateFormatting;
import net.fishandwhistle.ctexplorer.backend.FileExporter;
import net.fishandwhistle.ctexplorer.backend.LocationFormat;
import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import net.fishandwhistle.ctexplorer.backend.Units;
import net.fishandwhistle.ctexplorer.gps.WaypointManager;
import net.fishandwhistle.ctexplorer.gps.WaypointManager.Waypoint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WaypointDialog extends CustomAlertDialog {

	private EditText name ;
	private EditText description ;
	private TextView created ;
	private TextView altitude ;
	private TextView position ;
	private Waypoint w ;
	private WaypointManager waypoints ;
	private boolean showKeyboardOnShow ;
	private MapCacheManager cache ;

	public WaypointDialog(Context context, WaypointManager wpts) {
		super(context, R.layout.dialog_waypoint);
		cache = new MapCacheManager(context) ;
		addActions(context) ;
		name = (EditText)findViewById(R.id.waypoint_name) ;
		description = (EditText)findViewById(R.id.waypoint_description) ;
		created = (TextView)findViewById(R.id.waypoint_created) ;
		altitude = (TextView)findViewById(R.id.waypoint_altitude) ;
		position = (TextView)findViewById(R.id.waypoint_position) ;
		waypoints = wpts ;
		showKeyboardOnShow = false ;
	}

	public void show() {
		if(showKeyboardOnShow) {
			name.setSelection(0, name.getText().length());
			name.postDelayed(new Runnable() {
				public void run() {
					InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
				}
			}, 250) ;
			showKeyboardOnShow = false ;
		}
		super.show() ;
	}

	public void setWaypoint(Waypoint waypoint) {
		w = waypoint ;
		if(w.name != null && w.name.length() > 0) {
			name.setText(w.name) ;
		} else {
			String defaultName = waypoints.nextWaypointName() ;
			name.setText(defaultName) ;
			w.name = defaultName ;
		}
		description.setText(w.description);

		if(!Double.isNaN(w.altitude)) {
			String[] units = new String[] {"m", "ft"} ;
			String unit = units[Units.getUnitCategoryConstant(getContext())] ;
			double valueU = Units.fromSI(w.altitude, unit) ;
			String altString = new DecimalFormat("0").format(valueU) ;
			altitude.setText(String.format(getString(R.string.waypoint_altitude), altString, unit));
			altitude.setVisibility(View.VISIBLE);
		} else {
			altitude.setVisibility(View.GONE);
		}

		CharSequence locationText = LocationFormat.formatLocation(getContext(), w.lat, w.lon) ;
		position.setText(locationText);

		String dateText = DateFormatting.getDateTimeFormat(getContext()).format(new Date(w.time)) ;
		created.setText(String.format(getString(R.string.waypoint_created), dateText));

		if(w.id <= 0)
			showKeyboardOnShow = true ;

	}

	@Override
	protected Builder getBuilder(final Context context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(R.string.waypoint_title);

		alert.setPositiveButton(R.string.waypoint_save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				updateWaypointObject() ;
				cancel() ;
			}
		});

		alert.setNegativeButton(R.string.waypoint_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				cancel();
			}
		});
		return alert ;

	}

	private void updateWaypointObject() {
		String nameText = name.getText().toString();
		w.name = nameText.trim() ;
		if(w.name.length() > 0) {
			String descText = description.getText().toString() ;
			w.description = descText.trim() ;
			w.temporary = false ;
			if(w.id > 0) {
				//attempt to update
				if(!waypoints.update(w))
					Toast.makeText(getContext(), R.string.waypoint_error_noupdate, Toast.LENGTH_SHORT).show() ;
			} else {
				//add
				if(waypoints.add(w) <= 0)
					Toast.makeText(getContext(), R.string.waypoint_error_noadd, Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getContext(), R.string.waypoint_error_noname, Toast.LENGTH_SHORT).show() ;
		}
	}
	
	private void showConfirmDeleteDialog(Context context) {
		AlertDialog.Builder b = new AlertDialog.Builder(context) ;
		b.setTitle(R.string.waypoint_delete_title) ;
		b.setMessage(String.format(getString(R.string.waypoint_delete_message), w.name)) ;
		b.setNegativeButton(R.string.waypoint_delete_cancel, null) ;
		b.setPositiveButton(R.string.waypoint_delete, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(!waypoints.remove(w.id)) {
					Toast.makeText(getContext(), R.string.waypoint_error_nodelete, Toast.LENGTH_SHORT).show();
				} else {
					dialog.cancel();
					cancel() ; 
				}

			}
		}) ;
		b.create().show() ;
	}

	private void showExportDialog(Context context) {
		AlertDialog.Builder b = new AlertDialog.Builder(context) ;
		b.setTitle(R.string.waypoint_export_title) ;
		b.setMessage(R.string.export_choosemessage) ;
		b.setNegativeButton(R.string.waypoint_delete_cancel, null) ;
		b.setNeutralButton(R.string.export_kml, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				FileExporter fe = getExporter() ;
				File out = getShareTempFile(".kml") ;
				try {
					fe.writeKML(out);
					share(out, "application/vnd.google-earth.kml+xml") ;
				} catch(IOException e) {
					Toast.makeText(getContext(), R.string.export_error_writeerror, Toast.LENGTH_SHORT).show() ;
				}
			}
		}) ;
		b.setPositiveButton(R.string.export_gpx, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				FileExporter fe = getExporter() ;
				File out = getShareTempFile(".gpx") ;
				try {
					fe.writeGPX(out);
					share(out, "application/gpx+xml") ;
				} catch(IOException e) {
					Toast.makeText(getContext(), R.string.export_error_writeerror, Toast.LENGTH_SHORT).show() ;
				}
			}
		}) ;
		b.create().show() ;
	}

	private void addActions(Context context) {
		LayoutInflater inf = LayoutInflater.from(context) ;
		String[] actions = context.getResources().getStringArray(R.array.waypoint_actions) ;
		OnClickListener[] onClickListeners = new OnClickListener[] {
				new OnClickListener() {
					public void onClick(View v) {
						//view in google maps
						Intent i = new Intent(Intent.ACTION_VIEW) ;
						i.setData(Uri.parse(String.format("geo:0,0?q=%s,%s(%s)", w.lat, w.lon, w.name))) ;
						((Activity)getContext()).startActivity(Intent.createChooser(i, getString(R.string.waypoint_action_viewchooser)));
					}
				}, 
				new OnClickListener() {
					public void onClick(View v) {
						//export to gpx, kml
						showExportDialog(getContext()) ;
					}
				}, 
				new OnClickListener() {
					public void onClick(View v) {
						if(w.id != -1)
							showConfirmDeleteDialog(getContext()) ;
						else
							Toast.makeText(getContext(), R.string.waypoint_error_nosaved, Toast.LENGTH_SHORT).show() ;
					}
				}} ;
		LinearLayout root = (LinearLayout)this.getRootView() ;
		for(int i=0; i<actions.length; i++) {
			View v = inf.inflate(R.layout.dialog_action, root, false) ;
			TextView tv = (TextView)v.findViewById(R.id.dialog_action_text) ;
			tv.setText(actions[i]);
			tv.setOnClickListener(onClickListeners[i]);
			root.addView(v);
		}
	}

	
	private FileExporter getExporter() {
		List<Waypoint> wpts = new ArrayList<Waypoint>();
		wpts.add(w) ;
		return new FileExporter(getContext(), wpts, null) ;
	}
	
	private File getShareTempFile(String ext) {
		return cache.getTempFile(cache.getPublicTempDir(), ext) ;
	}
	
	private void share(File f, String mimeType) {
		Uri shareUri = Uri.fromFile(f) ;
		Intent i = new Intent(Intent.ACTION_SEND) ;
		i.setData(shareUri) ;
		i.setType(mimeType) ;
		i.putExtra(Intent.EXTRA_STREAM, shareUri);
		try {
			((Activity)getContext()).startActivity(i) ;
		} catch(ActivityNotFoundException e) {
			Toast.makeText(getContext(), R.string.export_error_noactivity, Toast.LENGTH_SHORT).show();
		}
	}
}
