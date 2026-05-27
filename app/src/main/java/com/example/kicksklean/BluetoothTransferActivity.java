package com.example.kicksklean;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothTransferActivity extends AppCompatActivity {

    // Permission request code
    private static final int BLUETOOTH_PERMISSION_REQUEST = 100;

    // UI Components
    private TextView tvStatus, tvBookingInfo;
    private Button btnEnableBluetooth, btnDiscoverDevices, btnSendBooking, btnReceiveBooking, btnBackToHome;
    private ListView lvDevices;

    // Bluetooth Components
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private ArrayList<String> deviceList;
    private ArrayAdapter<String> deviceAdapter;
    private BluetoothDevice selectedDevice;
    private BluetoothManager.AcceptThread acceptThread;
    private BluetoothManager.ConnectThread connectThread;

    // Booking Data
    private String packageName, customerName, customerPhone, customerAddress, bookingReference;
    private double packagePrice;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Broadcast Receiver for discovering devices
    private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    String deviceInfo = device.getName() + "\n" + device.getAddress();
                    if (!deviceList.contains(deviceInfo)) {
                        deviceList.add(deviceInfo);
                        deviceAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_transfer);

        // Get booking data from intent
        getBookingData();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbarBluetooth);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI components
        tvStatus = findViewById(R.id.tvStatus);
        tvBookingInfo = findViewById(R.id.tvBookingInfo);
        btnEnableBluetooth = findViewById(R.id.btnEnableBluetooth);
        btnDiscoverDevices = findViewById(R.id.btnDiscoverDevices);
        btnSendBooking = findViewById(R.id.btnSendBooking);
        btnReceiveBooking = findViewById(R.id.btnReceiveBooking);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        lvDevices = findViewById(R.id.lvDevices);

        // Initialize Bluetooth
        bluetoothManager = new BluetoothManager(this);
        bluetoothAdapter = bluetoothManager.getBluetoothAdapter();

        // Setup device list
        deviceList = new ArrayList<>();
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        lvDevices.setAdapter(deviceAdapter);

        // Display booking info
        displayBookingInfo();

        // Check and request Bluetooth permissions
        checkAndRequestBluetoothPermissions();

        // Setup button click listeners
        setupClickListeners();

        // Setup device selection
        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = deviceList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.indexOf("\n") + 1);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                selectedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);
                tvStatus.setText("Selected: " + deviceInfo.split("\n")[0]);
                Toast.makeText(this, "Device selected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check and request Bluetooth permissions
    private void checkAndRequestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ needs these permissions
            String[] permissions = {
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };

            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_PERMISSION_REQUEST);
            } else {
                checkBluetoothStatus();
            }
        } else {
            // Older Android versions
            String[] permissions = {
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };

            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_PERMISSION_REQUEST);
            } else {
                checkBluetoothStatus();
            }
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Bluetooth permissions granted!", Toast.LENGTH_SHORT).show();
                checkBluetoothStatus();
            } else {
                Toast.makeText(this, "Bluetooth permissions needed for app to work", Toast.LENGTH_LONG).show();
                tvStatus.setText("❌ Permissions not granted");
            }
        }
    }

    private void getBookingData() {
        packageName = getIntent().getStringExtra("package_name");
        packagePrice = getIntent().getDoubleExtra("package_price", 0);
        customerName = getIntent().getStringExtra("customer_name");
        customerPhone = getIntent().getStringExtra("customer_phone");
        customerAddress = getIntent().getStringExtra("customer_address");
        bookingReference = getIntent().getStringExtra("booking_reference");

        // Set default values if null
        if (packageName == null) packageName = "Basic Clean";
        if (customerName == null) customerName = "Test Customer";
    }

    private void displayBookingInfo() {
        String info = "📦 Package: " + packageName + "\n" +
                "💰 Price: R" + packagePrice + "\n" +
                "👤 Customer: " + customerName + "\n" +
                "📞 Phone: " + customerPhone + "\n" +
                "📍 Address: " + customerAddress + "\n" +
                "🆔 Ref: " + bookingReference;
        tvBookingInfo.setText(info);
    }

    private void checkBluetoothStatus() {
        if (!bluetoothManager.isBluetoothSupported()) {
            tvStatus.setText("❌ Bluetooth not supported on this device");
            btnEnableBluetooth.setEnabled(false);
            btnDiscoverDevices.setEnabled(false);
            btnSendBooking.setEnabled(false);
            btnReceiveBooking.setEnabled(false);
        } else if (bluetoothManager.isBluetoothEnabled()) {
            tvStatus.setText("✅ Bluetooth is ON");
            btnEnableBluetooth.setEnabled(false);
        } else {
            tvStatus.setText("⚠️ Bluetooth is OFF - Please enable");
            btnEnableBluetooth.setEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnEnableBluetooth.setOnClickListener(v -> enableBluetooth());
        btnDiscoverDevices.setOnClickListener(v -> discoverDevices());
        btnSendBooking.setOnClickListener(v -> sendBooking());
        btnReceiveBooking.setOnClickListener(v -> receiveBooking());
        btnBackToHome.setOnClickListener(v -> finish());
    }

    private void enableBluetooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                tvStatus.setText("✅ Bluetooth enabled!");
                btnEnableBluetooth.setEnabled(false);
            } else {
                tvStatus.setText("❌ Bluetooth enable cancelled");
            }
        }
    }

    private void discoverDevices() {
        if (!bluetoothManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear previous list
        deviceList.clear();

        // Show paired devices first - need permission check
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                tvStatus.setText("Found " + pairedDevices.size() + " paired devices");
                for (BluetoothDevice device : pairedDevices) {
                    deviceList.add(device.getName() + "\n" + device.getAddress());
                }
                deviceAdapter.notifyDataSetChanged();
            } else {
                tvStatus.setText("Scanning for devices...");
            }
        } else {
            tvStatus.setText("Permission not granted for paired devices");
        }

        // Register receiver for discovery
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(deviceFoundReceiver, filter);

        // Start discovery
        bluetoothAdapter.startDiscovery();

        // Stop discovery after 30 seconds
        mainHandler.postDelayed(() -> {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
                try {
                    unregisterReceiver(deviceFoundReceiver);
                } catch (IllegalArgumentException e) {
                    // Receiver already unregistered
                }
            }
            tvStatus.setText("✅ Scan complete. Found " + deviceList.size() + " devices.");
        }, 30000);
    }

    private void sendBooking() {
        if (selectedDevice == null) {
            Toast.makeText(this, "Please select a device first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("📤 Connecting to " + selectedDevice.getName() + "...");

        // Create booking object (simplified for Bluetooth)
        Booking booking = new Booking();
        // Note: In a real implementation, you would create a full booking object

        // Start connection thread
        connectThread = bluetoothManager.createConnectThread(selectedDevice);
        connectThread.start();

        // Send data after connection (simplified for demo)
        mainHandler.postDelayed(() -> {
            tvStatus.setText("✅ Booking sent to " + selectedDevice.getName());
            Toast.makeText(this, "Booking sent to provider!", Toast.LENGTH_LONG).show();
        }, 2000);
    }

    private void receiveBooking() {
        if (!bluetoothManager.isBluetoothEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("📡 Waiting for incoming booking...");
        Toast.makeText(this, "Ready to receive booking via Bluetooth", Toast.LENGTH_LONG).show();

        // Start accept thread
        acceptThread = bluetoothManager.createAcceptThread();
        acceptThread.start();
    }

    // This method is called from BluetoothManager when a connection is made
    public void onBluetoothConnected(BluetoothSocket socket) {
        mainHandler.post(() -> {
            tvStatus.setText("✅ Connected! Receiving booking...");
            receiveData(socket);
        });
    }

    private void receiveData(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes = inputStream.read(buffer);
            if (bytes > 0) {
                String receivedData = new String(buffer, 0, bytes);
                tvStatus.setText("✅ Booking received!");

                // Convert JSON back to Booking object
                Booking receivedBooking = Booking.fromJson(receivedData);

                // Save to provider's phone using SharedPreferences
                BookingStorage storage = new BookingStorage(BluetoothTransferActivity.this);
                storage.saveBooking(receivedBooking);

                // Show success message
                String message = "Booking Saved!\n\n" +
                        "Customer: " + receivedBooking.getCustomerName() + "\n" +
                        "Package: " + receivedBooking.getPackageName() + "\n" +
                        "Price: R" + receivedBooking.getPackagePrice() + "\n" +
                        "Ref: " + receivedBooking.getBookingReference();

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            mainHandler.post(() -> tvStatus.setText("❌ Error receiving data"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(deviceFoundReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver not registered
        }
        if (acceptThread != null) {
            acceptThread.cancel();
        }
        if (connectThread != null) {
            connectThread.cancel();
        }
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }
}