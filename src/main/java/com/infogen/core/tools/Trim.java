/**
 * 
 */
package com.infogen.core.tools;

import java.util.regex.Pattern;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Trim {
	/**
	 * @param string
	 *            输入字符串
	 * @return trim 替换中文空格的特殊字符为普通空格 去掉字符串中前后的空格，并将中间多个连续的空格合并成一个 去掉开头出现的空格
	 */
	public static String trim(String string) {
		if (string == null) {
			return null;
		}
		// 去掉特殊空格
		string = string.replace(" ", " ");
		// 并将中间多个连续的空格合并成一个
		String trim = Pattern.compile("[' ']+").matcher(string).replaceAll(" ").trim();
		//
		if (trim.startsWith(" ")) {
			trim = trim.substring(1);
		}
		return trim;
	}


}
