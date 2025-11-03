import java.util.HashMap;
import java.util.Map;

public class RHEA_Evaluator {

    private final RHEA_Config config;
    private final Map<Integer, Double> fitnessCache;
    private long evalCount;
    private long totalEvalTimeNs;

    private static final double MAX_SCORE = 200.0;

    public RHEA_Evaluator(RHEA_Config config) {
        this.config = config;
        this.fitnessCache = new HashMap<>();
        this.evalCount = 0;
        this.totalEvalTimeNs = 0;
    }

    public double evaluate(Individual_Action individual, AbstractGameState state, int playerId,
                           Map<Integer, OpponentModel> opponentModels, Random random) {
        if (individual == null || individual.getActionSequence() == null || individual.getActionSequence().isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }

        int hash = individual.hashCode();
        if (fitnessCache.containsKey(hash)) {
            return fitnessCache.get(hash);
        }

        long start = System.nanoTime();
        double fitness = simulate(individual, state, playerId, opponentModels, random);
        long end = System.nanoTime();

        fitnessCache.put(hash, fitness);
        evalCount++;
        totalEvalTimeNs += (end - start);

        return fitness;
    }

    private double simulate(Individual_Action ind, AbstractGameState state, int playerId,
                            Map<Integer, OpponentModel> opponentModels, Random random) {
        AbstractGameState simState = state.copy();

        try {
            int steps = 0;
            for (int action : ind.getActionSequence()) {
                if (simState.isTerminal() || steps++ >= config.getHorizon()) break;

                simState.performAction(simState.getCurrentPlayer(), action);

                // Opponent simulation (same logic as in B1’s agent)
                while (!simState.isTerminal() && simState.getCurrentPlayer() != playerId) {
                    int opponentId = simState.getCurrentPlayer();
                    OpponentModel model = opponentModels.get(opponentId);
                    if (model == null) break;

                    var validActions = simState.getValidActions(opponentId);
                    int predictedAction = model.sampleAction(opponentId, validActions, random);
                    simState.performAction(opponentId, predictedAction);
                }
            }
        } catch (Exception e) {
            return Double.NEGATIVE_INFINITY;
        }

        double score = simState.evaluateGameForPlayer(playerId);
        double normalized = score / MAX_SCORE;
        return normalized;
    }

    public long getEvaluationCount() { return evalCount; }

    public double getAverageEvalTimeMs() {
        if (evalCount == 0) return 0;
        return (totalEvalTimeNs / 1e6) / evalCount;
    }

    public void clearCache() { fitnessCache.clear(); }
}