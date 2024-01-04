//==================================================================================================
package com.edrosframework.placartenis1;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.EventListener;

//--------------------------------------------------------------------------------------------------
public class Scoreboard implements Serializable {

    //----------------------------------------------------------------------------------------------
    public final int PLAYER_ID_NONE = 0;
    public final int PLAYER_ID_1 = 1;
    public final int PLAYER_ID_2 = 2;

    // used as indexes for "player1_set[]"
    public final byte SET_INDEX_1 = 0;
    public final byte SET_INDEX_2 = 1;
    public final byte SET_INDEX_3 = 2;

    // used by "current_set"
    public final byte SET_1 = 1;
    public final byte SET_2 = 2;
    public final byte SET_3 = 3;

    //---------------------------------------------------
    // Transport section: this data is sent to the scoreboard
    protected byte player1_tens = 0;
    protected byte player1_units = 0;
    protected final byte[] player1_set = new byte[3];

    protected byte player2_tens = 0;
    protected byte player2_units = 0;
    protected final byte[] player2_set = new byte[3];

    protected byte match_flags = 0;

    private byte match_seconds = 0;
    private byte match_minutes = 0;
    private byte match_hours = 0;

    //---------------------------------------------------
    // Game control variables
    protected byte player1_points = 0;
    protected byte player2_points = 0;
    protected byte player1_sets = 0;
    protected byte player2_sets = 0;

    //----------------------------------------------------------------------------------------------
    protected byte current_set;
    protected byte current_server;
    protected boolean game_on = false;
    protected boolean change_court = false;
    protected boolean change_server = false;
    protected boolean tiebreaking = false;
    protected int points_to_win = 7;
    protected int winner = 0;

    ScoreParameters Rules;

    //----------------------------------------------------------------------------------------------
    public void setMode(int new_mode){
        Rules.setMode(new_mode);
    }

    //----------------------------------------------------------------------------------------------
    public int getMode(){
        return(Rules.GetMode());
    }

    //----------------------------------------------------------------------------------------------
    protected transient MyEventListener eventListener;

    //----------------------------------------------------------------------------------------------
    public void setEventListener(MyEventListener eventListener) {
        this.eventListener = eventListener;
    }

    //----------------------------------------------------------------------------------------------
    // Inner interface representing the event listener
    public interface MyEventListener extends EventListener {
        void onEvent();
    }

    //----------------------------------------------------------------------------------------------
    public boolean isGameOn() {  return this.game_on;}

    //----------------------------------------------------------------------------------------------
    public boolean isTiebreaking() { return this.tiebreaking;}

    //----------------------------------------------------------------------------------------------
    public boolean isTiebreak() { return Rules.tiebreak;}

    //----------------------------------------------------------------------------------------------
    public void setTiebreak(boolean tiebreak) { Rules.tiebreak = tiebreak;}

    //----------------------------------------------------------------------------------------------
    public boolean isAdvantage() { return Rules.advantage;}

    //----------------------------------------------------------------------------------------------
    public void setAdvantage(boolean advantage) { Rules.advantage = advantage;}

    //----------------------------------------------------------------------------------------------
    public boolean isMatchTiebreak() { return Rules.match_tiebreak;}

    //----------------------------------------------------------------------------------------------
    public void setMatchTiebreak(boolean tennis_mode) { Rules.match_tiebreak = tennis_mode;}

    //----------------------------------------------------------------------------------------------
    public void setMatchTiebreakPoints(int points) {
        if(points > 80){ points = 80;}
        Rules.points_matchTiebreak = points;
    }

    //----------------------------------------------------------------------------------------------
    public int getMatchTiebreakPoints() {
        return Rules.points_matchTiebreak;
    }

    //----------------------------------------------------------------------------------------------
    public boolean isAlternateService() { return Rules.alternate_service;}

    //----------------------------------------------------------------------------------------------
    public void setAlternateService(boolean alternate_service) { Rules.alternate_service = alternate_service;}

    //----------------------------------------------------------------------------------------------
    public int getGamesPerSet() { return Rules.games_per_set;}

    //----------------------------------------------------------------------------------------------
    public void setGamesPerSet(int games_set) {
        if(games_set < 1){ Rules.games_per_set = 1;}
        else if(games_set > 8){ Rules.games_per_set = 8;}
        else{ Rules.games_per_set = games_set;}
    }

