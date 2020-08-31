package com.ceruti.bov.util;

import com.ceruti.bov.model.Event;
import com.ceruti.bov.EventBook;

public class SportLogicUtil {

    public static final int TENNIS_FINAL_SET_NUM_GAMES_THRESHOLD = 4;
    public static final int TENNIS_FIRST_SET_GAMES_WON_THRESHOLD = 2;

    public static final int TABLE_TENNIS_FINAL_GAME_NUM_POINTS_THRESHOLD = 5;
    public static final int TABLE_TENNIS_GAMES_TO_WIN_MATCH = 3;
    public static final int TABLE_TENNIS_FIRST_GAME_POINT_THRESHOLD = 5;

    public static final int VOLLEYBALL_FINAL_SET_NUM_POINTS_THRESHOLD = 5;
    public static final int VOLLEYBALL_NUM_SETS_TO_WIN_MATCH = 3;
    public static final int VOLLEYBALL_FIRST_SET_POINTS_THRESHOLD = 10;

    public static final int AMERICAN_FOOTBALL_FIRST_QUARTER_MINUTE_THRESHOLD = 8;
    private static final int AMERICAN_FOOTBALL_FINAL_QUARTER_MINUTES_THRESHOLD = 4;

    private static final int BASKERTBALL_FIRST_QUARTER_MINUTE_THRESHOLD = 5;
    private static final int BASKETBALL_FOURTH_QUARTER_MINUTES_THRESHOLD = 2;

    private static final int HOCKEY_FIRST_PERIOD_MINUTE_THRESHOLD = 12;
    private static final int HOCKEY_THIRD_PERIOD_MINUTES_THRESHOLD = 8;

