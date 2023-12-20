//==================================================================================================
package com.edrosframework.placartenis1.prosa;

//--------------------------------------------------------------------------------------------------
public class NComFifo extends NFifo {
    //PARAMETERS cparams = new PARAMETERS();
    byte[] local_buffer;
    char local_buffer_size;

    //----------------------------------------------------------------------------------------------
    public NComFifo(int Packets, int MaxPacketSize) {
        super(Packets, MaxPacketSize);
        local_buffer_size = (char) MaxPacketSize;
        local_buffer = new byte[local_buffer_size];

        super.LockOnOverflow = true;
        //params = super.GetControl();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    public PARAMETERS GetControl() {
        //params = super.GetControl();
        return (params);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Put(NDatagram inDatagram) {
        boolean result = false;
        char packet_size = inDatagram.Export(local_buffer);
        if(local_buffer_size >= packet_size){
            result = super.Put(local_buffer, (byte)packet_size);
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Get(NDatagram outDatagram) {
        boolean result = false;
        if(params.counter>0){
            int packet_size = super.Get(local_buffer);
            outDatagram.Import(local_buffer);
            result = outDatagram.Validate();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public int Counter() {
        //params = super.GetControl();
        return (params.counter);
    }
}

//==================================================================================================