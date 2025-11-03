import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RHEA_Evaluator {

    /** Configuration settings such as horizon, time limits, etc. */
    private final RHEA_Config config;

    /** Cache to store previously computed fitness values (based on hash of individuals). */
    private final Map<Integer, Double> fitnessCache;

    /** Total number of individuals evaluated (for performance tracking). */
    private long evalCount;

    /** Total cumulative time spent in evaluations (nanoseconds). */
    private long totalEvalTimeNs;

    /** Normalization constant — adjust to match game’s maximum possible score. */
    private static final double MAX_SCORE = 200.0;

    /**
     * Constructor for the evaluator.
     * @param config RHEA_Config containing algorithm parameters such as horizon, time limits, etc.
     */
    public RHEA_Evaluator(RHEA_Config config) {
        this.config = config;
        this.fitnessCache = new HashMap<>();
        this.evalCount = 0;
        this.totalEvalTimeNs = 0;
    }

    /**
     * Evaluates the fitness of an individual (sequence of actions).
     *
     * @param individual The candidate solution to evaluate.
     * @param state The current game state (will be cloned for simulation).
     * @param playerId The ID of the agent being evaluated.
     * @param opponentModels Map of opponent models for simulating other players' actions.
     * @param random A shared random generator used for opponent move sampling.
     * @return A fitness score between 0 and 1 (higher is better).
     *         Invalid or failed rollouts return Double.NEGATIVE_INFINITY.
     */
    public double evaluate(Individual_Action individual, AbstractGameState state, int playerId,
                           Map<Integer, OpponentModel> opponentModels, Random random) {

        // Sanity check — ensure we’re not evaluating an empty or null individual
        if (individual == null || individual.getActionSequence() == null || individual.getActionSequence().isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }

        // Use cached value if available (based on individual hash code)
        int hash = individual.hashCode();
        if (fitnessCache.containsKey(hash)) {
            return fitnessCache.get(hash);
        }

        // Measure simulation time for performance statistics
        long start = System.nanoTime();
        double fitness = simulate(individual, state, playerId, opponentModels, random);
        long end = System.nanoTime();

        // Store result in cache and update stats
        fitnessCache.put(hash, fitness);
        evalCount++;
        totalEvalTimeNs += (end - start);

        return fitness;
    }

    /**
     * Simulates an individual’s sequence of actions on a cloned game state,
     * while also predicting opponent moves using OpponentModel.
     *
     * @param ind The individual (action sequence) to simulate.
     * @param state The base game state (will be cloned).
     * @param playerId The player ID of this agent.
     * @param opponentModels Opponent prediction models.
     * @param random Random generator for sampling actions.
     * @return Normalized fitness score representing the quality of the sequence.
     */
    private double simulate(Individual_Action ind, AbstractGameState state, int playerId,
                            Map<Integer, OpponentModel> opponentModels, Random random) {
        // Clone state to avoid modifying the real game
        AbstractGameState simState = state.copy();

        try {
            int steps = 0;

            // Step through each action in the individual's sequence
            for (int action : ind.getActionSequence()) {
                // Stop if we’ve reached the horizon or the game has ended
                if (simState.isTerminal() || steps++ >= config.getHorizon()) break;

                // Perform our agent's action
                simState.performAction(simState.getCurrentPlayer(), action);

                // Simulate all opponent turns before our next move
                while (!simState.isTerminal() && simState.getCurrentPlayer() != playerId) {
                    int opponentId = simState.getCurrentPlayer();
                    OpponentModel model = opponentModels.get(opponentId);

                    // If no model is available, skip prediction for this opponent
                    if (model == null) break;

                    // Sample an opponent action based on probability distribution
                    var validActions = simState.getValidActions(opponentId);
                    int predictedAction = model.sampleAction(opponentId, validActions, random);

                    simState.performAction(opponentId, predictedAction);
                }
            }
        } catch (Exception e) {
            // Any runtime issue (invalid action, null state, etc.) penalizes fitness
            return Double.NEGATIVE_INFINITY;
        }

        // Compute player's final score and normalize relative to opponents
        double playerScore = simState.evaluateGameForPlayer(playerId);
        double opponentAvg = getOpponentAverageScore(simState, playerId);

        double relative = playerScore - opponentAvg;
        double normalized = relative / MAX_SCORE;

        return normalized;
    }

    /**
     * Computes the average score of all opponents (used for relative fitness scoring).
     *
     * @param state The simulated game state after the rollout.
     * @param playerId The player being evaluated.
     * @return Average opponent score.
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
     * @return The total number of fitness evaluations performed so far.
     */
    public long getEvaluationCount() {
        return evalCount;
    }

    /**
     * @return The average evaluation time per individual (milliseconds).
     */
    public double getAverageEvalTimeMs() {
        if (evalCount == 0) return 0;
        return (totalEvalTimeNs / 1e6) / evalCount;
    }

    /**
     * Clears all cached fitness values.
     * Should be called when starting a new game or generation.
     */
    public void clearCache() {
        fitnessCache.clear();
    }
}