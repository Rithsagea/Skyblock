package com.rithsagea.skyblock;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.rithsagea.skyblock.util.SheetsUtil;

public class Uploader {
	
//	private static DatabaseConnection db_con = new DatabaseConnection();
	
	private static Sheets service;
	private static String SPREADSHEET_ID = "1X60Ud5yDbtG_IgH34DBTdXt5Mjes34-XdoeMIEtaUOo";
	
	public static void writeDouble(double val, String cell) throws IOException {
		ValueRange body = new ValueRange()
				.setValues(Arrays.asList(
						Arrays.asList(String.valueOf(val))));
		UpdateValuesResponse result = service.spreadsheets().values()
				.update(SPREADSHEET_ID, cell, body)
				.setValueInputOption("RAW")
				.execute();
	}
	
	public static void main(String[] args) throws GeneralSecurityException, IOException {
		service = SheetsUtil.getSheetsService();
		ValueRange response = service.spreadsheets().values()
				.get(SPREADSHEET_ID, "Prices!A1:E17")
				.execute();
		
		List<List<Object>> values = response.getValues();
		
		for(List row : values) {
			System.out.println(Arrays.toString(row.toArray()));
		}
	}
}
