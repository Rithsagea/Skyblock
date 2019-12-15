package com.rithsagea.skyblock.util;

public class MathUtil {
	public static boolean between(long value, long lower, long upper) {
		return (value > lower) && (value < upper);
	}
	
	public static double clamp(double val, double min, double max) {
	    return Math.max(min, Math.min(max, val));
	}
}
