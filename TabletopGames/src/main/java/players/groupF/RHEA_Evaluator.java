package players.groupF;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.AbstractForwardModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RHEA_Evaluator that maps integer action indices (Individual_Action) to real
 * AbstractAction objects via forwardModel.computeAvailableActions(...) and
 * then advances the state with forwardModel.next(...).
 */
public class RHEA_Evaluator {

    private final RHEA_Config config;
    private final AbstractForwardModel forwardModel; // public next(...) and computeAvailableActions(...)
    private final Map<Integer, Double> fitnessCache;
    private long evalCount;
    private long totalEvalTimeNs;
    private static final double MAX_SCORE = 50;

    /**
     * @param config RHEA hyperparameters (horizon, etc.)
     * @param forwardModel The game's forward model instance (must expose public next(...) and computeAvailableActions(...))
     */
    public RHEA_Evaluator(RHEA_Config config, AbstractForwardModel forwardModel) {
        this.config = config;
        this.forwardModel = forwardModel;
        this.fitnessCache = new HashMap<>();
        this.evalCount = 0;
        this.totalEvalTimeNs = 0;
    }

    /**
     * Evaluate an individual (sequence of integer action indices).
     *
     * @param individual  Individual_Action containing List<Integer> action indices
     * @param state       current game state (will be copied)
     * @return normalized fitness (higher is better) or Double.NEGATIVE_INFINITY for invalid plans
     */
    public double evaluate(Individual_Action individual, AbstractGameState state) {
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

        fitnessCache.put(hash, fitness);
        evalCount++;
        totalEvalTimeNs += (end - start);

        return fitness;
    }

    /**
     * Simulate the integer-indexed action sequence by mapping to real AbstractAction objects
     * from forwardModel.computeAvailableActions(simState) at each step.
     */
    private double simulate(Individual_Action ind, AbstractGameState originalState) {
        AbstractGameState simState = originalState.copy();

        try {
            int steps = 0;
            for (Integer actionIndex : ind.getActionSequence()) {
                // stop on terminal or horizon
                if (!simState.isNotTerminal() || steps++ >= config.getHorizon()) break;

                // get available actions for the current player in this simulated state
                List<AbstractAction> available = forwardModel.computeAvailableActions(simState);

                // invalid if no available actions
                if (available == null || available.isEmpty()) {
                    return Double.NEGATIVE_INFINITY;
                }

                // actionIndex must be a valid index into 'available'
                if (actionIndex == null || actionIndex < 0 || actionIndex >= available.size()) {
                    // plan requests a non-existent action -> penalize
                    return Double.NEGATIVE_INFINITY;
                }

                AbstractAction chosen = available.get(actionIndex);

                // advance the simulated state using the forward model (public next method)
                forwardModel.next(simState, chosen);

                // forwardModel.next(...) will update simState (turns, scoring, etc.)
            }
        } catch (Exception e) {
            // runtime error during simulation -> penalize the individual
            return Double.NEGATIVE_INFINITY;
        }

        // after simulation compute relative normalized score for the original player
        int playerId = originalState.getCurrentPlayer();
        double myScore = simState.getGameScore(playerId);
        double avgOpp = getOpponentAverageScore(simState, playerId);
        double relative = myScore - avgOpp;
        return relative / MAX_SCORE;
    }

    private double getOpponentAverageScore(AbstractGameState state, int playerId) {
        double total = 0;
        int count = 0;
        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != playerId) {
                total += state.getGameScore(i);
                count++;
            }
        }
        return (count == 0) ? 0 : total / count;
    }

    public void clearCache() {
        fitnessCache.clear();
    }

    public long getEvaluationCount() {
        return evalCount;
    }

    public double getAverageEvalTimeMs() {
        if (evalCount == 0) return 0;
        return (totalEvalTimeNs / 1e6) / evalCount;
    }
}