package com.example.spiderremote;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        Intent ish = getIntent();
        Bundle bleh = ish.getExtras();
        TextView btInfoText = (TextView)findViewById(R.id.connected_to);

        SwitchCompat mySwitch = (SwitchCompat) findViewById(R.id.connection_switch);
        mySwitch.setClickable(false);

        ImageButton connect_btn = (ImageButton) findViewById(R.id.btn_connect);
        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createConnectThread != null){
                    createConnectThread.cancel();
                }
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
                createConnectThread.start();
            }
        });


        if (bleh != null){
            String blueToothInfo = (String) bleh.get("bt-info");
            String[] btArray =blueToothInfo.split("<->");
            String btName = btArray[0];
            String btMac = btArray[1];
            deviceAddress = btMac;
            deviceName = btName;
        }

        // If a bluetooth device has been selected from SelectDeviceActivity
        if (deviceName != null){
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                Log.d("Logger1", "Connected to " + deviceName);
                                mySwitch.setChecked(true);
                                btInfoText.setText("Connected to: " + deviceName);
                                //connect_btn.setClickable(false);
                                break;
                            case -1:
                                Log.d("Logger1", "Device fails to connect");
                                mySwitch.setChecked(false);
                                btInfoText.setText("Failed to connect...");
                                //connect_btn.setClickable(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        Log.d("Logger1", "Msg from Arduino: " + arduinoMsg);
                        break;
                }
            }
        };
        /*
        MyTouchListener touchListener = new MyTouchListener();

        Button btn0 = (Button) findViewById(R.id.btn_center);
        btn0.setOnTouchListener(touchListener);
        */
        RepeatListener myTouchListener = new RepeatListener(400, 900, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDataOnTouch(String.valueOf(view.getTag()));
            }
        });

        ImageButton btn1 = (ImageButton) findViewById(R.id.btn_forward);
        btn1.setOnTouchListener(myTouchListener);

        ImageButton btn2 = (ImageButton) findViewById(R.id.btn_backward);
        btn2.setOnTouchListener(myTouchListener);

        ImageButton btn3 = (ImageButton) findViewById(R.id.btn_left);
        btn3.setOnTouchListener(myTouchListener);

        ImageButton btn4 = (ImageButton) findViewById(R.id.btn_right);
        btn4.setOnTouchListener(myTouchListener);
    }

    /*
    public class MyTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d("Logger1", "FUCKIN TAG!!" + String.valueOf(v.getTag()));
            switch((String) v.getTag()){
                case "0":
                    Log.d("Logger1", "THIS IS TOUCHBTN!!!" + String.valueOf(v.getTag()));
                    sendDataOnTouch(String.valueOf(v.getTag()));
                    break;
                case "1":
                    Log.d("Logger1", "THIS IS TOUCHBTN!!!" + String.valueOf(v.getId()));
                    sendDataOnTouch(String.valueOf(v.getTag()));
                    break;
            }
            return true;
        }
    }
    */

    public void sendData(View v){
        String sendVal = (String) v.getTag();
        Log.d("Logger1", "sendData -> " + (String) v.getTag());
        connectedThread.write(sendVal);
    }

    public void sendDataOnTouch(String s){
        Log.d("Logger1", "sendDataOnTouch -> " + s);
        connectedThread.write(s);
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e("Logger1", "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e("Logger1", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("Logger1", "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /* ============================ Handle holding down button ====================== */

    public class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final View.OnClickListener clickListener;
        private View touchedView;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if(touchedView.isEnabled()) {
                    handler.postDelayed(this, normalInterval);
                    clickListener.onClick(touchedView);
                } else {
                    // if the view was disabled by the clickListener, remove the callback
                    handler.removeCallbacks(handlerRunnable);
                    touchedView.setPressed(false);
                    touchedView = null;
                }
            }
        };

        /**
         * @param initialInterval The interval after first click event
         * @param normalInterval The interval after second and subsequent click
         *       events
         * @param clickListener The OnClickListener, that will be called
         *       periodically
         */
        public RepeatListener(int initialInterval, int normalInterval,
                              View.OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(handlerRunnable);
                    handler.postDelayed(handlerRunnable, initialInterval);
                    touchedView = view;
                    touchedView.setPressed(true);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(handlerRunnable);
                    touchedView.setPressed(false);
                    touchedView = null;
                    return true;
            }
            return false;
        }
    }

}