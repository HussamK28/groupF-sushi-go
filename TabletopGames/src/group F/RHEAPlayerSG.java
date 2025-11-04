// Import statements needed for this class
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;import java.util.Random;

import core.AbstractPlayer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import core.AbstractGameState;
import core.actions.AbstractAction;

public class RHEAPlayerSG extends AbstractPlayer {
    private List<Individual_Action> population;
    private Individual_Action bestIndividual;
    private HashMap<Integer, OpponentModel> opponentModels;
    private Random random;
    private int myPlayerId;


    private final RHEA_Config config;
    private final RHEA_Evaluator evaluator;
    private Random random1;
    

    public RHEAPlayerSG(RHEA_Config c) {
        this.evaluator = new RHEA_Evaluator(config);
        this.population = new ArrayList<>();
        this.opponentModels = new HashMap<>();
        this.random = new Random(c.seed);
    }


    
    public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }

    public int getPopulationSize() { return populationSize; }
    public int getHorizon() { return horizon; }
    public int getGenerations() { return generations; }
    public double getMutationRate() { return mutationRate; }
    public int getTimeLimitMs() { return timeLimitMs; }
    
    public String getJson(){

        return jsonObj.toJSONString();
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AbstractPlayer copy() {
        return new RHEAPlayerSG(this.config);
    }

}
