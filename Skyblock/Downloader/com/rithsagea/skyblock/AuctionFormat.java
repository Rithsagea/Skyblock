package com.rithsagea.skyblock;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

import com.rithsagea.skyblock.api.datatypes.Auction;
import com.rithsagea.skyblock.api.datatypes.ItemData;
import com.rithsagea.skyblock.util.NBTUtil;

public class AuctionFormat {
	public String uuid;
	public long start;
	public long end;
	public String item_name;
	public String item_lore;
	public String item_bytes;
	public double highest_bid_amount;
	
	public Auction createAuction() {
		Auction auction = new Auction();
		
		//uuid - fun
		BigInteger mostSig = new BigInteger(uuid.substring(0, 16), 16);
		BigInteger leastSig = new BigInteger(uuid.substring(16, 32), 16);
		auction.id = new UUID(mostSig.longValue(), leastSig.longValue());
		
		//item bytes
		ItemData data = NBTUtil.getData(item_bytes);
		auction.item_type = data.itemType;
		auction.modifier = data.modifier;
		auction.amount = data.count;
		
		auction.enchants = NBTUtil.enchantsToString(data.enchants);
		
		//normal information
		auction.start_time = new Timestamp(start);
		auction.end_time = new Timestamp(end);
		auction.price = highest_bid_amount;
		
		return auction;
	}
}
