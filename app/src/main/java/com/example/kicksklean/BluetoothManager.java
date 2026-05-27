package com.example.kicksklean;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothManager {

    private static final String APP_NAME = "KicksKlean";
    private static final UUID MY_UUID = UUID.fromString("a60f35f0-b93a-11eb-8529-0242ac130003");

    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    public BluetoothManager(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // Check if device supports Bluetooth
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    // Check if Bluetooth is enabled
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // Get Bluetooth adapter
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    // Server side - Wait for connection (Provider phone)
    public AcceptThread createAcceptThread() {
        return new AcceptThread();
    }

    // Client side - Connect to server (Customer phone)
    public ConnectThread createConnectThread(BluetoothDevice device) {
        return new ConnectThread(device);
    }

    // Accept Thread - Listens for incoming connections
    public class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                if (socket != null) {
                    // Connection accepted
                    if (context instanceof BluetoothTransferActivity) {
                        ((BluetoothTransferActivity) context).onBluetoothConnected(socket);
                    }
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Connect Thread - Connects to server
    public class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        public void run() {
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
            if (context instanceof BluetoothTransferActivity) {
                ((BluetoothTransferActivity) context).onBluetoothConnected(socket);
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send data over Bluetooth
    public void sendData(BluetoothSocket socket, String data) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Receive data from Bluetooth
    public String receiveData(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes = inputStream.read(buffer);
            return new String(buffer, 0, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
