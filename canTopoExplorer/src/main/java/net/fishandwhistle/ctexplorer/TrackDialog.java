package net.fishandwhistle.ctexplorer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fishandwhistle.ctexplorer.backend.CustomAlertDialog;
import net.fishandwhistle.ctexplorer.backend.DateFormatting;
import net.fishandwhistle.ctexplorer.backend.FileExporter;
import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import net.fishandwhistle.ctexplorer.gps.TrackManager;
import net.fishandwhistle.ctexplorer.gps.TrackManager.Track;
import net.fishandwhistle.ctexplorer.gps.TrackerBroadcastReceiver;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TrackDialog extends CustomAlertDialog {

	private EditText name ;
	private EditText description ;
	private TextView times ;
	private TextView points ;
	private Track t ;
	private TrackManager manager ;
	private MapCacheManager cache ;

	public TrackDialog(Context context, TrackManager tracks) {
		super(context, R.layout.dialog_track);
		cache = new MapCacheManager(context) ;
		addActions(context) ;
		name = (EditText)findViewById(R.id.track_name) ;
		description = (EditText)findViewById(R.id.track_description) ;
		times = (TextView)findViewById(R.id.track_times) ;
		points = (TextView)findViewById(R.id.track_points) ;
		manager = tracks ;
	}

	public void setTrack(Track track) {
		t = track ;
		name.setText(t.name);
		description.setText(t.description);
		String dateTextS = DateFormatting.getDateFormat(getContext()).format(new Date(t.timeDateBegin)) ;
		String dateTextE = DateFormatting.getDateTimeFormat(getContext()).format(new Date(t.timeDateEnd)) ;
		times.setText(String.format(getString(R.string.track_times), dateTextS, dateTextE));
		points.setText(String.format(getString(R.string.track_points), t.numPoints));
	}

	@Override
	protected Builder getBuilder(final Context context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		alert.setTitle(R.string.track_title);

		alert.setPositiveButton(R.string.track_save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String nameText = name.getText().toString();
				if(nameText.length() > 0) {
					t.name = nameText.trim() ;
					String descText = description.getText().toString() ;
					t.description = descText.trim() ;

					//attempt to update
					if(!manager.updateTrack(t))
						Toast.makeText(getContext(), R.string.track_error_noupdate, Toast.LENGTH_SHORT).show() ;
					else
						cancel();

				} else {
					Toast.makeText(getContext(), R.string.track_error_noname, Toast.LENGTH_SHORT).show() ;
				}
			}
		});

		alert.setNegativeButton(R.string.track_cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				cancel();
			}
		});
		return alert ;

	}

	private void showConfirmDeleteDialog(Context context) {
		AlertDialog.Builder b = new AlertDialog.Builder(context) ;
		b.setTitle(R.string.track_delete_title) ;
		b.setMessage(String.format(getString(R.string.track_delete_message), t.name)) ;
		b.setNegativeButton(R.string.track_delete_cancel, null) ;
		b.setPositiveButton(R.string.track_delete, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(!manager.removeTrack(t.id)) {
					Toast.makeText(getContext(), R.string.track_error_nodelete, Toast.LENGTH_SHORT).show();
				} else {
					dialog.cancel();
					cancel() ;
					//ensure state of notification if this was the active track
					Intent ensureLoggingState = new Intent(getContext(), TrackerBroadcastReceiver.class) ;
					ensureLoggingState.setAction(TrackerBroadcastReceiver.ACTION_ENSURE_STATE) ;
					getContext().sendBroadcast(ensureLoggingState) ;
				}
			}
		}) ;
		b.create().show() ;
	}
	
	private void showExportDialog(Context context) {
		AlertDialog.Builder b = new AlertDialog.Builder(context) ;
		b.setTitle(R.string.track_export_title) ;
		b.setMessage(R.string.export_choosemessage) ;
		b.setNegativeButton(R.string.track_delete_cancel, null) ;
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
		String[] actions = context.getResources().getStringArray(R.array.track_actions) ;
		OnClickListener[] onClickListeners = new OnClickListener[] { 
				new OnClickListener() {
					public void onClick(View v) {
						//export to gpx, kml
						showExportDialog(getContext()) ;
					}
				}, 
				new OnClickListener() {
					public void onClick(View v) {
						showConfirmDeleteDialog(getContext()) ;
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
		List<Track> trks = new ArrayList<Track>();
		trks.add(t) ;
		return new FileExporter(getContext(), null, trks) ;
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
