package com.marcrh.graph.delaunay;

import java.util.ArrayList;
import java.util.HashSet;

import com.marcrh.graph.Point;
import com.marcrh.graph.Utils;

public class Triangulator {
	private ArrayList<? extends Point> points;

	public Triangulator() {
	}

	private void analyse(ArrayList<? extends Point> suppliedPoints, Hull hull,
			ArrayList<Triad> triads, boolean rejectDuplicatePoints,
			boolean hullOnly) {
		if (suppliedPoints.size() < 3)
			throw new RuntimeException("Number of points supplied must be >= 3");

		this.points = suppliedPoints;
		int nump = points.size();

		double[] distance2ToCentre = new double[nump];
		int[] sortedIndices = new int[nump];

		// Choose first point as the seed
		for (int k = 0; k < nump; k++) {
			distance2ToCentre[k] = points.get(0).getDistanceXYTo(points.get(k));
			sortedIndices[k] = k;
		}

		// Sort by distance to seed point
		Utils.sort(distance2ToCentre, sortedIndices);

		// Duplicates are more efficiently rejected now we have sorted the
		// vertices
		if (rejectDuplicatePoints) {
			// Search backwards so each removal is independent of any other
			for (int k = nump - 2; k >= 0; k--) {
				// If the points are identical then their distances will be the
				// same,
				// so they will be adjacent in the sorted list
				if (distance2ToCentre[k] == distance2ToCentre[k + 1]) {
					// System.out.println("Distancia igual");
				}

				if ((points.get(sortedIndices[k]).x == points
						.get(sortedIndices[k + 1]).x)
						&& (points.get(sortedIndices[k]).y == points
								.get(sortedIndices[k + 1]).y)) {
					// Duplicates are expected to be rare, so this is not
					// particularly efficient
					System.arraycopy(sortedIndices, k + 2, sortedIndices,
							k + 1, nump - k - 2);
					System.arraycopy(distance2ToCentre, k + 2,
							distance2ToCentre, k + 1, nump - k - 2);
					nump--;
				}
			}
		}

		System.out.println((points.size() - nump)
				+ " duplicate points rejected");

		if (nump < 3)
			throw new RuntimeException(
					"Number of unique points supplied must be >= 3");

		int mid = -1;
		double romin2 = Double.MAX_VALUE;
		double circumCentreX = 0, circumCentreY = 0;

		// Find the point which, with the first two points, creates the triangle
		// with the smallest circumcircle
		Triad tri = new Triad(sortedIndices[0], sortedIndices[1], 2, points);
		for (int kc = 2; kc < nump; kc++) {
			tri.c = sortedIndices[kc];
			if (tri.findCircumcircle(points) && tri.circumcircleR2 < romin2) {
				mid = kc;
				// Centre of the circumcentre of the seed triangle
				romin2 = tri.circumcircleR2;
				circumCentreX = tri.x;
				circumCentreY = tri.y;
			} else if (romin2 * 4 < distance2ToCentre[kc])
				break;
		}

		// Change the indices, if necessary, to make the 2th point produce the
		// smallest circumcircle with the 0th and 1th
		if (mid != 2) {
			int indexMid = sortedIndices[mid];
			double distance2Mid = distance2ToCentre[mid];

			System.arraycopy(sortedIndices, 2, sortedIndices, 3, mid - 2);
			System.arraycopy(distance2ToCentre, 2, distance2ToCentre, 3,
					mid - 2);
			sortedIndices[2] = indexMid;
			distance2ToCentre[2] = distance2Mid;
		}

		// These three points are our seed triangle
		tri.c = sortedIndices[2];
		tri.makeCW(points);
		tri.findCircumcircle(points);

		// Add tri as the first triad, and the three points to the convex hull
		triads.add(tri);
		hull.add(new HullPoint(points, tri.a));
		hull.add(new HullPoint(points, tri.b));
		hull.add(new HullPoint(points, tri.c));

		// Sort the remainder according to their distance from its centroid
		// Re-measure the points' distances from the centre of the circumcircle
		Point centre = new Point(circumCentreX, circumCentreY);
		for (int k = 3; k < nump; k++)
			distance2ToCentre[k] = points.get(sortedIndices[k])
					.getDistanceXYTo(centre);

		// Sort the _other_ points in order of distance to circumcentre
		Utils.quicksort(distance2ToCentre, sortedIndices, 3, nump - 1);
		// Array.Sort(distance2ToCentre, sortedIndices, 3, nump - 3);

		// Add new points into hull (removing obscured ones from the chain)
		// and creating triangles....
		int numt = 0;
		for (int k = 3; k < nump; k++) {
			int pointsIndex = sortedIndices[k];
			HullPoint ptx = new HullPoint(points, pointsIndex);

			HullPoint hp0 = hull.get(0);
			double dx = ptx.x - hp0.x, dy = ptx.y - hp0.y; // outwards pointing
															// from hull[0] to
															// pt.

			int numh = hull.size();
			int numh_old = numh;
			ArrayList<Integer> pidx = new ArrayList<Integer>();
			ArrayList<Integer> tridx = new ArrayList<Integer>();
			HullPoint hp = null;
			int hidx; // new hull point location within hull.....

			if (hull.edgeVisibleFrom(0, dx, dy)) {
				// starting with a visible hull facet !!!
				int e2 = numh;
				hidx = 0;

				// check to see if segment numh is also visible
				if (hull.edgeVisibleFrom(numh - 1, dx, dy)) {
					// visible.
					HullPoint visible = hull.get(numh - 1);
					pidx.add(visible.pointsIndex);
					tridx.add(hull.get(numh - 1).triadIndex);

					for (int h = 0; h < numh - 1; h++) {
						// if segment h is visible delete h
						hp = hull.get(h);
						pidx.add(hp.pointsIndex);
						tridx.add(hp.triadIndex);
						if (hull.edgeVisibleFrom(h, ptx)) {
							hull.remove(h);
							h--;
							numh--;
						} else {
							// quit on invisibility
							hull.add(0, ptx);
							numh++;
							break;
						}
					}
					// look backwards through the hull structure
					for (int h = numh - 2; h > 0; h--) {
						// if segment h is visible delete h + 1
						if (hull.edgeVisibleFrom(h, ptx)) {
							hp = hull.get(h);
							pidx.add(0, hp.pointsIndex);
							tridx.add(0, hp.triadIndex);
							hull.remove(h + 1); // erase end of chain
						} else
							break; // quit on invisibility
					}
				} else {
					hidx = 1; // keep pt hull[0]
					hp = hp0;
					tridx.add(hp.triadIndex);
					pidx.add(hp.pointsIndex);

					for (int h = 1; h < numh; h++) {
						// if segment h is visible delete h
						hp = hull.get(h);
						pidx.add(hp.pointsIndex);
						tridx.add(hp.triadIndex);
						if (hull.edgeVisibleFrom(h, ptx)) { // visible
							hull.remove(h);
							h--;
							numh--;
						} else {
							// quit on invisibility
							hull.add(h, ptx);
							break;
						}
					}
				}
			} else {
				int e1 = -1, e2 = numh;
				for (int h = 1; h < numh; h++) {
					if (hull.edgeVisibleFrom(h, ptx)) {
						if (e1 < 0)
							e1 = h; // first visible
					} else {
						if (e1 > 0) {
							// first invisible segment.
							e2 = h;
							break;
						}
					}
				}

				// triangle pidx starts at e1 and ends at e2 (inclusive).
				if (e2 < numh) {
					for (int e = e1; e <= e2; e++) {
						hp = hull.get(e);
						pidx.add(hp.pointsIndex);
						tridx.add(hp.triadIndex);
					}
				} else {
					for (int e = e1; e < e2; e++) {
						hp = hull.get(e);
						pidx.add(hp.pointsIndex);
						tridx.add(hp.triadIndex); // there are only n-1
													// triangles from n hull
													// pts.
					}
					pidx.add(hp0.pointsIndex);
				}

				// erase elements e1+1 : e2-1 inclusive.
				if (e1 < e2 - 1) {
					hull.removeRange(e1 + 1, e2);
				}
				// insert ptx at location e1+1.
				hull.add(e1 + 1, ptx);
				hidx = e1 + 1;
			}

			// If we're only computing the hull, we're done with this point
			if (hullOnly)
				continue;

			int a = pointsIndex, T0;

			int npx = pidx.size() - 1;
			numt = triads.size();
			T0 = numt;

			for (int p = 0; p < npx; p++) {
				Triad trx = new Triad(a, pidx.get(p), pidx.get(p + 1), points);
				// trx.FindCircumcirclePrecisely(points);

				trx.bc = tridx.get(p);
				if (p > 0)
					trx.ab = numt - 1;
				trx.ac = numt + 1;

				// index back into the triads.
				Triad txx = triads.get(tridx.get(p));
				if ((trx.b == txx.a && trx.c == txx.b)
						| (trx.b == txx.b && trx.c == txx.a))
					txx.ab = numt;
				else if ((trx.b == txx.a && trx.c == txx.c)
						| (trx.b == txx.c && trx.c == txx.a))
					txx.ac = numt;
				else if ((trx.b == txx.b && trx.c == txx.c)
						| (trx.b == txx.c && trx.c == txx.b))
					txx.bc = numt;

				triads.add(trx);
				numt++;
			}
			// Last edge is on the outside
			triads.get(numt - 1).ac = -1;

			hull.get(hidx).triadIndex = numt - 1;
			if (hidx > 0)
				hull.get(hidx - 1).triadIndex = T0;
			else {
				numh = hull.size();
				hull.get(numh - 1).triadIndex = T0;
			}
		}
	}

