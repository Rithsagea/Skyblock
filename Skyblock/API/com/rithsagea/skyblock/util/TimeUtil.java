package com.rithsagea.skyblock.util;

import java.sql.Timestamp;
import java.util.Calendar;

public class TimeUtil {
	
	public static Calendar calendar = Calendar.getInstance();
	
	public static Timestamp truncateTime(Timestamp time) {
		synchronized(calendar) {
			calendar.setTime(time);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			return new Timestamp(calendar.getTimeInMillis());
		}
	}
}
