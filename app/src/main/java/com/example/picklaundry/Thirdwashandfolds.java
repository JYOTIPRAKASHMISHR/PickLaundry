package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Thirdwashandfolds extends AppCompatActivity {

    private static final String TAG = "Thirdwashandfolds";

    private TextView orderIdText, shirtQuantity, pantsQuantity, othersQuantity, totalPieces, totalPrice;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirdwashandfolds);

        // Initialize TextViews
        orderIdText = findViewById(R.id.orderIdText);
        shirtQuantity = findViewById(R.id.shirtQuantity);
        pantsQuantity = findViewById(R.id.pantsQuantity);
        othersQuantity = findViewById(R.id.othersQuantity);
        totalPieces = findViewById(R.id.totalPieces);
        totalPrice = findViewById(R.id.totalPrice);

        // Initialize Firebase reference for Washandfolds
        databaseReference = FirebaseDatabase.getInstance().getReference("Washandfolds");

        // Fetch and display the latest order details
        fetchLatestOrderDetails();
    }

    private void fetchLatestOrderDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot latestOrderSnapshot = null;

                    // Retrieve the latest order
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrderSnapshot = orderSnapshot;
                    }

                    if (latestOrderSnapshot != null) {
                        String orderId = latestOrderSnapshot.getKey();
                        Integer shirts = latestOrderSnapshot.child("ShirtQuantity").getValue(Integer.class);
                        Integer pants = latestOrderSnapshot.child("PantsQuantity").getValue(Integer.class);
                        Integer others = latestOrderSnapshot.child("OthersQuantity").getValue(Integer.class);
                        Integer price = latestOrderSnapshot.child("TotalPrice").getValue(Integer.class);
                        Integer total = latestOrderSnapshot.child("TotalPieces").getValue(Integer.class);

                        // Default to 0 if any value is null
                        shirts = (shirts != null) ? shirts : 0;
                        pants = (pants != null) ? pants : 0;
                        others = (others != null) ? others : 0;
                        total = (total != null) ? total : (shirts + pants + others);
                        price = (price != null) ? price : 0;

                        // Log order details
                        Log.d(TAG, "Order ID: " + orderId);
                        Log.d(TAG, "Shirts: " + shirts);
                        Log.d(TAG, "Pants: " + pants);
                        Log.d(TAG, "Others: " + others);
                        Log.d(TAG, "Total Pieces: " + total);
                        Log.d(TAG, "Total Price: ₹" + price);

                        // Update UI with order details
                        orderIdText.setText("Order ID: " + orderId);
                        shirtQuantity.setText(String.valueOf(shirts));
                        pantsQuantity.setText(String.valueOf(pants));
                        othersQuantity.setText(String.valueOf(others));
                        totalPieces.setText(String.valueOf(total));
                        totalPrice.setText("₹" + price);

                    } else {
                        Log.w(TAG, "No orders found.");
                        Toast.makeText(Thirdwashandfolds.this, "No orders found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Washandfolds node is empty.");
                    Toast.makeText(Thirdwashandfolds.this, "No data available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(Thirdwashandfolds.this, "Failed to retrieve data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void continue2(View view) {
        Intent intent = new Intent(this, Fourwashandfolds.class);
        startActivity(intent);
    }
}
