package com.infogen.encryption;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年6月23日 下午5:50:34
 * @since 1.0
 * @version 1.0
 */
public class RSAKeyPairGenerator {
	public static RSAKeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(1024);
		KeyPair kp = keyGenerator.genKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

		RSAKeyPair rsakeypair = new RSAKeyPair();
		rsakeypair.setPrivateKey(privateKey.getEncoded());
		rsakeypair.setPublicKey(publicKey.getEncoded());
		return rsakeypair;
	}
}
