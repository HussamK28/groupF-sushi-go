import java.util.*;

public class OpponentModel {
    private Map<Integer, Map<Integer, Double>> actionCounts;
    private static final double probSmoothing = 1.0;
    private static final double decayRate = 0.9;

    public OpponentModel() {
        this.actionCounts = new HashMap<>();
    }

    // This function observes what actions our opponents performed and put it on the hashmap
    public void actionObserved(int opponentId, int action, List<Integer> validActions) {
        actionCounts.putIfAbsent(opponentId, new HashMap<>());
        Map<Integer, Double> counts = actionCounts.get(opponentId);
        // If the action is valid, it puts the action on the counts hashmap
        // buts decays after a certain amount of time
        for (int a : validActions) {
            counts.put(a, counts.getOrDefault(a, 0.0) * decayRate);
        }

        counts.put(action, counts.getOrDefault(action, 0.0) + 1.0);
    }
    // This function gets the probability distribution of each action to determine what approach is the best to choose.
    public Map<Integer, Double> getActionDistribution(int opponentId, List<Integer> validActions) {
        Map<Integer, Double> probs = new HashMap<>();
        Map<Integer, Double> counts = actionCounts.getOrDefault(opponentId, new HashMap<>());

        // Increments the total and uses smoothing to ensure probability sums to 1
        double total = 0.0;
        for (int action : validActions) {
            total += counts.getOrDefault(action, 0.0) + probSmoothing;
        }


        for (int action : validActions) {
            double count = counts.getOrDefault(action, 0.0) + probSmoothing;
            probs.put(action, count / total);
        }

        return probs;
    }

    // Determine what action an opponent could take according to probability
    public int sampleAction(int opponentId, List<Integer> validActions, Random random) {
        Map<Integer, Double> probs = getActionDistribution(opponentId, validActions);

        double r = random.nextDouble();
        double cumulative = 0.0;
        for (int action : validActions) {
            cumulative += probs.get(action);
            if (r <= cumulative) return action;
        }

        return validActions.get(random.nextInt(validActions.size()));
    }

    public void reset() {
        actionCounts.clear();
    }
}
