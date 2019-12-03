package com.rithsagea.skyblock.task;

import java.util.List;

import com.rithsagea.skyblock.Downloader;
import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;
import com.rithsagea.skyblock.api.data.Auction;

public class DownloadTask extends BaseTask {
	
	public DownloadTask(DatabaseConnection db, Downloader downloader) {
		super(db, downloader);
	}

	private List<Auction> auctions;
	private int runs = 0;
	
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
			Logger.log("Finished writing values to database");
			auctions.clear();
		} catch(Exception e) {
			Logger.log("Error: " + e.getMessage());
		}
	}
}
