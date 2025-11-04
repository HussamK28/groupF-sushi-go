import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// This class determines an individual action that the agent could take
public class Individual_Action {
    private List<Integer> actionSequence;
    private double fitness;
    private static final Random rnd = new Random();

    public Individual_Action(List<Integer> actions) {
        this.actionSequence = new ArrayList<>(actions);
        this.fitness = Double.NEGATIVE_INFINITY;
    }

    // Getter and setter functions
    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<Integer> getActionSequence() {
        return actionSequence;
    }

    // Creates a copy of the agent so it can be used for evolution
    public Individual_Action clone() {
        return new Individual_Action(new ArrayList<>(this.actionSequence));
    }

    // Adds variation so the child agent (clone) is acting different to its parent.
    public void mutate(int actionSpaceSize) {
        int index = rnd.nextInt(actionSequence.size());
        actionSequence.set(index, rnd.nextInt(actionSpaceSize));
    }
}
