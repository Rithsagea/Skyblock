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
import org.charts.dataviewer.api.trace.TimeSeriesTrace;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.datatypes.AnalyzeType;
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
	private Datapoint[] pd;
	
	private long interval;
	private long window;
	private TimeUnit unit;
	
	public Analyzer(ItemType itemType) throws SQLException {
		this(itemType, 1, 1, TimeUnit.HOURS);
	}
	
	public Analyzer(ItemType itemType, long interval, long window, TimeUnit unit) throws SQLException {
		this.itemType = itemType;
		this.interval = interval;
		this.window = window;
		this.unit = unit;
		
		updateData();
	}
	
	//Basic stuff
	public void setRollingSettings(long interval, long window, TimeUnit unit) {
		this.interval = interval;
		this.window = window;
		this.unit = unit;
	}
	
	public void updateData() throws SQLException {
		data = resultsToData(db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"" + itemType + "\" order by end_time asc"));
//		processData(AnalyzeType.MA);
//		cleanData();
	}
	
	public void processData(AnalyzeType type) {
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;
		
		start = TimeUtil.truncateTime(start);
		end = TimeUtil.truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		pd = null;
		
		switch(type) {
		case MA:
			pd = DataUtil.movingAverage(data, start, end, interval, unit);
			break;
		case MAW:
			pd = DataUtil.movingAverage(data, start, end, 1.5, window, interval, unit);
			break;
		case EXP_SIMP:
			processData(AnalyzeType.MAW);
			pd = DataUtil.expSmooth(pd, 0.8);
		}
		
		cleanData();
	}
	
	public void cleanData() {
		Datapoint lastPoint = new Datapoint(new Timestamp(0), 0, 0);
		lastPoint.unit_price = 0;
		for(int x = 0; x < pd.length; x++) {
			if(Double.isNaN(pd[x].value) || pd[x].value <= 0) {
				pd[x].value = lastPoint.value;
				pd[x].amount = lastPoint.amount;
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
	
	public TimeSeriesTrace<Object> getTSTrace(AnalyzeType type) {
		processData(type);
		Object[] values = new Object[pd.length];
		Timestamp[] times = new Timestamp[pd.length];
		for(int x = 0; x < pd.length; x++) {
			values[x] = pd[x].unit_price;
			times[x] = pd[x].time;
		}
		
		TimeSeriesTrace<Object> ts = new TimeSeriesTrace<>();
		ts.setTraceName(type.toString() + "_" + itemType.toString());
		
		ts.setxArray(times);
		ts.setyArray(values);
		
		return ts;
	}
	
	//Holt Winters
	
}
