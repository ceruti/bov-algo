import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.model.Market;
import lahsivjar.spring.websocket.template.model.Outcome;
import lahsivjar.spring.websocket.template.util.EventParseUtil;
import lahsivjar.spring.websocket.template.util.LiveOddsUpdateUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LiveOddsUpdateUtilTest {

    private Event event;

    private String LAKERS_BLAZERS_EVENT_INIT = "{\n" +
            "            \"id\":\"7625799\",\n" +
            "            \"description\":\"Los Angeles Lakers @ Portland Trail Blazers\",\n" +
            "            \"type\":\"GAMEEVENT\",\n" +
            "            \"link\":\"/basketball/nba/los-angeles-lakers-portland-trail-blazers-202008242100\",\n" +
            "            \"status\":\"O\",\n" +
            "            \"sport\":\"BASK\",\n" +
            "            \"startTime\":1598317200000,\n" +
            "            \"live\":true,\n" +
            "            \"awayTeamFirst\":true,\n" +
            "            \"denySameGame\":\"NO\",\n" +
            "            \"teaserAllowed\":true,\n" +
            "            \"competitionId\":\"2958468\",\n" +
            "            \"notes\":\"Best of 7 - Game 4 - Los Angeles leads series 2-1 - At AdventHealth Arena - Orlando, FL\",\n" +
            "            \"numMarkets\":55,\n" +
            "            \"lastModified\":1598318957767,\n" +
            "            \"competitors\":[\n" +
            "               {\n" +
            "                  \"id\":\"7625799-11757550\",\n" +
            "                  \"name\":\"Portland Trail Blazers\",\n" +
            "                  \"home\":true\n" +
            "               },\n" +
            "               {\n" +
            "                  \"id\":\"7625799-11757498\",\n" +
            "                  \"name\":\"Los Angeles Lakers\",\n" +
            "                  \"home\":false\n" +
            "               }\n" +
            "            ],\n" +
            "            \"displayGroups\":[\n" +
            "               {\n" +
            "                  \"id\":\"100-97\",\n" +
            "                  \"description\":\"Game Lines\",\n" +
            "                  \"defaultType\":true,\n" +
            "                  \"alternateType\":false,\n" +
            "                  \"markets\":[\n" +
            "                     {\n" +
            "                        \"id\":\"125690446\",\n" +
            "                        \"descriptionKey\":\"Main Dynamic Asian Handicap\",\n" +
            "                        \"description\":\"Point Spread\",\n" +
            "                        \"key\":\"2W-HCAP\",\n" +
            "                        \"marketTypeId\":\"120726\",\n" +
            "                        \"status\":\"O\",\n" +
            "                        \"singleOnly\":false,\n" +
            "                        \"notes\":\"\",\n" +
            "                        \"period\":{\n" +
            "                           \"id\":\"341\",\n" +
            "                           \"description\":\"Live Match\",\n" +
            "                           \"abbreviation\":\"M\",\n" +
            "                           \"live\":true,\n" +
            "                           \"main\":true\n" +
            "                        },\n" +
            "                        \"outcomes\":[\n" +
            "                           {\n" +
            "                              \"id\":\"674802829\",\n" +
            "                              \"description\":\"Los Angeles Lakers\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"A\",\n" +
            "                              \"competitorId\":\"7625799-11757498\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5704874790\",\n" +
            "                                 \"handicap\":\"-11.5\",\n" +
            "                                 \"american\":\"-110\",\n" +
            "                                 \"decimal\":\"1.909091\",\n" +
            "                                 \"fractional\":\"10/11\",\n" +
            "                                 \"malay\":\"0.91\",\n" +
            "                                 \"indonesian\":\"-1.10\",\n" +
            "                                 \"hongkong\":\"0.91\"\n" +
            "                              }\n" +
            "                           },\n" +
            "                           {\n" +
            "                              \"id\":\"674802828\",\n" +
            "                              \"description\":\"Portland Trail Blazers\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"H\",\n" +
            "                              \"competitorId\":\"7625799-11757550\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5704874789\",\n" +
            "                                 \"handicap\":\"11.5\",\n" +
            "                                 \"american\":\"-120\",\n" +
            "                                 \"decimal\":\"1.833333\",\n" +
            "                                 \"fractional\":\"5/6\",\n" +
            "                                 \"malay\":\"0.83\",\n" +
            "                                 \"indonesian\":\"-1.20\",\n" +
            "                                 \"hongkong\":\"0.83\"\n" +
            "                              }\n" +
            "                           }\n" +
            "                        ]\n" +
            "                     },\n" +
            "                     {\n" +
            "                        \"id\":\"125690428\",\n" +
            "                        \"descriptionKey\":\"Head To Head\",\n" +
            "                        \"description\":\"Moneyline\",\n" +
            "                        \"key\":\"2W-12\",\n" +
            "                        \"marketTypeId\":\"3059\",\n" +
            "                        \"status\":\"O\",\n" +
            "                        \"singleOnly\":false,\n" +
            "                        \"notes\":\"\",\n" +
            "                        \"period\":{\n" +
            "                           \"id\":\"341\",\n" +
            "                           \"description\":\"Live Match\",\n" +
            "                           \"abbreviation\":\"M\",\n" +
            "                           \"live\":true,\n" +
            "                           \"main\":true\n" +
            "                        },\n" +
            "                        \"outcomes\":[\n" +
            "                           {\n" +
            "                              \"id\":\"674802793\",\n" +
            "                              \"description\":\"Los Angeles Lakers\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"A\",\n" +
            "                              \"competitorId\":\"7625799-11757498\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5704874598\",\n" +
            "                                 \"american\":\"-550\",\n" +
            "                                 \"decimal\":\"1.181818\",\n" +
            "                                 \"fractional\":\"2/11\",\n" +
            "                                 \"malay\":\"0.18\",\n" +
            "                                 \"indonesian\":\"-5.50\",\n" +
            "                                 \"hongkong\":\"0.18\"\n" +
            "                              }\n" +
            "                           },\n" +
            "                           {\n" +
            "                              \"id\":\"674802792\",\n" +
            "                              \"description\":\"Portland Trail Blazers\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"H\",\n" +
            "                              \"competitorId\":\"7625799-11757550\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5704874597\",\n" +
            "                                 \"american\":\"+355\",\n" +
            "                                 \"decimal\":\"4.550\",\n" +
            "                                 \"fractional\":\"71/20\",\n" +
            "                                 \"malay\":\"-0.28\",\n" +
            "                                 \"indonesian\":\"3.55\",\n" +
            "                                 \"hongkong\":\"3.55\"\n" +
            "                              }\n" +
            "                           }\n" +
            "                        ]\n" +
            "                     },\n" +
            "                     {\n" +
            "                        \"id\":\"125690419\",\n" +
            "                        \"descriptionKey\":\"Main Dynamic Over/Under\",\n" +
            "                        \"description\":\"Total\",\n" +
            "                        \"key\":\"2W-OU\",\n" +
            "                        \"marketTypeId\":\"120725\",\n" +
            "                        \"status\":\"O\",\n" +
            "                        \"singleOnly\":false,\n" +
            "                        \"notes\":\"\",\n" +
            "                        \"period\":{\n" +
            "                           \"id\":\"341\",\n" +
            "                           \"description\":\"Live Match\",\n" +
            "                           \"abbreviation\":\"M\",\n" +
            "                           \"live\":true,\n" +
            "                           \"main\":true\n" +
            "                        },\n" +
            "                        \"outcomes\":[\n" +
            "                           {\n" +
            "                              \"id\":\"674802774\",\n" +
            "                              \"description\":\"Over\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"O\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5704889483\",\n" +
            "                                 \"handicap\":\"221.5\",\n" +
            "                                 \"american\":\"-115\",\n" +
            "                                 \"decimal\":\"1.870\",\n" +
            "                                 \"fractional\":\"20/23\",\n" +
            "                                 \"malay\":\"0.87\",\n" +
            "                                 \"indonesian\":\"-1.15\",\n" +
            "                                 \"hongkong\":\"0.87\"\n" +
            "                              }\n" +
            "                           },\n" +
            "                           {\n" +
            "                              \"id\":\"674802775\",\n" +
            "                              \"description\":\"Under\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"U\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5704889484\",\n" +
            "                                 \"handicap\":\"221.5\",\n" +
            "                                 \"american\":\"-115\",\n" +
            "                                 \"decimal\":\"1.870\",\n" +
            "                                 \"fractional\":\"20/23\",\n" +
            "                                 \"malay\":\"0.87\",\n" +
            "                                 \"indonesian\":\"-1.15\",\n" +
            "                                 \"hongkong\":\"0.87\"\n" +
            "                              }\n" +
            "                           }\n" +
            "                        ]\n" +
            "                     }\n" +
            "                  ],\n" +
            "                  \"order\":1\n" +
            "               }\n" +
            "            ]\n" +
            "         }";

    private String OKC_ODDS_UPDATE_WIRE_MESSAGE = "\"" +
            "{" +
                "\\\"type\\\":\\\"outcome\\\"," +
                "\\\"id\\\":\\\"674689262\\\"" +
            "}" +
            "|" +
            "{" +
            "\\\"id\\\":\\\"674689262\\\"," +
            "\\\"description\\\":\\\"Oklahoma City Thunder\\\"," +
            "\\\"status\\\":\\\"O\\\"," +
            "\\\"type\\\":\\\"H\\\"," +
            "\\\"competitorId\\\":\\\"7625801-11757547\\\"," +
            "\\\"price\\\":{" +
                "\\\"id\\\":\\\"5703471780\\\"," +
                "\\\"american\\\":\\\"+115\\\"," +
                "\\\"decimal\\\":\\\"2.150\\\"," +
                "\\\"fractional\\\":\\\"23/20\\\"," +
                "\\\"malay\\\":\\\"-0.87\\\"," +
                "\\\"indonesian\\\":\\\"1.15\\\"," +
                "\\\"hongkong\\\":\\\"1.15\\\"" +
                "}" +
            "}\"";

    private String TRAIL_BLAZERS_MONEY_LINE_WIRE_UPDATE = "\"{\\\"type\\\":\\\"outcome\\\",\\\"id\\\":\\\"674802792\\\"}|{\\\"id\\\":\\\"674802792\\\",\\\"description\\\":\\\"Portland Trail Blazers\\\",\\\"status\\\":\\\"O\\\",\\\"type\\\":\\\"H\\\",\\\"competitorId\\\":\\\"7625799-11757550\\\",\\\"price\\\":{\\\"id\\\":\\\"5704960880\\\",\\\"american\\\":\\\"+475\\\",\\\"decimal\\\":\\\"5.750\\\",\\\"fractional\\\":\\\"19/4\\\",\\\"malay\\\":\\\"-0.21\\\",\\\"indonesian\\\":\\\"4.75\\\",\\\"hongkong\\\":\\\"4.75\\\"}}\"";
    private String LAKERS_MONEY_LINE_WIRE_UPEATE = "\"{\\\"type\\\":\\\"outcome\\\",\\\"id\\\":\\\"674802793\\\"}|{\\\"id\\\":\\\"674802793\\\",\\\"description\\\":\\\"Los Angeles Lakers\\\",\\\"status\\\":\\\"O\\\",\\\"type\\\":\\\"A\\\",\\\"competitorId\\\":\\\"7625799-11757498\\\",\\\"price\\\":{\\\"id\\\":\\\"5704960881\\\",\\\"american\\\":\\\"-850\\\",\\\"decimal\\\":\\\"1.1176\\\",\\\"fractional\\\":\\\"2/17\\\",\\\"malay\\\":\\\"0.12\\\",\\\"indonesian\\\":\\\"-8.50\\\",\\\"hongkong\\\":\\\"0.12\\\"}}\"";


    @BeforeEach
    public void parseEvent() {
        JSONObject eventJSON = new JSONObject(LAKERS_BLAZERS_EVENT_INIT);
        this.event = EventParseUtil.parseEvent(eventJSON);
    }

    @Test
    public void testGetEventId() {
        Long eventId = LiveOddsUpdateUtil.getEventId(LAKERS_MONEY_LINE_WIRE_UPEATE);
        Assertions.assertEquals(7625799, eventId);
    }

    @Test
    public void testUpdateOdds() {
        Market moneyLineMarket = this.event.getMarkets().get("125690428");
        Outcome lakersWinOutcome = moneyLineMarket.getOutcomes().get("674802793");
        Assertions.assertEquals(-550, lakersWinOutcome.getPrice().getAmerican());
        LiveOddsUpdateUtil.updateEvent(this.event, LAKERS_MONEY_LINE_WIRE_UPEATE);
        Assertions.assertEquals(-850, lakersWinOutcome.getPrice().getAmerican());
    }

}
