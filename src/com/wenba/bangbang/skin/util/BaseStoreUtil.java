package com.wenba.bangbang.skin.util;

import java.io.Closeable;

import android.util.Log;

public class BaseStoreUtil {
	public static void closeObject(Closeable closeable) {
		if (closeable == null) {
			return;
		}

		try {
			closeable.close();
		} catch (Exception e) {
			Log.w("wenba", e);
		}
	}
}
