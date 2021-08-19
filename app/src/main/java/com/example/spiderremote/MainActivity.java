package com.example.spiderremote;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> myPairedDevArr = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void getBT(View view){
        //ListView devListView;

        ListView devListView = (ListView) findViewById(R.id.dev_list);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            findViewById(R.id.dev_list).setVisibility(View.VISIBLE);
            myPairedDevArr.clear();
            for (BluetoothDevice device : pairedDevices) {
                myPairedDevArr.add(device.getName() + "<->" + device.getAddress());
            }
            /*
            for (int c = 0; c < myPairedDevArr.size(); c++){
                Log.d("Logger1", myPairedDevArr.get(c).toString());
            }
            */
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myPairedDevArr);
            arrayAdapter.notifyDataSetChanged();
            devListView.setAdapter(arrayAdapter);

            devListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                    intent.putExtra("bt-info", myPairedDevArr.get(i));
                    startActivity(intent);


                }
            });

        } else {
            myPairedDevArr.add("No Paired Device.");
        }
    }

}