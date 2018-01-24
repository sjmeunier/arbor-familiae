package com.sjmeunier.arborfamiliae;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;


import org.apache.commons.lang3.text.WordUtils;

import java.util.Map;

public class FanchartCanvasView extends View {

    private boolean isLoaded = false;
    private int generations = 0;
    private Map<Integer, Individual> individuals;

    Context context;
    private Paint linePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint maleFillPaint;
    private Paint femaleFillPaint;

    private float scale = 1;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private static float MIN_ZOOM = 0.2f;
    private static float MAX_ZOOM = 5f;

    private float[] generationRadius = new float[9];
    private float centreX;
    private float centreY;
    private float offsetX;
    private float offsetY;

    private float textPadding = 5f;

    private boolean currentlyMultiTouch = false;

    private float prevTouchX;
    private float prevTouchY;
    private static final float TOUCH_TOLERANCE = 5;

    private NameFormat nameFormat;

    public FanchartCanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        generationRadius[0] = 50;
        generationRadius[1] = 150;
        generationRadius[2] = 250;
        generationRadius[3] = 350;
        generationRadius[4] = 600;
        generationRadius[5] = 850;
        generationRadius[6] = 1100;
        generationRadius[7] = 1500;
        generationRadius[8] = 1900;

        scale = 1;

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.parseColor("#0F5858"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(Color.parseColor("#0A6969"));
        fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaint.setStrokeJoin(Paint.Join.ROUND);

        maleFillPaint = new Paint();
        maleFillPaint.setAntiAlias(true);
        maleFillPaint.setColor(Color.parseColor("#44BA0AFF"));
        maleFillPaint.setStyle(Paint.Style.STROKE);
        maleFillPaint.setStrokeCap(Paint.Cap.BUTT);

        femaleFillPaint = new Paint();
        femaleFillPaint.setAntiAlias(true);
        femaleFillPaint.setColor(Color.parseColor("#44FF0AD5"));
        femaleFillPaint.setStyle(Paint.Style.STROKE);
        femaleFillPaint.setStrokeCap(Paint.Cap.BUTT);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#DDDDDD"));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeJoin(Paint.Join.ROUND);
        invalidate();
    }

