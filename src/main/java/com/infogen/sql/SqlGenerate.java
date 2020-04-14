package com.infogen.sql;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.infogen.json.JSONArray;
import com.infogen.json.JSONObject;

/**
 * @author larry
 * @version 创建时间 2018年3月1日 下午4:59:50
 */
public class SqlGenerate {

	public Operator generate(JSONObject jsonobject) throws JsonProcessingException, IOException {
		Operator operator = new Empty(true);

		String type = jsonobject.getAsString("type", "");
		String key = jsonobject.getAsString("key", "");

		/*  */ if (type.equals("AND")) {
			AND and = new AND();
			JSONArray array = jsonobject.getAsJSONArray("value");
			for (int i = 0; i < array.size(); i++) {
				and.add(generate(array.getAsJSONObject(i)));
			}
			operator = and;
		} else if (type.equals("OR")) {
			OR or = new OR();
			JSONArray array = jsonobject.getAsJSONArray("value");
			for (int i = 0; i < array.size(); i++) {
				or.add(generate(array.getAsJSONObject(i)));
			}
			operator = or;
		} else if (type.equals("IN")) {
			IN in = new IN(key);
			JSONArray array = jsonobject.getAsJSONArray("value");
			for (Object object : array) {
				in.add(object.toString());
			}
			operator = in;
		} else if (type.equals("NOTIN")) {
			NOTIN notin = new NOTIN(key);
			JSONArray array = jsonobject.getAsJSONArray("value");
			for (Object object : array) {
				notin.add(object.toString());
			}
			operator = notin;
		} else if (type.equals("EQ")) {
			operator = new EQ(key, jsonobject.getAsString("value", null));
		} else if (type.equals("NE")) {
			operator = new NE(key, jsonobject.getAsString("value", null));
		} else if (type.equals("GT")) {
			operator = new GT(key, jsonobject.getAsDouble("value", null));
		} else if (type.equals("LT")) {
			operator = new LT(key, jsonobject.getAsDouble("value", null));
		} else if (type.equals("GE")) {
			operator = new GE(key, jsonobject.getAsDouble("value", null));
		} else if (type.equals("LE")) {
			operator = new LE(key, jsonobject.getAsDouble("value", null));
		} else if (type.equals("BETWEEN")) {
			operator = new Between(key, jsonobject.getAsDouble("min", null), jsonobject.getAsDouble("max", null));
		}
		return operator;
	}
}
