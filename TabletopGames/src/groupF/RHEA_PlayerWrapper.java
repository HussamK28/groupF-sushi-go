
import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import players.PlayerParameters;
import games.sushigo.SGForwardModel;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;

import java.util.List;

public class RHEA_PlayerWrapper extends  AbstractPlayer{
    private RHEA_Agent agent;
    private String name;

    /**
     * Constructor.
     *
     * @param agent  The RHEA_Agent instance to delegate decisions to
     * @param params Player parameters
     * @param name   Player name
     */
    public RHEA_PlayerWrapper(RHEA_Agent agent, PlayerParameters params, String name) {
        super(params, name);
        this.agent = agent;
    }

    /**
     * Initialize the agent at the start of the game.
     */
    @Override
    public void initializePlayer(AbstractGameState state) {
        if (state instanceof SGGameState sgs) {
            agent.initializePlayer(sgs);
        }
    }

    /**
     * Main action selection method. Delegates to RHEA_Agent.
     */
    @Override
    public AbstractAction _getAction(AbstractGameState state, List<AbstractAction> possibleActions) {
        if (!(state instanceof SGGameState sgs)) return null;

        // Convert generic AbstractAction list to ChooseCard list for the agent
        List<ChooseCard> chooseCardActions = possibleActions.stream()
                .map(a -> (ChooseCard) a)
                .toList();

        return agent.getAction(sgs, chooseCardActions);
    }
    /**
     * Required copy method for TAG framework.
     */
    @Override
    public AbstractPlayer copy() {
        // Create a new wrapper around the same agent
        return new RHEA_PlayerWrapper(agent, parameters, name);
    }
}
