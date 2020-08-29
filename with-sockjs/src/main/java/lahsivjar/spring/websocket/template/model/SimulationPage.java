package lahsivjar.spring.websocket.template.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationPage {

    private SimulationAggregateResult aggregateResult;

    private List<BettingExecutionMetaResults> bettingExecutionMetaResultsList;

    private long pages;
}
