package com.infogen.demo.security;

import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月21日 下午6:09:51
 * @since 1.0
 * @version 1.0
 */
public class JWT_HMAC_Protection {

	/**
	 * @param args
	 * @throws JOSEException
	 * @throws ParseException
	 * 
	 * @JWSAlgorithm.HS256 - HMAC with SHA-256
	 * @JWSAlgorithm.HS384 - HMAC with SHA-384
	 * @JWSAlgorithm.HS512 - HMAC with SHA-512
	 */
	public static void main(String[] args) throws JOSEException, ParseException {
		// Generate random 256-bit (32-byte) shared secret
		SecureRandom random = new SecureRandom();
		byte[] sharedSecret = new byte[32];
		random.nextBytes(sharedSecret);

		// Create HMAC signer
		JWSSigner signer = new MACSigner(sharedSecret);

		// Prepare JWT with claims set
		JWTClaimsSet claimsSet = new JWTClaimsSet();
		claimsSet.setSubject("alice");
		claimsSet.setIssueTime(new Date());
		claimsSet.setIssuer("https://c2id.com");
		claimsSet.setCustomClaim("key", "sdasdsad");

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

		// Apply the HMAC
		signedJWT.sign(signer);

		// To serialize to compact form, produces something like
		// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
		String s = signedJWT.serialize();

		// To parse the JWS and verify it, e.g. on client-side
		signedJWT = SignedJWT.parse(s);

		JWSVerifier verifier = new MACVerifier(sharedSecret);

		System.out.println(signedJWT.verify(verifier));

		System.out.println(signedJWT.getJWTClaimsSet().getSubject());
		System.out.println(signedJWT.getJWTClaimsSet().getStringClaim("key"));
		System.out.println(signedJWT.getJWTClaimsSet().toString());
	}

}
