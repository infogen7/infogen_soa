package com.infogen.sql;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author larry
 * @version 创建时间 2017年9月26日 上午9:54:26
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, setterVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class OR extends Operator {
	private static final long serialVersionUID = -8882217945298737881L;

	public OR(Operator... items) {
		super();
		for (Operator operation : items) {
			this.items.add(operation);
		}
	}

	private List<Operator> items = new ArrayList<>();

	public void add(Operator item) {
		items.add(item);
	}

	public String sql() {
		Integer size = items.size();

		if (size == 0) {
			return " 1 = 1 ";
		}

		if (size == 1) {
			return items.get(0).sql();
		}

		// size > 1
		StringBuilder string_builder = new StringBuilder();
		string_builder.append("(");

		for (int i = 0; i < size; i++) {
			Operator operation = items.get(i);

			string_builder.append(operation.sql());
			if (i != size - 1) {
				string_builder.append(" OR ");
			}
		}

		string_builder.append(")");
		return string_builder.toString();
	}
}
