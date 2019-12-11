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

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.datatypes.Datapoint;
import com.rithsagea.skyblock.api.datatypes.ItemType;
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
	
	private final DatabaseConnection db;
	private final ItemType itemType;
	
	private Datapoint[] data;
	private Datapoint[] pd;
	
	private long interval;
	private long window;
	private TimeUnit unit;
	
	public Analyzer(ItemType itemType, DatabaseConnection db) throws SQLException {
		this.itemType = itemType;
		this.db = db;
		
		interval = 5;
		window = 60;
		unit = TimeUnit.MINUTES;
		
		updateData();
	}
	
	public void setRollingSettings(long interval, long window, TimeUnit unit) {
		this.interval = interval;
		this.window = window;
		this.unit = unit;
	}
	public void updateData() throws SQLException {
		data = resultsToData(db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"" + itemType + "\" order by end_time asc"));
		processData();
	}
	public void processData() {
		Timestamp start = data[0].time;
		Timestamp end = data[data.length - 1].time;
		
		start = TimeUtil.truncateTime(start);
		end = TimeUtil.truncateTime(end);
		
		end.setTime(end.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		pd = null;
		pd = DataUtil.rollingAverage(data, start, end, 1.5, window, interval, unit);
	}
	public void writeToCSV(String path) throws IOException {

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
				.withHeader("timestamp", "unit_price"));
		
		for(int x = 0; x < pd.length; x++) {
			printer.printRecord(pd[x].time, pd[x].unit_price);
		}
		
		printer.flush();
		printer.close();
	}
	public double[] getProcessedValues() {
		double[] values = new double[pd.length];
		for(int x = 0; x < pd.length; x++) {
			values[x] = pd[x].unit_price;
		}
		return values;
	}
}
