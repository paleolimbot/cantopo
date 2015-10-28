package ca.fwe.locations.geometry;

import java.io.Serializable;

public class SimpleRectangle extends Bounds implements Rectangle, Serializable {


	private static final long serialVersionUID = -2737218243322224511L;

	public SimpleRectangle(XY p1, XY p2) {
		super(p1.ignoreZ(), p2.ignoreZ());
	}

	public SimpleRectangle(Bounds b) {
		super(b) ;
	}

	@Override
	public SimpleRectangle[][] subdivide(int xRows, int yRows) {
		SimpleRectangle[][] out = new SimpleRectangle[xRows][yRows] ;
		double xSpread = this.width() / (double)xRows ;
		double ySpread = this.height() / (double)yRows ;
		double xMin = this.getMinX() ;
		double yMin = this.getMinY() ;

		for(int i=0; i<xRows; i++) {
			for(int j=0; j<yRows; j++) {
				XY sw = new XY(xMin + (i * xSpread), yMin + (j * ySpread)) ;
				XY ne = new XY(sw.x() + xSpread, sw.y() + ySpread) ;
				out[i][j] = new SimpleRectangle(sw, ne) ;
			}
		}
		return out ;
	}

	@Override
	public XY upperLeft() {
		return new XY(this.getMinX(), this.getMaxY()) ;
	}

	@Override
	public XY upperRight() {
		return new XY(this.getMaxX(), this.getMaxY()) ;
	}

	@Override
	public XY lowerLeft() {
		return new XY(this.getMinX(), this.getMinY()) ;
	}

	@Override
	public XY lowerRight() {
		return new XY(this.getMaxX(), this.getMinY()) ;
	}

	@Override
	public XY centre() {
		return XY.average(this.upperRight(), this.lowerLeft()) ;
	}

	public String encode() {
		//north, east, south, west
		String c = "," ;
		return this.getMaxY() + c + this.getMaxX() + c + this.getMinY() + c + this.getMinX() ;
	}

	public static SimpleRectangle valueOf(String encoded) {
		if(encoded != null) {
			String[] split = encoded.split(",") ;
			if(split.length == 4) {
				try {
					double maxY = Double.valueOf(split[0]) ;
					double maxX = Double.valueOf(split[1]) ;
					double minY = Double.valueOf(split[2]) ;
					double minX = Double.valueOf(split[3]) ;
					return new SimpleRectangle(new XY(minX, minY), new XY(maxX, maxY)) ;
				} catch(NumberFormatException e) {
					//one string NaN
					return null ;
				}
			} else {
				//invalid string
				return null ;
			}
		} else {
			//null string in
			return null ;
		}
	}

	@Override
	public XY getPoint(int index) {
		switch(index) {
		case 0: return upperLeft() ;
		case 1: return upperRight() ;
		case 2: return lowerRight() ;
		case 3: return lowerLeft() ;
		default: throw new IndexOutOfBoundsException() ;
		}
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

	@Override
	public int getCount() {
		return 4;
	}

}


