package com.linus.gofish;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {
	public static final boolean D = true;
	private GoldFishView myGoldFishView;
	private GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags
	(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.goldfish_layout);
		myGoldFishView = (GoldFishView) findViewById(R.id.gofish);
		gestureDetector = new GestureDetector(this, gestureListener);
		myGoldFishView.setKeepScreenOn(true); 
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}
SimpleOnGestureListener gestureListener = new SimpleOnGestureListener(){
	
	public boolean onDown(MotionEvent e) {
		if(D) Log.d("TAG", "onDown");
		return true;
	}
	public boolean onSingleTapConfirmed(MotionEvent e) {
		myGoldFishView.singleTapHandler(e);
		if(D) Log.d("TAG", "onsingle");
		return true;
	}
	
	public boolean onDoubleTap(MotionEvent e) {
		myGoldFishView.doubleTapHandler(e);
		if(D) Log.d("TAG", "onDouble");
		return true;
		
	}
	
	
};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


}


