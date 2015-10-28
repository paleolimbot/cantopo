package ca.fwe.geometry.interp;

public interface ColorRamp {
	
	public static final ColorRamp RAINBOW = new RainbowRamp(0.1, 128, 127) ;
	public static final ColorRamp MONOCHROME = new TwoColorRamp(ColorARGB.BLACK, ColorARGB.WHITE) ;
	
	public ColorARGB get(double fraction) ;
	public ColorRamp reverse() ;
	
}
