/**
 * 
 */
package com.infogen.core.tools;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年5月19日 下午2:50:30
 * @since 1.0
 * @version 1.0
 */
public class Tool_Pattern {


	private static final String PATTERN_LINE_START = "^";
	private static final String PATTERN_LINE_END = "$";
	private static final char[] META_CHARACTERS = { '$', '^', '[', ']', '(', ')', '{', '}', '|', '+', '.', '\\' };

	/**
	 * 通配符匹配
	 * 
	 * @param pattern
	 *            规则
	 * @param str
	 *            验证字符串
	 * @return 是否验证成功
	 */
	public static boolean pattern_match(String pattern, String str) {
		pattern = convertToRegexPattern(pattern);
		return Pattern.matches(pattern, str);
	}

	private static String convertToRegexPattern(String wildcardString) {
		String result = PATTERN_LINE_START;
		char[] chars = wildcardString.toCharArray();
		for (char ch : chars) {
			if (Arrays.binarySearch(META_CHARACTERS, ch) >= 0) {
				result += "\\" + ch;
				continue;
			}
			switch (ch) {
			case '*':
				result += ".*";
				break;
			case '?':
				result += ".{0,1}";
				break;
			default:
				result += ch;
			}
		}
		result += PATTERN_LINE_END;
		return result;
	}

}
