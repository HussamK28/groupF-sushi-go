public class RHEA_Evaluator {
    /**
     * Evaluate how good an Individual is by simulating its action sequence
     * on a copy of the current game state.
     */
    public double evaluate(Individual individual, AbstractGameState state) {
        AbstractGameState simState = state.copy(); // assuming copy() or clone() exists
        int player = simState.getCurrentPlayer();

        for (int action : individual.getActionSequence()) {
            if (simState.isTerminal()) break;
            if (!simState.isActionValid(player, action)) break;

            simState = simState.applyAction(player, action);
            player = simState.getCurrentPlayer();
        }

        // Return score (or heuristic)
        return simState.getScore(state.getCurrentPlayer());
    }
}
