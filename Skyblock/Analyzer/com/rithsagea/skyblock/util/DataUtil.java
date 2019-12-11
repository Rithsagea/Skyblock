package com.rithsagea.skyblock.util;

import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.StatUtils;

import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.datatypes.Datapoint;

public class DataUtil {
	public static int removeOutliers(List<Datapoint> data, double z_limit) {
		int outlierCount = 0;
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
		
		return outlierCount;
	}

	public static Datapoint[] rollingAverage(Datapoint[] data, Timestamp start, Timestamp end, double z_limit, long interval, long window, TimeUnit unit) {
		Logger.log("Getting the rolling average of " + data.length + " datapoints.");
		
		int outlierCount = 0;
		
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
				outlierCount += removeOutliers(elements, z_limit);
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
	
}
