package com.example.stockapp;

import java.io.Serializable;

// Implementing Serializable allows this object to be passed via Intent.putExtra()
public class StockModel implements Serializable {
    private String symbol;
    private String name;
    private double price;
    private double change;

    public StockModel(String symbol, String name, double price, double change) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
    }

    // Getters
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getChange() { return change; }

    // Setters (Essential for the MainActivity simulation)
    public void setPrice(double price) {
        this.price = price;
    }

    public void setChange(double change) {
        this.change = change;
    }
}