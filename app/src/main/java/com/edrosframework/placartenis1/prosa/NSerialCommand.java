//==================================================================================================
package com.edrosframework.placartenis1.prosa;

import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_BROADCAST_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_NULL;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_POSTPROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_ROUTING_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_ROUTING_RESPONSE;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERVICE_POSTPROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERVICE_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_UNICAST_POSTPROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_UNICAST_PROCESS;

enum fsmCommandStates{ fsmCommandIdle, fsmPendingProcess, fsmPendingDataProcess,
     fsmPendingPostProcess, fsmPendingDataPostProcess, fsmPendingResponse, fsmCommandAccomplished}

//--------------------------------------------------------------------------------------------------
public class NSerialCommand  extends NCommand {
    public interface NCommandListeners{
        void onPostProcess();
        NDatagram onProcess(NDatagram dt);
    }

    fsmCommandStates status;
    NCommandListeners listener;
    Boolean Enabled;
    Boolean Responsive;

    //----------------------------------------------------------------------------------------------
    // getters and setters
    public void setIdentifier(byte new_id){ super.ID = new_id;}
    public byte getIdentifier(){ return(super.ID);}

    //----------------------------------------------------------------------------------------------
    // Methods
    //----------------------------------------------------------------------------------------------
    public NSerialCommand(NComponent protocol){
        this.listener = null;
        Enabled = true;
        Responsive = true;
        ((NSerialProtocol)protocol).IncludeCommand(this);
    }

    //----------------------------------------------------------------------------------------------
    public void setListeners(NCommandListeners lsn){
        this.listener = lsn;
    }

    //--------------------------------------------------------------------------------------------
    public NMESSAGE Notify(NMESSAGE msg, NDatagram dt){
        if(Enabled){
            if(msg.message == NM_POSTPROCESS){
                if(status == fsmCommandStates.fsmPendingPostProcess){
                    if(listener !=null){ listener.onPostProcess();}
                    status = fsmCommandStates.fsmCommandIdle;
                }
            } else {
                switch(msg.message){
                    case NM_UNICAST_PROCESS: msg.message = NM_UNICAST_POSTPROCESS; break;
                    case NM_SERVICE_PROCESS: msg.message = NM_SERVICE_POSTPROCESS; break;
                    case NM_BROADCAST_PROCESS: msg.message = NM_NULL; break;
                    case NM_ROUTING_PROCESS: msg.message = NM_ROUTING_RESPONSE; break;
                }
                //dt = (NDatagram)((Object) msg.param1);
                if(listener!=null){ dt = listener.onProcess(dt);}

                if(Responsive){
                    msg.param1 = (int) ((Object)dt).hashCode();
                    status = fsmCommandStates.fsmPendingPostProcess;
                } else {
                    msg.message = NM_NULL; msg.param1 = 0;
                }
            }
        } else { msg.message = NM_NULL; msg.param1 = 0;}

        return(msg);
    }
    //----------------------------------------------------------------------------------------------

}


//==================================================================================================