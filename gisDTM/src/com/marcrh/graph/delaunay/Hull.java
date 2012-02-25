package com.marcrh.graph.delaunay;

import java.util.LinkedList;
import java.util.List;

import com.marcrh.graph.Point;

    /** 
     * Vertices belonging to the convex hull need to maintain a point and triad index
     */
    class HullPoint extends Point
    {
        public int pointsIndex;
        public int triadIndex;

        public HullPoint(List<? extends Point> points, int pointIndex)
        {
        	Point p = points.get(pointIndex);
            x = p.x;
            y = p.y;
            this.pointsIndex = pointIndex;
            triadIndex = 0;
        }
    }

    /**
     * Hull represents a list of vertices in the convex hull, and keeps track of their indices (into the associated points list) and triads
     */
    class Hull extends LinkedList<HullPoint>
    {
    	private Point auxiliar = new Point();
    	
    	public Hull(){    		
    	}
    	
    	public void removeRange(int startIndex, int toIndex){    		
    		super.removeRange(startIndex, toIndex);
    	}
    	
        private int nextIndex(int index)
        {
            if (index == size() - 1)
                return 0;
            else
                return index + 1;
        }

        /**
         * Assign to vector {@link Point} the vector values from the current index Hull point to the next indexed point.
         * @param index To the Hull point origin of the vector.
         * @param vector {@link Point} to retain the vector value.
         */
        public void vectorToNext(int index, Point vector)
        {
            Point et = this.get(index);            		
            Point en = this.get(nextIndex(index));
            
            vector.x = en.x - et.x;
            vector.y = en.y - et.y;
        }
        
        /**
         * Return whether the hull Point at index is visible from the supplied coordinates
         * @param index
         * @param dx
         * @param dy
         * @return
         */
        public boolean edgeVisibleFrom(int index, double dx, double dy)
        {            
            vectorToNext(index, auxiliar);

            double crossProduct = -dy * auxiliar.x + dx * auxiliar.y;
            return crossProduct < 0;
        }

        /**
         * Return whether the hull Point at index is visible from the point
         * @param index
         * @param point
         * @return
         */
        public boolean edgeVisibleFrom(int index, Point point)
        {
            vectorToNext(index, auxiliar);

            Point et = this.get(index);
            double dx = point.x - et.x;
            double dy = point.y - et.y;

            double crossProduct = -dy * auxiliar.x + dx * auxiliar.y;
            return crossProduct < 0;
        }
    }
