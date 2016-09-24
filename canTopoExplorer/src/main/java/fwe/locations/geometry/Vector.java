package fwe.locations.geometry;

public class Vector extends Line {
	//basically, a line starting at 0,0,0
	public Vector(XY point) {
		super(new XY(0,0,0), point) ;
	}
	public Vector(double i, double j, double k) {
		super(new XY(0,0,0), new XY(i, j, k)) ;
	}
	public double i() {
		return this.getEnd().x() ;
	}
	public double j() {
		return this.getEnd().y() ;
	}
	public double k() {
		return this.getEnd().z() ;
	}
	
	public Vector reverseDirection() {
		return multiply(this, -1) ;
	}
	
	public double length() {
		return this.magnitude() ;
	}
	
	public double magnitude() {
		return Math.sqrt(i() * i() + j() * j() + k() * k()) ;
	}
	
	public Vector getUnitVector() {
		return this.divideBy(this.magnitude()) ;
	}
	
	public Vector multiplyBy(double scalar) {
		return multiply(this, scalar) ;
	}
	public Vector divideBy(double scalar) {
		return divide(this, scalar) ;
	}
	public Vector addTo(Vector otherVector) {
		return add(this, otherVector) ;
	}
	public Vector minus(Vector otherVector) {
		return subtract(this, otherVector) ;
	}
	
	public Vector ortho() {
		return new Vector(-j(), i(), -k()) ;
	}
	
	public static Vector crossProduct(Vector v1, Vector v2) {
		// AxB = (AyBz - AzBy, AzBx - AxBz, AxBy - AyBx)
		double i = v1.j()*v2.k() - v1.k()*v2.j() ;
		double j = v1.k()*v2.i() - v1.i()*v2.k() ;
		double k = v1.i()*v2.j() - v1.j()*v2.i() ;
		
		return new Vector(i,j,k) ;
	}
	
	public static double dotProduct(Vector v1, Vector v2) {
		//ad + be + cf
		return v1.i()*v2.i() + v1.j()*v2.j() + v1.k()*v2.k() ;
	}
	
	public static Vector add(Vector v1, Vector v2) {
		double i = v1.i() + v2.i() ;
		double j = v1.j() + v2.j() ;
		double k = v1.k() + v2.k() ;
		return new Vector(i,j,k) ;
	}
	
	public static Vector subtract(Vector v1, Vector v2) {
		return add(v1, v2.reverseDirection()) ;
	}
	
	public static Vector multiply(Vector v1, double scalar) {
		double i = v1.i() * scalar ;
		double j = v1.j() * scalar ;
		double k = v1.k() * scalar ;
		return new Vector(i, j, k) ;
	}
	
	public static Vector divide(Vector v1, double scalar) {
		return multiply(v1, 1.0/scalar) ;
	}
	
}
