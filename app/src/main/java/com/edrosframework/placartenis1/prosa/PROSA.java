//==================================================================================================
package com.edrosframework.placartenis1.prosa;

//--------------------------------------------------------------------------------------------------
public class PROSA {
    // structural data
    public static final byte SIZE_HEADER        = ((byte) 4);
    public static final byte SIZE_PAYLOAD       = ((byte) 32);
    public static final byte SIZE_CRC           = ((byte) 2);
    public static final byte SIZE_DATAGRAM      = ((byte) SIZE_HEADER + SIZE_PAYLOAD + SIZE_CRC);

    // field positions
    public static final byte OFFESET_ADDR_DST   = ((byte) 0);
    public static final byte OFFESET_ADDR_SRC   = ((byte) 1);
    public static final byte OFFESET_LENGTH     = ((byte) 2);
    public static final byte OFFESET_COMMAND    = ((byte) 3);

    // constants
    public static final byte OK                 = ((byte) 0x4F);
    public static final byte FAILED             = ((byte) 0x46);

    // addresses
    public static final byte ADDR_NULL          = ((byte) 0x00);
    public static final byte ADDR_DEVICE1       = ((byte) 0x10);
    public static final byte ADDR_DEVICE2       = ((byte) 0x11);
    public static final byte ADDR_IHM1          = ((byte) 0x61);
    public static final byte ADDR_IHM2          = ((byte) 0x62);
    public static final byte ADDR_SERVICE       = ((byte) 0xFE);
    public static final byte ADDR_BROADCAST     = ((byte) 0xFF);

    // basic commands
    public static final byte CMD_SECURE_DATA    = ((byte) 0x00);
    public static final byte CMD_VERSION_GET    = ((byte) 0x01);
    public static final byte CMD_ECHO_GET       = ((byte) 0x02);
    public static final byte CMD_PING_GET       = ((byte) 0x03);

    // specific commands
    public static final byte CMD_SCORE_BITE     = ((byte) 0x50);
    public static final byte CMD_SCORE_POWER    = ((byte) 0x51);
    public static final byte CMD_SCORE_UPDATE   = ((byte) 0x52);

}
//==================================================================================================