package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.*;

public class Myrequest extends AppCompatActivity {
    private static final String TAG = "MyrequestActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;

    private TextView tvCategory, tvOrderId, tvPants, tvShirts, tvOthers, tvTotalPieces,
            tvSneakers, tvLeatherShoes, tvSuedeShoes, tvTotalPrice, tvAddress, tvEmail,
            tvGender, tvMobile, tvName, tvCancel, tvIsConfirm, tvIsStored, tvIsDelivered,
            tvIsOutForDelivery, tvIsOutForPickup, tvIsPaymentDone;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String currentUserId;
    private String latestOrderKey = null;
    private Map<String, Object> latestOrderData = new HashMap<>();
    private String smsMessage = "", recipientMobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myrequest);

        initializeViews();

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Order_request");

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            fetchLatestOrder();
        } else {
            Log.e(TAG, "User not logged in.");
        }
    }

    private void initializeViews() {
        tvCategory = findViewById(R.id.tvCategory);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvPants = findViewById(R.id.tvPants);
        tvShirts = findViewById(R.id.tvShirts);
        tvOthers = findViewById(R.id.tvOthers);
        tvSneakers = findViewById(R.id.tvSneakers);
        tvLeatherShoes = findViewById(R.id.tvLeatherShoes);
        tvSuedeShoes = findViewById(R.id.tvSuedeShoes);
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
    }

    private void fetchLatestOrder() {
        List<String> categories = Arrays.asList("DryCleanOrders", "IronOrders", "Washandfolds", "Washandiron", "ShoeCleaningOrders");


        for (String category : categories) {
            DatabaseReference userCategoryRef = databaseReference.child(currentUserId).child(category);
            userCategoryRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        latestOrderKey = orderSnapshot.getKey();
                        latestOrderData.clear();

                        latestOrderData.put("category", category);
                        latestOrderData.put("orderId", latestOrderKey);
                        latestOrderData.put("PantsQuantity", orderSnapshot.child("PantsQuantity").getValue(Integer.class));
                        latestOrderData.put("ShirtQuantity", orderSnapshot.child("ShirtQuantity").getValue(Integer.class));
                        latestOrderData.put("OthersQuantity", orderSnapshot.child("OthersQuantity").getValue(Integer.class));
                        latestOrderData.put("Sneakers", orderSnapshot.child("SneakersQuantity").getValue(Integer.class));
                        latestOrderData.put("LeatherShoes", orderSnapshot.child("LeatherShoesQuantity").getValue(Integer.class));
                        latestOrderData.put("SuedeShoes", orderSnapshot.child("SuedeShoesQuantity").getValue(Integer.class));
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
        tvSneakers.setText("Sneakers: " + latestOrderData.get("Sneakers"));
        tvLeatherShoes.setText("Leather Shoes: " + latestOrderData.get("LeatherShoes"));
        tvSuedeShoes.setText("Suede Shoes: " + latestOrderData.get("SuedeShoes"));
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
                boolean isConfirmed = snapshot.child("isConfirm").getValue(Boolean.class) != null && snapshot.child("isConfirm").getValue(Boolean.class);
                boolean isStored = snapshot.child("isStored").getValue(Boolean.class) != null && snapshot.child("isStored").getValue(Boolean.class);
                boolean isOutForPickup = snapshot.child("isoutforpickup").getValue(Boolean.class) != null && snapshot.child("isoutforpickup").getValue(Boolean.class);
                boolean isOutForDelivery = snapshot.child("isoutfordelivery").getValue(Boolean.class) != null && snapshot.child("isoutfordelivery").getValue(Boolean.class);
                boolean isDelivered = snapshot.child("isdelivered").getValue(Boolean.class) != null && snapshot.child("isdelivered").getValue(Boolean.class);
                boolean isPaymentDone = snapshot.child("ispaymentdone").getValue(Boolean.class) != null && snapshot.child("ispaymentdone").getValue(Boolean.class);

                updateStatusUI(isConfirmed, isStored, isOutForPickup, isOutForDelivery, isDelivered, isPaymentDone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Status fetch error: " + error.getMessage());
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

        DatabaseReference orderDoneRef = FirebaseDatabase.getInstance().getReference("order_done").child(latestOrderKey);
        orderDoneRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvCancel.setVisibility(snapshot.exists() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking order_done: " + error.getMessage());
            }
        });
    }

    public void cancel(View view) {
        if (latestOrderKey != null && latestOrderData.containsKey("category")) {
            String category = latestOrderData.get("category").toString();
            String orderId = latestOrderKey;
            String totalPieces = String.valueOf(latestOrderData.get("TotalPieces"));
            String totalPrice = String.valueOf(latestOrderData.get("TotalPrice"));
            recipientMobile = latestOrderData.get("mobile").toString();

            smsMessage = "This is from PickLaundry. Your order has been cancelled.\nCategory: " + category +
                    "\nOrder ID: " + orderId +
                    "\nTotal Pieces: " + totalPieces +
                    "\nTotal Price: ₹" + totalPrice;

            // Delete order
            FirebaseDatabase.getInstance().getReference("Order_request")
                    .child(currentUserId).child(category).child(orderId).removeValue();

            // Send SMS
            sendSMS();
        }
    }

    private void sendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(recipientMobile, null, smsMessage, null, null);
            Toast.makeText(this, "Cancellation SMS sent!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSMS();
        } else {
            Toast.makeText(this, "SMS permission denied. Cannot send cancellation message.", Toast.LENGTH_SHORT).show();
        }
    }
}
