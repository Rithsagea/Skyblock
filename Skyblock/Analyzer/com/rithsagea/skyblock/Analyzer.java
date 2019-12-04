package com.rithsagea.skyblock;

import java.sql.Timestamp;
import java.util.List;

import com.rithsagea.skyblock.api.datatypes.Datapoint;
import com.rithsagea.skyblock.util.MathUtil;

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
		long startTime = start.getTime();
		long endTime = end.getTime();
		for(Datapoint point : data) {
			if(!MathUtil.between(point.time.getTime(), startTime, endTime)) {
				data.remove(point);
			}
		}
	}
}
