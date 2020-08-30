package lahsivjar.spring.websocket.template.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Price {

    private int american;
    private String id;
    private Date created = new Date();
    private Clock clockAtTimeOfPrice;
    private String homeScoreAtTimeOfPrice;
    private String visitorScoreAtTimeOfPrice;

    public Price(int american, String id, Clock clockAtTimeOfPrice, String homeScoreAtTimeOfPrice, String visitorScoreAtTimeOfPrice) {
        this.american = american;
        this.id = id;
        this.clockAtTimeOfPrice = clockAtTimeOfPrice.clone();
        this.homeScoreAtTimeOfPrice = homeScoreAtTimeOfPrice;
        this.visitorScoreAtTimeOfPrice = visitorScoreAtTimeOfPrice;
    }

    public void updateCreated() {
        created = new Date();
    }

    public Price clone() {
        Price result = new Price();
        result.american = this.american;
        result.id = this.id;
        result.created = this.created;
        return result;
    }

}
