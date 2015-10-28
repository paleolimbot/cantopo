package ca.fwe.locations.geometry;

public class RotatableBounds extends Bounds {

	private static final long serialVersionUID = -7598183707670976729L;
	private double rotation ;
	
	public RotatableBounds(XY p1, XY p2, double rotation) {
		super(p1, p2);
		this.rotation = rotation ;
	}

	public RotatableBounds(XY p1, XY p2) {
		this(p1, p2, 0) ;
	}

	public RotatableBounds(Bounds bounds, double rotation) {
		super(bounds) ;
		this.rotation = rotation ;
	}

	public double getRotation() {
		return rotation ;
	}
	
}
