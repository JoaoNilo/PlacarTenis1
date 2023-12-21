//==============================================================================
package com.edrosframework.placartenis1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;

//------------------------------------------------------------------------------
public class GameSettings extends AppCompatActivity implements KeypadListener { //GestureDetector.OnGestureListener{

    private Button btBack;
    private TextView txtPlayer1Points;
    private TextView txtPlayer2Points;
    private TextView txtPlayer1Set1;
    private TextView txtPlayer1Set2;
    private TextView txtPlayer1Set3;
    private TextView txtPlayer2Set1;
    private TextView txtPlayer2Set2;
    private TextView txtPlayer2Set3;

    private TextView txtCurrentSet;
    private TextView txtGamesPerSet;
    private TextView txtPointsPerGame;
    private TextView txtSetsPerMatch;
    private TextView txtMatchTiebreak;
    private Switch swAdvantage;
    private Switch swTiebreak;
    private Switch swMatchTiebreak;
    private Switch swAlternateService;

    private TextView lbPointsPerGame;

    Scoreboard MatchConfig;
    boolean settings_changed = false;
    boolean score_changed = false;
    int sets_per_match;
    boolean close_it = false;

    //----------------------------------------------------------------------------
    // passed to the custom keyboard
    String value = "0";
    String keys = "0,1,2,3,4,5,6,7,8,9";
    String rangeMin = "1";
    String rangeMax = "3";
    String k_title = "Custom Keyboard";

    //--------------------------------------------------------------------------
    private void SettingsEnable(boolean status) {
        if(!status){
            swAdvantage.setTrackResource(R.drawable.track2_off);
            swTiebreak.setTrackResource(R.drawable.track2_off);
            swMatchTiebreak.setTrackResource(R.drawable.track2_off);
            swAlternateService.setTrackResource(R.drawable.track2_off);
        } else {
            swAdvantage.setTrackResource(R.drawable.track2_on);
            swTiebreak.setTrackResource(R.drawable.track2_on);
            swMatchTiebreak.setTrackResource(R.drawable.track2_on);
            swAlternateService.setTrackResource(R.drawable.track2_on);
        }
        swAdvantage.setEnabled(status);
        swTiebreak.setEnabled(status);
        swMatchTiebreak.setEnabled(status);

        swAlternateService.setEnabled(status);

        txtGamesPerSet.setEnabled(status);
        txtPointsPerGame.setEnabled(status);
        txtSetsPerMatch.setEnabled(status);
        txtMatchTiebreak.setEnabled(status);
    }

    //--------------------------------------------------------------------------
    private void CheckTiebreak() {
        if (swTiebreak.isChecked()) {
            txtPointsPerGame.setTextColor(Color.WHITE);
            txtPointsPerGame.setFocusable(true);
            lbPointsPerGame.setTextColor(Color.WHITE);
        } else {
            txtPointsPerGame.setTextColor(Color.DKGRAY);
            txtPointsPerGame.setFocusable(false);
            lbPointsPerGame.setTextColor(Color.DKGRAY);
        }
    }

    //--------------------------------------------------------------------------
    private void CheckMatchTiebreak(){
        if(swMatchTiebreak.isChecked()){
            txtMatchTiebreak.setFocusable(true);
            txtMatchTiebreak.setEnabled(true);
            txtMatchTiebreak.setTextColor(Color.GRAY);
        } else {
            txtMatchTiebreak.setFocusable(false);
            txtMatchTiebreak.setEnabled(false);
            txtMatchTiebreak.setTextColor(Color.DKGRAY);
        }
    }

    //--------------------------------------------------------------------------
    private void CheckSetsPerMatch(String val) {
        int v = Integer.parseInt(val);
        if(v<0){ return;}
        if(v<2) {
            if(MatchConfig.isGameOn()){ txtCurrentSet.setText("1");}
            else {txtCurrentSet.setText("0");}
            txtPlayer1Set2.setText("");
            txtPlayer1Set3.setText("");
            txtPlayer2Set2.setText("");
            txtPlayer2Set3.setText("");
        } else {
            txtPlayer1Set2.setText("0");
            txtPlayer1Set3.setText("0");
            txtPlayer2Set2.setText("0");
            txtPlayer2Set3.setText("0");
        }
    }