    public void configureChart(Map<Integer, Individual> individuals, int generations, NameFormat nameFormat)
    {
        this.individuals = individuals;
        this.generations = generations;
        this.nameFormat = nameFormat;
        this.isLoaded = true;
        Log.d("ARBORFAMILIAE-LOG", "Set up canvas with " + String.valueOf(individuals.size()) + " individuals and " + String.valueOf(generations) + " generations");
        invalidate();
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path textPath;
        PathMeasure pathMeasure = new PathMeasure();

        if (isLoaded) {
            float totalTextPadding = textPadding * 2f * scale;

            textPaint.setStrokeWidth(2f);
            textPaint.setTextSize(28f * scale);
            fillPaint.setStrokeWidth(2f);
            linePaint.setStrokeWidth(2f);

            centreX = getWidth() / 2.0f;
            centreY = getHeight() / 2.0f;

            canvas.drawCircle(centreX + offsetX, centreY + offsetY, generationRadius[0] * scale, fillPaint);

            String rootText = "";
            for (Map.Entry<Integer, Individual> entry : individuals.entrySet()) {
                int ahnenNumber = entry.getKey();
                if (ahnenNumber == 1) {
                    rootText = "Ancestry of " + AncestryUtil.generateName(entry.getValue(), nameFormat);
                } else {
                    int generation = AncestryUtil.getGenerationNumberFromAhnenNumber(ahnenNumber);
                    int positionIndex = ahnenNumber - (int)Math.pow(2, generation);
                    int positions = (int)Math.pow(2, generation);
                    float startAngle = (360f * (positionIndex) / positions) + 270f;
                    float sweepAngle = 360f / (float)positions;
                    float endAngle = startAngle + sweepAngle;
                    float innerRadius = generationRadius[generation - 1] * scale;
                    float outerRadius = generationRadius[generation] * scale;

                    float radius = (generationRadius[generation - 1] + ((generationRadius[generation] - generationRadius[generation - 1]) / 2f)) * scale;
                    if (entry.getValue().gender == GenderEnum.Male) {
                        maleFillPaint.setStrokeWidth(outerRadius - innerRadius);
                        canvas.drawArc(centreX + offsetX - radius, centreY + offsetY - radius, centreX + offsetX + radius, centreY + offsetY + radius, startAngle, sweepAngle, false, maleFillPaint);
                    } else {
                        femaleFillPaint.setStrokeWidth(outerRadius - innerRadius);
                        canvas.drawArc(centreX + offsetX - radius, centreY + offsetY - radius, centreX + offsetX + radius, centreY + offsetY + radius, startAngle, sweepAngle, false, femaleFillPaint);
                    }
                    canvas.drawArc(centreX + offsetX - innerRadius, centreY + offsetY - innerRadius, centreX + offsetX + innerRadius, centreY + offsetY + innerRadius, startAngle, sweepAngle, false, linePaint);
                    canvas.drawArc(centreX + offsetX - outerRadius, centreY + offsetY - outerRadius, centreX + offsetX + outerRadius, centreY + offsetY + outerRadius, startAngle, sweepAngle, false, linePaint);
                    canvas.drawLine(centreX + offsetX + ((generationRadius[generation] * scale) * (float)Math.cos(Math.toRadians(startAngle))), centreY + offsetY + ((generationRadius[generation] * scale) * (float)Math.sin(Math.toRadians(startAngle))), centreX + offsetX + ((generationRadius[generation - 1] * scale) * (float)Math.cos(Math.toRadians(startAngle))), centreY + offsetY + ((generationRadius[generation - 1] * scale) * (float)Math.sin(Math.toRadians(startAngle))), linePaint);
                    canvas.drawLine(centreX + offsetX + ((generationRadius[generation] * scale) * (float)Math.cos(Math.toRadians(endAngle))), centreY + offsetY + ((generationRadius[generation] * scale) * (float)Math.sin(Math.toRadians(endAngle))), centreX + offsetX + ((generationRadius[generation - 1] * scale) * (float)Math.cos(Math.toRadians(endAngle))), centreY + offsetY + ((generationRadius[generation - 1] * scale) * (float)Math.sin(Math.toRadians(endAngle))), linePaint);

                    textPath = new Path();

                    int maxLines = 2;
                    if (generation < 4) {
                        textPath.addArc(centreX + offsetX - radius, centreY + offsetY - radius, centreX + offsetX + radius, centreY + offsetY + radius, startAngle, sweepAngle);
                    } else {
                        float textAngle = startAngle + (sweepAngle / 2f);
                        textPath.moveTo(centreX + offsetX + ((generationRadius[generation] * scale) * (float)Math.cos(Math.toRadians(textAngle))), centreY + offsetY + ((generationRadius[generation] * scale) * (float)Math.sin(Math.toRadians(textAngle))));
                        textPath.lineTo(centreX + offsetX + ((generationRadius[generation - 1] * scale) * (float)Math.cos(Math.toRadians(textAngle))), centreY + offsetY + ((generationRadius[generation - 1] * scale) * (float)Math.sin(Math.toRadians(textAngle))));
                    }

                    if (generation == 4 || generation == 5) {
                        maxLines = 3;
                    } else if (generation  > 6) {
                        maxLines = 1;
                    } else {
                        maxLines = 2;
                    }

                    drawText(canvas, AncestryUtil.generateName(entry.getValue(), nameFormat), textPath, pathMeasure, maxLines, totalTextPadding);
                    //drawText(canvas, String.valueOf(ahnenNumber) + "-" + String.valueOf(positionIndex) + "-" + AncestryUtil.generateShortName(entry.getValue(), false), textPath, pathMeasure, maxLines, totalTextPadding);
                }

                textPaint.setTextSize(38f);
                canvas.drawText(rootText, 10, textPaint.getTextSize() + 10, textPaint);

            }
        }
    }

