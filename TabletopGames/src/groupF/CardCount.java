package groupFSubmissions;

import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;
import games.sushigo.SGParameters;
import java.util.HashMap;
import java.util.Map;

import core.components.Deck;
import utilities.Pair;
import java.util.ArrayList;
import java.util.Random;
import core.components.Component;

public class CardCount {
	
	private SGGameState currentState;	
	private HashMap<Pair<SGCard.SGCardType, Integer>, Double> cardProbDict;
	private SGParameters gameParams;
	private HashMap<Pair<SGCard.SGCardType, Integer>, Integer> playedCardsDict;
	private ArrayList<Integer> unknownHands;
	private ArrayList<DeckSGCard> probableHands;
	private ArrayList<DeckSGCard> knownHands;
	private int playerId;
	private int totalDeckSize = 0;
	private final Random rnd = new Random();
	private int maxPlayerId;
	
	public CardCount(int playerId, SGGameState s) {
		// may need to use copy player if functions are blocked at tournament
		this.currentState = s; // s._copyplayer(playerId);// currentState is never modified

		this.playerId = playerId;
		this.cardProbDict = new HashMap<Pair<SGCard.SGCardType, Integer>, Double>();
		this.gameParams = this.currentState.getGameParameters();
		this.unknownHands = new ArrayList<Integer>();
		this.playedCardsDict = new HashMap<Pair<SGCard.SGCardType, Integer>, Integer>();
		this.buidPFromState();
		this.maxPlayerId = this.currentState.getNPlayers();
		this.probableHands = new ArrayList<DeckSGCard>();
		this.knownHands = new ArrayList<DeckSGCard>();
		Step(s);
	}
	
	public void Step(SGGameState nextStep) {
		//clear & calculate unknown hands again
		this.unknownHands.clear();
		this.knownHands.clear();
		// update all known cards
		int allKnownCards = 0;
		// get all cards in play
		for (Deck<SGCard> deck : nextStep.getPlayedCards()) {
			knownHands.add(deck);
			for (SGCard card : deck.peek(0, nextDeck.getSize())) {
				Pair<SGCard.SGCardType, Integer> cardKey = new Pair<>(card.type, card.value);
				Integer playedEntry = this.playedCardsDict.getOrDefault(cardKey, 0);
				playedEntry++;
				this.playedCardsDict.put(cardKey, playedEntry);
				allKnownCards++;
			}
		}
		
		List<Deck<SGCard>> nextKnownHand = nextStep.getPlayerHands();
		
		for (int i = 0; i < this.maxPlayerId; i++) {
			if (!nextStep.isHandKnown(this.playerId, i)) {
				this.unknownHands.add(i);
				continue;
			}
			Deck<SGCard> nextDeck = nextKnownHand.get(i);
			for (SGCard card : nextDeck.peek(0, nextDeck.getSize())) {
				Pair<SGCard.SGCardType, Integer> cardKey = new Pair<>(card.type, card.value); // Or card.value
				Integer playedEntry = this.playedCardsDict.getOrDefault(cardKey, 0);
				playedEntry++;
				this.playedCardsDict.put(cardKey, playedEntry);
				allKnownCards++;
			}

		}
		
		//produce hand probabilities
		int totalUnknownCards = this.totalDeckSize - allKnownCards;

		this.cardProbDict.clear();

		for (Map.Entry<Pair<SGCard.SGCardType, Integer>, Integer> entry : this.gameParams.nCardsPerType.entrySet()) {
			SGCard.SGCardType type = entry.getKey().a; // .a gets the first item from Pair
			double count = 0.0 + (double) entry.getValue();
			double totalOfType = (double) entry.getValue();
			// Get played of this type (e.g., 3 Tempura)
			double playedOfType = (double) playedCardsDict.getOrDefault(entry.getKey(), 0);

			// Numerator is what's *left* (14 - 3 = 11)
			double unknownOfType = totalOfType - playedOfType;
			
			double next_probability = 0.0;
			if (totalUnknownCards > 0 && unknownOfType > 0) {
				next_probability = unknownOfType / totalUnknownCards;
			}
			cardProbDict.put(entry.getKey(), next_probability);

		}
		this.probableHands.clear()
		for(int i:this.unknownHands) {
			Deck<SGCard> nextHand = GenerateProbableDeck(i);
			this.probableHands.add(nextHand);
		}
		this.currentState = nextStep;
			
	}

	private void buidPFromState() {
		totalDeckSize = 0;
		this.playedCardsDict.clear();
		this.unknownHands.clear();
		for (Map.Entry<Pair<SGCard.SGCardType, Integer>, Integer> entry : this.gameParams.nCardsPerType.entrySet()) {
			SGCard.SGCardType type = entry.getKey().a; // .a gets the first item from Pair
			this.totalDeckSize += (entry.getValue());
		}
		Step(this.currentState);
	}
	
	public ArrayList<DeckSGCard> getAllHands(){
		ArrayList<DeckSGCard> outHands = new ArrayList<DeckSGCard>();
		
		return outHands;
	}
	
	
	

	public Deck<SGCard> GenerateProbableDeck(int targetId){
        Deck<SGCard> generatedHand = new Deck<>("GeneratedHand-" + targetId, targetId, Component.VisibilityMode.VISIBLE_TO_ALL);

        
        int handSize = this.currentState.getPlayerHands().get(targetId).getSize();

        
        for (int i = 0; i < handSize; i++) {
            
            double randomValue = rnd.nextDouble();
            double cumulativeProbability = 0.0;

            for (Map.Entry<Pair<SGCard.SGCardType, Integer>, Double> entry : this.cardProbDict.entrySet()) {
                cumulativeProbability += entry.getValue();
                if (randomValue < cumulativeProbability) {
                    Pair<SGCard.SGCardType, Integer> cardKey = entry.getKey();
                    
                    
                    SGCard.SGCardType type = cardKey.a;
                    int makiValue = cardKey.b;
                    
                    SGCard newCard = new SGCard(type, makiValue);
                    
                    generatedHand.add(newCard);
                    
                    
                    break; 
                }
            }
        }
        
        return generatedHand;
    }
}
