package com.ceruti.bov.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;

@Data
public class SimulationAggregateResult {

    @Id
    String id;
    private Map<String, SimulationAggregateResultElement> results;

}
