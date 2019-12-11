package com.rithsagea.skyblock;

import java.io.IOException;
import java.sql.SQLException;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.datatypes.Datapoint;
import com.rithsagea.skyblock.api.datatypes.ItemType;

public class AnalyzerWindow {
	
	private static final DatabaseConnection db = new DatabaseConnection();
	
	public static void printData(Datapoint[] data) {
		int row_count = data.length;
		
		for(int x = 0; x < row_count; x++) {
			System.out.println(data[x]);
		}
	}
	
	public static void main(String[] args) throws SQLException, IOException {
		
		ItemType type = ItemType.WISE_FRAGMENT;
		
		//Get Data
		Analyzer analyzer = new Analyzer(type, db);
		analyzer.writeToCSV("" + type + ".csv");
		
		double values[] = analyzer.getProcessedValues();
		double index[] = new double[values.length];
		for(int x = 0; x < index.length; index[x] = x, values[x] = Double.isNaN(values[x]) ? 0 : values[x], x++);
		
		//Graph Rolling Average
	}
}
