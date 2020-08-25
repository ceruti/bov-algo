package lahsivjar.spring.websocket.template.model;

import lombok.Data;

@Data
public class Clock {

    private String period;
    private int periodNumber;
    private String gameTime;
    private boolean isTicking;

}
