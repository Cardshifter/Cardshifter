package com.cardshifter.core;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class ExtLuaTable extends LuaTable {

	public static interface TableChange {
		void onChange(Object key, Object value);
	}

	private final TableChange changeListener;
	
	public ExtLuaTable(TableChange change) {
		this.changeListener = change;
	}
	
	@Override
	public void set(int key, LuaValue value) {
		super.set(key, value);
		System.out.println("set int, LuaValue");
		this.onChange(key, value.tojstring());
	}
	
	@Override
	public void set(int key, String value) {
		super.set(key, value);
		System.out.println("set int, String");
//		this.onChange(key, value);
	}
	
	@Override
	public void set(LuaValue key, LuaValue value) {
		super.set(key, value);
		System.out.println("set LuaValue, LuaValue");
		System.out.println("set " + key.tojstring() + " = " + value.tojstring());
		this.onChange(key.tojstring(), value.tojstring());
	}
	
	@Override
	public void set(String key, double value) {
		super.set(key, value);
		System.out.println("set String, double");
//		this.onChange(key, value);
	}
	
	@Override
	public void set(String key, int value) {
		super.set(key, value);
		System.out.println("set String, int");
//		this.onChange(key, value);
	}
	
	@Override
	public void set(String key, LuaValue value) {
		super.set(key, value);
		System.out.println("set String, LuaValue");
//		this.onChange(key, value);
	}
	
	@Override
	public void set(String key, String value) {
		super.set(key, value);
		System.out.println("set String, String");
//		this.onChange(key, value);
	}

	private void onChange(Object key, Object value) {
		System.out.println("onChange " + key.getClass() + " = " + value.getClass());
		this.changeListener.onChange(key, value);
	}
	
}
