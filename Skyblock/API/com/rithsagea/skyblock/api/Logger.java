package com.rithsagea.skyblock.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Logger {
	
	private static List<LogQueue> logs = new ArrayList<LogQueue>();
	private static final Calendar calendar = Calendar.getInstance();
	
	public static void addListener(LogQueue log) {
		logs.add(log);
	}
	
	public static void log(String info) {
		calendar.setTimeInMillis(System.currentTimeMillis());
		String line = "[" + calendar.getTime() + "] " + info;
		for(LogQueue log : logs) {
			log.addLine(line );
		}
		System.out.println(line);
	}
}
