package com.wenba.bangbang.skin.sp;

public class SPSetting {
	public static final String SETTING = "setting";
	private static final String SKIN_THEME = "skin_theme";
	
	public static void saveAppSkinTheme(String theme) {
		PrefsMgr.putString(SETTING, SKIN_THEME, theme);
	}

	public static String getAppSkinTheme() {
		return PrefsMgr.getString(SETTING, SKIN_THEME, null);
	}
}
