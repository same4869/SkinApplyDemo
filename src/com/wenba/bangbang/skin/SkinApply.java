package com.wenba.bangbang.skin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.wenba.bangbang.skin.SkinApplyHandler.ConfigItem;
import com.wenba.bangbang.skin.SkinApplyHandler.CustomValue;
import com.wenba.bangbang.skin.SkinApplyHandler.MethodCall;
import com.wenba.bangbang.skin.SkinApplyHandler.ThemeConfig;

public class SkinApply {
	private SkinPackageManager mSkinPackageManager;
	private String skinConfigName;
	private ThemeConfig commConfig;
	private Context context;
	private View rootView;

	public SkinApply(Context context, String skinConfigName, ThemeConfig commConfig, View rootView) {
		this.context = context;
		this.rootView = rootView;
		this.skinConfigName = skinConfigName;
		this.commConfig = commConfig;
		this.mSkinPackageManager = SkinPackageManager.getInstance(context);
	}

	public List<CustomValue> apply() {
		ThemeConfig thisConfig = null;
		try {
			// get specific name
			String fileName = "skin/config/" + skinConfigName + ".xml";
			thisConfig = SkinApplyHandler.parseThemeAsset(context, fileName);
			if (thisConfig == null) {
				Log.d("skin", "no config skinConfigName = " + skinConfigName);
				// apply common config
				applyThemeConfig(commConfig);
				return null;
			}

			if (thisConfig.applyCommonAttrbiute) {
				applyThemeConfig(commConfig);
			}

			applyThemeConfig(thisConfig);

		} catch (Exception e) {
			Log.w("wenba", e);
		}

		return thisConfig.customValueList;
	}

	/**
	 * 对所有类别的配置参数与对应的组件进行匹配
	 * 
	 * @param themeConfig
	 * @throws Exception
	 */
	private void applyThemeConfig(ThemeConfig themeConfig) throws Exception {

		if (themeConfig == null) {
			return;
		}

		// colors
		List<ConfigItem> colorArray = themeConfig.colorList;

		if (colorArray != null && colorArray.size() > 0) {
			try {
				applyThemeColors(colorArray);
			} catch (Exception e) {
				Log.w("wenba", e);
			}
		}

		// drawables
		List<ConfigItem> drawableArray = themeConfig.drawableList;
		if (drawableArray != null && drawableArray.size() > 0) {
			try {
				applyThemeDrawables(drawableArray);
			} catch (Exception e) {
				Log.w("wenba", e);
			}
		}

		// strings
		List<ConfigItem> stringArray = themeConfig.stringList;
		if (stringArray != null && stringArray.size() > 0) {
			try {
				applyThemeStrings(stringArray);
			} catch (Exception e) {
				Log.w("wenba", e);
			}
		}
	}

	/**
	 * 对color配置参数进行组件与参数的匹配组合
	 * 
	 * @param colorArray
	 * @throws Exception
	 */
	private void applyThemeColors(List<ConfigItem> colorArray) throws Exception {
		applyConfigGroup(colorArray, new ApplyConfigListener() {

			@Override
			public void applyMethodCall(View view, MethodCall call) throws NoSuchMethodException,
					IllegalAccessException, IllegalArgumentException, InvocationTargetException {

				String methodName = call.methodName;
				String resName = call.resName;
				boolean isSpecial = false;
				try {
					isSpecial = Boolean.parseBoolean(call.special);
				} catch (Exception e) {
					Log.w("wenba", e);
				}
				int color = mSkinPackageManager.getThemeColor(resName, isSpecial);

				Method method = view.getClass().getMethod(methodName, new Class[] { int.class });
				if (method == null) {
					return;
				}

				method.setAccessible(true);
				method.invoke(view, color);
			}
		});
	}

