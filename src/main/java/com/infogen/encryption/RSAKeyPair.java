package com.infogen.encryption;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月23日 下午5:30:50
 * @since 1.0
 * @version 1.0
 */
public class RSAKeyPair {
	private byte[] publicKey;
	private byte[] privateKey;

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

}
