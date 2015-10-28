package ca.fwe.locations.geometry;

import java.util.ArrayList;

public class Circle2D {
	
	//for intersections
	public static final double ALMOST_EQUALS_COEFFICIENT = 1e-6 ;
	
	private float radius ;
	private XY centre ;

	public Circle2D(XY centre, float radius) {
		this.centre = centre.ignoreZ() ;
		this.radius = radius ;
	}

	public Circle2D(XY centre, double radius) {
		this.centre = centre.ignoreZ() ;
		this.radius = (float)radius ;
	}

	public float getRadius() {
		return radius ;
	}
	public XY getCentre() {
		return centre ;
	}
	public double getPerimeter() {
		return radius * 2.0 * Math.PI ;
	}
	public double getArea() {
		return Math.PI * radius * radius ;
	}

	public boolean equals(Object o) {
		if(o instanceof Circle2D) {
			double rad = ((Circle2D) o).getRadius() ;
			XY centre = ((Circle2D) o).getCentre() ;
			if((rad == this.getRadius()) && (this.getCentre().equals(centre))) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}

	public boolean containsInclusive(XY point) {
		double dist = XY.distance(point.ignoreZ(), centre) ;
		if(dist <= radius) {
			return true ;
		} else {
			return false ;
		}
	}

	public boolean containsExclusive(XY point) {
		double dist = XY.distance(point.ignoreZ(), centre) ;
		return (this.containsInclusive(point) && !(dist == radius)) ;
	}


	public static Circle2D get(XY p1, XY p2, XY p3) {
		return get(new Triangle2D(p1, p2, p3)) ;
	}

	public static Circle2D get(Triangle2D t) {
		if(t.area() > 0) {
			double R = t.circumRadius() ;
			XY[] points = t.getPoints() ;
			ArrayList<XY> solutions = new ArrayList<XY>() ;
			Circle2D c1 = new Circle2D(points[0], R) ;
			Circle2D c2 = new Circle2D(points[1], R) ;
			Circle2D c3 = new Circle2D(points[2], R) ;

			solutions.addAll(intersection(c1, c2)) ;
			solutions.addAll(intersection(c1, c3)) ;
			solutions.addAll(intersection(c2, c3)) ;

			//one solution should show up twice
			XY centre = getDuplicateXY(solutions) ;
			if(centre != null) {
				return new Circle2D(centre, R) ;
			} else {
				return null ;
			}
		} else {
			//points are colinear
			return null ;
		}
	}

	private static XY getDuplicateXY(ArrayList<XY> solutions) {
		
		for(int i=0; i<solutions.size(); i++) {
			XY point1 = solutions.get(i) ;
			for(int j=i+1; j<solutions.size(); j++) {
				//for the rest, check to see equivalence
				if(equals2D(solutions.get(j), point1)) {
					return point1 ;
				}
			}
		}
		return null ;
	}

	public static ArrayList<XY> intersection(Circle2D c1, Circle2D c2) {
		if(!c1.equals(c2)) {
			Line betweenCentres = new Line(c1.getCentre(), c2.getCentre()) ;
			Vector betweenCentresVec = betweenCentres.toVector() ;

			double d = betweenCentres.length() ;	
			double totalRadius = c1.getRadius() + c2.getRadius() ;

			ArrayList<XY> out = new ArrayList<XY>() ;

			if(d == totalRadius) {
				double fraction = c1.getRadius() / totalRadius ;
				out.add(betweenCentres.get(fraction)) ;
			} else if(d < totalRadius) {
				double R = c1.getRadius() ;
				double r = c2.getRadius() ;
				double a = Math.sqrt((-d+r-R) * (-d-r+R) * (-d+r+R) * (d+r+R)) / d ;
				double yDist = a / 2 ;

				double d1 = ( (d*d) - (r*r) + (R*R) ) / (2*d) ;
				XY centerLens = betweenCentres.get(d1/d) ;
				Vector centerLensPosition = new Vector(centerLens) ;

				Vector yOffset = betweenCentresVec.ortho().getUnitVector().multiplyBy(yDist) ;
				
				out.add(centerLensPosition.addTo(yOffset).getEnd()) ;
				out.add(centerLensPosition.minus(yOffset).getEnd()) ;
			} else {
				//nothing
			}

			return out ;
		} else {
			//circles are the same
			return null ;
		}
	}
	
	public String toString() {
		return "Centre: " + centre.toString() + " Radius: " + radius ;
	}
	
	private static boolean equals2D(XY a, XY b) {
		if(a != null && b != null) {
			if(almostEquals(a.x(), b.x()) && almostEquals(a.y(), b.y())) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}
	
	private static boolean almostEquals(double val1, double val2) {
		double delta = Math.abs(val1 - val2) ;
		double deltaLimit = Math.max(Math.abs(val1), Math.abs(val2)) * ALMOST_EQUALS_COEFFICIENT ;
		if(delta <= deltaLimit) {
			return true ;
		} else {
			return false ;
		}
		
	}
	
}
