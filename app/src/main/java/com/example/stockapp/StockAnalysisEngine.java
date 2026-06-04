package com.example.stockapp;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.List;

public class StockAnalysisEngine {

    // Calculate Exponential Moving Average (EMA)
    public static List<Entry> calculateEMA(List<Entry> entries, int period) {
        List<Entry> emaList = new ArrayList<>();
        if (entries == null || entries.size() < period) return emaList;

        float k = 2.0f / (period + 1);
        float sum = 0;
        for (int i = 0; i < period; i++) {
            sum += entries.get(i).getY();
        }
        float previousEma = sum / period;
        emaList.add(new Entry(entries.get(period - 1).getX(), previousEma));

        for (int i = period; i < entries.size(); i++) {
            float currentEma = (entries.get(i).getY() * k) + (previousEma * (1 - k));
            emaList.add(new Entry(entries.get(i).getX(), currentEma));
            previousEma = currentEma;
        }
        return emaList;
    }

    // Calculate Relative Strength Index (RSI)
    public static List<Entry> calculateRSI(List<Entry> entries, int period) {
        List<Entry> rsiList = new ArrayList<>();
        if (entries == null || entries.size() <= period) return rsiList;

        float avgGain = 0;
        float avgLoss = 0;

        // First period initialization
        for (int i = 1; i <= period; i++) {
            float change = entries.get(i).getY() - entries.get(i - 1).getY();
            if (change > 0) avgGain += change;
            else avgLoss += Math.abs(change);
        }

        avgGain /= period;
        avgLoss /= period;

        float firstRsi = (avgLoss == 0) ? 100 : 100 - (100 / (1 + (avgGain / avgLoss)));
        rsiList.add(new Entry(entries.get(period).getX(), firstRsi));

        for (int i = period + 1; i < entries.size(); i++) {
            float change = entries.get(i).getY() - entries.get(i - 1).getY();
            float gain = change > 0 ? change : 0;
            float loss = change < 0 ? Math.abs(change) : 0;

            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;

            float rsi = (avgLoss == 0) ? 100 : 100 - (100 / (1 + (avgGain / avgLoss)));
            rsiList.add(new Entry(entries.get(i).getX(), rsi));
        }
        return rsiList;
    }

    // Calculate Moving Average Convergence Divergence (MACD)
    public static List<Entry> calculateMACD(List<Entry> entries, int fastPeriod, int slowPeriod) {
        List<Entry> macdLine = new ArrayList<>();
        if (entries == null || entries.size() < slowPeriod) return macdLine;

        List<Entry> fastEma = calculateEMA(entries, fastPeriod);
        List<Entry> slowEma = calculateEMA(entries, slowPeriod);

        int fastOffset = slowPeriod - fastPeriod;
        for (int i = 0; i < slowEma.size(); i++) {
            float xValue = slowEma.get(i).getX();
            float fastVal = fastEma.get(i + fastOffset).getY();
            float slowVal = slowEma.get(i).getY();
            macdLine.add(new Entry(xValue, fastVal - slowVal));
        }
        return macdLine;
    }
}