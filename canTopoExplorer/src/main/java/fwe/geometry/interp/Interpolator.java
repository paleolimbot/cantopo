package fwe.geometry.interp;

import java.util.ArrayList;

import fwe.locations.geometry.Bounds;
import fwe.locations.geometry.Circle2D;
import fwe.locations.geometry.Line;
import fwe.locations.geometry.Plane;
import fwe.locations.geometry.Triangle2D;
import fwe.locations.geometry.VerticalPlaneException;
import fwe.locations.geometry.XY;


public class Interpolator {
	public ArrayList<XY> points ;
	public ArrayList<Line> edges ;
	private ArrayList<Plane> planes ;
	private Bounds bounds ;

	public Interpolator(ArrayList<? extends XY>points) {
		//should take out duplicates
		if(points.size() >= 3) {
			this.points = new ArrayList<XY>(points) ;
			edges = new ArrayList<Line>() ;
			planes = new ArrayList<Plane>() ;
			bounds = new Bounds(points) ;
		} else {
			throw new IllegalArgumentException("Cannot create interpolator with less than 3 points") ;
		}
	}

	public void createTIN() {
		//shortest line is del edge
		Line l = this.getShortestLine() ;
		this.addEdge(l) ;
		XY firstPoint = this.getDelPoint(l, null) ;
		Plane firstPlane = new Plane(l, firstPoint) ;
		this.addPlane(firstPlane) ;
		addEdges(firstPlane, l) ;
	}

	public Bounds getBounds() {
		return bounds ;
	}

	public double getZValue(XY point, double noValue) {
		for(int i=0; i<planes.size(); i++) {
			Plane p = planes.get(i) ;
			if(p.withinBounds(point)) {
				try {
					return p.interpolateZ(point) ;
				} catch (VerticalPlaneException e) {
					return noValue ;
				}
			}
		}
		return noValue ;
	}

	public double getZRatio(XY point, double noValue) {
		double zValue = this.getZValue(point, Double.NaN) ;
		if(!Double.isNaN(zValue)) {
			double minZ = bounds.getMinZ() ;
			double depth = bounds.depth() ;
			return (zValue - minZ) / depth ;
		} else {
			return noValue ;
		}
	}

	private void addEdges(Plane p, Line edgeToAvoid) {
		//recursive element of createTIN, adding edges as long as they are not already in the edges arraylist
		Line[] currentEdges = p.getEdges() ;
		for(int i=0; i<currentEdges.length; i++) {
			Line currentEdge = currentEdges[i] ;
			if(!currentEdge.equals(edgeToAvoid) && !edges.contains(currentEdge)) {
				this.addEdge(currentEdge) ;
				XY point = this.getDelPoint(currentEdge, p.getPoint(currentEdge)) ;
				if(point != null) {
					Plane plane = new Plane(currentEdge, point) ;
					this.addPlane(plane) ;
					addEdges(plane, currentEdge) ;
				}
			}
		}
	}

	private Line getShortestLine() {
		Line shortestLine = null ;
		for(int i=0; i<points.size(); i++) {
			for(int j=i+1; j<points.size(); j++) {
				Line l = new Line(points.get(i), points.get(j)) ;
				if(shortestLine != null) {
					if(l.length() < shortestLine.length())
						shortestLine = l ;
				} else {
					shortestLine = l ;
				}
			}
		}
		return shortestLine ;
	}

	protected boolean isDeLTri(Triangle2D tri) {
		Circle2D c = Circle2D.get(tri) ;
		XY[] triPts = tri.getPoints() ;
		if(c != null) {
			for(int i=0; i<points.size(); i++) {
				XY point = points.get(i) ;
				boolean isTriPt = false ;

				for(XY triPt: triPts)
					if(triPt.equals2D(point)) isTriPt = true ;

				if(!isTriPt && c.containsInclusive(points.get(i))) {
					return false ;
				}
			}
			return true ;
		} else {
			return false ;
		}
	}

	protected XY getDelPoint(Line in, XY pointToAvoid) {
		XY c1 = in.getStart() ;
		XY c2 = in.getEnd() ;
		for(int i=0; i<points.size(); i++) {
			XY p = points.get(i) ;
			if(!p.equals(c1) && !p.equals(c2) && !p.equals(pointToAvoid)) {
				Triangle2D tri = new Triangle2D(in, p) ;
				if(isDeLTri(tri))
					return p ;
			}
		}
		return null ;
	}

	protected void addEdge(Line edge) {
		edges.add(edge) ;
	}

	protected void addPlane(Plane plane) {
		planes.add(plane) ;
	}

}
