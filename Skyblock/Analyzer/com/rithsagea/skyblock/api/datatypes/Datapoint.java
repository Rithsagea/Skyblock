package com.rithsagea.skyblock.api.datatypes;

import java.sql.Timestamp;

public class Datapoint {
	
	public Timestamp time = null;
	public double value = 0;
	public short amount = 0;
	
	public Datapoint(Timestamp time, double value, short amount) {
		this.time = time;
		this.value = value;
		this.amount = amount;
	}
}
