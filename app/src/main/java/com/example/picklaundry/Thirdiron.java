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

public class Thirdiron extends AppCompatActivity {

    private static final String TAG = "Thirdiron";

    private TextView orderIdText, shirtQuantity, pantsQuantity, othersQuantity, totalPieces, totalPrice;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirdiron);

        // Initialize TextViews
        orderIdText = findViewById(R.id.orderIdText);
        shirtQuantity = findViewById(R.id.shirtQuantity);
        pantsQuantity = findViewById(R.id.pantsQuantity);
        othersQuantity = findViewById(R.id.othersQuantity);
        totalPieces = findViewById(R.id.totalPieces);
        totalPrice = findViewById(R.id.totalPrice);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("IronOrders");

        // Fetch and display the latest order
        fetchLatestOrderDetails();
    }

    private void fetchLatestOrderDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot latestOrderSnapshot = null;

                    // Get the last order in the "Washandiron" node
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrderSnapshot = orderSnapshot;
                    }

                    if (latestOrderSnapshot != null) {
                        String orderId = latestOrderSnapshot.getKey();
                        int shirts = getValueOrDefault(latestOrderSnapshot, "ShirtQuantity");
                        int pants = getValueOrDefault(latestOrderSnapshot, "PantsQuantity");
                        int others = getValueOrDefault(latestOrderSnapshot, "OthersQuantity");
                        int total = getValueOrDefault(latestOrderSnapshot, "TotalPieces", shirts + pants + others);
                        int price = getValueOrDefault(latestOrderSnapshot, "TotalPrice");

                        // Log fetched data
                        Log.d(TAG, "Order ID: " + orderId);
                        Log.d(TAG, "Shirts: " + shirts);
                        Log.d(TAG, "Pants: " + pants);
                        Log.d(TAG, "Others: " + others);
                        Log.d(TAG, "Total Pieces: " + total);
                        Log.d(TAG, "Total Price: ₹" + price);

                        // Update UI
                        orderIdText.setText("Order ID: " + orderId);
                        shirtQuantity.setText(String.valueOf(shirts));
                        pantsQuantity.setText(String.valueOf(pants));
                        othersQuantity.setText(String.valueOf(others));
                        totalPieces.setText(String.valueOf(total));
                        totalPrice.setText("₹" + price);
                    } else {
                        showToastAndLog("No orders found.");
                    }
                } else {
                    showToastAndLog("Washandiron node is empty.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(Thirdiron.this, "Failed to retrieve data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getValueOrDefault(DataSnapshot snapshot, String key) {
        return getValueOrDefault(snapshot, key, 0);
    }

    private int getValueOrDefault(DataSnapshot snapshot, String key, int defaultValue) {
        Integer value = snapshot.child(key).getValue(Integer.class);
        return (value != null) ? value : defaultValue;
    }

    private void showToastAndLog(String message) {
        Log.w(TAG, message);
        Toast.makeText(Thirdiron.this, message, Toast.LENGTH_SHORT).show();
    }

    public void continue2(View view) {
        Intent intent = new Intent(this, Fouriron.class);
        startActivity(intent);
    }
}
