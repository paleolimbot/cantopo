package ca.fwe.locations.geometry;

public class Triangle2D implements Polygon {

	//for telling if a point is inside a triangle
	public static final double ALMOST_EQUALS_COEFFICIENT = 1e-6 ;

	private XY c1 ;
	private XY c2 ;
	private XY c3 ;

	private Line line1 ;
	private Line line2 ;
	private Line line3 ;

	public Triangle2D(XY point1, XY point2, XY point3) {
		this.c1 = point1.ignoreZ();
		this.c2 = point2.ignoreZ();
		this.c3 = point3.ignoreZ();
		line1 = new Line(c1, c2) ;
		line2 = new Line(c2, c3) ;
		line3 = new Line(c3, c1) ;
	}

	public Triangle2D(Line line, XY point) {
		this(point, line.getStart(), line.getEnd()) ;
	}

	public XY[] getPoints() {
		return new XY[] {c1, c2, c3} ;
	}

	public double perimeter() {
		return line1.length() + line2.length() + line3.length() ;
	}

	public double area() {
		double s = this.perimeter() / 2.0 ;
		double a = line1.length() ;
		double b = line2.length() ;
		double c = line3.length() ;
		return Math.sqrt(s*(s-a)*(s-b)*(s-c)) ;
	}

	public boolean contains(XY point) {
		XY noZ = point.ignoreZ() ;
		Triangle2D t1 = new Triangle2D(line1, noZ) ;
		Triangle2D t2 = new Triangle2D(line2, noZ) ;
		Triangle2D t3 = new Triangle2D(line3, noZ) ;
		if(almostEquals((t1.area() + t2.area() + t3.area()), this.area())) {
			return true ;
		} else {
			return false ;
		}
	}

	public double circumRadius() {
		double s = this.perimeter() / 2.0 ;
		double a = line1.length() ;
		double b = line2.length() ;
		double c = line3.length() ;

		return (a * b * c) / (4 * Math.sqrt(s * (a+b-s) * (a+c-s) * (b+c-s))) ;
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

	@Override
	public XY getPoint(int index) {
		switch(index) {
		case 0: return c1 ;
		case 1: return c2 ;
		case 3: return c3 ;
		default: throw new IndexOutOfBoundsException() ;
		}
	}
	
	@Override
	public Line getLine(int index) {
		switch(index) {
		case 0: return line1 ;
		case 1: return line2 ;
		case 2: return line3 ;
		default: throw new IndexOutOfBoundsException() ;
		}
	}

	@Override
	public int getCount() {
		return 3 ;
	}

}
