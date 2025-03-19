// Thirdshose.java
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

public class Thirdshose extends AppCompatActivity {

    private static final String TAG = "Thirdshose";

    private TextView orderIdText, sneakersQuantity, leatherShoesQuantity, suedeShoesQuantity, totalPieces, totalPrice;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thirdshose);

        Log.d(TAG, "Activity created. Initializing views and Firebase reference.");

        // Initialize TextViews
        orderIdText = findViewById(R.id.orderIdText);
        sneakersQuantity = findViewById(R.id.sneakersQuantity);
        leatherShoesQuantity = findViewById(R.id.leatherShoesQuantity);
        suedeShoesQuantity = findViewById(R.id.suedeShoesQuantity);
        totalPieces = findViewById(R.id.totalPieces);
        totalPrice = findViewById(R.id.totalPrice);

        Log.d(TAG, "TextViews initialized.");

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("ShoeCleaningOrders");
        Log.d(TAG, "Firebase reference initialized: ShoeCleaningOrders");

        // Fetch and display the latest order
        fetchLatestOrderDetails();
    }

    private void fetchLatestOrderDetails() {
        Log.d(TAG, "Fetching latest shoe cleaning order details...");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "DataSnapshot received. Checking for existing orders.");

                if (snapshot.exists()) {
                    DataSnapshot latestOrderSnapshot = null;

                    // Iterate to find the latest order
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrderSnapshot = orderSnapshot;
                    }

                    if (latestOrderSnapshot != null) {
                        Log.d(TAG, "Latest order found: " + latestOrderSnapshot.getKey());

                        String orderId = latestOrderSnapshot.getKey();
                        int sneakers = getValueOrDefault(latestOrderSnapshot, "SneakersQuantity");
                        int leatherShoes = getValueOrDefault(latestOrderSnapshot, "LeatherShoesQuantity");
                        int suedeShoes = getValueOrDefault(latestOrderSnapshot, "SuedeShoesQuantity");
                        int total = getValueOrDefault(latestOrderSnapshot, "TotalPieces", sneakers + leatherShoes + suedeShoes);
                        int price = getValueOrDefault(latestOrderSnapshot, "TotalPrice");

                        Log.d(TAG, String.format("Order Details - ID: %s, Sneakers: %d, Leather Shoes: %d, Suede Shoes: %d, Total: %d, Price: ₹%d",
                                orderId, sneakers, leatherShoes, suedeShoes, total, price));

                        // Update UI
                        orderIdText.setText("Order ID: " + orderId);
                        sneakersQuantity.setText(String.valueOf(sneakers));
                        leatherShoesQuantity.setText(String.valueOf(leatherShoes));
                        suedeShoesQuantity.setText(String.valueOf(suedeShoes));
                        totalPieces.setText(String.valueOf(total));
                        totalPrice.setText("₹" + price);

                        Log.d(TAG, "UI updated with order details.");
                    } else {
                        showToastAndLog("No shoe cleaning orders found.");
                    }
                } else {
                    showToastAndLog("ShoeCleaningOrders node is empty.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage(), error.toException());
                Toast.makeText(Thirdshose.this, "Failed to retrieve data: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
        Toast.makeText(Thirdshose.this, message, Toast.LENGTH_SHORT).show();
    }

    public void continue2(View view) {
        Log.d(TAG, "Navigating to Fourshose activity.");
        Intent intent = new Intent(this, Fourshose.class);
        startActivity(intent);
    }
}
