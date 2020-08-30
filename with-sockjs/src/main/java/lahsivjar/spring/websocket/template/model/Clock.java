package lahsivjar.spring.websocket.template.model;

import lombok.Data;

@Data
public class Clock {

    private String period;
    private int periodNumber;
    private String gameTime;
    private boolean isTicking;

    public Clock clone() {
        Clock result = new Clock();
        result.period = this.period;
        result.periodNumber = this.periodNumber;
        result.gameTime = this.gameTime;
        result.isTicking = this.isTicking;
        return result;
    }

}
