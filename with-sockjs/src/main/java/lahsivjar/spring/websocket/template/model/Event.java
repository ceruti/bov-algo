package lahsivjar.spring.websocket.template.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class Event {

    private int id;
    private int homeScore;
    private int visitorScore;
//    private Date vendorLastUpdated;
    private Clock clock;
    private String sport;
    private Map<String, Competitor> competitors;
    private String description;
    private String notes;
    private boolean live;
    private int competitionId;
    private Map<String, Market> markets;

    // TODO: add lines here


}
