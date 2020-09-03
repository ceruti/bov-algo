package com.ceruti.bov.model.betslip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetSlipResponse {

    private String key;
    private BetsResponse bets;

    @JsonIgnore
    public String getStatus() {
        return getResponseDetail().getStatus();
    }

    @JsonIgnore
    public String getErrorCode() {
        if (getError() != null) {
            return getError().getCode();
        }
        return null;
    }

    @JsonIgnore
    public String getErrorMessage() {
        if (getError() != null) {
            return getError().getMessage();
        }
        return null;
    }

    private BetResponseError getError() {
        if (getResponseDetail().getError() == null) {
            return null;
        }
        return getResponseDetail().getError().get(0);
    }

    private BetResponseElDetail getResponseDetail() {
        return bets.getBet().get(0).getResponse();
    }



}
