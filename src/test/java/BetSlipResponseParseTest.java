import com.ceruti.bov.model.betslip.BetSlipResponse;
import com.ceruti.bov.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BetSlipResponseParseTest {

    @Test
    public void testParseFail() throws Exception {
        String rawJson = "{\n" +
                "   \"channel\":\"WEB_BS\",\n" +
                "   \"device\":\"DESKTOP\",\n" +
                "   \"selections\":{\n" +
                "      \"selection\":[\n" +
                "         {\n" +
                "            \"outcomeId\":\"680278162\",\n" +
                "            \"priceId\":\"5810967894\",\n" +
                "            \"system\":\"A\",\n" +
                "            \"points\":0.0,\n" +
                "            \"oddsFormat\":\"AMERICAN\",\n" +
                "            \"id\":0\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"groups\":{\n" +
                "      \"group\":[\n" +
                "         {\n" +
                "            \"type\":\"STRAIGHT\",\n" +
                "            \"groupSelections\":[\n" +
                "               {\n" +
                "                  \"groupSelection\":[\n" +
                "                     {\n" +
                "                        \"selectionId\":0,\n" +
                "                        \"order\":0,\n" +
                "                        \"isBanker\":false\n" +
                "                     }\n" +
                "                  ]\n" +
                "               }\n" +
                "            ],\n" +
                "            \"id\":0\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"bets\":{\n" +
                "      \"bet\":[\n" +
                "         {\n" +
                "            \"betType\":\"SINGLE\",\n" +
                "            \"betTypeDescription\":\"Single\",\n" +
                "            \"betGroups\":{\n" +
                "               \"groupId\":[\n" +
                "                  0\n" +
                "               ]\n" +
                "            },\n" +
                "            \"stakePerLine\":50,\n" +
                "            \"totalStake\":50,\n" +
                "            \"price\":\"1.02\",\n" +
                "            \"priceFormatted\":\"-5000\",\n" +
                "            \"isBox\":false,\n" +
                "            \"buyPoints\":0.0,\n" +
                "            \"isFreebet\":false,\n" +
                "            \"inRunning\":true,\n" +
                "            \"oddsFormat\":\"AMERICAN\",\n" +
                "            \"potentialReturns\":51,\n" +
                "            \"betCount\":1,\n" +
                "            \"key\":\"77c82510-74f8-36fa-ae93-abcb057af288\",\n" +
                "            \"betRef\":\"20091176102329\",\n" +
                "            \"response\":{\n" +
                "               \"status\":\"FAIL\",\n" +
                "               \"system\":\"A\",\n" +
                "               \"error\":[\n" +
                "                  {\n" +
                "                     \"args\":{\n" +
                "                        \"marketDescription\":\"Moneyline\",\n" +
                "                        \"eventDescription\":\"San Diego Padres @ Los Angeles Angels\"\n" +
                "                     },\n" +
                "                     \"code\":\"betRejectedMessage_MarketUnavailable\",\n" +
                "                     \"message\":\"The selection you have made is not available.\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            \"isReferrable\":false,\n" +
                "            \"specifyingRisk\":true,\n" +
                "            \"cashoutAvailable\":false\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"totalStake\":0,\n" +
                "   \"betDelay\":0.0,\n" +
                "   \"key\":\"3999ddca-d988-3a2f-a211-c8db09ddcb5c\",\n" +
                "   \"status\":\"SUCCESS\",\n" +
                "   \"userAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36\",\n" +
                "   \"written\":1599107975084\n" +
                "}";
        BetSlipResponse betSlipResponse = ObjectMapperUtil.readValue(rawJson, BetSlipResponse.class);
        System.out.println(betSlipResponse.getStatus());
        System.out.println(betSlipResponse.getErrorCode());
        System.out.println(betSlipResponse.getErrorMessage());
    }

    @Test
    public void testParseSuccess() throws IOException {
        String rawJSON = "{\n" +
                "   \"channel\":\"WEB_BS\",\n" +
                "   \"device\":\"DESKTOP\",\n" +
                "   \"selections\":{\n" +
                "      \"selection\":[\n" +
                "         {\n" +
                "            \"outcomeId\":\"680278162\",\n" +
                "            \"priceId\":\"5810921881\",\n" +
                "            \"system\":\"A\",\n" +
                "            \"points\":0.0,\n" +
                "            \"oddsFormat\":\"AMERICAN\",\n" +
                "            \"id\":0\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"groups\":{\n" +
                "      \"group\":[\n" +
                "         {\n" +
                "            \"type\":\"STRAIGHT\",\n" +
                "            \"groupSelections\":[\n" +
                "               {\n" +
                "                  \"groupSelection\":[\n" +
                "                     {\n" +
                "                        \"selectionId\":0,\n" +
                "                        \"order\":0,\n" +
                "                        \"isBanker\":false\n" +
                "                     }\n" +
                "                  ]\n" +
                "               }\n" +
                "            ],\n" +
                "            \"id\":0\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"bets\":{\n" +
                "      \"bet\":[\n" +
                "         {\n" +
                "            \"betType\":\"SINGLE\",\n" +
                "            \"betGroups\":{\n" +
                "               \"groupId\":[\n" +
                "                  0\n" +
                "               ]\n" +
                "            },\n" +
                "            \"stakePerLine\":50,\n" +
                "            \"isBox\":false,\n" +
                "            \"buyPoints\":0.0,\n" +
                "            \"isFreebet\":false,\n" +
                "            \"inRunning\":false,\n" +
                "            \"oddsFormat\":\"AMERICAN\",\n" +
                "            \"key\":\"32781053-9ee0-3c43-a81c-8ba474927fcb\",\n" +
                "            \"response\":{\n" +
                "               \"status\":\"PLACING\"\n" +
                "            },\n" +
                "            \"isReferrable\":false,\n" +
                "            \"specifyingRisk\":true,\n" +
                "            \"cashoutAvailable\":false\n" +
                "         }\n" +
                "      ]\n" +
                "   },\n" +
                "   \"totalStake\":0,\n" +
                "   \"betDelay\":0.0,\n" +
                "   \"key\":\"68ece4be-84c3-3350-bfc3-3f2268ad4fa6\",\n" +
                "   \"status\":\"PLACING\",\n" +
                "   \"userAgent\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36\",\n" +
                "   \"written\":1599106016944\n" +
                "}";
        BetSlipResponse betSlipResponse = ObjectMapperUtil.readValue(rawJSON, BetSlipResponse.class);
        System.out.println(betSlipResponse.getStatus());
        System.out.println(betSlipResponse.getErrorCode());
        System.out.println(betSlipResponse.getErrorMessage());

    }

}