    //--------------------------------------------------------------------------
    private void ResetScore(String set_id) {
        //int v = Integer.parseInt(set_id);
        //if(set_id< 0){ return;}
        txtPlayer1Set1.setClickable(false);
        txtPlayer2Set1.setClickable(false);
        txtPlayer1Set2.setClickable(false);
        txtPlayer2Set2.setClickable(false);
        txtPlayer1Set3.setClickable(false);
        txtPlayer2Set3.setClickable(false);
        txtPlayer1Set1.setTextColor(Color.BLACK);
        txtPlayer2Set1.setTextColor(Color.BLACK);
        txtPlayer1Set2.setTextColor(Color.BLACK);
        txtPlayer2Set2.setTextColor(Color.BLACK);
        txtPlayer1Set3.setTextColor(Color.BLACK);
        txtPlayer2Set3.setTextColor(Color.BLACK);

        switch (set_id) {
            case "1":
                txtPlayer1Set1.setClickable(true);
                txtPlayer2Set1.setClickable(true);
                txtPlayer1Set1.setTextColor(Color.WHITE);
                txtPlayer2Set1.setTextColor(Color.WHITE);
                break;
            case "2":
                txtPlayer1Set1.setClickable(true);
                txtPlayer2Set1.setClickable(true);
                txtPlayer1Set2.setClickable(true);
                txtPlayer2Set2.setClickable(true);
                txtPlayer1Set1.setTextColor(Color.WHITE);
                txtPlayer2Set1.setTextColor(Color.WHITE);
                txtPlayer1Set2.setTextColor(Color.WHITE);
                txtPlayer2Set2.setTextColor(Color.WHITE);
                break;
            case "3":
                txtPlayer1Set1.setClickable(true);
                txtPlayer2Set1.setClickable(true);
                txtPlayer1Set2.setClickable(true);
                txtPlayer2Set2.setClickable(true);
                txtPlayer1Set3.setClickable(true);
                txtPlayer2Set3.setClickable(true);
                txtPlayer1Set1.setTextColor(Color.WHITE);
                txtPlayer2Set1.setTextColor(Color.WHITE);
                txtPlayer1Set2.setTextColor(Color.WHITE);
                txtPlayer2Set2.setTextColor(Color.WHITE);
                txtPlayer1Set3.setTextColor(Color.WHITE);
                txtPlayer2Set3.setTextColor(Color.WHITE);
                break;
        }
    }

    //--------------------------------------------------------------------------
    // check if set[set_index] has a winner. if so, update "current_set"
    private boolean ValidateSet(byte set_index){
        boolean result = false;
        if(set_index > MatchConfig.SET_INDEX_3){ return(false);}

        byte realSet = (byte)(set_index + 1);
        if(MatchConfig.CheckSet(set_index) != MatchConfig.PLAYER_ID_NONE){
            result = true;
            if(realSet < this.sets_per_match) {
                if (++realSet > MatchConfig.SET_3) {realSet = MatchConfig.SET_3;}
            }
        }

        MatchConfig.setCurrentSet(realSet);
        String prtSet = String.format(Locale.getDefault(), "%d", realSet);
        txtCurrentSet.setText(prtSet);
        return result;
    };

    //--------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_settings);
        Objects.requireNonNull(getSupportActionBar()).hide(); // remove action bar (title bar)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();
        MatchConfig = (Scoreboard) intent.getSerializableExtra("Match");

        settings_changed = false;
        score_changed = false;

        lbPointsPerGame = findViewById(R.id.lbPointsPerGame);

        txtPlayer1Points = (TextView) findViewById(R.id.txtPlayer1_Points);
        txtPlayer1Set1 = (TextView) findViewById(R.id.txtPlayer1_Set1);
        txtPlayer1Set2 = (TextView) findViewById(R.id.txtPlayer1_Set2);
        txtPlayer1Set3 = (TextView) findViewById(R.id.txtPlayer1_Set3);
        txtPlayer2Points = (TextView) findViewById(R.id.txtPlayer2_Points);
        txtPlayer2Set1 = (TextView) findViewById(R.id.txtPlayer2_Set1);
        txtPlayer2Set2 = (TextView) findViewById(R.id.txtPlayer2_Set2);
        txtPlayer2Set3 = (TextView) findViewById(R.id.txtPlayer2_Set3);

        txtCurrentSet = (TextView) findViewById(R.id.txtCurrentSet);
        txtGamesPerSet = (TextView) findViewById(R.id.txtGamesPerSet);
        txtPointsPerGame = (TextView) findViewById(R.id.txtPointsPerGame);
        txtSetsPerMatch = (TextView) findViewById(R.id.txtSetsPerMatch);
        txtMatchTiebreak = (TextView) findViewById(R.id.txtMatchTiebreak);
        swAdvantage = (Switch)   findViewById(R.id.swAdvantage);
        swTiebreak = (Switch)   findViewById(R.id.swTiebreak);
        swMatchTiebreak = (Switch) findViewById(R.id.swMatchTieBreak);
        swAlternateService = (Switch) findViewById(R.id.swTeste);

