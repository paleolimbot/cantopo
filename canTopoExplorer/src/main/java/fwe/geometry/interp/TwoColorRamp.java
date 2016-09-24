package fwe.geometry.interp;

public class TwoColorRamp implements ColorRamp {

	private ColorARGB color1 ;
	private ColorARGB color2 ;
	
	public TwoColorRamp(ColorARGB color1, ColorARGB color2) {
		this.color1 = color1 ;
		this.color2 = color2 ;
	}
	
	@Override
	public ColorARGB get(double fraction) {
		return ColorARGB.average(color1, color2, fraction) ;
	}

	@Override
	public ColorRamp reverse() {
		return new TwoColorRamp(color2, color1) ;
	}

}
