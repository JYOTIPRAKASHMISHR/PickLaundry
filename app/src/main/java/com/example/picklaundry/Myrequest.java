package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.*;

public class Myrequest extends AppCompatActivity {
    private static final String TAG = "MyrequestActivity";
    private TextView tvCategory, tvOrderId, tvPants, tvShirts, tvOthers, tvTotalPieces,
            tvTotalPrice, tvAddress, tvEmail, tvGender, tvMobile, tvName, tvCancel;
    private TextView tvIsConfirm, tvIsStored, tvIsDelivered, tvIsOutForDelivery, tvIsOutForPickup, tvIsPaymentDone;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private String currentUserId;
    private String latestOrderKey = null;
    private Map<String, Object> latestOrderData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myrequest);

        // Initialize TextViews
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
        tvCancel = findViewById(R.id.tvCancel);

        tvIsConfirm = findViewById(R.id.tvOrderConfirmed);
        tvIsStored = findViewById(R.id.tvOrderSuccessful);
        tvIsDelivered = findViewById(R.id.tvDelivered);
        tvIsOutForDelivery = findViewById(R.id.tvOutForDelivery);
        tvIsOutForPickup = findViewById(R.id.tvOutForPickup);
        tvIsPaymentDone = findViewById(R.id.tvPaymentSuccessful);

        // Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Order_request");

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            fetchLatestOrder(); // Fetch latest order & then fetch order status
        } else {
            Log.e(TAG, "User not logged in.");
        }
    }

    private void fetchLatestOrder() {
        List<String> categories = Arrays.asList("DryCleanOrders", "IronOrders", "Washandfolds", "Washandiron");

        for (String category : categories) {
            databaseReference.child(category).orderByKey().limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                String userId = orderSnapshot.child("userId").getValue(String.class);

                                if (userId != null && userId.equals(currentUserId)) {
                                    latestOrderKey = orderSnapshot.getKey();
                                    latestOrderData.clear();
                                    latestOrderData.put("category", category);
                                    latestOrderData.put("orderId", latestOrderKey);
                                    latestOrderData.put("PantsQuantity", orderSnapshot.child("PantsQuantity").getValue(Integer.class));
                                    latestOrderData.put("ShirtQuantity", orderSnapshot.child("ShirtQuantity").getValue(Integer.class));
                                    latestOrderData.put("OthersQuantity", orderSnapshot.child("OthersQuantity").getValue(Integer.class));
                                    latestOrderData.put("TotalPieces", orderSnapshot.child("TotalPieces").getValue(Integer.class));
                                    latestOrderData.put("TotalPrice", orderSnapshot.child("TotalPrice").getValue(Integer.class));
                                    latestOrderData.put("address", orderSnapshot.child("address").getValue(String.class));
                                    latestOrderData.put("email", orderSnapshot.child("email").getValue(String.class));
                                    latestOrderData.put("gender", orderSnapshot.child("gender").getValue(String.class));
                                    latestOrderData.put("mobile", orderSnapshot.child("mobile").getValue(String.class));
                                    latestOrderData.put("name", orderSnapshot.child("name").getValue(String.class));
                                }
                            }

                            if (latestOrderKey != null) {
                                updateUI();
                                fetchOrderStatus(); // Fetch order status after setting latestOrderKey
                            }
                        }

                        private void updateUI() {
                            if (latestOrderData.isEmpty()) return;

                            tvCategory.setText("Category: " + latestOrderData.get("category"));
                            tvOrderId.setText("Order ID: " + latestOrderData.get("orderId"));
                            tvPants.setText("Pants: " + latestOrderData.get("PantsQuantity"));
                            tvShirts.setText("Shirts: " + latestOrderData.get("ShirtQuantity"));
                            tvOthers.setText("Others: " + latestOrderData.get("OthersQuantity"));
                            tvTotalPieces.setText("Total Pieces: " + latestOrderData.get("TotalPieces"));
                            tvTotalPrice.setText("Total Price: ₹" + latestOrderData.get("TotalPrice"));
                            tvAddress.setText("Address: " + latestOrderData.get("address"));
                            tvEmail.setText("Email: " + latestOrderData.get("email"));
                            tvGender.setText("Gender: " + latestOrderData.get("gender"));
                            tvMobile.setText("Mobile: " + latestOrderData.get("mobile"));
                            tvName.setText("Name: " + latestOrderData.get("name"));
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                        }
                    });
        }
    }

    private void fetchOrderStatus() {
        if (latestOrderKey == null) return;

        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus").child(latestOrderKey);
        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isConfirmed = snapshot.child("isConfirm").getValue(Boolean.class) != null ? snapshot.child("isConfirm").getValue(Boolean.class) : false;
                boolean isStored = snapshot.child("isStored").getValue(Boolean.class) != null ? snapshot.child("isStored").getValue(Boolean.class) : false;
                boolean isOutForPickup = snapshot.child("isoutforpickup").getValue(Boolean.class) != null ? snapshot.child("isoutforpickup").getValue(Boolean.class) : false;
                boolean isOutForDelivery = snapshot.child("isoutfordelivery").getValue(Boolean.class) != null ? snapshot.child("isoutfordelivery").getValue(Boolean.class) : false;
                boolean isDelivered = snapshot.child("isdelivered").getValue(Boolean.class) != null ? snapshot.child("isdelivered").getValue(Boolean.class) : false;
                boolean isPaymentDone = snapshot.child("ispaymentdone").getValue(Boolean.class) != null ? snapshot.child("ispaymentdone").getValue(Boolean.class) : false;

                updateStatusUI(isConfirmed, isStored, isOutForPickup, isOutForDelivery, isDelivered, isPaymentDone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void updateStatusUI(boolean isConfirmed, boolean isStored, boolean isOutForPickup, boolean isOutForDelivery, boolean isDelivered, boolean isPaymentDone) {
        tvIsConfirm.setText(isConfirmed ? "✅ Order Confirmed" : "❌ Order Not Confirmed");
        tvIsStored.setText(isStored ? "✅ Order Stored" : "❌ Order Not Stored");
        tvIsOutForPickup.setText(isOutForPickup ? "🚚 Out for Pickup" : "❌ Not Picked Up Yet");
        tvIsOutForDelivery.setText(isOutForDelivery ? "🚛 Out for Delivery" : "❌ Not Out for Delivery");
        tvIsDelivered.setText(isDelivered ? "✅ Delivered" : "❌ Not Delivered Yet");
        tvIsPaymentDone.setText(isPaymentDone ? "💵 Payment Successful" : "❌ Payment Pending");
    }
}
