package com.infogen.core.structure.map.consistent_hash.hash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 下午12:11:25
 * @since 1.0
 * @version 1.0
 */
public class MD5Hash implements HashFunction {
	private static final Logger LOGGER = LogManager.getLogger(MD5Hash.class.getName());
	public ThreadLocal<MessageDigest> md5Holder = new ThreadLocal<MessageDigest>();

	@Override
	public long hash(String key, Charset charset) throws UnsupportedEncodingException {
		return hash(key.getBytes(charset));
	}

	@Override
	public long hash(byte[] key) {
		try {
			if (md5Holder.get() == null) {
				md5Holder.set(MessageDigest.getInstance("MD5"));
			}
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("获取MD5生成器失败", e);
			throw new IllegalStateException("++++ no md5 algorythm found");
		}
		MessageDigest md5 = md5Holder.get();

		md5.reset();
		md5.update(key);
		byte[] bKey = md5.digest();
		return ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16) | ((long) (bKey[1] & 0xFF) << 8) | (long) (bKey[0] & 0xFF);
	}
}
