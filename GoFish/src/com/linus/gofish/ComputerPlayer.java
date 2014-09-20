package com.linus.gofish;

import java.util.List;
import java.util.Random;

public class ComputerPlayer {

	public int makePlay(List <Card> hand){
		//pick a random card from the given hand
		int index = new Random().nextInt(hand.size());
		return index;
	}
	
	public boolean possessCard(List<Card> oppHand,int rank){
		
		for(int i=0; i<oppHand.size();i++){
			if ((oppHand.get(i)).getRank() == rank){
				return true;
			}
		}
		return false;
	}
	
	

}
