package fwe.geometry.interp;

import fwe.mathutil.FweMath;

public class ColorARGB {
	
	public static final ColorARGB ALPHA = new ColorARGB(0, 0, 0, 0) ;
	public static final ColorARGB BLACK = new ColorARGB(0, 0, 0) ;
	public static final ColorARGB WHITE = new ColorARGB(255, 255, 255) ;
	public static final ColorARGB RED = new ColorARGB(255, 0, 0) ;
	public static final ColorARGB GREEN = new ColorARGB(0, 255, 0) ;
	public static final ColorARGB BLUE = new ColorARGB(0, 0, 255) ;
	
	public int red ;
	public int green ;
	public int blue ;
	public int alpha ;
	public ColorARGB(int red, int green, int blue) {
		this.red = red ;
		this.green = green ;
		this.blue = blue ;
		alpha = 255 ;
	}
	public ColorARGB(int alpha, int red, int green, int blue) {
		this.red = red ;
		this.green = green ;
		this.blue = blue ;
		this.alpha = alpha ;
	}
	
	public String toString() {
		return "#" + hex(alpha) + hex(red) + hex(green) + hex(blue) ;
	}
	
	private String hex(int val) {
		return FweMath.addZeros(Integer.toHexString(val), 2) ;
	}
	
	public ColorARGB averageWith(ColorARGB otherColor) {
		return average(this, otherColor, 0.5) ;
	}
	
	
	public static ColorARGB average(ColorARGB color1, ColorARGB color2, double fraction) {
		int alpha = interpolate(color1.alpha, color2.alpha, fraction) ;
		int red = interpolate(color1.red, color2.red, fraction) ;
		int green = interpolate(color1.green, color2.green, fraction) ;
		int blue = interpolate(color1.blue, color2.blue, fraction) ;
		return new ColorARGB(alpha, red, green, blue) ;
	}
	
	private static int interpolate(int value1, int value2, double ratio) {
		int range = value2 - value1 ;
		double delta = ratio * range ;
		return (int)Math.round(value1 + delta) ;
	}
}