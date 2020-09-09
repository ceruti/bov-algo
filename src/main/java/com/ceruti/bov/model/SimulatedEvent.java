package com.ceruti.bov.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class SimulatedEvent extends Event {

    public SimulatedEvent(Event event) {
        // not a deep clone, but that's fine because we only need to cast before saving to DB
        this.id = event.id;
        this.homeScore = event.homeScore;
        this.visitorScore = event.visitorScore;
        this.currentPeriodHomeScore = event.currentPeriodHomeScore;
        this.currentPeriodVisitorScore = event.currentPeriodVisitorScore;
        this.clock = event.clock;
        this.sport = event.sport;
        this.competitors = event.competitors;
        this.description = event.description;
        this.notes = event.notes;
        this.live = event.live;
        this.competitionId = event.competitionId;
        this.markets = event.markets;
        this.gameStatus = event.gameStatus;
        this.lastUpdated = event.lastUpdated;
        this.createdAt = event.createdAt;
        this.version = event.version;
        this.rawWireMessages = event.rawWireMessages;
        this.rawEventSummaries = event.rawWireMessages;
        this.bettingEnabled = event.bettingEnabled;
    }

}
