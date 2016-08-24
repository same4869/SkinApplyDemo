package com.wenba.bangbang.skin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import com.wenba.bangbang.skin.util.BaseStoreUtil;

import android.content.Context;
import android.util.Log;
import android.util.Xml;
import android.view.View;

public class SkinApplyHandler {
	public static final String tag = SkinApplyHandler.class.getSimpleName();

	private static ThemeConfig sCommonConfig = null;

	public static class ConfigItem {
		public String idName;
		public List<MethodCall> methodCalls;

		public ConfigItem() {
			methodCalls = new ArrayList<MethodCall>();
		}

		public void addMethodCall(MethodCall call) {
			methodCalls.add(call);
		}

		public void addMethodCall(List<MethodCall> calls) {
			methodCalls.addAll(calls);
		}
	}

	public static class MethodCall {
		public String methodName;
		public String resName;
		public String special;
	}

	public static class CustomValue {
		public String name;
		public String value;
		public String type;
		public String special;
	}

	public static class ThemeConfig {

		public static final String NODE_THEME_ROOT = "ThemeRoot";
		public static final String NODE_COLORS = "colors";
		public static final String NODE_DRAWABLES = "drawables";
		public static final String NODE_STRINGS = "strings";
		public static final String NODE_APPLY_COMMON_ATTRIBIUTE = "applyCommonAttrbiute";
		public static final String NODE_CONFIG_ITEM = "ConfigItem";
		public static final String NODE_METHOD_CALL = "MethodCall";
		public static final String NODE_CUSTOM_VALUE = "CustomValue";
		public static final String NODE_VALUE_DEF = "ValueDef";

		public static final String ATTR_VIEW_ID = "viewId";
		public static final String ATTR_METHOD = "method";
		public static final String ATTR_RES_NAME = "resName";
		public static final String ATTR_SPECIAL_THEME = "specialTheme";
		public static final String ATTR_NAME = "name";
		public static final String ATTR_VALUE = "value";
		public static final String ATTR_TYPE = "type";

		public boolean applyCommonAttrbiute;
		public List<ConfigItem> colorList;
		public List<ConfigItem> drawableList;
		public List<ConfigItem> stringList;
		public List<CustomValue> customValueList;
	}

	public static List<CustomValue> applySkin(Context context, String skinConfigName, View rootView) {
		if (sCommonConfig == null) {
			initCommonConfig(context);
		}

		return new SkinApply(context, skinConfigName, sCommonConfig, rootView).apply();
	}

	private static synchronized void initCommonConfig(Context context) {
		if (sCommonConfig != null) {
			return;
		}
		try {
			sCommonConfig = parseThemeAsset(context, "skin/config/Common.xml");
		} catch (Exception e) {
			Log.w("wenba", e);
		} finally {
			// close input stream use app util
		}
	}

	/**
	 * 解析ConfigItem 节点，转化为ConfigItem集合
	 * 
	 * @param parser
	 * @return
	 */
	private static List<ConfigItem> parseConfigGroup(XmlPullParser parser) {

		if (parser == null || parser.getDepth() != 2) {
			return null;
		}

		int event = -1;
		int depth = parser.getDepth();
		List<ConfigItem> itemList = new ArrayList<ConfigItem>();
		ConfigItem configItem = null;
		try {
			while ((event = parser.next()) != XmlPullParser.END_TAG || (parser.getDepth() > depth && event != XmlPullParser.END_DOCUMENT)) {
				if (event != XmlPullParser.START_TAG) {
					continue;
				}

				String nodeTag = parser.getName();
				if (ThemeConfig.NODE_CONFIG_ITEM.equals(nodeTag)) {
					configItem = new ConfigItem();

					configItem.idName = parser.getAttributeValue(null, ThemeConfig.ATTR_VIEW_ID);

					if (parser.getAttributeCount() >= 3) {
						MethodCall call = new MethodCall();
						call.methodName = parser.getAttributeValue(null, ThemeConfig.ATTR_METHOD);
						call.resName = parser.getAttributeValue(null, ThemeConfig.ATTR_RES_NAME);
						call.special = parser.getAttributeValue(null, ThemeConfig.ATTR_SPECIAL_THEME);

						configItem.addMethodCall(call);
					} else {
						// parse method group
						configItem.addMethodCall(parseMethodCalls(parser));
					}

					itemList.add(configItem);
				}
			}
		} catch (Exception e) {
			Log.w("wenba", e);
		}

		return itemList;
	}

