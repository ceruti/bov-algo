package com.ceruti.bov.model.betslip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetSlipResponse {

    private String key;
    private BetsResponse bets;
    private List<BetResponseError> error;

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
        if (getResponseDetail().getError() != null) {
            return getResponseDetail().getError().get(0);
        }
        if (error != null) {
            return error.get(0);
        }
        return null;
    }

    private BetResponseElDetail getResponseDetail() {
        return bets.getBet().get(0).getResponse();
    }



}
