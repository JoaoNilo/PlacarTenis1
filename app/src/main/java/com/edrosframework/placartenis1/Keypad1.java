//==================================================================================================
package com.edrosframework.placartenis1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

//--------------------------------------------------------------------------------------------------
public class Keypad1 extends DialogFragment implements View.OnClickListener, View.OnTouchListener{
    /*public static final int REQUEST_GAMES   = 1;
    public static final int REQUEST_SETS    = 2;
    public static final int REQUEST_POINTS  = 3;

    public static final int REQUEST_P1PTS   = 4;
    public static final int REQUEST_P1SET1  = 5;
    public static final int REQUEST_P1SET2  = 6;
    public static final int REQUEST_P1SET3  = 7;
    public static final int REQUEST_P2PTS   = 8;
    public static final int REQUEST_P2SET1  = 9;
    public static final int REQUEST_P2SET2  = 10;
    public static final int REQUEST_P2SET3  = 11;*/

    private KeypadListener listener;
    String title = "";
    String keys = "";
    String rangeMin = "1";
    String rangeMax = "2";
    String value = "";

    byte digits_max = 1;
    byte digits_count = 0;

    private Button btN0;
    private Button btN1;
    private Button btN2;
    private Button btN3;
    private Button btN4;
    private Button btN5;
    private Button btN6;
    private Button btN7;
    private Button btN8;
    private Button btN9;
    private TextView txtDisplay;

    //private String received_value;

    private float lastX, lastY;
    private boolean isDragging = false;

    private static final String ARG_1_TITLE = "1";
    private static final String ARG_2_KEYS  = "2";
    private static final String ARG_3_RANGE_MIN = "3";
    private static final String ARG_4_RANGE_MAX = "4";
    private static final String ARG_5_VALUE = "5";

    //----------------------------------------------------------------------------------------------
    private void setKeyStatus(String keys_to_show){
        btN0.setEnabled(keys_to_show.contains("0"));
        btN1.setEnabled(keys_to_show.contains("1"));
        btN2.setEnabled(keys_to_show.contains("2"));
        btN3.setEnabled(keys_to_show.contains("3"));
        btN4.setEnabled(keys_to_show.contains("4"));
        btN5.setEnabled(keys_to_show.contains("5"));
        btN6.setEnabled(keys_to_show.contains("6"));
        btN7.setEnabled(keys_to_show.contains("7"));
        btN8.setEnabled(keys_to_show.contains("8"));
        btN9.setEnabled(keys_to_show.contains("9"));

        if(btN0.isEnabled()){ btN0.setTextColor(Color.WHITE);} else {btN0.setTextColor(Color.DKGRAY);}
        if(btN1.isEnabled()){ btN1.setTextColor(Color.WHITE);} else {btN1.setTextColor(Color.DKGRAY);}
        if(btN2.isEnabled()){ btN2.setTextColor(Color.WHITE);} else {btN2.setTextColor(Color.DKGRAY);}
        if(btN3.isEnabled()){ btN3.setTextColor(Color.WHITE);} else {btN3.setTextColor(Color.DKGRAY);}
        if(btN4.isEnabled()){ btN4.setTextColor(Color.WHITE);} else {btN4.setTextColor(Color.DKGRAY);}
        if(btN5.isEnabled()){ btN5.setTextColor(Color.WHITE);} else {btN5.setTextColor(Color.DKGRAY);}
        if(btN6.isEnabled()){ btN6.setTextColor(Color.WHITE);} else {btN6.setTextColor(Color.DKGRAY);}
        if(btN7.isEnabled()){ btN7.setTextColor(Color.WHITE);} else {btN7.setTextColor(Color.DKGRAY);}
        if(btN8.isEnabled()){ btN8.setTextColor(Color.WHITE);} else {btN8.setTextColor(Color.DKGRAY);}
        if(btN9.isEnabled()){ btN9.setTextColor(Color.WHITE);} else {btN9.setTextColor(Color.DKGRAY);}
    }

