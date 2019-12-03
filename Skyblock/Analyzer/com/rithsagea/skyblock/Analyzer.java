package com.rithsagea.skyblock;

import java.sql.Timestamp;
import java.util.List;

import com.rithsagea.skyblock.api.datatypes.Datapoint;

public class Analyzer {
	
	private final List<Datapoint> data;
	
	public Analyzer(List<Datapoint> data) {
		this.data = data;
	}
	
	public void printData() {
		for(Datapoint point : data) {
			System.out.format("[%s, %f]\n", point.time, point.price);
		}
	}
	
	public void filter(Timestamp start, Timestamp end) {
		for(Datapoint point : data) {
			if(point.time.getTime() > 0);
		}
	}
}
