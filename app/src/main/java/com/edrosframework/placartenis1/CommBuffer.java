//==============================================================================
package com.edrosframework.placartenis1;

//------------------------------------------------------------------------------
public class CommBuffer {
    public byte[] data;
    public int size;

    //--------------------------------------------------------------------------
    public CommBuffer(int data_size){
        data = new byte[data_size];
        size = data_size;
    }
}
//==============================================================================