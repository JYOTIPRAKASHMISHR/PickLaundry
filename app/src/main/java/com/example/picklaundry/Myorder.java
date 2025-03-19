package com.example.picklaundry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Myorder extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<OrderModel> orderList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorder);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerView.setAdapter(orderAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("Order_request").child("Washandfolds");

        fetchOrders();
    }

    private void fetchOrders() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Order_request");

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String category = categorySnapshot.getKey(); // Get the category name (e.g., "Washandfolds")

                    for (DataSnapshot orderSnapshot : categorySnapshot.getChildren()) {
                        String orderId = orderSnapshot.getKey();
                        String name = orderSnapshot.child("name").getValue(String.class);
                        Long totalPriceLong = orderSnapshot.child("TotalPrice").getValue(Long.class);
                        String totalPrice = totalPriceLong != null ? String.valueOf(totalPriceLong) : "0";

                        if (orderId != null && name != null) {
                            OrderModel order = new OrderModel(orderId, name, totalPrice, category);
                            orderList.add(order);
                        }
                    }
                }

                orderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Myorder.this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
