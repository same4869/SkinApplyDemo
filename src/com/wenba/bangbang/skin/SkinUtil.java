package com.wenba.bangbang.skin;

public class SkinUtil {
	public static String configNameAppendViewTag(String className, String viewTag) {
		StringBuffer sb = new StringBuffer(className);
		sb.append("_").append(viewTag);
		return sb.toString();
	}
}
