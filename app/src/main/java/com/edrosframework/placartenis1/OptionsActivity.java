//==================================================================================================
package com.edrosframework.placartenis1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

public class OptionsActivity extends AppCompatActivity {

    //private AppBarConfiguration appBarConfiguration;
    Button btTennis;
    Button btBeachTennis;
    Button btTableTennis;
    Button btBeachVolley;
    Button btFootvolley;

    Scoreboard MatchConfig;
    Button btTweaks;
    EditText MessageBoard;
    ScoreParameters TmpRules;
    boolean mode_changed;

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        //Objects.requireNonNull(getSupportActionBar()).hide(); // remove action bar (title bar)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();
        MatchConfig = (Scoreboard) intent.getSerializableExtra("Match");
        TmpRules = new ScoreParameters();
        TmpRules.SetMode(MatchConfig.Rules.GetMode());
        mode_changed = false;

        MessageBoard = findViewById(R.id.em_message);

        //-------------------------------------------------------------
        // return to previous activity
        Button btCancel = findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(view -> {
            finish();
        });

        //-------------------------------------------------------------
        // return to previous activity
        Button btOK = findViewById(R.id.bt_ok);
        btOK.setOnClickListener(view -> {
            //--------------------------------------------------------------
            // check if mode has changed
            if(mode_changed) {
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
            }

            // calls the "ConfigActivity" passing score data in "Match" object
            Intent i = new Intent(getApplicationContext(), GameSettings.class);
            i.putExtra("MatchConfig", MatchConfig);
            setResult(RESULT_OK, i);
            finish();
        });

        //-------------------------------------------------------------
        // call GameSettings activity
        btTweaks = findViewById(R.id.bt_tweaks);
        btTweaks.setOnClickListener(view -> {
            // calls the "ConfigActivity" passing score data in "Match" object
            btTweaks.setEnabled(false);
            Intent i = new Intent(OptionsActivity.this, GameSettings.class);
            i.putExtra("Match", MatchConfig);
            startActivityForResult(i, 10);
        });

        //-------------------------------------------------------------
        // selects one of the options
        btTennis = findViewById(R.id.bt_tennis);
        btTennis.setOnClickListener(view -> {
            if(TmpRules.GetMode() != ScoreParameters.MODE_ID_TENNIS) {
                // selected option: Tennis
                TmpRules.SetMode(ScoreParameters.MODE_ID_TENNIS);
                MessageBoard.setText(TmpRules.ModeDescription());
                HighlightOption(TmpRules.GetMode());
                mode_changed = true;
            }
        });

        //-------------------------------------------------------------
        // selects one of the options
        btBeachTennis = findViewById(R.id.bt_beachtennis);
        btBeachTennis.setOnClickListener(view -> {
            if(TmpRules.GetMode() != ScoreParameters.MODE_ID_TENNIS) {
                // selected option: Tennis
                TmpRules.SetMode(ScoreParameters.MODE_ID_BEACHTENNIS);
                MessageBoard.setText(TmpRules.ModeDescription());
                HighlightOption(TmpRules.GetMode());
                mode_changed = true;
            }
        });

        //-------------------------------------------------------------
        // selects one of the options
        btTableTennis = findViewById(R.id.bt_tabletennis);
        btTableTennis.setOnClickListener(view -> {
            if(TmpRules.GetMode() != ScoreParameters.MODE_ID_TABLETENNIS) {
                // selected option: Tennis
                TmpRules.SetMode(ScoreParameters.MODE_ID_TABLETENNIS);
                MessageBoard.setText(TmpRules.ModeDescription());
                HighlightOption(TmpRules.GetMode());
                mode_changed = true;
            }
        });

        //-------------------------------------------------------------
        // selects one of the options
        btBeachVolley = findViewById(R.id.bt_beachvolleyball);
        btBeachVolley.setOnClickListener(view -> {
            if(TmpRules.GetMode() != ScoreParameters.MODE_ID_BEACHVOLLEY) {
                // selected option: Tennis
                TmpRules.SetMode(ScoreParameters.MODE_ID_BEACHVOLLEY);
                MessageBoard.setText(TmpRules.ModeDescription());
                HighlightOption(TmpRules.GetMode());
                mode_changed = true;
            }
        });

        //-------------------------------------------------------------
        // selects one of the options
        btFootvolley = findViewById(R.id.bt_footvolley);
        btFootvolley.setOnClickListener(view -> {
            if(TmpRules.GetMode() != ScoreParameters.MODE_ID_FOOTVOLLEY) {
                // selected option: Tennis
                TmpRules.SetMode(ScoreParameters.MODE_ID_FOOTVOLLEY);
                MessageBoard.setText(TmpRules.ModeDescription());
                HighlightOption(TmpRules.GetMode());
                mode_changed = true;
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    void HighlightOption(int m){
        btTennis.setTextColor(Color.BLACK);
        btBeachTennis.setTextColor(Color.BLACK);
        btTableTennis.setTextColor(Color.BLACK);
        btBeachVolley.setTextColor(Color.BLACK);
        btFootvolley.setTextColor(Color.BLACK);
        switch(m){
            case ScoreParameters.MODE_ID_TENNIS:
                btTennis.setTextColor(Color.BLUE);
                break;
            case ScoreParameters.MODE_ID_BEACHTENNIS:
                btBeachTennis.setTextColor(Color.BLUE);
                break;
            case ScoreParameters.MODE_ID_TABLETENNIS:
                btTableTennis.setTextColor(Color.BLUE);
                break;
            case ScoreParameters.MODE_ID_BEACHVOLLEY:
                btBeachVolley.setTextColor(Color.BLUE);
                break;
            case ScoreParameters.MODE_ID_FOOTVOLLEY:
                btFootvolley.setTextColor(Color.BLUE);
                break;
        }
    }

    //----------------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {

            //------------------------------------------------------------------
            // SETTINGS ACTIVITY: Check for settings or game da updates
            //------------------------------------------------------------------
            case 10:
                btTweaks.setEnabled(true);
                if (resultCode == RESULT_OK) {
                    //String result = data.getStringExtra("result");
                    if (data != null) {
                        Scoreboard MatchConfig = (Scoreboard) data.getSerializableExtra("MatchConfig");
                        MatchConfig.copy(MatchConfig);
                        MatchConfig.UpdatePoints(MatchConfig.PLAYER_ID_1);
                        MatchConfig.UpdatePoints(MatchConfig.PLAYER_ID_2);
                    }
                }
                break;
        }
    }
}
//==================================================================================================
