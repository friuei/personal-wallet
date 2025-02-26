package com.example.personalwallet.model;

public class Data {
    private int amount;
    private String type;
    private String id;
    private String date;

    public Data() {}
    public Data(int amount, String type, String id, String date) {
        this.amount = amount;
        this.type = type;
        this.id = id;
        this.date = date;
    }

    // Getters and setters
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
