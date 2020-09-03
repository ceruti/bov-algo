package com.ceruti.bov;

import java.util.List;
import java.util.Map;

import com.ceruti.bov.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {

    private BettingFacilitatorService bettingFacilitatorService;
    private EventBook eventBook;
    private SimulationService simulationService;
    private BetPlacingService betPlacingService;

    @Autowired
    public MessageController(EventBook eventBook, SimulationService simulationService, BettingFacilitatorService bettingFacilitatorService, BetPlacingService betPlacingService) {
        this.simulationService = simulationService;
        this.eventBook = eventBook;
        this.bettingFacilitatorService = bettingFacilitatorService;
        this.betPlacingService = betPlacingService;
    }

    /*
     * This MessageMapping annotated method will be handled by
     * SimpAnnotationMethodMessageHandler and after that the Message will be
     * forwarded to Broker channel to be forwarded to the client via WebSocket
     */
    @MessageMapping("/all")
    @SendTo("/topic/all")
    public Map<String, String> post(@Payload Map<String, String> message) {
//        message.put("timestamp", Long.toString(System.currentTimeMillis()));
//        chatHistoryDao.save(message);
        // tODO: something with messages from the front end??
        return message;
    }

    @RequestMapping("/events")
    public Map<String, Map<Long, Event>> getEvents() {
        return this.eventBook.getLiveEvents();
    }

    // NOTE: these requests are served over HTTPS instead of WebSocket because WebSockets are not as reliable and these commands are critical, even if latency is higher

    @RequestMapping(value = "/events/{eventId}/enable", method = RequestMethod.PUT)
    public void enableEventForBetting(@PathVariable(value="eventId") Long eventId) {
        this.eventBook.enableEventForBetting(eventId);
    }

    @RequestMapping(value = "/events/{eventId}/disable", method = RequestMethod.PUT)
    public void disableEventForBetting(@PathVariable(value="eventId") Long eventId) {
        this.eventBook.disableEventForBetting(eventId);
    }

    @RequestMapping(value = "/events/{eventId}/markets/{marketId}/outcomes/{outcomeId}/bet", method = RequestMethod.PUT)
    public void placeCustomBet(@PathVariable(value="eventId") Long eventId,
                                 @PathVariable(value="marketId") String marketId,
                                 @PathVariable(value="outcomeId") String outcomeId,
                                 @RequestParam(required = true, value = "amountInCents") int amountInCents,
                                 @RequestParam(required = true, value = "opposingOutcomeId") String opposingOutcomeId,
                                 @RequestBody Price price)  {
        bettingFacilitatorService.attemptPlaceCustomBetAsync(eventId, marketId, outcomeId, opposingOutcomeId, price, amountInCents);
    }

    @RequestMapping(value = "/events/{eventId}/markets/{marketId}/outcomes/{outcomeId}/enable", method = RequestMethod.PUT)
    public void enableOutcomeForBetting(@PathVariable(value="eventId") Long eventId,
                                        @PathVariable(value="marketId") String marketId,
                                        @PathVariable(value="outcomeId") String outcomeId) {
        this.eventBook.enableOutcomeForBetting(eventId, marketId, outcomeId);
    }

    @RequestMapping(value = "/events/{eventId}/markets/{marketId}/outcomes/{outcomeId}/disable", method = RequestMethod.PUT)
    public void disableOutcomeForBetting(@PathVariable(value="eventId") Long eventId,
                                         @PathVariable(value="marketId") String marketId,
                                         @PathVariable(value="outcomeId") String outcomeId) {
        this.eventBook.disableOutcomeForBetting(eventId, marketId, outcomeId);
    }

    @RequestMapping(value = "/simulations", method = RequestMethod.GET)
    public List<String> listSimulations() {
        return this.simulationService.getSimulationids();
    }

    @RequestMapping(value = "/simulations/{simulationId}", method = RequestMethod.GET)
    public SimulationPage getSimulationPage(
            @PathVariable(value="simulationId") String simulationId,
            @RequestParam(required = false, value = "sortBy") String sortBy,
            @RequestParam(required = false, value = "sortDescending") Boolean sortDescending,
            @RequestParam(required = true, value = "page") int page,
            @RequestParam(required = true, value = "pageSize") int pageSize,
            @RequestParam(required = false, value = "filterBySport") String sportKey) {
        return this.simulationService.getSimulation(simulationId, sortBy, sortDescending, page, pageSize, sportKey);
    }

    @RequestMapping(value = "/simulations/{simulationId}/aggregate", method = RequestMethod.GET)
    public SimulationAggregateResult getSimulationAggregateResults(@PathVariable(value="simulationId") String simulationId) {
        return this.simulationService.getSimulationAggregate(simulationId);
    }

    @RequestMapping(value = "/token/{token}", method = RequestMethod.PUT)
    public void setToken(@PathVariable(value="token") String token) {
        this.betPlacingService.setToken("Bearer "+token);
    }

}
