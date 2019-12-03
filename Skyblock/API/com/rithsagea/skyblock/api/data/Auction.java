package com.rithsagea.skyblock.api.data;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import com.rithsagea.skyblock.util.NBTUtil;

public class Auction {
	
	public UUID			id;
	public String 		item_type;
	public String		modifier;
	public String		enchants;
	public byte	 		amount;
	public Timestamp		start_time;
	public Timestamp		end_time;
	public double 		price;
	
	public void printAuction() {
		System.out.println("\n\n\n");
		System.out.print(modifier + " ");
		System.out.println(item_type + " x" + amount);
		
		System.out.println("--------------------");
		
		Map<String, Integer> enchantMap  = NBTUtil.stringToEnchants(enchants);
		for(String enchant : enchantMap.keySet()) {
			System.out.println(enchant + " " + enchantMap.get(enchant));
		}
		
		System.out.println("Price: " + price);
		System.out.println("Start: " + start_time);
		System.out.println("End  : " + end_time);
	}
}
