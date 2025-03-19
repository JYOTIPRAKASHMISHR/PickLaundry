package com.example.picklaundry;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> orderList;

    public OrderAdapter(Context context, List<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel order = orderList.get(position);
        holder.tvOrderId.setText("Order ID: " + order.getOrderId());
        holder.tvName.setText("Name: " + order.getName());
        holder.tvTotalPrice.setText("Total Price: ₹" + order.getTotalPrice());
        holder.tvCategory.setText("Category: " + order.getCategory());

        // Open Mycart activity with order details on item click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MyCart.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("category", order.getCategory());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvName, tvTotalPrice, tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvName = itemView.findViewById(R.id.tvName);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}
