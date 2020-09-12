package com.ceruti.bov.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class Competitor {

    private boolean home;
    private String id;
    private String name;

    @JsonIgnore
    public Competitor clone() {
        Competitor result = new Competitor();
        result.home = this.home;
        result.id = this.id;
        result.name = this.name;
        return result;
    }

}
