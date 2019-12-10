package com.rithsagea.skyblock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
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
		int amount;
		
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
	public static Datapoint[] rollingAverage(Datapoint[] data, Timestamp start, Timestamp end, long interval, long window, TimeUnit unit) {
		List<Datapoint> list = Arrays.asList(data);
		Deque<Datapoint> countingData = new ArrayDeque<Datapoint>(list);
		Deque<Datapoint> currentData = new ArrayDeque<Datapoint>();
		List<Datapoint> storedData = new ArrayList<Datapoint>();
		
		Timestamp currentTime = new Timestamp(start.getTime());
		
		long dataInterval = TimeUnit.MILLISECONDS.convert(interval, unit);
		long dataWindow = TimeUnit.MILLISECONDS.convert(window, unit);
		
		do {
			//get rid of old values
			while((!currentData.isEmpty()) && 
					currentData.peekLast().time.getTime() < currentTime.getTime() - dataWindow) {
				currentData.removeLast();
			}
			
			//read new values
			while((!countingData.isEmpty()) && 
					countingData.peekFirst().time.getTime() + dataWindow < currentTime.getTime()) {
				currentData.add(countingData.poll());
			}
			
			Timestamp time = new Timestamp(currentTime.getTime());
			double price = 0;
			int amount = 0;
			
			if(!currentData.isEmpty()) {
			
				Iterator<Datapoint> iterator = currentData.iterator();
				while(iterator.hasNext()) {
					Datapoint point = iterator.next();
					price += point.value;
					amount += point.amount;
				}
			}
			
			storedData.add(new Datapoint(time, price, amount));
			
			currentTime.setTime(currentTime.getTime() + dataInterval);
		} while(currentTime.before(end));
		
		Datapoint[] array = new Datapoint[storedData.size()];
		return storedData.toArray(array);
	}
	
	public static void main(String[] args) throws SQLException {
		//Get Data
		ResultSet auctions = db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"WISE_FRAGMENT\" order by end_time asc");
		Datapoint[] data = resultsToData(auctions);
//		printData(data);
		
		//Process Data
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;

		start = truncateTime(start);
		end = truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		data = rollingAverage(data, start, end, 10, 10, TimeUnit.MINUTES);
		printData(data);
	}
}
