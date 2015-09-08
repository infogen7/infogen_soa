package com.infogen.encryption;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * RSA方式的加密和解密
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月23日 下午5:41:51
 * @since 1.0
 * @version 1.0
 */
public class JWT_RSA_Encryption {

	public String encryption(byte[] publicKeyBytes, JWTClaimsSet claimsSet) throws NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
		// Request JWT encrypted with RSA-OAEP and 128-bit AES/GCM
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

		// Create the encrypted JWT object
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet);

		// Create an encrypter with the specified public RSA key
		RSAEncrypter encrypter = new RSAEncrypter(publicKey);

		// Do the actual encryption
		jwt.encrypt(encrypter);
		return jwt.serialize();

	}

	public JWTClaimsSet decrypt(byte[] privateKeyBytes, String token) throws NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

		// Parse back
		EncryptedJWT jwt = EncryptedJWT.parse(token);

		// Create a decrypter with the specified private RSA keyF
		RSADecrypter decrypter = new RSADecrypter(privateKey);

		// Decrypt
		jwt.decrypt(decrypter);

		return jwt.getJWTClaimsSet();
	}
}
