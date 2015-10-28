package ca.fwe.geometry.interp;


public class RainbowRamp implements ColorRamp {

	private double frequency ;
	private double center ;
	private double width ;
	private boolean reversed ;
	private int alpha ;
	
	public RainbowRamp(double frequency, double center, double width, int alpha, boolean reversed) {
		this.frequency = frequency;
		this.center = center;
		this.width = width;
		this.reversed = reversed;
		this.alpha = alpha ;
	}

	public RainbowRamp(double frequency, double center, double width, int alpha) {
		this(frequency, center, width, alpha, false) ;
	}
	
	public RainbowRamp(double frequency, double center, double width) {
		this(frequency, center, width, 255, false) ;
	}

	@Override
	public ColorARGB get(double fraction) {
		return this.getRainbow(fraction * 32) ;
	}

	private ColorARGB getRainbow(double i) {
		//takes number from 0 to 32
		//frequency = 0.10 center = 128 width = 127
		
		if(i < 0)
			i=0 ;
		if(i>32)
			i=32 ;
		
		if(reversed)
			i = 32 - i ; //reverse
		int red   = (int) Math.round(Math.sin(frequency*i + 0) * width + center);
		int green = (int) Math.round(Math.sin(frequency*i + 2) * width + center);
		int blue  = (int) Math.round(Math.sin(frequency*i + 4) * width + center);
		return new ColorARGB(alpha, red, green, blue) ;
	}

	@Override
	public ColorRamp reverse() {
		return new RainbowRamp(frequency, center, width, alpha, !reversed) ;
	}

}
