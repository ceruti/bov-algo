package com.ceruti.bov;

import com.ceruti.bov.model.Bet;
import com.ceruti.bov.model.Price;
import com.ceruti.bov.model.betslip.BetSlipResponse;
import com.ceruti.bov.util.ObjectMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("live")
public class BetPlacingServiceLive implements BetPlacingService {

    public static final String PLACE_BET_URL = "https://services.bovada.lv/services/sports/bet/betslip";
    public static final int MAX_TRIES = 20;
    public static final String BETSLIP_URL = "https://services.bovada.lv/services/sports/bet/betslip/";

    private String token;

    private RestTemplate restTemplate;

    @Autowired
    public BetPlacingServiceLive() {
        this.restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("Authorization", this.token);
            headers.set("x-session-id", this.token);
            headers.set("Content-Type", "application/json");
            // don't let bovada know we are using java
            headers.set("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
            return execution.execute(request, body);
        });
    }

    public synchronized Bet placeBet(String outcomeId, Price price, double riskAmountInDollars) {
        int amountInCents = (int) (Math.ceil(riskAmountInDollars * 100));
        return placeBet(outcomeId, price, amountInCents);
    }

    // TODO: turn off synchronized? Only synch because Bovada might not like concurrent bets from one account
    public synchronized Bet placeBet(String outcomeId, Price price, int amountInCents) {
        Bet bet = new Bet(price, amountInCents / 100.0);
        try {
            if (token == null) {
                bet.markNoToken();
                return bet;
            }
            ResponseEntity<String> response = submitBetSlip(outcomeId, price, amountInCents);
            boolean failed = response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
            if (failed) {
                bet.markFailed();
                return bet;
            }
            String body = response.getBody();
            bet.getVendorResponses().add(body);
            BetSlipResponse betSlipResponse = ObjectMapperUtil.readValue(body, BetSlipResponse.class);
            bet.setVendorKey(betSlipResponse.getKey());
            if (betSlipResponse.getStatus().equalsIgnoreCase("FAIL")) {
                bet.markFailed();
                bet.setErrorCode(betSlipResponse.getErrorCode());
                bet.setErrorMessage(betSlipResponse.getErrorMessage());
                return bet;
            }
            for (int i = 0; i < MAX_TRIES; i++) {
                String betSlipBodyJSON = restTemplate.getForObject(BETSLIP_URL + bet.getVendorKey(), String.class);
                bet.getVendorResponses().add(betSlipBodyJSON);
                BetSlipResponse _betSlipResponse = ObjectMapperUtil.readValue(betSlipBodyJSON, BetSlipResponse.class);
                if (_betSlipResponse.getStatus().equalsIgnoreCase("SUCCESS")) {
                    bet.markPlaced();
                    return bet;
                }
                if (_betSlipResponse.getStatus().equalsIgnoreCase("FAIL")) {
                    bet.markFailed();
                    bet.setErrorCode(betSlipResponse.getErrorCode());
                    bet.setErrorMessage(betSlipResponse.getErrorMessage());
                    return bet;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // don't do anything
                }
            }
            bet.markTimedOut();
        } catch (Exception e) {
            e.printStackTrace();
            bet.markFailed();
        }
        return bet;
    }

    public ResponseEntity<String> submitBetSlip(String outcomeId, Price price, int amountInCents) {
        String requestBody = BET_REQUEST
                .replace("{{OUTCOME_ID}}", outcomeId)
                .replace("{{PRICE_ID}}", price.getId())
                .replace("{{RISK_AMOUNT_IN_CENTS}}", Integer.toString(amountInCents));
        return restTemplate.postForEntity(PLACE_BET_URL, requestBody, String.class);
    }

    @Override
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private static final String BET_REQUEST = "{\n" +
            "  \"device\": \"DESKTOP\",\n" +
            "  \"channel\": \"WEB_BS\",\n" +
            "  \"selections\": {\n" +
            "    \"selection\": [\n" +
            "      {\n" +
            "        \"outcomeId\": \"{{OUTCOME_ID}}\",\n" +
            "        \"id\": 0,\n" +
            "        \"system\": \"A\",\n" +
            "        \"priceId\": \"{{PRICE_ID}}\",\n" +
            "        \"oddsFormat\": \"AMERICAN\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"groups\": {\n" +
            "    \"group\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"type\": \"STRAIGHT\",\n" +
            "        \"groupSelections\": [\n" +
            "          {\n" +
            "            \"groupSelection\": [\n" +
            "              {\n" +
            "                \"selectionId\": 0,\n" +
            "                \"order\": 0,\n" +
            "                \"isBanker\": false\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"bets\": {\n" +
            "    \"bet\": [\n" +
            "      {\n" +
            "        \"betGroups\": {\n" +
            "          \"groupId\": [\n" +
            "            0\n" +
            "          ]\n" +
            "        },\n" +
            "        \"betType\": \"SINGLE\",\n" +
            "        \"isBox\": false,\n" +
            "        \"oddsFormat\": \"AMERICAN\",\n" +
            "        \"specifyingRisk\": true,\n" +
            "        \"stakePerLine\": {{RISK_AMOUNT_IN_CENTS}}\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";
}
