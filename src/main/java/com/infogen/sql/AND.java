package com.infogen.sql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午9:54:26
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class AND extends Operator {
	private static final long serialVersionUID = 8115752087744476840L;

	public AND(Operator... items) {
		super();
		for (Operator operation : items) {
			this.items.add(operation);
		}
	}

	private List<Operator> items = new ArrayList<>();

	public void add(Operator item) {
		items.add(item);
	}

	public String to_filter() {
		Integer size = items.size();
		if (size == 0) {
			return " 1 = 1 ";
		} else if (size == 1) {
			return items.get(0).to_filter();
		} else {
			StringBuilder string_builder = new StringBuilder();
			string_builder.append("(");
			for (int i = 0; i < items.size(); i++) {
				Operator operation = items.get(i);
				string_builder.append(operation.to_filter());
				if (i != size - 1) {
					string_builder.append(" AND ");
				}
			}
			return string_builder.append(")").toString();
		}
	}
}
