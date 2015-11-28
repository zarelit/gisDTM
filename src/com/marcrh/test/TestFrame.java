package com.marcrh.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.marcrh.graph.Point;
import com.marcrh.graph.Range;
import com.marcrh.graph.Utils;
import com.marcrh.graph.delaunay.Triad;
import com.marcrh.graph.delaunay.Voronoi;
import com.marcrh.utils.IntTextField;

public class TestFrame extends JFrame{
					
	private int NUM_POINTS = 500;
	private int size = 600;
	
	private ArrayList<Point> points;
	private Voronoi voronoi;
		
	private TestCanvas canvas = new TestCanvas();
	private JPanel toolsPanel = new JPanel();
	private JCheckBox viewPoints = new JCheckBox("Points",true);
	private JCheckBox viewTriangulation = new JCheckBox("Triangulation",true);
	private JCheckBox viewVoronoi = new JCheckBox("Voronoi",true);
	private IntTextField nPoints = new IntTextField(NUM_POINTS,7);
	
	public TestFrame(){
		Dimension dim = new Dimension(size,size);
		this.setMinimumSize(dim);
		this.setPreferredSize(dim);
		this.setLayout(new BorderLayout());
		this.add(canvas, BorderLayout.CENTER);
		toolsPanel.setLayout(new FlowLayout());
		toolsPanel.add(new JButton(new GenerateAction()));
		toolsPanel.add(viewTriangulation);
		toolsPanel.add(viewVoronoi);
		toolsPanel.add(viewPoints);
		viewPoints.addChangeListener(new InvalidateChangeListener());
		viewTriangulation.addChangeListener(new InvalidateChangeListener());
		viewVoronoi.addChangeListener(new InvalidateChangeListener());
		toolsPanel.add(nPoints);
		this.add(toolsPanel,BorderLayout.SOUTH);
		pack();
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private void reset(){
    	voronoi = new Voronoi();
    }
	
	private void generate(){
		try{
			reset();
			int top = -canvas.getWidth()/2;
			int left = -canvas.getHeight()/2;
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			
	        Range r = new Range(new Point(top+10,left+10),new Point(top+width-10,left+height-10));
	        points = Utils.generateRandomPoints(nPoints.getValue(), r);			
	        voronoi.generate(points, r);
		}catch(Exception e){
			//Utils.printPoints(map.getPoints());
			e.printStackTrace();
		}
		canvas.repaint();
	}
	
	private class InvalidateChangeListener implements ChangeListener{
		@Override
		public void stateChanged(ChangeEvent e) {
			canvas.invalidate();
		}
	}
	
	private class GenerateAction extends AbstractAction{
		
		public GenerateAction() {
			super("Generate");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			generate();
		}
	}
	
	private class TestCanvas extends JComponent{
		
		protected BufferedImage bmFront;
	    private Graphics2D bmFrontG;		
		
		public TestCanvas(){
			addComponentListener(new ComponentAdapter(){			
				@Override
				public void componentResized(ComponentEvent arg0) {
					bmFront = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
					bmFrontG = (Graphics2D)bmFront.createGraphics();
			        bmFrontG.translate(bmFront.getWidth()/2, bmFront.getHeight()/2);
					bmFrontG.scale(1, -1);
					invalidate();
				}			
			});		
		}
		
		@Override
		public void invalidate() {
			if(getGraphics()!=null){
				repaint();
			}
		}
		
		@Override
		public void paint(Graphics g) {
			if(bmFront==null || voronoi == null){
				return;
			}
			bmFrontG.setBackground(Color.GRAY);	        
	        bmFrontG.clearRect(-getWidth()/2, -getHeight()/2,getWidth(), getHeight());
	        bmFrontG.setBackground(Color.BLACK);	        
	        bmFrontG.clearRect(-getWidth()/2+10, -getHeight()/2+10,getWidth()-20, getHeight()-20);
			
			
			if(voronoi!=null){
				if(viewTriangulation.isSelected()){
					drawTriangulation(bmFrontG,voronoi.getTriads());
				}
				if(viewVoronoi.isSelected()){
					bmFrontG.setColor(Color.BLUE);
					drawVoronoi(bmFrontG);
				}
			}
			if(points!=null && viewPoints.isSelected()){
				bmFrontG.setColor(Color.WHITE);
				drawPoints(bmFrontG,points);
			}
			g.drawImage(bmFront, 0, 0, null);
		}
				
		private void drawTriadsCircumcenter(Graphics2D g,List<Triad> points){			
			for(int i=0;i<points.size();i++){
				Triad p = points.get(i);
				g.fillOval((int)p.x-1, (int)p.y-1, 3, 3);
			}
		}
		
		private void drawPoints(Graphics2D g,List<Point> points){			
			for(int i=0;i<points.size();i++){
				Point p = points.get(i);
				g.fillOval((int)p.x-1, (int)p.y-1, 3, 3);
			}
		}
				
		private void drawPoligon(Graphics2D g,List<Point> p){
			if(p.size()<2)return;
			Path2D path = new Path2D.Float();
			path.moveTo(p.get(0).x, p.get(0).y);
			for(int i=1;i<p.size();i++){
				path.lineTo(p.get(i).x, p.get(i).y);
			}
			path.lineTo(p.get(0).x, p.get(0).y);
			g.draw(path);
		}
				
		private void drawTriangulation(Graphics2D g, List<Triad> triads){
			bmFrontG.setColor(Color.RED);
			for (Triad triad : triads) {
				drawTriad(g,triad);
			}
			//bmFrontG.setColor(Color.ORANGE);
			//drawTriadsCircumcenter(g,triads);
		}
		
		private void drawTriad(Graphics2D g,Triad triad){
			Point a = points.get(triad.a);
			Point b = points.get(triad.b);
			Point c = points.get(triad.c);
			Path2D path = new Path2D.Float();
			path.moveTo(a.x, a.y);
			path.lineTo(b.x, b.y);
			path.lineTo(c.x, c.y);
			path.lineTo(a.x, a.y);
			g.draw(path);
		}
		
		private void drawVoronoi(Graphics2D g){
			for(int i=0;i<points.size();i++){
				List<Point> p = voronoi.getRegion(i).getPoints();
				drawPoligon(g, p);
			}
		}
	}
}