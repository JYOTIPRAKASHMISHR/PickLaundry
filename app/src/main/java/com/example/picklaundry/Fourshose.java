// Fourshose.java
package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

public class Fourshose extends AppCompatActivity {

    private static final String TAG = "Fourshose";

    private TextView orderIdText, nameText, emailText, mobileText, addressText, genderText;
    private TextView sneakersQuantity, leatherShoesQuantity, suedeShoesQuantity, totalPieces, totalPrice;

    private DatabaseReference shoesRef, orderAllRef, orderRequestRef;
    private String latestOrderId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourshose);

        initializeViews();

        // ✅ Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        shoesRef = database.getReference("ShoeCleaningOrders");
        orderAllRef = database.getReference("Order_all").child("ShoeCleaningOrders");
        orderRequestRef = database.getReference("Order_request").child("ShoeCleaningOrders");

        Log.d(TAG, "Fetching latest shoe cleaning order details...");
        fetchLatestOrder();
    }

    private void initializeViews() {
        orderIdText = findViewById(R.id.orderIdText);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        mobileText = findViewById(R.id.mobileText);
        addressText = findViewById(R.id.addressText);
        genderText = findViewById(R.id.genderText);
        sneakersQuantity = findViewById(R.id.sneakersQuantity);
        leatherShoesQuantity = findViewById(R.id.leatherShoesQuantity);
        suedeShoesQuantity = findViewById(R.id.suedeShoesQuantity);
        totalPieces = findViewById(R.id.totalPieces);
        totalPrice = findViewById(R.id.totalPrice);
    }

    private void fetchLatestOrder() {
        shoesRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrderId = orderSnapshot.getKey();
                        displayOrderDetails(orderSnapshot);
                        Log.d(TAG, "Latest order fetched: " + latestOrderId);
                    }
                } else {
                    showToast("No orders found.");
                    Log.w(TAG, "No orders in ShoeCleaningOrders.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Database error: " + error.getMessage());
                Log.e(TAG, "Error fetching order: " + error.getMessage());
            }
        });
    }

    private void displayOrderDetails(DataSnapshot orderSnapshot) {
        orderIdText.setText("Order ID: " + latestOrderId);
        nameText.setText(getValue(orderSnapshot, "name"));
        emailText.setText(getValue(orderSnapshot, "email"));
        mobileText.setText(getValue(orderSnapshot, "mobile"));
        addressText.setText(getValue(orderSnapshot, "address"));
        genderText.setText(getValue(orderSnapshot, "gender"));
        sneakersQuantity.setText(getValue(orderSnapshot, "SneakersQuantity"));
        leatherShoesQuantity.setText(getValue(orderSnapshot, "LeatherShoesQuantity"));
        suedeShoesQuantity.setText(getValue(orderSnapshot, "SuedeShoesQuantity"));
        totalPieces.setText(getValue(orderSnapshot, "TotalPieces"));
        totalPrice.setText("₹" + getValue(orderSnapshot, "TotalPrice"));
    }

    private String getValue(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return (value != null) ? value.toString() : "-";
    }

    public void Done1(View view) {
        if (latestOrderId == null) {
            showToast("No order to process.");
            Log.w(TAG, "No valid order ID.");
            return;
        }

        shoesRef.child(latestOrderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot orderSnapshot) {
                if (!orderSnapshot.exists()) {
                    showToast("Order not found.");
                    Log.w(TAG, "Order not found: " + latestOrderId);
                    return;
                }

                Object orderData = orderSnapshot.getValue();

                // ✅ Save to Order_all → PickLaundry → orderId
                orderAllRef.child(latestOrderId).setValue(orderData).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Log.d(TAG, "Order saved to Order_all.");

                        // ✅ Save to Order_request → PickLaundry → orderId
                        orderRequestRef.child(latestOrderId).setValue(orderData).addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                showToast("Order processed successfully.");
                                Log.d(TAG, "Order saved to Order_request.");

                                // ✅ Remove from ShoeCleaningOrders
                                shoesRef.child(latestOrderId).removeValue().addOnSuccessListener(aVoid ->
                                                Log.d(TAG, "Order removed from ShoeCleaningOrders."))
                                        .addOnFailureListener(e ->
                                                Log.e(TAG, "Failed to remove order: " + e.getMessage()));
                            } else {
                                showToast("Failed to save to Order_request.");
                                Log.e(TAG, "Error saving to Order_request.");
                            }
                        });
                    } else {
                        showToast("Failed to save to Order_all.");
                        Log.e(TAG, "Error saving to Order_all.");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Database error: " + error.getMessage());
                Log.e(TAG, "Error during Done1: " + error.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(Fourshose.this, message, Toast.LENGTH_SHORT).show();
    }
}
