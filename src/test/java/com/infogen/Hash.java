/**
 * @author larry/larrylv@outlook.com
 * @date 创建时间 2015年6月15日 下午6:40:03
 * @version 1.0
 */
package com.infogen;

import java.util.Random;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月15日 下午6:40:03
 * @since 1.0
 * @version 1.0
 */
public class Hash {
	public static void main(String[] args) {
		Random r = new Random("dasdasedsa".hashCode());
		for (int i = 0; i < 10; i++) {
			System.out.println(r.nextInt());
		}

	}
}
