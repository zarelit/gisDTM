package com.marcrh.graph.delaunay;

import java.util.ArrayList;
import java.util.List;

import com.marcrh.graph.Line;
import com.marcrh.graph.Point;
import com.marcrh.graph.Range;
import com.marcrh.graph.Segment;
import com.marcrh.utils.Stopwatch;

public class Voronoi {
	
	private Triangulator triangulator;
	private ArrayList<Triad> triads;
	private int[][] indexedTriads;
	private ArrayList<Point> points;
	private BoundingPolygon boundingPolygon;
			
	public Voronoi(){		
		triangulator = new Triangulator();
	}
	
	public void generate(ArrayList<Point> points, Range boundingBox){
		this.points = points;
		this.boundingPolygon = new BoundingPolygon(boundingBox);		
		regenerate();
	}
	
	public void regenerate(){
		Stopwatch sw = new Stopwatch();
		sw.start();
		System.out.println("Start triangulation");
		this.triads = triangulator.getTriangulation(points,true);
		
		for (Triad tr : triads) {
			tr.makeCW(points);
		}
		indexTriads();
		sw.stop();
		System.out.println("Triangulated in: " + sw);		
	}
	
	private void indexTriads(){
		indexedTriads = new int[points.size()][2];
		initializeIndex(indexedTriads);
		int size = triads.size();
		int remain = points.size();
		Triad current = null;
		for(int i=0;i<size && remain>0;i++){
			current = triads.get(i);
			if(indexedTriads[current.a][0]==Triad.PIVOT_INVALID){
				indexedTriads[current.a][0] = i;
				indexedTriads[current.a][1] = Triad.PIVOT_A;
				remain--;
			}
			if(indexedTriads[current.b][0]==Triad.PIVOT_INVALID){
				indexedTriads[current.b][0] = i;
				indexedTriads[current.b][1] = Triad.PIVOT_B;
				remain--;
			}
			if(indexedTriads[current.c][0]==Triad.PIVOT_INVALID){
				indexedTriads[current.c][0] = i;
				indexedTriads[current.c][1] = Triad.PIVOT_C;
				remain--;
			}
		}
	}
	
	private void initializeIndex(int index[][]){
		for (int i = 0; i < index.length; i++) {
			index[i][0] = Triad.PIVOT_INVALID;
			index[i][1] = Triad.PIVOT_INVALID;
		}
	}
	
	public Region getRegion(int index){
		Region region = new Region(index);
		//ArrayList<Point> region = new ArrayList<Point>();
		ArrayList<Triad> tr = getTriads(index);

		if(closeRegion(tr, region, index)){
			int size = tr.size();
			for (int i=0;i<size;i++) {
				Triad current = tr.get(i);
				int opositeIndex = current.getPointIndexInPivot((current.getPivot(index)+1)%3);
				region.add(current,opositeIndex);
			}
		}else{
			clipRegion(tr, region, index);
		}
		return region;
	}
	
	private void clipRegion(List<Triad> tr, Region region, int pointIndex){
		int size = tr.size();
		Range r = boundingPolygon.getRange();
		Triad previous = tr.get(tr.size()-1);
		boolean pInRange = r.isInRangeXY(previous);
		SegmentSuccession lastIntersected = null;
		for (int i=0;i<size;i++) {
			Triad current = tr.get(i);	
			int opositeIndex = current.getPointIndexInPivot((current.getPivot(pointIndex)+1)%3);
			boolean cInRange = r.isInRange(current);
			if(pInRange && cInRange){
				region.add(current,opositeIndex);
				lastIntersected = null;
			}else{
				if(!pInRange && !cInRange){
					Segment s = new Segment(previous, current);
					Point p[] = previous.getCommonPoints(current, points);
					Segment edge = new Segment(p[0],p[1]);
					Point intersection = s.intersectSegment(edge);
					if(intersection!=null){
						s.set(intersection.x, intersection.y, previous.x, previous.y);
						lastIntersected = boundingPolygon.addFirstIntersection(s, region, lastIntersected);
						s.set(intersection.x, intersection.y, current.x, current.y);
						lastIntersected = boundingPolygon.addFirstIntersection(s, region, null);
						region.setLastOutputIndex(opositeIndex);
					}
				}else{
					if(pInRange){
						lastIntersected = boundingPolygon.addFirstIntersection(new Segment(previous, current), region, null);
						region.setLastOutputIndex(opositeIndex);
					}
					if(cInRange){
						lastIntersected = boundingPolygon.addFirstIntersection(new Segment(previous, current), region, lastIntersected);
						region.add(current,opositeIndex);
					}
				}
			}
			previous = current;
			pInRange = cInRange;
		}
	}
	
