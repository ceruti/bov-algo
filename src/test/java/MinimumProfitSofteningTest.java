import com.ceruti.bov.model.Price;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class MinimumProfitSofteningTest {

    private double k1 = 1.00;
    private double k2 = 2.00;
    private double k3 = 1.00;
    private double k4 = -3.50;

    private List<DataPoint> getDataPoints() {
        List<DataPoint> dataPoints = new ArrayList<>();
        dataPoints.add(new DataPoint(-105, -100));
        dataPoints.add(new DataPoint(-105, -90));
        dataPoints.add(new DataPoint(-105, -50));
        dataPoints.add(new DataPoint(-105, -20));
        dataPoints.add(new DataPoint(-105, -10));
        dataPoints.add(new DataPoint(-105, -1));

        dataPoints.add(new DataPoint(-130, -100));
        dataPoints.add(new DataPoint(-130, -90));
        dataPoints.add(new DataPoint(-130, -50));
        dataPoints.add(new DataPoint(-130, -20));
        dataPoints.add(new DataPoint(-130, -10));
        dataPoints.add(new DataPoint(-130, -1));

        dataPoints.add(new DataPoint(-300, -100));
        dataPoints.add(new DataPoint(-300, -90));
        dataPoints.add(new DataPoint(-300, -50));
        dataPoints.add(new DataPoint(-300, -20));
        dataPoints.add(new DataPoint(-300, -10));
        dataPoints.add(new DataPoint(-300, -1));
        dataPoints.add(new DataPoint(-300, -.000001));


        dataPoints.add(new DataPoint(300, -100));
        dataPoints.add(new DataPoint(300, -90));
        dataPoints.add(new DataPoint(300, -50));
        dataPoints.add(new DataPoint(300, -20));
        dataPoints.add(new DataPoint(300, -10));
        dataPoints.add(new DataPoint(300, -1));
        dataPoints.add(new DataPoint(300, -.001));

        dataPoints.add(new DataPoint(400, -100));
        dataPoints.add(new DataPoint(400, -90));
        dataPoints.add(new DataPoint(400, -50));
        dataPoints.add(new DataPoint(400, -20));
        dataPoints.add(new DataPoint(400, -10));
        dataPoints.add(new DataPoint(400, -1));
        dataPoints.add(new DataPoint(400, -.001));

//        dataPoints.add(new DataPoint(600, -100));
//        dataPoints.add(new DataPoint(600, -90));
//        dataPoints.add(new DataPoint(600, -50));
//        dataPoints.add(new DataPoint(600, -20));
//        dataPoints.add(new DataPoint(600, -10));
//        dataPoints.add(new DataPoint(600, -1));
//        dataPoints.add(new DataPoint(600, -.001));

        dataPoints.add(new DataPoint(130, -100));
        dataPoints.add(new DataPoint(130, -90));
        dataPoints.add(new DataPoint(130, -50));
        dataPoints.add(new DataPoint(130, -20));
        dataPoints.add(new DataPoint(130, -10));
        dataPoints.add(new DataPoint(130, -1));

        return dataPoints;
    }

    @Test
    public void t() {
        System.out.println("ODDS, MINIMUM_PROFIT, BET, BET_PRIME");
        for (DataPoint dataPoint : getDataPoints()) {
            System.out.println(String.format("%d, %.2f, %.2f, %.2f",
                    dataPoint.american,
                    dataPoint.minimumProfit,
                    getRegularBetAmount(dataPoint.american),
                    getNewBetAmount(dataPoint.american, dataPoint.minimumProfit)));
        }
    }

    private double getNewBetAmount(int american, double minimumProfit) {
        return getRegularBetAmount(american) * getSofteningFactor(american > 0, minimumProfit);
    }

    private double getSofteningFactor(boolean positiveOdds, double minimumProfit) {
        double d = Math.abs(minimumProfit / 50.0);
        if (positiveOdds) {
            return k3 * Math.pow(Math.E, k4 * d);
        }
        return k1 * Math.pow(Math.E, k2 * d);
    }

    private double getRegularBetAmount(int american) {
        return 1.5 * Math.pow(winMultiplier(american), 2.5);
    }

    protected static double winMultiplier(int american) {
        if (american > 0) {
            return american / 100.0;
        }
        return Math.abs(100.0 / american);
    }

    @AllArgsConstructor
    private static class DataPoint {
        int american;
        double minimumProfit;
    }


}