    public static boolean startedRecently(Event event) {
        switch(EventBook.getEquivalentKey(event.getSport())) {
            case "TENNIS":
                return isTennisEventStartedRecently(event);
            case "HOCKEY":
                return isHockeyEventStartedRecently(event);
            case "SOCCER":
                return isSoccerEventStartedRecently(event);
            case "BASKETBALL":
                return isBasketballEventStartedRecently(event);
            case "TABLETENNIS":
                return isTableTennisEventStartedRecently(event);
            case "E-SPORTS":
                return isESportsEventStartedRecently(event);
            case "DARTS":
                return isDartsEventStartedRecently(event);
            case "RUGBY":
                return isRugbyEventStartedRecently(event);
            case "CRICKET":
                return isCricketEventStartedRecently(event);
            case "FOOTBALL":
                return isFootballEventStartedRecently(event);
            case "VOLLEYBALL":
                return isVolleyBallEventStartedRecently(event);
            case "BASEBALL":
            default:
                return isFirstPeriod(event);
        }
    }

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
        return isWithinFinalMinutesOfTimeBasedMatch(event, AMERICAN_FOOTBALL_FINAL_QUARTER_MINUTES_THRESHOLD);
    }

    private static boolean isWithinFinalMinutesOfTimeBasedMatch(Event event, int minutesThreshold) {
        GameTime gameTime = getGameTime(event);
        if (gameTime != null) {
            return isFinalPeriod(event) && gameTime.minutes < minutesThreshold;
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
        if (event.getHomeScore() == null || event.getVisitorScore() == null) {
            System.err.println("Scores not set for event: "+event.getId());
            return false;
        }
        int homeGamesWon = Integer.parseInt(event.getHomeScore());
        int homePointsWonThisGame = event.getCurrentPeriodHomeScore();
        int visitorGamesWon = Integer.parseInt(event.getVisitorScore());
        int visitorPointsWonThisGame = event.getCurrentPeriodVisitorScore();
        return isWithinPeriodScoreOfVictory(homeGamesWon, homePointsWonThisGame, primaryScoreToWinMatch, periodScorePointThreshold)
                || isWithinPeriodScoreOfVictory(visitorGamesWon, visitorPointsWonThisGame, primaryScoreToWinMatch, periodScorePointThreshold);
    }

    private static boolean isBasketballEventEndingSoon(Event event) {
        return isWithinFinalMinutesOfTimeBasedMatch(event, BASKETBALL_FOURTH_QUARTER_MINUTES_THRESHOLD);
    }

    private static boolean isFinalPeriod(Event event) {
        return event.getClock().getPeriodNumber() == event.getClock().getNumberOfPeriods();
    }

    private static boolean isFirstPeriod(Event event) {
        return event.getClock().getPeriodNumber() == 1;
    }

    private static boolean isSoccerEventEndingSoon(Event event) {
        return false; // TODO: implement
    }

    private static boolean isHockeyEventEndingSoon(Event event) {
        return isWithinFinalMinutesOfTimeBasedMatch(event, HOCKEY_THIRD_PERIOD_MINUTES_THRESHOLD);
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
        // however, the API always returns that the number of periods in the game is 3 (men or women)
        // therefore, tennis is F'd
        if (event.getClock() == null) {
            return 3;
        }
        return event.getClock().getNumberOfPeriods();
    }

    private static GameTime getGameTime(Event event) {
        if (event.getClock() != null && event.getClock().getGameTime() != null && GameTime.canParse(event.getClock().getGameTime())) {
            return new GameTime(event.getClock().getGameTime());
        }
        return null;
    }

    private static boolean isVolleyBallEventStartedRecently(Event event) {
        return noPeriodsWonYet(event) && currentPeriodScoreWithinThreshold(event, VOLLEYBALL_FIRST_SET_POINTS_THRESHOLD);
    }

    private static boolean noPeriodsWonYet(Event event) {
        if (event.getHomeScore() == null|| event.getVisitorScore() == null) {
            return true;
        }
        int homePeriodsWon = Integer.parseInt(event.getHomeScore());
        int visitorPeriodsWon = Integer.parseInt(event.getVisitorScore());
        return homePeriodsWon <= 0 && visitorPeriodsWon <= 0 && event.getClock().getPeriodNumber() <=1;
    }

    private static boolean currentPeriodScoreWithinThreshold(Event event, int periodScoreThreshold) {
        int homePeriodScore = event.getCurrentPeriodHomeScore();
        int visitorPeriodScore = event.getCurrentPeriodVisitorScore();
        return homePeriodScore <= periodScoreThreshold && visitorPeriodScore <= periodScoreThreshold;
    }

    private static boolean isFootballEventStartedRecently(Event event) {
        return isTimeBasedEventStartedRecently(event, AMERICAN_FOOTBALL_FIRST_QUARTER_MINUTE_THRESHOLD);
    }

    private static boolean isTimeBasedEventStartedRecently(Event event, int minutesRemainingThreshold) {
        GameTime gameTime = getGameTime(event);
        if (gameTime != null) {
            return event.getClock().getPeriodNumber() <= 1 && gameTime.minutes >= minutesRemainingThreshold;
        }
        return false;
    }


    private static boolean isCricketEventStartedRecently(Event event) {
        return false; // TODO: implement
    }

    private static boolean isRugbyEventStartedRecently(Event event) {
        return false; // TODO: implement
    }

    private static boolean isDartsEventStartedRecently(Event event) {
        return false; // TODO: implement
    }

    private static boolean isESportsEventStartedRecently(Event event) {
        return false; // TODO: implement
    }

    private static boolean isTableTennisEventStartedRecently(Event event) {
        return noPeriodsWonYet(event) && currentPeriodScoreWithinThreshold(event, TABLE_TENNIS_FIRST_GAME_POINT_THRESHOLD);
    }

    private static boolean isBasketballEventStartedRecently(Event event) {
        return isTimeBasedEventStartedRecently(event, BASKERTBALL_FIRST_QUARTER_MINUTE_THRESHOLD);
    }

    private static boolean isSoccerEventStartedRecently(Event event) {
        return false; // TODO: implement
    }

    private static boolean isHockeyEventStartedRecently(Event event) {
        return isTimeBasedEventStartedRecently(event, HOCKEY_FIRST_PERIOD_MINUTE_THRESHOLD);
    }

    private static boolean isTennisEventStartedRecently(Event event) {
        return noPeriodsWonYet(event) && currentPeriodScoreWithinThreshold(event, TENNIS_FIRST_SET_GAMES_WON_THRESHOLD);
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
