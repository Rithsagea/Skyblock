package com.rithsagea.skyblock.api.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import com.github.mryurihi.tbnbt.stream.NBTInputStream;
import com.github.mryurihi.tbnbt.tag.NBTTagCompound;

public class NBTUtil {
	
	protected static Decoder decoder = Base64.getDecoder();
	
	public static ItemData getData(String item_bytes) {
		byte[] data = decoder.decode(item_bytes);
		InputStream is = null;
		NBTInputStream nbt = null;
		
		ItemData itemData = new ItemData();
		
		try {
			//Read in compressed data as NBT structure
			is = new ByteArrayInputStream(data);
			nbt = new NBTInputStream(is, true);
			//extreme reverse engineering
			NBTTagCompound compoundTag = nbt
					.readTag()
					.getAsTagCompound()
					.get("i")
					.getAsTagList()
					.get(0)
					.getAsTagCompound();
			
			nbt.close();
			
			//Read basic values in
			itemData.count = compoundTag.get("Count").getAsTagByte().getValue();
			
			NBTTagCompound extraAttributes = compoundTag.get("tag")
					.getAsTagCompound()
					.get("ExtraAttributes")
					.getAsTagCompound();
			
			itemData.itemType = extraAttributes.get("id").getAsTagString().getValue();
			if(extraAttributes.containsKey("modifier"))
				itemData.modifier = extraAttributes.get("modifier").getAsTagString().getValue();
			else
				itemData.modifier = "";
			//Enchantments
			Map<String, Integer> enchantments = new HashMap<String, Integer>();
			itemData.enchants = enchantments;
			if(extraAttributes.containsKey("enchantments")) {
				NBTTagCompound enchantTag = extraAttributes.get("enchantments")
					.getAsTagCompound();
			
				for(String enchantment : enchantTag.getValue().keySet()) {
					enchantments.put(enchantment,
							enchantTag.get(enchantment).getAsTagInt().getValue());
				}
			}
			
			if(nbt != null) nbt.close();
			if(is != null) is.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return itemData;
	}
	
	public static Map<String, Integer> stringToEnchants(String enchants) {
		Map<String, Integer> enchantMap = new HashMap<String, Integer>();
		if(enchants.length() < 2) return enchantMap;
		
		int length = enchants.length();
		
		//true - letters
		//false - number
		boolean mode = true;
		char c;
		
		StringBuilder builder = new StringBuilder();
		int level = 0;
		String enchant;
		
		for(int x = 0; x < length; x++) {
			c = enchants.charAt(x);
			if(c == '\n') {
				enchant = builder.toString();
				enchantMap.put(enchant, level);
				builder.setLength(0);
				mode = true;
			} else if(c == ':') {
				level = 0;
				mode = false;
			} else if(mode) {
				builder.append(c);
			} else {
				level = level * 10 + (c - '0');
			}
		}
		
		return enchantMap;
	}
	/**
	 * Converts a map of enchants to an encoded string
	 * 
	 * Sharpness VI, Critical VI, Scavenger IV
	 * 
	 * sharpness:6.critical:6.scavenger:6.
	 * 
	 * @param enchants	a map of enchants
	 * @return			a string representation of the enchants
	 */
	public static String enchantsToString(Map<String, Integer> enchants) {
		if(enchants.size() < 1) return "";
		StringBuilder builder = new StringBuilder();
		for(String enchant : enchants.keySet()) {
			builder.append(enchant);
			builder.append(':');
			builder.append(enchants.get(enchant));
			builder.append('\n');
		}
		builder.setLength(Math.max(builder.length() - 1, 0));
		return builder.toString();
	}
}
