package net.fishandwhistle.ctexplorer.tiles;

import java.io.File;

import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import fwe.locations.geometry.Bounds;
import fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;

public class NTSImageTile extends NTSTileManager {

	public static final double INV_SCALE_BLOCK = 1.7e5 ;
	public static final double INV_SCALE_50K = 6e5 ;
	
	private MapCacheManager cache ;
	private float transparency ;
	
	public NTSImageTile(Context context, GoogleMap map) {
		super(context, map);
		cache = new MapCacheManager(context) ;
		transparency = 0 ;
	}
	
	public void setTransparency(float transparency) {
		this.transparency = transparency ;
		for(GeoMapSheet s: sheets) {
			if(s.getOverlay() != null)
				s.getOverlay().setTransparency(transparency);
			s.getGroundOverlayOptions().transparency(transparency) ;
		}
	}

	@Override
	protected int getScale(Bounds bounds, double invScale) {
		if(invScale <= INV_SCALE_BLOCK)
			return NTSMapSheet.SCALE_BLOCK ;
		else if(invScale <= INV_SCALE_50K)
			return NTSMapSheet.SCALE_50K ;
		else
			return -1 ;
	}
	
	@Override
	protected void prepareForMap(GoogleMap map, GeoMapSheet sheet) {
		File mapfile = cache.getMapFile(sheet.getNTSSheet()) ;
		if(mapfile.exists()) {
			sheet.getGroundOverlayOptions()
				.image(BitmapDescriptorFactory.fromPath(mapfile.getAbsolutePath()))
				.transparency(transparency) ;
		}
	}

	@Override
	protected void addToMap(GoogleMap map, GeoMapSheet sheet) {
		if(sheet.hasGroundOverlayOptions()) {
			GroundOverlay result = sheet.addGroundOverlay(map);
            if(result == null) {
                Toast.makeText(context, "Error adding " + sheet.getNTSSheet().getNtsId(), Toast.LENGTH_SHORT).show();
                // delete offending file (is probably corrupted)
                File mapfile = cache.getMapFile(sheet.getNTSSheet()) ;
                if(!mapfile.delete()) {
                    Log.e("NTSImageTile", "addToMap: failed to delete " + mapfile);
                }
            }
		}
	}

}
