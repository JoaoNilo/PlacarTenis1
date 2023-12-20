//==================================================================================================
package com.edrosframework.placartenis1.prosa;

import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_BROADCAST_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_NULL;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_POSTPROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERIAL_ONPOSTINTERPRET;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERIAL_ONSLAVESILENCE;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERVICE_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERVICE_RESPONSE;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_UNICAST_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_UNICAST_RESPONSE;

import java.util.Vector;

//--------------------------------------------------------------------------------------------------
public class NSerialProtocol extends NComponent{
    NMESSAGE msgProtocol;
    int commands_number;
    NCommand Command;
    Vector<NCommand> protCommands;
    NDataLink dataLink;
    NDatagram tmpData;

    public boolean Enabled;
    public byte maxPayload;
    public byte[] localPayload;

    //----------------------------------------------------------------------------------------------
    //auxiliary functions
    private void Dismiss(NDatagram oDt){
        NMESSAGE msgProtocol = new NMESSAGE();
        msgProtocol.message = NM_POSTPROCESS;
        msgProtocol.param1 = 0;
        msgProtocol.param2 = 0;

        for(NCommand C: protCommands){
            if(C.ID == oDt.command){
                ((NSerialCommand)C).Notify(msgProtocol, oDt);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // getters and setters
    public NDataLink GetDataLink(){ return(null);}
    public void SetDataLink(NDataLink dLink){
        dataLink = dLink;
        dataLink.BindReceptionAssistant((NComponent)this);
    }

    //----------------------------------------------------------------------------------------------
    // Methods
    public NSerialProtocol(NDataLink dLink){
        tmpData = new NDatagram();
        dataLink = dLink;
        dataLink.BindReceptionAssistant((NComponent)this);

        protCommands = new Vector<NCommand>(10);
        Enabled = true;
        localPayload = new byte[PROSA.SIZE_PAYLOAD];
    }

    //----------------------------------------------------------------------------------------------
    public int IncludeCommand(NSerialCommand newCommand){
        protCommands.add(newCommand);
        return(protCommands.size());
    }

    //----------------------------------------------------------------------------------------------
    public boolean CheckId(NDatagram xDat){
        boolean result = false;
        for (NCommand C: protCommands) {
            if(C.ID == xDat.command){ result = true; break;}
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public byte Interpret(NMESSAGE msg, NDatagram iDt){
        byte n = 0;
        iDt.ExtractPayload(localPayload);

        for (NCommand C: protCommands) {
            if(C.ID == iDt.command){
                msg = ((NSerialCommand)C).Notify(msg, iDt);
                if(msg.message != NM_NULL){
                    switch(msg.message){
                        case NM_UNICAST_RESPONSE:
                        case NM_SERVICE_RESPONSE:
                            if(dataLink.getBusPrivilege() == dlBusPrivileges.Slave){
                                // NOT YET AVAILABLE FOR JAVA VERSION
                                //dataLink.Put((NDatagram)(Object)(msgProtocol.param1));
                                //dataLink.Put(iDt);
                            }
                            break;
                    }
                    break;
                }
            } n++;
        }
        return(n);
    }

    //----------------------------------------------------------------------------------------------
    public void Notify(NMESSAGE msg){
        if(Enabled){
            switch(msg.message){
                case NM_UNICAST_PROCESS:
                case NM_SERVICE_PROCESS:
                case NM_BROADCAST_PROCESS:
                    tmpData = dataLink.Get();
                    Interpret(msg, tmpData);
                    msg.message = NM_NULL;
                    break;
                case NM_SERIAL_ONPOSTINTERPRET:
                    msg.message = NM_NULL;
                    Dismiss(tmpData);
                    break;
                case NM_SERIAL_ONSLAVESILENCE:
                    msg.message = NM_NULL;
                    break;
                default: msg.message = NM_NULL; break;
            }
        } else msg.message = NM_NULL;
    }

}

//==================================================================================================
