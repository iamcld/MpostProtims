package com.pax.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	private static final String TAG = "Utils";
	
	public static void long2Hex(long data,byte[] hex) {
		short nHigh, nLow;	
		nHigh = (short)(data/65536);
		nLow  = (short)(data%65536);
		hex[0] = (byte) (nHigh/256);
		hex[1] = (byte) (nHigh%256);
		hex[2] = (byte) (nLow/256);
		hex[3] = (byte) (nLow %256);
	}
	
	public static void randomBytes(byte[] in) {
		Random random = new Random();
		random.nextBytes(in);
	}

	/**
	 * 十六进制字符串装十进制
	 *
	 * @param hex
	 *            十六进制字符串
	 * @return 十进制数值
	 */
	public static int hexStringToAlgorism(String hex) {
		hex = hex.toUpperCase();
		int max = hex.length();
		int result = 0;
		for (int i = max; i > 0; i--) {
			char c = hex.charAt(i - 1);
			int algorism = 0;
			if (c >= '0' && c <= '9') {
				algorism = c - '0';
			} else {
				algorism = c - 55;
			}
			result += Math.pow(16, max - i) * algorism;
		}
		return result;
	}

	/**
	 * ASCII码字符串转数字字符串
	 *
	 * @param content
	 *            ASCII字符串
	 * @return 字符串
	 */
	public static String asciiStringToString(String content) {
		StringBuffer result = new StringBuffer();
		int length = content.length() / 2;
		for (int i = 0; i < length; i++) {
			String c = content.substring(i * 2, i * 2 + 2);
			int a = hexStringToAlgorism(c);
			char b = (char) a;
			String d = String.valueOf(b);
			result.append(d);
		}
		return result.toString();
	}


	// lrc
	public static byte lrc(byte[] data, int offset, int len) {
		byte lrc = 0;
		for (int i = 0; i < len; i ++) {
			lrc ^= data[i + offset];
		}
		return lrc;
	}
	
	// standard 32-bit CRC
	public static byte[] crc(byte[] data, int offset, int len){
		byte[] crc = new byte[4];
		byte tmpch;
		long tl  = 0L;
		long rsl = 0xffffffffL;
		for(int i=offset; i<len+offset; i++){
			tmpch = (byte) rsl; 	
			tmpch = (byte) (tmpch^data[i]);
			tl = (long)tmpch;
			for(int j=0; j<8; j++){
				if ((tl & 1) != 0){
					tl = 0xedb88320L^(tl>>1);
				}
				else {
					tl = tl>>1;
				}
			}
			rsl = tl^(rsl>>8);
		}
		
		rsl ^= 0xffffffffL;
		crc[0] = (byte)(rsl>>24);
		crc[1] = (byte)(rsl>>16);
		crc[2] = (byte)(rsl>>8);
		crc[3] = (byte)(rsl);
		return crc;
	}

