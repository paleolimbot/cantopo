package net.fishandwhistle.ctexplorer.maptools;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;

public abstract class MapTool implements GoogleMap.OnMapClickListener, 
										GoogleMap.OnMapLongClickListener,
										GoogleMap.OnMarkerClickListener {

	protected Context context ;
	protected GoogleMap map ;
	private boolean enabled ;
	
	public MapTool(Context c, GoogleMap m) {
		context = c ;
		map = m ;
		enabled = false ;
	}	
	
	public void setEnabled(boolean flag) {
		enabled = flag ;
		this.onSetEnabled(flag);
	}
	
	protected void onSetEnabled(boolean flag) {
		//default: do nothing
	}
	
	public boolean isEnabled() {
		return enabled ;
	}
	
}
