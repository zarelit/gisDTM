package com.marcrh.utils;

import java.util.Vector;

public class Stopwatch{
	  
	  Vector statsList;
	  Stats current = null;
	  
	  public Stopwatch(){
		  this(false);
	  }
	  
	  public Stopwatch(boolean acumulate){
		  if(acumulate){
			  statsList = new Vector();
		  }
	  }
	  
	  public void start(){		  
		  current = new Stats();
		  current.start = System.currentTimeMillis();
	  }
	  
	  public void stop(){
		  current.stop = System.currentTimeMillis();
		  current.calculateElapsed();
		  if(statsList!=null){
			  statsList.addElement(current);
		  }
	  }
	  
	  public void clear(){
		  statsList.removeAllElements();
	  }
	  
	  public String toString(){
		  return "Partial time: " + current.elapsed;  
	  }
	  
	  public String toStringTotal(){
		  int size = statsList.size();
		  long elapsedTotal = 0;
		  long elapsedAverage = 0;
		  for(int i=0;i<size;i++){
			  Stats aux = (Stats)statsList.elementAt(i);
			  elapsedTotal += aux.elapsed;
		  }
		  elapsedAverage = elapsedTotal/size;
		  return "Total time: " + current.elapsed + " steps: " + size + " average:" + elapsedAverage;
	  }
	  
	  private class Stats{
		  private long start;
		  private long stop;
		  private long elapsed;
		  
		  private void calculateElapsed(){
			  elapsed = stop-start;
		  }
	  }
}