package lahsivjar.spring.websocket.template.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Map;

@Data
public class Event {

    @Id
    private long id;
    private String homeScore;
    private String visitorScore;
//    private Date vendorLastUpdated;
    private Clock clock;
    private String sport;
    private Map<String, Competitor> competitors;
    private String description;
    private String notes;
    private boolean live;
    private int competitionId;
    private Map<String, Market> markets;
    private String gameStatus;

    // TODO: add lines here


}
