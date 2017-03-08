
package org.jennings.mvnsat;

public class Vector {

	final private double pi = 3.141592654;
	
	private double x;
	private double y;
	private double z;
	
	Vector() {
		x = 0;
		y = 0;
		z = 0;
	}

	Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double dot(Vector a) {
		return x*a.x + y*a.y + z*a.z;
	}
	
	public Vector cross(Vector b) {
		Vector c = new Vector();
		c.x = y*b.z - z*b.y;
		c.y = z*b.x - x*b.z;
		c.z = x*b.y - y*b.x;
		return c;
	}
	
	public double mag() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public double angle(Vector b, String units) {
		double ang = this.dot(b)/(this.mag()*b.mag());
		if (units == "deg") 
			return ang*180/pi;
		else
			return ang;
	}
	
	public Vector plus(Vector b) {
		Vector c = new Vector();
		c.x = x + b.x;
		c.y = y + b.y;
		c.z = z + b.z;
		return c;
	}

	public Vector minus(Vector b) {
		Vector c = new Vector();
		c.x = x - b.x;
		c.y = y - b.y;
		c.z = z - b.z;
		return c;
	}

	public Vector times(double n) {
		Vector c = new Vector();
		c.x = n*x;
		c.y = n*y;
		c.z = n*z;
		return c;
	}
	
	public double X() {
		return x;
	}
	
	public double Y() {
		return y;
	}
	
	public double Z() {
		return z;
	}
	
	public static void main(String[] args) {
//		Vector v1 = new Vector(4,3,2);
//		
//		System.out.println(v1.X());

            System.out.println(Math.atan2(3.0,3.0));
            System.out.println(Math.atan2(3.0,-3.0));
            System.out.println(Math.atan2(-3.0,-3.0));
            System.out.println(Math.atan2(-3.0,3.0));

	}

}
