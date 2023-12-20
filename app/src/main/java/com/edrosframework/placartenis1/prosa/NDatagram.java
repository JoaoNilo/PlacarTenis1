//==================================================================================================
package com.edrosframework.placartenis1.prosa;

import static com.edrosframework.placartenis1.prosa.NCrc.crc16Ccitt;

import com.edrosframework.placartenis1.CommBuffer;
import com.edrosframework.placartenis1.converts.NConverter;

import java.util.Arrays;
import java.util.Random;

//--------------------------------------------------------------------------------------------------

/**
 * @brief Datagram type for Prosa protocolo
 * This class defines fields and features for the datagram to be used by the Prosa protocol.
 */
public class NDatagram {

    private final byte RETRIES_MAX = 10;

    byte dest_addr;
    byte srce_addr;
    byte len;
    byte command;
    byte[] payload;
    char crc;

    byte retries;
    char size;
    byte payload_capacity;
    byte header_size;
    public boolean AutoUpdateCrc;

    NCrc Crc = new NCrc();
    Random Rnd = new Random();

    //----------------------------------------------------------------------------------------------
    //READ-WRITE PROPERTIES
    public byte getDestination(){ return dest_addr;}
    public void setDestination(byte dst){ dest_addr = dst;}
    public byte getSource(){ return srce_addr;}
    public void setSource(byte src){ srce_addr = src; }
    public byte getLength(){ return len;}
    public void setLength(byte n){ len = n; }
    public byte getCommand(){ return command;}
    public void setCommand(byte cmd){ command = cmd; }
    public byte getPayloadCapacity(){ return payload_capacity;}
    //public void setPayloadCapacity(byte cap){ payload_capacity = cap; }
    public byte getRetries(){ return retries;}
    public void setRetries(byte ret){ retries = ret; }
    public char getCrc(){ return crc;}
    public void setCrc(char c){ crc = c;}

    // READ-ONLY PROPERTIES
    public char getSize(){ return size;}


    //----------------------------------------------------------------------------------------------
    // auxiliary functions
    void Twist(){
        int sz1 = len; int sz2; byte swp;
        for(int c=1; c<sz1; c++){ payload[c] ^= payload[0];} sz2=sz1>>1; sz1--;
        for(int c=0; c<sz2; c++){
            swp = payload[c]; payload[c] = payload[sz1-c]; payload[sz1-c] = swp;
        }
    }

    //----------------------------------------------------------------------------------------------
    void Untwist(){
        int sz1 = len; int sz2; byte swp;
        sz2 = sz1>>1; sz1--;
        for(int c=0; c<sz2; c++) {
            swp=payload[c]; payload[c]=payload[sz1-c]; payload[sz1-c]=swp;
        } sz1++;

        for(int c=1; c<sz1; c++){ payload[c] ^= payload[0];};
    }

    //----------------------------------------------------------------------------------------------
    boolean UpdateSize(){
        size = (char)(header_size + len + (char)PROSA.SIZE_CRC);
        if(AutoUpdateCrc){ UpdateCrc();}
        return(true);
    }