	/**
	 * Return the convex hull of the supplied points, don't check for duplicate points
	 * @param points List of vertices
	 * @return List of points that form the convex hull
	 */
	public ArrayList<? extends Point> getConvexHull(ArrayList<? extends Point> points) {
		return getConvexHull(points, false);
	}

	/**
	 * Return the convex hull of the supplied points, optionally check for duplicate points
	 * @param points List of vertices
	 * @param rejectDuplicatePoints Whether to omit duplicated points
	 * @return List of points that form the convex hull
	 */
	public ArrayList<? extends Point> getConvexHull(ArrayList<? extends Point> points,
			boolean rejectDuplicatePoints) {
		Hull hull = new Hull();
		ArrayList<Triad> triads = new ArrayList<Triad>();

		analyse(points, hull, triads, rejectDuplicatePoints, true);

		ArrayList<Point> hullVertices = new ArrayList<Point>();

		for (HullPoint hv : hull)
			hullVertices.add(new Point(hv.x, hv.y));

		return hullVertices;
	}

	/**
	 * Return the Delaunay triangulation of the supplied points, don't check for duplicate points.
	 * @param points List of vertices.
	 * @return Triads specifying the triangulation.
	 */
	public ArrayList<Triad> getTriangulation(ArrayList<? extends Point> points) {
		return getTriangulation(points, false);
	}

