package com.rithsagea.skyblock;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.services.sheets.v4.Sheets;
import com.rithsagea.skyblock.util.SheetsUtil;

public class Uploader {
	
	private static Sheets sheetsService;
	private static String SPREADSHEET_ID = "1X60Ud5yDbtG_IgH34DBTdXt5Mjes34-XdoeMIEtaUOo";
	
	public static void main(String[] args) throws GeneralSecurityException, IOException {
		sheetsService = SheetsUtil.getSheetsService();
	}
}
