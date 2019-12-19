package com.rithsagea.skyblock.graphics;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.charts.dataviewer.api.data.PlotData;

import com.rithsagea.skyblock.Analyzer;

public class AnalyzerPanel extends JScrollPane {
	
	private static final long serialVersionUID = 1560570364551331910L;
	private List<Analyzer> analyzers = new ArrayList<Analyzer>();
	private PlotData plotData;
	private JTable table;
	
	public AnalyzerPanel() {
		plotData = new PlotData();
		table = new JTable(0, 5);
		
		@SuppressWarnings("serial")
		DefaultTableModel tableModel = new DefaultTableModel() {
			@Override 
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		
		tableModel.addColumn("Item Type");
		tableModel.addColumn("Interval");
		tableModel.addColumn("Window");
		tableModel.addColumn("Time Unit");
		tableModel.addColumn("Forecast");
		
		table.setModel(tableModel);
		
		setViewportView(table);
	}
	
	public void addAnalyzer(Analyzer analyzer) {
		analyzers.add(analyzer);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		
		model.addRow(new String[] {
				analyzer.itemType.toString(),
				String.valueOf(analyzer.interval),
				String.valueOf(analyzer.window),
				analyzer.unit.toString(),
				String.valueOf(analyzer.daysAhead)
		});
	}
	
	public void updateAnalyzers() {
		for(Analyzer analyzer : analyzers) {
			analyzer.updateTrace();
			plotData.addTrace(analyzer.getTrace());
		}
	}
	
	public void calculateTraces() {
		plotData = new PlotData();
		for(Analyzer analyzer : analyzers) {
			analyzer.appendTrace(plotData);
//			maData.addTrace(analyzer.getTrace());
		}
	}
	
	public PlotData getPlotData() {
		return plotData;
	}
}