	private static List<CustomValue> parseCustomValueGroup(XmlPullParser parser) {

		if (parser == null || parser.getDepth() != 2) {
			return null;
		}

		int event = -1;
		int depth = parser.getDepth();
		List<CustomValue> itemList = new ArrayList<CustomValue>();
		CustomValue configItem = null;
		try {
			while ((event = parser.next()) != XmlPullParser.END_TAG || (parser.getDepth() > depth && event != XmlPullParser.END_DOCUMENT)) {
				if (event != XmlPullParser.START_TAG) {
					continue;
				}

				String nodeTag = parser.getName();
				if (ThemeConfig.NODE_VALUE_DEF.equals(nodeTag)) {
					configItem = new CustomValue();

					if (parser.getAttributeCount() >= 2) {
						configItem.name = parser.getAttributeValue(null, ThemeConfig.ATTR_NAME);
						configItem.value = parser.getAttributeValue(null, ThemeConfig.ATTR_VALUE);
						configItem.type = parser.getAttributeValue(null, ThemeConfig.ATTR_TYPE);
						configItem.special = parser.getAttributeValue(null, ThemeConfig.ATTR_SPECIAL_THEME);
					}

					itemList.add(configItem);
				}
			}
		} catch (Exception e) {
			Log.w("wenba", e);
		}

		return itemList;
	}

	private static List<MethodCall> parseMethodCalls(XmlPullParser parser) {
		if (parser == null) {
			return null;
		}

		List<MethodCall> itemList = new ArrayList<MethodCall>();

		int event = -1;
		int depth = parser.getDepth();
		try {
			while ((event = parser.next()) != XmlPullParser.END_TAG || (parser.getDepth() > depth && event != XmlPullParser.END_DOCUMENT)) {
				if (event != XmlPullParser.START_TAG) {
					continue;
				}

				String nodeTag = parser.getName();
				if (ThemeConfig.NODE_METHOD_CALL.equals(nodeTag)) {

					if (parser.getAttributeCount() >= 2) {
						MethodCall call = new MethodCall();
						call.methodName = parser.getAttributeValue(null, ThemeConfig.ATTR_METHOD);
						call.resName = parser.getAttributeValue(null, ThemeConfig.ATTR_RES_NAME);

						itemList.add(call);
					}
				}
			}
		} catch (Exception e) {
			Log.w("wenba", e);
		}

		return itemList;

	}

	/**
	 * 格式皮肤配置xml，转化为ThemeConfig Bean
	 * 
	 * @param name
	 * @return
	 */
	public static ThemeConfig parseThemeAsset(Context context, String name) {
		InputStream inputStream = null;
		ThemeConfig themeConfig = null;
		try {
			inputStream = context.getResources().getAssets().open(name);
			if (inputStream == null) {
				return null;
			}

			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(inputStream, "utf-8");

			int event = -1;

			while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
				if (event != XmlPullParser.START_TAG) {
					continue;
				}

				String nodeTag = parser.getName();

				if (ThemeConfig.NODE_COLORS.equals(nodeTag)) {
					themeConfig.colorList = parseConfigGroup(parser);
				} else if (ThemeConfig.NODE_DRAWABLES.equals(nodeTag)) {
					themeConfig.drawableList = parseConfigGroup(parser);
				} else if (ThemeConfig.NODE_STRINGS.equals(nodeTag)) {
					themeConfig.stringList = parseConfigGroup(parser);
				} else if (ThemeConfig.NODE_CUSTOM_VALUE.equals(nodeTag)) {
					themeConfig.customValueList = parseCustomValueGroup(parser);
				} else if (ThemeConfig.NODE_APPLY_COMMON_ATTRIBIUTE.equals(nodeTag)) {
					String value = parser.getAttributeValue(null, "value");
					if (value != null && value.equalsIgnoreCase("true")) {
						themeConfig.applyCommonAttrbiute = true;
					} else {
						themeConfig.applyCommonAttrbiute = false;
					}
				} else if (ThemeConfig.NODE_THEME_ROOT.equals(nodeTag)) {
					if (parser.getDepth() == 1) {
						themeConfig = new ThemeConfig();
					} else {
						throw new Exception("ThemeRoot was in wrong place");
					}
				}
			}

			return themeConfig;
		} catch (Exception e) {
			Log.w("wenba", e);
		} finally {
			// close input stream
			BaseStoreUtil.closeObject(inputStream);
		}

		return themeConfig;
	}

}