    //----------------------------------------------------------------------------------------------
    public int getPointsPerGame() {
        return Rules.points_Tiebreak;
    }

    //----------------------------------------------------------------------------------------------
    public void setPointsPerGame(int new_points_per_game) {
        if(new_points_per_game < 2){ Rules.points_Tiebreak = 2;}
        else if(new_points_per_game > 98){ Rules.points_Tiebreak = 98;}
        else{ Rules.points_Tiebreak = new_points_per_game;}
    }

    //----------------------------------------------------------------------------------------------
    public int getSetsPerMatch() {
        return Rules.sets_per_match;
    }

    //----------------------------------------------------------------------------------------------
    public void setSetsPerMatch(int sets_per_match) {
        if(sets_per_match < 1){ Rules.sets_per_match = 1;}
        else if(sets_per_match > 3){ Rules.sets_per_match = 3;}
        else{ Rules.sets_per_match = sets_per_match;}
    }

    //----------------------------------------------------------------------------------------------
    public boolean isServerChange() { return this.change_server;}

    //----------------------------------------------------------------------------------------------
    public boolean isCourtChange() { return this.change_court;}

    //----------------------------------------------------------------------------------------------
    public int getWinner() { return this.winner;}

    //----------------------------------------------------------------------------------------------
    // MAIN CONSTRUCTOR
    //----------------------------------------------------------------------------------------------
    public Scoreboard(ScoreParameters r) {
        Rules = r;
        Restart();
    }

