package com.example.paul.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    EditText inputField;
    Button btnSend1;
    Button btnSend2;
    Button btnSend3;
    Button btnSend0;
    ArrayAdapter<String> listAdapter;
    ListView listView;
    Spinner dropdown;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    IntentFilter filter;
    BroadcastReceiver receiver;
    String tag = "debugging";
    ConnectedThread connectedThread;
    String string1;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    //do something
                    connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";
                    connectedThread.write(s.getBytes());
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "No Bluetooth Detected", Toast.LENGTH_SHORT).show();
            finish();
        }
            else {
            if (!btAdapter.isEnabled()) {
                turnOnBT();
            }
            getPairedDevices();
            startDiscovery();
        }
        //inputField = (EditText)findViewById(R.id.input);
        btnSend1 = (Button)findViewById(R.id.b1);
        btnSend2 = (Button)findViewById(R.id.b2);
        btnSend3 = (Button)findViewById(R.id.b3);
        btnSend0 = (Button)findViewById(R.id.b0);

        btnSend1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (connectedThread != null) {
                    string1 = "1";
                    connectedThread.write(string1.getBytes());
                }
            }
        });
        btnSend2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (connectedThread != null) {
                    string1 = "2";
                    connectedThread.write(string1.getBytes());
                }
            }
        });
        btnSend3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (connectedThread != null) {
                    string1 = "3";
                    connectedThread.write(string1.getBytes());
                }
            }
        });
        btnSend0.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (connectedThread != null) {
                    string1 = "0";
                    connectedThread.write(string1.getBytes());
                }
            }
        });
    }

    private void startDiscovery() {
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    private void turnOnBT() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void getPairedDevices() {
        devicesArray = btAdapter.getBondedDevices();
        if (devicesArray.size() > 0 ){
            for (BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());
            }
        }
    }

    private void init() {
        dropdown = (Spinner)findViewById(R.id.spinner);
        dropdown.setOnItemSelectedListener(this);
        //listView = (ListView)findViewById(R.id.listView);
        //listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, 0);
        dropdown.setAdapter(listAdapter);
        //listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND == action){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String s = "";
                    for (int a = 0; a < pairedDevices.size(); a++){
                        if (device.getName().equals(pairedDevices.get(a))){
                            //append
                            s = "(Paired)";
                            break;
                        }
                    }
                    listAdapter.add(device.getName() + s + "/n" + device.getAddress());
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                    //run some code
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    //run some code

                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if (btAdapter.getState() == btAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        if (listAdapter.getItem(position).contains("Paired")){
            BluetoothDevice selectedDevice = devices.get(position);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
        }
        else{
            Toast.makeText(getApplicationContext(), "Device is not paired", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
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
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    buffer = new byte[1024];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}
