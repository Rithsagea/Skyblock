package com.rithsagea.skyblock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.data.Datapoint;

public class AnalyzerWindow {
	
	private static final DatabaseConnection db = new DatabaseConnection();
	
	public static void main(String[] args) throws SQLException {
		ResultSet auctions = db.runQuery("select end_time, price, amount from skyblock.auction_data where item_type = \"WISE_FRAGMENT\"");
		List<Datapoint> data = new ArrayList<Datapoint>();
		
		while(auctions.next()) {
			data.add(new Datapoint(auctions.getTimestamp(1), auctions.getDouble(2) / auctions.getShort(3)));
		}
		
		Analyzer analyzer = new Analyzer(data);
		analyzer.printData();
	}
}
