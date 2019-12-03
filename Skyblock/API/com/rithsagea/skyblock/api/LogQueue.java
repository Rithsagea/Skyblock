package com.rithsagea.skyblock.api;

import java.util.ArrayDeque;
import java.util.Deque;

public class LogQueue {
	private final StringBuilder builder = new StringBuilder();
	private final Deque<String> messages = new ArrayDeque<String>(10);
	
	public void addLine(String line) {
		messages.add(line + '\n');
		if(messages.size() > 25)
			messages.remove();
	}
	
	public String getText() {
		builder.setLength(0);
		messages.iterator().forEachRemaining(builder::append);
		return builder.toString();
	}
}
