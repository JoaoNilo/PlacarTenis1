//==================================================================================================
package com.edrosframework.placartenis1;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    int REQUEST_ENABLE_BT = 1;

    private ImageButton btPlayer1Up;
    private ImageButton btPlayer1Down;
    private ImageButton btPlayer2Up;
    private ImageButton btPlayer2Down;

    private EditText txtPlayer1Name;
    private EditText txtPlayer2Name;
    private TextView txtPlayer1Games;
    private TextView txtPlayer1Set1;
    private TextView txtPlayer2Games;
    private TextView txtPlayer2Set1;

    private ImageView imgPlayer1ServArrow;
    private ImageView imgPlayer2ServArrow;

    private KeyListener player1_keyListener;
    private KeyListener player2_keyListener;

    private int player1_games = 0;
    private int player1_set1;
    private int player2_games = 0;
    private int player2_set1;
    private String[] Scores = {"00", "15", "30", "40"};

    //---------------------------------------------------
    private Button btNew;
    private Button btStartTimer;
    private TextView txtTimerDigits;

    Timer timer;
    TimerTask timerTask;
    Double time = 0.0;
    boolean timerStarted = false;

    //---------------------------------------------------
    private ImageButton btConfig;
    BluetoothAdapter myBluetoothAdapter = null;


    /** ----------------------------------------------------------------------------------------
     * @brief change svisibility of service arrow flags
     */
    public void ServiceChange() {
        if (imgPlayer1ServArrow.getVisibility() == View.VISIBLE) {
            imgPlayer1ServArrow.setVisibility(View.INVISIBLE);
            imgPlayer2ServArrow.setVisibility(View.VISIBLE);
        } else {
            imgPlayer1ServArrow.setVisibility(View.VISIBLE);
            imgPlayer2ServArrow.setVisibility(View.INVISIBLE);
        }
    }

    /** ----------------------------------------------------------------------------------------
     * @brief format timer string
     */
    @SuppressLint("DefaultLocale")
    private String formatTime(int seconds, int minutes, int hours) {
        return (String.format("%02d", hours) + ":" + String.format("%02d", minutes));// + " : " + String.format("%02d",seconds));
    }

    /** ----------------------------------------------------------------------------------------
     * @brief get timer string
     */
    private String getTimerText() {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);

        return formatTime(seconds, minutes, hours);
    }

    /** ----------------------------------------------------------------------------------------
     * @brief start timer
     */
    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        txtTimerDigits.setText(getTimerText());
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0, 10);
    }

    /** ----------------------------------------------------------------------------------------
     * @brief event handler for btConfig->onClick
     */
    public void openConfigActivity() {
        // abre a tela de configuração passando os valores atuais
        Intent it = new Intent(this, ConfigActivity.class);
        startActivity(it);
    }

    /** ----------------------------------------------------------------------------------------
     * @brief event handler for btStartTimer
     */
    private void btStartTimer_OnCLick(View view) {
        if(!timerStarted) {
            timerStarted = true;
            btStartTimer.setText("Stop");
            //txtPlayer1Name.setKeyListener(null);
            //txtPlayer2Name.setKeyListener(null);
            //startTimer();
        } else {
            //timerTask.cancel();
            btStartTimer.setText("Start");
            timerStarted = false;
        }
    }

    /** ----------------------------------------------------------------------------------------
     * @brief Update score digits
     */
    private void updateScore() {
        getTimerText();
        txtTimerDigits.setText(getTimerText());
        txtPlayer1Games.setText(Scores[player1_games]);
        txtPlayer2Games.setText(Scores[player2_games]);
        txtPlayer1Set1.setText(String.valueOf(player1_set1));
        txtPlayer2Set1.setText(String.valueOf(player1_set1));
        imgPlayer1ServArrow.setVisibility(View.VISIBLE);
        imgPlayer2ServArrow.setVisibility(View.INVISIBLE);
        player1_keyListener = txtPlayer1Name.getKeyListener();
        player2_keyListener = txtPlayer2Name.getKeyListener();
    }

    /** ----------------------------------------------------------------------------------------
     * @brief Reset all variables for a new match
     */
    private void resetMatch(){
        player1_games = 0;
        player1_set1 = 0;
        player2_games = 0;
        player2_set1 = 0;
        time = 0.0;

        updateScore();

    }

    /** ----------------------------------------------------------------------------------------
     * @brief Activity entry point
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); // remove action bar (title bar)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

        txtPlayer1Name = (EditText) findViewById(R.id.txtPlayer1Name);
        player1_keyListener = txtPlayer1Name.getKeyListener();

        txtPlayer2Name = (EditText) findViewById(R.id.txtPlayer2Name);
        player2_keyListener = txtPlayer2Name.getKeyListener();

        txtPlayer1Games = (TextView) findViewById(R.id.txtPlayer1Games);
        txtPlayer2Games = (TextView) findViewById(R.id.txtPlayer2Games);
        txtPlayer1Set1 = (TextView) findViewById(R.id.txtPlayer1Set1);
        txtPlayer2Set1 = (TextView) findViewById(R.id.txtPlayer2Set1);

        imgPlayer1ServArrow = (ImageView) findViewById(R.id.imgPlayer1ServArrow);
        imgPlayer2ServArrow = (ImageView) findViewById(R.id.imgPlayer2ServArrow);
        imgPlayer2ServArrow.setVisibility(View.INVISIBLE);

        /**-----------------------------------------------------------------------------------------
         * @brief creates the config button (Bluetooth)
         * used to open the config screen
         */
        btConfig = (ImageButton) findViewById(R.id.btConfig);
        btConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openConfigActivity();
            }
        });

        /**-----------------------------------------------------------------------------------------
         * @brief Player 1 increase score
         */
        btPlayer1Up = (ImageButton) findViewById(R.id.btPlayer1Up);
        btPlayer1Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (++player1_games > 3) {
                    player1_games = 0;
                    player2_games = 0;
                    player1_set1++;
                    ServiceChange();
                }
                txtPlayer1Games.setText(Scores[player1_games]);
                txtPlayer2Games.setText(Scores[player2_games]);
                txtPlayer1Set1.setText(String.valueOf(player1_set1));
            }
        });

        /**-----------------------------------------------------------------------------------------
         * @brief Player 1 decrease score
         */
        btPlayer1Down = (ImageButton) findViewById(R.id.btPlayer1Down);
        btPlayer1Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player1_games > 0) {
                    player1_games--;
                }
                txtPlayer1Games.setText(Scores[player1_games]);
            }
        });

        /**-----------------------------------------------------------------------------------------
         * @brief Player 2 increase score
         */
        btPlayer2Up = (ImageButton) findViewById(R.id.btPlayer2Up);
        btPlayer2Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (++player2_games > 3) {
                    player1_games = 0;
                    player2_games = 0;
                    player2_set1++;
                    ServiceChange();
                }
                txtPlayer1Games.setText(Scores[player1_games]);
                txtPlayer2Games.setText(Scores[player2_games]);
                txtPlayer2Set1.setText(String.valueOf(player2_set1));
            }
        });

        /**-----------------------------------------------------------------------------------------
         * @brief Player 1 decrease score
         */
        btPlayer2Down = (ImageButton) findViewById(R.id.btPlayer2Down);
        btPlayer2Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player2_games > 0) {
                    player2_games--;
                }
                txtPlayer2Games.setText(Scores[player2_games]);
            }
        });

        /**-----------------------------------------------------------------------------------------
         * @brief check if Bluetooth adapter is available
         */
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (myBluetoothAdapter == null) {
            // adaptador bluetooth nao disponivel
            Toast.makeText(getApplicationContext(), "Bluetooth unavailable!", Toast.LENGTH_LONG).show();
        } else if (!myBluetoothAdapter.isEnabled()) {
            // adaptador bluetooth nao habilitado
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);


        } else{
            // adaptador bluetooth já habilitado
            Toast.makeText(getApplicationContext(), "Bluetooth already enable!", Toast.LENGTH_LONG).show();

        }

        //------------------------------------------------------------
        txtTimerDigits = (TextView) findViewById(R.id.txtTimerDigits);
        timer = new Timer();
        timerStarted = false;
        btStartTimer = (Button)findViewById(R.id.btStartTimer);
        btNew = (Button)findViewById(R.id.btNew);
        btStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!timerStarted) {
                    btNew.setVisibility(View.INVISIBLE);
                    timerStarted = true;
                    btStartTimer.setText("Stop");
                    txtPlayer1Name.setKeyListener(null);
                    txtPlayer2Name.setKeyListener(null);
                    startTimer();
                } else {
                    timerTask.cancel();
                    btStartTimer.setText("Start");
                    timerStarted = false;
                    btNew.setVisibility(View.VISIBLE);
                }
            }
        });

        //-------------------------------------------------------------
        btNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetMatch();
            }
        });
    }

}
//==================================================================================================
