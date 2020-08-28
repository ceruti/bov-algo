package integration;

import lahsivjar.spring.websocket.template.model.BettingSession;

import java.util.List;
import java.util.Map;

public class BettingSimulationStore {

    Map<Long, BettingSession> bettingSessions;
    private double runningAverageProfit;

}
