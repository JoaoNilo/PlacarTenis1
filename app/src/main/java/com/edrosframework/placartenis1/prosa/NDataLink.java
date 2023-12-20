//==================================================================================================
package com.edrosframework.placartenis1.prosa;

import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_BROADCAST_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_ROUTING_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERIAL_ONSLAVESILENCE;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_SERVICE_PROCESS;
import static com.edrosframework.placartenis1.prosa.NMESSAGE.NM_UNICAST_PROCESS;

import java.util.Timer;
import java.util.TimerTask;
//--------------------------------------------------------------------------------------------------
public class NDataLink extends NComponent{

    public interface NDataLinkListeners{
        void onPacketToSend(byte[] d, byte n);
        void onPacketSent();
        void onReload(NDatagram d);
        void onSilence();
        void onBrokenPacket(byte[] d, byte n);
        void onInvalidPacket();
        void onUnicastReceived(NDatagram d);
        void onServiceReceived(NDatagram d);
        void onBroadcastReceived(NDatagram d);
        void onRoutingReceived(NDatagram d);

        void onAnyDatagram(NDatagram d);
        void onNewDatagram(NDatagram d);
        void onDispatch(NDatagram d);
        void onDisconnected();
    }

    NComFifo OutFifo;
    NDatagram InData;
    NDatagram OutData;
    NDatagram tmpInData;
    NDatagram periodicData;
    byte[] OutBuffer;
    int OutSize;

    int outbuffer_size;
    int lower_threshold;
    int upper_threshold;

    byte local_addr;
    byte service_addr;
    byte broadcast_addr;
    dlBusPrivileges privilege;
    int time_dispatch;
    int time_reload;
    int unanswered;
    int off_threshold;

    int timeout_packet;
    int timeout_counter;
    int packet_retries;
    int retries_counter;

    NDataLinkListeners listener;
    NComponent ProtocolInterpreter;
    boolean flagEncripted;
    byte last_source_address;

    boolean flagReloaded;
    boolean flagTimingOut;
    boolean flagRequestTiming;
    int dispatch_counter;
    int reload_counter;

    Timer timer;
    TimerTask timerTask;
    //Double time = 0.0;
    //boolean timerStarted = false;

    public boolean AssistedReception;
    public boolean AssistedTransmission;
    public boolean AddressesEnabled;
    public boolean RoutingEnabled;
    public boolean EncryptionEnabled;
    public boolean AutoDecryptionEnabled;

    //----------------------------------------------------------------------------------------------
    // auxiliary functions
    private void RestartDatagram(){}

