package integration.multirun;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "strategy_gorilla"})
public class GorillaIT extends MultiParameterStrategySimulationExecution {

    @Override
    protected String getStrategyName() {
        return "GORILLA";
    }
}
