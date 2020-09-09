package com.ceruti.bov.model;

import com.ceruti.bov.EventBook;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BettingExecutionMetaResults {

    public Long eventId;
    public String eventDescription;
    public String winningOutcomeDescription;
    public String sport;
    public double profitRealized;

    public boolean winnerWasInitialUnderdog;
    public int favoriteReversals;
    public int worstAmericanOddsForWinner = -1000000;
    public int bestOddsForLoser = 1000000;
    public double winnerOddsStandardDeviation;
    public double loserOddsStandardDeviation;
    public int numBetsPlaced;
    public int numOddsQuoted;

    public BettingExecutionMetaResults(Event event, Market market, Outcome outcome1, Outcome outcome2, String winningOutcomeId) {
        this.eventId = event.getId();
        this.eventDescription = event.getDescription();
        Outcome winningOutcome = outcome1.getId().equals(winningOutcomeId) ? outcome1 : outcome2;
        this.winningOutcomeDescription = winningOutcome.getDescription();
        this.sport = EventBook.getEquivalentKey(event.getSport());
    }
}