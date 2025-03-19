// Thirddryclean.java
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

public class Thirddryclean extends AppCompatActivity {

    private static final String TAG = "Thirddryclean";

    private TextView orderIdText, shirtQuantity, pantsQuantity, othersQuantity, totalPieces, totalPrice;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirddryclean);

        Log.d(TAG, "Activity created. Initializing views and Firebase reference.");

        // Initialize TextViews
        orderIdText = findViewById(R.id.orderIdText);
        shirtQuantity = findViewById(R.id.shirtQuantity);
        pantsQuantity = findViewById(R.id.pantsQuantity);
        othersQuantity = findViewById(R.id.othersQuantity);
        totalPieces = findViewById(R.id.totalPieces);
        totalPrice = findViewById(R.id.totalPrice);

        Log.d(TAG, "TextViews initialized.");

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("DryCleanOrders");
        Log.d(TAG, "Firebase reference initialized: DryCleanOrders");

        // Fetch and display the latest order
        fetchLatestOrderDetails();
    }

    private void fetchLatestOrderDetails() {
        Log.d(TAG, "Fetching latest dry clean order details...");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "DataSnapshot received. Checking for existing orders.");

                if (snapshot.exists()) {
                    DataSnapshot latestOrderSnapshot = null;

                    // Iterate to the latest order
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrderSnapshot = orderSnapshot;
                    }

                    if (latestOrderSnapshot != null) {
                        Log.d(TAG, "Latest order found: " + latestOrderSnapshot.getKey());

                        String orderId = latestOrderSnapshot.getKey();
                        int shirts = getValueOrDefault(latestOrderSnapshot, "ShirtQuantity");
                        int pants = getValueOrDefault(latestOrderSnapshot, "PantsQuantity");
                        int others = getValueOrDefault(latestOrderSnapshot, "OthersQuantity");
                        int total = getValueOrDefault(latestOrderSnapshot, "TotalPieces", shirts + pants + others);
                        int price = getValueOrDefault(latestOrderSnapshot, "TotalPrice");

                        Log.d(TAG, String.format("Order Details - ID: %s, Shirts: %d, Pants: %d, Others: %d, Total: %d, Price: ₹%d",
                                orderId, shirts, pants, others, total, price));

                        // Update UI
                        orderIdText.setText("Order ID: " + orderId);
                        shirtQuantity.setText(String.valueOf(shirts));
                        pantsQuantity.setText(String.valueOf(pants));
                        othersQuantity.setText(String.valueOf(others));
                        totalPieces.setText(String.valueOf(total));
                        totalPrice.setText("₹" + price);

                        Log.d(TAG, "UI updated with order details.");
                    } else {
                        showToastAndLog("No dry clean orders found.");
                    }
                } else {
                    showToastAndLog("DryCleanOrders node is empty.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(Thirddryclean.this, "Failed to retrieve data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getValueOrDefault(DataSnapshot snapshot, String key) {
        return getValueOrDefault(snapshot, key, 0);
    }

    private int getValueOrDefault(DataSnapshot snapshot, String key, int defaultValue) {
        Integer value = snapshot.child(key).getValue(Integer.class);
        Log.d(TAG, "Retrieved value for " + key + ": " + (value != null ? value : "null (defaulting to " + defaultValue + ")"));
        return (value != null) ? value : defaultValue;
    }

    private void showToastAndLog(String message) {
        Log.w(TAG, message);
        Toast.makeText(Thirddryclean.this, message, Toast.LENGTH_SHORT).show();
    }

    public void continue2(View view) {
        Log.d(TAG, "Navigating to Fourdryclean activity.");
        Intent intent = new Intent(this, Fourdryclean.class);
        startActivity(intent);
    }
}
