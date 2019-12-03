package com.rithsagea.skyblock.downloader;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.rithsagea.skyblock.api.data.Auction;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.skyblock.SkyBlockAuctionsReply;

public class Downloader {
	
	private final HypixelAPI API;
	private final Gson gson = new Gson();
	
	private long endingTime = 2 * 60 * 1000;
	public static Timestamp latestTime;
	
	public Downloader(UUID apiKey) {
		API = new HypixelAPI(apiKey);
	}
	
	/**
	 * Gets a page of an auction from hypixel
	 * @param page	the page number to get
	 * @return		the reply from hypixel's api
	 */
	public SkyBlockAuctionsReply getAuctionPage(int page) {
		SkyBlockAuctionsReply reply = null;
		try {
			reply = API.getSkyBlockAuctions(page).get();
			latestTime = new Timestamp(reply.getLastUpdated() - endingTime * 5);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reply;
	}
	
	/**
	 * Gets an array of all auctions ending in under endingTime
	 * @return		arraylist of auctions
	 */
	public List<Auction> getAuctions() {
		List<Auction> auctions = new ArrayList<Auction>();
		List<SkyBlockAuctionsReply> replies = new ArrayList<SkyBlockAuctionsReply>();
		SkyBlockAuctionsReply reply = null;
		int page = 0;
		//Downloads auctions
		do {
			reply = getAuctionPage(page); 
			replies.add(reply);
			page++;
		} while(reply.hasNextPage());
		//Reads in auctions as individual objects
		for(SkyBlockAuctionsReply auctionPage : replies) {
			long timeLimit = auctionPage.getLastUpdated() + endingTime;
			for(JsonElement element : auctionPage.getAuctions().getAsJsonArray()) {
				AuctionFormat auction = gson.fromJson(element, AuctionFormat.class);
				if(auction.end < timeLimit && auction.highest_bid_amount > 0) {
					auctions.add(auction.createAuction());
				}
			}
		}
		
		return auctions;
	}
}
