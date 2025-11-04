package players.groupF;

import games.sushigo.SGForwardModel;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * RHEA Agent for SushiGo using ChooseCard actions.
 *
 * Uses a Rolling Horizon Evolutionary Algorithm:
 *  - Simulates sequences of future actions (Individuals)
 *  - Evaluates them with a forward model and a fitness function
 *  - Selects the best first action to execute
 */
public class RHEA_Agent {

    // Hyperparameters and configuration for the RHEA algorithm
    private final RHEA_Config config;

    // Random number generator for reproducibility
    private final Random random;

    // Reference to the game's forward model, used to simulate future states
    private final SGForwardModel forwardModel;

    // Evaluator to compute fitness of action sequences
    private RHEA_Evaluator evaluator;

    // Current population of action sequences
    private List<Individual_Action> population;

    // Stores the best action sequence after evaluation
    private Individual_Action bestIndividual;

    // Optional opponent modeling (currently not used in decision-making)
    private HashMap<Integer, OpponentModel> opponentModels;

    /**
     * Constructor
     * @param config Hyperparameters for the RHEA agent
     * @param seed Random seed for reproducibility
     * @param forwardModel The game's forward model instance
     */
    public RHEA_Agent(RHEA_Config config, long seed, SGForwardModel forwardModel) {
        this.config = config;
        this.random = new Random(seed); // initialize RNG with seed
        this.forwardModel = forwardModel; // store forward model reference
        this.population = new ArrayList<>(); // initialize empty population
        this.opponentModels = new HashMap<>(); // initialize empty opponent model map
    }

    /**
     * Initialize opponent models for the current game state
     * @param state Current SGGameState
     */
    public void initializePlayer(SGGameState state) {
        opponentModels.clear(); // clear previous opponent models
        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != state.getCurrentPlayer()) {
                opponentModels.put(i, new OpponentModel()); // create model for each opponent
            }
        }
    }

    /**
     * Select the best action using RHEA simulation
     * @param gameState Current SGGameState
     * @param actions List of legal ChooseCard actions
     * @return Chosen ChooseCard action
     */
    public ChooseCard getAction(SGGameState gameState, List<ChooseCard> actions) {
        if (actions == null || actions.isEmpty()) {
            return null; // safety: no available actions
        }

        SGForwardModel fm = this.forwardModel; // reference forward model

        // Lazily initialize the evaluator if it hasn't been created yet
        if (evaluator == null) {
            evaluator = new RHEA_Evaluator(config, fm);
        }

        // 1️⃣ Initialize a random population of candidate action sequences
        initializePopulation(actions.size(), config.getHorizon());

        // 2️⃣ Evaluate fitness for each individual in the population
        for (Individual_Action ind : population) {
            double fitness = evaluator.evaluate(ind, gameState); // simulate action sequence
            ind.setFitness(fitness); // store fitness in individual
        }

        // 3️⃣ Select the individual with the highest fitness
        bestIndividual = population.stream()
                .max((a, b) -> Double.compare(a.getFitness(), b.getFitness()))
                .orElse(population.get(0)); // fallback: first individual

        // 4️⃣ Return the first action of the best plan
        int bestActionIndex = bestIndividual.getActionSequence().get(0);
        if (bestActionIndex >= 0 && bestActionIndex < actions.size()) {
            return actions.get(bestActionIndex); // return the mapped ChooseCard
        }

        // Safety fallback: return a random legal action if index is invalid
        return actions.get(random.nextInt(actions.size()));
    }

    /**
     * Initialize a population of random action sequences
     * @param actionSpaceSize Number of legal actions in the current state
     * @param horizon Length of action sequences to simulate
     */
    private void initializePopulation(int actionSpaceSize, int horizon) {
        population.clear(); // clear any existing population
        for (int i = 0; i < config.getPopulationSize(); i++) {
            List<Integer> sequence = new ArrayList<>();
            for (int j = 0; j < horizon; j++) {
                sequence.add(random.nextInt(actionSpaceSize)); // random index for each step
            }
            population.add(new Individual_Action(sequence)); // add new individual
        }
    }

    /**
     * Simple equality: two RHEA_Agents are considered equal if they are the same class
     * (ignores population, forward model, or config)
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof RHEA_Agent;
    }
}