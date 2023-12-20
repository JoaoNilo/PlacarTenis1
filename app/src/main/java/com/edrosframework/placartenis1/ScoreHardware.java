//==================================================================================================
package com.edrosframework.placartenis1;
import com.edrosframework.placartenis1.converts.NConverter;


//--------------------------------------------------------------------------------------------------
public class ScoreHardware {

    //----------------------------------------------------------------------------------------------
    private static final float BatteryUpperThreshold = (float)8.30;
    private static final float BatteryFullyCharged = (float)8.0;
    private static final float BatteryEmpty = (float)7.00;

    //----------------------------------------------------------------------------------------------
    private static final float[] Vtable = {
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 90%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 80%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 70&

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 60%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 50%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 40%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 30%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 20%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), // 10%

            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9),
            ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9), ((float) 99.9)  //0%

    };

    //----------------------------------------------------------------------------------------------
    private static final int FLAG_STATUS_PLAY1_TENS     = ((int) 0x00000001);
    private static final int FLAG_STATUS_PLAY1_UNITS    = ((int) 0x00000002);
    private static final int FLAG_STATUS_PLAY1_SET1     = ((int) 0x00000004);
    private static final int FLAG_STATUS_PLAY1_SET2     = ((int) 0x00000008);
    private static final int FLAG_STATUS_PLAY1_SET3     = ((int) 0x00000010);
    private static final int FLAG_STATUS_PLAY2_TENS     = ((int) 0x00000020);
    private static final int FLAG_STATUS_PLAY2_UNITS    = ((int) 0x00000040);
    private static final int FLAG_STATUS_PLAY2_SET1     = ((int) 0x00000080);
    private static final int FLAG_STATUS_PLAY2_SET2     = ((int) 0x00000100);
    private static final int FLAG_STATUS_PLAY2_SET3     = ((int) 0x00000200);

    private static final int FLAG_STATUS_CHARGED        = ((int) 0x00004000);
    private static final int FLAG_STATUS_CHARGING       = ((int) 0x00008000);

    private static final int CHARGER_FAULT_THRESHOLD    = 20;
    //----------------------------------------------------------------------------------------------
    //private boolean charger_fault;
    //private int charger_counter;
    private float battery_previous;
    private float battery_voltage;
    private float battery_charge;
    private byte nodeStatus_lsb;
    private byte nodeStatus_msb;
    private byte settings;
    private int nodeStatus;

    //----------------------------------------------------------------------------------------------
    public boolean Player1Tens;
    public boolean Player1Units;
    public boolean Player1Set1;
    public boolean Player1Set2;
    public boolean Player1Set3;
    public boolean Player2Tens;
    public boolean Player2Units;
    public boolean Player2Set1;
    public boolean Player2Set2;
    public boolean Player2Set3;
    public boolean Charged;
    public boolean Charging;

    //----------------------------------------------------------------------------------------------
    private NConverter Converter = new NConverter();

    //----------------------------------------------------------------------------------------------
    private void ClearFlags() {
        Player1Tens = Player1Units = false;
        Player1Set1 = Player1Set2 = Player1Set3 = false;
        Player2Tens = Player2Units = false;
        Player2Set1 = Player2Set2 = Player2Set3 = false;
        Charged = Charging = false;
    }

    //----------------------------------------------------------------------------------------------
    private void UpdateFlags(int flags) {
        Player1Tens = ((nodeStatus & FLAG_STATUS_PLAY1_TENS) > 0);
        Player1Units = ((nodeStatus & FLAG_STATUS_PLAY1_UNITS) > 0);
        Player1Set1 = ((nodeStatus & FLAG_STATUS_PLAY1_SET1) > 0);
        Player1Set2 = ((nodeStatus & FLAG_STATUS_PLAY1_SET2) > 0);
        Player1Set3 = ((nodeStatus & FLAG_STATUS_PLAY1_SET3) > 0);

        Player2Tens = ((nodeStatus & FLAG_STATUS_PLAY2_TENS) > 0);
        Player2Units = ((nodeStatus & FLAG_STATUS_PLAY2_UNITS) > 0);
        Player2Set1 = ((nodeStatus & FLAG_STATUS_PLAY2_SET1) > 0);
        Player2Set2 = ((nodeStatus & FLAG_STATUS_PLAY2_SET2) > 0);
        Player2Set3 = ((nodeStatus & FLAG_STATUS_PLAY2_SET3) > 0);

        Charged = ((nodeStatus & FLAG_STATUS_CHARGED) > 0);
        Charging = ((nodeStatus & FLAG_STATUS_CHARGING) > 0);
    }

    //----------------------------------------------------------------------------------------------
    public ScoreHardware() {
        battery_previous = 0;
        battery_voltage = 0;
        nodeStatus_lsb = 0;
        nodeStatus_msb = 0;
        nodeStatus = 0;
        settings = 0;

        ClearFlags();
    }

    //----------------------------------------------------------------------------------------------
    public void Update(byte[] new_data){
        settings = new_data[0];
        Converter.ShiftLeft(new_data, (byte)new_data.length, (byte)1);
        battery_previous = battery_voltage;
        battery_voltage = Converter.ToFloat(new_data);
        Converter.ShiftLeft(new_data, (byte)new_data.length, (byte)4);
        nodeStatus_lsb = new_data[0];
        Converter.ShiftLeft(new_data, (byte)new_data.length, (byte)1);
        nodeStatus_msb = new_data[1];
        nodeStatus = nodeStatus_msb;
        nodeStatus <<= 8;
        nodeStatus |= ((int)nodeStatus_lsb & 0x000000FF);
        nodeStatus &= 0x0000FFFF;

        UpdateFlags(nodeStatus);
    }

    //----------------------------------------------------------------------------------------------
    public float getBatteryVoltage(){
        return(battery_voltage);
    }


    //----------------------------------------------------------------------------------------------
    public short getNodeStatus() {
        return((short)(nodeStatus & 0x0000FFFF));
    }

    //----------------------------------------------------------------------------------------------
    public byte getSettings() {
        return(settings);
    }

    //----------------------------------------------------------------------------------------------
    public float getBatteryCharge(){
        float voltage = BatteryEmpty;
        if(battery_voltage >= voltage){ voltage = battery_voltage;}
        float ratio = (BatteryFullyCharged - BatteryEmpty);
        battery_charge = (float)((voltage - BatteryEmpty) / ratio) * 100;
        if(battery_charge > 100.0){ battery_charge = 100;}
        return(battery_charge);
    }

    //----------------------------------------------------------------------------------------------
    public int getBatteryChargeIndex(){
        int index = 0;
        float charge = getBatteryCharge();
        if(charge  >= 85){ index = 4;}
        else if(charge  >= 65){ index = 3;}
        else if(charge >= 35){ index = 2;}
        else if(charge >= 15){ index = 1;}
        return(index);
    }

}

//==================================================================================================
