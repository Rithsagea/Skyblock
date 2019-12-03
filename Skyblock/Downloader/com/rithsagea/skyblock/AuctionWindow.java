package com.rithsagea.skyblock;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.SecureConstants;
import com.rithsagea.skyblock.api.data.DroppingQueue;
import com.rithsagea.skyblock.task.DownloadTask;
import com.rithsagea.skyblock.task.TransferTask;

public class AuctionWindow {
	
	private static final Downloader downloader = new Downloader(UUID.fromString(SecureConstants.key));
	private static final DatabaseConnection db = new DatabaseConnection();
	private static final Timer timer = new Timer();
	
	private static final DroppingQueue<String> log = new DroppingQueue<String>(25);
	private static final JTextArea logInfo = new JTextArea(1, 1);
	
	public static void main(String[] args) {
		Logger.addListener(log);
		
		JFrame frame = new JFrame();
		frame.setVisible(true);
		frame.setLayout(null);
		frame.setSize(500, 500);
		
		logInfo.setBounds(10, 10, 480, 460);
		logInfo.setEditable(false);
		frame.add(logInfo);
		
		timer.scheduleAtFixedRate(new DownloadTask(db, downloader), 0, 1000 * 60);
		timer.scheduleAtFixedRate(new TransferTask(db, downloader), 500, 1000 * 60 * 5);
		
		//Log
		timer.scheduleAtFixedRate(new TimerTask() {
			
			StringBuilder builder = new StringBuilder();
			
			@Override
			public void run() {
				builder.setLength(0);
				log.iterator().forEachRemaining(builder::append);
				logInfo.setText(builder.toString());
			}
		}, 0, 1000);
	}
}
