package com.linus.gofish;

import android.graphics.Bitmap;

public class Card {

	

		private int id;
		private int suit;
		private int rank;
		private Bitmap bmp;
		private int scoreValue;
		
		public Card(int newId) {
			id = newId;
			suit = Math.round((id/100) * 100);
			rank = id - suit; 
			
		}

		public int getScoreValue() {
			return scoreValue;
		}
		
		public void setBitmap(Bitmap newBitmap) {
			bmp = newBitmap; 
		}
		
		public Bitmap getBitmap() {
			return bmp;
		}
		
		public int getId() {
			return id;
		}
		
		public int getSuit() {
			return suit;
		}
		
		public int getRank() {
			return rank;
		}
		public String getStringRank(){
			String temp;
			int tempRank = getRank();
			switch (tempRank) {
			case 11:
				temp = "J";
				break;
			case 12:
				temp = "Q";
				break;
			case 13:
				temp = "K";
				break;
			case 14:
				temp = "A";
				break;
				
			default:
				temp = Integer.toString(tempRank);
				break;
			}
			
			return temp;
			
		}

	}


