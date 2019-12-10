package com.rithsagea.skyblock.api.datatypes;

import java.sql.Timestamp;

public class Datapoint {
	
	public Timestamp time = null;
	public double unit_price = 0;
	public double value = 0;
	public int amount = 0;
	
	public String toString() {
		return String.format("[%s, %.0f, %d, %.2f]", time, value, amount, unit_price);
	}
	
	public Datapoint(Timestamp time, double value, int amount) {
		this.time = time;
		this.value = value;
		this.amount = amount;
		
		unit_price = value / amount;
	}
}
