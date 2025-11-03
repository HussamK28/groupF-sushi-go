import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RHEA_Agent {
    private List<Individual_Action> population;
    private final int populationSize = 20;
    private final int horizonSize = 5;
    private final int maxGenerations = 20;

    private Individual_Action bestIndividual;
    private Random random;
    private HashMap<Integer, OpponentModel> opponentModels;


    public void initialiseAgent(AbstractGameState state) {
        random = new Random();
        population = new ArrayList<>();
        opponentModels = new HashMap<>();


        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != state.getCurrentPlayer()) {
                opponentModels.put(i, new OpponentModel());
            }
        }

        for (int i = 0; i < populationSize; i++) {
            List<Integer> actions = randomActionSequence(horizonSize, state);
            Individual_Action individual = new Individual_Action(actions);
            population.add(individual);
        }
    }

    private List<Integer> randomActionSequence(int length, AbstractGameState state) {
        List<Integer> sequence = new ArrayList<>();
        int actionSpaceSize = state.getActionSpaceSize();

        for (int i = 0; i < length; i++) {
            sequence.add(random.nextInt(actionSpaceSize));
        }
        return sequence;
    }

    public void evolution(AbstractGameState state) {
        for (int generation = 0; generation < maxGenerations; generation++) {
            List<Individual_Action> newPop = new ArrayList<>();

            while (newPop.size() < populationSize) {
                Individual_Action parent = selectParent();
                Individual_Action child = parent.clone();
                child.mutate(state);
                newPop.add(child);
            }

            population = newPop;

            bestIndividual = Collections.max(population,
                    Comparator.comparingDouble(Individual_Action::getFitness));
        }
    }

    private Individual_Action selectParent() {
        int t1 = random.nextInt(population.size());
        int t2 = random.nextInt(population.size());

        if (population.get(t1).getFitness() > population.get(t2).getFitness()) {
            return population.get(t1);
        } else {
            return population.get(t2);
        }
    }

    public int getNextAction() {
        if (bestIndividual == null) {
            throw new IllegalStateException("Agent not evolved yet!");
        }
        return bestIndividual.getActionSequence().get(0);
    }
}
