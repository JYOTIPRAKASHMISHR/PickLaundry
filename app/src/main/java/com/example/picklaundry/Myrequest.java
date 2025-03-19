package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Myrequest extends AppCompatActivity {

    private static final String TAG = "MyrequestActivity";

    private TextView tvCategory, tvOrderId, tvPants, tvShirts, tvOthers, tvTotalPieces,
            tvTotalPrice, tvAddress, tvEmail, tvGender, tvMobile, tvName, tvCancel;
    private TextView tvIsConfirm, tvIsStored, tvIsDelivered, tvIsOutForDelivery, tvIsOutForPickup, tvIsPaymentDone;


    private DatabaseReference databaseReference,statusRef;
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

        // Initialize TextViews for Order Status
        tvIsConfirm = findViewById(R.id.tvOrderConfirmed);
        tvIsStored = findViewById(R.id.tvOrderSuccessful);
        tvIsDelivered = findViewById(R.id.tvDelivered);
        tvIsOutForDelivery = findViewById(R.id.tvOutForDelivery);
        tvIsOutForPickup = findViewById(R.id.tvOutForPickup);
        tvIsPaymentDone = findViewById(R.id.tvPaymentSuccessful);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Order_request");
//        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus").child(orderId);



        // Get current user ID
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            fetchLatestOrder();
            fetchOrderStatus();

        } else {
            Log.e(TAG, "User not logged in.");
        }
    }

    private void fetchOrderStatus() {
        if (latestOrderKey == null) {
            Log.e(TAG, "No latest order found.");
            return;
        }

        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus").child(latestOrderKey);

        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isConfirmed = snapshot.child("isConfirmed").getValue(Boolean.class) != null ? snapshot.child("isConfirmed").getValue(Boolean.class) : false;
                    boolean isStored = snapshot.child("isStored").getValue(Boolean.class) != null ? snapshot.child("isStored").getValue(Boolean.class) : false;
                    boolean isOutForPickup = snapshot.child("isOutForPickup").getValue(Boolean.class) != null ? snapshot.child("isOutForPickup").getValue(Boolean.class) : false;
                    boolean isOutForDelivery = snapshot.child("isOutForDelivery").getValue(Boolean.class) != null ? snapshot.child("isOutForDelivery").getValue(Boolean.class) : false;
                    boolean isDelivered = snapshot.child("isDelivered").getValue(Boolean.class) != null ? snapshot.child("isDelivered").getValue(Boolean.class) : false;
                    boolean isPaymentDone = snapshot.child("isPaymentDone").getValue(Boolean.class) != null ? snapshot.child("isPaymentDone").getValue(Boolean.class) : false;

                    updateStatusUI(isConfirmed, isStored, isOutForPickup, isOutForDelivery, isDelivered, isPaymentDone);
                } else {
                    Log.e(TAG, "Order status not found.");
                }
            }

            private void updateStatusUI(boolean isConfirmed, boolean isStored, boolean isOutForPickup, boolean isOutForDelivery, boolean isDelivered, boolean isPaymentDone) {
                runOnUiThread(() -> {
                    tvIsConfirm.setText(isConfirmed ? "✅ Order Confirmed" : "❌ Order Not Confirmed");
                    tvIsConfirm.setTextColor(isConfirmed ? Color.GREEN : Color.RED);

                    tvIsStored.setText(isStored ? "✅ Order Stored" : "❌ Order Not Stored");
                    tvIsStored.setTextColor(isStored ? Color.GREEN : Color.RED);

                    tvIsOutForPickup.setText(isOutForPickup ? "🚚 Out for Pickup" : "❌ Not Picked Up Yet");
                    tvIsOutForPickup.setTextColor(isOutForPickup ? Color.BLUE : Color.RED);

                    tvIsOutForDelivery.setText(isOutForDelivery ? "🚛 Out for Delivery" : "❌ Not Out for Delivery");
                    tvIsOutForDelivery.setTextColor(isOutForDelivery ? Color.BLUE : Color.RED);

                    tvIsDelivered.setText(isDelivered ? "✅ Delivered" : "❌ Not Delivered Yet");
                    tvIsDelivered.setTextColor(isDelivered ? Color.GREEN : Color.RED);

                    tvIsPaymentDone.setText(isPaymentDone ? "💵 Payment Successful" : "❌ Payment Pending");
                    tvIsPaymentDone.setTextColor(isPaymentDone ? Color.GREEN : Color.RED);
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }


    private void fetchLatestOrder() {
        List<String> categories = Arrays.asList("DryCleanOrders", "IronOrders", "Washandfolds", "Washandiron");

        for (String category : categories) {
            Log.d(TAG, "Checking category: " + category);

            databaseReference.child(category).orderByKey().limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                String userId = orderSnapshot.child("userId").getValue(String.class);

                                if (userId != null && userId.equals(currentUserId)) {
                                    String orderId = orderSnapshot.getKey();

                                    if (latestOrderKey == null || orderId.compareTo(latestOrderKey) > 0) {
                                        latestOrderKey = orderId;
                                        latestOrderData.clear();
                                        latestOrderData.put("category", category);
                                        latestOrderData.put("orderId", orderId);
                                        latestOrderData.put("PantsQuantity", orderSnapshot.child("PantsQuantity").getValue(Integer.class) != null ? orderSnapshot.child("PantsQuantity").getValue(Integer.class) : 0);
                                        latestOrderData.put("ShirtQuantity", orderSnapshot.child("ShirtQuantity").getValue(Integer.class) != null ? orderSnapshot.child("ShirtQuantity").getValue(Integer.class) : 0);
                                        latestOrderData.put("OthersQuantity", orderSnapshot.child("OthersQuantity").getValue(Integer.class) != null ? orderSnapshot.child("OthersQuantity").getValue(Integer.class) : 0);
                                        latestOrderData.put("TotalPieces", orderSnapshot.child("TotalPieces").getValue(Integer.class) != null ? orderSnapshot.child("TotalPieces").getValue(Integer.class) : 0);
                                        latestOrderData.put("TotalPrice", orderSnapshot.child("TotalPrice").getValue(Integer.class) != null ? orderSnapshot.child("TotalPrice").getValue(Integer.class) : 0);
                                        latestOrderData.put("address", orderSnapshot.child("address").getValue(String.class));
                                        latestOrderData.put("email", orderSnapshot.child("email").getValue(String.class));
                                        latestOrderData.put("gender", orderSnapshot.child("gender").getValue(String.class));
                                        latestOrderData.put("mobile", orderSnapshot.child("mobile").getValue(String.class));
                                        latestOrderData.put("name", orderSnapshot.child("name").getValue(String.class));
                                    }
                                }
                            }

                            // After checking all categories, update UI
                            if (latestOrderKey != null) {
                                updateUI();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "⚠ Database error: " + error.getMessage());
                        }
                    });
        }
    }

    private void updateUI() {
        runOnUiThread(() -> {
            tvCategory.setText("Category: " + latestOrderData.get("category"));
            tvOrderId.setText("📦 Order ID: " + latestOrderData.get("orderId"));
            tvPants.setText("👖 Pants: " + latestOrderData.get("PantsQuantity"));
            tvShirts.setText("👕 Shirts: " + latestOrderData.get("ShirtQuantity"));
            tvOthers.setText("🧥 Others: " + latestOrderData.get("OthersQuantity"));
            tvTotalPieces.setText("📊 Total Pieces: " + latestOrderData.get("TotalPieces"));
            tvTotalPrice.setText("💰 Total Price: ₹" + latestOrderData.get("TotalPrice"));
            tvAddress.setText("📍 Address:\n" + latestOrderData.get("address"));
            tvEmail.setText("📧 Email: " + latestOrderData.get("email"));
            tvGender.setText("👨 Gender: " + latestOrderData.get("gender"));
            tvMobile.setText("📞 Mobile: " + latestOrderData.get("mobile"));
            tvName.setText("🙋 Name: " + latestOrderData.get("name"));
        });
    }

    public void cancel(View view) {
        if (latestOrderKey != null && latestOrderData.get("category") != null) {
            String category = latestOrderData.get("category").toString();

            // Reference to the order in both nodes
            DatabaseReference orderRequestRef = FirebaseDatabase.getInstance()
                    .getReference("Order_request").child(category).child(latestOrderKey);
            DatabaseReference orderAllRef = FirebaseDatabase.getInstance()
                    .getReference("Order_all").child(category).child(latestOrderKey);

            // Delete order from both locations
            orderRequestRef.removeValue().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    orderAllRef.removeValue().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            Log.d(TAG, "✅ Order deleted successfully from both nodes");
                            tvCategory.setText("Order Canceled");
                            tvOrderId.setText("");
                            tvPants.setText("");
                            tvShirts.setText("");
                            tvOthers.setText("");
                            tvTotalPieces.setText("");
                            tvTotalPrice.setText("");
                            tvAddress.setText("");
                            tvEmail.setText("");
                            tvGender.setText("");
                            tvMobile.setText("");
                            tvName.setText("");
                        } else {
                            Log.e(TAG, "❌ Failed to delete order from Order_all");
                        }
                    });
                } else {
                    Log.e(TAG, "❌ Failed to delete order from Order_request");
                }
            });
        } else {
            Log.e(TAG, "❌ No order found to cancel.");
        }
    }
//    private void fetchOrderStatus(String orderId) {
//        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus").child(orderId);
//
//        statusRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                updateStatus(tvIsConfirm, snapshot.child("isConfirmed"));
//                updateStatus(tvIsStored, snapshot.child("isStored"));
//                updateStatus(tvIsDelivered, snapshot.child("isDelivered"));
//                updateStatus(tvIsOutForDelivery, snapshot.child("isOutForDelivery"));
//                updateStatus(tvIsOutForPickup, snapshot.child("isOutForPickup"));
//                updateStatus(tvIsPaymentDone, snapshot.child("isPaymentDone"));
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to fetch order status: " + error.getMessage());
//            }
//        });
//    }

//    private void updateStatus(TextView textView, DataSnapshot snapshot) {
//        if (snapshot.exists() && snapshot.getValue(Boolean.class) != null && snapshot.getValue(Boolean.class)) {
//            textView.setTextColor(Color.GREEN);
//        } else {
//            textView.setTextColor(Color.RED);
//        }
//    }
}