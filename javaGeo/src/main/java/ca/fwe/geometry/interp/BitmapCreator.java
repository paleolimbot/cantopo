package ca.fwe.geometry.interp;

import ca.fwe.locations.geometry.Bounds;
import ca.fwe.locations.geometry.XY;

public class BitmapCreator {

	private Interpolator interp ;
	private ColorRamp ramp ;
	private Projection proj ;
	private ProgressUpdater update ;
	
	private int currentPercent ;
	
	private int height ;
	private int width ;
	
	private int totalPixels ;
	
	private boolean cancelled ;
	
	public BitmapCreator(Interpolator interpolator, int width, int height) {
		this.height = height ;
		this.width = width ;
		totalPixels = width * height ;
		this.interp = interpolator ;
		this.proj = new Projection(interp.getBounds(), height, width) ;
		this.cancelled = false ;
		this.currentPercent = -1 ;
	}
	
	public void setRamp(ColorRamp ramp) {
		this.ramp = ramp;
	}
	

	public void cancel() {
		this.cancelled = true ;
	}
	
	public void setUpdater(ProgressUpdater update) {
		this.update = update;
	}
	
	public boolean createBitmap(PixelSetter setter) {
		int pixelsDone = 0 ;
		for(int x=0; x<width; x++) {
			if(cancelled)
				break ;
			for(int y=0; y<height; y++) {
				int[] cartesianPixels = new int[] {x, y} ;
				XY point = proj.getPoint(cartesianPixels) ;
				double zVal = interp.getZValue(point, Double.NaN) ;
				ColorARGB c = ColorARGB.ALPHA ;
				if(!Double.isNaN(zVal)) {
					c = this.getColor(zVal) ;
				}
				int[] graphicsPx = this.getGraphicsPixels(cartesianPixels) ;
				setter.setPixel(graphicsPx[0], graphicsPx[1], c) ;
				pixelsDone++ ;
				if(update != null) {
					int percent = Math.round((100 * (float)pixelsDone) / ((float)totalPixels)) ;
					if(percent != currentPercent) {
						update.updateProgress(percent) ;
						currentPercent = percent ;
					}
				}
			}
		}
		
		return !cancelled ;
	}

	private ColorARGB getColor(double zValue) {
		Bounds b = interp.getBounds() ;
		double ratio = ratio(b.getMinZ(), b.getMaxZ(), zValue) ;
		return ramp.get(ratio) ;
	}
	
	private static class Projection {
		
		Bounds bounds ;
		int height ;
		int width ;
		
		double resX ;
		double resY ;
		
		public Projection(Bounds bounds, int height, int width) {
			this.bounds = bounds ;
			this.height = height ;
			this.width = width ;
			
			resX = bounds.width() / width ;
			resY = bounds.height() / height ;
		}
		
		public int[] getCartesianPixels(XY point) {
			double ratioX = ratio(bounds.getMinX(), bounds.getMaxX(), point.x()) ;
			double ratioY = ratio(bounds.getMinY(), bounds.getMaxY(), point.y()) ;
			double pixelX = value(0, width, ratioX) ;
			double pixelY = value(0, height, ratioY) ;
			
			return new int[] {(int)Math.round(pixelX), (int)Math.round(pixelY)} ;
		}
		
		public XY getPoint(int[] pixels) {
			double ratioX = ratio(0, width, pixels[0]) ;
			double ratioY = ratio(0, height, pixels[1]) ;
			double pointX = value(bounds.getMinX(), bounds.getMaxX(), ratioX) + (resX / 2.0) ;
			double pointY = value(bounds.getMinY(), bounds.getMaxY(), ratioY) + (resY / 2.0) ;
			
			return new XY(pointX, pointY) ;
		}
		
	}
	
	private int[] getGraphicsPixels(int[] cartesianPixels) {
		int x = cartesianPixels[0] ;
		int y = (height - 1) - cartesianPixels[1] ;
		return new int[] {x, y} ;
	}
	
	private static double value(double min, double max, double ratio) {
		double range = max - min ;
		double delta = ratio * range ;
		return min + delta ;
	}
	
	private static double ratio(double min, double max, double value) {
		double range = max - min ;
		double delta = value - min ;
		return delta / range ;
	}
	
	public interface PixelSetter {
		public void setPixel(int x, int y, ColorARGB color) ;
	}
	
	public interface ProgressUpdater {
		public void updateProgress(int percent) ;
	}
	
	
}
