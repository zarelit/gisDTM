package com.marcrh.graph.delaunay;

import com.marcrh.graph.Segment;

class SegmentSuccession{
	SegmentSuccession(Segment segment){
		this.segment = segment;
	}
	Segment segment;
	SegmentSuccession next;
}