package com.linus.gofish;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GoldFishView gView = new GoldFishView(this);
		gView.setKeepScreenOn(true); 
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags
	(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(gView);
	}
}
