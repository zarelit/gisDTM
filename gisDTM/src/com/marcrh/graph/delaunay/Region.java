package com.marcrh.graph.delaunay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.marcrh.graph.Point;

public class Region {

	int inputIndex;
	ArrayList<Point> points;
	ArrayList<Integer> outputRelation;
	
	public Region(int index){
		this.inputIndex = index;
		points = new ArrayList<Point>();
		outputRelation = new ArrayList<Integer>();
	}
	
	public void add(Point p){
		points.add(p);
		outputRelation.add(-1);
	}
	
	void setFirsOutputIndex(int outputIndex){
		outputRelation.set(0, outputIndex);
	}
	
	void setLastOutputIndex(int outputIndex){
		outputRelation.set(outputRelation.size()-1, outputIndex);
	}
	
	void add(Point p, int outputIndex){
		points.add(p);
		outputRelation.add(outputIndex);
	}
	
	void addAll(Collection<Point> p){
		points.addAll(p);
		for(int i=0;i<p.size();i++){
			outputRelation.add(-1);
		}
	}
	
	public List<Point> getPoints(){
		return points;
	}
	
	public int getInputIndex(){
		return inputIndex;
	}
	
	public Point getPoint(int index){
		return points.get(index);
	}
	
	public int getOutputRelation(int index){
		return outputRelation.get(index);
	}
	
	public int getSize(){
		return points.size();
	}
	
	public Point[] getEdgePoints(int index){
		Point[] edge = new Point[]{
				points.get(index==0?getSize()-1:index-1)
				,points.get(index)
		};
		return edge;
	}
	
	@Override
	public String toString() {
		String output = "";
		for(int i=0;i<points.size();i++){
			output+=points.get(i) + " " + outputRelation.get(i) + "\r\n";
		}
		return output;
	}
}
