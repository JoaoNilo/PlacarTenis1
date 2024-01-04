package com.edrosframework.placartenis1;

public interface Sport {

    //----------------------------------------------------------------------------------------------
    public final int PLAYER_ID_NONE = 0;
    public final int PLAYER_ID_1 = 1;
    public final int PLAYER_ID_2 = 2;

    // used by "current_set"
    public final byte SET_1 = 1;
    public final byte SET_2 = 2;
    public final byte SET_3 = 3;


    //----------------------------------------------------------------------------------------------
    public default void setMode(int m) {}

    //----------------------------------------------------------------------------------------------
    public default int getMode() {return (0);}

    //----------------------------------------------------------------------------------------------
    // retrieve set winner if set is finished
    public default byte CheckSet(int set_id) { return (0);}

    //----------------------------------------------------------------------------------------------
    // retrieve match winner if match is finished
    public default byte CheckMatch() { return (0);}

    //----------------------------------------------------------------------------------------------
    public default int StringToIndex(String PointsString){
        return (0);
    }

    //----------------------------------------------------------------------------------------------
    public default boolean isGameOn() {  return true;}

    //----------------------------------------------------------------------------------------------
    public default boolean isTiebreaking() { return true;}

    //----------------------------------------------------------------------------------------------
    public default boolean isTiebreak() { return true;}

    //----------------------------------------------------------------------------------------------
    public default void setTiebreak(boolean tiebreak) { }

    //----------------------------------------------------------------------------------------------
    public default boolean isAdvantage() { return true;}

    //----------------------------------------------------------------------------------------------
    public default void setAdvantage(boolean advantage) { }

    //----------------------------------------------------------------------------------------------
    public default boolean isMatchTiebreak() { return true;}

    //----------------------------------------------------------------------------------------------
    public default void setMatchTiebreak(boolean tennis_mode) { }

    //----------------------------------------------------------------------------------------------
    public default void setMatchTiebreakPoints(int points) {}

    //----------------------------------------------------------------------------------------------
    public default int getMatchTiebreakPoints() {
        return 0;
    }

    //----------------------------------------------------------------------------------------------
    public default boolean isAlternateService() { return true;}

    //----------------------------------------------------------------------------------------------
    public default void setAlternateService(boolean alternate_service) { }

    //----------------------------------------------------------------------------------------------
    public default int getGamesPerSet() { return 0;}

    //----------------------------------------------------------------------------------------------
    public default void setGamesPerSet(int games_set) { }

    //----------------------------------------------------------------------------------------------
    public default int getPointsPerGame() {
        return 0;
    }

    //----------------------------------------------------------------------------------------------
    public default void setPointsPerGame(int new_points_per_game) {}

    //----------------------------------------------------------------------------------------------
    public default int getSetsPerMatch() {
        return 0;
    }

    //----------------------------------------------------------------------------------------------
    public default void setSetsPerMatch(int sets_per_match) {}

    //----------------------------------------------------------------------------------------------
    public default boolean isServerChange() { return true;}

    //----------------------------------------------------------------------------------------------
    public default boolean isCourtChange() { return true;}

    //----------------------------------------------------------------------------------------------
    public default int getWinner() { return 0;}

    //----------------------------------------------------------------------------------------------
    public default void Restart() { }

    //----------------------------------------------------------------------------------------------
    // Starts the game. No further "settings change" area allowed during the ongoing match.
    public default void Start() {}

    //----------------------------------------------------------------------------------------------
    public default void Stop(int player_id) {}

    //----------------------------------------------------------------------------------------------
    public default void ScoreIncrement(int player_id) {}

    //----------------------------------------------------------------------------------------------
    public default void ScoreDecrement(int player_id) {}

    //----------------------------------------------------------------------------------------------
    public default byte getCurrentSet() { return(0);}

    //----------------------------------------------------------------------------------------------
    public default void setCurrentSet(byte value) {}

    //----------------------------------------------------------------------------------------------
    public default void setSet(int player_id, int set_id, String value) {}

    //----------------------------------------------------------------------------------------------
    public default void setSet(int player_id, int set_id, byte value) {}

    //----------------------------------------------------------------------------------------------
    public default String getScore(int player_id) { return "";}

    //----------------------------------------------------------------------------------------------
    public default byte setPoints(int player_id, byte value) { return 0;}

    //----------------------------------------------------------------------------------------------
    public default byte setPoints(int player_id, String value) { return 0;}

    //----------------------------------------------------------------------------------------------
    public default void UpdatePoints(int player_id) {}

    //----------------------------------------------------------------------------------------------
    public default String getTens(int player_id) { return "";}

    //----------------------------------------------------------------------------------------------
    public default String getUnits(int player_id) {return "";}

    //----------------------------------------------------------------------------------------------
    public default String getSet1(int player_id) { return "";}

    //----------------------------------------------------------------------------------------------
    public default String getSet2(int player_id) { return "";}

    //----------------------------------------------------------------------------------------------
    public default String getSet3(int player_id) { return "";}

    //----------------------------------------------------------------------------------------------
    public default byte getCurrentServer() { return 0;}

    //----------------------------------------------------------------------------------------------
    public default void setCurrentServer(byte current_server) {}

    //----------------------------------------------------------------------------------------------
    public default void ToggleServer() {}

    //----------------------------------------------------------------------------------------------
    public default void setMatchFlags(byte server_id) {}

    //----------------------------------------------------------------------------------------------
    public default void copy(Scoreboard src) {}

    //----------------------------------------------------------------------------------------------
    public default void Export(CommBuffer dst) {}

    //----------------------------------------------------------------------------------------------
    // formats the timer string
    public default String getTimerText(double time) { return "";}

}
