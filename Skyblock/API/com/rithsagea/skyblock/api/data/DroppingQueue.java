package com.rithsagea.skyblock.api.data;

import java.util.ArrayDeque;

public class DroppingQueue<T> extends ArrayDeque<T> {

	private static final long serialVersionUID = 1498462806896897001L;
	private int size;

	public DroppingQueue(int size) {
		super(size);
		this.size = size;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	@Override
	public boolean add(T obj) {
		super.add(obj);
		while(super.size() > size) {
			super.remove();
		}
		return false;
	}
}