    //----------------------------------------------------------------------------------------------
    private boolean RangeCheck(String value_to_check){
        boolean result = false;
        if(!value_to_check.isEmpty()) {
            byte min = (byte) Integer.parseUnsignedInt(rangeMin);
            byte max = (byte) Integer.parseUnsignedInt(rangeMax);
            byte val = (byte) Integer.parseUnsignedInt(value_to_check);
            if ((val >= min) && (val <= max)) {
                result = true;
            }
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public static Keypad1 newInstance(String title, String keys, String range_min, String range_max, String value) {
        Bundle args = new Bundle();
        Keypad1 fragment = new Keypad1();
        args.putString(ARG_1_TITLE, title);
        args.putString(ARG_2_KEYS, keys);
        args.putString(ARG_3_RANGE_MIN, range_min);
        args.putString(ARG_4_RANGE_MAX, range_max);
        args.putString(ARG_5_VALUE, value);
        fragment.setArguments(args);
        return fragment;
    }

    //----------------------------------------------------------------------------------------------
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_keypad, null);
        txtDisplay = view.findViewById(R.id.txtDisplay);
        Button btBACK = view.findViewById(R.id.btBACK);
        Button btOK = view.findViewById(R.id.btOK);
        Button btCLEAR = view.findViewById(R.id.btCLEAR);
        btN0 = view.findViewById(R.id.btN0);
        btN1 = view.findViewById(R.id.btN1);
        btN2 = view.findViewById(R.id.btN2);
        btN3 = view.findViewById(R.id.btN3);
        btN4 = view.findViewById(R.id.btN4);
        btN5 = view.findViewById(R.id.btN5);
        btN6 = view.findViewById(R.id.btN6);
        btN7 = view.findViewById(R.id.btN7);
        btN8 = view.findViewById(R.id.btN8);
        btN9 = view.findViewById(R.id.btN9);

        assert getArguments() != null;
        title = getArguments().getString(ARG_1_TITLE);
        keys = getArguments().getString(ARG_2_KEYS);
        rangeMin = getArguments().getString(ARG_3_RANGE_MIN);
        rangeMax = getArguments().getString(ARG_4_RANGE_MAX);
        value = getArguments().getString(ARG_5_VALUE);

        if(Integer.parseUnsignedInt(rangeMax)>10){ digits_max = 2;}
        else{ digits_max = 1;}
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(digits_max);

        txtDisplay.setFilters(filters);
        txtDisplay.setText(value);

        setKeyStatus(keys);
        if(btN0.isEnabled()){ btN0.setOnClickListener(this);}
        if(btN1.isEnabled()){ btN1.setOnClickListener(this);}
        if(btN2.isEnabled()){ btN2.setOnClickListener(this);}
        if(btN3.isEnabled()){ btN3.setOnClickListener(this);}
        if(btN4.isEnabled()){ btN4.setOnClickListener(this);}
        if(btN4.isEnabled()){ btN5.setOnClickListener(this);}
        if(btN6.isEnabled()){ btN6.setOnClickListener(this);}
        if(btN7.isEnabled()){ btN7.setOnClickListener(this);}
        if(btN8.isEnabled()){ btN8.setOnClickListener(this);}
        if(btN9.isEnabled()){ btN9.setOnClickListener(this);}
        btOK.setOnClickListener(this);
        btCLEAR.setOnClickListener(this);
        btBACK.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.getContext().getTheme().applyStyle(R.style.KeyboarStyle, true);
        builder.setView(view);
        view.setOnTouchListener(this);
        return builder.create();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if(window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        Context context = requireContext();
        float width_dp = 240;
        params.width = DensityUtils.dpToPx(context, width_dp);
        float height_dp = 280;
        params.height = DensityUtils.dpToPx(context, height_dp);
        window.setAttributes(params);
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (KeypadListener) context;
        }catch (ClassCastException e){
           throw new ClassCastException(context + " must implement KeypadListener");
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v1) {
        String key_pressed = "";
        String current_string = txtDisplay.getText().toString();

        switch (v1.getId()) {

            case (int)R.id.btN0:
                key_pressed = "0";
                digits_count++;
                break;
            case (int)R.id.btN1:
                key_pressed = "1";
                digits_count++;
                break;
            case (int)R.id.btN2:
                key_pressed = "2";
                digits_count++;
                break;
            case (int)R.id.btN3:
                key_pressed = "3";
                digits_count++;
                break;
            case (int)R.id.btN4:
                key_pressed = "4";
                digits_count++;
                break;
            case (int)R.id.btN5:
                key_pressed = "5";
                digits_count++;
                break;
            case (int)R.id.btN6:
                key_pressed = "6";
                digits_count++;
                break;
            case (int)R.id.btN7:
                key_pressed = "7";
                digits_count++;
                break;
            case (int)R.id.btN8:
                key_pressed = "8";
                digits_count++;
                break;
            case (int)R.id.btN9:
                key_pressed = "9";
                digits_count++;
                break;
            case (int)R.id.btCLEAR:
                key_pressed = ""; current_string = "";
                digits_count = 0;
                break;
            case (int)R.id.btOK:
                if (RangeCheck(txtDisplay.getText().toString())) {
                    value = txtDisplay.getText().toString();
                    listener.onValueChanged(title, value);
                }
                dismiss();
                break;
            case (int)R.id.btBACK:
                dismiss();
                break;
            default:
                break;
        }

        // update the TextView
        if (digits_count <= digits_max){
            if(digits_max == 1){ current_string = key_pressed; digits_count = 0;}
            else { current_string += key_pressed;}
            txtDisplay.setText(current_string);
        } else {
            digits_count = (byte)digits_max;
        }
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        view.performClick();
        int action = motionEvent.getAction();
        float currentX = motionEvent.getRawX();
        float currentY = motionEvent.getRawY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = currentX;
                lastY = currentY;
                isDragging = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    float deltaX = currentX - lastX;
                    float deltaY = currentY - lastY;

                    View v = view.getRootView();
                    int newX = (int) (v.getX() + deltaX);
                    int newY = (int) (v.getY() + deltaY);

                    v.setX(newX);
                    v.setY(newY);

                    lastX = currentX;
                    lastY = currentY;
                }
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                break;
        }

        return true;
    }
    //----------------------------------------------------------------------------------------------

}
//==================================================================================================