    private void drawText(Canvas canvas, String text, Path textPath, PathMeasure pathMeasure, int maxLines, float textPadding) {
        pathMeasure.setPath(textPath, false);
        maxLines = Math.min(3, maxLines);

        text = text.trim();
        while(text.contains("  "))
            text = text.replace("  ", " ");

        float lineSpacing = 4f * scale;

        float maxMeasuredLength = pathMeasure.getLength() - textPadding;

        textPaint.setTextSize(28f * scale);

        String[] lines;

        if (textPaint.measureText(text) > maxMeasuredLength && maxLines > 1 && text.contains(" ")) {

            try {
                lines = WordUtils.wrap(text, text.length() / 2, "\n", false).replace("\n\n", "\n").split("\n");
            } catch (Exception e) {
                Log.d("ARBORFAMILIAE-LOG", "Could not split into 2 lines - " + text);
                lines = new String[1];
                lines[0] = text;
            }

            if (lines.length == 3) {
                String[] fixedLines = new String[2];
                fixedLines[0] = lines[0];
                fixedLines[1] = lines[1] + " " + lines[2];
                lines = fixedLines;
            }

            if (maxLines == 3 && lines.length == 2) {
                if (lines[0].length() > maxMeasuredLength || lines[1].length() > maxMeasuredLength) {
                    try {
                        lines = WordUtils.wrap(text, text.length() / 3, "\n", false).replace("\n\n", "\n").split("\n");
                    } catch (Exception e) {
                        Log.d("ARBORFAMILIAE-LOG", "Could not split into 3 lines - " + text);
                        lines = new String[1];
                        lines[0] = text;
                    }
                }
                if (lines.length == 4) {
                    String[] fixedLines = new String[3];
                    fixedLines[0] = lines[0];
                    fixedLines[1] = lines[1];
                    fixedLines[2] = lines[2] + " " + lines[3];
                    lines = fixedLines;
                }
            }
        } else {
            lines = new String[1];
            lines[0] = text;
        }

        boolean textFits = false;
        while(!textFits) {
            textFits = true;
            for(int i = 0; i < lines.length; i++) {
                if (textPaint.measureText(lines[i]) > maxMeasuredLength)
                    textFits = false;
            }
            if (!textFits) {
                textPaint.setTextSize(Math.max(0.2f, textPaint.getTextSize() - 0.5f));
            }
            if (textPaint.getTextSize() <= 0.2f)
                textFits = true;
        }

        float textOffsetY = 0;

        if (lines.length == 1) {
            textOffsetY = textPaint.getTextSize() / 2f;
        } else if (lines.length == 2) {
            textOffsetY = (lineSpacing / 2f) * -1;
        } else if (lines.length == 3) {
            textOffsetY = (textPaint.getTextSize() / 2f ) - (textPaint.getTextSize() + lineSpacing);
        }

        for(int i = 0; i < lines.length; i++) {
            float textOffsetX = (pathMeasure.getLength() / 2f) - (textPaint.measureText(lines[i]) / 2f);
            canvas.drawTextOnPath(lines[i], textPath, textOffsetX, textOffsetY, textPaint);
            textOffsetY += textPaint.getTextSize() + lineSpacing;
        }
    }

    // when ACTION_DOWN start touch according to the x,y values
    private void startTouch(float x, float y) {
        prevTouchX = x;
        prevTouchY = y;
    }

    // when ACTION_MOVE move touch according to the x,y values
    private void moveTouch(float x, float y) {
        float dx = x - prevTouchX;
        float dy = y - prevTouchY;
        if (Math.abs(dx) >= TOUCH_TOLERANCE || Math.abs(dy) >= TOUCH_TOLERANCE) {
            offsetX += dx;
            offsetY += dy;
            prevTouchX = x;
            prevTouchY = y;
        }
    }

    // when ACTION_UP stop touch
    private void upTouch() {

    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX -= distanceX;
            offsetY -= distanceY;
            invalidate();
            return true;
        }
    }
}
