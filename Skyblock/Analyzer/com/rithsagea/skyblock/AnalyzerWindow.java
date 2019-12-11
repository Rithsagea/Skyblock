package com.rithsagea.skyblock;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.charts.dataviewer.DataViewer;
import org.charts.dataviewer.api.config.DataViewerConfiguration;
import org.charts.dataviewer.api.data.PlotData;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.datatypes.AnalyzeType;
import com.rithsagea.skyblock.api.datatypes.Datapoint;
import com.rithsagea.skyblock.api.datatypes.ItemType;

public class AnalyzerWindow {
	
	private static final DatabaseConnection db = new DatabaseConnection();
	
	private static final DataViewer ma = new DataViewer("analyzer");
	private static final PlotData maData = new PlotData();
	
	public static void printData(Datapoint[] data) {
		int row_count = data.length;
		
		for(int x = 0; x < row_count; x++) {
			System.out.println(data[x]);
		}
	}
	
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
	
	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		
		Analyzer.db = db;
		List<Analyzer> analyzers = new ArrayList<Analyzer>();
		
		ItemType[] items = new ItemType[] {  
			ItemType.SUMMONING_EYE
		};
		
		//Get Data
		for(ItemType type : items) {
			analyzers.add(new Analyzer(type));
		}
		
		//Graph Rolling Average
		initDataviewer();
		
		for(Analyzer analyzer : analyzers) {
//			analyzer.writeToCSV();
			maData.addTrace(analyzer.getTSTrace(AnalyzeType.MA));
			maData.addTrace(analyzer.getTSTrace(AnalyzeType.MAW));
		}
		
		ma.updatePlot(maData);
		Logger.log("Analysis finished");
		
		List<String> itemTypes = db.getItemTypes();
		for(String str : itemTypes) {
			System.out.println(str);
		}
		
		Thread.currentThread().join();
	}
}
