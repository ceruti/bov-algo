package com.ceruti.bov.model.betslip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BetResponseEl {

    private String key;
    private BetResponseElDetail response;

}
