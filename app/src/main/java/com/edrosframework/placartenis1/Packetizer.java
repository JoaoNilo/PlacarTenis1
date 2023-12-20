//==================================================================================================
package com.edrosframework.placartenis1;

import android.os.Handler;

//--------------------------------------------------------------------------------------------------
public class Packetizer {

    private final int MAX_SIZE = 256;
    private byte[] data = new byte[MAX_SIZE];
    private int size = 0;
    private Handler xHandler = null;

    //----------------------------------------------------------------------------------------------
    public Packetizer(Handler h){
        xHandler = h;
        flush();
    }

    //----------------------------------------------------------------------------------------------
    public void flush() {
        for (int i = 0; i < MAX_SIZE; i++) {
            data[i] = 0;
        }
        size = 0;
    }

    //----------------------------------------------------------------------------------------------
    public int put(byte[] dat, int sz) {
        if ((size + sz) > MAX_SIZE) {
            sz = MAX_SIZE - size;
        }
        for (int i = 0; i < sz; i++) {
            data[size + i] = dat[i];
        }
        size += sz;
        return (size);
    }

    //----------------------------------------------------------------------------------------------
    public int get(byte[] xdat) {
        if (xdat == null) {
            return 0;
        }
        for (int i = 0; i < size; i++) {
            xdat[i] = data[i];
        }
        return (size);
    }

    //----------------------------------------------------------------------------------------------
    public int getSize() {
        return (size);
    }
}
//==================================================================================================