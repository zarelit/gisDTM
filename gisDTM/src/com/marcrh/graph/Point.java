package com.marcrh.graph;

public class Point {

	/**
	 * Z coordinate (altitude usually).
	 */
	public double z;
	/**
	 * Y coordinate (latitude if talk in grades usually).
	 */
	public double y;
	/**
	 * X coordinate (longitude if talk in grades usually)
	 */
	public double x;

	/**
	 * A new x=0 y=0 and z=0 Point instance. 
	 */
	public Point(){
	}
	
	/**
	 * Creates a point using the 3 first float values of the array.<p>
	 * The <b>x</b> will be the first coordinate, <b>y</b> the second and <b>z</b> the third.
	 * @param coords Array containing the ordered float values
	 * @exception ArrayIndexOutOfBoundsException if the <b>coords</b> length is shorter than 3.
	 */
	public Point(double coords[]){
		this(coords[0],coords[1],coords[2]);
	}
	
	/**
	 * Initializes a new Point with the specified <b>x</b> and <b>y</b>
	 * @param x The <b>x</b> coordinate.
	 * @param y The <b>y</b> coordinate.
	 */
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Initializes a new Point with the specified <b>x</b>, <b>y</b> and <b>z</b>
	 * @param x The <b>x</b> coordinate.
	 * @param y The <b>y</b> coordinate.
	 * @param z The <b>z</b> coordinate.
	 */
	public Point(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Initializes a new Point with the same coordinates than <b>p</b>
	 * @param p Point 
	 */
	public Point(Point p){
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	
	/**
	 * Getter of the property <b>x</b>
	 * 
	 * @return Returns the x.
	 * 
	 */
	public double getX() {
		return x;
	}

	/**
	 * Getter of the property <b>y</b>
	 * 
	 * @return Returns the y.
	 * 
	 */
	public double getY() {
		return y;
	}

	/**
	 * Getter of the property <b>z</b>
	 * 
	 * @return Returns the z.
	 * 
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Setter of the property <b>x</b>
	 * 
	 * @param x
	 *            The x to set.
	 * 
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Setter of the property <b>y</b>
	 * 
	 * @param y
	 *            The y to set.
	 * 
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Setter of the property <b>z</b>
	 * 
	 * @param z
	 *            The z to set.
	 * 
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * Scales the current instance of Point by the factor.
	 * @param factor float to use as scale factor.
	 */
	public void scale(double factor){
		this.x*=factor;
		this.y*=factor;
		this.z*=factor;		
	}
	
	/**
	 * Adds the coordinates of p to the current instance.
	 * @param p Point to be added.
	 */
	public void sum(Point p){
		this.x+=p.x;
		this.y+=p.y;
		this.z+=p.z;
	}
	
	/**
	 * Returns a new instance of Point resulting to scale the current instnce by the factor.
	 * @param factor  float to use as scale factor.
	 * @return New Point scaled.
	 */
	public Point getPointScale(double factor){
		return new Point(this.x*factor,this.y*factor,this.z*factor);
	}
	
	/**
	 * Return a new point that represents the vector from this instance to the parameter point
	 * @param p Point to calculate the vector
	 * @return new Point that represents the vector
	 */
	public Point getVectorToPoint(Point p){
		return new Point(p.x-this.x,p.y-this.y,p.z-this.z);
	}
	
	/**
	 * Return a new point that represents the vector from the parameter point to this instance
	 * @param p Point to calculate the vector
	 * @return new Point that represents the vector
	 */
	public Point getVectorFromPoint(Point p){
		return new Point(this.x-p.x,this.y-p.y,this.z-p.z);
	}
	
	/**
	 * Return a new point resulting to add a point to this instance
	 * @param p Point to add
	 * @return new Point like this instance with the p added
	 */
	public Point getPointSum(Point p){		
		return new Point(this.x+p.x,this.y+p.y,this.z+p.z);
	}
		
	/**
	 * Gets the distance from the current Point to the p Point
	 * @param p Point to calculate the distance
	 * @return Distance in float.
	 */
	public float getDistanceTo(Point p){
		double distX = p.x-x;
		double distY = p.y-y;
		double distZ = p.z-z;
		
		return (float)Math.sqrt(distX*distX+distY*distY+distZ*distZ);
	}
		
	/**
	 * Calculates the 3D geometric distance between the coordinates passed by arguments.
	 * @param x1 X coordinate of the first point.
	 * @param y1 Y coordinate of the first point.
	 * @param z1 Z coordinate of the first point.
	 * @param x2 X coordinate of the second point.
	 * @param y2 Y coordinate of the second point.
	 * @param z2 Z coordinate of the second point.
	 * @return double representing the 3D geometric distance.
	 */
	public static double getDistanceBeetween(double x1, double y1, double z1, double x2, double y2, double z2){
		double distX = x1-x2;
		double distY = y1-y2;
		double distZ = z1-z2;
		return Math.sqrt(distX*distX+distY*distY+distZ*distZ);
	}
		
	/**
	 * Calculates the 2D geometric distance between the coordinates passed by arguments.
	 * @param x1 X coordinate of the first point.
	 * @param y1 Y coordinate of the first point.
	 * @param x2 X coordinate of the second point.
	 * @param y2 Y coordinate of the second point.
	 * @return double representing the 2D geometric distance.
	 */
	public static double getDistanceBeetween(double x1, double y1, double x2, double y2){
		double distX = x1-x2;
		double distY = y1-y2;
		return Math.sqrt(distX*distX+distY*distY);
	}
	
	/**
	 * Returns the module of the Point if it's considered as a geometric vector.<p>
	 * The module is calculated as the distance from the coordinates origin to this instance coordinates.
	 * @return float Representing the module value.
	 */
	public double getModule(){
		return getDistanceBeetween(0, 0, 0, x, y, z);
	}
	
	/**
	 * Returns the module of the Point using only X and Y coordinates if it's considered as a geometric vector.<p>
	 * The module is calculated as the distance from the coordinates origin to this instance coordinates.
	 * @return float Representing the module value.
	 */
	public double getModuleXY(){
		return getDistanceBeetween(0, 0, x, y);
	}
	
	/**
	 * Gets the distance from the current Point to the p Point using only x and y coordinates
	 * @param p Point to calculate the distance
	 * @return Distance in float.
	 */
	public double getDistanceXYTo(Point p){
		double distX = p.x-x;
		double distY = p.y-y;
		
		return Math.sqrt(distX*distX+distY*distY);
	}
	
	/**
	 * Gets the distance from the current Point to the point represented by coordX and coordY 
	 * @param coordX X coordinate to calculate the distance
	 * @param coordY Y coordinate to calculate the distance
	 * @return Distance in float.
	 */
	public double getDistanceXYTo(double coordX, double coordY){
		double distX = coordX-x;
		double distY = coordY-y;
		
		return Math.sqrt(distX*distX+distY*distY);
	}
		
	public String toString(){
		return "Point " + " --> " + x + " , " + y + " , " + z;
	}

	public boolean equals(Object obj) {
		if(obj instanceof Point){
			Point aux = (Point)obj;
			return aux.x==x && aux.y==y && aux.z==z;
		}
		return false;
	}
}