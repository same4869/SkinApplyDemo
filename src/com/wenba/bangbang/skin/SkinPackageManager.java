package com.wenba.bangbang.skin;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.util.Log;

import com.wenba.bangbang.skin.SkinApplyHandler.CustomValue;
import com.wenba.bangbang.skin.sp.SPSetting;
import com.wenba.bangbang.skin.util.AppInfoUtils;

@SuppressLint("SdCardPath")
public class SkinPackageManager {
	public static final String tag = SkinPackageManager.class.getSimpleName();

	public static final String DEFAULT_THEME = "default";

	public static final String SKIN_PLUG_PACKAGE = "com.wenba.theme.plug";

	public static final String SKIN_SETTING = "skin_setting";

	private static final String SKIN_DIR = "theme";

	private String themeRootPath = null;

	private static SkinPackageManager mInstance;
	private Context mContext;

	private static Object lock = new Object();

	public String curTheme = null;

	private String curSpecialTheme = null;

	/**
	 * 皮肤资源
	 */
	public Resources mResources;

	/**
	 * 特殊主题资源
	 */
	public Resources specialThemeResources;

	public String getSkinApkDownloadPath(final String apkName, int version) {
		return new StringBuffer(themeRootPath).append("/").append(apkName)
				.append("-").append(version).append(".apk").toString();

	}

	public String getSkinApkDownloadTempPath(final String apkName, int version) {
		return new StringBuffer(themeRootPath).append("/").append(apkName)
				.append("-").append(version).append(".temp").toString();

	}

