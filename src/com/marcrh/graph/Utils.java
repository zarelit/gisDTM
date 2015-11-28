package com.marcrh.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {
	
	public static ArrayList<Point> generateRandomPoints(int size, Range range) {
		Random random = new Random();
		ArrayList<Point> points = new ArrayList<Point>(size);
		double x = range.getMinX();
		double y = range.getMinY();
		double width = range.getWidth();
		double height = range.getHeight();
		for (int i = 0; i < size; i++) {
			points.add(new Point(random.nextFloat()*width + x, random.nextFloat()*height + y));
		}
		return points;
	}
	
	public static ArrayList<Point> generateRandomPoints(int size, long randomSeed, Range range) {
		Random random = new Random(randomSeed);
		ArrayList<Point> points = new ArrayList<Point>(size);
		double x = range.getMinX();
		double y = range.getMinY();
		double width = range.getWidth();
		double height = range.getHeight();
		for (int i = 0; i < size; i++) {
			points.add(new Point(random.nextFloat()*width + x, random.nextFloat()*height + y));
		}
		return points;
	}
	
	public static void printPointsInRange(float x1, float y1, float x2, float y2, List<Point> points){
		Range r = new Range(new Point(x1,y1),new Point(x2,y2));
		
		for (Point point : points) {
			if(r.isInRange(point)){
				System.out.println("points.add(new Point(" + point.x + "f," + point.y + "f));");
			}
		}
	}
	
	public static void printPoints(List<Point> points){
		int size = points.size();
		for (int i = 0; i < points.size(); i++) {		
			Point point = points.get(i);
			System.out.println("points.add(new Point(" + point.x + "f," + point.y + "f));");
		}
	}
	
	public static void sort(double dist[], int indexlist[]){
		quicksort(dist, indexlist, 0, dist.length-1);		
	}
	
	public static void quicksort(double matrix[], int indexList[], int a, int b) {
		double buf;
		int buf2;
		int from = a;
		int to = b;
		double pivot = matrix[(from + to) / 2];
		do {
			while (pivot>matrix[from]) {
				from++;
			}
			while (pivot<matrix[to]) {
				to--;
			}
			if (from <= to) {
				buf = matrix[from];
				buf2 = indexList[from];
				matrix[from] = matrix[to];
				indexList[from] = indexList[to];
				matrix[to] = buf;
				indexList[to] = buf2;
				from++;
				to--;
			}
		} while (from <= to);
		if (a < to) {
			quicksort(matrix,indexList, a, to);
		}
		if (from < b) {
			quicksort(matrix,indexList, from, b);
		}
	}
}
