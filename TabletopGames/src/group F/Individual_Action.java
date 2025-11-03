import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Individual_Action {
    private List<Integer> actionSequence;
    private double fitness;

    public Individual_Action(List<Integer> actions) {
        this.actionSequence = new ArrayList<>(actions);
        this.fitness = Double.NEGATIVE_INFINITY;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public List<Integer> getActionSequence() {
        return actionSequence;
    }

    public Individual_Action clone() {
        return new Individual_Action(new ArrayList<>(this.actionSequence));
    }

    public void mutate(int actionSpaceSize) {
        int index = rnd.nextInt(actionSequence.size());
        actionSequence.set(index, rnd.nextInt(actionSpaceSize));
    }
}
