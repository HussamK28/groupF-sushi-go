import java.util.HashMap;
import java.util.Map;

/**
 * Evaluates an Individual by simulating its action sequence
 * from the current AbstractGameState.
 *
 * Supports caching, relative scoring, normalization, and performance tracking.
 */
public class RHEA_Evaluator {

    private final RHEA_Config config;
    private final Map<Integer, Double> fitnessCache;
    private long evalCount;
    private long totalEvalTimeNs;

    // Optional normalization constant (depends on game’s scoring scale)
    private static final double MAX_SCORE = 200.0;

    public RHEA_Evaluator(RHEA_Config config) {
        this.config = config;
        this.fitnessCache = new HashMap<>();
        this.evalCount = 0;
        this.totalEvalTimeNs = 0;
    }

    /**
     * Evaluate the fitness of an Individual.
     * The higher the return value, the better the action sequence.
     */
    public double evaluate(Individual individual, AbstractGameState state) {
        if (individual == null || individual.getActionSequence() == null || individual.getActionSequence().isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }

        int hash = individual.hashCode();
        if (fitnessCache.containsKey(hash)) {
            return fitnessCache.get(hash);
        }

        long start = System.nanoTime();
        double fitness = simulate(individual, state);
        long end = System.nanoTime();

        // Cache and update performance stats
        fitnessCache.put(hash, fitness);
        evalCount++;
        totalEvalTimeNs += (end - start);

        return fitness;
    }

    /**
     * The simulation core: clone state, apply action sequence, compute fitness.
     */
    private double simulate(Individual ind, AbstractGameState state) {
        AbstractGameState simState = state.copy();
        int player = simState.getCurrentPlayer();

        try {
            int steps = 0;
            for (int action : ind.getActionSequence()) {
                if (simState.isTerminal() || steps++ >= config.getHorizon()) break;

                if (!simState.isActionValid(player, action)) {
                    // Invalid sequence -> penalize
                    return Double.NEGATIVE_INFINITY;
                }

                simState = simState.applyAction(player, action);
                player = simState.getCurrentPlayer();
            }
        } catch (Exception e) {
            // Penalize exceptions (illegal transitions, etc.)
            return Double.NEGATIVE_INFINITY;
        }

        // Relative normalized score
        int evaluatedPlayer = state.getCurrentPlayer();
        double playerScore = simState.getScore(evaluatedPlayer);
        double oppAvg = getOpponentAverageScore(simState, evaluatedPlayer);
        double relative = playerScore - oppAvg;

        // Normalize to [0, 1]
        return relative / MAX_SCORE;
    }

    /**
     * Compute average opponent score to make fitness relative.
     */
    private double getOpponentAverageScore(AbstractGameState state, int playerId) {
        double total = 0;
        int count = 0;
        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != playerId) {
                total += state.getScore(i);
                count++;
            }
        }
        return (count == 0) ? 0 : total / count;
    }

    /**
     * Return how many individuals have been evaluated.
     */
    public long getEvaluationCount() {
        return evalCount;
    }

    /**
     * Return average evaluation time in milliseconds.
     */
    public double getAverageEvalTimeMs() {
        if (evalCount == 0) return 0;
        return (totalEvalTimeNs / 1e6) / evalCount;
    }

    /**
     * Clears cached fitness values (useful between games).
     */
    public void clearCache() {
        fitnessCache.clear();
    }
}