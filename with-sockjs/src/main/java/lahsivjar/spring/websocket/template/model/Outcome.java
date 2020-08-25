package lahsivjar.spring.websocket.template.model;

import lombok.Data;

@Data
public class Outcome {

    private String competitorId;
    private String description;
    private String id;
    private String status;
    private String type;
    private Price price;

}
