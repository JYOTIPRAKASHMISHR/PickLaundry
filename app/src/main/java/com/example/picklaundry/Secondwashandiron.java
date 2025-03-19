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

public class  Secondwashandiron extends AppCompatActivity {

    private EditText shirtInput, pantsInput, othersInput;
    private TextView totalPiecesText, totalPriceText, othersMessage;
    private Button continueButton;

    private final int PRICE_PER_SHIRT = 10;
    private final int PRICE_PER_PANTS = 15;

    private DatabaseReference orderRef, usersRef, statusRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondwashandiron);

        // ✅ Initialize Firebase references
        mAuth = FirebaseAuth.getInstance();
        orderRef = FirebaseDatabase.getInstance().getReference("Washandiron");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        statusRef = FirebaseDatabase.getInstance().getReference("OrderStatus"); // 🔑 New node for boolean flag

        // ✅ Initialize views
        shirtInput = findViewById(R.id.shirtInput);
        pantsInput = findViewById(R.id.pantsInput);
        othersInput = findViewById(R.id.othersInput);
        totalPiecesText = findViewById(R.id.totalPieces);
        totalPriceText = findViewById(R.id.totalPrice);
        othersMessage = findViewById(R.id.othersMessage);
        continueButton = findViewById(R.id.btn_continue);

        // 📝 Text watcher for real-time calculation
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateTotals(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        shirtInput.addTextChangedListener(textWatcher);
        pantsInput.addTextChangedListener(textWatcher);
        othersInput.addTextChangedListener(textWatcher);

        // 🎯 Handle continue button click
        continueButton.setOnClickListener(v -> saveOrderToFirebase());
    }

    private void calculateTotals() {
        int shirtQty = parseInput(shirtInput.getText().toString());
        int pantsQty = parseInput(pantsInput.getText().toString());
        int othersQty = parseInput(othersInput.getText().toString());

        int totalPieces = shirtQty + pantsQty + othersQty;
        int totalPrice = (shirtQty * PRICE_PER_SHIRT) + (pantsQty * PRICE_PER_PANTS);

        totalPiecesText.setText(String.valueOf(totalPieces));
        totalPriceText.setText("₹" + totalPrice);
        othersMessage.setVisibility(othersQty > 0 ? View.VISIBLE : View.GONE);
    }

    private void saveOrderToFirebase() {
        int shirtQty = parseInput(shirtInput.getText().toString());
        int pantsQty = parseInput(pantsInput.getText().toString());
        int othersQty = parseInput(othersInput.getText().toString());

        // 🚫 Check if no items are selected
        if (shirtQty == 0 && pantsQty == 0 && othersQty == 0) {
            Toast.makeText(this, "You haven't selected any items. Please place an order.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = (mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "User not logged in. Please log in to continue.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔍 Retrieve user details
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);

                    int totalPieces = shirtQty + pantsQty + othersQty;
                    int totalPrice = (shirtQty * PRICE_PER_SHIRT) + (pantsQty * PRICE_PER_PANTS);

                    // 📝 Prepare order data
                    HashMap<String, Object> orderData = new HashMap<>();
                    orderData.put("userId", userId);
                    orderData.put("name", name);
                    orderData.put("email", email);
                    orderData.put("mobile", mobile);
                    orderData.put("gender", gender);
                    orderData.put("address", location);
                    orderData.put("ShirtQuantity", shirtQty);
                    orderData.put("PantsQuantity", pantsQty);
                    orderData.put("OthersQuantity", othersQty);
                    orderData.put("TotalPieces", totalPieces);
                    orderData.put("TotalPrice", totalPrice);

                    // 📌 Generate order ID
                    String orderId = orderRef.push().getKey();

                    if (orderId != null) {
                        // 🚀 Save order in "Washandiron"
                        orderRef.child(orderId).setValue(orderData)
                                .addOnSuccessListener(unused -> saveOrderStatus(orderId))
                                .addOnFailureListener(e -> Toast.makeText(Secondwashandiron.this, "Order save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(Secondwashandiron.this, "Failed to generate order ID.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Secondwashandiron.this, "User details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Secondwashandiron.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Save the boolean flag in a separate node
    private void saveOrderStatus(String orderId) {
        HashMap<String, Object> statusData = new HashMap<>();
        statusData.put("isStored", true);

        statusRef.child(orderId).setValue(statusData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(Secondwashandiron.this, "Order & status saved successfully!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    navigateToThirdPage();
                })
                .addOnFailureListener(e -> Toast.makeText(Secondwashandiron.this, "Status save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void navigateToThirdPage() {
        Intent intent = new Intent(Secondwashandiron.this, Thirdwashandiron.class);
        startActivity(intent);
        finish(); // 🔄 Prevent returning on back press
    }

    private int parseInput(String input) {
        return input.isEmpty() ? 0 : Integer.parseInt(input.trim());
    }

    private void clearInputs() {
        shirtInput.setText("");
        pantsInput.setText("");
        othersInput.setText("");
        totalPiecesText.setText("0");
        totalPriceText.setText("₹0");
        othersMessage.setVisibility(View.GONE);
    }
}