	/**
	 * Return the Delaunay triangulation of the supplied points, optionally check for duplicate points.
	 * @param points List of vertices.
	 * @param rejectDuplicatePoints Whether to omit duplicated points
	 * @return Triads specifying the triangulation.
	 */
	public ArrayList<Triad> getTriangulation(ArrayList<? extends Point> points,
			boolean rejectDuplicatePoints) {
		ArrayList<Triad> triads = new ArrayList<Triad>();
		Hull hull = new Hull();

		analyse(points, hull, triads, rejectDuplicatePoints, false);

		// Now, need to flip any pairs of adjacent triangles not satisfying
		// the Delaunay criterion
		int numt = triads.size();
		boolean[] idsA = new boolean[numt];
		boolean[] idsB = new boolean[numt];

		// We maintain a "list" of the triangles we've flipped in order to
		// propogate any consequent changes
		// When the number of changes is large, this is best maintained as a
		// ArrayList of bools
		// When the number becomes small, it's best maintained as a set
		// We switch between these regimes as the number flipped decreases
		//
		// the iteration cycle limit is included to prevent degenerate cases
		// 'oscillating'
		// and the algorithm failing to stop.
		int flipped = flipTriangles(triads, idsA);

		int iterations = 1;
		while (flipped > (int) (fraction * (float) numt) && iterations < 1000) {
			if ((iterations & 1) == 1)
				flipped = flipTriangles(triads, idsA, idsB);
			else
				flipped = flipTriangles(triads, idsB, idsA);

			iterations++;
		}

		HashSet<Integer> idSetA = new HashSet<Integer>();
		HashSet<Integer> idSetB = new HashSet<Integer>();
		flipped = flipTriangles(triads, ((iterations & 1) == 1) ? idsA : idsB,
				idSetA);

		iterations = 1;
		while (flipped > 0 && iterations < 2000) {
			if ((iterations & 1) == 1)
				flipped = flipTriangles(triads, idSetA, idSetB);
			else
				flipped = flipTriangles(triads, idSetB, idSetA);

			iterations++;
		}

		if (iterations == 2000)
			System.out.println("Error flipping iterations");

		return triads;
	}

