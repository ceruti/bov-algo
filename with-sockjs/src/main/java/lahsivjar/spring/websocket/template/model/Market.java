package lahsivjar.spring.websocket.template.model;

import lombok.Data;

import java.util.Map;

@Data
public class Market {

    private String description;
    private String descriptionKey;
    private String id;
    private String key;
    private String marketTypeId;
    private String notes;
    private Map<String, Outcome> outcomes;

}
