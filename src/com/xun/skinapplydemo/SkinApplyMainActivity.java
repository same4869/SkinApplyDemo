package com.xun.skinapplydemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SkinApplyMainActivity extends BaseActivity implements OnClickListener {
	private Button nextPage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_skin_apply_main);

		initView();
	}

	private void initView() {
		nextPage = (Button) findViewById(R.id.next_page);
		nextPage.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.next_page:
			Intent intent = new Intent(SkinApplyMainActivity.this, OtherActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

}
