package com.infogen.tools;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pojo {
	public static final ZoneId zoneid = ZoneId.of("GMT+08:00");
	public int i ;
	public short ver_short ;
	public char var_char ;
	public byte var_byte ;
	public boolean var_boolean ;
	public long var_long ;
	public float var_float ;
	public double var_double ;
	public Timestamp Ct ;
	public String name ;
	public List<String> list = new ArrayList<String>();
	public Map<String,String> map = new HashMap<String,String>();
	public enum status {
		开始, 执行中, 完成
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	public short getVer_short() {
		return ver_short;
	}
	public void setVer_short(short ver_short) {
		this.ver_short = ver_short;
	}
	public char getVar_char() {
		return var_char;
	}
	public void setVar_char(char var_char) {
		this.var_char = var_char;
	}
	public byte getVar_byte() {
		return var_byte;
	}
	public void setVar_byte(byte var_byte) {
		this.var_byte = var_byte;
	}
	public boolean isVar_boolean() {
		return var_boolean;
	}
	public void setVar_boolean(boolean var_boolean) {
		this.var_boolean = var_boolean;
	}
	public long getVar_long() {
		return var_long;
	}
	public void setVar_long(long var_long) {
		this.var_long = var_long;
	}
	public float getVar_float() {
		return var_float;
	}
	public void setVar_float(float var_float) {
		this.var_float = var_float;
	}
	public double getVar_double() {
		return var_double;
	}
	public void setVar_double(double var_double) {
		this.var_double = var_double;
	}
	public Timestamp getCt() {
		return Ct;
	}
	public void setCt(Timestamp ct) {
		Ct = ct;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
	public Map<String, String> getMap() {
		return map;
	}
	public void setMap(Map<String, String> map) {
		this.map = map;
	}	
}
