//==============================================================================
package com.edrosframework.placartenis1;

public class ScoreParameters{

    //--------------------------------------------------------------------------
    public final int MODE_ID_TENNIS         = 1;
    public final int MODE_ID_BEACHTENNIS    = 2;
    public final int MODE_ID_TABLETENNIS    = 3;
    public final int MODE_ID_BEACHVOLLEY    = 4;
    public final int MODE_ID_FOOTVOLLEY     = 5;

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
    public ScoreParameters(){
        SetMode(MODE_ID_TENNIS);
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
    public String ToString(){
        String result = "Not Initialized.";
        switch (mode){
            case MODE_ID_TENNIS: result = "Tennis"; break;
            case MODE_ID_BEACHTENNIS: result = "Beach Tennis"; break;
            case MODE_ID_TABLETENNIS: result = "Table Tennis"; break;
            case MODE_ID_BEACHVOLLEY: result = "Beach Volleyball"; break;
            case MODE_ID_FOOTVOLLEY: result = "Footvolley"; break;
        }
        return result;
    }
}
//==============================================================================