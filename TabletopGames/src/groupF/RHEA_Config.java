
import players.PlayerParameters;


public class RHEA_Config extends PlayerParameters {
    private int populationSize;
    private int horizon;
    private int generations;
    private double mutationRate;
    private int timeLimitMs;

    public RHEA_Config() {
        this(20, 5, 15, 0.2, 100);
    }

    public RHEA_Config(int populationSize, int horizon, int generations, double mutationRate, int timeLimitMs) {
        if (populationSize <= 0) throw new IllegalArgumentException("Population size must be > 0");

        this.populationSize = populationSize;
        this.horizon = horizon;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.timeLimitMs = timeLimitMs;
    }

    // Getters
    public int getPopulationSize() { return populationSize; }
    public int getHorizon() { return horizon; }
    public int getGenerations() { return generations; }
    public double getMutationRate() { return mutationRate; }
    public int getTimeLimitMs() { return timeLimitMs; }

    // Optional setters if needed
    public void setMutationRate(double mutationRate) { this.mutationRate = mutationRate; }

    //for JSON loading
    //public static RHEA_Config fromJSON(String filePath) { ... }

    @Override
    public void _reset() {
        super._reset();
        populationSize = (int) getParameterValue("populationSize");
        horizon = (int) getParameterValue("horizon");
        generations = (int) getParameterValue("generations");
        mutationRate = (double) getParameterValue("mutationRate");
    }

    @Override
    protected RHEA_Config  _copy() {
        // All the copying is done in TunableParameters.copy()
        // Note that any *local* changes of parameters will not be copied
        // unless they have been 'registered' with setParameterValue("name", value)
        return new RHEA_Config();
    }

    @Override
    public IStateHeuristic getStateHeuristic() {
        return AbstractGameState::getGameScore;
    }

    @Override
    public BasicMCTSPlayer instantiate() {
        return new BasicMCTSPlayer((BasicMCTSParams) this.copy());
    }
}