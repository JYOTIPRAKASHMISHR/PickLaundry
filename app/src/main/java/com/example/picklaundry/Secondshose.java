// Secondshose.java
package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Secondshose extends AppCompatActivity {

    private EditText sneakersInput, leatherShoesInput, suedeShoesInput;
    private TextView totalPiecesText, totalPriceText;
    private Button continueButton;

    // 💰 Prices per shoe type
    private final int PRICE_SNEAKERS = 20;
    private final int PRICE_LEATHER = 30;
    private final int PRICE_SUEDE = 35;

    private DatabaseReference orderRef, usersRef, statusRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondshose);

        // ✅ Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        orderRef = FirebaseDatabase.getInstance().getReference("ShoeCleaningOrders");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus");

        // 📝 Initialize views
        sneakersInput = findViewById(R.id.sneakersInput);
        leatherShoesInput = findViewById(R.id.leatherShoesInput);
        suedeShoesInput = findViewById(R.id.suedeShoesInput);
        totalPiecesText = findViewById(R.id.totalPieces);
        totalPriceText = findViewById(R.id.totalPrice);
        continueButton = findViewById(R.id.btn_continue);

        // 🖊️ Real-time calculation watcher
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateTotals(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        sneakersInput.addTextChangedListener(textWatcher);
        leatherShoesInput.addTextChangedListener(textWatcher);
        suedeShoesInput.addTextChangedListener(textWatcher);

        // 🎯 Handle "Continue" button click
        continueButton.setOnClickListener(v -> saveOrderToFirebase());
    }

    private void calculateTotals() {
        int sneakersQty = parseInput(sneakersInput.getText().toString());
        int leatherQty = parseInput(leatherShoesInput.getText().toString());
        int suedeQty = parseInput(suedeShoesInput.getText().toString());

        int totalPieces = sneakersQty + leatherQty + suedeQty;
        int totalPrice = (sneakersQty * PRICE_SNEAKERS) + (leatherQty * PRICE_LEATHER) + (suedeQty * PRICE_SUEDE);

        totalPiecesText.setText(String.valueOf(totalPieces));
        totalPriceText.setText("₹" + totalPrice);
    }

    private void saveOrderToFirebase() {
        int sneakersQty = parseInput(sneakersInput.getText().toString());
        int leatherQty = parseInput(leatherShoesInput.getText().toString());
        int suedeQty = parseInput(suedeShoesInput.getText().toString());

        if (sneakersQty == 0 && leatherQty == 0 && suedeQty == 0) {
            Toast.makeText(this, "You haven't selected any items. Please place an order.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not logged in. Please log in to continue.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String address = snapshot.child("location").getValue(String.class);

                    int totalPieces = sneakersQty + leatherQty + suedeQty;
                    int totalPrice = (sneakersQty * PRICE_SNEAKERS) + (leatherQty * PRICE_LEATHER) + (suedeQty * PRICE_SUEDE);

                    HashMap<String, Object> orderData = new HashMap<>();
                    orderData.put("userId", userId);
                    orderData.put("name", name);
                    orderData.put("email", email);
                    orderData.put("mobile", mobile);
                    orderData.put("gender", gender);
                    orderData.put("address", address);
                    orderData.put("SneakersQuantity", sneakersQty);
                    orderData.put("LeatherShoesQuantity", leatherQty);
                    orderData.put("SuedeShoesQuantity", suedeQty);
                    orderData.put("TotalPieces", totalPieces);
                    orderData.put("TotalPrice", totalPrice);

                    String orderId = orderRef.push().getKey();
                    if (orderId != null) {
                        orderRef.child(orderId).setValue(orderData)
                                .addOnSuccessListener(unused -> saveOrderStatus(orderId))
                                .addOnFailureListener(e -> Toast.makeText(Secondshose.this, "Order save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(Secondshose.this, "Failed to generate order ID.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Secondshose.this, "User details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Secondshose.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrderStatus(String orderId) {
        HashMap<String, Object> statusData = new HashMap<>();
        statusData.put("isStored", true);

        statusRef.child(orderId).setValue(statusData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(Secondshose.this, "Order & status saved successfully!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    navigateToNextPage();
                })
                .addOnFailureListener(e -> Toast.makeText(Secondshose.this, "Status save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToNextPage() {
        Intent intent = new Intent(Secondshose.this, Thirdshose.class); // Make sure Thirdshose.java exists
        startActivity(intent);
        finish();
    }

    private int parseInput(String input) {
        return input.isEmpty() ? 0 : Integer.parseInt(input.trim());
    }

    private void clearInputs() {
        sneakersInput.setText("");
        leatherShoesInput.setText("");
        suedeShoesInput.setText("");
        totalPiecesText.setText("0");
        totalPriceText.setText("₹0");
    }
}
