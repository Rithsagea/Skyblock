package com.rithsagea.skyblock.util;

public class MathUtil {
	public static boolean between(long value, long lower, long upper) {
		return (value > lower) && (value < upper);
	}
}
