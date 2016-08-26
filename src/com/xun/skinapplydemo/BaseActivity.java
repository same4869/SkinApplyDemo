package com.xun.skinapplydemo;

import java.util.List;

import android.app.Activity;

import com.wenba.bangbang.skin.ISkinUpate;
import com.wenba.bangbang.skin.SkinApplyHandler;
import com.wenba.bangbang.skin.SkinApplyHandler.CustomValue;
import com.wenba.bangbang.skin.SkinPackageManager;

public class BaseActivity extends Activity implements ISkinUpate {
	private List<CustomValue> customValues;
	private String baseLastThemeId;

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		//updateTheme();
	}
	
	public void onResume() {
		super.onResume();
		updateTheme();
	}

	protected SkinPackageManager getSkinPackageManager() {
		return SkinPackageManager.getInstance(this);
	}

	public List<CustomValue> getSkinCustomValues() {
		return customValues;
	}

	@Override
	public boolean updateTheme() {
		if (!applyTheme()) {
			return false;
		}
		if (isThemeApplied()) {
			return false;
		}
		SkinPackageManager skinManager = SkinPackageManager.getInstance(getApplicationContext());
		baseLastThemeId = skinManager.getCurSkinTheme();

		customValues = SkinApplyHandler.applySkin(this, this.getClass().getSimpleName(), getWindow().getDecorView());
		return true;
	}

	/**
	 * 主题是否已应用
	 * 
	 * @return
	 */
	public boolean isThemeApplied() {
		SkinPackageManager skinManager = SkinPackageManager.getInstance(getApplicationContext());
		if (skinManager.getCurSkinTheme().equals(baseLastThemeId)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean applyTheme() {
		return true;
	}

}
