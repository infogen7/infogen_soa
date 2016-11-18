package com.infogen.core.structure.map.consistent_hash.hash;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月16日 上午11:17:11
 * @since 1.0
 * @version 1.0
 */
public class MurmurHash implements HashFunction {
	private int default_seed = 0x1234ABCD;

	/**
	 * Hashes bytes in an array.
	 * 
	 * @param data
	 *            The bytes to hash.
	 * @return The 32 bit hash of the bytes in question.
	 */

	public int hash32(byte[] data) {
		return hash(ByteBuffer.wrap(data), default_seed);
	}

	public long hash32(String data) throws UnsupportedEncodingException {
		return hash64A(ByteBuffer.wrap(data.getBytes("UTF-8")), default_seed);
	}

	@Override
	public long hash(byte[] key) {
		return hash64A(ByteBuffer.wrap(key), default_seed);
	}

	@Override
	public long hash(String key, Charset charset) throws UnsupportedEncodingException {
		return hash64A(ByteBuffer.wrap(key.getBytes(charset)), default_seed);
	}

	/**
	 * Hashes the bytes in a buffer from the current position to the limit.
	 * 
	 * @param buf
	 *            The bytes to hash.
	 * @param seed
	 *            The seed for the hash.
	 * @return The 32 bit murmur hash of the bytes in the buffer.
	 */
	private int hash(ByteBuffer buf, int seed) {
		// save byte order for later restoration
		ByteOrder byteOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);

		int m = 0x5bd1e995;
		int r = 24;

		int h = seed ^ buf.remaining();

		int k;
		while (buf.remaining() >= 4) {
			k = buf.getInt();

			k *= m;
			k ^= k >>> r;
			k *= m;

			h *= m;
			h ^= k;
		}

		if (buf.remaining() > 0) {
			ByteBuffer finish = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
			// for big-endian version, use this first:
			// finish.position(4-buf.remaining());
			finish.put(buf).rewind();
			h ^= finish.getInt();
			h *= m;
		}

		h ^= h >>> 13;
		h *= m;
		h ^= h >>> 15;

		buf.order(byteOrder);
		return h;
	}

	private long hash64A(ByteBuffer buf, int seed) {
		ByteOrder byteOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);

		long m = 0xc6a4a7935bd1e995L;
		int r = 47;

		long h = seed ^ (buf.remaining() * m);

		long k;
		while (buf.remaining() >= 8) {
			k = buf.getLong();

			k *= m;
			k ^= k >>> r;
			k *= m;

			h ^= k;
			h *= m;
		}

		if (buf.remaining() > 0) {
			ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
			// for big-endian version, do this first:
			// finish.position(8-buf.remaining());
			finish.put(buf).rewind();
			h ^= finish.getLong();
			h *= m;
		}

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		buf.order(byteOrder);
		return h;
	}

}