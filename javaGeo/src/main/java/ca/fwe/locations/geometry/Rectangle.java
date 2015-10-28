package ca.fwe.locations.geometry;

public interface Rectangle extends Polygon {

	public boolean contains(XY point) ;
	public Rectangle[][] subdivide(int xRows, int yRows) ;
	public XY upperLeft() ;
	public XY upperRight() ;
	public XY lowerLeft() ;
	public XY lowerRight() ;
	public XY centre() ;
	
}
