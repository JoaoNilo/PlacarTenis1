//===============================================================================
package com.edrosframework.placartenis1;

import android.Manifest;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Objects;
import java.util.Set;

//------------------------------------------------------------------------------
public class ListDevices_Activity extends ListActivity {

    static String MAC_ADDRESS = null;

    //--------------------------------------------------------------------------
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> ArrayBluetooth = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean permission_granted = false;

        // Galaxy A54 take these permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            permission_granted = true;
        } else {
            String permission = Manifest.permission.BLUETOOTH;
            ActivityCompat.requestPermissions(ListDevices_Activity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                permission_granted = true;
            }
        }

        if(!permission_granted) {
            // Galaxy Note 8 take these permissions
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED)) {
                permission_granted = true;
            }
        }

        if(permission_granted){
            try {
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if(pairedDevices!=null) {
                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceMac = device.getAddress();
                            String adapter = getResources().getString(R.string.adapter_name);
                            if (Objects.equals(deviceName, adapter)) {
                                ArrayBluetooth.add(deviceName + "\n" + deviceMac);
                            }
                        }
                    }
                    setListAdapter(ArrayBluetooth);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    //--------------------------------------------------------------------------
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String description = ((TextView) v).getText().toString();
        String MacAddress = description.substring(description.length()-17);

        Intent MacRetrieve = new Intent();
        MacRetrieve.putExtra(MAC_ADDRESS, MacAddress);
        setResult(RESULT_OK, MacRetrieve);
        finish();
    }
}
//==============================================================================