package com.rithsagea.skyblock.task;

import com.rithsagea.skyblock.Downloader;
import com.rithsagea.skyblock.api.DatabaseConnection;
import com.rithsagea.skyblock.api.Logger;

public class TransferTask extends BaseTask {

	public TransferTask(DatabaseConnection db, Downloader downloader) {
		super(db, downloader);
	}

	private int transfers = 0;
	
	@Override
	public void run() {
		transfers++;
		Logger.log("-=-=- Transfer " + transfers + " -=-=-");
		Logger.log("Attempting to transfer data");
		db.databaseTransfer();
		Logger.log("Transfer finished");
	}
}
