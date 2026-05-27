package com.example.kicksklean;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookingStorage {

    private static final String PREF_NAME = "KicksKleanPrefs";
    private static final String BOOKINGS_KEY = "saved_bookings";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public BookingStorage(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveBooking(Booking booking) {
        List<Booking> bookings = getAllBookings();
        bookings.add(booking);
        String json = gson.toJson(bookings);
        sharedPreferences.edit().putString(BOOKINGS_KEY, json).apply();
    }

    public List<Booking> getAllBookings() {
        String json = sharedPreferences.getString(BOOKINGS_KEY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Booking[] bookings = gson.fromJson(json, Booking[].class);
        return new ArrayList<>(Arrays.asList(bookings));
    }

    public Booking getLastBooking() {
        List<Booking> bookings = getAllBookings();
        if (bookings.isEmpty()) {
            return null;
        }
        return bookings.get(bookings.size() - 1);
    }

    public void clearAllBookings() {
        sharedPreferences.edit().remove(BOOKINGS_KEY).apply();
    }
}