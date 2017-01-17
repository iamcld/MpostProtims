package com.pax.utils;


public class MAC {

	public static byte[] calc(byte[] key, byte[] dataIn, byte mode) {
		int realLen = (dataIn.length + 7) / 8 * 8;
		byte[] paddedData = new byte[realLen];
		System.arraycopy(dataIn, 0, paddedData, 0, dataIn.length);

		byte[] iv = new byte[8];
		for (int i = 0; i < realLen; i += 8) {
			byte[] block = new byte[8];
			System.arraycopy(paddedData, i, block, 0, 8);
			byte[] in = Utils.xor(block, iv, 8);

			if (mode == 0) {
				if (key.length > 8) {
					iv = TDES.encryptMode(key, in);
				} else {
					iv = Des.desCrypto(in, key);
				}
			} else if (mode == 1) {
				if (i == realLen - 8) {
					if (key.length > 8) {
						iv = TDES.encryptMode(key, in);
					} else {
						iv = Des.desCrypto(in, key);
					}
				} else {
					iv = in;
				}
			} else {
				if ((i == realLen - 8) && key.length > 8) {
					iv = TDES.encryptMode(key, in);
				} else {
					iv = Des.desCrypto(in, key);
				}
			}
		}
		return iv;
	}
}
