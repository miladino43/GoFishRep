package com.linus.gofish;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class TitleView extends View {
	private Context myContext;
	private Bitmap titlegraphic;
	private Bitmap playButtonUp;
	private Bitmap playButtonDown;
	private boolean playButtonPressed;
	private int screenW;
	private int screenH;
	
	public TitleView(Context context) {
		super(context);
		myContext = context;
		titlegraphic = BitmapFactory.decodeResource(getResources(), 
				R.drawable.title_gofish);
		playButtonUp = BitmapFactory.decodeResource(getResources(), 
				R.drawable.play_button_up);
		playButtonDown = BitmapFactory.decodeResource(getResources(), 
				R.drawable.play_button_down);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(titlegraphic, (screenW - titlegraphic.getWidth())/2, (screenH - titlegraphic.getHeight())/2, null);
		if (playButtonPressed) {
			canvas.drawBitmap(playButtonDown, (screenW-playButtonUp.getWidth())/2, (int)(screenH*0.7), null);	
		} else {
			canvas.drawBitmap(playButtonUp, (screenW-playButtonUp.getWidth())/2, (int)(screenH*0.7), null);			
		}	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//handle user interaction with the screen
		int eventaction = event.getAction();
		int X = (int)event.getX();
		int Y = (int)event.getY();
		
		switch(eventaction){
		
		case MotionEvent.ACTION_DOWN:
			if (X > (screenW-playButtonUp.getWidth())/2 &&
	        		X < ((screenW-playButtonUp.getWidth())/2) + playButtonUp.getWidth() &&
	        		Y > (int)(screenH*0.7) &&
	        		Y < (int)(screenH*0.7) + playButtonUp.getHeight()) {
	        		playButtonPressed = true;
	        	}	        		
	        	break;
		
		case MotionEvent.ACTION_UP:
			if (playButtonPressed) {
				Intent gameIntent = new Intent(myContext, GameActivity.class); 
				myContext.startActivity(gameIntent); 
				}
			playButtonPressed = false;
			break;
			
		case MotionEvent.ACTION_MOVE:
			break;
		}
		invalidate();
		return true;
		
		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
	}
	
	

    
}
