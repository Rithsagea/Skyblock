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
	public static double getAverage(Datapoint[] data) {
		double sum = 0;
		for(int x = 0; x < data.length; x++) {
			sum += data[x].unit_price;
		}
		
		return sum / data.length;
	}
	
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
		Logger.log("-=-=- Moving Average -=-=-");
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
	public static Datapoint[] expSmooth(Datapoint[] data, double alpha, boolean debug) {
		if(debug) {
			Logger.log("-=-=- Simple Exponential Smoothing -=-=-");
			Logger.log("Smoothing " + data.length + " elements.");
		}
		
		Datapoint[] newData = new Datapoint[data.length];
		double coefficient = 1 - alpha;
		double sum = 0;
		
		newData[0] = data[0];
		
		for(int x = 1; x < data.length; x++) {
			sum = (alpha * data[x - 1].unit_price) + (coefficient * newData[x - 1].unit_price);
			newData[x] = new Datapoint(data[x].time, sum);
		}
		
		if(debug) Logger.log("Final array has a total number of " + newData.length + " elements");
		return newData;
	}
	
	public static double[] trendSmoothing(Datapoint[] data, double alpha, double gamma, boolean debug) {
		if(debug) {
			Logger.log("-=-=- Trend Smoothing -=-=-");
			Logger.log("Smoothing " + data.length + " elements.");
		}
		
		
		Datapoint[] simpExp = expSmooth(data, alpha, false);
		double[] newData = new double[data.length];
		double coefficient = 1 - gamma;
		double sum = 0;
		
		newData[0] = simpExp[0].unit_price;
		
		for(int x = 1; x < data.length; x++) {
			sum = gamma * (simpExp[x].unit_price - simpExp[x - 1].unit_price) + coefficient * newData[x - 1];
			
			newData[x] = sum;
		}
		
		if(debug) Logger.log("Final array has a total number of " + newData.length + " elements");
		return newData;
	}
	
	public static double[] seasonalSmoothing(Datapoint[] data, double alpha, double beta, int periods, boolean debug) {
		if(debug) {
			Logger.log("-=-=- Seasonal Smoothing -=-=-");
			Logger.log("Smoothing " + data.length + " elements.");
		}
		
		double[] newData = new double[periods];
		Datapoint[] simpExp = expSmooth(data, alpha, false);
		double coefficient = 1 - beta;
		int currPer = 1;
		
		for(int x = 1; x < data.length; x++) {
			newData[currPer] = beta * (data[x].unit_price / Math.max(simpExp[x].unit_price, 1)) + coefficient * newData[currPer];
			currPer++;
			if(currPer >= periods)
				currPer %= periods;
		}
		if(debug) Logger.log("Final array has a total number of " + newData.length + " elements");
		return newData;
	}
	
	//Holt Winters
	public static Datapoint[] generateForecast(Datapoint[] data, double alpha, double beta, double gamma, long interval, int periods, int seasonsAhead, boolean debug) {
		if(debug) {
			Logger.log("-=-=- Forecaster -=-=-");
			Logger.log("Calculating data for " + data.length + " datapoints.");
		}
		Datapoint[] smooth = expSmooth(data, alpha, false);
		double[] trend = trendSmoothing(data, alpha, gamma, false);
		double[] season = seasonalSmoothing(data, alpha, beta, periods, false);
		
		if(debug) Logger.log("Generating forecast for the next " + seasonsAhead + " seasons.");
		Datapoint[] forecast = new Datapoint[data.length];
		
		long periodLength = interval * periods;
		double sum = 0;
		
		for(int x = 0; x < data.length; x++) {
			sum = (smooth[x].unit_price + trend[x]) * season[x % periods];
			forecast[x] = new Datapoint(new Timestamp(data[x].time.getTime() + periodLength), sum);
		}
		
		if(debug) Logger.log("MSE: " + getMSE(data, forecast));
		return forecast;
	}
	
	//Optimization
	public static double getMSE(Datapoint[] ma, Datapoint[] forecast) {
		double mse = 0;
		int offset = 0;
		
		for(int x = 0; x < ma.length; x++) {
			if(ma[0].time.equals(forecast[x].time)) {
				offset = x;
				break;
			}
		}
		
		for(int x = offset; x < ma.length; x++)
			mse += Math.pow(ma[x].unit_price - forecast[x - offset].unit_price, 2);
		
		mse /= ma.length - offset;
		
		return mse;
	}
	
	public static double acceptProb(double a, double b, double c) {
		return 0;
	}
	
	public static double[] simAnnealing(Datapoint[] ma, long interval, int periods, int seasonsAhead) {
		double par[] = {0, 0, 0};
		
		double accept = 0.3; //margin for error or something
		double lim = getAverage(ma) * accept * (ma.length); //how small mse has to be or something
		
		
		return par;
	}
	
	//particle swarm
	
	//genetics
}
