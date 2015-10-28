package net.fishandwhistle.ctexplorer;

import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class Upgrader {

	private static String VERSION_KEY = "__ct_version" ;

	public static boolean upgrade(Context context) {
		onStartApplication(context) ;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context) ;
		int storedVersion = prefs.getInt(VERSION_KEY, -1) ;
		PackageInfo pinfo;
		try {
			pinfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			int packageVersion = pinfo.versionCode;
			if(storedVersion != packageVersion) {
				onUpgrade(context, storedVersion, packageVersion) ;
				prefs.edit().putInt(VERSION_KEY, packageVersion).commit() ;
				return true ;
			} else {
				//do nothing
				Log.i("Upgrader", "no upgrade necessary!") ;
				return false ;
			}
		} catch (NameNotFoundException e) {
			throw new RuntimeException("package not found in on upgrade!") ;
		}

	}

	private static void onUpgrade(Context context, int version1, int version2) {
		//nothing yet, first version!
	}
	
	private static void onStartApplication(Context context) {
		MapCacheManager cache = new MapCacheManager(context) ;
		int num = cache.clearTemporaryFiles() ;
		Log.i("Upgrader", "cleared " + num + " temporary files") ;
	}

}
