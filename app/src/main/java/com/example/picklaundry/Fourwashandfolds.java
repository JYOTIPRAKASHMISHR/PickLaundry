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

    public class Fourwashandfolds extends AppCompatActivity {

        private static final String TAG = "Fourwashandfolds";
        private TextView orderIdText, nameText, emailText, mobileText, addressText, genderText;
        private TextView shirtQuantity, pantsQuantity, othersQuantity, totalPieces, totalPrice;

        private DatabaseReference washAndFoldsRef, orderAllRef, orderRequestRef;
        private String latestOrderId = null;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_fourwashandfolds);

            initializeViews();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            washAndFoldsRef = database.getReference("Washandfolds");
            orderAllRef = database.getReference("Order_all").child("Washandfolds");
            orderRequestRef = database.getReference("Order_request").child("Washandfolds");

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
            washAndFoldsRef.orderByKey().limitToLast(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                    latestOrderId = orderSnapshot.getKey();
                                    Log.d(TAG, "Latest Order ID: " + latestOrderId);
                                    displayOrderDetails(orderSnapshot);
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
            return snapshot.child(key).getValue() != null ? snapshot.child(key).getValue().toString() : "-";
        }

        private void showToast(String message) {
            Toast.makeText(Fourwashandfolds.this, message, Toast.LENGTH_SHORT).show();
        }

        public void Done1(View view) {
            if (latestOrderId == null) {
                showToast("No order to process.");
                return;
            }

            washAndFoldsRef.child(latestOrderId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot orderSnapshot) {
                    if (orderSnapshot.exists()) {
                        Object orderData = orderSnapshot.getValue();

                        // Save to Order_all
                        orderAllRef.child(latestOrderId).setValue(orderData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Order saved to Order_all.");

                                    // Save to Order_request
                                    orderRequestRef.child(latestOrderId).setValue(orderData)
                                            .addOnSuccessListener(aVoid1 -> {
                                                showToast("Order saved to both databases.");

                                                // Remove from Washandfolds
                                                washAndFoldsRef.child(latestOrderId).removeValue()
                                                        .addOnSuccessListener(aVoid2 -> Log.d(TAG, "Order removed from Washandfolds."))
                                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to remove order: " + e.getMessage()));
                                            })
                                            .addOnFailureListener(e -> showToast("Failed to save to Order_request."));
                                })
                                .addOnFailureListener(e -> showToast("Failed to save to Order_all."));
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
