package com.marcrh.graph;

public class Segment implements Line{

	private double a;
	private double b;
	private double c;
	public double x1;
	public double x2;
	public double y1;
	public double y2;
		
	public Segment(Point p1, Point p2){
		this(p1.x,p1.y,p2.x,p2.y);
	}
	
	public Segment(double x1, double y1, double x2, double y2){
		set(x1,y1,x2,y2);
	}
	
	public void set(double x1, double y1, double x2, double y2){
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		
		this.a = x2 - x1;
        this.b = y2 - y1;
        this.c = x2 * y1 - x1 * y2;
	}
	
	public double getA() {	
		return a;
	}
	
	public double getB() {	
		return b;
	}
	
	public double getC() {
		return c;
	}
	
	public Point getPoint1(){
		return new Point((float)x1,(float)y1);
	}
	
	public Point getPoint2(){
		return new Point((float)x2,(float)y2);
	}
	
	public Range getRange(){
		return new Range(new Point((float)x1,(float)y1),new Point((float)x2,(float)y2));
	}
	
	public Point intersectLine(Line line) {
        double det = a * line.getB() - line.getA() * b;
        if (det == 0) return null;

        double x = (line.getA() * c - a * line.getC()) / det;
        double y = (line.getB() * c - b * line.getC()) / det;        
        return new Point((float)x,(float)y);
    }
	
	public Point intersectSegment(Segment line) {
		double det = a * line.getB() - line.getA() * b;
        if (det == 0) return null;

        double x = (line.getA() * c - a * line.getC()) / det;        
        double y = (line.getB() * c - b * line.getC()) / det;
        
        if(getRange().isInRangeXY((float)x, (float)y) && line.getRange().isInRangeXY((float)x, (float)y)){        
        	return new Point((float)x,(float)y);
        }else{
        	return null;
        }
    }
		
	public Line getProjection(Point p){
		return getProjection(p.x,p.y);
	}
	
	public Line getProjection(double x, double y){
		if(a==0){
			return new Segment(x,y,x+b,y-a);
		}else{
			return new Segment(x,y,x-b,y+a);
		}
	}
	
	public Point nearestPoint(Point p){
		Point nearest = intersectLine(getProjection(p));
		
		if(nearest==null || !getRange().isInRangeXY(nearest)){
			if(Point.getDistanceBeetween(x1, y1, p.x, p.y)<Point.getDistanceBeetween(x2, y2, p.x, p.y)){
				nearest = new Point((float)x1,(float)y1);
			}else{
				nearest = new Point((float)x2,(float)y2);
			}
		}
		return nearest;
	}
	
	public double getLongitude(){
		return Point.getDistanceBeetween(x1, y1, x2, y2);
	}
	
	public static int getNearestSegmentIndex(Segment list[], Point p){
		int size = list.length;
		double distance = Double.POSITIVE_INFINITY;
		double currentDistance = 0;
		int nearestIndex = -1;
		Point nearestPoint = null;
		for(int i=0;i<size;i++){
			nearestPoint = list[i].nearestPoint(p);
			currentDistance = nearestPoint.getDistanceXYTo(p);
			if(currentDistance<distance){
				nearestIndex = i;
				distance = currentDistance;
			}
		}
		return nearestIndex;
	}
	
	public static Segment getNearestSegment(Segment list[], Point p){
		int nearestIndex = getNearestSegmentIndex(list, p);
		return list[nearestIndex];
	}
	
	public static double getDistanceToNearest(Segment list[], Point p){
		int size = list.length;
		double distance = Float.POSITIVE_INFINITY;
		double currentDistance = 0;
		int nearestIndex = -1;
		Point nearestPoint = null;
		for(int i=0;i<size;i++){
			nearestPoint = list[i].nearestPoint(p);
			currentDistance = nearestPoint.getDistanceXYTo(p);
			if(currentDistance<distance){
				nearestIndex = i;
				distance = currentDistance;
			}
		}
		return distance;
	}
	
	/**
	 * Returns the distance along the segments of the list to the projected point from p to the closest segment to p.
	 * It's assuming that the segments are a sequence of the same line.
	 * @param list Array of segments to compute.
	 * @param p Point to project to the closest segment.
	 * @return The distance computed.
	 */
	public static double getDistanceAlong(Segment list[], Point p){
		int nearestIndex = getNearestSegmentIndex(list, p);
		Segment nearestSegment = list[nearestIndex];
		Point projectedPoint = nearestSegment.nearestPoint(p);
		double distance = 0;
		for(int i=0;i<nearestIndex;i++){
			distance+=list[i].getLongitude();
		}
		distance+=nearestSegment.getPoint1().getDistanceXYTo(projectedPoint);
		return distance;
	}
}