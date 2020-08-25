package lahsivjar.spring.websocket.template.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Price {

    private int american;
    private String id;
    private Date created = new Date();

    public void updateCreated() {
        created = new Date();
    }

}
