import org.jsoup.select.Evaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RHEA_Agent {
    private List<Individual> population;
    private Individual bestPlanOfAction;
    private HashMap<Integer, OpponentModel> opponentModels;
    private RHEA_Config config;
    private Evaluator evaluator;
    private EvolutionEngine evolution;


    public RHEA_Agent(RHEA_Config config, Evaluator evaluator, EvolutionEngine evolution){
        this.config = config;
        this.evaluator = evaluator;
        this.evolution = evolution;
        this.population = new ArrayList<>();
        this.opponentModels = new HashMap<>();
    }


    public void initaliseAgent(AbstractGameState state) {
        this.population.clear();
        this.bestPlanOfAction = null;
        this.opponentModels.clear();

        for (int i = 0; i < state.getNPlayers(); i++) {
            if (i != state.getCurrentPlayer()) {
                this.opponentModels.put(i, new OpponentModel(i));
            }
        }
    }

    public int selectAction(AbstractGameState state) {
        this.bestPlanOfAction = evolution.run(state);
        return bestPlanOfAction.getActionSequence().get(0);
    }

}