	public float fraction = 0.3f;

	/**
	 * Test the triad against its 3 neighbours and flip it with any neighbour whose oposite point
	 * is inside the circumcircle of the triad
	 * @param triads The list of triads
	 * @param triadIndexToTest The index of the triad to test.
	 * @return Index of adjacent triangle it was flipped with (if any) or -1.
	 */
	int flipTriangle(ArrayList<Triad> triads, int triadIndexToTest) {
		int opositeIndexes[] = new int[] { 0, 0, 0 };
		int edge1;
		int edge2;

		int triadIndexFlipped = 0;

		Triad tri = triads.get(triadIndexToTest);
		// test all 3 neighbours of tri

		if (tri.bc >= 0) {
			triadIndexFlipped = tri.bc;
			Triad t2 = triads.get(triadIndexFlipped);
			// find relative orientation (shared limb).
			t2.findAdjacency(tri.b, triadIndexToTest, opositeIndexes);
			if (tri.isInCircumcircle(points.get(opositeIndexes[0]))
					&& t2.isInCircumcircle(points.get(tri.a))) { // not valid in
																	// the
																	// Delaunay
																	// sense.
				edge1 = tri.ab;
				edge2 = tri.ac;
				// if (edge1 != opositeIndexes[1] && edge2 != opositeIndexes[2])
				{
					int tria = tri.a, trib = tri.b, tric = tri.c;
					tri.set(tria, trib, opositeIndexes[0], edge1,
							opositeIndexes[1], triadIndexFlipped, points);
					t2.set(tria, tric, opositeIndexes[0], edge2,
							opositeIndexes[2], triadIndexToTest, points);

					// change knock on triangle labels.
					if (opositeIndexes[1] >= 0)
						triads.get(opositeIndexes[1]).changeAdjacentIndex(
								triadIndexFlipped, triadIndexToTest);
					if (edge2 >= 0)
						triads.get(edge2).changeAdjacentIndex(triadIndexToTest,
								triadIndexFlipped);
					return triadIndexFlipped;
				}
			}
		}

		if (tri.ab >= 0) {
			triadIndexFlipped = tri.ab;
			Triad t2 = triads.get(triadIndexFlipped);
			// find relative orientation (shared limb).
			t2.findAdjacency(tri.a, triadIndexToTest, opositeIndexes);
			if (tri.isInCircumcircle(points.get(opositeIndexes[0]))
					&& t2.isInCircumcircle(points.get(tri.c))) { // not valid in
																	// the
																	// Delaunay
																	// sense.
				edge1 = tri.ac;
				edge2 = tri.bc;
				// if (edge1 != opositeIndexes[1] && edge2 != opositeIndexes[2])
				{
					int tria = tri.a, trib = tri.b, tric = tri.c;
					tri.set(tric, tria, opositeIndexes[0], edge1,
							opositeIndexes[1], triadIndexFlipped, points);
					t2.set(tric, trib, opositeIndexes[0], edge2,
							opositeIndexes[2], triadIndexToTest, points);

					// change knock on triangle labels.
					if (opositeIndexes[1] >= 0)
						triads.get(opositeIndexes[1]).changeAdjacentIndex(
								triadIndexFlipped, triadIndexToTest);
					if (edge2 >= 0)
						triads.get(edge2).changeAdjacentIndex(triadIndexToTest,
								triadIndexFlipped);
					return triadIndexFlipped;
				}
			}
		}

		if (tri.ac >= 0) {
			triadIndexFlipped = tri.ac;
			Triad t2 = triads.get(triadIndexFlipped);
			// find relative orientation (shared limb).
			t2.findAdjacency(tri.a, triadIndexToTest, opositeIndexes);
			if (tri.isInCircumcircle(points.get(opositeIndexes[0]))
					&& t2.isInCircumcircle(points.get(tri.b))) { // not valid in
																	// the
																	// Delaunay
																	// sense.
				edge1 = tri.ab; // .ac shared limb
				edge2 = tri.bc;
				// if (edge1 != opositeIndexes[1] && edge2 != opositeIndexes[2])
				{
					int tria = tri.a, trib = tri.b, tric = tri.c;
					tri.set(trib, tria, opositeIndexes[0], edge1,
							opositeIndexes[1], triadIndexFlipped, points);
					t2.set(trib, tric, opositeIndexes[0], edge2,
							opositeIndexes[2], triadIndexToTest, points);

					// change knock on triangle labels.
					if (opositeIndexes[1] >= 0)
						triads.get(opositeIndexes[1]).changeAdjacentIndex(
								triadIndexFlipped, triadIndexToTest);
					if (edge2 >= 0)
						triads.get(edge2).changeAdjacentIndex(triadIndexToTest,
								triadIndexFlipped);
					return triadIndexFlipped;
				}
			}
		}

		return -1;
	}

