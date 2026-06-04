package com.example.stockapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CustomMarkerView implements IMarker {

    private final Paint paintDot = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String priceString = "0.00";
    private int markerColor = Color.parseColor("#FF5252");

    public CustomMarkerView(Context context) {
        paintDot.setStyle(Paint.Style.FILL);

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(30f);
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setFakeBoldText(true);

        paintBg.setStyle(Paint.Style.FILL);
    }

    public void updateMarkerColor(int color) {
        this.markerColor = color;
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(0, 0); // Self anchoring calculations managed on manual draw pass override
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        return new MPPointF(0, 0);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        priceString = String.format("%.2f", e.getY());
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        // Draw the tracking marker dot intersection point directly on the node path
        paintDot.setColor(markerColor);
        canvas.drawCircle(posX, posY, 12f, paintDot);

        // Draw the right-aligned anchored floating label bar block layout
        float labelWidth = 140f;
        float labelHeight = 50f;
        float rightEdgeX = canvas.getWidth() - 10f;

        paintBg.setColor(markerColor);
        canvas.drawRect(rightEdgeX - labelWidth, posY - (labelHeight / 2), rightEdgeX, posY + (labelHeight / 2), paintBg);
        canvas.drawText(priceString, rightEdgeX - (labelWidth / 2), posY + 10f, paintText);
    }
}