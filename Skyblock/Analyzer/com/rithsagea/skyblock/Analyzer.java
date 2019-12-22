package com.rithsagea.skyblock;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
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
	public final ItemType itemType;
	
	private Datapoint[] data;
	private Datapoint[] ma;
	private Datapoint[] pd;
	
	private Object[] values;
	private Timestamp[] time;
	
	public long interval;
	public long window;
	public TimeUnit unit;
	public int daysAhead;
	
	private TimeSeriesTrace<Object> trace;
	private Calendar calendar;
	
	private int periods = 0;
	
	public Analyzer(ItemType itemType) throws SQLException {
		this(itemType, 1, 1, 24, 1, TimeUnit.HOURS);
	}
	
	public Analyzer(ItemType itemType, long interval, long window, int periods, int daysAhead, TimeUnit unit) throws SQLException {
		this.itemType = itemType;
		
		setRollingSettings(interval, window, unit);
		this.periods = periods;
		
		this.daysAhead = daysAhead;
		
		calendar = new GregorianCalendar(TimeZone.getTimeZone("EST"));
		
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
		
		start.setTime(start.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
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
		
		List<Datapoint> data = new ArrayList<Datapoint>();
		
		Timestamp now = new Timestamp(calendar.getTimeInMillis() - (5 * 60 * 60 * 1000));
		
		for(Datapoint point : pd) {
//			if(point.time.after(now)) {
				data.add(point);
//			}
		}
		
		int len = data.size();
		
		values = new Object[data.size()];
		time = new Timestamp[data.size()];
		
		for(int x = 0; x < len; x++) {
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
	
	public void updateTrace() {
		double[] pars = DataUtil.geneticsOpt(ma, 10000, 100, TimeUnit.MILLISECONDS.convert(interval, unit), periods, 2);
		
		pd = DataUtil.generateForecast(ma, pars[0], pars[1], pars[2], TimeUnit.MILLISECONDS.convert(interval, unit), periods, daysAhead, true);
		TimeSeriesTrace<Object> forecast_trace = createTrace("F_" + itemType.toString());
		trace = forecast_trace;
	}
	
	public TimeSeriesTrace<Object> getTrace() {
		return trace;
	}
	
	public void appendTrace(PlotData plot) {
		pd = ma;
		TimeSeriesTrace<Object> ma_trace = createTrace("MA_" + itemType.toString());
		plot.addTrace(ma_trace);
		
//		double[] pars = DataUtil.randWalkOpt(ma, 100000, TimeUnit.MILLISECONDS.convert(interval, unit), periods, 2);
		double[] pars = DataUtil.geneticsOpt(ma, 10000, 100, TimeUnit.MILLISECONDS.convert(interval, unit), periods, 2);
		
		pd = DataUtil.generateForecast(ma, pars[0], pars[1], pars[2], TimeUnit.MILLISECONDS.convert(interval, unit), periods, daysAhead, true);
		TimeSeriesTrace<Object> forecast_trace = createTrace("F_" + itemType.toString());
		plot.addTrace(forecast_trace);
	}
}
