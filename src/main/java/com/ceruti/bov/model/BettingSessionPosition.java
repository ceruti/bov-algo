package com.ceruti.bov.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BettingSessionPosition {

    private String outcomeId;
    private String opposingOutcomeId;
    private List<Bet> bets = new ArrayList<>();

    public BettingSessionPosition(String outcomeId, String opposingOutcomeId) {
        this.outcomeId = outcomeId;
        this.opposingOutcomeId = opposingOutcomeId;
    }

    public BettingSessionPosition(String outcomeId, String opposingOutcomeId, Bet initialBet) {
        this(outcomeId, opposingOutcomeId);
        bets.add(initialBet);
    }

    public double getTotalRiskAmount() {
        return bets.stream().filter(bet -> bet.getStatus().equals(Bet.Status.PLACED)).mapToDouble(Bet::getRiskAmount).sum();
    }

    public double getTotalWinAmount() {
        return bets.stream().filter(bet -> bet.getStatus().equals(Bet.Status.PLACED)).mapToDouble(Bet::getWinAmount).sum();
    }

    public double getNetProfitInWinAmount() {
        return bets.stream().filter(bet -> bet.getStatus().equals(Bet.Status.PLACED)).mapToDouble(Bet::getNetProfitInWinAmount).sum();
    }

    public double getNetProfitInLossAmount() {
        return bets.stream().filter(bet -> bet.getStatus().equals(Bet.Status.PLACED)).mapToDouble(Bet::getNetProfitInLoseAmount).sum();
    }


    public void update(Bet additionalBet) {
        bets.add(additionalBet);
    }

    public BettingSessionPosition clone() {
        BettingSessionPosition result = new BettingSessionPosition();
        result.outcomeId = this.outcomeId;
        result.opposingOutcomeId = this.opposingOutcomeId;
        List<Bet> copyBets = new ArrayList<>();
        for (Bet bet : bets) {
            copyBets.add(bet.clone());
        }
        result.bets = copyBets;
        return result;
    }
}
