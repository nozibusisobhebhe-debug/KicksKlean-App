package com.example.kicksklean;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ConfirmationActivity extends AppCompatActivity {

    private TextView confPackage, confPrice, confQuantity, confTotal, confTurnaround, confReturnDate, confBookingDate;
    private TextView confName, confPhone, confAddress, confReference;
    private Button btnEditBooking, btnProceedToBluetooth;

    private String packageName, customerName, customerPhone, customerAddress, bookingReference;
    private String turnaroundTime, expectedReturn, bookingDate;
    private double packagePrice, totalPrice;
    private int quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Toolbar toolbar = findViewById(R.id.toolbarConfirmation);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get data from Intent
        packageName = getIntent().getStringExtra("package_name");
        packagePrice = getIntent().getDoubleExtra("package_price", 0);
        quantity = getIntent().getIntExtra("quantity", 1);
        totalPrice = getIntent().getDoubleExtra("total_price", packagePrice);
        turnaroundTime = getIntent().getStringExtra("turnaround_time");
        expectedReturn = getIntent().getStringExtra("expected_return");
        bookingDate = getIntent().getStringExtra("booking_date");
        customerName = getIntent().getStringExtra("customer_name");
        customerPhone = getIntent().getStringExtra("customer_phone");
        customerAddress = getIntent().getStringExtra("customer_address");
        bookingReference = getIntent().getStringExtra("booking_reference");

        // Initialize views
        confPackage = findViewById(R.id.confPackage);
        confPrice = findViewById(R.id.confPrice);
        confQuantity = findViewById(R.id.confQuantity);
        confTotal = findViewById(R.id.confTotal);
        confTurnaround = findViewById(R.id.confTurnaround);
        confReturnDate = findViewById(R.id.confReturnDate);
        confBookingDate = findViewById(R.id.confBookingDate);
        confName = findViewById(R.id.confName);
        confPhone = findViewById(R.id.confPhone);
        confAddress = findViewById(R.id.confAddress);
        confReference = findViewById(R.id.confReference);
        btnEditBooking = findViewById(R.id.btnEditBooking);
        btnProceedToBluetooth = findViewById(R.id.btnProceedToBluetooth);

        // Set data
        confPackage.setText(packageName);
        confPrice.setText(String.format("R%.0f", packagePrice));
        confQuantity.setText(String.valueOf(quantity));
        confTotal.setText(String.format("R%.0f", totalPrice));
        confTurnaround.setText(turnaroundTime);
        confReturnDate.setText(expectedReturn);
        confBookingDate.setText(bookingDate);
        confName.setText(customerName);
        confPhone.setText(customerPhone);
        confAddress.setText(customerAddress);
        confReference.setText(bookingReference);

        // Edit button - go back to BookingActivity
        btnEditBooking.setOnClickListener(v -> finish());

        // Proceed to Bluetooth button
        btnProceedToBluetooth.setOnClickListener(v -> {
            // Create booking object
            Booking booking = new Booking(packageName, packagePrice, quantity, totalPrice,
                    turnaroundTime, expectedReturn, customerName, customerPhone, customerAddress);

            // Save to SharedPreferences
            BookingStorage storage = new BookingStorage(ConfirmationActivity.this);
            storage.saveBooking(booking);

            Toast.makeText(ConfirmationActivity.this, "Booking saved! Ready for Bluetooth transfer.", Toast.LENGTH_SHORT).show();

            // Proceed to Bluetooth
            Intent intent = new Intent(ConfirmationActivity.this, BluetoothTransferActivity.class);
            intent.putExtra("package_name", packageName);
            intent.putExtra("package_price", packagePrice);
            intent.putExtra("quantity", quantity);
            intent.putExtra("total_price", totalPrice);
            intent.putExtra("customer_name", customerName);
            intent.putExtra("customer_phone", customerPhone);
            intent.putExtra("customer_address", customerAddress);
            intent.putExtra("booking_reference", bookingReference);
            startActivity(intent);
        });
    }
}