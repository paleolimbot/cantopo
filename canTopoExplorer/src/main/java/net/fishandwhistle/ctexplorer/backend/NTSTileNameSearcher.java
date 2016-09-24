package net.fishandwhistle.ctexplorer.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import fwe.nts.NTSMapSheet;

public class NTSTileNameSearcher {

	private static final String TAG = "NTSTileNameSearcher" ;

	private AssetManager am ;

	private Map<String, String> loadedNames =  new HashMap<String, String>();
	private List<String> availableSeries = new ArrayList<String>() ;

	public NTSTileNameSearcher(Context context) {
		am = context.getResources().getAssets() ;
		try {
			String[] nameAssets = am.list("names") ;
			for(String s: nameAssets) {
				availableSeries.add(s) ;
			}
		} catch (IOException e) {
			Log.e(TAG, "error opening assets to list series files!", e) ;
			throw new RuntimeException("could not open assets!") ;
		}
	}

	public void cleanCache() {
		loadedNames.clear();
	}

	public String getName(NTSMapSheet sheet) {
		return getName(sheet.getNtsId(), true) ;
	}

	private String getName(String ntsId, boolean load) {
		if(loadedNames.containsKey(ntsId)) {
			return loadedNames.get(ntsId) ;
		} else if(load) {
			String series = ntsId.split("-")[0] ;
			loadSeries(series) ;
			return getName(ntsId, false) ;
		} else {
			return null ;
		}
	}

	private void loadSeries(String seriesName) {
		String assetName = seriesName + ".txt" ;
		try {
			if(availableSeries.contains(assetName)) {
				long start = System.currentTimeMillis() ;
				InputStream s = am.open("names/" + assetName) ;
				BufferedReader r = new BufferedReader(new InputStreamReader(s, "UTF-8")) ;
				String line ;
				int sheets = 0 ;
				while((line = r.readLine()) != null) {
					String[] keyval = line.split("\\|") ;
					if(keyval.length == 2) {
						loadedNames.put(keyval[0], keyval[1]) ;
						sheets++ ;
					}
				}
				r.close();
				s.close();
				long elapsed = System.currentTimeMillis() - start ;
				Log.i(TAG, "loaded " + sheets + " sheets in " + elapsed + "ms") ;
			} else {
				Log.i(TAG, "no asset name found: " + assetName) ;
			}
		} catch(IOException e) {
			//do nothing
			Log.e(TAG, "IOError on read assset for series name " + seriesName, e) ;
		}
	}

}
