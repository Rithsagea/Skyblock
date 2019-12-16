package com.rithsagea.skyblock;

import java.awt.Container;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;

import org.charts.dataviewer.DataViewer;
import org.charts.dataviewer.api.config.DataViewerConfiguration;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.datatypes.items.ItemType;
import com.rithsagea.skyblock.graphics.AnalyzerPanel;

public class AnalyzerWindow extends JFrame {
	
	private static final long serialVersionUID = -2765575597712416061L;

	private final DatabaseConnection db;
	
	private final DataViewer dataviewer;
	
	private JLabel itemSettingsLabel;
	private JLabel timeSettingsLabel;
	
	private JLabel itemTypeLabel;
	private JLabel intervalLabel;
	private JLabel windowLabel;
	private JLabel timeUnitLabel;
	private JLabel dayLabel;
	
	private JComboBox<ItemType> itemTypeComboBox;
	private JTextField intervalTextField;
	private JTextField windowTextField;
	private JComboBox<TimeUnit> timeUnitComboBox;
	private JTextField dayTextField;
	
	private JButton addButton;
	private JButton graphButton;
	
	private AnalyzerPanel itemList;
	
	public AnalyzerWindow() {
		super("Rithsagea's Skyblock Auction Analyzer");
		
		//Components
		itemSettingsLabel = new JLabel("Item Settings", SwingConstants.CENTER);
		timeSettingsLabel = new JLabel("Time Settings", SwingConstants.CENTER);
		
		itemTypeLabel = new JLabel("Item Type: ");
		intervalLabel = new JLabel("Interval: ");
		windowLabel = new JLabel("Window: ");
		timeUnitLabel = new JLabel("Time Unit: ");
		dayLabel = new JLabel("Days Ahead: ");
		
		itemTypeComboBox = new JComboBox<ItemType>(ItemType.values());
		intervalTextField = new JTextField();
		windowTextField = new JTextField();
		timeUnitComboBox = new JComboBox<TimeUnit>(TimeUnit.values());
		dayTextField = new JTextField();
		
		addButton = new JButton("Add Item");
		graphButton = new JButton("Graph Items");
		
		itemList = new AnalyzerPanel();
		
		//Layout
		Container contentPanel = getContentPane();
		GroupLayout layout = new GroupLayout(contentPanel);
		contentPanel.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.CENTER)	//panel for settings and buttons
							.addComponent(itemSettingsLabel)
							.addGroup(layout.createSequentialGroup()
									.addComponent(itemTypeLabel)
									.addComponent(itemTypeComboBox))
							.addComponent(timeSettingsLabel)
							.addGroup(layout.createSequentialGroup()
									.addComponent(intervalLabel)
									.addComponent(intervalTextField))
							.addGroup(layout.createSequentialGroup()
									.addComponent(windowLabel)
									.addComponent(windowTextField))
							.addGroup(layout.createSequentialGroup()
									.addComponent(timeUnitLabel)
									.addComponent(timeUnitComboBox))
							.addGroup(layout.createSequentialGroup()
									.addComponent(dayLabel)
									.addComponent(dayTextField))
							.addGroup(layout.createSequentialGroup()
									.addComponent(addButton)
									.addComponent(graphButton)))
					.addGroup(layout.createSequentialGroup()	//panel with all active analyzers
							.addComponent(itemList)));
		
		layout.setVerticalGroup(
				layout.createParallelGroup()
					//Right Column
					.addGroup(layout.createSequentialGroup()
							.addComponent(itemSettingsLabel)
							.addGroup(layout.createParallelGroup()
									.addComponent(itemTypeLabel)
									.addComponent(itemTypeComboBox))
							.addComponent(timeSettingsLabel)
							.addGroup(layout.createParallelGroup()
									.addComponent(intervalLabel)
									.addComponent(intervalTextField))
							.addGroup(layout.createParallelGroup()
									.addComponent(windowLabel)
									.addComponent(windowTextField))
							.addGroup(layout.createParallelGroup()
									.addComponent(timeUnitLabel)
									.addComponent(timeUnitComboBox))
							.addGroup(layout.createParallelGroup()
									.addComponent(dayLabel)
									.addComponent(dayTextField))
							.addGroup(layout.createParallelGroup()
									.addComponent(addButton)
									.addComponent(graphButton)))
					.addComponent(itemList));	//Left Column
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		
		db = new DatabaseConnection();
		dataviewer = new DataViewer("analyzer");
		
		Analyzer.db = db;
		initDataviewer();
	}
	
	public void initDataviewer() {
		DataViewerConfiguration config = new DataViewerConfiguration();
		config.setPlotTitle("Moving Averages");
		config.setxAxisTitle("Time");
		config.setyAxisTitle("Unit Price");
		config.setMarginBottom(250);
		config.showLegend(true);
		config.setLegendInsidePlot(false);
		dataviewer.updateConfiguration(config);
	}
	
	public void addAnalyzer(Analyzer analyzer) {
		itemList.addAnalyzer(analyzer);
	}
	
	public void addAnalyzer(ItemType item, long interval, long window, int period, TimeUnit unit) throws SQLException {
		addAnalyzer(new Analyzer(item, interval, window, period, unit));
	}
	
	public void addAnalyzer(ItemType item) throws SQLException {
		addAnalyzer(item, 1, 1, 24, TimeUnit.HOURS);
	}
	
	public void addItem(ItemType item) throws SQLException {
		addAnalyzer(item);
	}
	
	public void addItems(ItemType...itemTypes) throws SQLException {
		for(ItemType item : itemTypes) {
			addAnalyzer(item);
		}
	}
	
	public void graphData() throws SQLException {
		itemList.calculateTraces();
		dataviewer.updatePlot(itemList.getPlotData());
	}
	
	public void printItems() throws SQLException {
		List<String> itemTypes = db.getItemTypes();
		Collections.sort(itemTypes);
		for(String str : itemTypes) {
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) throws SQLException, IOException, InterruptedException {
		AnalyzerWindow window = new AnalyzerWindow();
		
//		ItemType[] items = new ItemType[] {  
//			ItemType.STRONG_DRAGON_HELMET,
//			ItemType.STRONG_DRAGON_CHESTPLATE,
//			ItemType.STRONG_DRAGON_LEGGINGS,
//			ItemType.STRONG_DRAGON_BOOTS
//			
////			ItemType.STRONG_FRAGMENT
//		};
		
//		window.addItems(items);
//		window.graphData();
		
		//URL Here:
		//http://localhost:8090/view/analyzer
		
		Thread.currentThread().join();
	}
}
