package com.example.picklaundry;

public class OrderModel {
    private String orderId;
    private String UserId;
    private String name;
    private String totalPrice;
    private String category;

    public OrderModel() {
        // Default constructor required for Firebase
    }

    public OrderModel(String orderId, String name, String totalPrice, String category, String UserId) {
        this.orderId = orderId;
        this.name = name;
        this.totalPrice = totalPrice;
        this.category = category;
        this.UserId=UserId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getName() {
        return name;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getCategory() {
        return category;
    }
}
