package com.ruinscraft.chip;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.google.common.io.Files;

public class Crypto {

	private static final String alg = "AES";
	
	private byte[] secret;
	
	public Crypto(String secret) {
		this.secret = secret.getBytes();
	}
	
	public Crypto(File file) {
		try {
			secret = Files.readFirstLine(file, Charsets.UTF_8).getBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String generateSecret() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public Key getKey() {
		return new SecretKeySpec(secret, alg);
	}
	
	public String encrypt(String data) {
		try {
			Key key = getKey();
			
			Cipher cipher = Cipher.getInstance(alg);
			
			cipher.init(Cipher.ENCRYPT_MODE, key);
			
			byte[] encrData = cipher.doFinal(data.getBytes());
			
			byte[] encrDataEncoded = Base64.encodeBase64(encrData);
					
			return new String(encrDataEncoded);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String decrypt(String data) {
		try {
			Key key = getKey();
			
			Cipher cipher = Cipher.getInstance(alg);
			
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] decodedData = Base64.decodeBase64(data.getBytes());
			
			byte[] decrDecodedData = cipher.doFinal(decodedData);
			
			return new String(decrDecodedData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
