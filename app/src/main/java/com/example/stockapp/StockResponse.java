package com.example.stockapp;

import com.google.gson.annotations.SerializedName;

public class StockResponse {
    @SerializedName("Global Quote")
    private GlobalQuote globalQuote;

    public GlobalQuote getGlobalQuote() {
        return globalQuote;
    }
}