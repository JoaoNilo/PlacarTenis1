//==================================================================================================
package com.edrosframework.placartenis1.converts;

import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class NConverter {

    //----------------------------------------------------------------------------------------------
    public String ToHex(byte bt) {
        char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        char[] nibles = new char[2];

        nibles[1] = HEX_DIGITS[((bt >> 0) & 0x0F)];
        nibles[0] = HEX_DIGITS[((bt >> 4) & 0x0F)];
        return (new String(nibles));
    }

    //----------------------------------------------------------------------------------------------
    public String ToHex(char C) {
        byte[] B = new byte[2];
        B[1] = (byte)(C & 0x00FF);
        B[0] = (byte)(C >> 8);
        return ( ToHex(B));
    }

    //----------------------------------------------------------------------------------------------
    public String ToHex(byte[] dat, byte size, String sep) {
        String result = "";
        if (sep.length() > 3) {
            sep = sep.substring(0, 2);
        }

        if (size > 0) {
            result += ToHex(dat[0]);
            if (size > 1) {
                for (int c = 1; c < size; c++) {
                    result += (sep + ToHex(dat[c]));
                }
            }
        }
        return (result);
    }

    //----------------------------------------------------------------------------------------------
    public String ToHex(byte[] dat, String sep) {
        return(ToHex(dat, (byte)dat.length, sep));
    }

    //----------------------------------------------------------------------------------------------
    public String ToHex(byte[] dat) {
        return(ToHex(dat, (byte)dat.length, ""));
    }

    //-------------------------------------------------------
    /* remove spaces or 'separators' */
    public String Trim(String s) {
        int len = s.length();
        String Ref = "0123456789ABCDEF";
        int szRef = Ref.length();
        String Out = "";
        for (int i = 0; i < len; i ++) {
            if(Ref.contains(s.substring(i, i+1))){
                Out += s.substring(i,i+1);
            }
        }
        return Out;
    }

    //-------------------------------------------------------
    /* s must be an even-length string. */
    public byte[] ToBytes(String s) {
        int len = s.length();
        if((len % 2) > 0){
            String last = s.substring(s.length()-1);
            s = s.substring(0, s.length()-1)+ "0";
            s+= last; len = s.length();
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //-------------------------------------------------------
    // get 2 bytes from char 'C' (16-bit) : LSB first
    public byte[] ToBytes(char C){
        return( new byte[] { (byte)C, (byte)(C>>8) } );
    }

    //-------------------------------------------------------
    // get 4 bytes from int 'C' (16-bit) : LSB first
    public byte[] ToBytes(int C){
        return( new byte[] { (byte)C, (byte)(C>>8), (byte)(C>>16), (byte)(C>>24)} );
    }

    //-------------------------------------------------------
    // get 4 bytes from int 'C' (16-bit) : LSB first
    public byte[] ToBytes(float F){
        int I =  Float.floatToIntBits(F);
        return( ToBytes(I));
    }

    //-------------------------------------------------------
    // get 2 bytes from char 'C' (16-bit) : LSB first
    public byte[] Mirror(byte[] B){
        int size = B.length;
        byte[] result = new byte[size];
        for(int c = 0, i = size-1; c < size; c++ ){
            result[c] = B[i--];
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public void ShiftRight(byte[] payload, byte len, byte N){
        if(len>1) {
            len--;
            for (int j = 0; j < N; j++) {
                for (int i = len; i > 0; i--) {
                    payload[i] = payload[i - 1];
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public void ShiftLeft(byte[] payload, byte len, byte N){
        if(len > 1) {
            len--;
            for (byte j = 0; j < N; j++) {
                for (int i = 0; i < len; i++) {
                    payload[i] = payload[i + 1];
                }
            }
        }
    }

    //-------------------------------------------------------
    // build char from 2 bytes of byte[] : LSB first
    public char ToChar(byte[] B){
        return((char)(((char)B[1]<<8) + B[0]));
    }

    //-------------------------------------------------------
    // build int from 4 bytes of byte[] : LSB first
    public int ToInt(byte[] B){
        ByteBuffer wrapper = ByteBuffer.wrap(Arrays.copyOf(B, 4));
        wrapper.order(ByteOrder.LITTLE_ENDIAN);
        return(wrapper.getInt());
    }

    //-------------------------------------------------------
    // build float from 4 bytes of byte[] : LSB first
    public float ToFloat(byte[] B){
        ByteBuffer wrapper = ByteBuffer.wrap(Arrays.copyOf(B, 4));
        wrapper.order(ByteOrder.LITTLE_ENDIAN);
        return(wrapper.getFloat());
    }

    //----------------------------------------------------------------------------------------------
    public int getInt(Object This){
        int result=0;
        if(This  instanceof TextView){
            TextView T = (TextView) This;
            String v = T.getText().toString();
            try{
                result = Integer.getInteger(v);
            }catch (NumberFormatException e){
                result = -1;
            }
        } else if(This  instanceof String){
            String T = (String) This;
            try{
                result = Integer.getInteger(T);
            }catch (NumberFormatException e){
                result = -1;
            }
        }
        return(result);
    }

}
//==================================================================================================