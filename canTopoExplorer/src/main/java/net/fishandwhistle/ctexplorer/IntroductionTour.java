package net.fishandwhistle.ctexplorer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;

public class IntroductionTour {

	private Context context ;

	public IntroductionTour(Context context) {
		this.context = context ;
	}

	public void startTour() {
		AlertDialog.Builder b = this.getBuilder(0) ;
		buildTour(b, 0) ;
		b.create().show();
	}

	private AlertDialog.Builder buildTour(AlertDialog.Builder seed, final int index) {
		final AlertDialog.Builder next = this.getBuilder(index+1) ;
		if(next != null) {
			seed.setPositiveButton("Next", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					AlertDialog.Builder nextNext = buildTour(next, index+1) ;
					AlertDialog d = nextNext.create() ;
					d.show();
				}
			}) ;
		} else {
			seed.setPositiveButton("That's it!", null) ;
		}
		
		return seed ;
	}

	private AlertDialog.Builder getBuilder(int index) {
		switch(index) {
		case 0:
			return this.getMaptypeDialog() ;
		case 1:
			return this.getLayersDialog() ;
		case 2:
			return this.getGridDialog() ;
		case 3:
			return this.getTopoDialog() ;
		case 4:
			return this.getMyLocationDialog() ;
		case 5:
			return this.getMarkMyLocationDialog() ;
		case 6:
			return this.getTrackingDialog() ;
		case 7:
			return this.getMeasureDialog() ;
		case 8:
			return this.getSearchDialog() ;
		case 9:
			return this.getLocationManagerDialog() ;
		case 10:
			return this.getPreferencesDialog() ;
		default:
			return null ;
		}
	}

	private AlertDialog.Builder baseBuilder() {
		ContextThemeWrapper ct = new ContextThemeWrapper(context, android.R.style.Theme_Holo);
		return new AlertDialog.Builder(ct) ;
	}

	private AlertDialog.Builder getMaptypeDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Map Type" ;
		String message = "Users of Google Maps will be familiar with the option to switch the basemap "
				+ "between Map, Imagery, Imagery with labels (hybrid), Terrain, and None (saves on "
				+ "data if you're just using Toporama maps)." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_maptype_map) ;
		return b ;
	}

	private AlertDialog.Builder getGridDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "NTS Grid On/Off" ;
		String message = "Use this button to toggle the NTS Grid on or off. the National Topographic System "
				+ "(NTS) is how topographic maps are organized in Canada, as well as some additional "
				+ "information available through Government of Canada portals. When you download maps "
				+ "(which you can do by long-pressing the map when you've zoomed in far enough) they will "
				+ "appear shaded in the grid so you can see where your downloaded maps are geographically." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_grid_on) ;
		return b ;
	}

	private AlertDialog.Builder getLayersDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Layers" ;
		String message = "Click this to open a  menu that has a few options regarding what layers "
				+ "you'd like to see on the map, such as Topo maps, Waypoints, Tracks, etc." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_layers) ;
		return b ;
	}
	
	private AlertDialog.Builder getTopoDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Toporama Options" ;
		String message = "This button will open a dialog where you can turn Toporama topo maps "
				+ "on or off and adjust their transparency. Experiment with using an Imagery or"
				+ " terrain basemap with partial transparency for some cool effects." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_topo_on) ;
		return b ;
	}

	private AlertDialog.Builder getMyLocationDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "GPS Options" ;
		String message = "This menu will open a list of options regarding using this app as a "
				+ "basic GPS device. An option with the same icon under this menu "
				+ "will toggle your location on or off from the map. When turned "
				+ "on, it will display your GPS location on the map, and you'll have the option to"
				+ " mark your location with a waypoint." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_location_on) ;
		return b ;
	}

	private AlertDialog.Builder getSearchDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Search" ;
		String message = "Use this option to search for locations or street addresses just like you would in "
				+ "Google Maps." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_search) ;
		return b ;
	}

	private AlertDialog.Builder getMarkMyLocationDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Mark My Location" ;
		String message = "When My Location is enabled, select this option to save your current "
				+ "location information." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_mark_my_location) ;
		return b ;
	}


	private AlertDialog.Builder getTrackingDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Start Tracking" ;
		String message = "When tracking is enabled, the device will save your location every "
				+ "several minutes (you can configure exactly how many minutes in the Preferences "
				+ "screen) and display your route on the map. This isn't designed to be particularly "
				+ "accurate but to give you a rough idea of your location history. Select "
				+ "'Stop Tracking' to turn tracking off." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_tracking_on) ;
		return b ;
	}

	private AlertDialog.Builder getMeasureDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Measure Area/Measure Distance" ;
		String message = "Use these two options to measure distances and areas on the map. "
				+ "Without going into the mathematical details, you won't be able to measure"
				+ " large areas with the Measure Area tool, which is just a matter of how much "
				+ "work I have to do to make that happen. You can change the units in which your "
				+ "measurement is given in the Preferences dialog." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_measure_distance) ;
		return b ;
	}

	private AlertDialog.Builder getLocationManagerDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Location Manager" ;
		String message = "Open the Location Manager to manage/export your tracks to GPX or KML "
				+ "formats, or to clear the library or organize them into categories." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		b.setIcon(R.drawable.ic_action_locmanager) ;
		return b ;
	}

	private AlertDialog.Builder getPreferencesDialog() {
		AlertDialog.Builder b = baseBuilder() ;
		String title = "Preferences" ;
		String message = "Open the Preferences screen to change units, location formats, "
				+ "clear your map library and other options." ;
		b.setTitle(title) ;
		b.setMessage(message) ;
		return b ;
	}

	
	
}
