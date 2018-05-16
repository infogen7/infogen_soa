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
		return Regular(string, "^[_0-9a-zA-Z\\u4e00-\\u9fa5]{1,32}$");
	}

	public static String[] is_sql_safety(String[] item_array) {
		List<String> return_list = new ArrayList<>();
		for (int i = 0; i < item_array.length; i++) {
			String string = item_array[i];

			if (Regular(string, "^[_0-9a-zA-Z\\u4e00-\\u9fa5]{1,32}$")) {
				return_list.add(string);
			}
		}
		return return_list.toArray(new String[] {});
	}

	/**
	 * 匹配是否符合正则表达式pattern 匹配返回true
	 * 
	 * @param str
	 *            匹配的字符串
	 * @param pattern
	 *            匹配模式
	 * @return Boolean
	 */
	private static Boolean Regular(String string, String pattern) {
		if (null == string || string.trim().length() <= 0) {
			return false;
		}
		return Pattern.compile(pattern).matcher(string).matches();
	}
}
