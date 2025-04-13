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

public class Fouriron extends AppCompatActivity {

    private static final String TAG = "Fouriron";

    private TextView orderIdText, nameText, emailText, mobileText, addressText, genderText;
    private TextView shirtQuantity, pantsQuantity, othersQuantity, totalPieces, totalPrice;

    private DatabaseReference ironRef, orderAllRef, orderRequestRef;
    private String latestOrderId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fouriron);

        initializeViews();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ironRef = database.getReference("IronOrders");
        orderAllRef = database.getReference("Order_all");
        orderRequestRef = database.getReference("Order_request");

        Log.d(TAG, "Fetching order details...");
        fetchOrderDetails();
    }

    private void initializeViews() {
        orderIdText = findViewById(R.id.orderIdText);
        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);
        mobileText = findViewById(R.id.mobileText);
        addressText = findViewById(R.id.addressText);
        genderText = findViewById(R.id.genderText);
        shirtQuantity = findViewById(R.id.shirtQuantity);
        pantsQuantity = findViewById(R.id.pantsQuantity);
        othersQuantity = findViewById(R.id.othersQuantity);
        totalPieces = findViewById(R.id.totalPieces);
        totalPrice = findViewById(R.id.totalPrice);
    }

    private void fetchOrderDetails() {
        ironRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot latestOrder = null;
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrder = orderSnapshot;  // get last (latest) order
                    }

                    if (latestOrder != null) {
                        latestOrderId = latestOrder.getKey();
                        displayOrderDetails(latestOrder);
                    } else {
                        showToast("No valid orders found.");
                    }
                } else {
                    showToast("No orders found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Database Error: " + error.getMessage());
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
        shirtQuantity.setText(getValue(orderSnapshot, "ShirtQuantity"));
        pantsQuantity.setText(getValue(orderSnapshot, "PantsQuantity"));
        othersQuantity.setText(getValue(orderSnapshot, "OthersQuantity"));
        totalPieces.setText(getValue(orderSnapshot, "TotalPieces"));
        totalPrice.setText("₹" + getValue(orderSnapshot, "TotalPrice"));
    }

    private String getValue(DataSnapshot snapshot, String key) {
        Object value = snapshot.child(key).getValue();
        return (value != null) ? value.toString() : "-";
    }

    private void showToast(String message) {
        Toast.makeText(Fouriron.this, message, Toast.LENGTH_SHORT).show();
    }

    public void Done1(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        if (latestOrderId == null) {
            showToast("No order to process.");
            return;
        }

        ironRef.child(latestOrderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot orderSnapshot) {
                if (orderSnapshot.exists()) {
                    Object orderData = orderSnapshot.getValue();
                    String userId = orderSnapshot.child("userId").getValue(String.class);

                    if (userId == null || userId.isEmpty()) {
                        showToast("User ID missing in order data.");
                        return;
                    }

                    DatabaseReference userOrderAllRef = orderAllRef.child(userId).child("IronOrders").child(latestOrderId);
                    DatabaseReference userOrderRequestRef = orderRequestRef.child(userId).child("IronOrders").child(latestOrderId);

                    userOrderAllRef.setValue(orderData).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d(TAG, "Order saved to Order_all → userId → IronOrders.");

                            userOrderRequestRef.setValue(orderData).addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful()) {
                                    showToast("Order saved to both Order_all and Order_request.");

                                    ironRef.child(latestOrderId).removeValue()
                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Order removed from IronOrders."))
                                            .addOnFailureListener(e -> Log.e(TAG, "Failed to remove order: " + e.getMessage()));
                                } else {
                                    showToast("Saved to Order_all but failed in Order_request.");
                                }
                            });

                        } else {
                            showToast("Failed to save to Order_all.");
                        }
                    });

                } else {
                    showToast("Order not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error: " + error.getMessage());
            }
        });
    }
}
