//==================================================================================================
package com.edrosframework.placartenis1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edrosframework.placartenis1.prosa.NDatagram;
import com.edrosframework.placartenis1.prosa.PROSA;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NTimerListener{

    public static final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_CONNECTION_BT = 2;


    private final int SCORE_PARAMS_SIZE = 14;
    private final int TIMER_ID_DEBOUNCING = 1;
    private final int TIMER_ID_GAMEOVER = 2;

    private boolean isDoubleClick = false;
    private static final long DOUBLE_CLICK_DELAY = 300; // Delay in milliseconds

    private int counter_tx;
    private int counter_rx;

    private EditText txtPlayer1Name;
    private TextView txtPlayer1Tens;
    private TextView txtPlayer1Units;
    private TextView txtPlayer1Set1;
    private TextView txtPlayer1Set2;
    private TextView txtPlayer1Set3;

    private EditText txtPlayer2Name;
    private TextView txtPlayer2Tens;
    private TextView txtPlayer2Units;
    private TextView txtPlayer2Set1;
    private TextView txtPlayer2Set2;
    private TextView txtPlayer2Set3;

    ImageButton btPlayer1Up;
    ImageButton btPlayer1Down;
    ImageButton btPlayer2Up;
    ImageButton btPlayer2Down;

    private ImageView imgPlayer1ServArrow;
    private ImageView imgPlayer2ServArrow;

    private TextView txtSet1;
    private TextView txtSet2;
    private TextView txtSet3;

    //---------------------------------------------------
    private Button btNew;
    private Button btStartTimer;
    private ImageButton btConfig;

    private TextView txtTimerDigits;
    private ImageView imgCharger;
    private ImageView imgBattery;
    Context context;

    Drawable arrow_off1;
    Drawable arrow_off2;
    Drawable arrow_on1;
    Drawable arrow_on2;

    ScoreHardware ScoreHealth;
    Scoreboard Match;

    //---------------------------------------------------
    // time base for Dispatch/Reload/Timeout
    Timer GameTimer;
    TimerTask timerTask;

    //---------------------------------------------------
    // timer for debouncing UP/DOWN keys
    NTimer Debouncing;
    NTimer GameOver;

    //---------------------------------------------------
    // time variables for the "Game Time" display
    Double time = 0.0;
    boolean timerStarted = false;
    boolean ticktack = false;

    //---------------------------------------------------
    // hardware parameters
    String charge = "Battery: ";// + String.format("%03.1f", ScoreHealth.getBatteryCharge()) + "%";
    String volts = "Voltage: "; // + String.format("%02.3f", ScoreHealth.getBatteryVoltage()) + "V";
    String status = "Status: "; // +  String.format("%04X", ScoreHealth.getNodeStatus()) + "h";

    //---------------------------------------------------
    // Bluetooth
    private static final String TAG = "MY_APP_DEBUG_TAG";
    BluetoothAdapter myBluetoothAdapter = null;
    BluetoothDevice myDevice = null;
    BluetoothSocket mySocket = null;
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    boolean connected = false;

    ConnectedThread myConnection;
    private TextView txtInMessage;
    private com.edrosframework.placartenis1.Packetizer InPacket = null;
    //private final NConverter Converter = new NConverter();

    //---------------------------------------------------
    // Prosa Protocol
    //---------------------------------------------------
    byte[] outBuffer = new byte[PROSA.SIZE_DATAGRAM];
    byte outBufferSize = 0;
    CommBuffer OutCommBuffer;
    public NDatagram InData;
    public NDatagram OutData;

    //---------------------------------------------------
    // Screen resolution parameters
    //---------------------------------------------------
    private float screenWidth;
    private float screenHeight;

    //----------------------------------------------------------------------------------------------
    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public interface MessageConstants {
        int MESSAGE_PACKET              = 3;
        int MSG_UPDATE_COUNTERS         = 0x0100;
        int MSG_UPDATE_GAMETIME         = 0x0101;
        int MSG_RESPONSE_GAMETIME       = 0x0102;
        int MSG_ALERT_DISCONNECTION     = 0x0103;
        int MSG_UPDATE_GAMEOVER         = 0x0105;
    }

    //----------------------------------------------------------------------------------------------
    // workarround
    @SuppressWarnings({"unchecked", "deprecation"})
    @Nullable
    public static <T extends Serializable> T getSerializable(@Nullable Bundle bundle, @Nullable String key, @NonNull Class<T> clazz) {
        if (bundle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return bundle.getSerializable(key, clazz);
            } else {
                try {
                    return (T) bundle.getSerializable(key);
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    //----------------------------------------------------------------------------------------------
    // sets the color of each display according to its arrow flags
    //@RequiresApi(api = Build.VERSION_CODES.M)
    private void UpdateDisplayStatus() {
        Resources resources = getResources();
        Drawable newImage;
        Drawable currentImage;
        ColorDrawable[] displayColor = new ColorDrawable[SCORE_PARAMS_SIZE];
        ColorDrawable ColorOff = new ColorDrawable(Color.GRAY);
        ColorDrawable ColorFault = new ColorDrawable(Color.RED);
        ColorDrawable ColorClk = new ColorDrawable(Color.rgb(0xE0, 0xE0, 0xE0));
        ColorDrawable ColorOn = new ColorDrawable(Color.rgb(0xFF, 0xFF, 0xFF));

        if(connected) {
            ColorClk = new ColorDrawable(Color.rgb(0x05, 0x6A, 0x05));
            ColorOn = new ColorDrawable(Color.rgb(0x5D, 0xFF, 0x63));
        }

        //------------------------------------------------------------------------------------------
        // initialize display color according to the "game status"
        int PARAM_SECONDS = 11;
        if(!Match.isGameOn()) {
            for(int c=0; c< SCORE_PARAMS_SIZE; c++){ displayColor[c] = ColorOff;}
        } else {
            for(int c=0; c< SCORE_PARAMS_SIZE; c++){ displayColor[c] = ColorOn;}
            displayColor[PARAM_SECONDS] = ColorClk;
        }

        //------------------------------------------------------------------------------------------
        // Player 1 Tens
        int PARAM_PLAY1_TENS = 0;
        if(!Match.isGameOn()){

            arrow_on1 = resources.getDrawable(R.drawable.arrow_gray_32x48, getTheme());
        } else {
            if(connected) {
                if (!ScoreHealth.Player1Tens) {

                    arrow_on1 = resources.getDrawable(R.drawable.arrow_red_32x48, getTheme());
                    displayColor[PARAM_PLAY1_TENS] = ColorFault;
                } else {
                    arrow_on1 = resources.getDrawable(R.drawable.arrow_green_32x48, getTheme());
                }
            } else {

                arrow_on1 = resources.getDrawable(R.drawable.arrow_white_32x48, getTheme());
            }
        }

        //------------------------------------------------------------------------------------------
        // Player 2 Tens
        int PARAM_PLAY2_TENS = 5;
        if(!Match.isGameOn()){
            arrow_on2 = resources.getDrawable(R.drawable.arrow_gray_32x48, getTheme());
        } else {
            if(connected) {
                if (!ScoreHealth.Player2Tens) {
                    arrow_on2 = resources.getDrawable(R.drawable.arrow_red_32x48, getTheme());
                    displayColor[PARAM_PLAY2_TENS] = ColorFault;
                } else {
                    arrow_on2 = resources.getDrawable(R.drawable.arrow_green_32x48, getTheme());
                }
            } else {
                arrow_on2 = resources.getDrawable(R.drawable.arrow_white_32x48, getTheme());
            }
        }

        SetService(Match.getCurrentServer());

        //------------------------------------------------------------------------------------------
        // if connected, check for faulty displays
        int PARAM_PLAY1_UNITS = 1;
        int PARAM_PLAY1_SET1 = 2;
        int PARAM_PLAY1_SET2 = 3;
        int PARAM_PLAY1_SET3 = 4;
        int PARAM_PLAY2_UNITS = 6;
        int PARAM_PLAY2_SET1 = 7;
        int PARAM_PLAY2_SET2 = 8;
        int PARAM_PLAY2_SET3 = 9;
        if(connected) {
            if (!ScoreHealth.Player1Units) { displayColor[PARAM_PLAY1_UNITS] = ColorFault;}
            if (!ScoreHealth.Player1Set1) { displayColor[PARAM_PLAY1_SET1] = ColorFault;}
            if (!ScoreHealth.Player1Set2) { displayColor[PARAM_PLAY1_SET2] = ColorFault;}
            if (!ScoreHealth.Player1Set3) { displayColor[PARAM_PLAY1_SET3] = ColorFault;}
            if (!ScoreHealth.Player2Units) { displayColor[PARAM_PLAY2_UNITS] = ColorFault;}
            if (!ScoreHealth.Player2Set1) { displayColor[PARAM_PLAY2_SET1] = ColorFault;}
            if (!ScoreHealth.Player2Set2) { displayColor[PARAM_PLAY2_SET2] = ColorFault;}
            if (!ScoreHealth.Player2Set3) { displayColor[PARAM_PLAY2_SET3] = ColorFault;}
        }

        //------------------------------------------------------------------------------------------
        // finally, assign each TextView its updated color
        txtPlayer1Tens.setTextColor(displayColor[PARAM_PLAY1_TENS].getColor());
        txtPlayer1Units.setTextColor(displayColor[PARAM_PLAY1_UNITS].getColor());
        txtPlayer1Set1.setTextColor(displayColor[PARAM_PLAY1_SET1].getColor());
        txtPlayer1Set2.setTextColor(displayColor[PARAM_PLAY1_SET2].getColor());
        txtPlayer1Set3.setTextColor(displayColor[PARAM_PLAY1_SET3].getColor());
        txtPlayer2Tens.setTextColor(displayColor[PARAM_PLAY2_TENS].getColor());
        txtPlayer2Units.setTextColor(displayColor[PARAM_PLAY2_UNITS].getColor());
        txtPlayer2Set1.setTextColor(displayColor[PARAM_PLAY2_SET1].getColor());
        txtPlayer2Set2.setTextColor(displayColor[PARAM_PLAY2_SET2].getColor());
        txtPlayer2Set3.setTextColor(displayColor[PARAM_PLAY2_SET3].getColor());
        txtTimerDigits.setTextColor(displayColor[PARAM_SECONDS].getColor());

        //------------------------------------------------------------------------------------------
        // additionally, update battery and charger icons
        if(connected){
            // update charge
            // update charger icon
            if(!ScoreHealth.Charging){
                // CHARGER unplugged, icon is INVISIBLE
                imgCharger.setVisibility(View.INVISIBLE);
            } else {
                currentImage = imgCharger.getDrawable().getCurrent();
                if (ScoreHealth.Charged) {
                    // blink the blue LED
                    if (ticktack) { newImage = resources.getDrawable(R.drawable.charged_on_32x32, getTheme());}
                    else { newImage = resources.getDrawable(R.drawable.charged_off_32x32, getTheme());}
                } else {
                    newImage = resources.getDrawable(R.drawable.charging_32x32, getTheme());
                }
                // check if image changed
                if(newImage != currentImage) { imgCharger.setImageDrawable(newImage);}
                // CHARGER unplugged, icon is VISIBLE
                imgCharger.setVisibility(View.VISIBLE);
            }

            // update battery
            currentImage = imgBattery.getDrawable().getCurrent();
            if(ScoreHealth.Charging){
                newImage = resources.getDrawable(R.drawable.battery_charging, getTheme());
            } else {
     	        int index = ScoreHealth.getBatteryChargeIndex();
                if (index == 0) { newImage = resources.getDrawable(R.drawable.battery_00_32x32, getTheme());
                } else if (index == 1) { newImage = resources.getDrawable(R.drawable.battery_25_32x32, getTheme());
                } else if (index == 2) { newImage = resources.getDrawable(R.drawable.battery_50_32x32, getTheme());
                } else if (index == 3) { newImage = resources.getDrawable(R.drawable.battery_75_32x32, getTheme());
                } else { newImage = resources.getDrawable(R.drawable.battery_100_32x32, getTheme());}
            }
            if (newImage != currentImage) { imgBattery.setImageDrawable(newImage);}
            // battery icon MUST be visible as long as the connection is on.
            imgBattery.setVisibility(View.VISIBLE);

            // update "status" line
            String result = "";
            if(!ScoreHealth.Charging){  result  = "Charge: " + charge + "   ";}
            result = result.concat("Voltage: " + volts);
            txtInMessage.setText(result);

        } else {
            // if connection is lost, hide all icons
            imgCharger.setVisibility(View.INVISIBLE);
            imgBattery.setVisibility(View.INVISIBLE);
            txtInMessage.setVisibility(View.INVISIBLE);
        }
    }

    //----------------------------------------------------------------------------------------------
    // sets visibility of service arrow flags
    public void SetService(byte server) {

        if (server == 2) {
            imgPlayer1ServArrow.setImageDrawable(arrow_off1);
            imgPlayer2ServArrow.setImageDrawable(arrow_on2);
        } else if(server == 1){
            imgPlayer1ServArrow.setImageDrawable(arrow_on1);
            imgPlayer2ServArrow.setImageDrawable(arrow_off2);
        } else {
            imgPlayer1ServArrow.setImageDrawable(arrow_off1);
            imgPlayer2ServArrow.setImageDrawable(arrow_off2);
        }
    }

    //----------------------------------------------------------------------------------------------
    // assigns appropriate colors to SET1, SET2, SET3 according to "current set"
    public void SetCurrentSet(byte set) {
        if (set == 1) {
            txtSet1.setTextColor(Color.rgb(255,255,255));
            txtSet2.setTextColor(Color.rgb(120,120,120));
            txtSet3.setTextColor(Color.rgb(120,120,120));
        } else if (set == 2) {
            txtSet2.setTextColor(Color.rgb(255,255,255));
            txtSet1.setTextColor(Color.rgb(120,120,120));
            txtSet3.setTextColor(Color.rgb(120,120,120));
        } else if (set == 3) {
            txtSet3.setTextColor(Color.rgb(255, 255, 255));
            txtSet1.setTextColor(Color.rgb(120, 120, 120));
            txtSet2.setTextColor(Color.rgb(120, 120, 120));
        } else {
            txtSet1.setTextColor(Color.rgb(120,120,120));
            txtSet2.setTextColor(Color.rgb(120,120,120));
            txtSet3.setTextColor(Color.rgb(120,120,120));
        }
    }

    //----------------------------------------------------------------------------------------------
    private void RefreshScreen() {
        txtPlayer1Tens.setText(Match.getTens(Match.PLAYER_ID_1));
        txtPlayer1Units.setText(Match.getUnits(Match.PLAYER_ID_1));
        txtPlayer1Set1.setText(Match.getSet1(Match.PLAYER_ID_1));
        txtPlayer1Set2.setText(Match.getSet2(Match.PLAYER_ID_1));
        txtPlayer1Set3.setText(Match.getSet3(Match.PLAYER_ID_1));

        txtPlayer2Tens.setText(Match.getTens(Match.PLAYER_ID_2));
        txtPlayer2Units.setText(Match.getUnits(Match.PLAYER_ID_2));
        txtPlayer2Set1.setText(Match.getSet1(Match.PLAYER_ID_2));
        txtPlayer2Set2.setText(Match.getSet2(Match.PLAYER_ID_2));
        txtPlayer2Set3.setText(Match.getSet3(Match.PLAYER_ID_2));

        SetService(Match.getCurrentServer());
        SetCurrentSet(Match.getCurrentSet());
    }

    //----------------------------------------------------------------------------------------------
    // sets the game control components enabled
    public void UpDownEnabled(boolean game_on) {
        Resources resources = getResources();
        Drawable UpArrow;
        Drawable DownArrow;

        // enabled when game is on
        btPlayer1Up.setEnabled(true);
        btPlayer1Down.setEnabled(true);
        btPlayer2Up.setEnabled(true);
        btPlayer2Down.setEnabled(true);

        if(game_on) {
            UpArrow = resources.getDrawable(R.drawable.arrow_up_48x32_on, getTheme());
            DownArrow = resources.getDrawable(R.drawable.arrow_down_48x32_on, getTheme());
        } else{
            UpArrow = resources.getDrawable(R.drawable.arrow_up_48x32_off, getTheme());
            DownArrow = resources.getDrawable(R.drawable.arrow_down_48x32_off, getTheme());
        }
        btPlayer1Up.setImageDrawable(UpArrow);
        btPlayer2Up.setImageDrawable(UpArrow);
        btPlayer1Down.setImageDrawable(DownArrow);
        btPlayer2Down.setImageDrawable(DownArrow);

        // enabled when game is on
        btPlayer1Up.setEnabled(game_on);
        btPlayer1Down.setEnabled(game_on);
        btPlayer2Up.setEnabled(game_on);
        btPlayer2Down.setEnabled(game_on);
    }

    //----------------------------------------------------------------------------------------------
    // UNIFIED NTimer handler
    //----------------------------------------------------------------------------------------------
    @Override
    public void onTimeout(Object sender){
        if(sender != null){
            if(sender.getClass() == NTimer.class) {
                NTimer timer = (NTimer) sender;
                // UP/DOWN keys debouncing
                if (timer.getIndex() == TIMER_ID_DEBOUNCING) {
                    // only called when game is on
                    UpDownEnabled(true);
                }

                if (timer.getIndex() == TIMER_ID_GAMEOVER) {
                    // called when game has finished
                    // THE WINNER IS...
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MSG_UPDATE_GAMEOVER, 0, 0, null);
                    readMsg.sendToTarget();
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    private void  UpdateBluetoothIcon(){
        Resources resources = getResources();
        Drawable newImage;
        if(connected) {
            newImage = resources.getDrawable(R.drawable.btn_border_on, getTheme());
        } else {
            newImage = resources.getDrawable(R.drawable.btn_border_off, getTheme());
        }
        ImageButton btButton = findViewById(R.id.btn_connect);
        btButton.setBackground(newImage);
    }

    //----------------------------------------------------------------------------------------------
    private void  SetPlayerNames(boolean is_game_on){
        ColorDrawable NameOn = new ColorDrawable(Color.argb(0xFF,0xFF,0x88, 0x00));    // orange
        ColorDrawable NameOff = new ColorDrawable(Color.argb(0xFF,0xC0,0xC0, 0xC0));   // gray

        if(!is_game_on){
            //enabled when game is off
            txtPlayer1Name.setEnabled(true);
            txtPlayer2Name.setEnabled(true);
            txtPlayer1Name.setTextColor(NameOn.getColor());
            txtPlayer2Name.setTextColor(NameOn.getColor());
        } else {
            //enabled when game is on
            txtPlayer1Name.setEnabled(false);
            txtPlayer2Name.setEnabled(false);
            txtPlayer1Name.setTextColor(NameOff.getColor());
            txtPlayer2Name.setTextColor(NameOff.getColor());
        }
    }

    //----------------------------------------------------------------------------------------------
    // starts the "GameTime" timer
    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                //runOnUiThread(new Runnable() {
                runOnUiThread(() -> {
                    if(timerStarted){ time++;}
                    // sends an update request to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MSG_UPDATE_GAMETIME, 0, 0, null);
                    readMsg.sendToTarget();

                });
            }

        };
        GameTimer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    //----------------------------------------------------------------------------------------------
    // resets all variables for a new match
    private void resetMatch(){
        time = 0.0;
        timerStarted = false;
        //if(timerTask != null){ timerTask.cancel();}
        txtPlayer1Name.setText(R.string.Player1Name);
        txtPlayer2Name.setText(R.string.Player2Name);
        btNew.setVisibility(View.VISIBLE);
        btStartTimer.setText(R.string.StartTimer);
        btStartTimer.setEnabled(true);
        SharedPreferences appSettings = getSharedPreferences("PLT-01", Context.MODE_PRIVATE);
        // Retrieve the app settings
        Match.setGamesPerSet(appSettings.getInt("GamesPerSet", 6));
        Match.setPointsPerGame(appSettings.getInt("PointsPerGame", 7));
        Match.setSetsPerMatch(appSettings.getInt("SetsPerMatch", 3));
        Match.setMatchTiebreakPoints(appSettings.getInt("PointsMatchTie", 10));

        Match.setAdvantage(appSettings.getBoolean("Advantage", true));
        Match.setTiebreak(appSettings.getBoolean("Tiebreak", true));
        Match.setMatchTiebreak(appSettings.getBoolean("TennisMode", true));
        Match.setAlternateService(appSettings.getBoolean("AlternateService", true));

        Match.Restart();
        Match.UpdatePoints(Match.PLAYER_ID_1);
        Match.UpdatePoints(Match.PLAYER_ID_2);
        RefreshScreen();
    }

    //----------------------------------------------------------------------------------------------
    // MainActivity entry point
    //----------------------------------------------------------------------------------------------
    @SuppressLint({"MissingPermission", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Objects.requireNonNull(getSupportActionBar()).hide(); // remove action bar (title bar)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        context = getApplicationContext();

        //----------------------------------------------------------------
        // get the screen resolution
        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;*/
        //----------------------------------------------------------------

        Resources graphic_resources = getResources();
        arrow_off1 = graphic_resources.getDrawable(R.drawable.arrow_darkgray_32x48, getTheme());
        arrow_off2 = graphic_resources.getDrawable(R.drawable.arrow_darkgray_32x48, getTheme());


        txtPlayer1Name = findViewById(R.id.txtPlayer1Name);
        txtPlayer2Name = findViewById(R.id.txtPlayer2Name);

        txtPlayer1Tens = findViewById(R.id.txtPlayer1Tens);
        txtPlayer1Units = findViewById(R.id.txtPlayer1Units);
        txtPlayer2Tens = findViewById(R.id.txtPlayer2Tens);
        txtPlayer2Units = findViewById(R.id.txtPlayer2Units);
        txtPlayer1Set1 = findViewById(R.id.txtPlayer1Set1);
        txtPlayer2Set1 = findViewById(R.id.txtPlayer2Set1);
        txtPlayer1Set2 = findViewById(R.id.txtPlayer1Set2);
        txtPlayer2Set2 = findViewById(R.id.txtPlayer2Set2);
        txtPlayer1Set3 = findViewById(R.id.txtPlayer1Set3);
        txtPlayer2Set3 = findViewById(R.id.txtPlayer2Set3);
        txtSet1 = findViewById(R.id.txtSet1);
        txtSet2 = findViewById(R.id.txtSet2);
        txtSet3 = findViewById(R.id.txtSet3);

        imgPlayer1ServArrow = findViewById(R.id.imgPlayer1ServArrow);
        imgPlayer2ServArrow = findViewById(R.id.imgPlayer2ServArrow);

        //-------------------------------------------------------------
        txtInMessage = findViewById(R.id.txtInMessage);
        txtInMessage.setVisibility(View.INVISIBLE);


        imgCharger = findViewById(R.id.imgCharger);
        imgBattery = findViewById(R.id.imgBattery);
        imgBattery.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            //private final Handler handler = new Handler();
            @Override
            public void onClick(View view) {
                clickCount++;
                if (clickCount == 1) {
                    handler.postDelayed(() -> {
                        if (!isDoubleClick) {
                            // Single-click action
                            txtInMessage.setVisibility(View.INVISIBLE);
                        }
                        isDoubleClick = false;
                        clickCount = 0;
                    }, DOUBLE_CLICK_DELAY);
                } else if (clickCount == 2) {
                    isDoubleClick = true;
                    // Double-click action
                    if(connected){ txtInMessage.setVisibility(View.VISIBLE);}
                    clickCount = 0;
                }
            }
        });

        counter_tx = 0; counter_rx = 0;
        OutCommBuffer = new CommBuffer(SCORE_PARAMS_SIZE);
        ScoreHealth = new ScoreHardware();

        //---------------------------------------------------------------
        Match = new Scoreboard();
        //Match.setEventListener(new Scoreboard.MyEventListener() {
        Match.setEventListener(() -> {
            if(Match.getWinner() == 0) {
                if (Match.isServerChange()) {
                    SetService(Match.getCurrentServer());
                }
                if (Match.isCourtChange()) {
                    Toast.makeText(getApplicationContext(), "SWITCH COURT ENDS!", Toast.LENGTH_SHORT).show();
                }
            } else {
                RefreshScreen();
                GameOver.startTimer(500);
            }
        });

        //--------------------------------------------------------------
        // LOAD "settings"
        // Get the shared preferences instance
        SharedPreferences appSettings = getSharedPreferences("PLT-01", Context.MODE_PRIVATE);
        // Retrieve the app settings
        Match.setGamesPerSet(appSettings.getInt("GamesPerSet", 6));
        Match.setPointsPerGame(appSettings.getInt("PointsPerGame", 7));
        Match.setSetsPerMatch(appSettings.getInt("SetsPerMatch", 3));
        Match.setMatchTiebreakPoints(appSettings.getInt("PointsMatchTie", 10));

        Match.setAdvantage(appSettings.getBoolean("Advantage", true));
        Match.setTiebreak(appSettings.getBoolean("Tiebreak", true));
        Match.setMatchTiebreak(appSettings.getBoolean("TennisMode", true));
        Match.setAlternateService(appSettings.getBoolean("AlternateService", true));

        Debouncing = new NTimer(this, TIMER_ID_DEBOUNCING);
        GameOver = new NTimer(this, TIMER_ID_GAMEOVER);


        //-------------------------------------------------------------
        // Player 1 increase score
        btPlayer1Up = findViewById(R.id.btPlayer1Up);
        btPlayer1Up.setOnClickListener(view -> {
            // debounce key first
            UpDownEnabled(false);
            Debouncing.startTimer(1000);
            // do the job
            Match.ScoreIncrement(1);
            RefreshScreen();
        });

        //-------------------------------------------------------------
        // Player 1 decrease score
        btPlayer1Down = findViewById(R.id.btPlayer1Down);
        btPlayer1Down.setOnClickListener(view -> {
            // debounce key first
            UpDownEnabled(false);
            Debouncing.startTimer(1000);
            // do the job
            Match.ScoreDecrement(1);
            RefreshScreen();
        });

        //-------------------------------------------------------------
        // Player 2 increase score
        btPlayer2Up = findViewById(R.id.btPlayer2Up);
        btPlayer2Up.setOnClickListener(view -> {
            // debounce key first
            UpDownEnabled(false);
            Debouncing.startTimer(1000);
            // do the job
            Match.ScoreIncrement(2);
            RefreshScreen();
        });

        //-------------------------------------------------------------
        // Player 2 decrease score
        btPlayer2Down = findViewById(R.id.btPlayer2Down);
        btPlayer2Down.setOnClickListener(view -> {
            // debounce key first
            UpDownEnabled(false);
            Debouncing.startTimer(1000);
            // do the job
            Match.ScoreDecrement(2);
            RefreshScreen();
        });

        //-------------------------------------------------------------
        // creates the B button used to scan bluetooth devices
        //---------------------------------------------------
        ImageButton btConnect = findViewById(R.id.btn_connect);
        btConnect.setOnClickListener(view -> {
            if(connected){
                // disconnect
                try {
                    mySocket.close();
                    connected = false;
                    Toast.makeText(getApplicationContext(), "Bluetooth disconnected!", Toast.LENGTH_LONG).show();
                } catch (IOException erro) {
                    Toast.makeText(getApplicationContext(), "Bluetooth error:" + erro, Toast.LENGTH_LONG).show();
                }
            } else {
                // connect
                Intent abreLista = new Intent(MainActivity.this, ListDevices_Activity.class);
                startActivityForResult(abreLista, REQUEST_CONNECTION_BT);
            }
            UpdateBluetoothIcon();
        });

        //-------------------------------------------------------------
        btNew = findViewById(R.id.btNew);
        btNew.setOnClickListener(view -> {

            // if NEW is clicked, shows the "NEW MATCH" dialog only
            AlertDialog.Builder builder = new AlertDialog.Builder(btNew.getContext());
            builder.setCancelable(true);
            builder.setTitle(R.string.DialoagNewMatchTitle);
            builder.setMessage(R.string.DialogNewMatch);
            //----------------------------------------------------------------------------------
            // OK button, start a new match
            builder.setPositiveButton(R.string.ButtonConfirm, (dialog, which) -> {
                //--------------------------------------------------------------
                // update values
                resetMatch();
                UpdateDisplayStatus();
                txtTimerDigits.setText(Match.getTimerText(time));
                SetPlayerNames(Match.isGameOn());
                UpDownEnabled(Match.isGameOn());
                btStartTimer.setVisibility(View.VISIBLE);
            });

            //----------------------------------------------------------------------------------
            // CANCEL button, do nothing
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> finish());

            AlertDialog dialog = builder.create();
            dialog.show();
        });

        //------------------------------------------------------------
        //-------------------------------------------------------------
        btStartTimer = findViewById(R.id.btStartTimer);
        btStartTimer.setOnClickListener(view -> {
            if(!timerStarted) {
                btNew.setVisibility(View.INVISIBLE);
                timerStarted = true;
                btStartTimer.setText(R.string.Pause);
                Match.Start();
                //enabled when game is off
                SetPlayerNames(Match.isGameOn());
                UpDownEnabled(Match.isGameOn());
            } else {
                timerTask.cancel();
                btStartTimer.setText(R.string.Start);
                timerStarted = false;
                btNew.setVisibility(View.VISIBLE);
            }
        });
        //------------------------------------------------------------
        txtTimerDigits = findViewById(R.id.txtTimerDigits);
        GameTimer = new Timer();
        timerStarted = false;
        //------------------------------------------------------------

        //-------------------------------------------------------------
        btConfig = findViewById(R.id.btn_config);
        //findViewById(R.id.btn_config).setOnClickListener(this);
        btConfig.setOnClickListener(view -> {
            // calls the "ConfigActivity" passing score data in "Match" object
            btConfig.setVisibility(View.INVISIBLE);

            Intent i;
            if(Match.isGameOn()) {
                i = new Intent(MainActivity.this, GameSettings.class);
            } else {
                i = new Intent(MainActivity.this, OptionsActivity.class);
            }

            i.putExtra("Match", Match);
            startActivityForResult(i, 10);
        });

        //------------------------------------------------------------
        //-------------------------------------------------------------
        // check if Bluetooth adapter is available and enabled
        //myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        BluetoothManager btManager;
        btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = btManager.getAdapter();

        if (myBluetoothAdapter == null) {
            // bluetooth adapter not available
            Toast.makeText(getApplicationContext(), "Bluetooth unavailable!", Toast.LENGTH_LONG).show();
        } else if (!myBluetoothAdapter.isEnabled()) {
            // bluetooth not enabled
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            // bluetooth already enabled
            Toast.makeText(getApplicationContext(), "Bluetooth already enable!", Toast.LENGTH_LONG).show();
        }

        //-------------------------------------------------------------
        txtInMessage = findViewById(R.id.txtInMessage);

        //-------------------------------------------------------------
        // Bluetooth serial data auxiliary objects
        InPacket = new Packetizer(handler);
        //Dispatcher outPacket = new Dispatcher(handler);

        //-------------------------------------------------------------
        // Prosa protocol: main classes and dependencies
        InData = new NDatagram();
        OutData = new NDatagram();
        OutData.setDestination(PROSA.ADDR_DEVICE1);
        OutData.setSource(PROSA.ADDR_IHM1);
        OutData.setCommand(PROSA.CMD_SCORE_UPDATE);

        //-------------------------------------------------------------
        // Register a "MESSAGE FILTER" to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "NDATALINK_PUT".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("NDATALINK_PUT"));

        //-------------------------------------------------------------
        resetMatch();
        startTimer();

        //enabled when game is off
        SetPlayerNames(Match.isGameOn());
        UpDownEnabled(false);

        //-------------------------------------------------------------
        // ============ REMOVER =================
        //String resolution = "Width = " + String.format(Locale.getDefault(),"%f", screenWidth) + "  ";
        //resolution = resolution.concat("Height = " + String.format(Locale.getDefault(), "%f", screenHeight));
        //-------------------------------------------------------------

    }

    //----------------------------------------------------------------------------------------------
    // ACTIVITY EVENT HANDLERS
    //----------------------------------------------------------------------------------------------
    // MainActivity: finishes the application
    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    //----------------------------------------------------------------------------------------------
    // MainActivity: Handle the MainActivity's onClick event
    @Override
    public void onClick(View v) {}

    //----------------------------------------------------------------------------------------------
    // MainActivity: Handle the MainActivity's onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean result = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // code to execute when finger touches the screen
                txtPlayer1Name.clearFocus(); txtPlayer2Name.clearFocus();
                result = true;
                break;
            case MotionEvent.ACTION_MOVE:
                // code to execute when finger moves on the screen
                break;
            case MotionEvent.ACTION_UP:
                // code to execute when finger releases the screen
                break;
            default:
                break;
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    // This functions is used to receive "BROADCAST MESSAGES". In this application they are being
    // used among the various threads withing the same activity.
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            //if(connected){ DLink.Put(tmpData);}
        }
    };

    //----------------------------------------------------------------------------------------------
    // MainActivity: get messages from the message queue and update screen accordingly
    private final Handler handler = new Handler(Looper.getMainLooper()){
        //@RequiresApi(api = Build.VERSION_CODES.M)
        @SuppressLint({"HandlerLeak", "DefaultLocale"})
        @Override
        public void handleMessage(Message msg) {

            if(msg.what == MessageConstants.MSG_UPDATE_GAMEOVER){
                // disable point control
                UpDownEnabled(false);
                btStartTimer.setEnabled(false);

                // announces the WINNER of the current match
                AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle(R.string.DialogGameOverTitle);
                String player = "PLAYER ?: NO NAME";
                if(Match.getWinner() == Match.PLAYER_ID_1){
                    player = "Player 1: " + txtPlayer1Name.getText();
                } else if(Match.getWinner() == Match.PLAYER_ID_2) {
                    player = "Player 2: " + txtPlayer2Name.getText();
                }
                String FinalMessage = String.format(Locale.getDefault(), "The winner is %s!", player);
                //builder.setMessage(R.string.DialogGameOver);
                builder.setMessage(FinalMessage);
                builder.setPositiveButton(R.string.ButtonGameOverConfirm, (dialog, which) -> {
                    //--------------------------------------------------------------
                    // update values
                    resetMatch();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        UpdateDisplayStatus();
                    }
                    txtTimerDigits.setText(Match.getTimerText(time));
                    UpDownEnabled(Match.isGameOn());
                    btStartTimer.setVisibility(View.INVISIBLE);
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            } else if(msg.what == MessageConstants.MSG_UPDATE_COUNTERS){
                String Tx = "Tx: " + String.format("%05d", counter_tx);
                String Rx = "Rx: " + String.format("%05d", counter_rx);
                String Cont = Tx + "  " + Rx;
                txtInMessage.setText(Cont);

            } else if(msg.what == MessageConstants.MSG_UPDATE_GAMETIME) {
                //---------------------------------------------------------
                // Process messages to be sent through the bluetooth interface
                //---------------------------------------------------------
                ticktack  = !ticktack;
                txtTimerDigits.setText(Match.getTimerText(time));
                OutData.Flush();
                Match.Export(OutCommBuffer);
                OutData.Append(OutCommBuffer);
                outBufferSize  = (byte)OutData.Export(outBuffer);
                if(connected){ myConnection.write(Arrays.copyOf(outBuffer, outBufferSize));}

                //----------------------------------------------------------
                //UPDATE DISPLAYS
                UpdateDisplayStatus();
                //----------------------------------------------------------

            } else if(msg.what == MessageConstants.MSG_RESPONSE_GAMETIME) {
                //---------------------------------------------------------
                // Process incoming data from bluetooth interface
                //---------------------------------------------------------
                InData.CopyFrom((NDatagram)msg.obj);
                if(InData.Validate()){
                    if(InData.getCommand( )== PROSA.CMD_SCORE_UPDATE) {
                        if (InData.getLength() >= 7) {
                            //---------------------------------------------
                            byte[] indata_params = new byte[InData.getLength()];
                            InData.Extract(indata_params, InData.getLength());
                            ScoreHealth.Update(indata_params);
                            charge = String.format("%03.1f", ScoreHealth.getBatteryCharge()) + "%";
                            volts = String.format("%02.3f", ScoreHealth.getBatteryVoltage()) + "V";
                            status = String.format("%04X", ScoreHealth.getNodeStatus()) + "h";
                            //---------------------------------------------
                        }
                    }
                }
                //---------------------------------------------------------
            } else if(msg.what == MessageConstants.MSG_ALERT_DISCONNECTION) {
                 //---------------------------------------------------------
                 // Process bluetooth DISCONNECTION message
                 //---------------------------------------------------------
                 connected = false;
                 //----------------------------------------------------------
                 //UPDATE BLUETOOTH BUTTON
                 UpdateBluetoothIcon();
                 //----------------------------------------------------------
                 //UPDATE DISPLAYS
                 UpdateDisplayStatus();
                 //----------------------------------------------------------
             }
        }
    };

    //----------------------------------------------------------------------------------------------
    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){

            //------------------------------------------------------------------
            // SETTINGS ACTIVITY: Check for settings or game da updates
            //------------------------------------------------------------------
            case 10:
                btConfig.setVisibility(View.VISIBLE);
                if(resultCode == RESULT_OK) {
                    //String result = data.getStringExtra("result");
                    if(data != null) {
                        Scoreboard MatchConfig = (Scoreboard) data.getSerializableExtra("MatchConfig");
                        Match.copy(MatchConfig);
                        Match.UpdatePoints(Match.PLAYER_ID_1);
                        Match.UpdatePoints(Match.PLAYER_ID_2);
                        RefreshScreen();
                        if(Match.CheckMatch() != Match.PLAYER_ID_NONE){
                            GameOver.startTimer(500);
                        }
                    }
                }
                break;

            //------------------------------------------------------------------
            // BLUETOOTH CLASSIC: Manage the bluetooth connection process
            //------------------------------------------------------------------
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK){
                    // bluetooth adapter enabled
                    Toast.makeText(getApplicationContext(), "Bluetooth enable!", Toast.LENGTH_LONG).show();
                } else {
                    // bluetooth adapter not enabled
                    Toast.makeText(getApplicationContext(), "Bluetooth was NOT enable! \nPLT-01 scoreboard cannot be used!", Toast.LENGTH_LONG).show();
                    //finish();
                }
                break;

            case REQUEST_CONNECTION_BT:
                if(resultCode==Activity.RESULT_OK){
                    try {
                        assert data != null;
                        String MAC = data.getExtras().getString(ListDevices_Activity.MAC_ADDRESS);
                        myDevice = myBluetoothAdapter.getRemoteDevice(MAC);
                        try{
                            mySocket = myDevice.createRfcommSocketToServiceRecord(MY_UUID);
                            mySocket.connect();
                            myConnection = new ConnectedThread(mySocket);
                            myConnection.start();
                            connected = true;
                            Toast.makeText(getApplicationContext(), "Connected to: " + MAC, Toast.LENGTH_LONG).show();
                            UpdateBluetoothIcon();
                        } catch(IOException error) {
                            if(connected){ myConnection.cancel();}
                            connected = false;
                            Toast.makeText(getApplicationContext(), "Failed to connect to: " + MAC, Toast.LENGTH_LONG).show();
                            UpdateBluetoothIcon();
                        }
                    } catch (NullPointerException ignored){}
                } else {
                    // bluetooth device not selected
                    Toast.makeText(getApplicationContext(), "Bluetooth DEVICE was NOT selected for connection!", Toast.LENGTH_LONG).show();
                }
            break;
        }
    }

    //----------------------------------------------------------------------------------------------
    private class ConnectedThread extends Thread {

        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        int idle_timeout = 0;
        byte[] ibuffer = new byte[1024];
        int LocalBufferSize;
        NDatagram tmpData = new NDatagram();

        private final TimerTask Task1 = new TimerTask() {
            @Override
            public void run() {
                //if(idle_timeout > 0){ idle_timeout--;}
                //else {
                    // this section "emulates" the serial IDLE LINE event (end of packet)
                    if(InPacket.getSize()>0) {
                        // packs the new chunk of data from InputStream into ibuffer
                        LocalBufferSize = InPacket.get(ibuffer);
                        // store data in a PROSA protocol datagram
                        tmpData.Import(ibuffer);
                        // send the incoming datagram to the UI handler.
                        if(tmpData.IsValid()) {
                            Message readMsg = handler.obtainMessage(
                                    MessageConstants.MSG_RESPONSE_GAMETIME, 0, 0, tmpData);
                            readMsg.sendToTarget();
                        }
                        // resets the packetizer for new incoming data
                        InPacket.flush();
                    }
                //}
            }
        };

        private final Timer T1= new Timer();

        //------------------------------------------------------------------------------------------
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //------------------------------------------------------------------------------------------
        public void run() {
            T1.schedule(Task1, 0,15);

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    LocalBufferSize = mmInStream.read(ibuffer);
                    if(LocalBufferSize > 0) {
                        idle_timeout = 0;
                        InPacket.put(ibuffer, LocalBufferSize);
                    }
                } catch (IOException e) {
                    Message alertMsg = handler.obtainMessage(
                            MessageConstants.MSG_ALERT_DISCONNECTION, 0, 0, null);
                    alertMsg.sendToTarget();
                    break;
                }
            }
        }

        //------------------------------------------------------------------------------------------
        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        //------------------------------------------------------------------------------------------
        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mySocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}

//==================================================================================================