    //----------------------------------------------------------------------------------------------
    void ShiftRight(byte N){
        for (int j=0; j<N; j++){
            for(int i=len; i>0; i--){
                payload[i] = payload[i-1];
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    void ShiftLeft(byte N){
        for (int j=0; j<N; j++){
            for(int i=0; i<len; i++){
                payload[i] = payload[i+1];
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // METHODS
    //----------------------------------------------------------------------------------------------
    // Main constructor
    public NDatagram(){
        AutoUpdateCrc = true;
        header_size = PROSA.SIZE_HEADER;
        payload_capacity = PROSA.SIZE_PAYLOAD;
        payload = new byte[payload_capacity];
        retries = 0;
        Rnd.nextInt(253);
        Refresh();
    }

    //----------------------------------------------------------------------------------------------
    public void Refresh(){
        dest_addr = 0; srce_addr = 0; len = 0; command = 0; crc = 0;
        size = (char)(header_size + PROSA.SIZE_CRC);
    }

    //----------------------------------------------------------------------------------------------
    public void Flush(){
        len = 0; size = (char)(header_size + PROSA.SIZE_CRC);
        if(AutoUpdateCrc){ UpdateCrc();}
    }

    //----------------------------------------------------------------------------------------------
    // @return number of bytes remaining to complete this datagram
    public char FillHeader(byte[] extHeader){
        char result = 2; // crc size
        if(extHeader[PROSA.OFFESET_LENGTH] > payload_capacity){ return (0);}
        dest_addr = extHeader[PROSA.OFFESET_ADDR_DST];
        srce_addr = extHeader[PROSA.OFFESET_ADDR_SRC];
        len = extHeader[PROSA.OFFESET_LENGTH];
        command = extHeader[PROSA.OFFESET_COMMAND];
        size = (char)(result + header_size + len);
        if(AutoUpdateCrc){ UpdateCrc();}
        retries = 0;
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    // @return payload size
    public char FillPayload(byte[] extPayload, byte extSize){
        if(extSize>payload_capacity){ extSize = payload_capacity;}
        for(int c=0; c< extSize; c++){  payload[c] = extPayload[c];}
        len = extSize;
        UpdateSize();
        return((char)len);
    }

    //----------------------------------------------------------------------------------------------
    public char ExtractHeader(byte[] extHeader){
        extHeader[PROSA.OFFESET_ADDR_DST] = dest_addr;
        extHeader[PROSA.OFFESET_ADDR_SRC] = srce_addr;
        extHeader[PROSA.OFFESET_LENGTH] = len;
        extHeader[PROSA.OFFESET_COMMAND] = command;
        return((char)header_size);
    }

    //----------------------------------------------------------------------------------------------
    public char ExtractPayload(byte[] extPayload){
        char result = (char)len;
        if(result>0) {
            for(int c=0; c<len; c++){ extPayload[c] = payload[c];}
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Append(float f){
        boolean result = false;
        NConverter Convert = new NConverter();
        int varsize = Float.BYTES;
        if((len + varsize)<payload_capacity){
            byte[] float_bytes = Convert.ToBytes(f);
            len += varsize;
            ShiftRight((byte)varsize);
            for(int c=0; c<varsize; c++){
                payload[c] = float_bytes[c];
            }
            result = UpdateSize();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Append(int i){
        boolean result = false;
        NConverter Convert = new NConverter();
        int varsize = Integer.BYTES;
        if((len + varsize)<payload_capacity){
            byte[] int_bytes = Convert.ToBytes(i);
            len += varsize;
            ShiftRight((byte)varsize);
            for(int c=0; c<varsize; c++){
                payload[c] = int_bytes[c];
            }
            result = UpdateSize();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Append(char C){
        boolean result = false;
        NConverter Convert = new NConverter();
        int varsize = Character.BYTES;
        if((len + varsize)<payload_capacity){
            byte[] char_bytes = Convert.ToBytes(C);
            len += varsize;
            ShiftRight((byte)varsize);
            for(int c=0; c<varsize; c++){
                payload[c] = char_bytes[c];
            }
            result = UpdateSize();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Append(byte b){
        boolean result = false;
        if((len + 1) < payload_capacity){
            len += 1;
            ShiftRight((byte)1);
            payload[0] = b;
            result = UpdateSize();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Append(byte[] xbuffer, byte xsize){
        boolean result = false;
        if((len + xsize) < payload_capacity){
            len += xsize;
            ShiftRight(xsize);
            for(int c=0; c<xsize; c++){ payload[c] = xbuffer[c];}
        }
        result = UpdateSize();
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public boolean Append(CommBuffer xBuffer){
        boolean result = false;
        if((len + xBuffer.size) < payload_capacity){
            len += xBuffer.size;
            ShiftRight((byte)xBuffer.size);
            for(int c=0; c<xBuffer.size; c++){ payload[c] = xBuffer.data[c];}
        }
        result = UpdateSize();
        return(result);
    }


    //----------------------------------------------------------------------------------------------
    // extracts 1 float value (4 bytes) from payload
    public float ExtractFloat(){
        float result =0;
        NConverter Convert = new NConverter();
        if(len >= Float.BYTES){
            result = Convert.ToFloat(payload);
             ShiftLeft((byte)Float.BYTES);
             len -= Float.BYTES;
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    // extracts 1 Integer value (4 bytes) from payload
    public int ExtractInt(){
        int result = 0;
        NConverter Convert = new NConverter();
        int varsize = Integer.BYTES;
        if(len >= varsize){
            result = Convert.ToInt(payload);
            ShiftLeft((byte)varsize);
            len -= varsize;
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    // extracts 1 Character value (2 bytes) from payload
    public boolean Extract(char[] c){
        boolean result = false;
        NConverter Convert = new NConverter();
        int varsize = Character.BYTES;
        if(len >= varsize){
            c[0] = Convert.ToChar(payload);
            ShiftLeft((byte)varsize);
            len -= varsize;
            result = UpdateSize();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    // extracts n Bytes from payload
    public boolean Extract(byte[] xbuffer, byte xsize){
        boolean result = false;
        NConverter Convert = new NConverter();
        int varsize = xsize;
        if(len >= varsize){
            for(int c=0; c<varsize; c++){
                xbuffer[c] = payload[c];
            }
            ShiftLeft((byte)varsize);
            len -= varsize;
            result = UpdateSize();
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    // extracts 1 Byte from payload
    public boolean Extract(byte[] b){
        return(Extract(b,(byte)1));
    }

    //----------------------------------------------------------------------------------------------
    // extracts 1 Byte from payload
    public byte Extract(){
        byte[] result = new byte[1];
        Extract(result);
        return(result[0]);
    }

    //----------------------------------------------------------------------------------------------
    // exports the datagram into a byte buffer
    public char Export(byte[] xbuffer){
        int i=0;
        xbuffer[i++] = dest_addr; xbuffer[i++] = srce_addr; xbuffer[i++] = len; xbuffer[i++]=command;
        for(int c = 0; c < len; c++){ xbuffer[i++] = payload[c];}
        xbuffer[i++]= (byte)(crc &0xFF); xbuffer[i++] = (byte)(crc >>8);
        return((char)i);
    }

    //----------------------------------------------------------------------------------------------
    // imports datagram from a byte buffer
    public char Import(byte[] xbuffer){
        int i=0;
        if(xbuffer != null){
            if(xbuffer.length >= header_size){
                dest_addr = xbuffer[i++];
                srce_addr = xbuffer[i++];
                len = xbuffer[i++];
                command = xbuffer[i++];
            }

            if(len > payload_capacity){ return 0;}
            size = (char)(header_size + len + (char)PROSA.SIZE_CRC);

            if(xbuffer.length >= (header_size+len)) {
                for (int c = 0; c < len; c++) { payload[c] = xbuffer[i++];}
            }

            if(xbuffer.length >= size) {
                crc = (char) ((char) xbuffer[i++] & 0xFF);
                crc += ((char) xbuffer[i] << 8);
            }
        }
        return((char)i);
    }

    //----------------------------------------------------------------------------------------------
    public void CopyFrom(NDatagram SrcDat){
        byte[] datagram = new byte[SrcDat.getSize()];
        SrcDat.Export(datagram);
        Import(datagram);
        retries = SrcDat.getRetries();
    }

    //----------------------------------------------------------------------------------------------
    public boolean Compare(NDatagram CompDat){
        boolean result = false;
        if(size == CompDat.getSize()){
            if(len == CompDat.getLength()){
                //UpdateCrc(); CompDat.UpdateCrc();
                if(crc == CompDat.getCrc()) { result = true;}
            }
        }
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public void ExchangeHeader(NDatagram xDat){
        dest_addr = xDat.getSource();
        srce_addr = xDat.getDestination();
        command = xDat.getCommand();
        len = 0; UpdateSize();
    }

    //----------------------------------------------------------------------------------------------
    public void SwapAddresses(){
        byte temp_dest = dest_addr;
        dest_addr = srce_addr;
        srce_addr = temp_dest;
        if(AutoUpdateCrc){ UpdateCrc();}
    }

    //----------------------------------------------------------------------------------------------
    public void Encrypt(){
        byte padding=0; byte KSC=0;
        if(payload_capacity > len) { padding = (byte) (payload_capacity - (len + 1));}
        else { padding = (byte) (payload_capacity - len);}

        byte PAD = (byte) (Rnd.nextInt(padding));
        byte[] Garbage = new byte[PAD];
        for(int c=0; c<PAD; c++){ Garbage[c] = (byte)Rnd.nextInt(255);}

        KSC = (byte)(1 + Rnd.nextInt(253));
        Append(command); Append(KSC);
        if(PAD>0) { Append(Garbage, PAD);}
        Append(PAD);

        command = PROSA.CMD_SECURE_DATA;
        Twist(); UpdateCrc();
    }

    //----------------------------------------------------------------------------------------------
    public void Decrypt(){
        byte[] data = new byte[2];
        Untwist();
        byte PAD = Extract();
        if(PAD>0){ byte[] Garbage = new byte[PAD]; Extract(Garbage, PAD);}
        Extract(data, (byte)2);
        command = data[1];
        UpdateCrc();
    }

    //----------------------------------------------------------------------------------------------
    public boolean IsEncrypted(){
        boolean result = false;
        if(Validate()){ if(command == PROSA.CMD_SECURE_DATA){ result = true;}}
        return(result);
    }

    //----------------------------------------------------------------------------------------------
    public byte Decryption(){
        byte[] data = new byte[2];
        Untwist();
        Extract(data, (byte)2);
        command = data[1];
        UpdateCrc();
        return(data[0]);
    }

    //----------------------------------------------------------------------------------------------
    public void UpdateCrc(){
        byte[] header = new byte[4];
        header[0] = dest_addr; header[1] = srce_addr; header[2] = len; header[3]=command;

        int work_crc = crc16Ccitt(header, 0x1D0F);
        if(len>0){ work_crc = crc16Ccitt(Arrays.copyOf(payload, len) , work_crc);}
        crc = (char)(work_crc & 0xFFFF);
    }

    //----------------------------------------------------------------------------------------------
    public int CheckCrc(){

        byte[] header = new byte[4];
        header[0] = dest_addr; header[1] = srce_addr; header[2] = len; header[3]=command;

        int work_crc = crc16Ccitt(header, 0x1D0F);
        if(len>0){ work_crc = crc16Ccitt(Arrays.copyOf(payload, len) , work_crc);}
        return(work_crc & 0xFFFF);
    }

    //----------------------------------------------------------------------------------------------
    public boolean IsValid(){ return Validate();}

    //----------------------------------------------------------------------------------------------
    public boolean Validate(){
        return crc == CheckCrc();
    }

    //----------------------------------------------------------------------------------------------
    public void Invalidate(){
        crc = (char)CheckCrc(); crc++;
    }

    //----------------------------------------------------------------------------------------------
    public String ToString() {
        byte[] buffer = new byte[size];
        Export(buffer);
        NConverter Convert = new NConverter();
        return( Convert.ToHex(buffer, (byte)size, " "));
    }
}

//==================================================================================================