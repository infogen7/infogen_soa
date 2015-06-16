package com.infogen.util.map.consistent_hash.hash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 下午12:11:25
 * @since 1.0
 * @version 1.0
 */
public class MD5Hash implements HashFunction {
	public ThreadLocal<MessageDigest> md5Holder = new ThreadLocal<MessageDigest>();

	public long hash(String key, Charset charset) throws UnsupportedEncodingException {
		return hash(key.getBytes(charset));
	}

	public long hash(byte[] key) {
		try {
			if (md5Holder.get() == null) {
				md5Holder.set(MessageDigest.getInstance("MD5"));
			}
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("++++ no md5 algorythm found");
		}
		MessageDigest md5 = md5Holder.get();

		md5.reset();
		md5.update(key);
		byte[] bKey = md5.digest();
		long res = ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) | ((long) (bKey[1] & 0xFF) << 8) | (long) (bKey[0] & 0xFF);
		return res;
	}
}