	private boolean closeRegion(List<Triad> tr, Region region, int pointIndex){
		Triad trOr = tr.get(tr.size()-1);
		
		int pivotOr = trOr.getPivot(pointIndex);		
		if(trOr.getNext(triads, pivotOr)==null){
			Range r = boundingPolygon.getRange();
			boolean orInRange = r.isInRangeXY(trOr);
			while(tr.size()>1 && !orInRange){
				tr.remove(tr.size()-1);
				trOr = tr.get(tr.size()-1);
				orInRange = r.isInRangeXY(trOr);
			}
			Triad trDest = tr.get(0);
			boolean destInRange = r.isInRangeXY(trDest);
			while(tr.size()>1 && !destInRange){
				tr.remove(0);
				trDest = tr.get(0);
				destInRange = r.isInRangeXY(trDest);
			}
			
			pivotOr = trOr.getPivot(pointIndex);
			
			Segment segments[]= null;
			int opositeIndex1 = trOr.getPointIndexInPivot((trOr.getPivot(pointIndex)+2)%3);
			//int opositeIndex2 = -1;
			if(tr.size()==1 && !orInRange){
				tr.clear();
				segments = getPependicularsFromShortestEdge(trOr, pivotOr);	
			}else{
				int pivotDest = trOr.getCommonPivot(trDest, pivotOr);
				segments = new Segment[2];
				segments[0] = getPerpendicularFromCenter(trOr, (pivotOr+2)%3);
				segments[1] = getPerpendicularFromCenter(trDest, pivotDest);
				//opositeIndex2 = trDest.getPointIndexInPivot((trDest.getPivot(pointIndex)+1)%3);
			}		
			List<Point> p = boundingPolygon.getIntersectBB(segments[0],segments[1]);
			if(p.size()>0){
				region.add(p.get(0),opositeIndex1);
				for(int i=1;i<p.size();i++){
					region.add(p.get(i));
				}
			}
			//region.add(p.get(p.size()-1),opositeIndex2);
			//int opositeIndex = tr.getPointIndexInPivot((tr.getPivot(pointIndex)+1)%3);
			return true;
		}else{
			return false;
		}
	}
	
	private Segment[] getPependicularsFromShortestEdge(Triad startTriad, int pivot){
		Segment segment[] = new Segment[2];
		Triad endTriad = startTriad.getNext(getTriads(), pivot);
		Point pivotPoint = startTriad.getPointInPivot(points, pivot);
		Point commonPoint = startTriad.getPointInPivot(points, (pivot+2)%3);
		Point opositePoint1 = startTriad.getPointInPivot(points, (pivot+1)%3);		
		Point opositePoint2 = null;
		if(endTriad!=null){
			opositePoint2 = endTriad.getPointInPivot(points, (startTriad.getCommonPivot(endTriad, pivot)+2)%3);
		}
		double longitude1 = pivotPoint.getDistanceXYTo(opositePoint1);
		double longitude2 = pivotPoint.getDistanceXYTo(commonPoint);
		double longitude3 = longitude2;
		if(endTriad!=null)longitude3 = pivotPoint.getDistanceXYTo(opositePoint2);
		if(longitude1<longitude2 && longitude1<longitude3){
			segment = getPerpendicularsTroughtPoint(pivotPoint, opositePoint1, startTriad);
		}else if(longitude3<longitude1 && longitude3<longitude2){
			segment = getPerpendicularsTroughtPoint(pivotPoint, opositePoint2, endTriad);
		}else{
			segment = getPerpendicularsTroughtPoint(pivotPoint, commonPoint, startTriad);
		}
		return segment;
	}
	
