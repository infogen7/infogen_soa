package com.infogen.sql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午11:18:48
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class NotIN extends Operator {
	private static final long serialVersionUID = 103243415527557652L;

	public NotIN(String key, String... items) {
		super();
		this.key = key;
		for (String string : items) {
			this.items.add(string);
		}
	}

	public NotIN(String key, Number... items) {
		super();
		this.key = key;
		for (Number string : items) {
			this.items.add(string.toString());
		}
		this.compare_number = true;
	}

	public String key = "";
	private List<String> items = new ArrayList<>();
	private Boolean compare_number = false;

	public void add(String item) {
		items.add(item);
	}

	public String sql() {
		if (items.isEmpty()) {
			return " 1 = 1 ";
		}

		StringBuilder string_builder = new StringBuilder();
		string_builder.append(" ").append(key).append(" NOT IN ");
		string_builder.append("(");

		for (int i = 0, size = items.size(); i < size; i++) {
			if (compare_number) {
				string_builder.append(items.get(i));
			} else {
				string_builder.append("'");
				string_builder.append(items.get(i));
				string_builder.append("'");
			}

			if (i != size - 1) {
				string_builder.append(" , ");
			}
		}

		string_builder.append(")");
		return string_builder.toString();
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the items
	 */
	public List<String> getItems() {
		return items;
	}

	/**
	 * @param items
	 *            the items to set
	 */
	public void setItems(List<String> items) {
		this.items = items;
	}

}
