//==================================================================================================
package com.edrosframework.placartenis1;

import java.util.Locale;

//--------------------------------------------------------------------------------------------------
public class Tennis extends Scoreboard {

    //----------------------------------------------------------------------------------------------
    private final String[] Scores = {"00", "15", "30", "40", "Ad", "  "};
    private final byte[] Tens  = {0x00, 0x01, 0x03, 0x04, 0x0A, 0x10};
    private final byte[] Units = {0x00, 0x05, 0x00, 0x00, 0x0D, 0x10};

    //----------------------------------------------------------------------------------------------
    private int StringToIndex(String PointsString){
        int result = -1;
        switch(PointsString){
            case "00": result = 0; break;
            case "15": result = 1; break;
            case "30": result = 2; break;
            case "40": result = 3; break;
            case "Ad": result = 4; break;
            case "  ": result = 5; break;
            default: break;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setMatchTiebreakPoints(int points) {
        if(points > 80){ points = 80;}
        Rules.points_matchTiebreak = points;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setAlternateService(boolean alternate_service) { Rules.alternate_service = alternate_service;}

    //----------------------------------------------------------------------------------------------
    @Override
    public void setGamesPerSet(int games_set) {
        if(games_set < 1){ Rules.games_per_set = 1;}
        else if(games_set > 8){ Rules.games_per_set = 8;}
        else{ Rules.games_per_set = games_set;}
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setPointsPerGame(int new_points_per_game) {
        if(new_points_per_game < 2){ Rules.points_Tiebreak = 2;}
        else if(new_points_per_game > 98){ Rules.points_Tiebreak = 98;}
        else{ Rules.points_Tiebreak = new_points_per_game;}
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setSetsPerMatch(int sets_per_match) {
        if(sets_per_match < 1){ Rules.sets_per_match = 1;}
        else if(sets_per_match > 3){ Rules.sets_per_match = 3;}
        else{ Rules.sets_per_match = sets_per_match;}
    }

    //----------------------------------------------------------------------------------------------
    // MAIN CONSTRUCTOR
    //----------------------------------------------------------------------------------------------
    public Tennis(ScoreParameters set_rules) {
        super(set_rules);
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
    @Override
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
    public void ScoreIncrement(int player_id) {

        if((Rules.GetMode() == ScoreParameters.MODE_ID_BEACHTENNIS)||
                (Rules.GetMode() == ScoreParameters.MODE_ID_TENNIS)) {
            if (Rules.tiebreak && tiebreaking) {
                TennisTiebreak(player_id);
            } else {
                TennisIncrement(player_id);
            }
        } else {
            TennisTiebreak(player_id);
        }
    }

    //----------------------------------------------------------------------------------------------
    private static final int POINT_00 = 0;
    private static final int POINT_15 = 1;
    private static final int POINT_30 = 2;
    private static final int POINT_40 = 3;
    private static final int POINT_AD = 4;
    private static final int POINT_NO = 5;
    private void TennisIncrement(int player_id) {
        int game = 0;
        if (player_id == PLAYER_ID_1) {
            if(++player1_points > POINT_40) {
                if(!Rules.advantage) { game = player_id;}
                else {
                    if(player2_points < POINT_40) { game = player_id;}
                    else if(player2_points == POINT_40){
                        // player1 advantage
                        player2_points = POINT_NO;
                    } else if(player2_points == POINT_AD){
                        // deuce
                        player1_points = POINT_40;
                        player2_points = POINT_40;
                    } else { game = player_id;}
                }
            }

        } else if (player_id == PLAYER_ID_2) {
            if (++player2_points > POINT_40) {
                if(!Rules.advantage) { game = player_id;}
                else {
                    if(player1_points < POINT_40) { game = player_id;}
                    else if(player1_points == POINT_40){
                        // player2 advantage
                        player1_points = POINT_NO;
                    } else if(player1_points == POINT_AD){
                        // deuce
                        player1_points = POINT_40;
                        player2_points = POINT_40;
                    } else { game = player_id;}
                }
            }
        }

        if(game == PLAYER_ID_1){
            player1_points = 0;
            player2_points = 0;
            SetIncrement1(1);
            ToggleServer();
        } else if(game == PLAYER_ID_2){
            player1_points = 0;
            player2_points = 0;
            SetIncrement2(1);
            ToggleServer();
        }

        if(this.game_on) {
            // update tens and units
            UpdatePoints(PLAYER_ID_1);
            UpdatePoints(PLAYER_ID_2);
        }
    }

    //----------------------------------------------------------------------------------------------
    private void TennisTiebreak(int player_id) {
        int game = 0;
        if (player_id == PLAYER_ID_1) {
            if(++player1_points >= points_to_win) {
                if(player1_points-player2_points > 1) { game = player_id;}
            }
        } else if (player_id == PLAYER_ID_2) {
            if(++player2_points >= points_to_win) {
                if(player2_points-player1_points > 1) { game = player_id;}
            }
        }

        if(game == PLAYER_ID_1){
            player1_points = 0;
            player2_points = 0;
            SetIncrement1(1);
            if( Rules.alternate_service){ ToggleServer();}
            tiebreaking = false;
        } else if(game == PLAYER_ID_2){
            player1_points = 0;
            player2_points = 0;
            SetIncrement2(1);
            if( Rules.alternate_service){ ToggleServer();}
            tiebreaking = false;
        } else {
            if(Rules.alternate_service) {
                // check for court switch
                if (((player1_points + player2_points) % 6) == 0) {
                    // SWITCH COURT SIDES EVENT
                    // Trigger the event listener if it's not null
                    if (eventListener != null) {
                        change_court = true;
                        eventListener.onEvent();
                        change_court = false;
                    }
                }
                // check for service switch
                if (((player1_points + player2_points) % 2) == 1) {
                    // SWITCH SERVICE FLAG
                    ToggleServer();
                    // Trigger the event listener if it's not null
                    if (eventListener != null) {
                        change_server = true;
                        eventListener.onEvent();
                        change_server = false;
                    }
                }
            }
        }

        if(this.game_on) {
            // update tens and units
            UpdatePoints(PLAYER_ID_1);
            UpdatePoints(PLAYER_ID_2);
        }

    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void ScoreDecrement(int player_id) {
        if (player_id == PLAYER_ID_1) {
            if (player1_points > 0) {
                if(!tiebreaking && (player1_points == 5)){ return;}
                if(!tiebreaking&& (player1_points == 4)){ player1_points = 3; player2_points = 3;}
                else { player1_points--;}
            }
        } else {
            if (player2_points > 0) {
                if(!tiebreaking&& (player2_points == 5)){ return;}
                if(!tiebreaking && (player2_points == 4)){ player2_points = 3; player1_points = 3;}
                else { player2_points--;}
            }
        }
        // update tens and units
        UpdatePoints(PLAYER_ID_1);
        // update tens and units
        UpdatePoints(PLAYER_ID_2);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCurrentSet(byte value) {
        if(value > SET_3){ value = SET_3;}
        current_set = value;
        if(current_set == SET_1) {
            player1_set[SET_INDEX_2] = 0; player2_set[SET_INDEX_2] = 0;
            player1_set[SET_INDEX_3] = 0; player2_set[SET_INDEX_3] = 0;
        } else if(current_set == SET_2) {
            player1_set[SET_INDEX_3] = 0; player2_set[SET_INDEX_3] = 0;
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setSet(int player_id, int set_id, String value) {
        byte v = (byte)Integer.parseUnsignedInt(value);
        if(v <= Rules.games_per_set+1){
            setSet(player_id, set_id, v);
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setSet(int player_id, int set_id, byte value) {
        if((set_id >= SET_INDEX_1) && (set_id <= SET_INDEX_3)){
            if (player_id == PLAYER_ID_1) { player1_set[set_id] = value;}
            else if (player_id == PLAYER_ID_2) { player2_set[set_id] = value;}
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String getScore(int player_id) {
        String result = "";
        if(Rules.tiebreak && tiebreaking){
            if (player_id == PLAYER_ID_1) { result = String.format(Locale.getDefault(), "%d", player1_points);}
            else if (player_id == PLAYER_ID_2) { result = String.format(Locale.getDefault(), "%d", player2_points);}
        } else {
            if (player_id == PLAYER_ID_1) { result = Scores[player1_points];}
            else if (player_id == PLAYER_ID_2) { result = Scores[player2_points];}
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public byte setPoints(int player_id, byte value) {
        byte result = 0;
        if (player_id == PLAYER_ID_1) {
            player1_points = value;
            player1_tens = Tens[player1_points];
            player1_units = Units[player1_points];
        } else if (player_id == PLAYER_ID_2) {
            player2_points = value;
            player2_tens = Tens[player2_points];
            player2_units = Units[player2_points];
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public byte setPoints(int player_id, String value) {
        byte result = 0;
        if(!this.tiebreaking) {
            int pt = StringToIndex(value);
            if (player_id == PLAYER_ID_1) {
                if (pt > 0) {
                    player1_points = (byte) pt;
                }
                player1_tens = Tens[player1_points];
                player1_units = Units[player1_points];
            } else if (player_id == PLAYER_ID_2) {
                if (pt > 0) {
                    player2_points = (byte) pt;
                }
                player2_tens = Tens[player2_points];
                player2_units = Units[player2_points];
            }
        } else {
            int pt = Integer.parseUnsignedInt(value);
            if (player_id == PLAYER_ID_1) { player1_points = (byte)pt;}
            else if(player_id == PLAYER_ID_2) { player2_points = (byte)pt;}
            UpdatePoints(player_id);
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void UpdatePoints(int player_id) {
        // byte val = 0;
        if((Rules.GetMode() == ScoreParameters.MODE_ID_BEACHTENNIS)||
                (Rules.GetMode() == ScoreParameters.MODE_ID_TENNIS)) {
            if ((Rules.tiebreak) && (tiebreaking)) {
                // update tens and units
                if (player_id == PLAYER_ID_1) {
                    player1_tens = (byte) (player1_points / 10);
                    if (player1_tens == 0) {player1_tens = 0x10;}
                    player1_units = (byte) (player1_points % 10);
                } else if (player_id == PLAYER_ID_2) {
                    player2_tens = (byte) (player2_points / 10);
                    if (player2_tens == 0) {player2_tens = 0x10;}
                    player2_units = (byte) (player2_points % 10);
                }
            } else {
                if (player_id == PLAYER_ID_1) {
                    player1_tens = Tens[player1_points];
                    player1_units = Units[player1_points];
                } else if (player_id == PLAYER_ID_2) {
                    player2_tens = Tens[player2_points];
                    player2_units = Units[player2_points];
                }
            }
        } else {
            // update tens and units
            if (player_id == PLAYER_ID_1) {
                player1_tens = (byte) (player1_points / 10);
                if (player1_tens == 0) {player1_tens = 0x10;}
                player1_units = (byte) (player1_points % 10);
            } else if (player_id == PLAYER_ID_2) {
                player2_tens = (byte) (player2_points / 10);
                if (player2_tens == 0) {player2_tens = 0x10;}
                player2_units = (byte) (player2_points % 10);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String getTens(int player_id) {
        String result = "";
        if (player_id == PLAYER_ID_1) {
            if (player1_tens >= 0x10) { result = " ";}
            else { result = String.format("%01X", player1_tens);}
        } else if (player_id == PLAYER_ID_2) {
            if (player2_tens >= 0x10) { result = " ";}
            else { result = String.format("%01X", player2_tens);}
        }
        if(result == "D"){result = "d";}
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public String getUnits(int player_id) {
        String result = "";
        if((Rules.tiebreak)&&(tiebreaking)){
            // update tens and units
            if (player_id == PLAYER_ID_1) {
                result = String.valueOf(player1_units);
            } else if (player_id == PLAYER_ID_2) {
                result = String.valueOf(player2_units);
            }
        } else {
            if (player_id == PLAYER_ID_1) {
                result = Scores[player1_points].substring(1, 2);
            } else if (player_id == PLAYER_ID_2) {
                result = Scores[player2_points].substring(1, 2);
            }
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    /*public String getSet1(int player_id) {
        String result = "";
        if (player_id == PLAYER_ID_1) {
            result = String.valueOf(player1_set[SET_INDEX_1]);
        } else if (player_id == PLAYER_ID_2) {
            result = String.valueOf(player2_set[SET_INDEX_1]);
        }
        return (result);
    }*/

    //----------------------------------------------------------------------------------------------
    /*public String getSet2(int player_id) {
        String result = " ";

        if (player_id == PLAYER_ID_1) {
            if(current_set > SET_1){ result = String.valueOf(player1_set[SET_INDEX_2]);}
        } else if (player_id == PLAYER_ID_2) {
            if(current_set > SET_1){ result = String.valueOf(player2_set[SET_INDEX_2]);}
        }
        return (result);
    }*/

    //----------------------------------------------------------------------------------------------
    /*public String getSet3(int player_id) {
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
    }*/

    //----------------------------------------------------------------------------------------------
    @Override
    public byte getCurrentServer() {
        return current_server;
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void setCurrentServer(byte current_server) {
        if ((current_server == (byte)PLAYER_ID_1) || (current_server == (byte)PLAYER_ID_2)) {
            this.current_server = current_server;
            setMatchFlags(current_server);
        } else { setMatchFlags((byte)PLAYER_ID_NONE);}
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void ToggleServer() {
        if (current_server == (byte)PLAYER_ID_1) {
            this.current_server = (byte)PLAYER_ID_2;
        } else {
            this.current_server = (byte)PLAYER_ID_1;
        }
        setMatchFlags(current_server);
    }

    //----------------------------------------------------------------------------------------------
    @Override
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

}
//==================================================================================================