    //----------------------------------------------------------------------------------------------
    private boolean DispatchData(){
        boolean result = false;
        // checa se há re-envio pendente
        if(retries_counter>0){
            retries_counter--;
            //NSerial::Write(OutBuffer, OutSize); <<<<<<<<<<<<<<<<<<<<
            if(listener != null){ listener.onPacketToSend(OutBuffer, (byte)OutSize);}
            result = true;
        } else {
            // checa se há novo datagrama assíncrono pendente
            if(OutFifo.Counter()>0){
                if(OutFifo.Get(OutData)){

                    if(EncryptionEnabled){OutData.Encrypt();}
                    OutSize = OutData.Export(OutBuffer);

                    if(listener!= null){ listener.onPacketToSend(OutBuffer, (byte)OutSize);}
                    // Retries do datagrama prevalece sobre Retries do DataLink
                    if(OutData.getRetries() > 0){
                        retries_counter = OutData.getRetries(); OutData.setRetries((byte)0);
                    } else if(packet_retries > 0){ retries_counter = packet_retries;}
                    result = true;
                }
                // checa se há novo datagrama periódico pendente
            } else if(flagReloaded){
                flagReloaded = false;
                if(EncryptionEnabled){ periodicData.Encrypt();}
                OutSize = periodicData.Export(OutBuffer);
                if(listener != null){ listener.onPacketToSend(OutBuffer, (byte)OutSize);}
                result = true;
            }
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    // getters and setters
    public void setLocalAddress(byte la){ local_addr = la;}
    public byte getLocalAddress(){ return(local_addr);}
    public void setServiceAddress(byte sa){ service_addr = sa;}
    public byte getServiceAddress(){ return(service_addr);}
    public void setBroadcastAddress(byte ba){ broadcast_addr = ba;}
    public byte getBroadcastAddress(){ return(broadcast_addr);}

    public void setBusPrivilege(dlBusPrivileges pv){ privilege = pv;}
    public dlBusPrivileges getBusPrivilege(){ return(privilege);}

    //----------------------------------------------------------------------------------------------
    // Methods
    //----------------------------------------------------------------------------------------------
    public NDataLink() {
        listener = null;
        InData = new NDatagram();
        OutData = new NDatagram();
        tmpInData = new NDatagram();
        periodicData = new NDatagram();

        OutBuffer = new byte[PROSA.SIZE_DATAGRAM];
        OutSize = 0;
        OutFifo = new NComFifo(5, PROSA.SIZE_DATAGRAM);

        unanswered = 0;
        privilege = dlBusPrivileges.Slave;
        local_addr = PROSA.ADDR_NULL;

        AddressesEnabled = false;
        AssistedReception = false;

        lower_threshold = 2;        // break reload

        time_reload = 50;           // 50 * 10ms  = 500ms
        time_dispatch = 10;         // 10  * 10ms  = 100ms
        timeout_packet = 4;         // 4  * 10ms  = 40ms

        timer = new Timer();
    }

    //----------------------------------------------------------------------------------------------
    public void setListeners(NDataLinkListeners lsn){
        this.listener = lsn;
    }

    //----------------------------------------------------------------------------------------------
    public void BindReceptionAssistant(NComponent Protocol){
        ProtocolInterpreter = Protocol;
        AssistedReception = true; AddressesEnabled = true;
    }

    //----------------------------------------------------------------------------------------------
    public void Open(){
        if(off_threshold>0){ unanswered = off_threshold;}
        startTimer();
    }
    //----------------------------------------------------------------------------------------------
    public void Close() {
        timerTask.cancel();
    }

    //----------------------------------------------------------------------------------------------
    public boolean ProcessPacket(byte[] indata, byte size){
        InData.Import(indata);
        return(ProcessData());
    }

    //----------------------------------------------------------------------------------------------
    public boolean ProcessDatagram(NDatagram iDt){
        InData.CopyFrom(iDt);
        return(ProcessData());
    }

    //------------------------------------------------------------------------------
    private boolean ProcessData(){
        boolean result = false;
        NMESSAGE msg = new NMESSAGE();

        if(InData.Validate()){
            // zera o contador de re-envio
            retries_counter = 0;
            // pára a temporização de timeout
            flagTimingOut = false;
            // invalida a contagem de desconexão
            if(off_threshold>0){ unanswered = off_threshold;}

            // segue o jogo
            tmpInData.CopyFrom(InData);

            // checa se é criptografado
            if(EncryptionEnabled || AutoDecryptionEnabled){
                if(tmpInData.IsEncrypted()){
                    flagEncripted = true; tmpInData.Decrypt();
                }
            }

            // registra último endereço remetente válido
            last_source_address = tmpInData.getSource();

            // notifica recebimeno "irrestrito"
            if(listener != null){ listener.onAnyDatagram(tmpInData);}

            if(!AssistedReception){
                // se não houver um interpretador associado, chama evento
                if(AddressesEnabled){
                    if((local_addr != PROSA.ADDR_NULL)&&(tmpInData.getDestination()==local_addr)){
                        if(listener != null){ listener.onUnicastReceived(tmpInData);}
                    } else if((service_addr != PROSA.ADDR_NULL)&&(tmpInData.getDestination()==service_addr)){
                        if(listener != null){ listener.onServiceReceived(tmpInData);}
                    } else if((broadcast_addr != PROSA.ADDR_NULL)&&(tmpInData.getDestination()==broadcast_addr)){
                        if(listener != null){ listener.onBroadcastReceived(tmpInData);}
                    } else if((RoutingEnabled)&&(listener != null)){
                        listener.onRoutingReceived(tmpInData);
                    }
                } else { if(listener != null){ listener.onNewDatagram(tmpInData);}}
            } else {
                if(AddressesEnabled){
                    // se houver um interpretador associado, despacha mensagem
                    // ATENÇÃO: Habilitar endereçamento (AddressesEnabled = true)
                    if((local_addr != PROSA.ADDR_NULL)&&(tmpInData.getDestination()==local_addr)){
                        msg.message = NM_UNICAST_PROCESS;
                    } else if((service_addr != PROSA.ADDR_NULL)&&(tmpInData.getDestination()==service_addr)){
                        msg.message = NM_SERVICE_PROCESS;
                    } else if((broadcast_addr != PROSA.ADDR_NULL)&&(tmpInData.getDestination()==broadcast_addr)){
                        msg.message = NM_BROADCAST_PROCESS;
                    } else if(RoutingEnabled){ msg.message = NM_ROUTING_PROCESS;}
                }
                // finaliza a preparação da mensagem para notificação
                //msg.param1 = this.Handle;
                if(ProtocolInterpreter != null){ ProtocolInterpreter.Notify(msg);}
            }
            result = true;
        } else { if(listener != null){ listener.onInvalidPacket();}}

        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Put(NDatagram ExtData){
        boolean result = false;

        if(ExtData.Validate()){
            if(privilege == dlBusPrivileges.Master){
                result = OutFifo.Put(ExtData);
            } else if(privilege == dlBusPrivileges.Slave) {
                if((EncryptionEnabled)||((AutoDecryptionEnabled)&&(flagEncripted))){
                    ExtData.Encrypt();
                }
                OutSize = ExtData.Export(OutBuffer);
                if(listener != null){ listener.onPacketToSend(OutBuffer, (byte)OutSize);}
                result = true;
            }
        }

        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Reload(NDatagram ExtData){
        boolean result = false;
        if(ExtData.Validate()){ periodicData.CopyFrom(ExtData); result = true;}
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public NDatagram Get(){
        return(tmpInData);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Flush(dlFlushables nFlushable) {
        boolean result =false;
        switch(nFlushable){
            case InFifo: RestartDatagram(); result = true; break;
            case OutFifo: OutFifo.Flush(); result = true; break;
            case BothFifos:
            default: RestartDatagram(); OutFifo.Flush(); result = true; break;
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public void ForceRetry() {
        OutSize = OutData.Export(OutBuffer);
        retries_counter = 1;
        flagRequestTiming = true;
        timeout_counter = timeout_packet;
    }

    /** ----------------------------------------------------------------------------------------
     * @brief start timer
     */
    private void startTimer() {
        timer.scheduleAtFixedRate( new TimerTask() {
            //---------------------------------------------------------------------------------------
            @Override
            public void run() {
                if(privilege == dlBusPrivileges.Master){
                    if(time_reload>0){
                        if(reload_counter>0){ reload_counter--;}
                        else {
                            reload_counter = time_reload;

                            if(OutFifo.Counter() < lower_threshold){
                                if((listener != null) && (!flagReloaded)){
                                    periodicData.Invalidate();
                                    listener.onReload(periodicData);
                                    if(periodicData.Validate()){ flagReloaded = true;}
                                }
                            }
                        }
                    }

                    if(dispatch_counter>0){ dispatch_counter--;}
                    else {
                        dispatch_counter = time_dispatch;
                        flagRequestTiming = DispatchData();
                        if(flagRequestTiming){ timeout_counter = timeout_packet;}
                    }

                    if(flagTimingOut){
                        if(timeout_counter>0){ timeout_counter--;}
                        else{
                            flagTimingOut = false;
                            if(retries_counter == 0){
                                if(AssistedTransmission){
                                    NMESSAGE msg = new NMESSAGE();
                                    msg.message =  NM_SERIAL_ONSLAVESILENCE;
                                    msg.param1 = (int)(Object)this;
                                } else {
                                    if(listener!=null){ listener.onSilence();}
                                }
                                if(off_threshold>0){
                                    if(unanswered>0){ unanswered--;}
                                    else {
                                        Close();
                                        if(listener != null){ listener.onDisconnected();}
                                    }
                                }
                            }
                            RestartDatagram();
                        }
                    }
                }
            }
            //---------------------------------------------------------------------------------------
        }, 0, 10);
    }
}


//==================================================================================================