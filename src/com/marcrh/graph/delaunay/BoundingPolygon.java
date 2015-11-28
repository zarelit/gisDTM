package com.marcrh.graph.delaunay;

import java.util.ArrayList;

import com.marcrh.graph.Point;
import com.marcrh.graph.Range;
import com.marcrh.graph.Segment;

class BoundingPolygon{
		
		private Range range;
		private SegmentSuccession firstSegment;
		private double minSizeFactor;		
		
		public BoundingPolygon(Range r) {	
			this.range = r;
			
			minSizeFactor = range.getWidth()+range.getHeight();
			initSegments();
		}
		
		private void initSegments(){			
			Point p1 = new Point(range.getMinX(),range.getMaxY());
			Point p2 = range.getMax();
			Point p3 = new Point(range.getMaxX(),range.getMinY());
			Point p4 = range.getMin();
			
			SegmentSuccession current = new SegmentSuccession(new Segment(p1,p2));
			firstSegment = current;
			current.next = new SegmentSuccession(new Segment(p2, p3));			
			current = current.next;
			current.next = new SegmentSuccession(new Segment(p3,p4));
			current = current.next;			
			current.next = new SegmentSuccession(new Segment(p4,p1));
			current = current.next;	
			current.next = firstSegment;
		}
		
		public ArrayList<Point> getIntersectBB(Segment lOr, Segment lDest){
			ArrayList<Point> result = new ArrayList<Point>();
			SegmentSuccession startSegment = firstSegment;
			SegmentSuccession current = startSegment;
			Point pStart = null;
			while((pStart = current.segment.intersectSegment(lOr))==null){
				current = current.next;
				if(current==startSegment)return result;
			}
			startSegment = current;
			result.add(pStart);
			Point pEnd = null;
			while((pEnd = current.segment.intersectSegment(lDest))==null){
				result.add(current.segment.getPoint2());
				current = current.next;
				if(current==startSegment)return result;
			}
			result.add(pEnd);
			return result;
		}
		
		public SegmentSuccession addFirstIntersection(Segment segment, Region region, SegmentSuccession lastIntersected){
			SegmentSuccession startSegment = lastIntersected;
			boolean addEndPoints = startSegment!=null;
			SegmentSuccession current = null;
			if(!addEndPoints){
				current = firstSegment;
			}else{
				current = startSegment;
			}
			Point intersection = null;
			while((intersection = current.segment.intersectSegment(segment))==null){
				if(addEndPoints)region.add(current.segment.getPoint2());
				current = current.next;
				if(current==startSegment)break;
			}
			if(intersection!=null)region.add(intersection);
			return current;
		}
		
		public double getMinSizeFactor(){
			return minSizeFactor;
		}
		
		public Range getRange(){
			return range;
		}
	}