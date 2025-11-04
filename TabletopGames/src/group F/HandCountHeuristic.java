package groupFSubmissions;
import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;
import games.sushigo.SGParameters;
import games.sushigo.SGFeatures;
import games.sushigo.SGForwardModel;
import SGScoring;
import java.util.HashMap;
import java.util.Map;
import groupFSubmissions.SGscoring;
import core.components.Deck;
import utilities.Pair;
import java.util.ArrayList;
import java.util.Random;

import core.AbstractGameState;
import core.components.Component;
import core.components.Counter;

public class HandCountHeuristic {
	private CardCount cardCounter;
    private double initT = 1;
    private double a = 0.1;    
    private double currentT= 1;
    private int playerId;     
    private int maxActions = 4;
	
    public HandCountHeuristic(int playerId, SGGameState cState, double startT, double alpha) {
        this.playerId = playerId; // Store our agent's ID
        this.initT = startT;
        this.currentT = startT;
        this.a = alpha;             
        this.cardCounter = new CardCount(playerId, cState);
    }
    
   
    private double checkOpponentThreat(Map<SGCard.SGCardType, Counter> opponentCounts, int opponentId, SGParameters params) {    	// 
        double threatScore = 0.0;          
        int intialPoints= sgState.getGameScore(opponentId);
        int pointDelta = 0;     
        
        for (SGCard cardInHand : handToPass.getComponents()) {
        	pointDelta = SGScoring.calculatePlayScore(cardInHand.type,opponentCounts,params);
        	threatScore += pointsDelta;
        }
        if (sgState.isGameOver()) {
        	threatScore+= SGScoring.calculateMakiScores(opponentCounts,params)[opponentId];
        	threatScore+=SGScoring.calculatePuddingScores(opponentCounts)[opponentId];        			
        }        
        return threatScore;
    }
    
   
    
    private double evaluateState(Map<SGCard.SGCardType, Counter>[] allCards, int playerId, params) {
        SGGameState sgState = (SGGameState) stateAfterMyMove;
        
        double myScore = SGScoring.calculatePlayScore(allCards[playerId],params )

        int nextPlayerId = (playerId + 1) % sgState.getNPlayers();        
        
        Deck<SGCard> handToPass = sgState.getPlayerHands().get(playerId);
        myscore+= SGScoring.calculateMakiScores( allCards, SGParameters params)[playerId];
        myscore+= SGScoring.calculatePuddingScores( allCards, SGParameters params)[playerId];
       
        double opponentThreat = 0.0;
        if (handToPass != null && !handToPass.isEmpty()) {            
            opponentThreat = checkOpponentThreat(handToPass, nextPlayerId, sgState);
        }        
        
        return (myScore - opponentThreat);
    }
    
    public double getBestHeuristicScore(AbstractGameState state, int playerId) {

        this.cardCounter.Step((SGGameState)state);        
        
        int actionSpaceSize = state.getActionSpaceSize();
        List<Integer> allActions = new ArrayList<>();
        
        for (int i = 0; i < actionSpaceSize; i++) {
            allActions.add(i);
        }
        
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i =0; i<actionSpaceSize; i++ ) {

            AbstractGameState nextState = state.copy();
            nextState.applyAction(i);
            
            Map<SGCard.SGCardType, Counter>[] allCards = nextState.getPlayedCardTypes();

            double score = evaluateState(allCards, playerId); 

            if (score > bestScore) {
                bestScore = score;
            }            
        }
        
        bestScore *= this.currentT;
        
        this.currentT*=this.a;
        
        if(this.currentT< this.a) {
        	this.currentT = a;
        }
        
        return bestScore;
    }

}
