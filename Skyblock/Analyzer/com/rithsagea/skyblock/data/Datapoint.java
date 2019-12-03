package com.rithsagea.skyblock.data;

import java.sql.Timestamp;

public class Datapoint {
	public Timestamp time;
	public double price;
	
	public Datapoint(Timestamp time, double price) {
		this.time = time;
		this.price = price;
	}
}
