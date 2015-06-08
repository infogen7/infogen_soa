package com.infogen.tracking.enum0;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月8日 下午2:34:47
 * @since 1.0
 * @version 1.0
 */
public enum Track {
	x_track_id("x-track", "trackid"), //
	x_identify("x-iden", "identify"), //
	x_sequence("x-seq", "sequence"), //
	x_referer("x-ref", "referer");
	public String key;
	public String note;

	private Track(String key, String note) {
		this.key = key;
		this.note = note;
	}
}
