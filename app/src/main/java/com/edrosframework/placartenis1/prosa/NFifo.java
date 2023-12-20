//==================================================================================================
package com.edrosframework.placartenis1.prosa;

import android.os.Build;
import android.support.annotation.RequiresApi;

;import java.nio.ByteBuffer;

public class NFifo {
    private final int BUFFER_FIRST = 0;
    private final int LINE_SIZE = 65;
    private final int LINES_NUMBER = 10;
    private final int LINE_LENGTH_MAX = 255;
    private final int LINE_NUMBER_MAX = 20;

    protected PARAMETERS params = new PARAMETERS();

    private boolean overflow;
    private byte[] data = null;

    protected int line_size;
    protected int lines;

    public Boolean LockOnOverflow;

    //----------------------------------------------------------------------------------------------
    private void AllocateMemory(int length, int depth){
       // if((length < 2)||(depth < 2)) return;

        if((length < 2) || (length > LINE_LENGTH_MAX)) line_size = LINE_SIZE;
        if((depth < 2) || (depth > LINE_NUMBER_MAX)) lines = LINES_NUMBER;

        line_size = length; lines = depth;
        line_size = line_size + 1;

        data = new byte[line_size * lines];

        for(int y = 0; y < lines; y++){
            for(int x = 0; x < line_size; x++){
                data[y*line_size + x ] = 0;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    public NFifo (){
        Flush();
        AllocateMemory(LINES_NUMBER, LINE_SIZE);
        LockOnOverflow = false;
    }

    //----------------------------------------------------------------------------------------------
    public NFifo (int Lines, int LineSize){
        Flush();
        AllocateMemory(LineSize, Lines);
        LockOnOverflow = false;
    }

    //----------------------------------------------------------------------------------------------
    public PARAMETERS GetControl() {
        return(params);
    }

    //----------------------------------------------------------------------------------------------
    public void Flush(){
       params.head = 0; params.tail = params.counter = 0;
       params.overflow = false;
    }

    //----------------------------------------------------------------------------------------------
    int Counter(){
        return(params.counter);
    }

    //----------------------------------------------------------------------------------------------
    public byte GetSize(){
        return(data[params.tail * line_size]);
    }

    //------------------------------------------------------------------------------
    public boolean Put(byte[] xBuffer, byte xSize){
        //int pt_depth;
        //byte[] pt_byte;
        boolean result = false;

        if(xSize > line_size){ return(result);}

        if(!LockOnOverflow || (params.counter < lines)){

            if((params.counter>0)&&(params.head == params.tail)){
                if(++params.tail >= lines){ params.tail = BUFFER_FIRST;}
            }

            // guarda o tamanho da linha
            data[(params.head * line_size)] = xSize;
            // guarda os dados da linha
            for(int c=0, base=((params.head * line_size) + 1); c < xSize; c++){
                data[base+c] = xBuffer[c];
            }

            if(++params.head >= lines){ params.head = BUFFER_FIRST;}

            if(++params.counter > lines){
                params.counter = lines;  params.overflow = true;
            }
            result = true;
        }
        return(result);
    }

    //------------------------------------------------------------------------------
    public byte Get(byte[] xBuffer){
        byte result = 0;

        if(params.counter == 0) return(0);

        // busca o tamanho da linha
        result = data[(params.tail * line_size)];
        // busca os dados da linha
        for(int c=0, base=((params.tail * line_size) + 1); c < result; c++){
            xBuffer[c] = data[base+c];
        }

        if(++params.tail >= lines){ params.tail = BUFFER_FIRST;}

        if(params.tail == params.head){ params.counter = 0;}
        else { params.counter--;}

        if(params.counter == 0){ params.overflow = false;}

        return(result);
    }

    //----------------------------------------------------------------------------------------------


}
//==================================================================================================