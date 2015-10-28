package net.fishandwhistle.ctexplorer;

import java.text.DecimalFormat;

import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.Toast;

public class Preferences extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		
		
		
		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
		.replace(android.R.id.content, new PFrag())
		.commit();

	}

	private static String megabyteString(long bytes) {
		double lengthMb = bytes / 1048576.0 ;
		return new DecimalFormat("0.0").format(lengthMb) ;
	}
	
	public static class PFrag extends PreferenceFragment {

		MapCacheManager cache ;
		Preference clearLib ;
		
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			this.setupCache();
			//set default preferences
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()) ;
			if(!prefs.contains("xml_units"))
				prefs.edit().putString("xml_units", "0").commit() ;
			if(!prefs.contains("xml_location_format"))
				prefs.edit().putString("xml_location_format", "0").commit() ;
			if(!prefs.contains("xml_log_point_duration"))
				prefs.edit().putString("xml_log_point_duration", "3").commit() ;
			
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preferences) ;
			Preference libloc = this.findPreference("xml_library_location") ;
			
			libloc.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Toast.makeText(getActivity(), "I know, I know, you want to put this on your external SD card. Unfortunately, this is harder to do than it seems, so stay calm and when I figure it out I'll figure it out. Sorry!", Toast.LENGTH_SHORT).show() ;
					return true ;
				}
			});
			
			clearLib = this.findPreference("xml_clear_library") ;
			if(cache != null) {
				libloc.setSummary(cache.getMapCacheFolder().toString());
				setClearCacheSummary() ;
			}
			
			clearLib.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					setupCache() ;
					long[] cacheSize = cache.cacheSize() ;
					AlertDialog.Builder b = new AlertDialog.Builder(getActivity()) ;
					b.setTitle("Confirm Clear Library") ;
					b.setMessage(String.format("About to delete %s files (%s MB)", cacheSize[0], megabyteString(cacheSize[1]))) ;
					b.setPositiveButton("Delete Files", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cache.clearCache() ;
							setClearCacheSummary() ;
						}
					}) ;
					b.setNegativeButton("Cancel", null) ;
					b.create().show();
					return true;
				}
				
			});
			
			
		}
		
		private void setClearCacheSummary() {
			if(cache != null) {
				long[] cacheSize = cache.cacheSize() ;
				long mapsheets = cacheSize[0] / 13 ;
				String message = String.format("%s files (%s mapsheets, %s MB)", cacheSize[0], mapsheets, megabyteString(cacheSize[1])) ;
				clearLib.setSummary(message);
			}
		}
		
		private void setupCache() {
			if(cache == null && this.getActivity() != null)
				cache = new MapCacheManager(this.getActivity()) ;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			this.onBackPressed();
			return true ;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}

