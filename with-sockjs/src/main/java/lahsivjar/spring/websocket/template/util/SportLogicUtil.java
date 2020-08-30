package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.EventBook;
import lahsivjar.spring.websocket.template.model.Event;

public class SportLogicUtil {

    public static final int MINUTES_LEFT_IN_BASKETBALL_GAME = 5;

    public static boolean isEndingSoon(Event event) {
        switch(EventBook.getEquivalentKey(event.getSport())) {
            case "TENNIS":
                return isTennisEventEndingSoon(event);
            case "HOCKEY":
                return isHockeyEventEndingSoon(event);
            case "SOCCER":
                return isSoccerEventEndingSoon(event);
            case "BASKETBALL":
                return isBasketballEventEndingSoon(event);
            case "TABLETENNIS":
                return isTableTennisEventEndingSoon(event);
            case "E-SPORTS":
                return isESportsEventEndingSoon(event);
            case "DARTS":
                return isDartsEventEndingSoon(event);
            case "RUGBY":
                return isRugbyEventEndingSoon(event);
            case "CRICKET":
                return isCricketEventEndingSoon(event);
            case "FOOTBALL":
                return isFootballEventEndingSoon(event);
            case "VOLLEYBALL":
                return isVolleyBallEventEndingSoon(event);
            default:
            case "BASEBALL":
                return isFinalPeriod(event);
        }
    }

    private static boolean isVolleyBallEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isFootballEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isCricketEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isRugbyEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isDartsEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isESportsEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isTableTennisEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isBasketballEventEndingSoon(Event event) {
        GameTime gameTime = getGameTime(event);
        if (gameTime != null) {
            return isFinalPeriod(event) && gameTime.minutes < MINUTES_LEFT_IN_BASKETBALL_GAME;
        }
        return false;
    }

    private static boolean isFinalPeriod(Event event) {
        return event.getClock().getPeriodNumber() == event.getClock().getNumberOfPeriods();
    }

    private static boolean isSoccerEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isHockeyEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isTennisEventEndingSoon(Event event) {
        int setsToWinMatch = setsToWinTennisMatch(event);
        int homeSetsWon = Integer.parseInt(event.getHomeScore());
        int homeGamesWonThisSet = event.getCurrentPeriodHomeScore();
        int visitorSetsWon = Integer.parseInt(event.getVisitorScore());
        int visitorGamesWonThisSet = event.getCurrentPeriodVisitorScore();
        return isWithinTwoGamesOfTennisVictory(homeSetsWon, homeGamesWonThisSet, setsToWinMatch)
                || isWithinTwoGamesOfTennisVictory(visitorSetsWon, visitorGamesWonThisSet, setsToWinMatch);
    }

    private static boolean isWithinTwoGamesOfTennisVictory(int setsWon, int gamesWonThisSet, int setsToWinMatch) {
        return setsWon + 1 == setsToWinMatch && gamesWonThisSet >=4;
    }

    private static int setsToWinTennisMatch(Event event) {
        // mens is best 3 of 5, womens is best 2 of 3
        return event.getClock().getNumberOfPeriods() == 5 ? 3 : 2;
    }

    private static GameTime getGameTime(Event event) {
        if (event.getClock() != null && event.getClock().getGameTime() != null && GameTime.canParse(event.getClock().getGameTime())) {
            return new GameTime(event.getClock().getGameTime());
        }
        return null;
    }

    private static class GameTime {
        int minutes;
        int seconds;

        GameTime(String gameTimeString) {
            String[] split = gameTimeString.trim().split(":");
            this.minutes = Integer.parseInt(split[0]);
            this.seconds = Integer.parseInt(split[1]);
        }

        static boolean canParse(String gameTimeString) {
            return gameTimeString != null && gameTimeString.contains(":");
        }
    }

}
