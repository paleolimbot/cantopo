package ca.fwe.locations.geometry;

public class Line {
	
	private XY c1 ;
	private XY c2 ;
	private boolean hasZ ;

	public Line(XY start, XY end) {
		c1 = start ;
		c2 = end ;
		hasZ = start.hasZ() && end.hasZ() ;
	}

	public boolean hasZ() {
		return hasZ ;
	}

	public XY getStart() {
		if(hasZ)
			return c1 ;
		else
			return c1.ignoreZ() ;
	}

	public XY getEnd() {
		if(hasZ)
			return c2 ;
		else
			return c2.ignoreZ() ;
	}

	public Vector toVector() {
		double deltaX = c2.x() - c1.x() ;
		double deltaY = c2.y() - c1.y() ;
		double deltaZ = c2.z() - c1.z() ;
		return new Vector(deltaX, deltaY, deltaZ) ;
	}

	public double length() {
		return this.toVector().magnitude() ;
	}
	
	public double slope() {
		double x1 = c1.x();
		double x2 = c2.x();
		double y1 = c1.y();
		double y2 = c2.y();
		
		return (y2-y1) / (x2-x1) ;
	}

	public boolean equals(Object o) {
		//order does not matter, as with a vector
		if(o instanceof Line) {
			Line l = (Line) o ;
			if(this.getStart().equals(l.getStart()) && this.getEnd().equals(l.getEnd())) {
				return true ;
			} else if(this.getEnd().equals(l.getStart()) && this.getStart().equals(l.getEnd())) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}

	public XY get(double fractionAlongLine) {
		double deltaX = c2.x() - c1.x() ;
		double deltaY = c2.y() - c1.y() ;
		double deltaZ = c2.z() - c1.z() ;
		double newX = c1.x() + fractionAlongLine * deltaX ;
		double newY = c1.y() + fractionAlongLine * deltaY ;
		double newZ = c1.z() + fractionAlongLine * deltaZ ;
		return new XY(newX, newY, newZ) ;
	}



	public static double interpolateX(Line line, double yValue) {
		//if point1.y() == point2.y(), is a vertical line, will produce an infinity value
		XY point1 = line.getStart() ;
		XY point2 = line.getEnd() ;
		double x1 = point1.x();
		double x2 = point2.x();
		double y1 = point1.y();
		double y2 = point2.y();

		return (x2-x1) / (y2-y1) * (yValue - y1) + x1 ;

	}

	public static double interpolateY(Line line, double xValue) {
		//if point1.x() == point2.x(), is a vertical line, will produce an infinity value
		XY point1 = line.getStart() ;
		XY point2 = line.getEnd() ;
		double x1 = point1.x();
		double x2 = point2.x();
		double y1 = point1.y();
		double y2 = point2.y();

		return (y2-y1) / (x2-x1) * (xValue - x1) + y1 ;
	}

	public String toString() {
		return "Line from " + c1.toString() + " to " + c2.toString() ;
	}
	
}
