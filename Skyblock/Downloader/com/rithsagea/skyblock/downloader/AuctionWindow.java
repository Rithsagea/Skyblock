package com.rithsagea.skyblock.downloader;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.LogQueue;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.SecureConstants;
import com.rithsagea.skyblock.api.data.Auction;

public class AuctionWindow {
	
	public static final Downloader downloader = new Downloader(UUID.fromString(SecureConstants.key));
	public static final DatabaseConnection db = new DatabaseConnection();
	public static final Timer timer = new Timer();
	
	public static final LogQueue log = new LogQueue();
	public static final JTextArea logInfo = new JTextArea(1, 1);
	
	public static void main(String[] args) {
		Logger.addListener(log);
		
		JFrame frame = new JFrame();
		frame.setVisible(true);
		frame.setLayout(null);
		frame.setSize(500, 500);
		
		logInfo.setBounds(10, 10, 480, 460);
		logInfo.setEditable(false);
		frame.add(logInfo);
		//Download
		timer.scheduleAtFixedRate(new TimerTask() {
			
			List<Auction> auctions;
			int runs = 0;
			
			@Override
			public void run() {
				runs++;
				Logger.log("-=-=- Run " + runs + " -=-=-");
				
				Logger.log("Downloading auctions");
				try {
					auctions = downloader.getAuctions();
					Logger.log("Succesfully downloaded auctions");
					Logger.log("Writing values into database");
					db.writeEntries(auctions, "skyblock.auction_running");
					Logger.log("Finished writing values to database\n\n\n");
					auctions.clear();
				} catch(Exception e) {
					Logger.log("Error: " + e.getMessage());
				}
			}
		}, 0, 1000 * 60);
		//Transfer
		timer.scheduleAtFixedRate(new TimerTask() {
			
			int transfers = 0;
			
			@Override
			public void run() {
				transfers++;
				Logger.log("\n\n\n -=-=- Transfer " + transfers + " -=-=-");
				Logger.log("Attempting to transfer data");
				db.databaseTransfer();
				Logger.log("Transfer finished");
			}
		}, 500, 1000 * 60 * 5);
		//Log
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				logInfo.setText(log.getText());
			}
		}, 0, 1000);
	}
}
