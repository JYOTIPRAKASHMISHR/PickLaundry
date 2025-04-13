package com.example.picklaundry;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class MyCart extends AppCompatActivity {
    private static final String TAG = "MyCart";

    // TextViews
    private TextView tvCategory, tvOrderId, tvPants, tvShirts, tvOthers, tvTotalPieces, tvTotalPrice, tvAddress, tvEmail, tvGender, tvMobile, tvName;
    private TextView tvIsConfirm, tvIsStored, tvIsDelivered, tvIsOutForDelivery, tvIsOutForPickup, tvIsPaymentDone;

    // Firebase references
    private DatabaseReference orderRef, statusRef;

    // Intent extras
    private String userId, orderId, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycart);

        Log.d(TAG, "onCreate: Initializing...");

        // Initialize views
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

        tvIsConfirm = findViewById(R.id.tvOrderConfirmed);
        tvIsStored = findViewById(R.id.tvOrderSuccessful);
        tvIsDelivered = findViewById(R.id.tvDelivered);
        tvIsOutForDelivery = findViewById(R.id.tvOutForDelivery);
        tvIsOutForPickup = findViewById(R.id.tvOutForPickup);
        tvIsPaymentDone = findViewById(R.id.tvPaymentSuccessful);

        // Get Intent data
        userId = getIntent().getStringExtra("userId");
        orderId = getIntent().getStringExtra("orderId");
        category = getIntent().getStringExtra("category");

        Log.d(TAG, "Received userId: " + userId);
        Log.d(TAG, "Received orderId: " + orderId);
        Log.d(TAG, "Received category: " + category);

        if (userId == null || orderId == null || category == null) {
            Log.e(TAG, "Missing intent data!");
            return;
        }

        // Firebase references
        orderRef = FirebaseDatabase.getInstance().getReference("Order_request")
                .child(userId)
                .child(category)
                .child(orderId);

        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus")
                .child(orderId);

        loadOrderDetails();
        loadOrderStatus();
    }

    private void loadOrderDetails() {
        Log.d(TAG, "Fetching order details from: " + orderRef.toString());

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Data retrieved for order details");

                if (!snapshot.exists()) {
                    Log.e(TAG, "Order details not found at given path.");
                    return;
                }

                Integer othersQuantity = snapshot.child("OthersQuantity").getValue(Integer.class);
                Integer pantsQuantity = snapshot.child("PantsQuantity").getValue(Integer.class);
                Integer shirtsQuantity = snapshot.child("ShirtQuantity").getValue(Integer.class);
                Integer totalPieces = snapshot.child("TotalPieces").getValue(Integer.class);
                Integer totalPrice = snapshot.child("TotalPrice").getValue(Integer.class);
                String address = snapshot.child("address").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String gender = snapshot.child("gender").getValue(String.class);
                String mobile = snapshot.child("mobile").getValue(String.class);
                String name = snapshot.child("name").getValue(String.class);

                Log.d(TAG, "Order Details Loaded: "
                        + "\nOthers: " + othersQuantity
                        + "\nPants: " + pantsQuantity
                        + "\nShirts: " + shirtsQuantity
                        + "\nTotalPieces: " + totalPieces
                        + "\nTotalPrice: " + totalPrice);

                tvCategory.setText("📌 Category: " + category);
                tvOrderId.setText("📦 Order ID: " + orderId);
                tvPants.setText("👖 Pants: " + (pantsQuantity != null ? pantsQuantity : 0));
                tvShirts.setText("👕 Shirts: " + (shirtsQuantity != null ? shirtsQuantity : 0));
                tvOthers.setText("🧥 Others: " + (othersQuantity != null ? othersQuantity : 0));
                tvTotalPieces.setText("📊 Total Pieces: " + (totalPieces != null ? totalPieces : 0));
                tvTotalPrice.setText("💰 Total Price: ₹" + (totalPrice != null ? totalPrice : 0));
                tvAddress.setText("📍 Address:\n" + (address != null ? address : "N/A"));
                tvEmail.setText("📧 Email: " + (email != null ? email : "N/A"));
                tvGender.setText("👤 Gender: " + (gender != null ? gender : "N/A"));
                tvMobile.setText("📱 Mobile: " + (mobile != null ? mobile : "N/A"));
                tvName.setText("🙋 Name: " + (name != null ? name : "N/A"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Order details loading cancelled: " + error.getMessage());
            }
        });
    }

    private void loadOrderStatus() {
        Log.d(TAG, "Fetching order status from: " + statusRef.toString());

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Order status snapshot received");

                if (!snapshot.exists()) {
                    Log.e(TAG, "Order status not found at path.");
                    return;
                }

                setStatusColor(tvIsConfirm, snapshot.child("isConfirm").getValue(Boolean.class));
                setStatusColor(tvIsStored, snapshot.child("isStored").getValue(Boolean.class));
                setStatusColor(tvIsDelivered, snapshot.child("isdelivered").getValue(Boolean.class));
                setStatusColor(tvIsOutForDelivery, snapshot.child("isoutfordelivery").getValue(Boolean.class));
                setStatusColor(tvIsOutForPickup, snapshot.child("isoutforpickup").getValue(Boolean.class));
                setStatusColor(tvIsPaymentDone, snapshot.child("ispaymentdone").getValue(Boolean.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Order status loading cancelled: " + error.getMessage());
            }
        });
    }

    private void setStatusColor(TextView textView, Boolean status) {
        String label = textView.getText().toString().split(":")[0];

        if (status != null && status) {
            textView.setText("✅ " + label + ": Yes");
            textView.setTextColor(Color.GREEN);
            Log.d(TAG, label + " → TRUE ✅");
        } else {
            textView.setText("❌ " + label + ": No");
            textView.setTextColor(Color.RED);
            Log.d(TAG, label + " → FALSE ❌");
        }
    }
}
