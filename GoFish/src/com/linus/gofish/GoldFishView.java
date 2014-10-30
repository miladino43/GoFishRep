package com.linus.gofish;

import java.security.spec.MGF1ParameterSpec;
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
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class GoldFishView extends SurfaceView implements SurfaceHolder.Callback{
	
	public static final boolean D = true;
	private Context myContext;
	private SurfaceHolder mySurfaceHolder;
	private boolean running;
	private List<Card> deck = new ArrayList<Card>();
	private List<Card> myHand = new ArrayList<Card>(); 
	private List<Card> oppHand = new ArrayList<Card>(); 
	private int scaledCardW;
	private int scaledCardH;
	private  int screenW;
	private  int screenH;
	private float scale;
	private Paint whitePaint;
	private int oppScore; 
	private int myScore;
	private Bitmap cardBack;
	private boolean myTurn;
	private int movingCardIdx = -1; 
	private int movingX;
	private int movingY;
	private int chosenRank;
	private Bitmap nextCardButton;
	private  boolean oppPossesses;

	private boolean gameOver;
	private Bitmap gameOverBitmap;
	boolean lie;

	
	private ComputerPlayer computerPlayer = new ComputerPlayer();
    private final Object mRunLock = new Object();


	private GoFishThread thread;
	
	public GoldFishView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		SurfaceHolder holder = getHolder();
        holder.addCallback(this);
//        mGesDetect = new GestureDetector(context, new DoubleTapGestureDetector());
        //setLongClickable(true);


        thread = new GoFishThread(holder,context, new Handler(){
        	
	        public void handleMessage(Message M){
	        	
	        
	        }
        
        });
		
		
        setFocusable(true);
		
	}
	
	 /**
     * Fetches the animation thread corresponding to this GoFishView.
     *
     * @return the animation thread
     */
    public GoFishThread getThread() {
        return thread;
    }
    
	class GoFishThread extends Thread{
		
		public GoFishThread(SurfaceHolder surfaceHolder, Context context,

                Handler handler) {
			mySurfaceHolder = surfaceHolder;
			myContext= context;
			scale = myContext.getResources().getDisplayMetrics().density;

		}

		@Override
		public void run() {
			while (running) {
                Canvas c = null;
                try {
                    c = mySurfaceHolder.lockCanvas(null);
                    synchronized (mySurfaceHolder) {
                    	synchronized(mRunLock){
                        	if(running) draw(c);
                    	}
                    }
                } finally {
                    if (c != null) {
                        mySurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
		}
		
		public void setRunning(boolean b) {
            synchronized(mRunLock){
            	running = b;
            }
        }
		
		private void draw(Canvas canvas) {
			
			try {
				//draw white to screen to refill screen. So animation traces dont show
				canvas.drawColor(Color.WHITE);
				
				canvas.drawText("Computer Score:" + Integer.toString(oppScore), 
						10, whitePaint.getTextSize() + 10, whitePaint);
				canvas.drawText("Your Score:" + Integer.toString(myScore), 
						10, screenH-whitePaint.getTextSize()-10, whitePaint);
				
				//Draw computer hand
				for (int i = 0; i < oppHand.size(); i++) {
					
					canvas.drawBitmap(cardBack, i*(scaledCardW-50),whitePaint.getTextSize()+(50*scale), null);
				
				
				}
//				
				//Draw Pile as a single card back
				canvas.drawBitmap(cardBack, (screenW/2)-cardBack.getWidth()-10,
						(screenH/2)-(cardBack.getHeight()/2), null);

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
				
				if(gameOver){
					
					canvas.drawBitmap(gameOverBitmap,(screenW/2)-(gameOverBitmap.getWidth()/2),
							(screenH/2) - (gameOverBitmap.getHeight()/2),null);

					
				}
			} catch (Exception e) {
				
			}
			
		}
		
		public void setSurfaceSize(int width, int hight){
			synchronized (mySurfaceHolder) {
			screenW = width;
			screenH = hight;
			Bitmap tempBitmap = BitmapFactory.decodeResource
					(myContext.getResources(),
					R.drawable.card_back); 
			scaledCardW = (int) (screenW/7);
			scaledCardH = (int) (scaledCardW*1.3); 
			cardBack = Bitmap.createScaledBitmap
					(tempBitmap, scaledCardW, scaledCardH,false);
			
			nextCardButton = BitmapFactory.decodeResource(getResources(), R.drawable.arrow_next);
			gameOverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);

			whitePaint = new Paint();
			whitePaint.setAntiAlias(true); 
			whitePaint.setColor(Color.BLACK); 
			whitePaint.setStyle(Paint.Style.STROKE); 
			whitePaint.setTextAlign(Paint.Align.LEFT); 
			whitePaint.setTextSize(scale*15);
			initCards();//load card images
			dealCards();//set the players hands
			myScore = 0;
			oppScore = 0;
			checkOppForPairs();
			checkMeForPairs();
			gameOver = false;
			//random turn set at each game start
			myTurn = new Random().nextBoolean();
//			oppPossesses = true;
			
				if(!myTurn){
					//computer starts play
					makeComputerPlay();
				}
			}
		}
		
		private void checkOppForPairs() {
			synchronized(mySurfaceHolder){
			int tempRank;
			for(int i = 0; i < (oppHand.size()-1); i++){
				tempRank = oppHand.get(i).getRank();
				
				for(int j = i+1; j < oppHand.size(); j++){
					if(oppHand.get(j).getRank() == tempRank){
						oppScore++;
						oppHand.remove(i);
						//decrement j since we just took out an element
						oppHand.remove(j-1);
					}
				}			
			}
			
			if(oppHand.isEmpty()) gameOver = true;
			
			}
			
		}
		
		private void checkMeForPairs(){
			synchronized(mySurfaceHolder){
			int tempRank;
			for(int i = 0; i < (myHand.size()-1); i++){
				tempRank = myHand.get(i).getRank();
				
				for(int j = i+1; j < myHand.size(); j++){
					if(myHand.get(j).getRank() == tempRank){
						myScore++;
						myHand.remove(i);
						//decrement j since we just took out an element
						myHand.remove(j-1);
					}
				}			
			}
			if(myHand.isEmpty()) gameOver = true;
			
			}
		}
			
		private void animateCard(MotionEvent event){
			synchronized(mySurfaceHolder){
			int X = (int)event.getX();
			int Y = (int)event.getY();
			int Rank;
				if (myTurn && !gameOver) {
					for (int i = 0; i < 7; i++) {
						//check to see if the touched location is on a card-
						//--Has to be modified so a touch below the cards will not pick 
						//up the cards
						if (X > i*(scaledCardW+5) && X < i*(scaledCardW+5)
						+ scaledCardW &&
						Y > screenH-scaledCardH- whitePaint.getTextSize()- (50*scale)) { 
							Rank = myHand.get(i).getRank();
							oppPossesses = checkForChosenRank(oppHand,Rank);
							if(!oppPossesses){
								
								Toast.makeText(myContext, "Go Fish!", Toast.LENGTH_SHORT).show();
								//add a card to player hand
								drawCard(myHand);
								//increment score if a pair exists in hand
								checkMeForPairs();
								//make sure deck is not empty
								checkDeck();
								if(myHand.isEmpty() || oppHand.isEmpty()) gameOver = true;

							}else{
								Toast.makeText(myContext,"Score!!", Toast.LENGTH_SHORT).show();
								
								for(int j=0; i<oppHand.size();j++){
									if ((oppHand.get(j)).getRank() == Rank){
										 oppHand.remove(j);
										break;
									}
								}
								for(int j=0; i<myHand.size();j++){
									if ((myHand.get(j)).getRank() == Rank){
										 myHand.remove(j);
										break;
									}
								}
								myScore++;
								if(oppHand.isEmpty() || myHand.isEmpty()) gameOver = true;
							}
							myTurn = false;
							
								if(!gameOver){
									makeComputerPlay();
								}
						} 
					}
				}
			}
			
		}
		
		private void singleTap(MotionEvent event){
			synchronized (mySurfaceHolder) {
				
				int X = (int)event.getX();
				int Y = (int)event.getY();
				
					//Allow for card rotation if hand size is greater than 7
					if (myHand.size() > 7 &&
			        		X > screenW-nextCardButton.getWidth()-(30*scale) &&
			        		Y > screenH-nextCardButton.getHeight()-scaledCardH-(90*scale) &&
			        		Y < screenH-nextCardButton.getHeight()-scaledCardH-(60*scale)) {
			        			Collections.rotate(myHand, 1);
			        	}
					if(gameOver){
						myScore =0;
						oppScore= 0;
							if(!myHand.isEmpty()){
								myHand.clear();
							}
							if(!oppHand.isEmpty()){
								oppHand.clear();
							}
							
							initCards();//load card images
							dealCards();//set the players hands
							checkOppForPairs();
							checkMeForPairs();
							
						gameOver = false;
						myTurn = new Random().nextBoolean();

					}
				}
		}
		//load all the cards from the drawable folder into the deck and set the image for each card
		private void initCards() {
			synchronized(mySurfaceHolder){
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
		}

		private void dealCards() {
			synchronized(mySurfaceHolder){
					Collections.shuffle(deck,new Random());
					//give four cards to each player
						for (int i = 0; i < 4; i++) { 
							drawCard(myHand);
							drawCard(oppHand); 
						}
			}
		}
				
		private void drawCard(List<Card> handToDraw) { 
				synchronized(mySurfaceHolder){	
					handToDraw.add(0, deck.get(0));
					deck.remove(0);
				}
		}
				
		private void showChooseRankDialog(){
					
					final Dialog chooseRankDialog = new Dialog(myContext);
					chooseRankDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					chooseRankDialog.setContentView(R.layout.choose_rank_dialog);
					chooseRankDialog.setCancelable(false);
					chooseRankDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

					final Spinner rankSpinner = (Spinner) chooseRankDialog.findViewById(R.id.rankSpinner);
					ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
							myContext, R.array.ranks, android.R.layout.simple_spinner_item);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					rankSpinner.setAdapter(adapter);
					Button okButton = (Button) chooseRankDialog.findViewById(R.id.okButton);
					okButton.setOnClickListener(new View.OnClickListener(){
						public void onClick(View view){
							chosenRank = (rankSpinner. getSelectedItemPosition()+1);
							chooseRankDialog.dismiss();
							oppPossesses = checkForChosenRank(oppHand,chosenRank);
							if(!oppPossesses){
								Toast.makeText(myContext, "Go Fish!", Toast.LENGTH_SHORT).show();
								//add a card to player hand
								drawCard(myHand);
								//increment score if a pair exists in hand
								checkMeForPairs();
								//make sure deck is not empty
								checkDeck();
								if(myHand.isEmpty() || oppHand.isEmpty()) gameOver = true;

							}else{
								Toast.makeText(myContext,"Score!!", Toast.LENGTH_SHORT).show();
								
								for(int i=0; i<oppHand.size();i++){
									if ((oppHand.get(i)).getRank() == chosenRank){
										 oppHand.remove(i);
										break;
									}
								}
								for(int i=0; i<myHand.size();i++){
									if ((myHand.get(i)).getRank() == chosenRank){
										 myHand.remove(i);
										break;
									}
								}
								myScore++;
								if(oppHand.isEmpty() || myHand.isEmpty()) gameOver = true;
							}
							myTurn = false;
								if(!gameOver){
									makeComputerPlay();
								}
						}
					});
					chooseRankDialog.show();
			        
				}
				
		//go through opp hand to see if they have the card
		private boolean checkForChosenRank(List hand ,int rank){
			synchronized(mySurfaceHolder){
					return computerPlayer.possessCard(hand, rank);
			}
		}
		
		private void checkDeck(){
			synchronized(mySurfaceHolder){
				if(deck.isEmpty()) gameOver = true;
			}
		}	
		
		public void makeComputerPlay(){
			synchronized(mySurfaceHolder){
						int tempPlay = computerPlayer.makePlay(oppHand);
						int Rank = oppHand.get(tempPlay).getRank();
						showComputerQuestionDialog(Rank);						
						myTurn = true;

				}
		}
				
		private void showComputerQuestionDialog(int cardRank){
			synchronized(mySurfaceHolder){
					final int rank = cardRank;
					lie = true;
					final Dialog ComputerQuestionDialog = new Dialog(myContext);
					ComputerQuestionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
					ComputerQuestionDialog.setContentView(R.layout.computer_question_dialog);
					ComputerQuestionDialog.setCancelable(false);
					ComputerQuestionDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					Button confirmButton = (Button) ComputerQuestionDialog.findViewById(R.id.confirmButton);
					confirmButton.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							
							for(int i=0; i<myHand.size();i++){
								if ((myHand.get(i)).getRank() == rank){
									lie = false;
									myHand.remove(i);
								}
							}
							if(!lie){
								for(int i=0; i<oppHand.size();i++){
									if ((oppHand.get(i)).getRank() == rank){
										 oppHand.remove(i);
										break;
									}
								}
								oppScore++;
							}
							if(myHand.isEmpty() || oppHand.isEmpty()) gameOver = true;
							Toast.makeText(myContext, "CONFIRM!", Toast.LENGTH_SHORT).show();
							ComputerQuestionDialog.dismiss();
							
						}
					});
					Button declineButton = (Button) ComputerQuestionDialog.findViewById(R.id.declineButton);
					declineButton.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {

							//add a card to opponent
							drawCard(oppHand);
							//increment score if a pair exists in the hand
							checkOppForPairs();
							//make sure deck is not empty
							checkDeck();
							Toast.makeText(myContext, "DECLINE!", Toast.LENGTH_SHORT).show();
							ComputerQuestionDialog.dismiss();
							if(myHand.isEmpty() || oppHand.isEmpty()) gameOver = true;

						}
					});
					TextView question = (TextView) ComputerQuestionDialog.findViewById(R.id.questionTextView);
					TextView yourHand = (TextView) ComputerQuestionDialog.findViewById(R.id.YourHand);
					String temp;
					switch (cardRank) {
					case 11:
						temp = "A Jack";
						break;
					case 12:
						temp = "A Queen";
						break;
					case 13:
						temp = "A King";
						break;
					case 14:
						temp = "An Ace";
						break;
					case 8:
						temp = "An 8";
						break;
					default:
						temp = Integer.toString(cardRank);
						break;
					}
					question.setText("Do You Have " + temp);
					
					StringBuilder b = new StringBuilder();
					for (Card card : myHand){
						
					  b.append(card.getStringRank() +",");
					}
					//remove last comma
					b.deleteCharAt(b.length()-1);
					
					yourHand.setText(b.toString());
					ComputerQuestionDialog.show();
				}
			}
		}	
	
	public void doubleTapHandler(MotionEvent event){
		
		thread.animateCard(event);
	}

	public void singleTapHandler(MotionEvent event){
		thread.singleTap(event);
	}


		@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			thread.setSurfaceSize(width, height);
			
	}

		@Override
	public void surfaceCreated(SurfaceHolder arg0) {
			thread.setRunning(true);
			if (thread.getState() == Thread.State.NEW ){
				thread.start();
			}
	}

		@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
			 // we have to tell thread to shut down & wait for it to finish, or else
	        // it might touch the Surface after we return and explode
	        boolean retry = true;
	        thread.setRunning(false);
	        while (retry) {
	            try {
	                thread.join();
	                retry = false;
	            } catch (InterruptedException e) {
	            }
	        }
	    }

}
