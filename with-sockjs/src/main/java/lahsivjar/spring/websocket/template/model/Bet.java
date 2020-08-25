package lahsivjar.spring.websocket.template.model;

import lombok.Data;

@Data
public class Bet {

    private Price price;
    private Status status;
    private double riskAmount;
    private double winAmount;

    public void markPlaced() {
        this.status = Status.PLACED;
    }

    enum Status {
        PLACING, PLACED, FAILED
    }

    public Bet(Price price, double riskAmount) {
        this.price = price;
        this.riskAmount = riskAmount;
        this.status = Status.PLACING;
        this.winAmount = getWinAmount(price, riskAmount);
    }

    private double getWinAmount(Price price, double riskAmount) {
        if (price.getAmerican() > 0) {
            return riskAmount * (price.getAmerican() / 100.0);
        }
        return riskAmount * Math.abs(100.0 / price.getAmerican());
    }

    public double getNetWinAmount() {
        return riskAmount+winAmount;
    }

    public double getNetLoseAmount() {
        return -riskAmount;
    }

    public boolean isPlaced() {
        return status.equals(Status.PLACED);
    }

}