	/**
	 * Flip triangles that do not satisfy the Delaunay condition.
	 * @param triads List of triads.
	 * @param idsFlipped array that contains the list of flipped triads.
	 * @return Number of flips
	 */
	private int flipTriangles(ArrayList<Triad> triads, boolean[] idsFlipped) {
		int numt = (int) triads.size();
		for (int i = 0; i < numt; i++) {
			idsFlipped[i] = false;
		}

		int flipped = 0;
		for (int t = 0; t < numt; t++) {
			int t2 = flipTriangle(triads, t);
			if (t2 > -1) {
				flipped += 2;
				idsFlipped[t] = true;
				idsFlipped[t2] = true;

			}
		}

		return flipped;
	}

	private int flipTriangles(ArrayList<Triad> triads, boolean[] idsToTest,
			boolean[] idsFlipped) {
		int numt = (int) triads.size();
		for (int i = 0; i < numt; i++) {
			idsFlipped[i] = false;
		}

		int flipped = 0;
		for (int t = 0; t < numt; t++) {
			if (idsToTest[t]) {
				int t2 = flipTriangle(triads, t);
				if (t2 > -1) {
					flipped += 2;
					idsFlipped[t] = true;
					idsFlipped[t2] = true;
				}
			}
		}

		return flipped;
	}

	private int flipTriangles(ArrayList<Triad> triads, boolean[] idsToTest,
			HashSet<Integer> idsFlipped) {
		int numt = (int) triads.size();
		idsFlipped.clear();

		int flipped = 0;
		for (int t = 0; t < numt; t++) {
			if (idsToTest[t]) {
				int t2 = flipTriangle(triads, t);
				if (t2 > -1) {
					flipped += 2;
					idsFlipped.add(t);
					idsFlipped.add(t2);
				}
			}
		}

		return flipped;
	}

	private int flipTriangles(ArrayList<Triad> triads,
			HashSet<Integer> idsToTest, HashSet<Integer> idsFlipped) {
		int flipped = 0;
		idsFlipped.clear();

		for (int t : idsToTest) {
			int t2 = flipTriangle(triads, t);
			if (t2 > -1) {
				flipped += 2;
				idsFlipped.add(t);
				idsFlipped.add(t2);
			}
		}

		return flipped;
	}
}
