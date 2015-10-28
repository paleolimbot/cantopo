package ca.fwe.locations.geometry;

import java.util.ArrayList;
import java.util.List;

public class NGon implements Polygon {

	private List<XY> points ;
	private Bounds bounds ;

	public NGon(List<? extends XY> list) {
		points = new ArrayList<XY>() ;
		if(list.size() >= 3)
			points.addAll(list) ;
		else
			throw new IllegalArgumentException() ;
	}

	public NGon(XY[] list) {
		points = new ArrayList<XY>() ;
		if(list.length >= 3)
			for(XY xy: list) {
				points.add(xy) ;
			}
		else
			throw new IllegalArgumentException() ;
	}

	public Bounds getBounds() {
		if(bounds == null)
			bounds = new Bounds(points) ;
		return bounds ;
	}

	@Override
	public XY getPoint(int index) {
		return points.get(index) ;
	}

	@Override
	public int getCount() {
		return points.size() ;
	}

	@Override
	public Line getLine(int index) {
		if(index < getCount() && index >= 0) {
			int beginIndex = index ;
			int endIndex = index + 1 ;
			if(endIndex == getCount())
				endIndex = 0 ;
			return new Line(getPoint(beginIndex), getPoint(endIndex)) ;
		} else {
			throw new IndexOutOfBoundsException() ;
		}
	}

	public NGon resample(int interval) {
		int newSize = this.getCount() / interval + 1 ;
		if(newSize >= 3) {
			ArrayList<XY> newPoints = new ArrayList<XY>(newSize) ;
			for(int i=0; i<this.getCount(); i=i+interval) {
				newPoints.add(getPoint(i)) ;
			}
			return new NGon(newPoints) ;
		} else {
			return null ;
		}
	}

}
