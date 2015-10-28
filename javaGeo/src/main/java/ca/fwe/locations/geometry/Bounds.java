package ca.fwe.locations.geometry;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ca.fwe.fwx.FWXObject;
import ca.fwe.fwx.FWXUtils;


public class Bounds implements Serializable, FWXObject, FWXObject.FWXHandler {

	public static final String TAG_NAME = "bounds" ;
	public static final String PROPERTY_CLOSE = "bounds_finished" ;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7640838896664727974L;
	private double minX ;
	private double maxX ;
	private double minY ;
	private double maxY ;
	private double minZ ;
	private double maxZ ;

	private boolean hasZ ;
	
	private ArrayList<XY> pointsForXMLImport ;

	public Bounds(XY[] points) {
		if(points.length > 0) {
			List<XY> pointsList = new ArrayList<XY>() ;
			for(XY p: points)
				pointsList.add(p) ;
			this.setFromPoints(pointsList) ;
		} else {
			throw new IllegalArgumentException("Cannot create Bounds with empty list") ;
		}
	}

	public Bounds(List<? extends XY> points) {
		if(points.size() > 0) {
			this.setFromPoints(points) ;
		} else {
			throw new IllegalArgumentException("Cannot create Bounds with empty list") ;
		}
	}

	private void setFromPoints(List<? extends XY> points) {
		XY firstp = points.get(0) ;
		minX = firstp.x();
		maxX = firstp.x();
		minY = firstp.y();
		maxY = firstp.y();
		minZ = firstp.z();
		maxZ = firstp.z();
		hasZ = true ;

		for(XY p: points) {
			if(p.x() < minX)
				minX = p.x() ;
			if(p.x() > maxX)
				maxX = p.x() ;
			if(p.y() < minY)
				minY = p.y() ;
			if(p.y() > maxY)
				maxY = p.y() ;
			if(p.z() < minZ)
				minZ = p.z() ;
			if(p.z() > maxZ)
				maxZ = p.z() ;
			if(!p.hasZ())
				hasZ = false ;
		}
	}

	protected Bounds(double minX, double maxX, double minY, double maxY,
			double minZ, double maxZ) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = minZ;
		this.maxZ = maxZ;
		hasZ = true ;
	}

	public Bounds(double minX, double maxX, double minY, double maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minZ = 0;
		this.maxZ = 0;
		hasZ = false ;
	}

	public Bounds(XY p1, XY p2) {
		this(new XY[] {p1, p2}) ;
	}

	public Bounds(Bounds b) {
		this(b.minX, b.maxX, b.minY, b.maxY) ;
		if(b.hasZ) {
			this.minZ = b.minZ ;
			this.maxZ = b.maxZ ;
			this.hasZ = true ;
		}
	}

	public Bounds() {
		//used for XML creation
	}

	public Bounds extend(XY point) {
		if(this.hasZ() && point.hasZ()) {
			return new Bounds(new XY[] {new XY(getMinX(), getMinY(), getMinZ()), new XY(getMaxX(), getMaxY(), getMaxZ()), point}) ;
		} else {
			return new Bounds(new XY[] {new XY(getMinX(), getMinY()), new XY(getMaxX(), getMaxY()), point}) ;
		}
	}
	
	public boolean contains(XY point) {
		double x = point.x() ;
		double y = point.y() ;
		double z = point.z() ;

		if(x >= minX && x <= maxX && y >= minY && y <= maxY) {
			if(point.hasZ() && this.hasZ()) {
				if(z >= minZ && z <= maxZ)
					return true ;
				else
					return false ;
			} else {
				return true ;
			}
		} else {
			return false ;
		}
	}

	public boolean hasZ() {
		return hasZ ;
	}

	public double getMinX() {
		return minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public double getMinY() {
		return minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public double getMinZ() {
		return minZ;
	}

	public double getMaxZ() {
		return maxZ;
	}

	public double width() {
		return maxX - minX ;
	}

	public double height() {
		return maxY - minY ;
	}

	public double depth() {
		return maxZ - minZ ;
	}

	public static Bounds fromMultiple(ArrayList<Bounds> bounds) {
		ArrayList<XY> points = new ArrayList<XY>(bounds.size() * 2) ;
		for(Bounds b: bounds) {
			if(!b.hasZ()) {
				points.add(new XY(b.getMinX(), b.getMinY())) ;
				points.add(new XY(b.getMaxX(), b.getMaxY())) ;
			} else {
				points.add(new XY(b.getMinX(), b.getMinY(), b.getMinZ())) ;
				points.add(new XY(b.getMaxX(), b.getMaxY(), b.getMaxZ())) ;
			}
		}
		return new Bounds(points) ;
	}

	public XY getCentre() {
		if(hasZ)
			return new XY(minX + width()/2, minY + height()/2, minZ + depth()/2) ;
		else
			return new XY(minX + width()/2, minY + height()/2) ;
	}

	@Override
	public void write(int level, BufferedWriter w) throws IOException {
		FWXUtils.indent(level, w) ;
		FWXUtils.open(this, w) ;
		w.write(">\n") ;
		XY mins ;
		if(this.hasZ())
			mins = new XY(this.getMinX(), this.getMinY(), this.getMinZ()) ;
		else
			mins = new XY(this.getMinX(), this.getMinY()) ;
		
		XY maxes ;
		if(this.hasZ())
			maxes = new XY(this.getMaxX(), this.getMaxY(), this.getMaxZ()) ;
		else
			maxes = new XY(this.getMaxX(), this.getMaxY()) ;
		
		mins.write(level+1, w) ;
		maxes.write(level+1, w) ;
		FWXUtils.writeProperty(level+1, PROPERTY_CLOSE, "", w) ;
		FWXUtils.indent(level,  w) ;
		FWXUtils.close(this, w) ;
	}

	@Override
	public FWXHandler getSAXHandler() {
		return this ;
	}

	@Override
	public String getTagName() {
		return TAG_NAME ;
	}

	@Override
	public void onAttribute(String name, String value) {
		//no attributes
	}

	@Override
	public void onProperty(String propertyName, String value) {
		if(propertyName.equals(PROPERTY_CLOSE)) {
			//hack to finalize object
			this.setFromPoints(pointsForXMLImport) ;
			pointsForXMLImport = null ;
		}
	}

	@Override
	public void onValue(String value) {
		//should never be a value
	}

	@Override
	public FWXObject getObject(String tagName) {
		if(tagName.equals(XY.TAG_NAME)) {
			if(pointsForXMLImport == null)
				pointsForXMLImport = new ArrayList<XY>() ;
			XY xy = new XY(0,0) ;
			pointsForXMLImport.add(xy) ;
			return xy ;
		} else {
			return null ;
		}
	}

}
