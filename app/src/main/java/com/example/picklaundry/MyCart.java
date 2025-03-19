package com.example.picklaundry;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyCart extends AppCompatActivity {
    private static final String TAG = "MyCartActivity";

    private TextView tvCategory, tvOrderId, tvPants, tvShirts, tvOthers, tvTotalPieces, tvTotalPrice, tvAddress, tvEmail, tvGender, tvMobile, tvName;
    private TextView tvIsConfirm, tvIsStored, tvIsDelivered, tvIsOutForDelivery, tvIsOutForPickup, tvIsPaymentDone;

    private DatabaseReference orderRef, statusRef;
    private String orderId, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycart);

        Log.d(TAG, "Initializing UI components...");

        // Initialize TextViews for Order Details
        tvCategory = findViewById(R.id.tvCategory);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvPants = findViewById(R.id.tvPants);
        tvShirts = findViewById(R.id.tvShirts);
        tvOthers = findViewById(R.id.tvOthers);
        tvTotalPieces = findViewById(R.id.tvTotalPieces);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvAddress = findViewById(R.id.tvAddress);
        tvEmail = findViewById(R.id.tvEmail);
        tvGender = findViewById(R.id.tvGender);
        tvMobile = findViewById(R.id.tvMobile);
        tvName = findViewById(R.id.tvName);

        // Initialize TextViews for Order Status
        tvIsConfirm = findViewById(R.id.tvOrderConfirmed);
        tvIsStored = findViewById(R.id.tvOrderSuccessful);
        tvIsDelivered = findViewById(R.id.tvDelivered);
        tvIsOutForDelivery = findViewById(R.id.tvOutForDelivery);
        tvIsOutForPickup = findViewById(R.id.tvOutForPickup);
        tvIsPaymentDone = findViewById(R.id.tvPaymentSuccessful);

        // Get Intent data
        orderId = getIntent().getStringExtra("orderId");
        category = getIntent().getStringExtra("category");

        if (orderId == null || category == null) {
            Log.e(TAG, "Error: Order ID or Category is NULL! Exiting...");
            return;
        }

        Log.d(TAG, "Received Order ID: " + orderId);
        Log.d(TAG, "Received Category: " + category);

        // Get Firebase references
        orderRef = FirebaseDatabase.getInstance().getReference("Order_request").child(category).child(orderId);
        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus").child(orderId);

        // Load data from Firebase
        loadOrderDetails();
        loadOrderStatus();
    }

    // Load Order Details from Firebase
    private void loadOrderDetails() {
        Log.d(TAG, "Fetching order details from Firebase...");

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "Error: Order ID " + orderId + " not found in Order_request!");
                    return;
                }

                Log.d(TAG, "Order details found. Updating UI...");

                Integer othersQuantity = dataSnapshot.child("OthersQuantity").getValue(Integer.class);
                Integer pantsQuantity = dataSnapshot.child("PantsQuantity").getValue(Integer.class);
                Integer shirtsQuantity = dataSnapshot.child("ShirtQuantity").getValue(Integer.class);
                Integer totalPieces = dataSnapshot.child("TotalPieces").getValue(Integer.class);
                Integer totalPrice = dataSnapshot.child("TotalPrice").getValue(Integer.class);
                String address = dataSnapshot.child("address").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String gender = dataSnapshot.child("gender").getValue(String.class);
                String mobile = dataSnapshot.child("mobile").getValue(String.class);
                String name = dataSnapshot.child("name").getValue(String.class);

                // Update UI
                tvCategory.setText("📌 Category: " + category);
                tvOrderId.setText("📦 Order ID: " + orderId);
                tvPants.setText("👔 Pants: " + (pantsQuantity != null ? pantsQuantity : 0));
                tvShirts.setText("👕 Shirts: " + (shirtsQuantity != null ? shirtsQuantity : 0));
                tvOthers.setText("🧥 Others: " + (othersQuantity != null ? othersQuantity : 0));
                tvTotalPieces.setText("📊 Total Pieces: " + (totalPieces != null ? totalPieces : 0));
                tvTotalPrice.setText("💰 Total Price: ₹" + (totalPrice != null ? totalPrice : 0));
                tvAddress.setText("📍 Address: \n" + (address != null ? address : "N/A"));
                tvEmail.setText("📧 Email: " + (email != null ? email : "N/A"));
                tvGender.setText("👨 Gender: " + (gender != null ? gender : "N/A"));
                tvMobile.setText("📞 Mobile: " + (mobile != null ? mobile : "N/A"));
                tvName.setText("🙋 Name: " + (name != null ? name : "N/A"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Load Order Status from Firebase
    private void loadOrderStatus() {
        Log.d(TAG, "Fetching order status from Firebase...");

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.e(TAG, "Error: Order ID " + orderId + " not found in OrderStatus!");
                    return;
                }

                Log.d(TAG, "Order status found. Updating UI...");

                setStatusColor(tvIsConfirm, dataSnapshot.child("isConfirm").getValue(Boolean.class));
                setStatusColor(tvIsStored, dataSnapshot.child("isStored").getValue(Boolean.class));
                setStatusColor(tvIsDelivered, dataSnapshot.child("isdelivered").getValue(Boolean.class));
                setStatusColor(tvIsOutForDelivery, dataSnapshot.child("isoutfordelivery").getValue(Boolean.class));
                setStatusColor(tvIsOutForPickup, dataSnapshot.child("isoutforpickup").getValue(Boolean.class));
                setStatusColor(tvIsPaymentDone, dataSnapshot.child("ispaymentdone").getValue(Boolean.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    // Helper method to set text and color dynamically
    private void setStatusColor(TextView textView, Boolean status) {
        String fieldName = textView.getText().toString().split(":")[0];

        if (status != null && status) {
            textView.setText("✅ " + fieldName + ": Yes");
            textView.setTextColor(Color.GREEN);
            Log.d(TAG, fieldName + " is TRUE (✅)");
        } else {
            textView.setText("❌ " + fieldName + ": No");
            textView.setTextColor(Color.RED);
            Log.d(TAG, fieldName + " is FALSE (❌)");
        }
    }
}
