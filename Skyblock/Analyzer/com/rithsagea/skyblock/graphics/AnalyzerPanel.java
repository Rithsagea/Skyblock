package com.rithsagea.skyblock.graphics;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.charts.dataviewer.api.data.PlotData;

import com.rithsagea.skyblock.Analyzer;

public class AnalyzerPanel extends JScrollPane {
	
	private static final long serialVersionUID = 1560570364551331910L;
	private List<Analyzer> analyzers = new ArrayList<Analyzer>();
	private PlotData maData;
	
	public AnalyzerPanel() {
		maData = new PlotData();
		JTextArea content = new JTextArea("Content yay");
		add(content);
	}
	
	public void addAnalyzer(Analyzer analyzer) {
		analyzers.add(analyzer);
	}
	
	public void updateAnalyzers() {
		for(Analyzer analyzer : analyzers) {
			analyzer.updateTrace();
		}
	}
	
	public void calculateTraces() {
		maData = new PlotData();
		for(Analyzer analyzer : analyzers) {
			maData.addTrace(analyzer.getTrace());
		}
	}
	
	public PlotData getPlotData() {
		return maData;
	}
}
