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
	
	public static Datapoint[] movingAverage(Datapoint[] data, Timestamp start, Timestamp end, long interval, TimeUnit unit) {
		Timestamp currentTime = new Timestamp(start.getTime());
		Deque<Datapoint> dataIn = new ArrayDeque<Datapoint>(Arrays.asList(data));
		List<Datapoint> ma = new ArrayList<Datapoint>();
		
		long dataInterval = TimeUnit.MILLISECONDS.convert(interval, unit);
		
		double totalPrice = 0;
		int totalAmount = 0;
		
		do {
			while((!dataIn.isEmpty()) && 
					dataIn.peekFirst().time.getTime() < currentTime.getTime()) {
				Datapoint point = dataIn.poll();
				totalPrice += point.value;
				totalAmount += point.amount;
			}
			
			ma.add(new Datapoint(new Timestamp(currentTime.getTime()), totalPrice, totalAmount));
			currentTime.setTime(currentTime.getTime() + dataInterval);
		} while(currentTime.before(end));
		
		return ma.toArray(new Datapoint[ma.size()]);
	}
	
	public static Datapoint[] movingAverage(Datapoint[] data, Timestamp start, Timestamp end, double z_limit, long interval, long window, TimeUnit unit) {
		Logger.log("Getting the rolling average of " + data.length + " datapoints.");
		
		int outlierCount = 0;
		
		List<Datapoint> list = Arrays.asList(data);
		Deque<Datapoint> dataIn = new ArrayDeque<Datapoint>(list);
		Deque<Datapoint> dataActive = new ArrayDeque<Datapoint>();
		List<Datapoint> storedData = new ArrayList<Datapoint>();
		
		Timestamp currentTime = new Timestamp(start.getTime());
		
		long dataInterval = TimeUnit.MILLISECONDS.convert(interval, unit);
		long dataWindow = TimeUnit.MILLISECONDS.convert(window, unit);
		
		do {
			//get rid of old values
			while((!dataActive.isEmpty()) && 
					dataActive.peekLast().time.getTime() < currentTime.getTime() - dataWindow) {
				dataActive.removeLast();
			}
			
			//read new values
			while((!dataIn.isEmpty()) && 
					dataIn.peekFirst().time.getTime() + dataWindow < currentTime.getTime()) {
				dataActive.add(dataIn.poll());
			}
			
			Timestamp time = new Timestamp(currentTime.getTime());
			double price = 0;
			int amount = 0;
			
			//write data
			if(!dataActive.isEmpty()) {
				List<Datapoint> elements = new ArrayList<Datapoint>(dataActive);
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
	
	//TODO implement
	public static Datapoint[] expSmooth(Datapoint[] data, double alpha) {
		Datapoint[] newData = new Datapoint[data.length];
		double[] values = new double[data.length];
		double sum;
		
		for(int x = 0; x < data.length; x++) {
			sum = 0;
			values[x] = data[x].unit_price;
			for(int y = x; y >= 0; y--) {
				values[x] *= alpha;
				sum += values[x];
			}
			newData[x] = new Datapoint(data[x].time, sum);
		}
		
		return newData;
	}
}
