package com.example.kicksklean;

import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Booking {
    private String packageName;
    private double packagePrice;
    private int quantity;
    private double totalPrice;
    private String turnaroundTime;
    private String expectedReturnDate;
    private String bookingDate;
    private String customerName;
    private String customerPhone;
    private String customerAddress;
    private String bookingReference;

    // Empty constructor for Gson
    public Booking() {
    }

    // Full constructor
    public Booking(String packageName, double packagePrice, int quantity, double totalPrice,
                   String turnaroundTime, String expectedReturnDate, String customerName,
                   String customerPhone, String customerAddress) {
        this.packageName = packageName;
        this.packagePrice = packagePrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.turnaroundTime = turnaroundTime;
        this.expectedReturnDate = expectedReturnDate;
        this.bookingDate = getCurrentDateTime();
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerAddress = customerAddress;
        this.bookingReference = generateReference();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String generateReference() {
        return "KICK" + System.currentTimeMillis();
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Booking fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Booking.class);
    }

    // Getters
    public String getPackageName() { return packageName; }
    public double getPackagePrice() { return packagePrice; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public String getTurnaroundTime() { return turnaroundTime; }
    public String getExpectedReturnDate() { return expectedReturnDate; }
    public String getBookingDate() { return bookingDate; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public String getBookingReference() { return bookingReference; }
}