package com.rithsagea.skyblock.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.rithsagea.skyblock.api.datatypes.DroppingQueue;

public class Logger {
	
	private static List<DroppingQueue<String>> logs = new ArrayList<DroppingQueue<String>>();
	private static final Calendar calendar = Calendar.getInstance();
	
	public static void addListener(DroppingQueue<String> log) {
		logs.add(log);
	}
	
	public static void log(String info) {
		calendar.setTimeInMillis(System.currentTimeMillis());
		String line = "[" + calendar.getTime() + "] " + info + '\n';
		for(DroppingQueue<String> log : logs) {
			log.add(line);
		}
		System.out.print(line);
	}
}