        SettingsEnable(!MatchConfig.isGameOn());

        //----------------------------------------------------------------------
        // handles the "Service" flag
        ImageView btP1Service = findViewById(R.id.imgPlayer1Serv);
        ImageView btP2Service = findViewById(R.id.imgPlayer2Serv);
        final Resources resources = getResources();
        final Drawable arrow_on;
        final Drawable arrow_off;
        arrow_on = resources.getDrawable(R.drawable.arrow_white, getTheme());
        arrow_off = resources.getDrawable(R.drawable.arrow_darkgray, getTheme());

        //----------------------------------------------------------------------
        // LEFT PANEL: SCOREBOARD PARAMETERS
        //----------------------------------------------------------------------
        // Advantage: handles the "Advantage" switch events
        swAdvantage.setChecked(MatchConfig.isAdvantage());
        swAdvantage.setOnClickListener(view -> settings_changed  = true);

        //----------------------------------------------------------------------
        // Tiebreak: handles the "Tiebreak" switch events
        swTiebreak.setChecked(MatchConfig.isTiebreak());
        swTiebreak.setOnClickListener(view -> {
            settings_changed  = true;
            CheckTiebreak();
        });

        //----------------------------------------------------------------------
        // Tennis Mode: handles the "Tennis Mode" switch events
        swMatchTiebreak.setChecked(MatchConfig.isMatchTiebreak());
        swMatchTiebreak.setOnClickListener(view -> {
            settings_changed  = true;
            CheckMatchTiebreak();
        });

        //----------------------------------------------------------------------
        // Teste: handles the "Teste" switch events
        swAlternateService.setChecked(MatchConfig.isAlternateService());
        swAlternateService.setOnClickListener(view -> settings_changed  = true);

        //----------------------------------------------------------------------
        // Games Per Set: handles the "Games Per Set" events
        txtGamesPerSet.setText(String.format(Locale.getDefault(), "%d",MatchConfig.getGamesPerSet()));
        txtGamesPerSet.setOnClickListener(view -> {
            // prepare the keyboard settings
            value = String.format(Locale.getDefault(),"%01d", MatchConfig.getGamesPerSet());
            keys = "1,2,4,6,";
            rangeMin = "1";
            rangeMax = "6";
            k_title = "REQUEST_GAMES";
            // instantiate and show the keyboard
            Keypad1 Teclado1 = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado1.show(getSupportFragmentManager(), "Games Per Set");
        });

        //----------------------------------------------------------------------
        // Points Per Game: handles the "Tiebreak Points" events (tiebreak points)
        txtPointsPerGame.setText(String.format(Locale.getDefault(), "%d", MatchConfig.getPointsPerGame()));
        CheckTiebreak();
        txtPointsPerGame.setOnClickListener(view -> {
            value = String.format(Locale.getDefault(),"%01d", MatchConfig.getPointsPerGame());
            keys = "0,1,2,3,4,5,6,7,8,9";
            rangeMin = "1";
            rangeMax = "98";
            k_title = "REQUEST_POINTS";

            Keypad1 Teclado2 = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado2.show(getSupportFragmentManager(), "Tiebreak Points");
        });

        //----------------------------------------------------------------------
        // Sets Per Match: handles the "Sets Per Match" events
        txtSetsPerMatch.setText(String.format(Locale.getDefault(), "%d",MatchConfig.getSetsPerMatch()));
        sets_per_match = Integer.parseUnsignedInt(txtSetsPerMatch.getText().toString());
        CheckMatchTiebreak();
        txtSetsPerMatch.setOnClickListener(view -> {
            value = String.format(Locale.getDefault(),"%01d", MatchConfig.getSetsPerMatch());
            keys = "1,3";
            rangeMin = "1";
            rangeMax = "3";
            k_title = "REQUEST_SETS";

            Keypad1 Teclado3 = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado3.show(getSupportFragmentManager(), "Sets Per Match");
        });

        //----------------------------------------------------------------------
        // Match Tiebreak Points: handles the "Match Tiebreak Points" events (match tiebreak points)
        txtMatchTiebreak.setText(String.format(Locale.getDefault(), "%d", MatchConfig.getMatchTiebreakPoints()));
        CheckMatchTiebreak();
        txtMatchTiebreak.setOnClickListener(view -> {
            value = String.format(Locale.getDefault(),"%01d", MatchConfig.getMatchTiebreakPoints());
            keys = "0,1,2,3,4,5,6,7,8,9";
            rangeMin = "1";
            rangeMax = "98";
            k_title = "REQUEST_MATCH_TIE";

            Keypad1 Teclado2 = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado2.show(getSupportFragmentManager(), "Match Tiebreak Points");
        });

