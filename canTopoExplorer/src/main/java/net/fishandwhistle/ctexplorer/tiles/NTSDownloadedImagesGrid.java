package net.fishandwhistle.ctexplorer.tiles;

import net.fishandwhistle.ctexplorer.backend.MapCacheManager;
import android.content.Context;
import android.graphics.Color;
import fwe.locations.geometry.Bounds;
import fwe.nts.NTSMapSheet;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PolygonOptions;

public class NTSDownloadedImagesGrid extends NTSGrid {

	private static final double SCALE_RATIO = 1.8 ;
	
	private static final int ALPHA_ALL = 65 ;
	private static final int ALPHA_SOME = 33 ;
	
	private MapCacheManager cache ;
	
	public NTSDownloadedImagesGrid(Context context, GoogleMap map) {
		super(context, map);
		cache = new MapCacheManager(context) ;
		cache.setCachingEnabled(true);
	}
	
	@Override
	protected int getScale(Bounds bounds, double invScale) {
		if(invScale <= NTSImageTile.INV_SCALE_50K) {
			return -1 ;
		} else if(invScale <= MAX_SCALE_50K*SCALE_RATIO) {
			return NTSMapSheet.SCALE_50K ;
		} else if(invScale <= MAX_SCALE_250K*SCALE_RATIO) {
			return NTSMapSheet.SCALE_250K ;
		} else if(invScale <= MAX_SCALE_SERIES) {
			return NTSMapSheet.SCALE_SERIES ;
		} else {
			return -1 ;
		}
	}
	
	@Override
	protected void prepareForMap(GoogleMap map, GeoMapSheet sheet) {
		int alpha = alpha(sheet.getNTSSheet()) ;
		if(alpha != 0) {
			PolygonOptions po = sheet.getPolygonOptions() ;
			po.fillColor(getFillColor(alpha)) ;
			po.strokeWidth(0) ;
		}
	}

	@Override
	protected void addToMap(GoogleMap map, GeoMapSheet sheet) {
		if(sheet.hasPolygonOptions())
			sheet.addPolygon(map) ;
	}
	
	private int alpha(NTSMapSheet sheet) {
		if(cache.hasAnyFiles(sheet)) {
			if(cache.hasAllFiles(sheet))
				return ALPHA_ALL ;
			else
				return ALPHA_SOME ;
		} else {
			return 0 ;
		}
	}
	
	@Override
	public void onBoundsUpdated(Bounds newBounds, double invScale) {
		cache.rebuildCache();
		super.onBoundsUpdated(newBounds, invScale);
	}

	private static int getFillColor(int alpha) {
		return Color.argb(alpha, 0, 0, 255) ;
	}

}
