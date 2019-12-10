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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
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
			//TODO remove outliers
			if(!currentData.isEmpty()) {
				List<Datapoint> a = new ArrayList<Datapoint>();
				Iterator<Datapoint> iterator = currentData.iterator();
				while(iterator.hasNext()) {
					a.add(iterator.next());
					//remove outliers here
					Datapoint point = iterator.next();
					price += point.value;
					amount += point.amount;
				}
				
				//calculate stuff here
			}
			
			storedData.add(new Datapoint(time, price, amount));
			
			currentTime.setTime(currentTime.getTime() + dataInterval);
		} while(currentTime.before(end));
		
		Datapoint[] array = new Datapoint[storedData.size()];
		return storedData.toArray(array);
	}
	
	public static Datapoint[] processData(Datapoint[] data) {
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;
		
		start = truncateTime(start);
		end = truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		long interval = 5;
		long window = 30;
		TimeUnit unit = TimeUnit.MINUTES;
		
		return rollingAverage(data, start, end, interval, window, unit);
	}
	
	public static Datapoint[] removeOutliers(Datapoint[] data, double z_limit) {
		List<Datapoint> withoutOutliers = new ArrayList<Datapoint>();
		//calculate mean
		double[] values = new double[data.length];
		for(int x = 0; x < data.length; x++) {
			values[x] = data[x].value / data[x].amount;
		}
		
		double mean = StatUtils.mean(values);
		double variance = StatUtils.populationVariance(values, mean);
		double sd = Math.sqrt(variance);
		NormalDistribution nd = new NormalDistribution();
		
		for(int x = 0; x < values.length; x++) {
			double stdscore = (values[x] - mean) / sd;
			double sf = 1.0 - nd.cumulativeProbability(Math.abs(stdscore));
			Logger.log("" + stdscore + " " + sf);
		}
		
		Datapoint[] r = new Datapoint[withoutOutliers.size()];
		return withoutOutliers.toArray(r);
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		//Get Data
		String itemType = "WISE_FRAGMENT";
		
		ResultSet auctions = db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"WISE_FRAGMENT\" order by end_time asc");
		Datapoint[] data = resultsToData(auctions);
		
		//Process Data
		Datapoint[] pd = processData(data);
		
		//Write Rolling Average to CSV
		BufferedWriter writer = Files.newBufferedWriter(Paths.get("./" + itemType + ".csv"));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
				.withHeader("timestamp", "total", "amount"));
		
		for(int x = 0; x < pd.length; x++) {
			printer.printRecord(pd[x].time, pd[x].value, pd[x].amount);
		}
		printer.flush();
		printer.close();
	}
}
