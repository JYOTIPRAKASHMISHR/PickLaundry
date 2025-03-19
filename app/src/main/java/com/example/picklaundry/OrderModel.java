package com.example.picklaundry;

public class OrderModel {
    private String orderId;
    private String name;
    private String totalPrice;
    private String category;

    public OrderModel() {
        // Default constructor required for Firebase
    }

    public OrderModel(String orderId, String name, String totalPrice, String category) {
        this.orderId = orderId;
        this.name = name;
        this.totalPrice = totalPrice;
        this.category = category;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getName() {
        return name;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getCategory() {
        return category;
    }
}
