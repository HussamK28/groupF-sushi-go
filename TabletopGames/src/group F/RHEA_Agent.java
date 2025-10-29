import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class RHEA_Agent {
    public void initaliseAgent(AbstractGameState state) {
        ArrayList population = new ArrayList<>();
        // bestPlanOfAction stores the best move for the agent to take
        String bestPlanOfAction = null;
        HashMap <> opponentModels = new HashMap<>();
        for (int i=0; i<state.getNPlayers(); i++) {
            if(i !=state.getCurrentPlayer()) {
                opponentModels.put(new opponentModel());
            }
        }

    }
}