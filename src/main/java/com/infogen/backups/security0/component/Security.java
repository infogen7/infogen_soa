package com.infogen.backups.security0.component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月8日 下午4:32:46
 * @since 1.0
 * @version 1.0
 */
public class Security {
	private List<WhiteList> white_lists = new ArrayList<>();

	public List<WhiteList> getWhite_lists() {
		return white_lists;
	}

	public void setWhite_lists(List<WhiteList> white_lists) {
		this.white_lists = white_lists;
	}

}
