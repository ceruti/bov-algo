import lahsivjar.spring.websocket.template.model.Event;
import lahsivjar.spring.websocket.template.model.Market;
import lahsivjar.spring.websocket.template.model.Outcome;
import lahsivjar.spring.websocket.template.util.EventParseUtil;
import lahsivjar.spring.websocket.template.util.LiveOddsUpdateUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LiveOddsUpdateUtilTest {

    private Event event;
    private Event sorinEvent;

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

    private String SORIN_MATCH_EVENT = " {\n" +
            "        \"id\":\"7632283\",\n" +
            "        \"description\":\"Maksim Sorin vs Vyacheslav Tsvetkov\",\n" +
            "        \"type\":\"GAMEEVENT\",\n" +
            "        \"link\":\"/table-tennis/russia/liga-pro/maksim-sorin-vyacheslav-tsvetkov-202008250315\",\n" +
            "        \"status\":\"O\",\n" +
            "        \"sport\":\"TABL\",\n" +
            "        \"startTime\":1598339700000,\n" +
            "        \"live\":true,\n" +
            "        \"awayTeamFirst\":false,\n" +
            "        \"denySameGame\":\"NO\",\n" +
            "        \"teaserAllowed\":true,\n" +
            "        \"competitionId\":\"8699120\",\n" +
            "        \"notes\":\"\",\n" +
            "        \"numMarkets\":19,\n" +
            "        \"lastModified\":1598340496539,\n" +
            "        \"competitors\":[\n" +
            "          {\n" +
            "            \"id\":\"7632283-16413311\",\n" +
            "            \"name\":\"Maksim Sorin\",\n" +
            "            \"home\":true\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\":\"7632283-16411196\",\n" +
            "            \"name\":\"Vyacheslav Tsvetkov\",\n" +
            "            \"home\":false\n" +
            "          }\n" +
            "        ],\n" +
            "        \"displayGroups\":[\n" +
            "          {\n" +
            "            \"id\":\"100-1031\",\n" +
            "            \"description\":\"Game Lines\",\n" +
            "            \"defaultType\":true,\n" +
            "            \"alternateType\":false,\n" +
            "            \"markets\":[\n" +
            "              {\n" +
            "                \"id\":\"125711627\",\n" +
            "                \"descriptionKey\":\"Main Dynamic Asian Handicap - Points\",\n" +
            "                \"description\":\"Point Spread - Points\",\n" +
            "                \"key\":\"2W-HCAP\",\n" +
            "                \"marketTypeId\":\"120754\",\n" +
            "                \"status\":\"O\",\n" +
            "                \"singleOnly\":false,\n" +
            "                \"notes\":\"\",\n" +
            "                \"period\":{\n" +
            "                  \"id\":\"308\",\n" +
            "                  \"description\":\"Live Match\",\n" +
            "                  \"abbreviation\":\"MT\",\n" +
            "                  \"live\":true,\n" +
            "                  \"main\":true\n" +
            "                },\n" +
            "                \"outcomes\":[\n" +
            "                  {\n" +
            "                    \"id\":\"674941289\",\n" +
            "                    \"description\":\"Maksim Sorin\",\n" +
            "                    \"status\":\"O\",\n" +
            "                    \"type\":\"H\",\n" +
            "                    \"competitorId\":\"7632283-16413311\",\n" +
            "                    \"price\":{\n" +
            "                      \"id\":\"5706298487\",\n" +
            "                      \"handicap\":\"-4.5\",\n" +
            "                      \"american\":\"-115\",\n" +
            "                      \"decimal\":\"1.870\",\n" +
            "                      \"fractional\":\"20/23\",\n" +
            "                      \"malay\":\"0.87\",\n" +
            "                      \"indonesian\":\"-1.15\",\n" +
            "                      \"hongkong\":\"0.87\"\n" +
            "                    }\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"id\":\"674941290\",\n" +
            "                    \"description\":\"Vyacheslav Tsvetkov\",\n" +
            "                    \"status\":\"O\",\n" +
            "                    \"type\":\"A\",\n" +
            "                    \"competitorId\":\"7632283-16411196\",\n" +
            "                    \"price\":{\n" +
            "                      \"id\":\"5706298488\",\n" +
            "                      \"handicap\":\"4.5\",\n" +
            "                      \"american\":\"-120\",\n" +
            "                      \"decimal\":\"1.833333\",\n" +
            "                      \"fractional\":\"5/6\",\n" +
            "                      \"malay\":\"0.83\",\n" +
            "                      \"indonesian\":\"-1.20\",\n" +
            "                      \"hongkong\":\"0.83\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ]\n" +
            "              },\n" +
            "              {\n" +
            "                \"id\":\"125711661\",\n" +
            "                \"descriptionKey\":\"Head To Head\",\n" +
            "                \"description\":\"Moneyline\",\n" +
            "                \"key\":\"2W-12\",\n" +
            "                \"marketTypeId\":\"1901\",\n" +
            "                \"status\":\"O\",\n" +
            "                \"singleOnly\":false,\n" +
            "                \"notes\":\"\",\n" +
            "                \"period\":{\n" +
            "                  \"id\":\"308\",\n" +
            "                  \"description\":\"Live Match\",\n" +
            "                  \"abbreviation\":\"MT\",\n" +
            "                  \"live\":true,\n" +
            "                  \"main\":true\n" +
            "                },\n" +
            "                \"outcomes\":[\n" +
            "                  {\n" +
            "                    \"id\":\"674941357\",\n" +
            "                    \"description\":\"Maksim Sorin\",\n" +
            "                    \"status\":\"O\",\n" +
            "                    \"type\":\"H\",\n" +
            "                    \"competitorId\":\"7632283-16413311\",\n" +
            "                    \"price\":{\n" +
            "                      \"id\":\"5706298499\",\n" +
            "                      \"american\":\"-305\",\n" +
            "                      \"decimal\":\"1.327869\",\n" +
            "                      \"fractional\":\"20/61\",\n" +
            "                      \"malay\":\"0.33\",\n" +
            "                      \"indonesian\":\"-3.05\",\n" +
            "                      \"hongkong\":\"0.33\"\n" +
            "                    }\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"id\":\"674941358\",\n" +
            "                    \"description\":\"Vyacheslav Tsvetkov\",\n" +
            "                    \"status\":\"O\",\n" +
            "                    \"type\":\"A\",\n" +
            "                    \"competitorId\":\"7632283-16411196\",\n" +
            "                    \"price\":{\n" +
            "                      \"id\":\"5706298500\",\n" +
            "                      \"american\":\"+215\",\n" +
            "                      \"decimal\":\"3.150\",\n" +
            "                      \"fractional\":\"43/20\",\n" +
            "                      \"malay\":\"-0.47\",\n" +
            "                      \"indonesian\":\"2.15\",\n" +
            "                      \"hongkong\":\"2.15\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ]\n" +
            "              },\n" +
            "              {\n" +
            "                \"id\":\"125711632\",\n" +
            "                \"descriptionKey\":\"Total Points O/U\",\n" +
            "                \"description\":\"Total Points O/U\",\n" +
            "                \"key\":\"2W-OU\",\n" +
            "                \"marketTypeId\":\"1903\",\n" +
            "                \"status\":\"O\",\n" +
            "                \"singleOnly\":false,\n" +
            "                \"notes\":\"\",\n" +
            "                \"period\":{\n" +
            "                  \"id\":\"308\",\n" +
            "                  \"description\":\"Live Match\",\n" +
            "                  \"abbreviation\":\"MT\",\n" +
            "                  \"live\":true,\n" +
            "                  \"main\":true\n" +
            "                },\n" +
            "                \"outcomes\":[\n" +
            "                  {\n" +
            "                    \"id\":\"674941299\",\n" +
            "                    \"description\":\"Over\",\n" +
            "                    \"status\":\"O\",\n" +
            "                    \"type\":\"O\",\n" +
            "                    \"price\":{\n" +
            "                      \"id\":\"5706298491\",\n" +
            "                      \"handicap\":\"84.5\",\n" +
            "                      \"american\":\"-115\",\n" +
            "                      \"decimal\":\"1.870\",\n" +
            "                      \"fractional\":\"20/23\",\n" +
            "                      \"malay\":\"0.87\",\n" +
            "                      \"indonesian\":\"-1.15\",\n" +
            "                      \"hongkong\":\"0.87\"\n" +
            "                    }\n" +
            "                  },\n" +
            "                  {\n" +
            "                    \"id\":\"674941300\",\n" +
            "                    \"description\":\"Under\",\n" +
            "                    \"status\":\"O\",\n" +
            "                    \"type\":\"U\",\n" +
            "                    \"price\":{\n" +
            "                      \"id\":\"5706298492\",\n" +
            "                      \"handicap\":\"84.5\",\n" +
            "                      \"american\":\"-120\",\n" +
            "                      \"decimal\":\"1.833333\",\n" +
            "                      \"fractional\":\"5/6\",\n" +
            "                      \"malay\":\"0.83\",\n" +
            "                      \"indonesian\":\"-1.20\",\n" +
            "                      \"hongkong\":\"0.83\"\n" +
            "                    }\n" +
            "                  }\n" +
            "                ]\n" +
            "              }\n" +
            "            ],\n" +
            "            \"order\":1\n" +
            "          }\n" +
            "        ]\n" +
            "      }";

    @BeforeEach
    public void parseEvent() throws Exception {
        JSONObject eventJSON = new JSONObject(LAKERS_BLAZERS_EVENT_INIT);
        this.event = EventParseUtil.parseEvent(eventJSON);
    }

    @Test
    public void testGetEventId() {
        Long eventId = LiveOddsUpdateUtil.getEventIds(LAKERS_MONEY_LINE_WIRE_UPEATE).iterator().next();
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

    private String SORIN_ODDS_UPDATE =
            "\"{" +
                    "\\\"type\\\":\\\"outcome\\\"," +
                    "\\\"eventId\\\":\\\"7632283\\\"," +
                    "\\\"parentId\\\":\\\"125711661\\\"," +
                    "\\\"target\\\":\\\"live\\\"" +
                "}" +
                    "|" +
                "{" +
                    "\\\"id\\\":\\\"674941357\\\"," +
                    "\\\"price\\\":{" +
                        "\\\"id\\\":\\\"5706268937\\\"," +
                        "\\\"american\\\":\\\"-400\\\"," +
                        "\\\"decimal\\\":\\\"1.250\\\"," +
                        "\\\"fractional\\\":\\\"1/4\\\"," +
                        "\\\"malay\\\":\\\"0.25\\\"," +
                        "\\\"indonesian\\\":\\\"-4.00\\\"," +
                        "\\\"hongkong\\\":\\\"0.25" +
                "\\\"}" +
                "}\"";

    @Test
    public void testUpdateOdds_Sorin() throws Exception {
        JSONObject eventJSON = new JSONObject(SORIN_MATCH_EVENT);
        this.sorinEvent = EventParseUtil.parseEvent(eventJSON);
        Market moneyLineMarket = this.sorinEvent.getMarkets().get("125711661");
        Outcome sorinWinsOutcome = moneyLineMarket.getOutcomes().get("674941357");
        Assertions.assertEquals(-305, sorinWinsOutcome.getPrice().getAmerican());
        Long eventId = LiveOddsUpdateUtil.getEventIds(SORIN_ODDS_UPDATE).iterator().next();
        Assertions.assertEquals(7632283, eventId);
        LiveOddsUpdateUtil.updateEvent(sorinEvent, SORIN_ODDS_UPDATE);
        Assertions.assertEquals(-400, sorinWinsOutcome.getPrice().getAmerican());
    }
}
