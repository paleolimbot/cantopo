package net.fishandwhistle.ctexplorer;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fishandwhistle.ctexplorer.backend.DateFormatting;
import net.fishandwhistle.ctexplorer.backend.FileExporter;
import net.fishandwhistle.ctexplorer.backend.LocationFormat;
import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import net.fishandwhistle.ctexplorer.backend.Units;
import net.fishandwhistle.ctexplorer.gps.TrackManager;
import net.fishandwhistle.ctexplorer.gps.TrackManager.Track;
import net.fishandwhistle.ctexplorer.gps.WaypointManager;
import net.fishandwhistle.ctexplorer.gps.WaypointManager.Waypoint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocationManagerActivity extends Activity implements TabListener {

	private static final String TAG = "LocationManager" ;
	private static final String KEY_SAVED_TAB = "last_tab" ;

	private WaypointManagerFragment waypointFrag ;
	private TrackManagerFragment tracksFrag ;
	private MapCacheManager cache ;
	
	private boolean tabsLoaded = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cache = new MapCacheManager(this) ;
		this.getActionBar().setDisplayHomeAsUpEnabled(true);

		Tab tWpt = this.getActionBar().newTab()
				.setText(R.string.manager_tab_waypoints)
				.setTag("tab_waypoints")
				.setTabListener(this) ;
		Tab tTrack = this.getActionBar().newTab()
				.setText(R.string.manager_tab_tracks)
				.setTag("tab_tracks")
				.setTabListener(this) ;
		this.getActionBar().addTab(tWpt);
		this.getActionBar().addTab(tTrack);
		this.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//restore last tab
		int lastTab = this.getPreferences(MODE_PRIVATE).getInt(KEY_SAVED_TAB, 0) ;
		Log.i(TAG, "trying to restore tab at index " + lastTab) ;
		this.getActionBar().setSelectedNavigationItem(lastTab);
		tabsLoaded = true ;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == android.R.id.home) {
			this.finish();
		} else if(id == R.id.action_share_gpx) {
			FileExporter fe = this.getExporter() ;
			File export = this.getShareTempFile(".gpx") ;
			try {
				fe.writeGPX(export);
				share(export, "application/gpx+xml") ;
			} catch(IOException e) {
				Toast.makeText(this, R.string.export_error_writeerror, Toast.LENGTH_SHORT).show();
			}
			return true ;
		} else if(id == R.id.action_share_kml) {
			FileExporter fe = this.getExporter() ;
			File export = this.getShareTempFile(".kml") ;
			try {
				fe.writeKML(export);
				share(export, "application/vnd.google-earth.kml+xml") ;
			} catch(IOException e) {
				Toast.makeText(this, R.string.export_error_writeerror, Toast.LENGTH_SHORT).show();
			}
			return true ;
		}
		return super.onOptionsItemSelected(item);
	}

	private FileExporter getExporter() {
		List<Waypoint> wpts ;
		if(waypointFrag != null) {
			wpts = waypointFrag.getSelectedItems() ;
		} else {
			wpts = null ;
		}
		List<Track> trks ;
		if(tracksFrag != null) {
			trks = tracksFrag.getSelectedItems() ;
		} else {
			trks = null ;
		}
		return new FileExporter(this, wpts, trks) ;
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
			startActivity(i) ;
		} catch(ActivityNotFoundException e) {
			Toast.makeText(this, R.string.export_error_noactivity, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Check if the fragment is already initialized

		if(tab.getTag().equals("tab_waypoints")) {
			if(waypointFrag == null)
				waypointFrag = new WaypointManagerFragment() ;
			ft.replace(android.R.id.content, waypointFrag) ;
		} else if(tab.getTag().equals("tab_tracks")) {
			if(tracksFrag == null)
				tracksFrag = new TrackManagerFragment() ;
			ft.replace(android.R.id.content, tracksFrag) ;
		} else {
			//do nothing
		}

		if(tabsLoaded) {
			SharedPreferences prefs = this.getPreferences(MODE_PRIVATE) ;
			int tabPos = tab.getPosition() ;
			prefs.edit().putInt(KEY_SAVED_TAB, tabPos).commit() ;
			Log.i(TAG, "saved tab position " + tabPos) ;
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		//nothing for now ;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		//do nothing
	}

	public static class WaypointManagerFragment extends MultiListSelectFragment<Waypoint> {

		WaypointManager waypoints ;
		WaypointDialog waypointDialog ;
		boolean waypointDialogShowing ;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if(waypoints == null) {
				waypoints = new WaypointManager(getActivity(), null) ;
				waypointDialog = new WaypointDialog(getActivity(), waypoints) ;
				waypointDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						setItems(waypoints.getAll()) ;
						waypointDialogShowing = false ;
					}
				});
				waypointDialogShowing = false ;
				this.setItems(waypoints.getAll());
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			if(waypointDialogShowing)
				waypointDialog.show();
		}

		@Override
		public void onPause() {
			if(waypointDialogShowing)
				waypointDialog.dismiss();
			super.onPause();
		}

		@Override
		protected CharSequence getItemTitle(Waypoint item) {
			return item.name ;
		}

		@Override
		protected CharSequence getItemSubtext(Waypoint item) {
			String desc = "" ;
			String div = "" ;
			if(item.description != null && item.description.length() > 0) {
				desc = item.description ;
				div = "<br />" ;
			}
			
			String date = DateFormatting.getDateFormat(getActivity()).format(new Date(item.time)) ;
			String loc = LocationFormat.formatLocationHtml(getActivity(), item.lat, item.lon) ;

			String alt = "" ;
			String altSep = "" ;
			if(!Double.isNaN(item.altitude)) {
				String[] units = new String[] {"m", "ft"} ;
				String unit = units[Units.getUnitCategoryConstant(getActivity())] ;
				double valueU = Units.fromSI(item.altitude, unit) ;
				String altString = new DecimalFormat("0").format(valueU) ;
				alt = String.format("%s %s", altString, unit) ;
				altSep = " @ " ;
			}

			return Html.fromHtml(loc + altSep + alt + " " + date + div + desc)  ;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			if (id == R.id.action_delete) {
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity()) ;
				b.setTitle(R.string.manager_delete_title) ;
				final List<Waypoint> items = this.getSelectedItems() ;
				b.setMessage(String.format(getString(R.string.manager_delete_message), items.size())) ;
				b.setNegativeButton(R.string.manager_delete_cancel, null) ;
				b.setPositiveButton(R.string.manager_delete_delete, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int deleted = 0 ;
						for(Waypoint w: items) {
							if(waypoints.remove(w.id))
								deleted++ ;
						}
						if(deleted != items.size()) {
							Toast.makeText(getActivity(), R.string.manager_error_nodelete, Toast.LENGTH_SHORT).show() ;
						} else {
							Toast.makeText(getActivity(), String.format(getString(R.string.manager_delete_waypoint_success), deleted), Toast.LENGTH_SHORT).show(); ;
							setItems(waypoints.getAll()) ;
						}
					}
				}) ;
				b.show() ;
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

		@Override
		protected void onItemClick(Waypoint item) {
			Log.i(TAG, "waypoint clicked! " + item.name) ;
			waypointDialog.setWaypoint(item);
			waypointDialog.show();
			waypointDialogShowing = true ;
		}

		@Override
		protected String getNoItemsText() {
			return getString(R.string.manager_nowaypoints) ;
		}

	}

	public static class TrackManagerFragment extends MultiListSelectFragment<Track> {

		TrackManager tracks ;
		TrackDialog trackDialog ;
		boolean trackDialogShowing ;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if(tracks == null) {
				tracks = new TrackManager(this.getActivity()) ;
				trackDialog = new TrackDialog(getActivity(), tracks) ;
				trackDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						setItems(tracks.getTracks()) ;
						trackDialogShowing = false ;
					}
				});
				trackDialogShowing = false ;
				this.setItems(tracks.getTracks());
			}
		}

		@Override
		public void onResume() {
			super.onResume();
			if(trackDialogShowing)
				trackDialog.show();
		}

		@Override
		public void onPause() {
			if(trackDialogShowing)
				trackDialog.dismiss();
			super.onPause();
		}


		@Override
		protected CharSequence getItemTitle(Track item) {
			if(item.activeTrack) {
				return String.format(getString(R.string.manager_logging_track), item.name) ;
			} else {
				return item.name ;
			}
		}

		@Override
		protected CharSequence getItemSubtext(Track item) {
			String format = getString(R.string.manager_tracksubtext) ;
			String created = DateFormatting.getDateTimeFormat(getActivity()).format(new Date(item.timeDateBegin)) ;
			return String.format(format, created, item.numPoints) ;
		}

		@Override
		protected void onItemClick(Track item) {
			trackDialog.setTrack(item);
			trackDialog.show() ;
			trackDialogShowing = true ;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			if (id == R.id.action_delete) {
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity()) ;
				b.setTitle(R.string.manager_delete_title) ;
				final List<Track> items = this.getSelectedItems() ;
				b.setMessage(String.format(getString(R.string.manager_delete_message), items.size())) ;
				b.setNegativeButton(R.string.manager_delete_cancel, null) ;
				b.setPositiveButton(R.string.manager_delete_delete, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int deleted = 0 ;
						for(Track t: items) {
							if(tracks.removeTrack(t.id))
								deleted++ ;
						}
						if(deleted != items.size()) {
							Toast.makeText(getActivity(), R.string.manager_error_nodelete, Toast.LENGTH_SHORT).show() ;
						} else {
							Toast.makeText(getActivity(), String.format(getString(R.string.manager_delete_track_success)), Toast.LENGTH_SHORT).show(); ;
							setItems(tracks.getTracks()) ;
						}
							
					}
				}) ;
				b.show() ;
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

		@Override
		protected String getNoItemsText() {
			return getString(R.string.manager_notracks) ;
		}

	}

	public static abstract class MultiListSelectFragment<T> extends ListFragment {

		private SelectableAdapter adapter ;
		private boolean[] selectedState ;
		private boolean checkState ;
		private List<T> selectedItems ;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if(selectedItems == null) {
				Log.i("MultiSelectListFragment", "calling onCreate and setting check state to false") ;
				checkState = false ;
				this.setHasOptionsMenu(true);
				selectedItems = new ArrayList<T>() ;
				selectedState = new boolean[0] ;
			}
		}

		public void setItems(List<T> items) {
			selectedState = new boolean[items.size()] ;
			for(int i=0; i<selectedState.length; i++) {
				selectedState[i] = false ;
			}
			if(items.size() > 0) {
				adapter = new SelectableAdapter(items) ;
				selectedItems.clear();
				this.setCheckState(false) ;
				this.setListAdapter(adapter);
			} else {
				adapter = null ;
				this.setListAdapter(new ArrayAdapter<String>(getActivity(), 
						android.R.layout.simple_list_item_1, 
						new String[] {this.getNoItemsText()}));
			}
			getActivity().invalidateOptionsMenu();
		}

		public void selectNone() {
			for(int i=0; i<selectedState.length; i++) {
				selectedState[i] = false ;
			}
		}

		public void selectAll() {
			for(int i=0; i<selectedState.length; i++) {
				selectedState[i] = true ;
			}
		}

		public boolean getCheckState() {
			return checkState ;
		}

		public void setCheckState(boolean state) {
			Log.i("MultiSelectListFragment", "setting check state to " + state) ;
			checkState = state ;
			if(!state)
				selectNone() ; //clear selection when check state is turned off
			if(adapter != null)
				adapter.notifyDataSetChanged();
		}

		private void resetSelectedItems() {
			selectedItems.clear();
			for(int i=0; i<selectedState.length; i++) {
				if(selectedState[i])
					selectedItems.add(adapter.getItem(i)) ;
			}
		}

		public List<T> getSelectedItems() {
			return selectedItems ;
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			if(checkState) {
				selectedState[position] = !selectedState[position] ;
				adapter.notifyDataSetChanged();
				this.resetSelectedItems();
				this.getActivity().invalidateOptionsMenu();
			} else {
				this.onItemClick(adapter.getItem(position));
			}
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.location_manager, menu);

			MenuItem selectItem = menu.findItem(R.id.action_select) ;
			int title ;
			int icon ;
			if(this.getCheckState()) {
				title = R.string.action_select_off ;
				icon = R.drawable.ic_action_select_off ;
			} else {
				title = R.string.action_select_on ;
				icon = R.drawable.ic_action_select_on ;
			}
			selectItem.setTitle(title) ;
			selectItem.setIcon(icon) ;

			MenuItem selectAllItem = menu.findItem(R.id.action_selectall) ;
			selectAllItem.setVisible(this.getCheckState()) ;
			
			Log.i(TAG, "number of selected items: " + selectedItems.size()) ;
			MenuItem deleteItem = menu.findItem(R.id.action_delete) ;
			MenuItem shareItem = menu.findItem(R.id.action_share) ;
			if(selectedItems.size() > 0 && this.getCheckState()) {
				deleteItem.setVisible(true) ;
				shareItem.setVisible(true) ;
			} else {
				deleteItem.setVisible(false) ;
				shareItem.setVisible(false) ;
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle action bar item clicks here. The action bar will
			// automatically handle clicks on the Home/Up button, so long
			// as you specify a parent activity in AndroidManifest.xml.
			int id = item.getItemId();
			if (id == R.id.action_select) {
				this.setCheckState(!getCheckState());
				this.getActivity().invalidateOptionsMenu();
				return true;
			} else if(id == R.id.action_selectall) {
				this.selectAll() ;
				this.adapter.notifyDataSetChanged();
				this.resetSelectedItems();
				this.getActivity().invalidateOptionsMenu();
				return true ;
			}
			return super.onOptionsItemSelected(item);
		}

		protected abstract CharSequence getItemTitle(T item) ;
		protected abstract CharSequence getItemSubtext(T item) ;
		protected abstract void onItemClick(T item) ;
		protected abstract String getNoItemsText() ;

		private class SelectableAdapter extends ArrayAdapter<T> {

			SelectableAdapter(List<T> list) {
				super(getActivity(), R.layout.location_manager_item, R.id.manager_item_title, list) ;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				TextView title = (TextView)v.findViewById(R.id.manager_item_title) ;
				TextView subtext = (TextView)v.findViewById(R.id.manager_item_subtext) ;
				CheckBox cb = (CheckBox)v.findViewById(R.id.manager_item_check) ;
				T item = this.getItem(position) ;

				title.setText(getItemTitle(item));
				subtext.setText(getItemSubtext(item));
				cb.setChecked(selectedState[position]);
				if(checkState) {
					cb.setVisibility(View.VISIBLE);
				} else {
					cb.setVisibility(View.GONE);
				}
				return v ;
			}



		}

	}

}
