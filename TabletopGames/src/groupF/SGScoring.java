package groupFSubmissions;
import core.components.Counter;
import games.sushigo.SGParameters;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** 
 * Contains decoupled scoring logic for Sushi Go! cards.
 * These functions calculate score increments without modifying game state.
 */
public class SGScoring {
    /**
     * Calculates the score increment for a card that is played (onReveal).
     * Note: This function is pure and has NO side effects. The caller is responsible
     * for handling side effects (like decrementing Wasabi) based on the score returned.
     *
     * @param cardPlayed       The type of card just played.
     * @param playedCardCounts A Map of all cards currently in the player's played area (including the one just played).
     * @param params           The game parameters object containing score values.
     * @return The score increment to be added to the player's total score.
     */
    public static int calculatePlayScore(SGCard.SGCardType cardPlayed, Map<SGCard.SGCardType, Counter> playedCardCounts, SGParameters params) {
        
        // Get the total count of the card type that was just played
        int amount = 0;
        if (playedCardCounts.containsKey(cardPlayed)) {
            amount = playedCardCounts.get(cardPlayed).getValue();
        }

        switch (cardPlayed) {
            case Tempura:
                // Adds points for pairs
                if (amount % 2 == 0) {
                    return params.valueTempuraPair;
                }
                return 0;

            case Sashimi:
                // Adds points for triplets
                if (amount % 3 == 0) {
                    return params.valueSashimiTriple;
                }
                return 0;

            case Dumpling:
                // Add points depending on how many were collected
                if (amount == 0) return 0;
                int idx = Math.min(amount, params.valueDumpling.length) - 1;
                return params.valueDumpling[idx];

            case SquidNigiri:
                // Gives points, more if played on Wasabi
                int squidValue = params.valueSquidNigiri;
                if (playedCardCounts.get(SGCard.SGCardType.Wasabi).getValue() > 0) {
                    squidValue *= params.multiplierWasabi;
                    // Note: The caller is responsible for decrementing Wasabi
                }
                return squidValue;

            case SalmonNigiri:
                // Gives points, more if played on Wasabi
                int salmonValue = params.valueSalmonNigiri;
                if (playedCardCounts.get(SGCard.SGCardType.Wasabi).getValue() > 0) {
                    salmonValue *= params.multiplierWasabi;
                    // Note: The caller is responsible for decrementing Wasabi
                }
                return salmonValue;

            case EggNigiri:
                // Gives points, more if played on Wasabi
                int eggValue = params.valueEggNigiri;
                if (playedCardCounts.get(SGCard.SGCardType.Wasabi).getValue() > 0) {
                    eggValue *= params.multiplierWasabi;
                    // Note: The caller is responsible for decrementing Wasabi
                }
                return eggValue;

            case Wasabi:
            case Chopsticks:
            case Maki:
            case Pudding:
            default:
                // These cards do not score on reveal
                return 0;
        }
    }

    /**
     * Calculates round-end scores for Maki rolls.
     *
     * @param allPlayerCardCounts An array where each element is a player's Map of card counts.
     * @param params              The game parameters object containing score values.
     * @return A Map where Key = Player ID, Value = Score Increment.
     */
    public static Map<Integer, Integer> calculateMakiScores(Map<SGCard.SGCardType, Counter>[] allPlayerCardCounts, SGParameters params) {
        Map<Integer, Integer> scoreIncrements = new HashMap<>();
        int nPlayers = allPlayerCardCounts.length;

        // Calculate who has the most points and who has the second most points
        int most = 0;
        int secondMost = 0;
        HashSet<Integer> mostPlayers = new HashSet<>();
        HashSet<Integer> secondPlayers = new HashSet<>();
        for (int i = 0; i < nPlayers; i++) {
            int nMakiRolls = allPlayerCardCounts[i].get(SGCard.SGCardType.Maki).getValue();

            if (nMakiRolls > most) {
                secondMost = most;
                secondPlayers.clear();
                secondPlayers.addAll(mostPlayers);

                most = nMakiRolls;
                mostPlayers.clear();
                mostPlayers.add(i);
            } else if (nMakiRolls == most && nMakiRolls != 0) {
                mostPlayers.add(i);
            } else if (nMakiRolls > secondMost) {
                secondMost = nMakiRolls;
                secondPlayers.clear();
                secondPlayers.add(i);
            } else if (nMakiRolls == secondMost && nMakiRolls != 0) {
                secondPlayers.add(i);
            }
        }

        // Calculate the score each player gets and add to the map
        int mostScore = params.valueMakiMost;
        int secondScore = params.valueMakiSecond;
        if (!mostPlayers.isEmpty()) {
            // Best score is split among the tied players with no remainder
            mostScore /= mostPlayers.size();
            for (Integer mostPlayer : mostPlayers) {
                scoreIncrements.put(mostPlayer, mostScore);
            }
        }
        if (!secondPlayers.isEmpty() && mostPlayers.size() == 1) {
            // Second-best score is split, only awarded if no ties for most
            secondScore /= secondPlayers.size();
            for (Integer secondPlayer : secondPlayers) {
                scoreIncrements.put(secondPlayer, secondScore);
            }
        }
        
        return scoreIncrements;
    }

    /**
     * Calculates game-end scores for Puddings.
     *
     * @param allPlayerCardCounts An array where each element is a player's Map of card counts.
     * @param nPlayers            The number of players in the game.
     * @param params              The game parameters object containing score values.
     * @return A Map where Key = Player ID, Value = Score Increment.
     */
    public static Map<Integer, Integer> calculatePuddingScores(Map<SGCard.SGCardType, Counter>[] allPlayerCardCounts, int nPlayers, SGParameters params) {
        Map<Integer, Integer> scoreIncrements = new HashMap<>();

        //Calculate who has the most points and who has the least points
        int best = allPlayerCardCounts[0].get(SGCard.SGCardType.Pudding).getValue();
        int worst = best;
        HashSet<Integer> mostPlayers = new HashSet<>();
        HashSet<Integer> leastPlayers = new HashSet<>();
        for (int i = 0; i < nPlayers; i++) {
            int nPuddings = allPlayerCardCounts[i].get(SGCard.SGCardType.Pudding).getValue();

            if (nPuddings > best) {
                best = nPuddings;
                mostPlayers.clear();
                mostPlayers.add(i);
            } else if (nPuddings == best && nPuddings != 0) {
                mostPlayers.add(i);
            }
            if (nPuddings < worst) {
                worst = nPuddings;
                leastPlayers.clear();
                leastPlayers.add(i);
            } else if (nPuddings == worst) {
                leastPlayers.add(i);
            }
        }
        
        if (best > worst) {
            // Calculate the score each player gets
            int mostScore = params.valuePuddingMost;
            int leastScore = params.valuePuddingLeast;
            if (!mostPlayers.isEmpty()) {
                // Best score is split among the tied players
                mostScore /= mostPlayers.size();
                for (Integer mostPlayer : mostPlayers) {
                    scoreIncrements.put(mostPlayer, mostScore);
                }
            }
            if (!leastPlayers.isEmpty() && nPlayers > 2) {
                // Least score is split, only awarded in games with more than 2 players
                leastScore /= leastPlayers.size();
                for (Integer leastPlayer : leastPlayers) {
                    scoreIncrements.put(leastPlayer, leastScore);
                }
            }
        }
        
        return scoreIncrements;
    }
}