//	public static String byte2HexStrUnFormatted(byte[] bytes, int offset, int len) {
//		if (offset > bytes.length || offset + len > bytes.length) {
//			return "";
//		}
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < len; i ++) {
//			sb.append(Integer.toHexString(bytes[i + offset] | 0xFFFFFF00).substring(6));
////			if (((i + 1) % 16) == 0) {
////			}
//		}
//		return sb.toString();
//	}
	
	public static void logHexData(byte[] bytes, int offset, int len) {
		if (offset > bytes.length || offset + len > bytes.length) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i ++) {
			if ((i % 16) == 0) {
				sb.append((i / 16) + 1).append(". ");
			}
			sb.append(Integer.toHexString(bytes[i + offset] | 0xFFFFFF00).substring(6));
			sb.append(" ");
			if (((i + 1) % 16) == 0) {
				sb.append("\n");
			}
		}
		LogUtils.d(sb.toString());
	}

	public static String byte2HexStr(byte[] bytes, int offset, int len) {
		if (offset > bytes.length || offset + len > bytes.length) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (int i = 0; i < len; i ++) {
			sb.append(Integer.toHexString(bytes[i + offset] | 0xFFFFFF00).substring(6));
			sb.append(" ");
			if (((i + 1) % 16) == 0) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static byte[] xor(byte[] a, byte[] b, int len) {
		byte[] result = new byte[len]; 
		for (int i = 0; i < len; i++) {
			result[i] = (byte)(a[i] ^ b[i]);
		}
		return result;
	}

	public static String bcd2Str(byte[] bytes){
	    StringBuilder temp=new StringBuilder(bytes.length*2);
	    for(int i=0;i<bytes.length;i++){
	    	byte left = (byte)((bytes[i] & 0xf0)>>>4);
	    	byte right = (byte)(bytes[i] & 0x0f);
	    	if (left >= 0x0a && left <= 0x0f) {
	    		left -= 0x0a;
	    		left += 'A';
	    	} else {
	    		left += '0';
	    	}

	    	if (right >= 0x0a && right <= 0x0f) {
	    		right -= 0x0a;
	    		right += 'A';
	    	} else {
	    		right += '0';
	    	}

	    	temp.append(String.format("%c", left));
	    	temp.append(String.format("%c", right));
	    }
	    return temp.toString();
	}

	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}
		//byte abt[] = new byte[len];
		byte abt[];
		if (len >= 2) {
			len = len / 2;
		}
		byte bbt[] = new byte[len];
		abt = asc.getBytes(Charset.defaultCharset());
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else if ((abt[2 * p] >= 'A') && (abt[2 * p] <= 'Z')) {
				j = abt[2 * p] - 'A' + 0x0a;
			} else {
				j = abt[2 * p] - '0';
			}

			if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else if ((abt[2 * p + 1] >= 'A') && (abt[2 * p + 1] <= 'Z')) {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			} else {
				k = abt[2 * p + 1] - '0';
			}

			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	public static boolean cmpByteArray(byte[] a, int aOffset, byte[] b, int bOffset, int len) {
		if ((aOffset + len)> a.length || (bOffset + len) > b.length) {
			return false;
		}
		
		for (int i = 0; i < len; i++) {
			if (a[aOffset + i] != b[bOffset + i]) {
				return false;
			}
		}
		
		return true;
	}
	
	public static void int2ByteArray(int x, byte[] to, int offset) {
		to[offset] 		= (byte)((x >>> 24) & 0xff);
		to[offset + 1] 	= (byte)((x >>> 16) & 0xff);
		to[offset + 2] 	= (byte)((x >>> 8) & 0xff);
		to[offset + 3] 	= (byte)(x & 0xff);
	}

	public static void short2ByteArray(short x, byte[] to, int offset) {
		to[offset] 		= (byte)((x >>> 8) & 0xff);
		to[offset + 1] 	= (byte)(x & 0xff);
	}	

	public static int intFromByteArray(byte[] from, int offset) {
    	return ((from[offset] << 24) & 0xff000000) | ((from[offset + 1] << 16) & 0xff0000) | ((from[offset + 2] << 8) & 0xff00) | (from[offset + 3] & 0xff);
	}

	public static short shortFromByteArray(byte[] from, int offset) {
    	return (short)(((from[offset] << 8) & 0xff00) | (from[offset + 1] & 0xff));
	}
	
	public static int min(int a, int b) {
		return (a < b) ? a : b;
	}
	
	public static class RingBuffer {
		private byte[] buffer;
		private int wp = 0;		//write pointer
		private int rp = 0;		//read pointer
		
		public RingBuffer(int size) {
			buffer = new byte[size];
		}
		
		// 0      			1       		2
		//total bytes/forward bytes/backward bytes
		private synchronized int[] statusForRead() {
			int[] ret = new int[3];
			if (wp >= rp) {
				ret[1] = wp - rp;
				ret[2] = 0;
			} else {
				ret[1] = buffer.length - rp;
				ret[2] = wp;
			}
			
			ret[0] = ret[1] + ret[2];
			return ret;
		}
		
		// 0      			1       		2
		//free bytes/forward bytes/backward bytes
		private synchronized int[] statusForWrite() {
			int[] ret = new int[3];
			if (wp >= rp) {
				ret[1] = buffer.length - wp;
				if (rp == 0) {
					ret[1]--;	//so that the wp won't overlap with rp.
				}
				
				ret[2] = rp;
				if (ret[2] > 0) {
					ret[2]--;
				}
			} else {
				ret[1] = rp - wp - 1;
				ret[2] = 0;
			}
			
			ret[0] = ret[1] + ret[2];	//maximum buffer.length - 1;
			return ret;
		}
		
		public synchronized int read(byte[] out, int offset, int exp) {
			int[] status = statusForRead();
			if (exp > status[0]) {
				exp = status[0];
			}
			
			if (exp <= status[1]) {
				System.arraycopy(buffer, rp, out, offset, exp);
				rp += exp;
				rp %= buffer.length;
				
				return exp;
			} else {
				System.arraycopy(buffer, rp, out, offset, status[1]);
				System.arraycopy(buffer, 0, out, offset + status[1], min(exp - status[1], status[2]));
				rp = min(exp - status[1], status[2]);
				
				return status[1] + rp;
			}
		}
		
		@SuppressLint("DefaultLocale")
		public synchronized int write(byte[] data, int len) {
			int[] status = statusForWrite();
			int realLen = len;
			
			if (realLen > status[0]) {
				LogUtils.d( String.format("len %d too long, free space %d not enough, only %d will be saved!", realLen, status[0], status[0]));
				realLen = status[0];
			}
			
			if (realLen <= status[1]) {
				System.arraycopy(data, 0, buffer, wp, realLen);
				wp += realLen;
				wp %= buffer.length;
			} else {
				System.arraycopy(data, 0, buffer, wp, status[1]);
				System.arraycopy(data, status[1], buffer, 0, realLen - status[1]);
				wp = realLen - status[1];
			}
			return realLen;
		}
		
		//NOTE: don't set buffer to null!
		public synchronized void reset() {
			wp = 0;
			rp = 0;
		}
	}
	
	public static boolean isValidIp(String ip){
		String reg = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	public static boolean isVliadPort(String port){
		if(null == port || port.trim().equals("")){
			return false;
		}
			
		int p = 0;
		try {
			p = Integer.parseInt(port);
			if (p > 65535 || p <= 0) {
				return false;
			}
		}
		catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean isVliadDownloadId(String downloadId) {
		if(null==downloadId || downloadId.trim().equals("")){
			return false;
		}
		if(downloadId.length() != 8){
			return false;
		}
		return true;
	}

	/**
	 * 判断网络是否可用
	 * @param context
	 * @param flag 是否支持移动网络
     * @return
     */
	public static boolean isNetworkAvailable(Context context, Boolean flag) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();


			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					LogUtils.d(i + "===状态===" + networkInfo[i].getState());
					LogUtils.d(i + "===类型===" + networkInfo[i].getTypeName());

					if (flag){
						// 判断当前网络状态是否为连接状态
						if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}else {
						// 判断当前wifi网络状态是否为连接状态
						if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED &&
								networkInfo[i].getTypeName().equals("WIFI") ||
								networkInfo[i].getTypeName().equals("wifi")) {
							return true;
						}
					}

				}
			}

			Toast.makeText(context,"请检查网络", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	public static boolean isBluetoothAvailable() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			return false;
		}
		if (!btAdapter.isEnabled()) {
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                getContext().startActivity(intent);
			LogUtils.d("蓝牙不可用");
			return false;
		} else {
			LogUtils.d("蓝牙可用");
			return true;
		}
	}
}
