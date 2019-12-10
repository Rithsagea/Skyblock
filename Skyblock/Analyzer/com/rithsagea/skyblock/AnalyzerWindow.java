package com.rithsagea.skyblock;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.stat.StatUtils;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.datatypes.Datapoint;

public class AnalyzerWindow {
	
	private static final DatabaseConnection db = new DatabaseConnection();
	private static final Calendar calendar = Calendar.getInstance();
	
	private static int outlierCount = 0;
	
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
		int row_count = data.length;
		
		for(int x = 0; x < row_count; x++) {
			System.out.println(data[x]);
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
	
	public static Datapoint[] rollingAverage(Datapoint[] data, Timestamp start, Timestamp end, long interval, long window, TimeUnit unit) {
		Logger.log("Getting the rolling average of " + data.length + " datapoints.");
		
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
			
			//write data
			if(!currentData.isEmpty()) {
				List<Datapoint> elements = new ArrayList<Datapoint>(currentData);
				removeOutliers(elements, 2);
				for(Datapoint point : elements) {
					price += point.value;
					amount += point.amount;
				}
			}
			
			storedData.add(new Datapoint(time, price, amount));
			
			currentTime.setTime(currentTime.getTime() + dataInterval);
		} while(currentTime.before(end));
		
		Datapoint[] array = new Datapoint[storedData.size()];
		Logger.log("There were " + outlierCount + " outliers found.");
		Logger.log("Final array has a total number of " + storedData.size() + " elements");
		return storedData.toArray(array);
	}
	
	public static Datapoint[] processData(Datapoint[] data, long interval, long window, TimeUnit unit) {
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;
		
		start = truncateTime(start);
		end = truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		return rollingAverage(data, start, end, interval, window, unit);
	}
	
	public static void removeOutliers(List<Datapoint> data, double z_limit) {
		//calculate mean
		List<Double> values = new ArrayList<Double>();
		for(Datapoint point : data) {
			values.add(point.unit_price);
		}
		double[] value_array = values.stream().mapToDouble(Double::doubleValue).toArray();
		
		double mean = StatUtils.mean(value_array);
		double variance = StatUtils.populationVariance(value_array, mean);
		double stdDeviation = Math.sqrt(variance);
		
		List<Datapoint> outliers = new ArrayList<Datapoint>();
		
		//get outliers
		for(Datapoint point : data) {
			double stdscore = (point.unit_price - mean) / stdDeviation;
			
			if(stdscore > z_limit) {
				outliers.add(point);
				outlierCount++;
			}
		}
		
		//remove outliers
		for(Datapoint point : outliers) {
			data.remove(point);
		}
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		
		long interval = 5;
		long window = 60;
		TimeUnit unit = TimeUnit.MINUTES;
		
		//Get Data
		String itemType = "WISE_FRAGMENT";
		
		ResultSet auctions = db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"WISE_FRAGMENT\" order by end_time asc");
		Datapoint[] data = resultsToData(auctions);
		
		//Process Data
		Datapoint[] pd = processData(data, interval, window, unit);
		
		//Write Rolling Average to CSV
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("./" + itemType + ".csv"));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
				.withHeader("timestamp", "unit_price"));
		
		for(int x = 0; x < pd.length; x++) {
			printer.printRecord(pd[x].time, pd[x].unit_price);
		}
		
		printer.flush();
		printer.close();
	}
}