	private Segment[] getPerpendicularsTroughtPoint(Point p1, Point p2, Point inline){
		Segment edge = new Segment(p1, p2);
		Line projection = edge.getProjection(inline);
		Point projectedPoint = edge.intersectLine(projection);
		
		Point edgeVector = p1.getVectorToPoint(p2);
		double aux = edgeVector.x;
		edgeVector.x = -edgeVector.y;
		edgeVector.y = aux;
		edgeVector.scale(boundingPolygon.getMinSizeFactor()/edgeVector.getModuleXY());
		Segment result[] = new Segment[2];
		result[0] = new Segment(projectedPoint.x,projectedPoint.y,projectedPoint.x-edgeVector.x,projectedPoint.y-edgeVector.y);
		result[1] = new Segment(projectedPoint.x,projectedPoint.y,projectedPoint.x+edgeVector.x,projectedPoint.y+edgeVector.y);
		return result;
	}
	
	private Segment getPerpendicularFromCenter(Triad tr,int edge){
		Point edgeVector = null;
		Point p1 = null;
		Point p2 = null;
		switch(edge){
			case Triad.TR_EDGE_AB: 
				p1 = points.get(tr.a);
				p2 = points.get(tr.b);
				break;
			case Triad.TR_EDGE_BC: 
				p1 = points.get(tr.b);
				p2 = points.get(tr.c);
				break;
			case Triad.TR_EDGE_AC: 
				p1 = points.get(tr.c);
				p2 = points.get(tr.a);
				break;
		}
		edgeVector = p1.getVectorToPoint(p2);
		double aux = edgeVector.x;
		edgeVector.x = -edgeVector.y;
		edgeVector.y = aux;
		edgeVector.scale(boundingPolygon.getMinSizeFactor()/edgeVector.getModuleXY());
		return new Segment(tr.x,tr.y,tr.x+edgeVector.x,tr.y+edgeVector.y);
	}
		
	public ArrayList<Triad> getTriads(){
		return triads;
	}
	
	private ArrayList<Triad> getTriads(int pointIndex){
		ArrayList<Triad> result = null;
		Triad triad = triads.get(indexedTriads[pointIndex][0]);
		result = getAdjacentTriads(triad, indexedTriads[pointIndex][1]);		
		return result;
	}
	
	private ArrayList<Triad> getAdjacentTriads(Triad triad, int pivot){
		ArrayList<Triad> result = new ArrayList<Triad>();
		
		Triad firstTr = findFirstTriad(triad, pivot);
		int newPivot = triad.getCommonPivot(firstTr, pivot);
		result.add(firstTr);
		Triad newTr = firstTr;
		int i=0;
		while((newTr = newTr.getNext(triads, newPivot))!=null){
			i++;
			if(i>50){
				System.out.println("Error 1");
				throw new RuntimeException("Error searching last triad");
			}
			newPivot = triad.getCommonPivot(newTr, pivot);
			if(newTr==firstTr){
				break;
			}
			result.add(newTr);
		}
		return result;
	}
	
	private Triad findFirstTriad(Triad triad, int pivot){
		Triad first = triad;
		Triad current = null;
		int newPivot = pivot;
		int i=0;
		while((current=first.getPrevious(triads,newPivot))!=null){
			i++;
			if(i>50){
				System.out.println("Error 2");
				//Utils.printPoints(points);
				//findFirstTriad(triad, pivot);
				throw new RuntimeException("Error searching first triad");				
			}
			if(current==triad)return first;
			newPivot = first.getCommonPivot(current, newPivot);
			first = current;
		}
		return first;
	}
}
