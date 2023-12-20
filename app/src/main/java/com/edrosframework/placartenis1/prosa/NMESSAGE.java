//==================================================================================================
package com.edrosframework.placartenis1.prosa;

//--------------------------------------------------------------------------------------------------
public class NMESSAGE {
    public static final int NM_NULL                     = 0x00000000;
    public static final int NM_UNICAST_PROCESS          = 0x10000001;
    public static final int NM_SERVICE_PROCESS          = 0x10000002;
    public static final int NM_BROADCAST_PROCESS        = 0x10000003;
    public static final int NM_UNICAST_POSTPROCESS      = 0x10000004;
    public static final int NM_SERVICE_POSTPROCESS      = 0x10000005;
    public static final int NM_UNICAST_RESPONSE         = 0x10000006;
    public static final int NM_SERVICE_RESPONSE         = 0x10000007;
    public static final int NM_POSTPROCESS              = 0x10000008;

    public static final int NM_ROUTING_PROCESS          = 0x10000009;
    public static final int NM_ROUTING_RESPONSE         = 0x1000000A;

    public static final int NM_SERIAL_ONINTERPRET       = 0x10000021;
    public static final int NM_SERIAL_ONPOSTINTERPRET   = 0x10000022;
    public static final int NM_SERIAL_ONSLAVESILENCE    = 0x10000023;
    public static final int NM_DATAGRAM_PROCESS         = 0x1000000C;

    public int message;
    public int param1;
    public int param2;
    public int tag;

    public NMESSAGE(){ message=0; param1=0; param2=0; tag=0;}
}
//--------------------------------------------------------------------------------------------------

//==================================================================================================
