package fwe.locations.geometry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

import fwe.fwx.FWXObject;
import fwe.fwx.FWXUtils;


public class XY implements Serializable, FWXObject {

	public static final String TAG_NAME = "xy" ;
	public static final String ATTRIBUTE_X = "x" ;
	public static final String ATTRIBUTE_Y = "y" ;
	public static final String ATTRIBUTE_Z = "z" ;

	private static final long serialVersionUID = 4927206486565653167L;

	private double x ;
	private double y ;
	private double z ;
	private boolean hasZ ;

	public XY(double x, double y) {
		this(x, y, 0) ;
		hasZ = false ;
	}

	public XY(double x, double y, double z) {
		this.x = x ;
		this.y = y ;
		this.z = z ;
		hasZ = true ;
	}

	public double x() {
		return x ;
	}

	public double y() {
		return y ;
	}

	public double z() {
		return z ;
	}

	public boolean hasZ() {
		return hasZ ;
	}

	public XY ignoreZ() {
		return new XY(x(), y()) ;
	}

	public boolean equals(Object o) {
		if(o instanceof XY) {
			XY xy = (XY)o ;
			if(xy.x() == this.x() && xy.y() == this.y() && xy.z() == this.z() && xy.hasZ() == this.hasZ()) {
				return true ;
			} else {
				return false ;
			}
		} else {
			return false ;
		}
	}

	public boolean equals2D(XY pt) {
		if((pt.x() == this.x()) && pt.y() == this.y()) {
			return true ;
		} else {
			return false ;
		}
	}

	public String toString() {
		return x + ", " + y ;
	}
	public String directionFromLine(XY point1, XY point2) {
		return directionFromLineStatic(point1, point2, this) ;
	}

	public XY averageWith(XY otherPoint) {
		return average(this, otherPoint) ;
	}

	public double distanceTo(XY otherPoint) {
		return distance(this, otherPoint) ;
	}

	public static XY[] subdivide(XY point1, XY point2, int numLengths) {

		if(point1!=null && point2!=null && numLengths > 0) {
			double x1 = point1.x() ;
			double y1 = point1.y() ;
			double x2 = point2.x() ;
			double y2 = point2.y() ;
			double deltaX = x2 - x1 ;
			double deltaY = y2 - y1 ;
			double xStep = deltaX / (double)numLengths ;
			double yStep = deltaY / (double)numLengths ;

			XY[] newPoints = new XY[numLengths + 1] ;

			newPoints[0] = point1 ;
			for(int i=1; i<newPoints.length; i++) {
				newPoints[i] = new XY(x1 + (xStep * (i)), y1 + (yStep*(i))) ;
			}

			return newPoints ;
		} else {
			return null ;
		}

	}

	public String directionFromLineStatic(XY point1, XY point2, XY pointInQuestion) {
		if(point1!=null && point2!=null && pointInQuestion != null) {
			double deltaX = Math.abs(point2.x() - point1.x()) ;
			double deltaY = Math.abs(point2.y() - point1.y()) ;
			if(deltaX > deltaY) { //primarily an east-west line
				if(pointInQuestion.y() > Math.max(point1.y(), point2.y())) { //point is north of either input points
					return "N" ;
				} else if(pointInQuestion.y() < Math.min(point1.y(),point2.y())) {//point is south of either input points
					return "S" ;
				} else {
					double yValueOnLine = interpolateY(point1, point2, pointInQuestion.x()) ;
					if(pointInQuestion.y() > yValueOnLine) {
						return "N" ;
					} else {
						return "S" ;
					}
				}
			} else { //deltaY >= deltaX - favours east-west judgement vs. north/south judgement
				if(pointInQuestion.x() > Math.max(point1.x(), point2.x())) { //point is east of either input points
					return "E" ;
				} else if(pointInQuestion.x() < Math.min(point1.x(),point2.x())) {//point is west of either input points
					return "W" ;
				} else {
					double xValueOnLine = interpolateX(point1, point2, pointInQuestion.y()) ;
					if(pointInQuestion.x() > xValueOnLine) {
						return "E" ;
					} else {
						return "W" ;
					}
				}

			}
		} else {
			return null ;
		}
	}

	public static double interpolateX(XY point1, XY point2, double yValue) {
		//if point1.y() == point2.y(), is a vertical line, will produce an infinity value
		double x1 = point1.x();
		double x2 = point2.x();
		double y1 = point1.y();
		double y2 = point2.y();

		return (x2-x1) / (y2-y1) * (yValue - y1) + x1 ;

	}

	public static double interpolateY(XY point1, XY point2, double xValue) {
		//if point1.x() == point2.x(), is a vertical line, will produce an infinity value
		double x1 = point1.x();
		double x2 = point2.x();
		double y1 = point1.y();
		double y2 = point2.y();

		return (y2-y1) / (x2-x1) * (xValue - x1) + y1 ;
	}

	public static XY average(XY point1, XY point2) {
		if(point1 != null && point2 != null) {
			double newX = (point1.x() + point2.x()) / 2.0 ;
			double newY = (point1.y() + point2.y()) / 2.0 ;
			return new XY(newX, newY) ;
		} else {
			return null ;
		}
	}

	public String encode() {
		String outString = x + "," + y ;
		if(hasZ)
			outString += "," + z ;
		return outString ;
	}

	public static XY valueOf(String encoded) {
		String[] split = encoded.trim().split(",") ;
		if(split.length == 3) {
			try {
				double xCoord = new Double(split[0]) ;
				double yCoord = new Double(split[1]) ;
				double zCoord = new Double(split[2]) ;
				return new XY(xCoord, yCoord, zCoord) ;
			} catch(NumberFormatException e) {
				//one or more parts cannot be parsed as a number
				return null ;
			}
		} else if(split.length == 2) {
			try {
				double xCoord = new Double(split[0]) ;
				double yCoord = new Double(split[1]) ;
				return new XY(xCoord, yCoord) ;
			} catch(NumberFormatException e) {
				//one or more parts cannot be parsed as a number
				return null ;
			}
		} else {
			return null ;
		}
	}

	public static double distance(XY p1, XY p2) {
		return new Line(p1, p2).length() ;
	}

	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		FWXUtils.indent(level, w) ;
		FWXUtils.open(this, w) ;
		w.write(" ") ;
		FWXUtils.writeAttribute(ATTRIBUTE_X, x(), w) ;
		w.write(" ") ;
		FWXUtils.writeAttribute(ATTRIBUTE_Y, y(), w) ;
		w.write(" ") ;
		if(hasZ()) {
			FWXUtils.writeAttribute(ATTRIBUTE_Z, z(), w) ;
			w.write(" ") ;
		}
		w.write("/>\n") ;
	}

	@Override
	public FWXHandler getSAXHandler() {
		return new FWXObject.FWXHandler() {

			@Override
			public void onValue(String value) {}

			@Override
			public void onProperty(String propertyName, String value) {}

			@Override
			public void onAttribute(String name, String value) {
				if(name.equals(ATTRIBUTE_X)) {
					x = Double.valueOf(value) ;
				} else if(name.equals(ATTRIBUTE_Y)) {
					y = Double.valueOf(value) ;
				} else if(name.equals(ATTRIBUTE_Z)) {
					z = Double.valueOf(value) ;
				}
			}
			public FWXObject getObject(String tagName) {return null ;} 
		};
	}

	@Override
	public String getTagName() {
		return TAG_NAME ;
	}

}
