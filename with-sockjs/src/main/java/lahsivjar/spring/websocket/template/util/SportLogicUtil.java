package lahsivjar.spring.websocket.template.util;

import lahsivjar.spring.websocket.template.EventBook;
import lahsivjar.spring.websocket.template.model.Event;

public class SportLogicUtil {

    public static final int MINUTES_LEFT_IN_TIME_BASED_GAME = 5;
    public static final int TENNIS_FINAL_SET_NUM_GAMES_THRESHOLD = 4;
    public static final int TABLE_TENNIS_FINAL_GAME_NUM_POINTS_THRESHOLD = 5;
    public static final int TABLE_TENNIS_GAMES_TO_WIN_MATCH = 3;
    public static final int VOLLEYBALL_FINAL_SET_NUM_POINTS_THRESHOLD = 5;
    public static final int VOLLEYBALL_NUM_SETS_TO_WIN_MATCH = 3;

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
        return isPeriodScoreBasedGameEndingSoon(event, VOLLEYBALL_FINAL_SET_NUM_POINTS_THRESHOLD, VOLLEYBALL_NUM_SETS_TO_WIN_MATCH);
    }

    private static boolean isFootballEventEndingSoon(Event event) {
        return isWithinFinalMinutesOfTimeBasedMatch(event);
    }

    private static boolean isWithinFinalMinutesOfTimeBasedMatch(Event event) {
        GameTime gameTime = getGameTime(event);
        if (gameTime != null) {
            return isFinalPeriod(event) && gameTime.minutes < MINUTES_LEFT_IN_TIME_BASED_GAME;
        }
        return false;
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
        return isPeriodScoreBasedGameEndingSoon(event, TABLE_TENNIS_FINAL_GAME_NUM_POINTS_THRESHOLD, TABLE_TENNIS_GAMES_TO_WIN_MATCH);
    }

    private static boolean isPeriodScoreBasedGameEndingSoon(Event event, int periodScorePointThreshold, int primaryScoreToWinMatch) {
        int homeGamesWon = Integer.parseInt(event.getHomeScore());
        int homePointsWonThisGame = event.getCurrentPeriodHomeScore();
        int visitorGamesWon = Integer.parseInt(event.getVisitorScore());
        int visitorPointsWonThisGame = event.getCurrentPeriodVisitorScore();
        return isWithinPeriodScoreOfVictory(homeGamesWon, homePointsWonThisGame, primaryScoreToWinMatch, periodScorePointThreshold)
                || isWithinPeriodScoreOfVictory(visitorGamesWon, visitorPointsWonThisGame, primaryScoreToWinMatch, periodScorePointThreshold);
    }

    private static boolean isBasketballEventEndingSoon(Event event) {
        return isWithinFinalMinutesOfTimeBasedMatch(event);
    }

    private static boolean isFinalPeriod(Event event) {
        return event.getClock().getPeriodNumber() == event.getClock().getNumberOfPeriods();
    }

    private static boolean isSoccerEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isHockeyEventEndingSoon(Event event) {
        return isWithinFinalMinutesOfTimeBasedMatch(event);
    }

    private static boolean isTennisEventEndingSoon(Event event) {
        int setsToWinMatch = setsToWinTennisMatch(event);
        return isPeriodScoreBasedGameEndingSoon(event, TENNIS_FINAL_SET_NUM_GAMES_THRESHOLD, setsToWinMatch);
    }

    private static boolean isWithinPeriodScoreOfVictory(int competitorPrimaryScore, int competitorPeriodScore, int primaryScoreToWinMatch, int periodScorePointThreshold) {
        return competitorPrimaryScore + 1 == primaryScoreToWinMatch && competitorPeriodScore >= periodScorePointThreshold;
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
