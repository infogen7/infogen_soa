package com.infogen.encryption;

import java.text.ParseException;

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
 * HMAC方式的签名和验签
 * 
 * @author larry/larrylv@outlook.com/创建时间 2015年6月23日 下午5:13:24
 * @since 1.0
 * @version 1.0
 */
public class JWT_HMAC_Signature {

	public String signer(byte[] sharedSecret, JWTClaimsSet claimsSet) throws JOSEException {
		// Create HMAC signer
		JWSSigner signer = new MACSigner(sharedSecret);

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
		// Apply the HMAC
		signedJWT.sign(signer);
		// To serialize to compact form, produces something like
		// eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
		return signedJWT.serialize();

	}

	public Boolean verify(byte[] sharedSecret, String token) throws ParseException, JOSEException {
		JWSVerifier verifier = new MACVerifier(sharedSecret);
		SignedJWT signedJWT = SignedJWT.parse(token);
		return signedJWT.verify(verifier);
	}

	public static void main(String[] args) {

	}
}
