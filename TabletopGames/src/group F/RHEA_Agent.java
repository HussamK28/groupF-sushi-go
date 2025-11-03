// Import statements needed for this class
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
// The Rhea Agent class definition defines all the major variables for this class
public class RHEA_Agent {
    private List<Individual_Action> population;
    private Individual_Action bestIndividual;
    private HashMap<Integer, OpponentModel> opponentModels;
    private Random random;
    private int myPlayerId;

    private final RHEA_Config config;
    private final RHEA_Evaluator evaluator;

    public RHEA_Agent(RHEA_Config config) {
        this.config = config;
        this.evaluator = new RHEA_Evaluator(config);
        this.population = new ArrayList<>();
        this.opponentModels = new HashMap<>();
        this.random = new Random();
    }

    // Intialise agent creates our agent and the opponent model that our agent uses
    public void initialiseAgent(AbstractGameState state) {
        myPlayerId = state.getCurrentPlayer();
        opponentModels.clear();

        // For loop to add each player to a new opponent model each
        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != state.getCurrentPlayer()) {
                opponentModels.put(i, new OpponentModel());
            }
        }

        // Initialize random population
        population.clear();
        for (int i = 0; i < config.getPopulationSize(); i++) {
            List<Integer> actions = randomActionSequence(config.getHorizon(), state);
            population.add(new Individual_Action(actions));
        }
        // Resets opponent model after each game
        for (OpponentModel model : opponentModels.values()) {
            model.reset();
        }
        evaluator.clearCache();
    }

    // This method creates a list of random action sequences
    // and adds a random number to the sequence list
    private List<Integer> randomActionSequence(int length, AbstractGameState state) {
        List<Integer> sequence = new ArrayList<>();
        int actionSpaceSize = state.getActionSpaceSize();

        for (int i = 0; i < length; i++) {
            sequence.add(random.nextInt(actionSpaceSize));
        }
        return sequence;
    }
    // This function involves evolution to change its fitness rate depending on how each move is perceived
    // It iterates through each individual action in the population to evaluate the fitness 
    public void evolution(AbstractGameState state) {
        long start = System.currentTimeMillis();
        bestIndividual = null;

        while (System.currentTimeMillis() - start < config.getTimeLimitMs()) {
            // Evaluate population using the evaluator
            for (Individual_Action ind : population) {
                double fitness = evaluator.evaluate(ind, state.copy(), myPlayerId, opponentModels, random);
                ind.setFitness(fitness);
            }

            // Elitism
            bestIndividual = Collections.max(population, Comparator.comparingDouble(Individual_Action::getFitness));

            // Create next generation
            List<Individual_Action> newPop = new ArrayList<>();
            while (newPop.size() < config.getPopulationSize() - 1) {
                Individual_Action parent = selectParent();
                Individual_Action child = parent.clone();
                child.mutate(state.getActionSpaceSize());
                newPop.add(child);
            }

            newPop.add(bestIndividual.clone());
            population = newPop;
        }
    }

    // Checks which tournament (t1/t2) to branch to according to their fitness
    private Individual_Action selectParent() {
        int t1 = random.nextInt(population.size());
        int t2 = random.nextInt(population.size());

        if (population.get(t1).getFitness() > population.get(t2).getFitness()) {
            return population.get(t1);
        } else {
            return population.get(t2);
        }
    }
    // Gets the next action based off of best performing individual in action sequence
    public int getNextAction() {
        if (bestIndividual == null) {
            throw new IllegalStateException("Agent not evolved yet!");
        }
        return bestIndividual.getActionSequence().get(0);
    }

    // observes the actions that the opponent model has performed
    public void observeOpponentAction(int opponentId, int action, List<Integer> validActions) {
        if (opponentModels.containsKey(opponentId)) {
            opponentModels.get(opponentId).actionObserved(opponentId, action, validActions);
        }
    }

}

