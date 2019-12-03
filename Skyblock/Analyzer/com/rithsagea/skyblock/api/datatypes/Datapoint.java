package com.rithsagea.skyblock.api.datatypes;

import java.sql.Timestamp;

public class Datapoint {
	public Timestamp time;
	public double price;
	
	public Datapoint(Timestamp time, double price) {
		this.time = time;
		this.price = price;
	}
}
