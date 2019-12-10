package com.rithsagea.skyblock.api.datatypes;

import java.sql.Timestamp;

public class Datapoint {
	
	public Timestamp time = null;
	public double value = 0;
	public int amount = 0;
	
	public Datapoint(Timestamp time, double value, int amount) {
		this.time = time;
		this.value = value;
		this.amount = amount;
	}
}
