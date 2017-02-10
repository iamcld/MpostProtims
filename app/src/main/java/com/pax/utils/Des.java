package com.pax.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Des {
	public static byte[] desCrypto(byte[] datasource, byte[] key) {
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance("DES" + "/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			return cipher.doFinal(datasource);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decrypt(byte[] src, byte[] key) {
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(key);
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance("DES" + "/ECB/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, securekey, random);
			return cipher.doFinal(src);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] sessionkey(byte[] Distributedpara, byte[] keysource) {
		byte[] sessionkey;
		byte[] Distributedparaback = new byte[Distributedpara.length];		
		int keylen = keysource.length;
		byte[] keysourceleft = new byte[keylen/2];
		System.arraycopy(keysource, 0, keysourceleft, 0, keylen/2);
		byte[] keysourceright = new byte[keylen/2];
		System.arraycopy(keysource, keylen/2, keysourceright, 0, keylen/2);
		
		byte[] Templeft = Des.desCrypto(Distributedpara, keysourceleft);
		Templeft = Des.decrypt(Templeft, keysourceright);
		Templeft = Des.desCrypto(Templeft, keysourceleft);
		byte[] sessionkeyleft = new byte[Templeft.length];
		System.arraycopy(Templeft, 0, sessionkeyleft, 0, Templeft.length);
		
		for(int i = 0; i < Distributedpara.length; i++){
			Distributedparaback[i] = (byte)(~(int)Distributedpara[i]);
		}
		
		byte[] Tempright = Des.desCrypto(Distributedparaback, keysourceleft);
		Tempright = Des.decrypt(Tempright, keysourceright);
		Tempright = Des.desCrypto(Tempright, keysourceleft);
		byte[] sessionkeyright = new byte[Tempright.length];
		System.arraycopy(Tempright, 0, sessionkeyright, 0, Tempright.length);

		sessionkey = new byte[sessionkeyleft.length + sessionkeyright.length];
		System.arraycopy(sessionkeyleft, 0, sessionkey, 0, sessionkeyleft.length);
		System.arraycopy(sessionkeyright, 0, sessionkey, sessionkeyleft.length, sessionkeyright.length);
		
		StringBuffer sessionkeystr = new StringBuffer();
 		for(int i = 0; i<sessionkey.length; i++){
 			sessionkeystr.append(Integer.toHexString((sessionkey[i]& 0x000000FF) | 0xFFFFFF00).substring(6));
		}
 		System.out.println("sessionkey:" + sessionkeystr);
 		
		return sessionkey;
	}

}
