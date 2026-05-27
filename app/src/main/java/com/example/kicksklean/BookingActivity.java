package com.example.kicksklean;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private TextView tvSelectedPackage, tvPackageName, tvPackagePrice, tvTotalPrice;
    private TextView tvTurnaround, tvExpectedReturn, tvQuantity, tvQuantityDisplay;
    private TextInputEditText etFullName, etPhone, etAddress;
    private Button btnConfirmBooking, btnMinus, btnPlus;

    private String packageName = "";
    private double packagePrice = 0;
    private String turnaroundTime = "";
    private int turnaroundDays = 0;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        packageName = getIntent().getStringExtra("package_name");
        packagePrice = getIntent().getDoubleExtra("package_price", 0);

        setTurnaroundTime();

        Toolbar toolbar = findViewById(R.id.toolbarBooking);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        tvSelectedPackage = findViewById(R.id.tvSelectedPackage);
        tvPackageName = findViewById(R.id.tvPackageName);
        tvPackagePrice = findViewById(R.id.tvPackagePrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTurnaround = findViewById(R.id.tvTurnaround);
        tvExpectedReturn = findViewById(R.id.tvExpectedReturn);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvQuantityDisplay = findViewById(R.id.tvQuantityDisplay);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);

        // Set package info
        tvSelectedPackage.setText(packageName);
        tvPackageName.setText(packageName);
        tvPackagePrice.setText(String.format("R%.0f", packagePrice));
        updateTotalPrice();
        tvTurnaround.setText(turnaroundTime);
        tvExpectedReturn.setText(calculateExpectedReturnDate());

        // Quantity - Minus button
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityDisplay();
            } else {
                Toast.makeText(this, "Minimum quantity is 1 pair", Toast.LENGTH_SHORT).show();
            }
        });

        // Quantity - Plus button
        btnPlus.setOnClickListener(v -> {
            if (quantity < 20) {
                quantity++;
                updateQuantityDisplay();
            } else {
                Toast.makeText(this, "Maximum quantity is 20 pairs", Toast.LENGTH_SHORT).show();
            }
        });

        // Confirm button
        btnConfirmBooking.setOnClickListener(v -> {
            if (validateInputs()) {
                double totalPrice = packagePrice * quantity;
                String expectedReturn = calculateExpectedReturnDate();

                Booking booking = new Booking(packageName, packagePrice, quantity, totalPrice,
                        turnaroundTime, expectedReturn, etFullName.getText().toString(),
                        etPhone.getText().toString(), etAddress.getText().toString());

                BookingStorage storage = new BookingStorage(BookingActivity.this);
                storage.saveBooking(booking);

                Intent intent = new Intent(BookingActivity.this, ConfirmationActivity.class);
                intent.putExtra("package_name", packageName);
                intent.putExtra("package_price", packagePrice);
                intent.putExtra("quantity", quantity);
                intent.putExtra("total_price", totalPrice);
                intent.putExtra("turnaround_time", turnaroundTime);
                intent.putExtra("expected_return", expectedReturn);
                intent.putExtra("booking_date", booking.getBookingDate());
                intent.putExtra("customer_name", etFullName.getText().toString());
                intent.putExtra("customer_phone", etPhone.getText().toString());
                intent.putExtra("customer_address", etAddress.getText().toString());
                intent.putExtra("booking_reference", booking.getBookingReference());
                startActivity(intent);
            }
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(quantity));
        tvQuantityDisplay.setText(String.valueOf(quantity));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        double total = packagePrice * quantity;
        tvTotalPrice.setText(String.format("R%.0f", total));
    }

    private void setTurnaroundTime() {
        if (packageName.contains("Basic")) {
            turnaroundTime = "1-2 days";
            turnaroundDays = 2;
        } else if (packageName.contains("Premium")) {
            turnaroundTime = "3 days";
            turnaroundDays = 3;
        } else if (packageName.contains("Deep")) {
            turnaroundTime = "3-5 days";
            turnaroundDays = 5;
        } else if (packageName.contains("Express")) {
            turnaroundTime = "Same day (24hr)";
            turnaroundDays = 1;
        } else {
            turnaroundTime = "2-3 days";
            turnaroundDays = 3;
        }
    }

    private String calculateExpectedReturnDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, turnaroundDays);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    private boolean validateInputs() {
        if (etFullName.getText().toString().trim().isEmpty()) {
            etFullName.setError("Name is required");
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
            return false;
        }

        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phone.length() != 10) {
            etPhone.setError("Must be exactly 10 digits");
            Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!phone.matches("\\d+")) {
            etPhone.setError("Numbers only please");
            Toast.makeText(this, "Phone number must contain only digits (0-9)", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etAddress.getText().toString().trim().isEmpty()) {
            etAddress.setError("Pickup address is required");
            Toast.makeText(this, "Please enter your pickup address", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}