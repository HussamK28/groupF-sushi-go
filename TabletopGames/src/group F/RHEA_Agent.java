import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
public class RHEA_Agent {
    public void initaliseAgent(AbstractGameState state) {
        ArrayList population = new ArrayList<>();
        final int populationSize = 20;
        final int horizonSize = 5;
        final int maxGenerations = 20;
        // bestPlanOfAction stores the best move for the agent to take
        String bestPlanOfAction = null;

        final Random random = new Random();


        HashMap <> opponentModels = new HashMap<>();
        for (int i=0; i<state.getNPlayers(); i++) {
            if(i !=state.getCurrentPlayer()) {
                opponentModels.put(new opponentModel());
            }
        }

        for (int i=0; i<populationSize; i++){

        }

    }
}