	/**
	 * 获得皮肤资源的绝对路径
	 * 
	 * @param apkName
	 * @return
	 */
	public String loadSkinPackagePath(final String skinId) {
		File dir = new File(themeRootPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File[] apkFiles = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (!pathname.isFile()) {
					return false;
				}

				if (!pathname.getName().startsWith(skinId)) {
					return false;
				}

				return true;
			}
		});

		// load and delete old files if need
		if (apkFiles == null || apkFiles.length == 0) {
			return null;
		}

		if (apkFiles.length == 1) {
			return apkFiles[0].getName().matches(".*\\.apk") ? apkFiles[0]
					.getAbsolutePath() : null;
		} else {
			int version = parseApkVersion(apkFiles[0].getName());
			File target = apkFiles[0];

			for (File file : apkFiles) {
				int flagVer = parseApkVersion(file.getName());
				if (flagVer > version) {
					version = flagVer;

					target.delete();
					target = file;
				}
			}

			return target.getAbsolutePath();
		}
	}

	private int parseApkVersion(String name) {
		Pattern pattern = Pattern.compile("-(\\d+)\\.apk");

		Matcher matcher = pattern.matcher(name);
		if (matcher.find()) {
			String version = matcher.group(1);

			int verInt = 0;

			try {
				verInt = Integer.parseInt(version);
			} catch (Exception e) {
				// TODO: handle exception
			}

			return verInt;

		}

		return 0;
	}

	/**
	 * 检查有没有下载完成的主题文件并重命名
	 * 
	 * @return
	 */
	public boolean checkTempThemeFiles() {
		File dir = new File(themeRootPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File[] tempFiles = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (!pathname.isFile()) {
					return false;
				}

				if (!pathname.getName().endsWith(".temp")) {
					return false;
				}

				return true;
			}
		});

		if (tempFiles == null || tempFiles.length == 0) {
			return true;
		}
		boolean renameFailed = false;
		for (File file : tempFiles) {
			PackageInfo mInfo = getSkinPackageInfo(file.getAbsolutePath());
			if (mInfo != null) {
				File apkFile = new File(file.getAbsolutePath().replace(".temp",
						".apk"));
				if (!file.renameTo(apkFile)) {
					try {
						FileUtils.deleteQuietly(apkFile);
						FileUtils.copyFile(file, apkFile);
						FileUtils.deleteQuietly(file);
					} catch (IOException e) {
						renameFailed = true;
						Log.w("wenba", e);
					}
				}
			}
		}

		return !renameFailed;
	}

	/**
	 * 删除无效的特殊主题包
	 * 
	 * @param themeIdList
	 */
	public void deleteInvalidSpecialThemeApks(ArrayList<String> themeIdList) {
		File dir = new File(themeRootPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (!pathname.isFile()) {
					return false;
				}

				if (!pathname.getName().startsWith("special")) {
					return false;
				}

				return true;
			}
		});
		for (File file : files) {
			if (themeIdList.contains(file.getName().split("-")[0])) {
				continue;
			}
			FileUtils.deleteQuietly(file);
		}
	}

	private SkinPackageManager(Context mContext) {
		this.mContext = mContext;
		try {
			File file = mContext.getFilesDir();
			if (file != null) {
				file = new File(file, SKIN_DIR);
				if (!file.exists()) {
					file.mkdirs();
				}
				themeRootPath = file.getAbsolutePath();
			}
		} catch (Exception e) {

		}
	}

	public static SkinPackageManager getInstance(Context mContext) {
		if (mInstance == null) {
			synchronized (lock) {
				if (mInstance == null) {
					mInstance = new SkinPackageManager(mContext);
				}
			}
		}
		return mInstance;
	}

	public Resources getThemeResources() {
		if (mResources == null) {
			synchronized (this) {
				if (mResources == null) {
					mResources = mContext.getResources();
					curTheme = DEFAULT_THEME;
				}
			}
		}
		return mResources;
	}

	public Resources getThemeResources(boolean specialTheme) {
		if (DEFAULT_THEME.equals(curTheme) && specialTheme
				&& specialThemeResources != null) {// 只有在当前主题为默认主题时加载首页的特殊主题
			return specialThemeResources;
		} else {
			return getThemeResources();
		}
	}

	public String getCurSkinTheme() {
		if (curTheme == null) {
			curTheme = SPSetting.getAppSkinTheme();
		}
		if (curTheme == null) {
			curTheme = DEFAULT_THEME;
		}
		return curTheme;
	}

	public void resetDefaultSkin(LoadSkinCallBack callback) {
		Resources resources = mContext.getResources();
		if (resources != null) {
			mResources = resources;
			curTheme = DEFAULT_THEME;
			SPSetting.saveAppSkinTheme(DEFAULT_THEME);
			if (callback != null) {
				callback.loadSkinSuccess();
			}
		} else {
			if (callback != null) {
				callback.loadSkinFail();
			}
		}
	}

	public Bitmap getThemeBitamp(String drawableName) {
		Resources resources = getThemeResources();

		int resId = getThemeResourceId(drawableName, "drawable");
		if (resId <= 0) {
			return null;
		}
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeResource(resources, resId);
		} catch (OutOfMemoryError e) {
			Log.w("wenba", e);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
		return bm;
	}

	public Drawable getThemeDrawable(String drawableName) {
		return getThemeDrawable(drawableName, false);
	}

	public Drawable getThemeDrawable(CustomValue item) {
		boolean isSpecial = false;
		try {
			isSpecial = Boolean.parseBoolean(item.special);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
		return getThemeDrawable(item.value, isSpecial);
	}

	public Drawable getThemeDrawable(String drawableName, boolean isSpecial) {
		Resources resources = getThemeResources(isSpecial);

		int resId = getThemeResourceId(drawableName, "drawable", isSpecial);
		if (resId <= 0) {
			return null;
		}
		try {
			return resources.getDrawable(resId);
		} catch (OutOfMemoryError e) {
			Log.w("wenba", e);
			Log.e("skin", "OutOfMemoryError: drawableName = " + drawableName);
		}
		return null;
	}

	public ConstantState getThemeDrawableState(String drawableName) {
		return getThemeDrawableState(drawableName, false);
	}

	public ConstantState getThemeDrawableState(String drawableName,
			boolean isSpecial) {
		Drawable drawable = getThemeDrawable(drawableName);
		if (drawable != null) {
			return drawable.getConstantState();
		}
		return null;
	}

	public String getThemeString(String stringName) {
		Resources resources = getThemeResources();

		int resId = getThemeResourceId(stringName, "string");
		if (resId <= 0) {
			return null;
		}

		return resources.getString(resId);
	}

	public int getThemeColor(String colorName) {
		return getThemeColor(colorName, false);
	}

	public int getThemeColor(CustomValue item) {
		boolean isSpecial = false;
		try {
			isSpecial = Boolean.parseBoolean(item.special);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
		return getThemeColor(item.value, isSpecial);
	}

	public int getThemeColor(String colorName, boolean isSpecial) {
		Resources resources = getThemeResources(isSpecial);

		int resId = getThemeResourceId(colorName, "color", isSpecial);
		if (resId <= 0) {
			return -1;
		}

		return resources.getColor(resId);
	}

	public int getThemeResourceId(String name, String type) {
		return getThemeResourceId(name, type, false);
	}

	private int getThemeResourceId(String name, String type, boolean isSpecial) {
		Resources resources = getThemeResources(isSpecial);

		String pkgName = DEFAULT_THEME.equals(curTheme)
				&& (!isSpecial || specialThemeResources == null) ? mContext
				.getPackageName() : SkinPackageManager.SKIN_PLUG_PACKAGE;

		int resId = resources.getIdentifier(name, type, pkgName);
		if (resId <= 0) {
			Log.d("skin", "no resource found: type = " + type + ", name = "
					+ name);
		}

		return resId;
	}

	public Object getThemeResource(CustomValue item) {
		boolean isSpecial = false;
		try {
			isSpecial = Boolean.parseBoolean(item.special);
		} catch (Exception e) {
			Log.w("wenba", e);
		}
		Resources resources = getThemeResources(isSpecial);
		int resId = getThemeResourceId(item.value, item.type, isSpecial);
		if ("color".equals(item.type)) {
			if (resId <= 0) {
				return -1;
			}
			return resources.getColor(resId);
		} else if ("drawable".equals(item.type)) {
			if (resId <= 0) {
				return null;
			}
			try {
				return resources.getDrawable(resId);
			} catch (OutOfMemoryError e) {
				Log.w("wenba", e);
				Log.e("skin", "OutOfMemoryError: drawableName = "
						+ item.value);
			}
		}
		return null;
	}

	/**
	 * 根据皮肤Id获得皮肤版本号
	 * 
	 * @param themeId
	 * @return
	 */
	public int getSkinPackageVersionCode(String themeId) {
		if (themeId == null) {
			return -1;
		}
		String dexPath = loadSkinPackagePath(themeId);
		PackageInfo mInfo = getSkinPackageInfo(dexPath);
		if (mInfo == null) {
			return -1;
		}
		if (themeId.equals(mInfo.packageName)) {
			return -1;
		}
		return mInfo.versionCode;
	}

	private PackageInfo getSkinPackageInfo(String dexPath) {
		if (dexPath == null) {
			return null;
		}
		String sign = AppInfoUtils.getApkSignature(mContext, dexPath);
		String localSign = "A6:08:A8:28:1A:BF:55:AF:A4:72:9A:F4:F6:34:06:CF:16:EF:BB:A2";
		if (sign == null || (localSign != null && localSign.equals(sign))) {// 对主题包进行签名认证
			PackageManager mPm = mContext.getPackageManager();
			PackageInfo mInfo = mPm.getPackageArchiveInfo(dexPath, 0);
			return mInfo;
		}
		return null;
	}

	/**
	 * 加载皮肤资源, 同步加载
	 * 
	 * @param dexPath
	 *            需要加载的皮肤资源
	 * @param callback
	 *            回调接口
	 */
	public void loadSkin(String themeId, final LoadSkinCallBack callback) {

		// 当为默认皮肤时，加载本地资源
		if (DEFAULT_THEME.equals(themeId)) {
			resetDefaultSkin(callback);
			return;
		}

		Resources resources = loadSkinById(themeId);
		if (resources != null) {
			curTheme = themeId;
			mResources = resources;
			if (callback != null) {
				callback.loadSkinSuccess();
			}
		} else {
			if (callback != null) {
				callback.loadSkinFail();
			}
		}

	}

	public String getCurSpecialTheme() {
		return curSpecialTheme;
	}

	/**
	 * 恢复首页主题为默认
	 */
	public void cancelSpecialTheme(LoadSkinCallBack callback) {
		curSpecialTheme = null;
		specialThemeResources = null;
		if (callback != null) {
			callback.loadSkinSuccess();
		}
	}

	/**
	 * 加载特殊皮肤资源
	 * 
	 * @param themeId
	 * @param callback
	 */
	public void loadSpecialSkin(String themeId, final LoadSkinCallBack callback) {
		Resources resources = loadSkinById(themeId);
		if (resources != null) {
			curSpecialTheme = themeId;
			specialThemeResources = resources;
			if (callback != null) {
				callback.loadSkinSuccess();
			}
		} else {
			if (callback != null) {
				callback.loadSkinFail();
			}
		}
	}

	private Resources loadSkinById(String themeId) {
		Resources resources = null;
		String dexPath = loadSkinPackagePath(themeId);
		PackageInfo mInfo = getSkinPackageInfo(dexPath);
		if (mInfo == null) {
			return resources;
		}

		AssetManager assetManager;
		try {
			assetManager = AssetManager.class.newInstance();

			Method addAssetPath = assetManager.getClass().getMethod(
					"addAssetPath", String.class);
			addAssetPath.invoke(assetManager, dexPath);

			Resources superRes = mContext.getResources();
			resources = new Resources(assetManager,
					superRes.getDisplayMetrics(), superRes.getConfiguration());
		} catch (Exception e) {
			Log.w("wenba", e);
		}
		return resources;
	}

	/**
	 * 加载资源的回调接口
	 * 
	 */
	public static interface LoadSkinCallBack {
		public void loadSkinSuccess();

		public void loadSkinFail();
	}
}
