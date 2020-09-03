package com.ceruti.bov.model.betslip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BetResponseError {

    private String code;
    private String message;

}
