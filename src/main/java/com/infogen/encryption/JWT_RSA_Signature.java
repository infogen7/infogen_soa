package com.infogen.encryption;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * RSA方式的签名和验签
 * @author larry/larrylv@outlook.com/创建时间 2015年6月23日 下午5:21:06
 * @since 1.0
 * @version 1.0
 */
public class JWT_RSA_Signature {

	public String signer(byte[] privateKeyBytes, JWTClaimsSet claimsSet) throws InvalidKeySpecException, NoSuchAlgorithmException, JOSEException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(privateKey);

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

		// Compute the RSA signature
		signedJWT.sign(signer);

		// To serialize to compact form, produces something like
		// eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
		// mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
		// maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
		// -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
		return signedJWT.serialize();

	}

	public Boolean verify(byte[] publicKeyBytes, String token) throws ParseException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException {
		SignedJWT signedJWT = SignedJWT.parse(token);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
		JWSVerifier verifier = new RSASSAVerifier(publicKey);
		return signedJWT.verify(verifier);
	}

	public static void main(String[] args) throws ParseException, NoSuchAlgorithmException, JOSEException {
	}

}