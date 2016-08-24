package com.wenba.bangbang.skin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

public class AppInfoUtils {
	/**
	 * 获得apk签名
	 * 
	 * @param context
	 * @param apkPath
	 * @return 当签名无法获得，返回null
	 */
	public static String getApkSignature(Context context, String apkPath) {
		if (context == null) {
			return "";
		}
		PackageManager pm = context.getPackageManager();
		if (pm == null) {
			return "";
		}
		PackageInfo mInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES);
		if (mInfo != null) {
			if (mInfo.signatures != null && mInfo.signatures.length > 0) {
				Signature signatures = mInfo.signatures[0];
				try {
					MessageDigest md2 = MessageDigest.getInstance("SHA1");
					md2.update(signatures.toByteArray());
					byte[] digest2 = md2.digest();
					return toHexString(digest2);
				} catch (NoSuchAlgorithmException e) {
					Log.w("wenba", e);
				}
			} else {
				return null;// 当为null时，无法获得签名
			}
		}
		return "";
	}
	
	private static String toHexString(byte[] block) {
		StringBuffer buf = new StringBuffer();
		int len = block.length;
		for (int i = 0; i < len; i++) {
			byte2hex(block[i], buf);
			if (i < len - 1) {
				buf.append(":");
			}
		}
		return buf.toString();
	}
	
	private static void byte2hex(byte b, StringBuffer buf) {
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(hexChars[high]);
		buf.append(hexChars[low]);
	}
}
