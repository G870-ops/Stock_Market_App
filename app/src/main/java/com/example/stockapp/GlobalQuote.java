package com.example.stockapp;

import com.google.gson.annotations.SerializedName;

public class GlobalQuote {

    @SerializedName("01. symbol")
    private String symbol;

    @SerializedName("05. price")
    private String price;

    @SerializedName("10. change percent")
    private String changePercent;

    // These getters solve your "Cannot resolve method" errors
    public String getSymbol() {
        return symbol;
    }

    public String getPrice() {
        return price;
    }

    public String getChangePercent() {
        return changePercent;
    }
}