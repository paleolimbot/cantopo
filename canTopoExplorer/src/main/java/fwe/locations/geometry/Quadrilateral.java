package fwe.locations.geometry;

public class Quadrilateral implements Rectangle {

	private XY ne ;
	private XY nw ;
	private XY sw ;
	private XY se ;

	public Quadrilateral (XY ne, XY nw, XY sw, XY se) {
		this.ne = ne ;
		this.nw = nw ;
		this.sw = sw ;
		this.se = se ;
	}

	public Quadrilateral(Quadrilateral quadrilateral) {
		this(quadrilateral.upperRight(), quadrilateral.upperLeft(), quadrilateral.lowerLeft(), quadrilateral.lowerRight()) ;
	}

	public XY upperRight() {
		return ne ;
	}

	public XY upperLeft() {
		return nw ;
	}

	public XY lowerLeft() {
		return sw ;
	}

	public XY lowerRight() {
		return se ;
	}

	public XY centre() {
		if(this.verifyGeometry()) {
			XY averageN = this.upperLeft().averageWith(this.upperRight()) ;
			XY averageS = this.lowerLeft().averageWith(this.lowerRight()) ;
			return averageN.averageWith(averageS) ;
		} else {
			return null ;
		}
	}

	public boolean verifyGeometry() {
		if(this.ne != null && this.nw !=null && this.sw != null && this.se != null) {
			return true ;
		} else {
			return false ;
		}
	}

	public String toString() {
		if(this.verifyGeometry()) {
			return "nw:" + upperLeft().toString() + " \n ne:" + upperRight().toString() + " \n sw:" + lowerLeft().toString() + " \n se:" + lowerRight().toString() ;
		} else {
			return "geometry not valid" ;
		}
	}

	public double getMaxX() {
		double maxN = Math.max(this.ne.x(), this.nw.x()) ;
		double maxS = Math.max(this.se.x(), this.sw.x()) ;
		return Math.max(maxN, maxS) ;
	}

	public double getMinX() {
		double minN = Math.min(this.ne.x(), this.nw.x()) ;
		double minS = Math.min(this.se.x(), this.sw.x()) ;
		return Math.min(minN, minS) ;
	}

	public double getMaxY() {
		double maxE = Math.max(this.ne.y(), this.se.y()) ;
		double maxW = Math.max(this.nw.y(), this.sw.y()) ;
		return Math.max(maxE, maxW) ;
	}

	public double getMinY() {
		double minE = Math.min(this.ne.y(), this.se.y()) ;
		double minW = Math.min(this.nw.y(), this.sw.y()) ;
		return Math.min(minE, minW) ;
	}

	public boolean contains(XY point) {

		if(point != null && this.verifyGeometry()) {
			boolean southOfNorth = false ;
			boolean northOfSouth = false ;
			boolean eastOfWest = false ;
			boolean westOfEast = false ;

			if(point.directionFromLine(this.upperLeft(), this.upperRight()) == "S")
				southOfNorth = true ;
			if(point.directionFromLine(this.lowerLeft(), this.lowerRight()) == "N") 
				northOfSouth = true ;
			if(point.directionFromLine(this.lowerLeft(), this.upperLeft()) == "E")
				eastOfWest = true ;
			if(point.directionFromLine(this.lowerRight(), this.upperRight()) == "W") {
				westOfEast = true ;
			}

			if(southOfNorth && northOfSouth && eastOfWest && westOfEast) {
				return true ;
			} else {
				return false ;
			}

		} else {
			return false ;
		}

	}

	public Quadrilateral[][] subdivide(int xRows, int yRows) { 

		if(xRows >= 1 & yRows >= 1) {

			XY[] northEdge = XY.subdivide(this.upperLeft(), this.upperRight(), xRows) ;
			XY[] southEdge = XY.subdivide(this.lowerLeft(), this.lowerRight(), xRows) ;

			XY[][] points = new XY[southEdge.length][] ;

			for(int i=0; i<southEdge.length; i++) {
				points[i] = XY.subdivide(southEdge[i], northEdge[i], yRows) ;
			}

			return Quadrilateral.areaArrayFromPoints(points) ;
		} else {
			return null ;
		}
	}

	private static Quadrilateral[][] areaArrayFromPoints(XY[][] points) {
		if(points != null ) {
			if(points.length >= 2) {
				if(points[0].length >= 2) {

					Quadrilateral[][] returnArray = new Quadrilateral[points.length - 1][points[0].length - 1] ;

					for(int i=1; i<(points.length); i++) {
						for(int j=1; j<(points[i].length); j++) {
							XY NE = points[i][j] ;
							XY NW = points[i-1][j] ;
							XY SW = points[i-1][j-1] ;
							XY SE = points[i][j-1] ;
							returnArray[i-1][j-1] =  new Quadrilateral(NE, NW, SW, SE) ;
						}
					}

					return returnArray ;

				} else {
					return null ;
				}
			} else {
				return null ;
			}
		} else {
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
