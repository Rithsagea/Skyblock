package com.rithsagea.skyblock;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.NumberFormatter;

import org.charts.dataviewer.DataViewer;
import org.charts.dataviewer.api.config.DataViewerConfiguration;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.datatypes.DroppingQueue;
import com.rithsagea.skyblock.api.datatypes.items.ItemType;
import com.rithsagea.skyblock.graphics.AnalyzerPanel;

public class AnalyzerWindow extends JFrame {
	
	private static final long serialVersionUID = -2765575597712416061L;
	private static final int logLength = 14;
	
	private final DatabaseConnection db;
	
	private final DataViewer dataviewer;
	private final DroppingQueue<String> logQueue;
	private final Timer timer;
	
	private JLabel itemSettingsLabel;
	private JLabel timeSettingsLabel;
	
	private JLabel itemTypeLabel;
	private JLabel intervalLabel;
	private JLabel windowLabel;
	private JLabel timeUnitLabel;
	private JLabel dayLabel;
	
	private JComboBox<ItemType> itemTypeComboBox;
	private JFormattedTextField intervalTextField;
	private JFormattedTextField windowTextField;
	private JComboBox<TimeUnit> timeUnitComboBox;
	private JFormattedTextField dayTextField;
	
	private JButton addButton;
	private JButton graphButton;
	
	private AnalyzerPanel itemList;
	private JTextArea logTextArea;
	
	public AnalyzerWindow() {
		super("Rithsagea's Skyblock Auction Analyzer");
		
		//Logger
		logQueue = new DroppingQueue<String>(logLength);
		Logger.addListener(logQueue);
		
		//Timer
		timer = new Timer();
		
		//Initialize functional stuff
		db = new DatabaseConnection();
		dataviewer = new DataViewer("analyzer");
		
		Analyzer.db = db;
		initDataviewer();
		
		//Formatters
		NumberFormat format = NumberFormat.getIntegerInstance();
		
		NumberFormatter longFormatter = new NumberFormatter(format);
		
		longFormatter.setValueClass(Long.class);
		longFormatter.setAllowsInvalid(false);
		longFormatter.setMinimum(0l);
		
		NumberFormatter intFormatter = new NumberFormatter(format);
		
		intFormatter.setValueClass(Integer.class);
		intFormatter.setAllowsInvalid(false);
		intFormatter.setMinimum(0);
		
		//Components
		itemSettingsLabel = new JLabel("Item Settings", SwingConstants.CENTER);
		timeSettingsLabel = new JLabel("Time Settings", SwingConstants.CENTER);
		
		itemTypeLabel = new JLabel("Item Type: ");
		intervalLabel = new JLabel("Interval: ");
		windowLabel = new JLabel("Window: ");
		timeUnitLabel = new JLabel("Time Unit: ");
		dayLabel = new JLabel("Days Ahead: ");
		
		itemTypeComboBox = new JComboBox<ItemType>(ItemType.values());
		intervalTextField = new JFormattedTextField(longFormatter);
		windowTextField = new JFormattedTextField(longFormatter);
		timeUnitComboBox = new JComboBox<TimeUnit>(TimeUnit.values());
		dayTextField = new JFormattedTextField(intFormatter);
		
		addButton = new JButton("Add Item");
		graphButton = new JButton("Graph Items");
		
		itemList = new AnalyzerPanel();
		//find a better way to deal with the initial layout
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		
		initLayout();
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		
		initListeners();
		
		setVisible(true);		
	}
	
	public void initLayout() {
		Container contentPanel = getContentPane();
		GroupLayout layout = new GroupLayout(contentPanel);
		contentPanel.setLayout(layout);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
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
									.addComponent(graphButton))) //panel with all active analyzers
					.addComponent(itemList))
				.addComponent(logTextArea));
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup()
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
							.addComponent(itemList)) //list of analyzers
					.addComponent(logTextArea)); //the log
	}
	
	public void initListeners() {
		addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(intervalTextField.getText().isEmpty()) {
					Logger.log("The [Interval] text field is empty!");
					return;
				}
				if(windowTextField.getText().isEmpty()) {
					Logger.log("The [Window] text field is empty!");
					return;
				}
				if(dayTextField.getText().isEmpty()) {
					Logger.log("The [Days Ahead] text field is empty!");
					return;
				}
				
				ItemType type = (ItemType) itemTypeComboBox.getSelectedItem();
				long interval = Long.parseLong(intervalTextField.getText());
				long window = Long.parseLong(windowTextField.getText());
				TimeUnit unit = (TimeUnit) timeUnitComboBox.getSelectedItem();
				int daysAhead = Integer.parseInt(dayTextField.getText());
				
				try {
					addAnalyzer(type, interval, window, (int) (unit.convert(1, TimeUnit.DAYS) / interval), daysAhead, unit);
				} catch (SQLException e1) {
					Logger.log("Error with SQL, please try again");
				}
			}
			
		});
		
		graphButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				itemList.updateAnalyzers();
			}
		});
		
		timer.scheduleAtFixedRate(new TimerTask() {
			
			StringBuilder builder = new StringBuilder();
			
			@Override
			public void run() {
				
				builder.setLength(0);
				logQueue.iterator().forEachRemaining(builder::append);
				logTextArea.setText(builder.toString());
			}
			
		}, 10000, 1000);
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
	
	public void addAnalyzer(ItemType item, long interval, long window, int period, int daysAhead, TimeUnit unit) throws SQLException {
		addAnalyzer(new Analyzer(item, interval, window, period, daysAhead, unit));
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
		@SuppressWarnings("unused")
		AnalyzerWindow window = new AnalyzerWindow();
		
		//URL Here:
		//http://localhost:8090/view/analyzer
		
		Thread.currentThread().join();
	}
}
