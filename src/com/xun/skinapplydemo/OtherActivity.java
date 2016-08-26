package com.xun.skinapplydemo;

import com.wenba.bangbang.skin.SkinPackageManager;
import com.wenba.bangbang.skin.SkinPackageManager.LoadSkinCallBack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class OtherActivity extends BaseActivity implements OnClickListener {
	private Button setDefaultBtn, setDarkBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other);

		initView();
	}

	private void initView() {
		setDefaultBtn = (Button) findViewById(R.id.skin_default_theme_btn);
		setDarkBtn = (Button) findViewById(R.id.skin_new_theme_btn);
		setDefaultBtn.setOnClickListener(this);
		setDarkBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.skin_default_theme_btn:
			SkinPackageManager.getInstance(getApplicationContext()).resetDefaultSkin(new LoadSkinCallBack() {

				@Override
				public void loadSkinSuccess() {
					Log.d("kkkkkkkk", "loadSkinSuccess");
					updateTheme();
				}

				@Override
				public void loadSkinFail() {
					Log.d("kkkkkkkk", "loadSkinFail");
				}
			});
			break;
		case R.id.skin_new_theme_btn:
			SkinPackageManager.getInstance(getApplicationContext()).loadSkin("SkinApple", new LoadSkinCallBack() {

				@Override
				public void loadSkinSuccess() {
					Log.d("kkkkkkkk", "loadSkinSuccess");
					updateTheme();
				}

				@Override
				public void loadSkinFail() {
					Log.d("kkkkkkkk", "loadSkinFail");
				}

			});
			break;
		default:
			break;
		}
	}
}
