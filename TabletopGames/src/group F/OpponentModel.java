import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OpponentModel {
    private Map<Integer, Map <Integer, Integer>> actionCounts = new HashMap<>();
    private static final double probSmoothing = 1.0;
    private static final double decayRate = 0.9;

    public OpponentModel() {
        this.actionCounts = new HashMap<>();
    }

    public void actionObserved(int oID, int action) {
        actionCounts.putIfAbsent(oID, new HashMap<>());
        Map<Integer, Integer> counts = actionCounts.get(oID);
        counts.put(action, counts.getOrDefault(action,0) + 1);

        for(int a: validActions){
            counts.put(a, counts.getOrDefault(a,0.0) * decayRate);
        }
        counts.put(action, counts.getOrDefault(action,0.0) + 1.0);
    }

    public Map<Integer, Double> getActionDistribution(int opponentId, List<Integer> validActions) {
        Map<Integer, Double> probs = new HashMap<>();
        Map<Integer, Integer> counts = actionCounts.getOrDefault(opponentId, new HashMap<>());

        double total = 0.0;
        for (int action : validActions) {
            total += counts.getOrDefault(action, 0) + SMOOTHING;
        }

        for (int action : validActions) {
            double count = counts.getOrDefault(action, 0) + SMOOTHING;
            probs.put(action, count / total);
        }

        return probs;
    }

    public int sampleAction(int opponentId, List<Integer> validActions, Random random) {
        Map<Integer, Double> probs = getActionDistribution(opponentId, validActions);

        double r = random.nextDouble();
        double cumulative = 0.0;

        for (int action : validActions) {
            cumulative += probs.get(action);
            if (r <= cumulative) {
                return action;
            }
        }

        return validActions.get(random.nextInt(validActions.size()));
    }

    public void reset() {
        actionCounts.clear();
    }
}