        //-----------------------------------------------------------------------
        // RIGHT PANEL: MATCH PARAMETERS
        // ----------------------------------------------------------------------
        // Player 1: handles the "Service" flag
        btP1Service.setOnClickListener(view -> {
            btP1Service.setImageDrawable(arrow_on);
            if (MatchConfig.getCurrentServer() == MatchConfig.PLAYER_ID_2) {
                btP2Service.setImageDrawable(arrow_off);
            }
            MatchConfig.setCurrentServer((byte) 1);
        });

        //----------------------------------------------------------------------
        // Player 2: handles the "Service" flag
        btP2Service.setOnClickListener(view -> {
            btP2Service.setImageDrawable(arrow_on);
            if (MatchConfig.getCurrentServer() == MatchConfig.PLAYER_ID_1) {
                btP1Service.setImageDrawable(arrow_off);
            }
            MatchConfig.setCurrentServer((byte) 2);
        });

        //----------------------------------------------------------------------
        // Player 1 Points: handles the "Points" touch event (call keyboard)
        txtPlayer1Points.setText(MatchConfig.getScore(MatchConfig.PLAYER_ID_1));
        txtPlayer1Points.setOnClickListener(view -> {
            if(!MatchConfig.isTiebreaking()) {
                value = txtPlayer1Points.getText().toString();
                k_title = "Player1";
                if(MatchConfig.isAdvantage()){ keys = "Full";}
                else {keys = "40";}
                Keypad2 Teclado = Keypad2.newInstance(k_title, keys, value);
                Teclado.show(getSupportFragmentManager(), "Player1 points");
            } else {
                value = txtPlayer1Points.getText().toString();
                keys = "0,1,2,3,4,5,6,7,8,9";
                rangeMin = "1";
                rangeMax = "98";
                k_title = "Player1";
                Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
                Teclado.show(getSupportFragmentManager(), "Player1 points");
            }
        });

        //----------------------------------------------------------------------
        // Player 2 Points: handles the "Points" touch event (call keyboard)
        txtPlayer2Points.setText(MatchConfig.getScore(MatchConfig.PLAYER_ID_2));
        txtPlayer2Points.setOnClickListener(view -> {
            if(!MatchConfig.isTiebreaking()) {
                value = txtPlayer2Points.getText().toString();
                k_title = "Player2";
                if(MatchConfig.isAdvantage()){ keys = "Full";}
                else {keys = "40";}
                Keypad2 Teclado = Keypad2.newInstance(k_title, keys, value);
                Teclado.show(getSupportFragmentManager(), "Player2 points");
            } else {
                value = txtPlayer1Points.getText().toString();
                keys = "0,1,2,3,4,5,6,7,8,9";
                rangeMin = "1";
                rangeMax = "98";
                k_title = "Player2";
                Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
                Teclado.show(getSupportFragmentManager(), "Player2 points");
            }
        });

        //----------------------------------------------------------------------
        // Player 1 Set1: handles the "Set1" touch event (call keyboard)
        txtPlayer1Set1.setText(MatchConfig.getSet1(MatchConfig.PLAYER_ID_1));
        txtPlayer1Set1.setOnClickListener(view -> {
            if(!txtPlayer1Set1.isClickable()){ return;}
            int range = MatchConfig.getGamesPerSet()+1;
            if(range < 2){ return;}
                String k;
                keys = "0";
            for(int i=0; i <= range; i++){
                    k = String.format(Locale.getDefault(),"%01d", i);
                keys = keys.concat( ",").concat(k);
            }
            value = txtPlayer1Set1.getText().toString();
            rangeMin = "0";
            rangeMax = String.format(Locale.getDefault(),"%d", range);
            k_title = "Player1_Set1";

            //EXCEPTION
            if(MatchConfig.getSet1(MatchConfig.PLAYER_ID_2).equals("7")){
                rangeMin = "6"; rangeMax="6"; keys = "6"; value = "6";
            }

            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado.show(getSupportFragmentManager(), "Player1 Set1");
        });

