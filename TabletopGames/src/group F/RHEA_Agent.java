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
    private final int populationSize = 20;
    private final int horizonSize = 5;
    private final int maxGenerations = 20;
    private int myPlayerId;

    private Individual_Action bestIndividual;
    private Random random;
    private HashMap<Integer, OpponentModel> opponentModels;

    // Intialise agent creates our agent and the opponent model that our agent uses
    public void initialiseAgent(AbstractGameState state) {
        random = new Random();
        population = new ArrayList<>();
        opponentModels = new HashMap<>();
        myPlayerId = state.getCurrentPlayer();

        // For loop to add each player to a new opponent model each
        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != state.getCurrentPlayer()) {
                opponentModels.put(i, new OpponentModel());
            }
        }

        // This for loop iterates through my population and adds an individual action to my population
        for (int i = 0; i < populationSize; i++) {
            List<Integer> actions = randomActionSequence(horizonSize, state);
            Individual_Action individual = new Individual_Action(actions);
            population.add(individual);
        }
        // Resets opponent model after each game
        for (OpponentModel model : opponentModels.values()) {
            model.reset();
        }
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
        for (int generation = 0; generation < maxGenerations; generation++) {
            for (Individual_Action ind : population) {
                double fitness = evaluate(ind, state.copy());
                ind.setFitness(fitness);
            }
            List<Individual_Action> newPop = new ArrayList<>();

            // Checks what the population size is and the child action is inherited from its parents.
            // child action then mutates in the action space and adds child action to pop.
            while (newPop.size() < populationSize) {
                Individual_Action parent = selectParent();
                Individual_Action child = parent.clone();
                child.mutate(state.getActionSpaceSize());
                newPop.add(child);
            }
            // This uses elitism to keep the best from previous generations according to their fitness
            newPop.add(bestIndividual.clone());
            population = newPop;
            for (Individual_Action ind: population) {
                double fitness = evaluate(ind, state.copy());
                ind.setFitness(fitness);
            }

            // calculates the best individual in the population
            bestIndividual = Collections.max(population, Comparator.comparingDouble(Individual_Action::getFitness));
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

    // Evaluates each action in the simulation of the sushiGo game to predict what action is going to be performed
    // next by our opponents
    private double evaluate(Individual_Action ind, AbstractGameState simState) {
        for (int action : ind.getActionSequence()) {
            simState.performAction(simState.getCurrentPlayer(), action);

            while (!simState.isTerminal() && simState.getCurrentPlayer() != myPlayerId) {
                int opponentId = simState.getCurrentPlayer();
                List<Integer> validActions = simState.getValidActions(opponentId);
                OpponentModel model = opponentModels.get(opponentId);
                int predictedAction = model.sampleAction(opponentId, validActions, random);
                simState.performAction(opponentId, predictedAction);
            }
        }

        double score = simState.evaluateGameForPlayer(myPlayerId);
        return score;
    }
}
