package com.infogen.core.structure.map.consistent_hash.hash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 上午10:44:17
 * @since 1.0
 * @version 1.0
 */
public interface HashFunction {
	public static final HashFunction MURMUR_HASH = new MurmurHash();
	public static final HashFunction MD5 = new MD5Hash();

	public long hash(String key, Charset charset) throws UnsupportedEncodingException;

	public long hash(byte[] key);
}