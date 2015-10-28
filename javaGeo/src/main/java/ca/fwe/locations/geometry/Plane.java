package ca.fwe.locations.geometry;

public class Plane {

	private XY c1 ;
	private XY c2 ;
	private XY c3 ;

	private Line[] edges ;

	private double a ;
	private double b ;
	private double c ;
	private double d ;
	
	private Bounds bounds ;

	public Plane(XY coordinate1, XY coordinate2, XY coordinate3) {
		c1 = coordinate1 ;
		c2 = coordinate2 ;
		c3 = coordinate3 ;
		Vector v1 = new Line(c1, c2).toVector() ;
		Vector v2 = new Line(c1, c3).toVector() ;
		Vector normal = Vector.crossProduct(v1, v2).getUnitVector() ;
		this.setParameters(c1, normal) ;
		edges = new Line[] {new Line(c1, c2), new Line(c2, c3), new Line(c3, c1)} ;
		bounds = new Bounds(new XY[] {c1, c2, c3}) ;
	}

	public Plane(XY point, Vector normal) {
		this.setParameters(point, normal) ;
	}

	public Plane(Line l, XY firstPoint) {
		this(firstPoint, l.getStart(), l.getEnd()) ;
	}

	private void setParameters(XY point, Vector normal) {
		a = normal.i() ;
		b = normal.j() ;
		c = normal.k() ;
		d = (a * point.x() + b * point.y() + c*point.z()) ;
		c1 = point ;
	}

	public Vector getNormal() {
		return new Vector(a, b, c) ;
	}

	public XY getPoint() {
		return c1 ;
	}

	public XY[] getPoints() {
		return new XY[] {c1, c2, c3} ;
	}

	public Triangle2D getTriangle() {
		return new Triangle2D(c1, c2, c3) ;
	}

	public Line[] getEdges() {
		return edges ;
	}

	public XY getPoint(Line edge) {
		Line[] edges = this.getEdges() ;
		if(edge.equals(edges[0])) {
			return c3 ;
		} else if(edge.equals(edges[1])) {
			return c1 ;
		} else if(edge.equals(edges[2])){
			return c2 ;
		} else {
			return null ;
		}
	}

	public boolean withinBounds(XY xyPoint) {
		if(c2 != null && c3 != null) {
			if(bounds.contains(xyPoint)) {
				return new Triangle2D(c1, c2, c3).contains(xyPoint) ;
			} else {
				return false ;
			}
		} else {
			throw new IllegalStateException() ;
		}
	}

	public double interpolateZ(XY xyPoint) throws VerticalPlaneException {
		if(this.withinBounds(xyPoint)) {
			return extrapolateZ(xyPoint) ;
		} else {
			throw new IllegalArgumentException() ;
		}
	}

	public double extrapolateZ(XY xyPoint) throws VerticalPlaneException {
		if(c != 0) {
			double z = (d - a * xyPoint.x() - b*xyPoint.y()) / c ;
			return z ;
		} else {
			throw new VerticalPlaneException() ;
		}
	}
	
	public String toString() {
		return "a:" + a + " b:" + b + " c:" + c + " d:" + d ;
	}
	
}
