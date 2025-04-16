package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

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

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Order_request");

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            fetchLatestOrder();

        } else {
            Log.e(TAG, "User not logged in.");
        }
    }

    private void fetchLatestOrder() {
        List<String> categories = Arrays.asList("DryCleanOrders", "IronOrders", "Washandfolds", "Washandiron");

        for (String category : categories) {
            DatabaseReference userCategoryRef = databaseReference.child(currentUserId).child(category);

            userCategoryRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        latestOrderKey = orderId;

                        latestOrderData.clear();
                        latestOrderData.put("category", category);
                        latestOrderData.put("orderId", orderId);
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

                        updateUI();
                        fetchOrderStatus();
                        checkOrderDone();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
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

    private void checkOrderDone() {
        if (latestOrderKey == null) return;

        DatabaseReference orderDoneRef = FirebaseDatabase.getInstance()
                .getReference("order_done").child(latestOrderKey);

        orderDoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // The order is already marked as done
                    tvCancel.setVisibility(View.GONE);
                } else {
                    // The order is not done yet, allow cancel
                    tvCancel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderCheck", "Error checking order_done: " + error.getMessage());
            }
        });
    }

    public void cancel(View view) {
        if (latestOrderKey != null && latestOrderData.containsKey("category")) {
            String category = latestOrderData.get("category").toString();
            String orderId = latestOrderKey;
            String totalPieces = latestOrderData.get("TotalPieces").toString();
            String totalPrice = latestOrderData.get("TotalPrice").toString();
            String mobile = latestOrderData.get("mobile").toString();

            // Prepare the SMS content
            String smsMessage = "This is From PickLaundry. Your Order is canceling. Your category is: " + category +
                    " and your order ID is: " + orderId +
                    ". Total pieces: " + totalPieces +
                    " and Total price: " + totalPrice;

            // Check for SEND_SMS permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
            } else {
                // Permission granted, proceed with SMS sending
                sendSMS(smsMessage, mobile);
            }

            // Animation
            tvCancel.animate().alpha(0f).setDuration(500).withEndAction(() -> {
                Log.d(TAG, "Attempting to remove order from Firebase...");

                // Remove from Order_request
                databaseReference.child(currentUserId).child(category).child(orderId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Order removed successfully.");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to remove order: " + e.getMessage());
                        });
            });
        }
    }

    private void sendSMS(String message, String phoneNumber) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "SMS sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to send SMS: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
}
