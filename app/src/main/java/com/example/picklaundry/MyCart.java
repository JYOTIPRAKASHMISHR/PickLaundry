package com.example.picklaundry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.*;

public class MyCart extends AppCompatActivity {
    private static final String TAG = "MyCart";
    private static final int SMS_PERMISSION_REQUEST = 101;

    // TextViews
    private TextView tvCategory, tvOrderId, tvPants, tvShirts, tvOthers,tvSneakers, tvLeatherShoes,tvSuedeShoes, tvTotalPieces, tvTotalPrice, tvAddress, tvEmail, tvGender, tvMobile, tvName;
    private TextView tvIsConfirm, tvIsStored, tvIsDelivered, tvIsOutForDelivery, tvIsOutForPickup, tvIsPaymentDone;

    private DatabaseReference orderRef, statusRef, userRef, pickupBoyRef,deliveryBoyRef;

    private String userId, orderId, category;
    private String userMobile;

    // Flags to track sent SMS
    private boolean smsConfirmSent = false;
    private boolean smsStoredSent = false;
    private boolean smsPickupSent = false;
    private boolean smsDeliverySent = false;
    private boolean smsDeliveredSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycart);

        // Request SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        }

        // Initialize views
        tvCategory = findViewById(R.id.tvCategory);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvPants = findViewById(R.id.tvPants);
        tvShirts = findViewById(R.id.tvShirts);
        tvOthers = findViewById(R.id.tvOthers);
        tvSneakers=findViewById(R.id.tvSneakers);
        tvLeatherShoes=findViewById(R.id.tvLeatherShoes);
        tvSuedeShoes=findViewById(R.id.tvSuedeShoes);
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

        // Get intent extras
        userId = getIntent().getStringExtra("userId");
        orderId = getIntent().getStringExtra("orderId");
        category = getIntent().getStringExtra("category");

        if (userId == null || orderId == null || category == null) {
            Log.e(TAG, "Missing intent data!");
            return;
        }

        // Firebase references
        orderRef = FirebaseDatabase.getInstance().getReference("Order_request").child(userId).child(category).child(orderId);
        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus").child(orderId);
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        pickupBoyRef = FirebaseDatabase.getInstance().getReference("Boy-outforpickup").child(orderId);
        deliveryBoyRef = FirebaseDatabase.getInstance().getReference("Boy-outfordelivery").child(orderId);

        loadOrderDetails();
        loadUserMobile();
        monitorOrderStatus();
    }

    private void loadOrderDetails() {
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                tvCategory.setText("📌 Category: " + category);
                tvOrderId.setText("📦 Order ID: " + orderId);
                tvPants.setText("👖 Pants: " + snapshot.child("PantsQuantity").getValue(Integer.class));
                tvShirts.setText("👕 Shirts: " + snapshot.child("ShirtQuantity").getValue(Integer.class));
                tvOthers.setText("🧥 Others: " + snapshot.child("OthersQuantity").getValue(Integer.class));
                tvSneakers.setText("👟 Sneakers: " + snapshot.child("SneakersQuantity").getValue(Integer.class));
                tvLeatherShoes.setText("👟 LeatherShoes: " + snapshot.child("LeatherShoesQuantity").getValue(Integer.class));
                tvSuedeShoes.setText("👞 SuedeShoes: " + snapshot.child("SuedeShoesQuantity").getValue(Integer.class));

                tvTotalPieces.setText("📊 Total Pieces: " + snapshot.child("TotalPieces").getValue(Integer.class));
                tvTotalPrice.setText("💰 Total Price: ₹" + snapshot.child("TotalPrice").getValue(Integer.class));
                tvAddress.setText("📍 Address:\n" + snapshot.child("address").getValue(String.class));
                tvEmail.setText("📧 Email: " + snapshot.child("email").getValue(String.class));
                tvGender.setText("👤 Gender: " + snapshot.child("gender").getValue(String.class));
                tvMobile.setText("📱 Mobile: " + snapshot.child("mobile").getValue(String.class));
                tvName.setText("🙋 Name: " + snapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Order details load error: " + error.getMessage());
            }
        });
    }

    private void loadUserMobile() {
        userRef.child("mobile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userMobile = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "User mobile fetch error: " + error.getMessage());
            }
        });
    }

    private void monitorOrderStatus() {
        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                Boolean isConfirm = snapshot.child("isConfirm").getValue(Boolean.class);
                Boolean isStored = snapshot.child("isStored").getValue(Boolean.class);
                Boolean isOutForPickup = snapshot.child("isoutforpickup").getValue(Boolean.class);
                Boolean isOutForDelivery = snapshot.child("isoutfordelivery").getValue(Boolean.class);
                Boolean isDelivered = snapshot.child("isdelivered").getValue(Boolean.class);
                Boolean isPaymentDone = snapshot.child("ispaymentdone").getValue(Boolean.class);

                setStatusColor(tvIsConfirm, isConfirm, "Confirm");
                setStatusColor(tvIsStored, isStored, "Stored");
                setStatusColor(tvIsOutForPickup, isOutForPickup, "Out for Pickup");
                setStatusColor(tvIsOutForDelivery, isOutForDelivery, "Out for Delivery");
                setStatusColor(tvIsDelivered, isDelivered, "Delivered");
                setStatusColor(tvIsPaymentDone, isPaymentDone, "Payment Done");

                if (Boolean.TRUE.equals(isConfirm) && !smsConfirmSent) {
                    sendSmsNow("Your order has been confirmed. Order ID: " + orderId);
                    smsConfirmSent = true;
                }

                if (Boolean.TRUE.equals(isStored) && !smsStoredSent) {
                    sendSmsNow("Your order has been stored. Order ID: " + orderId);
                    smsStoredSent = true;
                }

                if (Boolean.TRUE.equals(isOutForPickup) && !smsPickupSent) {
                    sendPickupSmsWithBoyDetails();
                    smsPickupSent = true;
                }

                if (Boolean.TRUE.equals(isOutForDelivery) && !smsDeliverySent) {
                    sendDeliverySmsWithBoyDetails();
                    smsDeliverySent = true;
                }

                if (Boolean.TRUE.equals(isDelivered) && !smsDeliveredSent) {
                    sendSmsNow("Your laundry has been delivered. Order ID: " + orderId);
                    smsDeliveredSent = true;
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Status load error: " + error.getMessage());
            }
        });
    }
    private void sendDeliverySmsWithBoyDetails() {
        deliveryBoyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String boyName = snapshot.child("workerName").getValue(String.class);
                String boyMobile = snapshot.child("workerMobile").getValue(String.class);

                String msg = "We are out for Delivery of your laundry.\nOrder ID: " + orderId;
                if (boyName != null && boyMobile != null) {
                    msg += "\nPickup Boy: " + boyName + "\nContact: " + boyMobile;
                }

                sendSmsNow(msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Pickup boy info error: " + error.getMessage());
            }
        });
    }

    private void sendPickupSmsWithBoyDetails() {
        pickupBoyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String boyName = snapshot.child("workerName").getValue(String.class);
                String boyMobile = snapshot.child("workerMobile").getValue(String.class);

                String msg = "We are out for Pickup of your laundry.\nOrder ID: " + orderId;
                if (boyName != null && boyMobile != null) {
                    msg += "\nPickup Boy: " + boyName + "\nContact: " + boyMobile;
                }

                sendSmsNow(msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Pickup boy info error: " + error.getMessage());
            }
        });
    }

    private void setStatusColor(TextView textView, Boolean status, String label) {
        if (status != null && status) {
            textView.setText("✅ " + label + ": Yes");
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setText("❌ " + label + ": No");
            textView.setTextColor(Color.RED);
        }
    }

    private void sendSmsNow(String message) {
        if (userMobile == null || userMobile.isEmpty()) return;

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(userMobile, null, message, null, null);
            Toast.makeText(this, "SMS sent: " + message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "SMS sent: " + message);
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "SMS Error: " + e.getMessage());
        }
    }

    // Handle SMS permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
