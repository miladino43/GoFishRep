package com.linus.gofish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class GoldFishView extends View {
	private Context myContext;
	private List<Card> deck = new ArrayList<Card>();
	private List<Card> myHand = new ArrayList<Card>(); 
	private List<Card> oppHand = new ArrayList<Card>(); 
	private int scaledCardW;
	private int scaledCardH;
	private static int screenW;
	private static int screenH;
	private float scale;
	private Paint whitePaint;
	private int oppScore; 
	private int myScore;
	private Bitmap cardBack;
	private boolean myTurn;
	private int movingCardIdx = -1; 
	private int movingX;
	private int movingY;
	private int validRank;
	private Bitmap nextCardButton;
	private boolean oppDiscard;
	private Bitmap chooseRankButton;
	private boolean chooseRankButtonPressed;
	private ComputerPlayer computerPlayer = new ComputerPlayer();
	
	public GoldFishView(Context context) {
		super(context);
		myContext= context;
		scale = myContext.getResources().getDisplayMetrics().density;
		whitePaint = new Paint();
		whitePaint.setAntiAlias(true); 
		whitePaint.setColor(Color.BLACK); 
		whitePaint.setStyle(Paint.Style.STROKE); 
		whitePaint.setTextAlign(Paint.Align.LEFT); 
		whitePaint.setTextSize(scale*15);
		
		
		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
		Bitmap tempBitmap = BitmapFactory.decodeResource
				(myContext.getResources(),
				R.drawable.card_back); 
		scaledCardW = (int) (screenW/7);
		scaledCardH = (int) (scaledCardW*1.3); 
		cardBack = Bitmap.createScaledBitmap
				(tempBitmap, scaledCardW, scaledCardH,false);
		Bitmap tempBitmap2 = BitmapFactory.decodeResource
				(myContext.getResources(),
				R.drawable.pink_button); 
		chooseRankButton = Bitmap.createScaledBitmap
				(tempBitmap2, screenW/5, screenW/10,false);
		nextCardButton = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_next); 

		initCards();//load card images
		dealCards();//set the players hands
		myTurn = new Random().nextBoolean();
		oppDiscard = true;
		if(!myTurn){
			makeComputerPlay();
		}
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		canvas.drawText("Computer Score:" + Integer.toString(oppScore), 
				10, whitePaint.getTextSize() + 10, whitePaint);
		canvas.drawText("Your Score:" + Integer.toString(myScore), 
				10, screenH-whitePaint.getTextSize()-10, whitePaint);
		
		//Draw computer hand
		for (int i = 0; i < oppHand.size(); i++) {
			
			canvas.drawBitmap(cardBack, i*(scaledCardW-50),whitePaint.getTextSize()+(50*scale), null);
		
		
		}
		//Draw Pile as a single card back
		canvas.drawBitmap(cardBack, (screenW/2)-cardBack.getWidth()-10,
				(screenH/2)-(cardBack.getHeight()/2), null);
		canvas.drawBitmap(chooseRankButton, 0,
				(int)(screenH*0.7), null);
		canvas.drawText("Choose Rank", 
				30, (float) (screenH*0.735), whitePaint);
		//draw next arrow if hand i bigger than 7
		if (myHand.size() > 7) {
			canvas.drawBitmap(nextCardButton, 
					screenW-nextCardButton.getWidth()-(30*scale), 
					screenH-nextCardButton.getHeight()-scaledCardH-(90*scale), 
					null);
		}
		//Draw player hand
		for (int i = 0; i < myHand.size(); i++) {
			if (i == movingCardIdx) { 
				canvas.drawBitmap(myHand.get(i).getBitmap(),
						movingX,movingY,null);
			}else{
				//only draw card if the index is less than 7
				if(i<7){
				canvas.drawBitmap(myHand.get(i).getBitmap(), i*(scaledCardW+5),screenH-scaledCardH-
						whitePaint.getTextSize()-(50*scale), null);
				}
			}
			
		}
		
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();
		int X = (int)event.getX();
		int Y = (int)event.getY();
		switch (eventaction ) {
		
		case MotionEvent.ACTION_DOWN: 
			if (myTurn) {
				for (int i = 0; i < 7; i++) {
					//check to see if the touched location is on a card--Has to be modified to so below card touch wont pickup card
					if (X > i*(scaledCardW+5) && X < i*(scaledCardW+5)
					+ scaledCardW &&
					Y > screenH-scaledCardH- whitePaint.getTextSize()- (50*scale)) { 
						movingCardIdx = i; 
						movingX = X-(int)(30*scale);//offset so you can see the card above finger
						movingY = Y-(int)(70*scale);//offset so you can see the card above finger
					} 
				}
				if (X > 0 && X < chooseRankButton.getWidth() &&
		        		Y > (int)(screenH*0.7) &&
		        		Y < (int)(screenH*0.7) +chooseRankButton.getHeight()) {
		        		chooseRankButtonPressed = true;
		        	}	    
			} 
			break;
			
			case MotionEvent.ACTION_MOVE:
				movingX = X-(int)(30*scale);//offset so you can see the card above finger
				movingY = Y-(int)(70*scale);//offset so you can see the card above finger
			break;
			
			case MotionEvent.ACTION_UP:
			if(movingCardIdx == -1 ){
				if(chooseRankButtonPressed){
					showChooseRankDialog();
					
				}
				chooseRankButtonPressed = false;
				
				
			}
			if (myHand.size() > 7 &&
	        		X > screenW-nextCardButton.getWidth()-(30*scale) &&
	        		Y > screenH-nextCardButton.getHeight()-scaledCardH-(90*scale) &&
	        		Y < screenH-nextCardButton.getHeight()-scaledCardH-(60*scale)) {
	        			Collections.rotate(myHand, 1);
	        	}
			movingCardIdx = -1;
				
				
			
			break; 
		}
			
		invalidate();
		return true;
			
	}

	
	//load all the cards from the drawable folder into the deck and set the image for each card
		private void initCards() {
			for (int i = 0; i < 4; i++) { //through suits 
				for (int j = 102; j < 115; j++) {//through face value 2-14
					int tempId = j + (i*100); 
					Card tempCard = new Card(tempId); 
					int resourceId = getResources().getIdentifier("card" + tempId, 
							"drawable", myContext.getPackageName());
					
					Bitmap tempBitmap = BitmapFactory.decodeResource(myContext.getResources(),
							resourceId);
					scaledCardW = (int) (screenW/7);
			        scaledCardH = (int) (scaledCardW*1.3);
					
					Bitmap scaledBitmap = Bitmap.createScaledBitmap(tempBitmap,
							scaledCardW, scaledCardH, false);
					
					tempCard.setBitmap(scaledBitmap);
					deck.add(tempCard); 
				}
			}
		}

		private void drawCard(List<Card> handToDraw) { 
		
			handToDraw.add(0, deck.get(0));
			deck.remove(0);
				if (deck.isEmpty()) { 
					//Dialogue shows Game Over Sign 
					} 
				
		}
		
		
		private void dealCards() {
		
			Collections.shuffle(deck,new Random());
			//give four cards to each player
				for (int i = 0; i < 4; i++) { 
					drawCard(myHand);
					drawCard(oppHand); 
				}
		}
		
		private void showChooseRankDialog(){
			
			final Dialog chooseRankDialog = new Dialog(myContext);
			chooseRankDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			chooseRankDialog.setContentView(R.layout.choose_rank_dialog);
			final Spinner rankSpinner = (Spinner) chooseRankDialog.findViewById(R.id.rankSpinner);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
					myContext, R.array.ranks, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			rankSpinner.setAdapter(adapter);
			Button okButton = (Button) chooseRankDialog.findViewById(R.id.okButton);
			okButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View view){
					validRank = (rankSpinner. getSelectedItemPosition()+1);
					String rankText=Integer.toString(validRank);
					chooseRankDialog.dismiss();
					oppDiscard = checkForValidRank(oppHand,validRank);
					if(!oppDiscard){
						drawCard(myHand); 
						Toast.makeText(myContext, "Go Fish!", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(myContext,"Score!!", Toast.LENGTH_SHORT).show();

					}
					myTurn = false;
					makeComputerPlay();
				}
			});
			chooseRankDialog.show();
	        
		}
		//go through opp hand to see if they have the card
		private boolean checkForValidRank(List hand ,int rank){
			return computerPlayer.possessCard(hand, rank);
		}
		public void makeComputerPlay(){
			
			int tempPlay = 0;
//			while (tempPlay == 0) {
				tempPlay = computerPlayer.makePlay(oppHand);
				int Rank = oppHand.get(tempPlay).getRank();
				showComputerQuestionDialog(Rank);
				
				myTurn = true;
//				}
		}
		
		private void showComputerQuestionDialog(int cardRank){
			
			final Dialog ComputerQuestionDialog = new Dialog(myContext);
			ComputerQuestionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			ComputerQuestionDialog.setContentView(R.layout.computer_question_dialog);
			Button confirmButton = (Button) ComputerQuestionDialog.findViewById(R.id.confirmButton);
			confirmButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(myContext, "CONFIRM!", Toast.LENGTH_SHORT).show();
					ComputerQuestionDialog.dismiss();
					
				}
			});
			Button declineButton = (Button) ComputerQuestionDialog.findViewById(R.id.declineButton);
			declineButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(myContext, "DECLINE!", Toast.LENGTH_SHORT).show();
					ComputerQuestionDialog.dismiss();

				}
			});
			TextView question = (TextView) ComputerQuestionDialog.findViewById(R.id.questionTextView);
			question.setText("Do You Have Any " + cardRank);
			
			ComputerQuestionDialog.show();
		}

}