    //----------------------------------------------------------------------------------------------
    // retrieve set winner if set is finished
    public byte CheckSet(int set_id) {
        byte result = 0; this.tiebreaking = false;
        // tiebreak game victory
        if(player1_set[set_id] > Rules.games_per_set){
            result = PLAYER_ID_1; player2_set[set_id] = (byte)Rules.games_per_set;
        } else if(player2_set[set_id] > Rules.games_per_set){
            result = PLAYER_ID_2; player1_set[set_id] = (byte)Rules.games_per_set;
        }

        // simple game victory
        if((player1_set[set_id] == Rules.games_per_set) && ((player1_set[set_id]-player2_set[set_id])>1)){
            result = PLAYER_ID_1;
        } else if((player2_set[set_id] == Rules.games_per_set) && ((player2_set[set_id]-player1_set[set_id])>1)){
            result = PLAYER_ID_2;
        }

        // check for tiebreaks (both modes)
        if((player1_set[set_id] == Rules.games_per_set) && (player2_set[set_id] == Rules.games_per_set)){
            this.tiebreaking = true;
        }

        // "special case" game victory (prepare for match tiebreak)
        if(set_id == SET_INDEX_3){
            if(Rules.match_tiebreak){
                this.tiebreaking = true;
                this.points_to_win = Rules.points_matchTiebreak;
                if(player1_set[set_id] == 1) { result = PLAYER_ID_1;}
                else if(player2_set[set_id] == 1) { result = PLAYER_ID_2;}
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    // retrieve match winner if match is finished
    public byte CheckMatch() {
        byte result = PLAYER_ID_NONE;
        player1_sets = 0; player2_sets = 0;
        // compute won sets for both players
        for (byte c=0; c < Rules.sets_per_match; c++){
            if(CheckSet(c) == PLAYER_ID_1){ player1_sets++;}
            else if(CheckSet(c) == PLAYER_ID_2){ player2_sets++;}
        }
        // check for a winner
        int winner = (Rules.sets_per_match / 2) + 1;
        if(player1_sets >= winner){ result = PLAYER_ID_1;}
        else if(player2_sets >= winner){ result = PLAYER_ID_2;}
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    private void CheckChangeSides(byte set_id){
        int sum = player1_set[set_id] + player2_set[set_id];
        int rest = sum % 2;
        if(rest == 1) {
            if (eventListener != null) {
                change_court = true;
                eventListener.onEvent();
                change_court = false;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void Restart() {
        player1_points = 0;
        player1_sets = 0;
        player1_tens = 0;
        player1_units = 0;
        player1_set[SET_INDEX_1] = 0;
        player1_set[SET_INDEX_2] = 0;
        player1_set[SET_INDEX_3] = 0;
        player2_points = 0;
        player2_sets = 0;
        player2_tens = 0;
        player2_units = 0;
        player2_set[SET_INDEX_1] = 0;
        player2_set[SET_INDEX_2] = 0;
        player2_set[SET_INDEX_3] = 0;
        match_flags = 0;

        // game control variables
        this.game_on = false;
        this.winner = 0;
        this.current_set = 0;
        this.current_server = 0;
        this.points_to_win = Rules.points_Tiebreak;
        this.tiebreaking = false;

    }

    //----------------------------------------------------------------------------------------------
    // Starts the game. No further "settings change" area allowed during the ongoing match.
    public void Start() {
        game_on = true;
        this.current_set = 1;
        setCurrentServer((byte) PLAYER_ID_1);
    }

    //----------------------------------------------------------------------------------------------
    public void Stop(int player_id) {
        game_on = false;
        this.winner = player_id;
        this.current_server = 0;
        player1_points = 0;
        player1_tens = 0x10;
        player1_units = 0x10;
        player2_points = 0;
        player2_tens = 0x10;
        player2_units = 0x10;
        setMatchFlags((byte)PLAYER_ID_NONE);
    }

    //----------------------------------------------------------------------------------------------
    private void SetIncrement1(int n) {
        if (current_set == 1) {
            player1_set[SET_INDEX_1] += n;
            if (player1_set[SET_INDEX_1] > Rules.games_per_set) {
                // >>>> GANHOU O SET NO TIE-BREAK <<<<<
                // vai para o próximo set
                player1_sets++;
                if(Rules.sets_per_match == 1){ winner = PLAYER_ID_1;}
                else { current_set = 2;}
            } else if (player1_set[SET_INDEX_1] == Rules.games_per_set) {
                if ((player1_set[SET_INDEX_1] - player2_set[SET_INDEX_1]) > 1) {
                    // >>>> GANHOU O SET POR 2 OU MAIS GAMES <<<<<
                    player1_sets++;
                    if(Rules.sets_per_match == 1){ winner = PLAYER_ID_1;}
                    else { current_set = 2;}
                } else if(player1_set[SET_INDEX_1] == player2_set[SET_INDEX_1]) { tiebreaking = true;}
            }
            if(current_set == 1){ CheckChangeSides((byte) SET_INDEX_1);}
        } else if (current_set == 2) {
            player1_set[SET_INDEX_2] += n;
            if (player1_set[SET_INDEX_2] > Rules.games_per_set) {
                // >>>> GANHOU O SET NO TIE-BREAK <<<<<
                // vai para o próximo set
                player1_sets++;
                if(player1_sets >= 2){ winner = PLAYER_ID_1;}
                else {
                    current_set = 3;
                    if(Rules.match_tiebreak){
                        Rules.tiebreak = true; tiebreaking = true; this.points_to_win = Rules.points_matchTiebreak;
                    }
                }
            } else if (player1_set[SET_INDEX_2] == Rules.games_per_set) {
                if ((player1_set[SET_INDEX_2] - player2_set[SET_INDEX_2]) > 1) {
                    // >>>> GANHOU O SET POR 2 OU MAIS GAMES <<<<<
                    player1_sets++;
                    if(player1_sets >= 2){ winner = PLAYER_ID_1;}
                    else { current_set = 3;}
                } else if(player1_set[SET_INDEX_2] == player2_set[SET_INDEX_2]) { tiebreaking = true;}
            }
            if(current_set == 2){ CheckChangeSides((byte) SET_INDEX_2);}
        } else if (current_set == 3) {
            if (!Rules.match_tiebreak) {
                player1_set[SET_INDEX_3] += n;
                if (player1_set[SET_INDEX_3] > Rules.games_per_set) {
                    // >>>> GANHOU O JOGO TIE-BREAK <<<<<
                    // vai para o próximo JOGO
                    player1_sets++;
                    //winner = PLAYER_ID_1;
                    Stop(PLAYER_ID_1);
                    //current_set = 0;
                } else if (player1_set[SET_INDEX_3] == Rules.games_per_set) {
                    if ((player1_set[SET_INDEX_3] - player2_set[SET_INDEX_3]) > 1) {
                        // >>>> GANHOU O SET POR 2 OU MAIS GAMES <<<<<
                        player1_sets++;
                        //winner = PLAYER_ID_1;
                        Stop(PLAYER_ID_1);
                       // current_set = 0;
                    } else if(player1_set[SET_INDEX_3] == player2_set[SET_INDEX_3]) { tiebreaking = true;}
                }
                if(current_set == 3){ CheckChangeSides((byte) SET_INDEX_3);}
            } else {
                ++player1_set[SET_INDEX_3]; winner = PLAYER_ID_1;
                if (eventListener != null) { eventListener.onEvent();}
                return;
            }
        }
        if(this.winner > 0){ if(eventListener != null){ eventListener.onEvent();}}
}

    //----------------------------------------------------------------------------------------------
    private void SetIncrement2(int n) {
        if (current_set == 1) {
            player2_set[SET_INDEX_1] += n;
            if (player2_set[SET_INDEX_1] > Rules.games_per_set) {
                // >>>> GANHOU O SET NO TIE-BREAK <<<<<
                // vai para o próximo set
                player2_sets++;
                if(Rules.sets_per_match == 1){ winner = PLAYER_ID_2;}
                else { current_set = 2;}
            } else if (player2_set[SET_INDEX_1] == Rules.games_per_set) {
                if ((player2_set[SET_INDEX_1] - player1_set[SET_INDEX_1]) > 1) {
                    // >>>> GANHOU O SET POR 2 OU MAIS GAMES <<<<<
                    player2_sets++;
                    if(Rules.sets_per_match == 1){ winner = PLAYER_ID_2;}
                    else { current_set = 2;}
                } else if(player1_set[SET_INDEX_1] == player2_set[SET_INDEX_1]) { tiebreaking = true;}
            }
            if(current_set == 1){ CheckChangeSides((byte) SET_INDEX_1);}
        } else if (current_set == 2) {
            player2_set[SET_INDEX_2] += n;
            if (player2_set[SET_INDEX_2] > Rules.games_per_set) {
                // >>>> GANHOU O SET NO TIE-BREAK <<<<<
                // vai para o próximo set
                player2_sets++;
                if(player2_sets >= 2){ winner = PLAYER_ID_2;}
                else { current_set = 3;}
            } else if (player2_set[SET_INDEX_2] == Rules.games_per_set) {
                if ((player2_set[SET_INDEX_2] - player1_set[SET_INDEX_2]) > 1) {
                    // >>>> GANHOU O SET POR 2 OU MAIS GAMES <<<<<
                    player2_sets++;
                    if(player2_sets >= 2){ winner = PLAYER_ID_2;}
                    else {
                        current_set = 3;
                        if(Rules.match_tiebreak){
                            Rules.tiebreak = true; tiebreaking = true; this.points_to_win = Rules.points_matchTiebreak;
                        }
                    }
                } else if(player1_set[SET_INDEX_2] == player2_set[SET_INDEX_2]) { tiebreaking = true;}
            }
            if(current_set == 2){ CheckChangeSides((byte) SET_INDEX_2);}
        } else if (current_set == 3) {
            if (!Rules.match_tiebreak) {
                player2_set[SET_INDEX_3] += n;
                if (player2_set[SET_INDEX_3] > Rules.games_per_set) {
                    // >>>> GANHOU O JOGO TIE-BREAK <<<<<
                    // vai para o próximo JOGO
                    player2_sets++;
                    //winner = PLAYER_ID_2;
                    Stop(PLAYER_ID_2);
                    //current_set = 0;
                } else if (player2_set[SET_INDEX_3] == Rules.games_per_set) {
                    if ((player2_set[SET_INDEX_3] - player1_set[SET_INDEX_3]) > 1) {
                        // >>>> GANHOU O SET POR 2 OU MAIS GAMES <<<<<
                        player2_sets++;
                        //winner = PLAYER_ID_2;
                        Stop(PLAYER_ID_2);
                        //current_set = 0;
                    } else if (player1_set[SET_INDEX_3] == player2_set[SET_INDEX_3]) {
                        tiebreaking = true;
                    }
                }
                if (current_set == 3) { CheckChangeSides((byte) SET_INDEX_3);}
            } else {
                ++player2_set[SET_INDEX_3];  winner = PLAYER_ID_2;
                if (eventListener != null) { eventListener.onEvent();}
            }
        }
        if (this.winner > 0) { if (eventListener != null) { eventListener.onEvent();}}
    }

    //----------------------------------------------------------------------------------------------
    public void ScoreIncrement(int player_id) {}

    //----------------------------------------------------------------------------------------------
    public void ScoreDecrement(int player_id) {
        // update tens and units
        UpdatePoints(PLAYER_ID_1);
        // update tens and units
        UpdatePoints(PLAYER_ID_2);
    }

    //----------------------------------------------------------------------------------------------
    public byte getCurrentSet() { return(current_set);}

    //----------------------------------------------------------------------------------------------
    public void setCurrentSet(byte value) {}

    //----------------------------------------------------------------------------------------------
    public void setSet(int player_id, int set_id, String value) {}

    //----------------------------------------------------------------------------------------------
    public void setSet(int player_id, int set_id, byte value) {}

    //----------------------------------------------------------------------------------------------
    public String getScore(int player_id) { return ("");}

    //----------------------------------------------------------------------------------------------
    public byte setPoints(int player_id, byte value) { return (0);}

    //----------------------------------------------------------------------------------------------
    public byte setPoints(int player_id, String value) { return (0);}

    //----------------------------------------------------------------------------------------------
    public void UpdatePoints(int player_id) {}

    //----------------------------------------------------------------------------------------------
    public String getTens(int player_id) {  return ("");}

    //----------------------------------------------------------------------------------------------
    public String getUnits(int player_id) { return ("");}

    //----------------------------------------------------------------------------------------------
    public String getSet1(int player_id) {
        String result = "";
        if (player_id == PLAYER_ID_1) {
            result = String.valueOf(player1_set[SET_INDEX_1]);
        } else if (player_id == PLAYER_ID_2) {
            result = String.valueOf(player2_set[SET_INDEX_1]);
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    public String getSet2(int player_id) {
        String result = " ";

        if (player_id == PLAYER_ID_1) {
            if(current_set > SET_1){ result = String.valueOf(player1_set[SET_INDEX_2]);}
        } else if (player_id == PLAYER_ID_2) {
            if(current_set > SET_1){ result = String.valueOf(player2_set[SET_INDEX_2]);}
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    public String getSet3(int player_id) {
        String result = " ";
        if (player_id == PLAYER_ID_1) {
            if((current_set > SET_2) && !Rules.match_tiebreak){
                result = String.valueOf(player1_set[SET_INDEX_3]);
            }
        } else if (player_id == PLAYER_ID_2) {
            if((current_set > SET_2) && !Rules.match_tiebreak){
                result = String.valueOf(player2_set[SET_INDEX_3]);
            }
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    public byte getCurrentServer() {
        return current_server;
    }

    //----------------------------------------------------------------------------------------------
    public void setCurrentServer(byte current_server) {
        if ((current_server == (byte)PLAYER_ID_1) || (current_server == (byte)PLAYER_ID_2)) {
            this.current_server = current_server;
            setMatchFlags(current_server);
        } else { setMatchFlags((byte)PLAYER_ID_NONE);}
    }

    //----------------------------------------------------------------------------------------------
    public void ToggleServer() {
        if (current_server == (byte)PLAYER_ID_1) {
            this.current_server = (byte)PLAYER_ID_2;
        } else {
            this.current_server = (byte)PLAYER_ID_1;
        }
        setMatchFlags(current_server);
    }

    //----------------------------------------------------------------------------------------------
    public void setMatchFlags(byte server_id) {
        //----------------------------------------------------------------------------------------------
        byte FLAG_SERVICE_MASK = (byte) 0xFC;
        match_flags &= FLAG_SERVICE_MASK;
        if (current_server == PLAYER_ID_1) {
            byte FLAG_SERVICE1 = 0x01;
            match_flags |= FLAG_SERVICE1;
        } else if (current_server == PLAYER_ID_2) {
            byte FLAG_SERVICE2 = 0x02;
            match_flags |= FLAG_SERVICE2;
        }
    }

    //----------------------------------------------------------------------------------------------
    public void copy(Scoreboard src) {
        //---------------------------------------------------
        // Transport section: this data is sent to the scoreboard
        this.player1_tens = src.player1_tens;
        this.player1_units = src.player1_units;
        this.player1_set[SET_INDEX_1] = src.player1_set[SET_INDEX_1];
        this.player1_set[SET_INDEX_2] = src.player1_set[SET_INDEX_2];
        this.player1_set[SET_INDEX_3] = src.player1_set[SET_INDEX_3];
        this.player2_tens = src.player2_tens;
        this.player2_units = src.player2_units;
        this.player2_set[SET_INDEX_1] = src.player2_set[SET_INDEX_1];
        this.player2_set[SET_INDEX_2] = src.player2_set[SET_INDEX_2];
        this.player2_set[SET_INDEX_3] = src.player2_set[SET_INDEX_3];
        this.match_flags = src.match_flags;
        this.match_seconds = src.match_seconds;
        this.match_minutes = src.match_minutes;
        this.match_hours = src.match_hours;

        this.player1_points = src.player1_points;
        this.player2_points = src.player2_points;
        this.player1_sets = src.player1_sets;
        this.player2_sets = src.player2_sets;

        this.current_set = src.current_set;
        this.current_server = src.current_server;
        this.tiebreaking = src.tiebreaking;
        this.points_to_win = src.points_to_win;

        // config params
        this.game_on = src.game_on;
        Rules.copy(src.Rules);
    }

    //----------------------------------------------------------------------------------------------
    public void Export(CommBuffer dst) {
        int PARAM_PLAY1_TENS    = 0;
        int PARAM_PLAY1_UNITS   = 1;
        int PARAM_PLAY1_SET1    = 2;
        int PARAM_PLAY1_SET2    = 3;
        int PARAM_PLAY1_SET3    = 4;
        int PARAM_PLAY2_TENS    = 5;
        int PARAM_PLAY2_UNITS   = 6;
        int PARAM_PLAY2_SET1    = 7;
        int PARAM_PLAY2_SET2    = 8;
        int PARAM_PLAY2_SET3    = 9;
        int PARAM_FLAGS         = 10;
        int PARAM_SECONDS       = 11;
        int PARAM_MINUTES       = 12;
        int PARAM_HOURS         = 13;

        //---------------------------------------------------
        // Transport section: this data is sent to the scoreboard
        dst.data[PARAM_PLAY1_TENS]  = this.player1_tens;
        dst.data[PARAM_PLAY2_TENS]  = this.player2_tens;
        dst.data[PARAM_PLAY1_UNITS] = this.player1_units;
        dst.data[PARAM_PLAY2_UNITS] = this.player2_units;
        dst.data[PARAM_PLAY1_SET1] = this.player1_set[SET_INDEX_1];
        dst.data[PARAM_PLAY2_SET1] = this.player2_set[SET_INDEX_1];
        dst.data[PARAM_PLAY1_SET2] = 0x10;
        dst.data[PARAM_PLAY2_SET2] = 0x10;
        dst.data[PARAM_PLAY1_SET3] = 0x10;
        dst.data[PARAM_PLAY2_SET3] = 0x10;
        if(this.current_set > SET_1) {
            dst.data[PARAM_PLAY1_SET2] = this.player1_set[SET_INDEX_2];
            dst.data[PARAM_PLAY2_SET2] = this.player2_set[SET_INDEX_2];
        }
        if(this.current_set > SET_2) {
            if(!Rules.match_tiebreak) {
                dst.data[PARAM_PLAY1_SET3] = this.player1_set[SET_INDEX_3];
                dst.data[PARAM_PLAY2_SET3] = this.player2_set[SET_INDEX_3];
            }
        }
        dst.data[PARAM_FLAGS] = this.match_flags;
        dst.data[PARAM_HOURS] = this.match_seconds;
        dst.data[PARAM_MINUTES] = this.match_minutes;
        dst.data[PARAM_SECONDS] = this.match_hours;
    }

    //----------------------------------------------------------------------------------------------
    // format timer string
    @SuppressLint("DefaultLocale")
    private String formatTime(int seconds, int minutes, int hours) {
        return (String.format("%02d", hours) + ":" + String.format("%02d", minutes));// + " : " + String.format("%02d",seconds));
    }

    //----------------------------------------------------------------------------------------------
    // formats the timer string
    public String getTimerText(double time) {
        int rounded = (int) Math.round(time);
        match_seconds = (byte)(((rounded % 86400) % 3600) % 60);
        match_minutes = (byte)(((rounded % 86400) % 3600) / 60);
        match_hours = (byte)((rounded % 86400) / 3600);
        return formatTime(match_seconds, match_minutes, match_hours);
    }
}
//==================================================================================================
