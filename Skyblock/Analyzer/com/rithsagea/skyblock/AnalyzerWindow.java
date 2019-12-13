package com.rithsagea.skyblock;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.charts.dataviewer.DataViewer;
import org.charts.dataviewer.api.config.DataViewerConfiguration;
import org.charts.dataviewer.api.data.PlotData;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.datatypes.items.DragonEquipment;
import com.rithsagea.skyblock.api.datatypes.items.ItemType;

public class AnalyzerWindow {
	
	private static final DatabaseConnection db = new DatabaseConnection();
	
	private static final DataViewer ma = new DataViewer("analyzer");
	private static final PlotData maData = new PlotData();
	
	public static void initDataviewer() {
		DataViewerConfiguration config = new DataViewerConfiguration();
		config.setPlotTitle("Moving Averages");
		config.setxAxisTitle("Time");
		config.setyAxisTitle("Unit Price");
		config.setMarginBottom(250);
		config.showLegend(true);
		config.setLegendInsidePlot(false);
		ma.updateConfiguration(config);
	}
	
	public static void printItems() throws SQLException {
		List<String> itemTypes = db.getItemTypes();
		Collections.sort(itemTypes);
		for(String str : itemTypes) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		
		Analyzer.db = db;
		List<Analyzer> analyzers = new ArrayList<Analyzer>();
		
		ItemType[] items = new ItemType[] {  
			DragonEquipment.STRONG_FRAGMENT
		};
		
		//Get Data
		for(ItemType type : items) {
			analyzers.add(new Analyzer(type, 5, 120, 12 * 24, TimeUnit.MINUTES));
		}
		
		//Graph Rolling Average
		initDataviewer();
		
		for(Analyzer analyzer : analyzers) {
//			analyzer.writeToCSV();
			analyzer.appendTrace(maData);
		}
		
		ma.updatePlot(maData);
		Logger.log("Analysis finished");
		//URL Here:
		//http://localhost:8090/view/analyzer
		
		Thread.currentThread().join();
	}
}
