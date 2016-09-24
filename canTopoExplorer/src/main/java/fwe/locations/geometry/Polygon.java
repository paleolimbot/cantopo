package fwe.locations.geometry;

public interface Polygon {

	public XY getPoint(int index) ;
	public Line getLine(int index) ;
	public int getCount() ;
}
