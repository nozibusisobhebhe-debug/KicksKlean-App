package com.example.kicksklean;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        Toolbar toolbar = findViewById(R.id.toolbarBookings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView lvBookings = findViewById(R.id.lvBookings);
        TextView tvNoBookings = findViewById(R.id.tvNoBookings);

        BookingStorage storage = new BookingStorage(this);
        List<Booking> bookings = storage.getAllBookings();

        if (bookings.isEmpty()) {
            tvNoBookings.setVisibility(android.view.View.VISIBLE);
            lvBookings.setVisibility(android.view.View.GONE);
        } else {
            tvNoBookings.setVisibility(android.view.View.GONE);
            lvBookings.setVisibility(android.view.View.VISIBLE);

            List<String> bookingStrings = new ArrayList<>();
            for (Booking b : bookings) {
                String info = b.getPackageName() + " - R" + (int)b.getPackagePrice() +
                        "\nCustomer: " + b.getCustomerName() +
                        "\nPhone: " + b.getCustomerPhone() +
                        "\nAddress: " + b.getCustomerAddress() +
                        "\nBooked: " + b.getBookingDate() +
                        "\nReturn by: " + b.getExpectedReturnDate() +
                        "\nReference: " + b.getBookingReference();
                bookingStrings.add(info);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, bookingStrings);
            lvBookings.setAdapter(adapter);
        }
    }
}