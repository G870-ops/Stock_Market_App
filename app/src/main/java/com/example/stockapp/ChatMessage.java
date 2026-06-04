package com.example.stockapp;

public class ChatMessage {
    public String message;
    public boolean isUser;
    public StockModel relatedStock; // Add this to link a message to a specific stock

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public ChatMessage(String message, boolean isUser, StockModel stock) {
        this.message = message;
        this.isUser = isUser;
        this.relatedStock = stock;
    }
}