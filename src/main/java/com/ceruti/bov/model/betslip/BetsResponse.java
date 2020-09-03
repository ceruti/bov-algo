package com.ceruti.bov.model.betslip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BetsResponse {

    private List<BetResponseEl> bet;

}
