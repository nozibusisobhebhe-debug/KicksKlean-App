package com.example.kicksklean;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button btnBasic = findViewById(R.id.btnBasic);
        Button btnPremium = findViewById(R.id.btnPremium);
        Button btnDeep = findViewById(R.id.btnDeep);
        Button btnMyBookings = findViewById(R.id.btnMyBookings);

        btnBasic.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookingActivity.class);
            intent.putExtra("package_name", "Basic Clean");
            intent.putExtra("package_price", 150.00);
            startActivity(intent);
        });

        btnPremium.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookingActivity.class);
            intent.putExtra("package_name", "Premium Clean");
            intent.putExtra("package_price", 250.00);
            startActivity(intent);
        });

        btnDeep.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookingActivity.class);
            intent.putExtra("package_name", "Deep Clean");
            intent.putExtra("package_price", 350.00);
            startActivity(intent);
        });

        btnMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyBookingsActivity.class);
            startActivity(intent);
        });
    }
}