	/**
	 * 对drawable配置参数进行组件与参数的匹配组合
	 * 
	 * @param drawableArray
	 * @throws Exception
	 */
	private void applyThemeDrawables(List<ConfigItem> drawableArray) throws Exception {
		applyConfigGroup(drawableArray, new ApplyConfigListener() {

			@Override
			public void applyMethodCall(View view, MethodCall call) throws NoSuchMethodException,
					IllegalAccessException, IllegalArgumentException, InvocationTargetException {

				String methodName = call.methodName;
				String resName = call.resName;
				boolean isSpecial = false;
				try {
					isSpecial = Boolean.parseBoolean(call.special);
				} catch (Exception e) {
					Log.w("wenba", e);
				}

				Drawable drawable = mSkinPackageManager.getThemeDrawable(resName, isSpecial);
				if (drawable == null) {
					return;
				}
				int paddingLeft = 0;
				int paddingTop = 0;
				int paddingRight = 0;
				int paddingBottom = 0;

				boolean needPaddingSet = drawable.getPadding(new Rect());

				if (needPaddingSet) {
					paddingLeft = view.getPaddingLeft();
					paddingTop = view.getPaddingTop();
					paddingRight = view.getPaddingRight();
					paddingBottom = view.getPaddingBottom();

					if (paddingLeft == 0 && paddingTop == 0 && paddingRight == 0 && paddingBottom == 0) {
						needPaddingSet = false;
					}
				}

				if ("setBackgroundDrawable".equals(methodName)) {
					if (android.os.Build.VERSION.SDK_INT >= 16) {
						methodName = "setBackground";
					}
				}

				Method method = view.getClass().getMethod(methodName, new Class[] { Drawable.class });
				if (method == null) {
					return;
				}

				method.setAccessible(true);
				method.invoke(view, drawable);

				if (needPaddingSet) {
					view.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
				}

			}
		});
	}

	/**
	 * 对string配置参数进行组件与参数的匹配组合
	 * 
	 * @param stringArray
	 * @throws Exception
	 */
	private void applyThemeStrings(List<ConfigItem> stringArray) throws Exception {
		applyConfigGroup(stringArray, new ApplyConfigListener() {

			@Override
			public void applyMethodCall(View view, MethodCall call) throws NoSuchMethodException,
					IllegalAccessException, IllegalArgumentException, InvocationTargetException {

				String methodName = call.methodName;
				String resName = call.resName;

				String str = mSkinPackageManager.getThemeString(resName);
				if (str == null) {
					return;
				}

				Method method = view.getClass().getMethod(methodName, new Class[] { CharSequence.class });
				if (method == null) {
					return;
				}

				method.setAccessible(true);
				method.invoke(view, str);

			}
		});
	}

	private static interface ApplyConfigListener {
		public void applyMethodCall(View view, MethodCall call) throws NoSuchMethodException, IllegalAccessException,
				IllegalArgumentException, InvocationTargetException;
	}

	private void applyConfigGroup(List<ConfigItem> configList, ApplyConfigListener listener) {
		if (configList == null) {
			return;
		}

		for (int i = 0; i < configList.size(); i++) {
			ConfigItem itemObject = configList.get(i);

			String idName = itemObject.idName;

			if (idName == null) {
				continue;
			}

			int viewId = getViewId(idName);
			if (viewId <= 0) {
				Log.d("skin", "applyConfigGroup: no viewId found name = " + idName + " skinConfigName = "
						+ skinConfigName);
				continue;
			}

			View view = rootView.findViewById(viewId);
			if (view == null) {
				Log.d("skin", "applyConfigGroup: no view found viewId = " + idName + " skinConfigName = "
						+ skinConfigName);
				continue;
			}

			for (MethodCall call : itemObject.methodCalls) {
				try {
					listener.applyMethodCall(view, call);
				} catch (NoSuchMethodException e) {
					Log.w("wenba", e);
				} catch (IllegalAccessException e) {
					Log.w("wenba", e);
				} catch (IllegalArgumentException e) {
					Log.w("wenba", e);
				} catch (InvocationTargetException e) {
					Log.w("wenba", e);
				} catch (Exception e) {
					Log.w("wenba", e);
				}
			}
		}
	}

	private int getViewId(String idName) {
		return context.getResources().getIdentifier(idName, "id", context.getPackageName());
	}

}
