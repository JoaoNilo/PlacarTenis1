//==================================================================================================
package com.edrosframework.placartenis1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import androidx.navigation.ui.AppBarConfiguration;

public class OptionsActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    Scoreboard MatchConfig;
    Button btTweaks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        //Objects.requireNonNull(getSupportActionBar()).hide(); // remove action bar (title bar)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();
        MatchConfig = (Scoreboard) intent.getSerializableExtra("Match");

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

    /*@Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_options);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }*/
}
//==================================================================================================
