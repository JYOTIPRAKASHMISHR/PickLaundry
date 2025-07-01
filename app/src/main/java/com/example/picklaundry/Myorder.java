package com.example.picklaundry;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class Myorder extends AppCompatActivity {

    private static final String TAG = "MyOrderActivity";

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private final List<OrderModel> orderList = new ArrayList<>();
    private DatabaseReference orderRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorder);

        initViews();
        setupFirebase();
        fetchOrders();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
            Log.d(TAG, "UserID: " + currentUserId);
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderRef = FirebaseDatabase.getInstance()
                .getReference("Order_request")
                .child(currentUserId);
    }

    private void fetchOrders() {
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(Myorder.this, "No orders found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.getKey();

                    for (DataSnapshot orderSnapshot : categorySnapshot.getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        String name = orderSnapshot.child("name").getValue(String.class);
                        String totalPrice = orderSnapshot.child("TotalPrice").getValue() != null ?
                                String.valueOf(orderSnapshot.child("TotalPrice").getValue()) : "0";

                        if (orderId != null && name != null) {
                            orderList.add(new OrderModel(orderId, name, totalPrice, category, currentUserId));
                            Log.d(TAG, String.format("Order - ID: %s, Name: %s, Price: %s, Category: %s",
                                    orderId, name, totalPrice, category));
                        }
                    }
                }

                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching orders: " + error.getMessage(), error.toException());
                Toast.makeText(Myorder.this, "Failed to load orders", Toast.LENGTH_LONG).show();
            }
        });
    }
}
