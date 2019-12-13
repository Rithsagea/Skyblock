package com.rithsagea.skyblock;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.charts.dataviewer.api.data.PlotData;
import org.charts.dataviewer.api.trace.TimeSeriesTrace;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.datatypes.Datapoint;
import com.rithsagea.skyblock.api.datatypes.items.ItemType;
import com.rithsagea.skyblock.util.DataUtil;
import com.rithsagea.skyblock.util.TimeUtil;

public class Analyzer {
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

	public static DatabaseConnection db;
	private final ItemType itemType;
	
	private Datapoint[] data;
	private Datapoint[] ma;
	private Datapoint[] pd;
	
	private Object[] values;
	private Timestamp[] time;
	
	private long interval;
	private long window;
	private TimeUnit unit;
	
	private int periods = 0;
	
	public Analyzer(ItemType itemType) throws SQLException {
		this(itemType, 1, 1, 24, TimeUnit.HOURS);
	}
	
	public Analyzer(ItemType itemType, long interval, long window, int periods, TimeUnit unit) throws SQLException {
		this.itemType = itemType;
		
		setRollingSettings(interval, window, unit);
		this.periods = periods;
		
		updateData();
		loadMovingAverage();
	}
	
	//Basic stuff
	public void setRollingSettings(long interval, long window, TimeUnit unit) {
		this.interval = interval;
		this.window = window;
		this.unit = unit;
	}
	
	public void updateData() throws SQLException {
		data = resultsToData(db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"" + itemType + "\" order by end_time asc"));
	}
	
	public void loadMovingAverage() {
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;
		
		start = TimeUtil.truncateTime(start);
		end = TimeUtil.truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		pd = DataUtil.movingAverage(data, start, end, 1.5, interval, window, unit);
		cleanData();
		ma = pd;
		pd = null;
	}
	
	public void cleanData() {
		Datapoint lastPoint = new Datapoint(new Timestamp(0), 0, 0);
		lastPoint.unit_price = 0;
		for(int x = 0; x < pd.length; x++) {
			if(Double.isNaN(pd[x].unit_price) || pd[x].unit_price < 0) {
				pd[x].unit_price = lastPoint.unit_price;
			} else {
				lastPoint = pd[x];
			}
		}
	}
	
	public void writeToCSV() throws IOException {

		BufferedWriter writer = Files.newBufferedWriter(Paths.get("processed_data/" + itemType + ".csv"));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
				.withHeader("timestamp", "unit_price"));
		
		for(int x = 0; x < pd.length; x++) {
			printer.printRecord(pd[x].time, pd[x].unit_price);
		}
		
		printer.flush();
		printer.close();
	}
	
	public void loadData() {
		values = new Object[pd.length];
		time = new Timestamp[pd.length];
		
		for(int x = 0; x < pd.length; x++) {
			values[x] = pd[x].unit_price;
			time[x] = pd[x].time;
		}
	}
	
	public TimeSeriesTrace<Object> createTrace(String name) {
		loadData();
		TimeSeriesTrace<Object> trace = new TimeSeriesTrace<>(name);
		trace.setxArray(time);
		trace.setyArray(values);
		
		return trace;
	}
	
	public void appendTrace(PlotData plot) {
		pd = ma;
		TimeSeriesTrace<Object> ma_trace = createTrace("MA_" + itemType.toString());
		plot.addTrace(ma_trace);
		
		pd = DataUtil.generateForecast(ma, 0.6, 0.6, 0.9, TimeUnit.MILLISECONDS.convert(interval, unit), periods, 2, true);
		TimeSeriesTrace<Object> forecast_trace = createTrace("F_" + itemType.toString());
		plot.addTrace(forecast_trace);
	}
}
