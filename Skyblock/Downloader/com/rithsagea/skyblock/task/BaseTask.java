package com.rithsagea.skyblock.task;

import java.util.TimerTask;

import com.rithsagea.skyblock.Downloader;
import com.rithsagea.skyblock.api.DatabaseConnection;

public abstract class BaseTask extends TimerTask {
	
	protected final DatabaseConnection db;
	protected final Downloader downloader;
	
	public BaseTask(DatabaseConnection db, Downloader downloader) {
		this.db = db;
		this.downloader = downloader;
	}
}
