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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

//--------------------------------------------------------------------------------------------------
public class Keypad2 extends DialogFragment implements View.OnClickListener, View.OnTouchListener {

    private static final String ARG_1_TITLE = "1";
    private static final String ARG_2_KEYS  = "2";
    // private static final String ARG_3_RANGE_MIN = "3";
    //private static final String ARG_4_RANGE_MAX = "4";
    private static final String ARG_5_VALUE = "5";

    String key_pressed;
    private KeypadListener listener;
    String title = "";
    String keys = "";
    String value = "";

    //byte digits_max = 1;
    byte digits_count = 0;
    private float lastX, lastY;
    private boolean isDragging = false;

    private Button btPad;
    private Button btPno;
    private TextView txtDisplayPoints;

    //int width_pix;

    //----------------------------------------------------------------------------------------------
    private void SetKeyboard(String config){
        if(config.equals("Full")){
            btPad.setFocusable(true);
            btPad.setEnabled(true);
            btPad.setTextColor(Color.WHITE);
            btPno.setFocusable(true);
            btPno.setEnabled(true);
            btPno.setTextColor(Color.WHITE);
        } else {
            btPad.setFocusable(false);
            btPad.setEnabled(false);
            btPad.setTextColor(Color.DKGRAY);
            btPno.setFocusable(false);
            btPno.setEnabled(false);
            btPno.setTextColor(Color.DKGRAY);
        }
    }

    //----------------------------------------------------------------------------------------------
    public static Keypad2 newInstance(String title, String keys, String value) {

        Bundle args = new Bundle();
        Keypad2 fragment = new Keypad2();
        args.putString(ARG_1_TITLE, title);
        args.putString(ARG_2_KEYS, keys);
        args.putString(ARG_5_VALUE, value);
        fragment.setArguments(args);
        return fragment;
    }

    //----------------------------------------------------------------------------------------------
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_keypoints, null);
        txtDisplayPoints = view.findViewById(R.id.txtDisplayPoints);
        Button btOKPoints = view.findViewById(R.id.btOKPoints);
        Button btP00 = view.findViewById(R.id.btP00);
        Button btP15 = view.findViewById(R.id.btP15);
        Button btP30 = view.findViewById(R.id.btP30);
        Button btP40 = view.findViewById(R.id.btP40);
        btPad = view.findViewById(R.id.btPad);
        btPno = view.findViewById(R.id.btPno);

        try {
            assert getArguments() != null;
            title = getArguments().getString(ARG_1_TITLE);
            keys = getArguments().getString(ARG_2_KEYS);
            value = getArguments().getString(ARG_5_VALUE);
            key_pressed = value;
            String display = title + " = " + value;
            txtDisplayPoints.setText(display);

        } catch(NullPointerException e) {
            dismiss();
        }


        btP00.setOnClickListener(this);
        btP15.setOnClickListener(this);
        btP30.setOnClickListener(this);
        btP40.setOnClickListener(this);
        btPad.setOnClickListener(this);
        btPno.setOnClickListener(this);
        btOKPoints.setOnClickListener(this);


        SetKeyboard(keys);

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
        float height_dp = 270;
        params.height = DensityUtils.dpToPx(context, height_dp);
        float padding_dp = 16;
        params.verticalMargin = DensityUtils.dpToPx(context, padding_dp);
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

        switch (v1.getId()) {

            case (int)R.id.btP00:
                key_pressed = "00";
                digits_count=1;
                break;
            case (int)R.id.btP15:
                key_pressed = "15";
                digits_count=1;
                break;
            case (int)R.id.btP30:
                key_pressed = "30";
                digits_count=1;
                break;
            case (int)R.id.btP40:
                key_pressed = "40";
                digits_count=1;
                break;
            case (int)R.id.btPad:
                key_pressed = "Ad";
                digits_count=1;
                break;
            case (int)R.id.btPno:
                key_pressed = "  ";
                digits_count=1;
                break;
            case (int)R.id.btOKPoints:
                listener.onValueChanged(title, key_pressed);
                dismiss();
                break;
            default:
                break;
        }

        // update the TextView
        if(digits_count==1){
            String display = title+ " = "+ key_pressed;
            txtDisplayPoints.setText(display);
            digits_count = 0;
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