package com.ceruti.bov.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationAggregateResult {

    @Id
    String id;
    private Map<String, SimulationAggregateResultElement> results;

    private Date created = new Date();

}
