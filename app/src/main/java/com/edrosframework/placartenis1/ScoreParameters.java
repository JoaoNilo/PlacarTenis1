//==============================================================================
package com.edrosframework.placartenis1;

import java.io.Serializable;

public class ScoreParameters implements Serializable {

    //--------------------------------------------------------------------------
    public static final int MODE_ID_TENNIS         = 1;
    public static final int MODE_ID_BEACHTENNIS    = 2;
    public static final int MODE_ID_TABLETENNIS    = 3;
    public static final int MODE_ID_BEACHVOLLEY    = 4;
    public static final int MODE_ID_FOOTVOLLEY     = 5;

    //--------------------------------------------------------------------------
    public boolean advantage;
    public boolean tiebreak;
    public boolean match_tiebreak;
    public boolean alternate_service;

    //--------------------------------------------------------------------------
    private int mode;
    public int points_Tiebreak;
    public int games_per_set;
    public int sets_per_match;
    public int points_matchTiebreak;

    //--------------------------------------------------------------------------
    public void copy(ScoreParameters xParams){
        advantage = xParams.advantage;
        tiebreak = xParams.tiebreak;
        match_tiebreak = xParams.match_tiebreak;
        alternate_service = xParams.alternate_service;
        mode = xParams.GetMode();
        points_Tiebreak = xParams.points_Tiebreak;
        games_per_set = xParams.games_per_set;
        sets_per_match = xParams.sets_per_match;
        points_matchTiebreak = xParams.points_matchTiebreak;
    }

    //--------------------------------------------------------------------------
    public ScoreParameters(){
        SetMode(MODE_ID_TENNIS);
    }

    //--------------------------------------------------------------------------
    // usado para recarregar parâmetros "não voláteis"
    public void setMode(int new_mode) {
        mode = new_mode;
    }

    //--------------------------------------------------------------------------
    public void SetMode(int new_mode) {
        switch (new_mode) {
            case MODE_ID_TENNIS:
                mode = new_mode;
                advantage = true;
                tiebreak = true;
                match_tiebreak = true;
                alternate_service = true;
                points_Tiebreak = 7;
                games_per_set = 6;
                sets_per_match = 3;
                points_matchTiebreak = 10;
                break;

            case MODE_ID_BEACHTENNIS:
                mode = new_mode;
                advantage = false;
                tiebreak = true;
                match_tiebreak = true;
                alternate_service = true;
                points_Tiebreak = 7;
                games_per_set = 6;
                sets_per_match = 1;
                points_matchTiebreak = 10;
                break;

            case MODE_ID_TABLETENNIS:
                mode = new_mode;
                advantage = true;
                tiebreak = true;
                match_tiebreak = false;
                alternate_service = false;
                points_Tiebreak = 11;
                games_per_set = 3;
                sets_per_match = 1;
                points_matchTiebreak = 10;
                break;

            case MODE_ID_BEACHVOLLEY:
                mode = new_mode;
                advantage = true;
                tiebreak = true;
                match_tiebreak = true;
                alternate_service = true;
                points_Tiebreak = 21;
                games_per_set = 3;
                sets_per_match = 1;
                points_matchTiebreak = 15;
                break;

            case MODE_ID_FOOTVOLLEY:
                mode = new_mode;
                advantage = true;
                tiebreak = true;
                match_tiebreak = true;
                alternate_service = true;
                points_Tiebreak = 18;
                games_per_set = 3;
                sets_per_match = 1;
                points_matchTiebreak = 15;
                break;
        }
    }

    //--------------------------------------------------------------------------
    public int GetMode(){
        return mode;
    }

    //--------------------------------------------------------------------------
    public String ToString() {
        return(ToString(mode));
    }

    //--------------------------------------------------------------------------
    public String ToString(int tmode){
        String result = "Not Initialized.";
        switch (tmode){
            case MODE_ID_TENNIS: result = "Tennis"; break;
            case MODE_ID_BEACHTENNIS: result = "Beach Tennis"; break;
            case MODE_ID_TABLETENNIS: result = "Table Tennis"; break;
            case MODE_ID_BEACHVOLLEY: result = "Beach Volley"; break;
            case MODE_ID_FOOTVOLLEY: result = "Footvolley"; break;
        }
        return result;
    }

    //--------------------------------------------------------------------------
    public String ModeDescription(){
        String result = "     >>> SCORING RULES <<<\n\n";
        result += "Mode: ............. " + ToString(mode) + "\n";
        result += "Advantage: ........ " + (advantage?"yes":"no") + "\n";
        result += "Tiebreak: ......... " + (tiebreak?"yes":"no") + "\n";
        if(mode == MODE_ID_BEACHTENNIS) {
            result += "Match Tiebreak: ... " + (match_tiebreak ? "yes" : "no") + "\n";
        }
        result += "Alternate Service:  " + (alternate_service?"yes":"no") + "\n";
        result += "Tiebreak Count: ... " + String.valueOf(points_Tiebreak) + "\n";
        result += "Games/Set: ........ " + String.valueOf(games_per_set) + "\n";
        result += "Sets/Match: ....... " + String.valueOf(sets_per_match) + "\n";
        if(mode == MODE_ID_BEACHTENNIS) {
            result += "Match Tiebreak: ... " + String.valueOf(points_matchTiebreak) + "\n";
        }
        return result;
    }

}
//==============================================================================