import lahsivjar.spring.websocket.template.util.EventParseUtil;
import lahsivjar.spring.websocket.template.model.Event;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class EventParseUtilTest {

    private String eventJson = "{\n" +
            "            \"id\":\"7625321\",\n" +
            "            \"description\":\"Indiana Pacers @ Miami Heat\",\n" +
            "            \"type\":\"GAMEEVENT\",\n" +
            "            \"link\":\"/basketball/nba/indiana-pacers-miami-heat-202008241840\",\n" +
            "            \"status\":\"O\",\n" +
            "            \"sport\":\"BASK\",\n" +
            "            \"startTime\":1598308800000,\n" +
            "            \"live\":true,\n" +
            "            \"awayTeamFirst\":true,\n" +
            "            \"denySameGame\":\"NO\",\n" +
            "            \"teaserAllowed\":true,\n" +
            "            \"competitionId\":\"2958468\",\n" +
            "            \"notes\":\"Best of 7 - Game 4 - Miami leads series 3-0 - At The Field House - Orlando, FL\",\n" +
            "            \"numMarkets\":48,\n" +
            "            \"lastModified\":1598311685261,\n" +
            "            \"competitors\":[\n" +
            "               {\n" +
            "                  \"id\":\"7625321-203\",\n" +
            "                  \"name\":\"Miami Heat\",\n" +
            "                  \"home\":true\n" +
            "               },\n" +
            "               {\n" +
            "                  \"id\":\"7625321-11757534\",\n" +
            "                  \"name\":\"Indiana Pacers\",\n" +
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
            "                        \"id\":\"125681077\",\n" +
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
            "                              \"id\":\"674748343\",\n" +
            "                              \"description\":\"Indiana Pacers\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"A\",\n" +
            "                              \"competitorId\":\"7625321-11757534\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5703922227\",\n" +
            "                                 \"handicap\":\"7.5\",\n" +
            "                                 \"american\":\"-115\",\n" +
            "                                 \"decimal\":\"1.870\",\n" +
            "                                 \"fractional\":\"20/23\",\n" +
            "                                 \"malay\":\"0.87\",\n" +
            "                                 \"indonesian\":\"-1.15\",\n" +
            "                                 \"hongkong\":\"0.87\"\n" +
            "                              }\n" +
            "                           },\n" +
            "                           {\n" +
            "                              \"id\":\"674748342\",\n" +
            "                              \"description\":\"Miami Heat\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"H\",\n" +
            "                              \"competitorId\":\"7625321-203\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5703922226\",\n" +
            "                                 \"handicap\":\"-7.5\",\n" +
            "                                 \"american\":\"-115\",\n" +
            "                                 \"decimal\":\"1.870\",\n" +
            "                                 \"fractional\":\"20/23\",\n" +
            "                                 \"malay\":\"0.87\",\n" +
            "                                 \"indonesian\":\"-1.15\",\n" +
            "                                 \"hongkong\":\"0.87\"\n" +
            "                              }\n" +
            "                           }\n" +
            "                        ]\n" +
            "                     },\n" +
            "                     {\n" +
            "                        \"id\":\"125681071\",\n" +
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
            "                              \"id\":\"674748331\",\n" +
            "                              \"description\":\"Indiana Pacers\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"A\",\n" +
            "                              \"competitorId\":\"7625321-11757534\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5703921810\",\n" +
            "                                 \"american\":\"+265\",\n" +
            "                                 \"decimal\":\"3.650\",\n" +
            "                                 \"fractional\":\"53/20\",\n" +
            "                                 \"malay\":\"-0.38\",\n" +
            "                                 \"indonesian\":\"2.65\",\n" +
            "                                 \"hongkong\":\"2.65\"\n" +
            "                              }\n" +
            "                           },\n" +
            "                           {\n" +
            "                              \"id\":\"674748330\",\n" +
            "                              \"description\":\"Miami Heat\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"H\",\n" +
            "                              \"competitorId\":\"7625321-203\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5703921809\",\n" +
            "                                 \"american\":\"-375\",\n" +
            "                                 \"decimal\":\"1.266667\",\n" +
            "                                 \"fractional\":\"4/15\",\n" +
            "                                 \"malay\":\"0.27\",\n" +
            "                                 \"indonesian\":\"-3.75\",\n" +
            "                                 \"hongkong\":\"0.27\"\n" +
            "                              }\n" +
            "                           }\n" +
            "                        ]\n" +
            "                     },\n" +
            "                     {\n" +
            "                        \"id\":\"125681057\",\n" +
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
            "                              \"id\":\"674748287\",\n" +
            "                              \"description\":\"Over\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"O\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5703922075\",\n" +
            "                                 \"handicap\":\"209.5\",\n" +
            "                                 \"american\":\"-120\",\n" +
            "                                 \"decimal\":\"1.833333\",\n" +
            "                                 \"fractional\":\"5/6\",\n" +
            "                                 \"malay\":\"0.83\",\n" +
            "                                 \"indonesian\":\"-1.20\",\n" +
            "                                 \"hongkong\":\"0.83\"\n" +
            "                              }\n" +
            "                           },\n" +
            "                           {\n" +
            "                              \"id\":\"674748288\",\n" +
            "                              \"description\":\"Under\",\n" +
            "                              \"status\":\"O\",\n" +
            "                              \"type\":\"U\",\n" +
            "                              \"price\":{\n" +
            "                                 \"id\":\"5703922076\",\n" +
            "                                 \"handicap\":\"209.5\",\n" +
            "                                 \"american\":\"-110\",\n" +
            "                                 \"decimal\":\"1.909091\",\n" +
            "                                 \"fractional\":\"10/11\",\n" +
            "                                 \"malay\":\"0.91\",\n" +
            "                                 \"indonesian\":\"-1.10\",\n" +
            "                                 \"hongkong\":\"0.91\"\n" +
            "                              }\n" +
            "                           }\n" +
            "                        ]\n" +
            "                     }\n" +
            "                  ],\n" +
            "                  \"order\":1\n" +
            "               }\n" +
            "            ]\n" +
            "         }";

    @Test
    public void testParseEvent() {
        JSONObject eventJSON = new JSONObject(eventJson);
        Event event = EventParseUtil.parseEvent(eventJSON);
        System.out.println("done");
    }

}