        //----------------------------------------------------------------------
        // Player 1 Set2: handles the "Set2" touch event (call keyboard)
        txtPlayer1Set2.setText(MatchConfig.getSet2(MatchConfig.PLAYER_ID_1));
        txtPlayer1Set2.setOnClickListener(view -> {
            if(!txtPlayer1Set2.isClickable()){ return;}
            int range = MatchConfig.getGamesPerSet()+1;
            if(range < 2){ return;}
                String k;
                keys = "0";
            for(int i=0; i <= range; i++){
                k = String.format(Locale.getDefault(),"%01d", i);
                keys = keys.concat( ",").concat(k);
            }
            value = txtPlayer1Set2.getText().toString();
            rangeMin = "0";
            rangeMax = String.format(Locale.getDefault(),"%d", range);
            k_title = "Player1_Set2";

            //EXCEPTION
            if(MatchConfig.getSet2(MatchConfig.PLAYER_ID_2).equals("7")){
                rangeMin = "6"; rangeMax="6"; keys = "6"; value = "6";
            }

            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado.show(getSupportFragmentManager(), "PLayer1 Set2");
        });

        //----------------------------------------------------------------------
        // Player 1 Set3: handles the "Set3" touch event (call keyboard)
        txtPlayer1Set3.setText(MatchConfig.getSet3(MatchConfig.PLAYER_ID_1));
        txtPlayer1Set3.setOnClickListener(view -> {
            if(!txtPlayer1Set3.isClickable()){ return;}
            int range = MatchConfig.getGamesPerSet()+1;
            if(range < 2){ return;}
                String k;
                keys = "0";
            for(int i=0; i <= range; i++){
                k = String.format(Locale.getDefault(),"%01d", i);
                keys = keys.concat( ",").concat(k);
            }
            value = txtPlayer1Set3.getText().toString();
            rangeMin = "0";
            rangeMax = String.format(Locale.getDefault(),"%d", range);
            k_title = "Player1_Set3";

            //EXCEPTION
            if(MatchConfig.getSet3(MatchConfig.PLAYER_ID_2).equals("7")){
                rangeMin = "6"; rangeMax="6"; keys = "6"; value = "6";
            }

            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado.show(getSupportFragmentManager(), "PLayer1 Set3");
        });

        //----------------------------------------------------------------------
        // Player 2 Set1: handles the "Set1" touch event (call keyboard)
        txtPlayer2Set1.setText(MatchConfig.getSet1(MatchConfig.PLAYER_ID_2));
        txtPlayer2Set1.setOnClickListener(view -> {
            if(!txtPlayer2Set1.isClickable()){ return;}
            int range = MatchConfig.getGamesPerSet()+1;
            if(range < 2){ return;}
            String k;
            keys = "0";
            for(int i=0; i <= range; i++){
                k = String.format(Locale.getDefault(),"%01d", i);
                keys = keys.concat( ",").concat(k);
            }
            value = txtPlayer2Set1.getText().toString();
            rangeMin = "0";
            rangeMax = String.format(Locale.getDefault(),"%d", range);
            k_title = "Player2_Set1";

            //EXCEPTION
            if(MatchConfig.getSet1(MatchConfig.PLAYER_ID_1).equals("7")){
                rangeMin = "6"; rangeMax="6"; keys = "6"; value = "6";
            }
            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado.show(getSupportFragmentManager(), "PLayer2 Set1");
        });

        //----------------------------------------------------------------------
        // Player 2 Set2: handles the "Set2" touch event (call keyboard)
        txtPlayer2Set2.setText(MatchConfig.getSet2(MatchConfig.PLAYER_ID_2));
        txtPlayer2Set2.setOnClickListener(view -> {
            if(!txtPlayer2Set2.isClickable()){ return;}
            int range = MatchConfig.getGamesPerSet()+1;
            if(range < 2){ return;}
            String k;
            keys = "0";
            for(int i=0; i <= range; i++){
                k = String.format(Locale.getDefault(),"%01d", i);
                keys = keys.concat( ",").concat(k);
            }
            value = txtPlayer2Set2.getText().toString();
            rangeMin = "0";
            rangeMax = String.format(Locale.getDefault(),"%d", range);
            k_title = "Player2_Set2";

            //EXCEPTION
            if(MatchConfig.getSet2(MatchConfig.PLAYER_ID_1).equals("7")){
                rangeMin = "6"; rangeMax="6"; keys = "6"; value = "6";
            }

            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado.show(getSupportFragmentManager(), "PLayer2 Set2");
        });

