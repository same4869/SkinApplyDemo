package com.wenba.bangbang.skin.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class MyApplication extends Application {
	protected static MyApplication instance;

	public static MyApplication getInstance() {
		return (MyApplication) instance;
	}

	public SharedPreferences getWenbaSharedPreferences(String tbl) {
		return getSharedPreferences(tbl, Context.MODE_PRIVATE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
}
