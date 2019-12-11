package com.rithsagea.skyblock.api.datatypes;

import java.sql.Timestamp;

public class Datapoint {
	
	public Timestamp time;
	public double unit_price;
	public double value;
	public int amount;
	
	public String toString() {
		return String.format("[%s, %f]", time, unit_price);
	}
	
	public Datapoint(Timestamp time, double unit_price) {
		this.time = time;
		this.unit_price = unit_price;
	}
	
	public Datapoint(Timestamp time, double value, int amount) {
		this.time = time;
		this.value = value;
		this.amount = amount;
		
		unit_price = value / amount;
	}
}
