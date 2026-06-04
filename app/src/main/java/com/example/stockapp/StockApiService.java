package com.example.stockapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockApiService<StockResponse> {
    @GET("query?function=GLOBAL_QUOTE")
    Call<StockResponse> getStockQuote(
            @Query("symbol") String symbol,
            @Query("9SNT16ZNZROXR97I") String apiKey
    );
}