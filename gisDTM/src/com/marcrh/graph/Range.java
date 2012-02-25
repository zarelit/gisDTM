package com.marcrh.graph;

public class Range {

	double minx = Double.POSITIVE_INFINITY;
	double miny = Double.POSITIVE_INFINITY;
	double minz = Double.POSITIVE_INFINITY;
	double maxx = Double.NEGATIVE_INFINITY;
	double maxy = Double.NEGATIVE_INFINITY;
	double maxz = Double.NEGATIVE_INFINITY;
	
	public Range(){
	}
	
	public Range(Point p1, Point p2){
		minx = (p1.x<p2.x)?p1.x:p2.x;
		miny = (p1.y<p2.y)?p1.y:p2.y;
		minz = (p1.z<p2.z)?p1.z:p2.z;
		maxx = (p1.x>p2.x)?p1.x:p2.x;
		maxy = (p1.y>p2.y)?p1.y:p2.y;
		maxz = (p1.z>p2.z)?p1.z:p2.z;
	}
			
	public Range(Point points[]){
		Point current = null;
		int size = points.length;
		  
		double z,x,y;
		
		for(int i=0;i<size;i++){
			current = points[i];
			x = current.x;
			y = current.y;
			z = current.z;
			if(x<minx)minx = x;
			if(y<miny)miny = y;
			if(z<minz)minz = z;
			if(x>maxx)maxx = x;
			if(y>maxy)maxy = y;
			if(z>maxz)maxz = z;
		}		
	}
	
	public double getMinX(){
		return minx;
	}
	
	public double getMinY(){
		return miny;
	}
	
	public double getMinZ(){
		return minz;
	}
		
	public double getMaxX(){
		return maxx;
	}
	
	public double getMaxY(){
		return maxy;
	}
	
	public double getMaxZ(){
		return maxz;
	}
	
	/**
	 * Creates a point containing the minimum coordinates
	 * @return Point that represents the minimum coordinates
	 */
	public Point getMin(){
		return new Point(minx,miny,minz);
	}
	
	/**
	 * Creates a point containing the maximum coordinates
	 * @return Point that represents the maximum coordinates
	 */
	public Point getMax(){
		return new Point(maxx,maxy,maxz);
	}
	
	public double getWidth(){
		double result = maxx-minx;		
		return (result<0)?-result:result;
	}
	
	public double getHeight(){
		double result = maxy-miny;		
		return (result<0)?-result:result;
	}
	
	public void addRange(Range toSum){
		if(toSum==null)return;
		if(minx>toSum.minx)minx=toSum.minx;
		if(miny>toSum.miny)miny=toSum.miny;
		if(minz>toSum.minz)minz=toSum.minz;
		if(maxx<toSum.maxx)maxx=toSum.maxx;
		if(maxy<toSum.maxy)maxy=toSum.maxy;
		if(maxz<toSum.maxz)maxz=toSum.maxz;
	}
	
	public void add(Point p){
		if(p.x<minx)minx=p.x;
		if(p.y<miny)miny=p.y;
		if(p.z<minz)minz=p.z;
		if(p.x>maxx)maxx=p.x;
		if(p.y>maxy)maxy=p.y;
		if(p.z>maxz)maxz=p.z;
	}
	
	public Point getCenter(){
		Point min = getMin();
		Point vector = min.getVectorToPoint(getMax());
		vector.scale(0.5f);
		return min.getPointSum(vector);
	}
	
	public String toString(){
		String retorn = "Max: " + maxx + "," + maxy + "," + maxz;
		retorn += "  Min: " + minx + "," + miny + "," + maxz;
		return retorn;
	}
	
	public boolean isInRange(Point p){
		return p.x>=minx && p.x<=maxx 
				&& p.y>=miny && p.y<=maxy 
				&& p.z>=minz && p.z<=maxz;
	}
	
	public boolean isInRangeXY(Point p){
		return isInRangeXY(p.x, p.y);
	}
	
	public boolean isInRangeXY(double x, double y){
		return x>=minx && x<=maxx && y>=miny && y<=maxy;
	}
	
	public boolean isInRangeXY(double xy[]){
		return getRangeXY(xy).isInRangeXY(this);
	}
	
	public boolean isInRange(Range r){
		throw new RuntimeException("This procedure is not implemented yet");		
	}
	
	//TODO Es possible que existeixi un error.
	public boolean isInRangeXY(Range r){
		if(r==null)return false;
		if(isInRangeXY(r.minx,r.miny) || isInRangeXY(r.maxx,r.maxy) || r.isInRangeXY(minx,miny) || r.isInRangeXY(maxx,maxy))return true;
		
		if(isInRangeXY(r.minx,r.minx))return true;		
		if(isInRangeXY(r.maxx,r.miny))return true;		
		if(r.isInRangeXY(minx,maxy))return true;		
		if(r.isInRangeXY(maxx,miny))return true;
		
		if(r.minx<maxx && r.minx>minx && r.miny<miny && r.maxy>maxy)return true;
		if(r.maxx<maxx && r.maxx>minx && r.miny<miny && r.maxy>maxy)return true;
		if(minx<r.maxx && minx>r.minx && miny<r.miny && maxy>r.maxy)return true;
		if(maxx<r.maxx && maxx>r.minx && miny<r.miny && maxy>r.maxy)return true;
		return false;
	}	
	
	public static Range getRangeXYZ(float coords[]){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;
		
		int size = coords.length;
		  
		double z,x,y;
		
		for(int i=0;i<size;i+=3){			
			x = coords[i];
			y = coords[i+1];
			z = coords[i+2];
			if(x<minX)minX = x;
			if(y<minY)minY = y;
			if(z<minZ)minZ = z;
			if(x>maxX)maxX = x;
			if(y>maxY)maxY = y;
			if(z>maxZ)maxZ = z;
		}
		Range r = new Range();
		r.minx = minX;
		r.miny = minY;
		r.minz = minZ;
		r.maxx = maxX;
		r.maxy = maxY;
		r.maxz = maxZ;
		return r;
	}
	
	public static Range getRangeXY(double coords[]){
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		int size = coords.length;
		  
		double x,y;
		
		for(int i=0;i<size;i+=2){
			x = coords[i];
			y = coords[i+1];
			if(x<minX)minX = x;
			if(y<minY)minY = y;
			if(x>maxX)maxX = x;
			if(y>maxY)maxY = y;
		}
		Range r = new Range();
		r.minx = minX;
		r.miny = minY;
		r.maxx = maxX;
		r.maxy = maxY;
		return r;
	}	
}