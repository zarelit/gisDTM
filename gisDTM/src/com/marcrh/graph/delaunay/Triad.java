package com.marcrh.graph.delaunay;

import java.util.List;

import com.marcrh.graph.Point;

public class Triad extends Point {

	public static final int PIVOT_A = 0;
	public static final int PIVOT_B = 1;
	public static final int PIVOT_C = 2;
	public static final int PIVOT_INVALID = -1;

	public static final int TR_EDGE_AB = 0;
	public static final int TR_EDGE_BC = 1;
	public static final int TR_EDGE_AC = 2;
	public static final int TR_EDGE_INVALID = -1;

	private static double EPSILON = 0.000001;

	public int a, b, c;
	public int ab, bc, ac; // adjacent edges index to neighbouring triangle.

	public double circumcircleR2;

	public Triad(int a, int b, int c, List<? extends Point> points) {
		this.a = a;
		this.b = b;
		this.c = c;
		ab = -1;
		bc = -1;
		ac = -1;
		findCircumcircle(points);
	}

	public void set(int a, int b, int c, int ab, int bc, int ac,
			List<? extends Point> points) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.ab = ab;
		this.bc = bc;
		this.ac = ac;

		findCircumcircle(points);
	}

	public int getPivot(int pointIndex) {
		if (a == pointIndex) {
			return PIVOT_A;
		} else if (b == pointIndex) {
			return PIVOT_B;
		} else if (c == pointIndex) {
			return PIVOT_C;
		} else {
			return -1;
		}
	}

	public int getPointIndexInPivot(int pivot) {
		switch (pivot) {
		case Triad.PIVOT_A:
			return a;
		case Triad.PIVOT_B:
			return b;
		case Triad.PIVOT_C:
			return c;
		}
		return PIVOT_INVALID;
	}

	public Point getPointInPivot(List<? extends Point> points, int pivot) {
		switch (pivot) {
		case Triad.PIVOT_A:
			return points.get(a);
		case Triad.PIVOT_B:
			return points.get(b);
		case Triad.PIVOT_C:
			return points.get(c);
		}
		return null;
	}

	public int getCommonPivot(Triad tr, int pivot) {
		switch (pivot) {
		case Triad.PIVOT_A:
			if (a == tr.a) {
				return Triad.PIVOT_A;
			} else if (a == tr.b) {
				return Triad.PIVOT_B;
			} else if (a == tr.c) {
				return Triad.PIVOT_C;
			}
		case Triad.PIVOT_B:
			if (b == tr.a) {
				return Triad.PIVOT_A;
			} else if (b == tr.b) {
				return Triad.PIVOT_B;
			} else if (b == tr.c) {
				return Triad.PIVOT_C;
			}
		case Triad.PIVOT_C:
			if (c == tr.a) {
				return Triad.PIVOT_A;
			} else if (c == tr.b) {
				return Triad.PIVOT_B;
			} else if (c == tr.c) {
				return Triad.PIVOT_C;
			}
		}
		return Triad.PIVOT_INVALID;
	}

	public Point[] getCommonPoints(Triad tr, List<? extends Point> points) {
		Point p1 = null;
		Point p2 = null;
		if (a == tr.a || a == tr.b || a == tr.c) {
			p1 = points.get(a);
		}
		if (b == tr.a || b == tr.b || b == tr.c) {
			if (p1 == null) {
				p1 = points.get(b);
			} else {
				p2 = points.get(b);
			}
		}
		if (c == tr.a || c == tr.b || c == tr.c) {
			if (p1 == null) {
				p1 = points.get(c);
			} else {
				p2 = points.get(c);
			}
		}
		return new Point[] { p1, p2 };
	}

	public int getNeighbourIndex(int edge) {
		switch (edge) {
		case Triad.TR_EDGE_AB:
			return ab;
		case Triad.TR_EDGE_BC:
			return bc;
		case Triad.TR_EDGE_AC:
			return ac;
		}
		return -1;
	}

	public Triad getNeighbour(List<Triad> triads, int edge) {
		switch (edge) {
		case Triad.TR_EDGE_AB:
			return triads.get(ab);
		case Triad.TR_EDGE_BC:
			return triads.get(bc);
		case Triad.TR_EDGE_AC:
			return triads.get(ac);
		}
		return null;
	}

	public Triad getPrevious(List<Triad> triads, int pivot) {
		Triad previous = null;
		switch (pivot) {
		case Triad.PIVOT_A:
			if (ab != -1)
				previous = triads.get(ab);
			break;
		case Triad.PIVOT_B:
			if (bc != -1)
				previous = triads.get(bc);
			break;
		case Triad.PIVOT_C:
			if (ac != -1)
				previous = triads.get(ac);
		}
		return previous;
	}

	public Triad getNext(List<Triad> triads, int pivot) {
		Triad next = null;
		switch (pivot) {
		case Triad.PIVOT_A:
			if (ac != -1)
				next = triads.get(ac);
			break;
		case Triad.PIVOT_B:
			if (ab != -1)
				next = triads.get(ab);
			break;
		case Triad.PIVOT_C:
			if (bc != -1)
				next = triads.get(bc);
		}
		return next;
	}

	/**
	 * If current orientation is not clockwise, swap b<->c
	 * @param points
	 */
	void makeCW(List<? extends Point> points) {
		if (isClockwise(points)) {
			// Need to swap vertices b<->c and edges ab<->bc
			int t = b;
			b = c;
			c = t;

			t = ab;
			ab = ac;
			ac = t;
		}
	}

	public double area(List<? extends Point> points) {
		Point pa = points.get(a);
		Point pb = points.get(b);
		Point pc = points.get(c);
		double a = (pa.x - pc.x) * (pb.y - pa.y);
		double b = (pa.x - pb.x) * (pc.y - pa.y);

		return 0.5 * Math.abs(a - b);
	}

	boolean isClockwise(List<? extends Point> points) {
		Point pa = points.get(a);
		Point pb = points.get(b);
		Point pc = points.get(c);
		double pax = pa.x;
		double pay = pa.y;
		double pbx = pb.x;
		double pby = pb.y;
		double pcx = pc.x;
		double pcy = pc.y;
		double centroidX = (pax + pbx + pcx) / 3d;
		double centroidY = (pay + pby + pcy) / 3d;

		double dr0 = pax - centroidX;
		double dc0 = pay - centroidY;
		double dx01 = pbx - pax;
		double dy01 = pby - pay;

		double df = -dx01 * dc0 + dy01 * dr0;
		return df > 0;
	}

	boolean findCircumcircle(List<? extends Point> points) {
		Point pa = points.get(a);
		Point pb = points.get(b);
		Point pc = points.get(c);
		double x1 = pa.x;
		double y1 = pa.y;
		double x2 = pb.x;
		double y2 = pb.y;
		double x3 = pc.x;
		double y3 = pc.y;
		double m1, m2, mx1, mx2, my1, my2;
		double dx, dy, rsqr, drsqr;
		double xc, yc;

		if (Math.abs(y2 - y1) < EPSILON) {
			m2 = -(x3 - x2) / (y3 - y2);
			mx2 = (x2 + x3) / 2.0;
			my2 = (y2 + y3) / 2.0;
			xc = (x2 + x1) / 2.0;
			yc = m2 * (xc - mx2) + my2;
		} else if (Math.abs(y3 - y2) < EPSILON) {
			m1 = -(x2 - x1) / (y2 - y1);
			mx1 = (x1 + x2) / 2.0;
			my1 = (y1 + y2) / 2.0;
			xc = (x3 + x2) / 2.0;
			yc = m1 * (xc - mx1) + my1;
		} else {
			m1 = -(x2 - x1) / (y2 - y1);
			m2 = -(x3 - x2) / (y3 - y2);
			mx1 = (x1 + x2) / 2.0;
			mx2 = (x2 + x3) / 2.0;
			my1 = (y1 + y2) / 2.0;
			my2 = (y2 + y3) / 2.0;
			xc = (m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2);
			yc = m1 * (xc - mx1) + my1;
		}

		dx = x2 - xc;
		dy = y2 - yc;
		rsqr = dx * dx + dy * dy;

		x = xc;
		y = yc;
		circumcircleR2 = Math.sqrt(rsqr);
		circumcircleR2 = circumcircleR2 * circumcircleR2;
		return true;
	}

	/**
	 * Return true iff Point p is inside the circumcircle of this triangle
	 * @param p {@link Point} to check
	 * @return true if p is inside the circumcircle, false otherwise.
	 */
	public boolean isInCircumcircle(Point p) {
		double dx = x - p.x;
		double dy = y - p.y;
		double r2 = dx * dx + dy * dy;
		return r2 < circumcircleR2;
	}

	/**
	 * Change any adjacent triangle index that matches fromIndex, to toIndex
	 * @param fromIndex
	 * @param toIndex
	 */
	public void changeAdjacentIndex(int fromIndex, int toIndex) {
		if (ab == fromIndex)
			ab = toIndex;
		else if (bc == fromIndex)
			bc = toIndex;
		else if (ac == fromIndex)
			ac = toIndex;
	}

	/**
	 * Determine which edge matches the triangleIndex, then which Point the PointIndex
	 * Set the indices of the opposite Point, left and right edges accordingly
	 * @param PointIndex
	 * @param triangleIndex
	 * @param indexes
	 */	
	public void findAdjacency(int PointIndex, int triangleIndex, int indexes[]) {
		if (ab == triangleIndex) {
			indexes[0] = c; // indexOpposite = c;

			if (PointIndex == a) {
				indexes[1] = ac; // indexLeft = ac;
				indexes[2] = bc; // indexRight = bc;
			} else {
				indexes[1] = bc; // indexLeft = bc;
				indexes[2] = ac; // indexRight = ac;
			}
		} else if (ac == triangleIndex) {
			indexes[0] = b; // indexOpposite = b;

			if (PointIndex == a) {
				indexes[1] = ab; // indexLeft = ab;
				indexes[2] = bc; // indexRight = bc;
			} else {
				indexes[1] = bc; // indexLeft = bc;
				indexes[2] = ab; // indexRight = ab;
			}
		} else if (bc == triangleIndex) {
			indexes[0] = a; // indexOpposite = a;

			if (PointIndex == b) {
				indexes[1] = ab; // indexLeft = ab;
				indexes[2] = ac; // indexRight = ac;
			} else {
				indexes[1] = ac; // indexLeft = ac;
				indexes[2] = ab; // indexRight = ab;
			}
		} else {
			indexes[0] = 0;
			indexes[1] = 0;
			indexes[2] = 0;
		}
	}

	public String toString() {
		return "Triad vertices " + a + " " + b + " " + c + "; edges " + ab
				+ " " + bc + " " + ac;
	}
}
