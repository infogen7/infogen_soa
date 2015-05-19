package com.infogen.demo.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

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
 * @author larry/larrylv@outlook.com/创建时间 2015年4月22日 上午9:50:15
 * @since 1.0
 * @version 1.0
 */
public class JWT_RSA_Signature {

	/**
	 * @param args
	 * @throws ParseException
	 * @throws NoSuchAlgorithmException
	 * @throws JOSEException
	 * 
	 * @JWSAlgorithm.RS256 - RSA PKCS#1 signature with SHA-256
	 * @JWSAlgorithm.RS384 - RSA PKCS#1 signature with SHA-384
	 * @JWSAlgorithm.RS512 - RSA PKCS#1 signature with SHA-512
	 * @JWSAlgorithm.PS256 - RSA PSS signature with SHA-256
	 * @JWSAlgorithm.PS384 - RSA PSS signature with SHA-384
	 * @JWSAlgorithm.PS512 - RSA PSS signature with SHA-512
	 */
	public static void main(String[] args) throws ParseException, NoSuchAlgorithmException, JOSEException {
		// RSA signatures require a public and private RSA key pair,
		// the public key must be made known to the JWS recipient in
		// order to verify the signatures
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(1024);

		KeyPair kp = keyGenerator.genKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) kp.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) kp.getPrivate();

		// Create RSA-signer with the private key
		JWSSigner signer = new RSASSASigner(privateKey);

		// Prepare JWT with claims set
		JWTClaimsSet claimsSet = new JWTClaimsSet();
		claimsSet.setSubject("alice");
		claimsSet.setIssueTime(new Date());
		claimsSet.setIssuer("https://c2id.com");

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

		// Compute the RSA signature
		signedJWT.sign(signer);

		// To serialize to compact form, produces something like
		// eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
		// mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
		// maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
		// -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
		String s = signedJWT.serialize();

		// To parse the JWS and verify it, e.g. on client-side
		signedJWT = SignedJWT.parse(s);

		JWSVerifier verifier = new RSASSAVerifier(publicKey);
		System.out.println(signedJWT.verify(verifier));

		System.out.println(signedJWT.getJWTClaimsSet().getSubject());
		System.out.println(signedJWT.getJWTClaimsSet().getStringClaim("key"));
		System.out.println(signedJWT.getJWTClaimsSet().toString());
	}

}
