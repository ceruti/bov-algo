package lahsivjar.spring.websocket.template.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
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

    public double getNetWinAmount() {
        return bets.stream().filter(bet -> bet.getStatus().equals(Bet.Status.PLACED)).mapToDouble(Bet::getNetWinAmount).sum();
    }

    public double getNetLoseAmount() {
        return bets.stream().filter(bet -> bet.getStatus().equals(Bet.Status.PLACED)).mapToDouble(Bet::getNetLoseAmount).sum();
    }


    public void update(Bet additionalBet) {
        bets.add(additionalBet);
    }
}
