package com.rithsagea.skyblock.api;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.rithsagea.skyblock.api.data.Auction;
import com.rithsagea.skyblock.downloader.Downloader;


public class DatabaseConnection {
	
	private MysqlDataSource ds;
	private ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
	
	public DatabaseConnection() {
		ds = new MysqlDataSource();
		ds.setURL(SecureConstants.databaseLink);
		ds.setUser(SecureConstants.user);
		ds.setPassword(SecureConstants.password);
		ds.setDatabaseName("skyblock");
		
		try {
			Connection con = ds.getConnection();
			con.close();
			Logger.log("Database succesfully connected");
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log("Could not connect to database");
			System.exit(0);
		}
	}
	
	private void runAction(Auction auction, PreparedStatement ps) throws SQLException {
		//UUID
		buffer.clear();
		buffer.putLong(auction.id.getMostSignificantBits());
		buffer.putLong(auction.id.getLeastSignificantBits());
		ps.setBinaryStream(1, new ByteArrayInputStream(buffer.array()), 32);
		
		ps.setString(2, auction.item_type);	//Item Type
		ps.setString(3, auction.modifier);	//Modifier
		ps.setString(4, auction.enchants);	//Enchantments
		ps.setByte(5, auction.amount); 		//Amount
		
		ps.setTimestamp(6, auction.start_time);
		ps.setTimestamp(7, auction.end_time);
		ps.setDouble(8, auction.price);		//Price
		ps.execute();
	}
	
	public void writeEntries(List<Auction> auctions, String tableName) {
		
		Connection con;
		PreparedStatement ps;
		
		try {
			con = ds.getConnection();
			ps = con.prepareStatement("REPLACE INTO " + tableName
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			
			for(Auction auction : auctions) {
				runAction(auction, ps);
			}
			
			ps.close();
			
			if(con != null) con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void databaseTransfer() {
		Connection con;
		PreparedStatement stat;
		try {
			//TODO Optimize the hell out of this
			con = ds.getConnection();
			stat = con.prepareStatement("insert into skyblock.auction_data " + 
					"select * from skyblock.auction_running " + 
					"where end_time < ?");
			stat.setTimestamp(1, Downloader.latestTime);
			stat.execute();
			stat = con.prepareStatement("delete from skyblock.auction_running " + 
					"where end_time < ?");
			stat.setTimestamp(1, Downloader.latestTime);
			stat.execute();
			stat.setTimestamp(1, Downloader.latestTime);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
