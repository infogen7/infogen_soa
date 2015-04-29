package com.infogen.demo.security;

import java.security.SecureRandom;
import java.text.ParseException;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年4月21日 下午5:00:10
 * @since 1.0
 * @version 1.0
 */
public class JWS_HMAC_Protection {

	/**
	 * 
	 * @throws JOSEException
	 * @throws ParseException
	 * @JWSAlgorithm.HS256 - HMAC with SHA-256
	 * @JWSAlgorithm.HS384 - HMAC with SHA-384
	 * @JWSAlgorithm.HS512 - HMAC with SHA-512
	 */
	public static void main(String[] args) throws JOSEException, ParseException {
		// Generate random 256-bit (32-byte) shared secret
		//共享密钥 位数要大于等于SHA输出
		SecureRandom random = new SecureRandom();
		byte[] sharedSecret = new byte[32];
		random.nextBytes(sharedSecret);

		// Create HMAC signer
		JWSSigner signer = new MACSigner(sharedSecret);

		// Prepare JWS object with "Hello, world!" payload
		JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload("Hello, world!"));

		// Apply the HMAC
		jwsObject.sign(signer);

		// To serialize to compact form, produces something like
		// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
		String s = jwsObject.serialize();

		// To parse the JWS and verify it, e.g. on client-side
		jwsObject = JWSObject.parse(s);

		JWSVerifier verifier = new MACVerifier(sharedSecret);

		System.out.println(jwsObject.verify(verifier));

		System.out.println(jwsObject.getPayload().toString());
	}

}