        //----------------------------------------------------------------------
        // Player 2 Set3: handles the "Set3" touch event (call keyboard)
        txtPlayer2Set3.setText(MatchConfig.getSet3(MatchConfig.PLAYER_ID_2));
        txtPlayer2Set3.setOnClickListener(view -> {
            if(!txtPlayer2Set3.isClickable()){ return;}
            int range = MatchConfig.getGamesPerSet()+1;
            if(range < 2){ return;}
            String k;
            keys = "0";
            for(int i=0; i <= range; i++){
                k = String.format(Locale.getDefault(),"%01d", i);
                keys = keys.concat( ",").concat(k);
            }
            value = txtPlayer2Set3.getText().toString();
            rangeMin = "0";
            rangeMax = String.format(Locale.getDefault(),"%d", range);
            k_title = "Player2_Set3";

            //EXCEPTION
            if(MatchConfig.getSet3(MatchConfig.PLAYER_ID_1).equals("7")){
                rangeMin = "6"; rangeMax="6"; keys = "6"; value = "6";
            }

            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, value);
            Teclado.show(getSupportFragmentManager(), "PLayer2 Set3");
        });

        //----------------------------------------------------------------------
        // Current Set: handles the "Current Set" spinner events
        txtCurrentSet.setText(String.format(Locale.getDefault(), "%d", MatchConfig.getCurrentSet()));
        ResetScore(txtCurrentSet.getText().toString());
        txtCurrentSet.setOnClickListener(view -> {
            //value = String.format(Locale.getDefault(),"%01d", MatchConfig.getSetsPerMatch());
            String currentSet = txtCurrentSet.getText().toString();
            switch(currentSet){
                case "1": keys = "1"; rangeMax = "1"; break;
                case "2": keys = "1, 2"; rangeMax = "2"; break;
                case "3":  keys = "1,2,3"; rangeMax = "3"; break;
                default: break;
            }
            rangeMin = "1";
            k_title = "REQUEST_CURSET";
            //if(txtSetsPerMatch.getText().toString().equals("1")){ keys = "1"; rangeMax = "1";}
            if(MatchConfig.getSetsPerMatch() == 1){ keys = "1"; rangeMax = "1";}

            Keypad1 Teclado = Keypad1.newInstance(k_title, keys, rangeMin, rangeMax, currentSet);
            Teclado.show(getSupportFragmentManager(), "Current Set");
        });


        //----------------------------------------------------------------------
        btBack = (Button)findViewById(R.id.btSettingsBack);
        btBack.setOnClickListener(view -> {
            // calls the "ConfigActivity" passing score data in "Match" object
            Intent i = new Intent(getApplicationContext(), GameSettings.class);
            i.putExtra("MatchConfig", MatchConfig);
            setResult(RESULT_OK, i);

            // cleanup set results above "current set"
            //MatchConfig.UpdateSets();

            if(settings_changed) {
                // if settings changed, shows the "SAVE" dialog only
                AlertDialog.Builder builder = new AlertDialog.Builder(btBack.getContext());
                builder.setCancelable(true);
                builder.setTitle(R.string.DialoagSaveSettingsTitle);
                builder.setMessage(R.string.DialoagSaveSettings);
                builder.setPositiveButton(R.string.ButtonConfirm, (dialog, which) -> {


                    //--------------------------------------------------------------
                    // update values
                    MatchConfig.setAdvantage(swAdvantage.isChecked());
                    MatchConfig.setTiebreak(swTiebreak.isChecked());
                    MatchConfig.setMatchTiebreak(swMatchTiebreak.isChecked());
                    MatchConfig.setAlternateService(swAlternateService.isChecked());


                    String strValue = txtPointsPerGame.getText().toString();
                    int intValue = Integer.parseInt(strValue);
                    MatchConfig.setPointsPerGame(intValue);

                    strValue = txtGamesPerSet.getText().toString();
                    intValue = Integer.parseInt(strValue);
                    MatchConfig.setGamesPerSet(intValue);

                    strValue = txtSetsPerMatch.getText().toString();
                    intValue = Integer.parseInt(strValue);
                    MatchConfig.setSetsPerMatch(intValue);

                    strValue = txtMatchTiebreak.getText().toString();
                    intValue = Integer.parseInt(strValue);
                    MatchConfig.setMatchTiebreakPoints(intValue);

                    //--------------------------------------------------------------
                    // Get the shared preferences instance
                    SharedPreferences sharedPreferences = getSharedPreferences("PLT-01", Context.MODE_PRIVATE);
                    SharedPreferences.Editor settings = sharedPreferences.edit();
                    // Save the app settings
                    settings.putInt("Mode", MatchConfig.getMode());
                    settings.putInt("GamesPerSet", MatchConfig.getGamesPerSet());
                    settings.putInt("PointsPerGame", MatchConfig.getPointsPerGame());
                    settings.putInt("SetsPerMatch", MatchConfig.getSetsPerMatch());
                    settings.putInt("PointsMatchTie", MatchConfig.getMatchTiebreakPoints());
                    settings.putBoolean("Advantage", MatchConfig.isAdvantage());
                    settings.putBoolean("Tiebreak", MatchConfig.isTiebreak());
                    settings.putBoolean("TennisMode", MatchConfig.isMatchTiebreak());
                    settings.putBoolean("AlternateService", MatchConfig.isAlternateService());
                    // Commit the changes
                    settings.apply();
                    //--------------------------------------------------------------
                    Toast.makeText(getApplicationContext(), "Saving new settings...", Toast.LENGTH_LONG).show();
                    finish();
                });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> finish());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                if(MatchConfig.CheckMatch() != MatchConfig.PLAYER_ID_NONE) {
                    AlertDialog.Builder alerta = new AlertDialog.Builder(btBack.getContext());
                    alerta.setCancelable(true);
                    alerta.setTitle(R.string.DialoagEndMatchTitle);
                    alerta.setMessage(R.string.DialogIsMatchFinished);
                    alerta.setPositiveButton(R.string.ButtonConfirm, (dialog, which) -> {
                        finish();
                    });
                    alerta.setNegativeButton(R.string.ButtonDismiss, (dialog, which) -> {});
                    AlertDialog dialog = alerta.create();
                    alerta.show();
                } else { finish();}
            }
        });

        //----------------------------------------------------------------------
        // initial assert of the "server"
        // ATTENTION: DON'T MOVE THIS PIECE OF CODE
        if (MatchConfig.getCurrentServer() == MatchConfig.PLAYER_ID_1) {
            btP1Service.setImageDrawable(arrow_on);
        } else if (MatchConfig.getCurrentServer() == MatchConfig.PLAYER_ID_2) {
            btP2Service.setImageDrawable(arrow_on);
        }
        //----------------------------------------------------------------------

        ResetScore(txtCurrentSet.getText().toString());
    }

    //----------------------------------------------------------------------
    @Override
    public void onValueChanged(String title, String value) {
        switch(title){
            case "REQUEST_GAMES":
                if(!txtGamesPerSet.getText().toString().equals(value)) {
                    txtGamesPerSet.setText(value);
                    settings_changed = true;
                }
                break;
            case "REQUEST_POINTS":
                if(!txtPointsPerGame.getText().toString().equals(value)) {
                    txtPointsPerGame.setText(value);
                    settings_changed = true;
                }
                break;
            case "REQUEST_SETS":
                if(!txtSetsPerMatch.getText().toString().equals(value)) {
                    txtSetsPerMatch.setText(value);
                    CheckSetsPerMatch(value);
                    settings_changed = true;
                }
                break;

            case "REQUEST_MATCH_TIE":
                if(!txtMatchTiebreak.getText().toString().equals(value)) {
                    txtMatchTiebreak.setText(value);
                    settings_changed = true;
                }
                break;

            case "Player1":
                if(!txtPlayer1Points.getText().toString().equals(value)) {
                    txtPlayer1Points.setText(value);
                    MatchConfig.setPoints(MatchConfig.PLAYER_ID_1, value);
                    if(!MatchConfig.isTiebreaking()) {
                        if (txtPlayer1Points.getText().toString().contains("Ad")) {
                            txtPlayer2Points.setText("  ");
                            MatchConfig.setPoints(MatchConfig.PLAYER_ID_2, (byte) 5);
                        } else if(txtPlayer1Points.getText().toString().contains("  ")){
                            txtPlayer2Points.setText(R.string.Advantage);
                            MatchConfig.setPoints(MatchConfig.PLAYER_ID_2, (byte) 4);
                        }
                    }
                    score_changed = true;
                }
                break;
            case "Player1_Set1":
                if(!txtPlayer1Set1.getText().toString().equals(value)) {
                    txtPlayer1Set1.setText(value);
                    MatchConfig.setSet(MatchConfig.PLAYER_ID_1, MatchConfig.SET_INDEX_1, value);

                    //EXCEPTION
                    if(value.equals("7")){
                        txtPlayer2Set1.setText("6");
                        MatchConfig.setSet(MatchConfig.PLAYER_ID_2, MatchConfig.SET_INDEX_1, "6");
                    }

                    // if set1 has a winner, prepare set2
                    if(ValidateSet(MatchConfig.SET_INDEX_1)){ txtPlayer1Set2.setText("0"); txtPlayer2Set2.setText("0");}
                    ResetScore(txtCurrentSet.getText().toString());
                    score_changed = true;
                }
                break;
            case "Player1_Set2":
                if(!txtPlayer1Set2.getText().toString().equals(value)) {
                    txtPlayer1Set2.setText(value);
                    MatchConfig.setSet(MatchConfig.PLAYER_ID_1, MatchConfig.SET_INDEX_2, value);

                    //EXCEPTION
                    if(value.equals("7")){
                        txtPlayer2Set2.setText("6");
                        MatchConfig.setSet(MatchConfig.PLAYER_ID_2, MatchConfig.SET_INDEX_2, "6");
                    }
                    //MatchConfig.CheckSet(MatchConfig.SET_2);

                    if(ValidateSet(MatchConfig.SET_INDEX_2)){ txtPlayer1Set3.setText("0"); txtPlayer2Set3.setText("0");}
                    ResetScore(txtCurrentSet.getText().toString());
                    score_changed = true;
                }
                break;
            case "Player1_Set3":
                if(!txtPlayer1Set3.getText().toString().equals(value)) {
                    txtPlayer1Set3.setText(value);
                    MatchConfig.setSet(MatchConfig.PLAYER_ID_1, MatchConfig.SET_INDEX_3, value);

                    //EXCEPTION
                    if(value.equals("7")){
                        txtPlayer2Set3.setText("6");
                        MatchConfig.setSet(MatchConfig.PLAYER_ID_2, MatchConfig.SET_INDEX_3, "6");
                    }

                    ValidateSet(MatchConfig.SET_INDEX_3);
                    ResetScore(txtCurrentSet.getText().toString());
                    score_changed = true;
                }
                break;
            case "Player2":
                if(!txtPlayer2Points.getText().toString().equals(value)) {
                    txtPlayer2Points.setText(value);
                    MatchConfig.setPoints(MatchConfig.PLAYER_ID_2, value);
                    if(txtPlayer2Points.getText().toString().contains("Ad")){
                        txtPlayer1Points.setText("  ");
                        MatchConfig.setPoints(MatchConfig.PLAYER_ID_1, (byte)5);
                    } else if(txtPlayer2Points.getText().toString().contains("  ")) {
                        txtPlayer1Points.setText(R.string.Advantage);
                        MatchConfig.setPoints(MatchConfig.PLAYER_ID_1, (byte) 4);
                    }
                    score_changed = true;
                }
                break;
            case "Player2_Set1":
                if(!txtPlayer2Set1.getText().toString().equals(value)) {
                    txtPlayer2Set1.setText(value);
                    MatchConfig.setSet(MatchConfig.PLAYER_ID_2, MatchConfig.SET_INDEX_1, value);

                    //EXCEPTION
                    if(value.equals("7")){
                        txtPlayer1Set1.setText("6");
                        MatchConfig.setSet(MatchConfig.PLAYER_ID_1, MatchConfig.SET_INDEX_1, "6");
                    }

                    if(ValidateSet(MatchConfig.SET_INDEX_1)){ txtPlayer1Set2.setText("0"); txtPlayer2Set2.setText("0");}
                    ResetScore(txtCurrentSet.getText().toString());
                    score_changed = true;
                }
                break;
            case "Player2_Set2":
                if(!txtPlayer2Set2.getText().toString().equals(value)) {
                    txtPlayer2Set2.setText(value);
                    MatchConfig.setSet(MatchConfig.PLAYER_ID_2, MatchConfig.SET_INDEX_2, value);

                    //EXCEPTION
                    if(value.equals("7")){
                        txtPlayer1Set2.setText("6");
                        MatchConfig.setSet(MatchConfig.PLAYER_ID_1, MatchConfig.SET_INDEX_2, "6");
                    }

                    if(ValidateSet(MatchConfig.SET_INDEX_2)){ txtPlayer1Set3.setText("0"); txtPlayer2Set3.setText("0");}
                    ResetScore(txtCurrentSet.getText().toString());
                    score_changed = true;
                }
                break;
            case "Player2_Set3":
                if(!txtPlayer2Set3.getText().toString().equals(value)) {
                    txtPlayer2Set3.setText(value);
                    MatchConfig.setSet(MatchConfig.PLAYER_ID_2, MatchConfig.SET_INDEX_3, value);

                    //EXCEPTION
                    if(value.equals("7")){
                        txtPlayer1Set3.setText("6");
                        MatchConfig.setSet(MatchConfig.PLAYER_ID_1, MatchConfig.SET_INDEX_3, "6");
                    }

                    ValidateSet(MatchConfig.SET_INDEX_3);
                    ResetScore(txtCurrentSet.getText().toString());
                    score_changed = true;
                }
                break;
            case  "REQUEST_CURSET":
                if(!txtCurrentSet.getText().toString().equals(value)) {
                    txtCurrentSet.setText(value);
                    MatchConfig.setCurrentSet((byte) Integer.parseInt(value));
                    score_changed = true;
                    ResetScore(txtCurrentSet.getText().toString());
                }
                break;
        }
    }
    //----------------------------------------------------------------------
}

//==============================================================================
