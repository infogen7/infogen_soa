package com.infogen.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author larry
 * @version 创建时间 2018年3月1日 下午4:59:50
 */
public class SqlValidation {
	// 判断是否存在sql注入
	public static Boolean is_sql_safety(String string) {
		if (null == string || string.trim().length() <= 0) {
			return false;
		}
		//- 多用于 CODE 等业务，但是--可能被用来做注入
		return Pattern.compile("^[-_0-9a-zA-Z\\u4e00-\\u9fa5]{1,32}$").matcher(string).matches() && string.indexOf("--") == -1;
	}

	public static String[] is_sql_safety(String[] item_array) {
		List<String> return_list = new ArrayList<>();
		for (int i = 0; i < item_array.length; i++) {
			String string = item_array[i];

			if (is_sql_safety(string)) {
				return_list.add(string);
			}
		}
		return return_list.toArray(new String[] {});
	}

}
