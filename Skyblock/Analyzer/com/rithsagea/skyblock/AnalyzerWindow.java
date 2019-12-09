package com.rithsagea.skyblock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.datatypes.Datapoint;

public class AnalyzerWindow {
	
	private static final DatabaseConnection db = new DatabaseConnection();
	private static final Calendar calendar = Calendar.getInstance();
	
	public static Datapoint[] resultsToData(ResultSet auctions) throws SQLException {
		int row_count = 0;
		if(auctions.last()) {
			row_count = auctions.getRow();
			auctions.beforeFirst();
		}
		
		Datapoint[] data = new Datapoint[row_count];
		
		Datapoint point;
		for(int row = 0; row < row_count; row++) {
			auctions.next();
			point = new Datapoint(
					auctions.getTimestamp(1),
					auctions.getDouble(2),
					auctions.getShort(3));
			data[row] = point;
		}
		
		return data;
	}
	
	public static void printData(Datapoint[] data) {
		Timestamp time;
		double price;
		short amount;
		
		int row_count = data.length;
		
		for(int x = 0; x < row_count; x++) {
			time = data[x].time;
			price = data[x].value;
			amount = data[x].amount;
			
			System.out.format("[%s, %f, %f, %d]\n", time, price / amount, price, amount);
		}
	}
	
	public static Timestamp truncateTime(Timestamp time) {
		calendar.setTime(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return new Timestamp(calendar.getTimeInMillis());
	}
	
	//TODO make this generate an empty array for future rolling average
	public static Datapoint[] generateRange(Timestamp start, Timestamp end, long interval, TimeUnit interval_unit) {
		return null;
	}
	
	public static void main(String[] args) throws SQLException {
		//Get Data
		ResultSet auctions = db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"WISE_FRAGMENT\"");
		Datapoint[] data = resultsToData(auctions);
//		printData(data);
		
		//Process Data
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;

		start = truncateTime(start);
		end = truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		
	}
}
