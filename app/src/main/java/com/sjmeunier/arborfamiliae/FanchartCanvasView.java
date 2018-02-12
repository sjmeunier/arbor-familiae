package com.sjmeunier.arborfamiliae;

import android.content.Context;
import android.graphics.Bitmap;
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

import com.sjmeunier.arborfamiliae.data.FanChartIndividual;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.database.AppDatabase;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.FamilyChild;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;


import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FanchartCanvasView extends View {

    private boolean isLoaded = false;
    private boolean isConfigured = false;
    private boolean showAncestors = true;

    private int treeId = 0;

    private AppDatabase database;
    private int generations = 1;
    private List<FanChartIndividual> individuals;

    private Map<Integer, Individual> allIndividualsInTree;
    private Map<Integer, Family> allFamiliesInTree;
    private Individual rootIndividual;
    private MainActivity mainActivity;

    Context context;
    private Paint linePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint maleFillPaint;
    private Paint femaleFillPaint;
    private Paint backgroundPaint;

    private float scale = 1;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;
    private static float MIN_ZOOM = 0.2f;
    private static float MAX_ZOOM = 5f;

    private float[] generationDescendantRadius = new float[9];
    private float[] generationAncestorRadius = new float[9];
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

        generationAncestorRadius[0] = 80;
        generationAncestorRadius[1] = generationAncestorRadius[0] + 100;
        generationAncestorRadius[2] = generationAncestorRadius[1] + 100;
        generationAncestorRadius[3] = generationAncestorRadius[2] + 100;
        generationAncestorRadius[4] = generationAncestorRadius[3] + 250;
        generationAncestorRadius[5] = generationAncestorRadius[4] + 250;
        generationAncestorRadius[6] = generationAncestorRadius[5] + 250;
        generationAncestorRadius[7] = generationAncestorRadius[6] + 400;
        generationAncestorRadius[8] = generationAncestorRadius[7] + 400;

        generationDescendantRadius[0] = 80;
        generationDescendantRadius[1] = generationDescendantRadius[0] + 250;
        generationDescendantRadius[2] = generationDescendantRadius[1] + 250;
        generationDescendantRadius[3] = generationDescendantRadius[2] + 400;
        generationDescendantRadius[4] = generationDescendantRadius[3] + 400;
        generationDescendantRadius[5] = generationDescendantRadius[4] + 400;
        generationDescendantRadius[6] = generationDescendantRadius[5] + 400;
        generationDescendantRadius[7] = generationDescendantRadius[6] + 400;
        generationDescendantRadius[8] = generationDescendantRadius[7] + 400;

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

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.parseColor("#FFFFFF"));
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        backgroundPaint.setStrokeJoin(Paint.Join.ROUND);

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

    public void configureChart(Individual rootIndividual, Map<Integer, Individual> allIndividualsInTree, Map<Integer, Family> allFamiliesInTree, AppDatabase database, int treeId, MainActivity mainActivity, int generations, NameFormat nameFormat)
    {
        this.individuals = new ArrayList<>();
        this.allIndividualsInTree = allIndividualsInTree;
        this.allFamiliesInTree = allFamiliesInTree;
        this.generations = generations;
        this.nameFormat = nameFormat;
        this.rootIndividual = rootIndividual;
        this.database = database;
        this.treeId = treeId;
        this.mainActivity = mainActivity;
        this.showAncestors = true;
        this.isConfigured = true;
        processAncestorData();
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

        if (isLoaded) {
            textPaint.setColor(Color.parseColor("#DDDDDD"));
            femaleFillPaint.setColor(Color.parseColor("#44FF0AD5"));
            maleFillPaint.setColor(Color.parseColor("#44BA0AFF"));
            centreX = getWidth() / 2.0f;
            centreY = getHeight() / 2.0f;
            drawChart(canvas, centreX, centreY, offsetX, offsetY, scale);
        }
    }

    private void drawChart(Canvas canvas, float centreX, float centreY, float offsetX, float offsetY, float scale) {
        Path textPath;
        PathMeasure pathMeasure = new PathMeasure();

        float totalTextPadding = textPadding * 2f * scale;

        textPaint.setStrokeWidth(2f);
        textPaint.setTextSize(28f * scale);
        fillPaint.setStrokeWidth(2f);
        linePaint.setStrokeWidth(2f);

        float generationRadius0 = 0;
        float generationRadius1 = 0;

        canvas.drawCircle(centreX + offsetX, centreY + offsetY, generationAncestorRadius[0] * scale, fillPaint);

        String rootText = "";
        for (FanChartIndividual individual : individuals) {
            if (individual.generation == 0) {
                if (showAncestors)
                    rootText = "Ancestry of " + individual.name;
                else
                    rootText = "Descendants of " + individual.name;
            } else {
                if (showAncestors) {
                    generationRadius0 = generationAncestorRadius[individual.generation - 1];
                    generationRadius1 = generationAncestorRadius[individual.generation];
                } else {
                    generationRadius0 = generationDescendantRadius[individual.generation - 1];
                    generationRadius1 = generationDescendantRadius[individual.generation];
                }

                float endAngle = individual.startAngle + individual.sweepAngle;
                float innerRadius = generationRadius0 * scale;
                float outerRadius = generationRadius1 * scale;

                float radius = (generationRadius0 + ((generationRadius1 - generationRadius0) / 2f)) * scale;
                if (individual.gender == GenderEnum.Male) {
                    maleFillPaint.setStrokeWidth(outerRadius - innerRadius);
                    canvas.drawArc(centreX + offsetX - radius, centreY + offsetY - radius, centreX + offsetX + radius, centreY + offsetY + radius, individual.startAngle, individual.sweepAngle, false, maleFillPaint);
                } else {
                    femaleFillPaint.setStrokeWidth(outerRadius - innerRadius);
                    canvas.drawArc(centreX + offsetX - radius, centreY + offsetY - radius, centreX + offsetX + radius, centreY + offsetY + radius, individual.startAngle, individual.sweepAngle, false, femaleFillPaint);
                }
                canvas.drawArc(centreX + offsetX - innerRadius, centreY + offsetY - innerRadius, centreX + offsetX + innerRadius, centreY + offsetY + innerRadius, individual.startAngle, individual.sweepAngle, false, linePaint);
                canvas.drawArc(centreX + offsetX - outerRadius, centreY + offsetY - outerRadius, centreX + offsetX + outerRadius, centreY + offsetY + outerRadius, individual.startAngle, individual.sweepAngle, false, linePaint);
                canvas.drawLine(centreX + offsetX + ((generationRadius1 * scale) * (float) Math.cos(Math.toRadians(individual.startAngle))), centreY + offsetY + ((generationRadius1 * scale) * (float) Math.sin(Math.toRadians(individual.startAngle))), centreX + offsetX + ((generationRadius0 * scale) * (float) Math.cos(Math.toRadians(individual.startAngle))), centreY + offsetY + ((generationRadius0 * scale) * (float) Math.sin(Math.toRadians(individual.startAngle))), linePaint);
                canvas.drawLine(centreX + offsetX + ((generationRadius1 * scale) * (float) Math.cos(Math.toRadians(endAngle))), centreY + offsetY + ((generationRadius1 * scale) * (float) Math.sin(Math.toRadians(endAngle))), centreX + offsetX + ((generationRadius0 * scale) * (float) Math.cos(Math.toRadians(endAngle))), centreY + offsetY + ((generationRadius0 * scale) * (float) Math.sin(Math.toRadians(endAngle))), linePaint);

                textPath = new Path();

                int maxLines = 2;
                if (showAncestors) {
                    if (individual.generation < 4) {
                        textPath.addArc(centreX + offsetX - radius, centreY + offsetY - radius, centreX + offsetX + radius, centreY + offsetY + radius, individual.startAngle, individual.sweepAngle);
                    } else {
                        float textAngle = individual.startAngle + (individual.sweepAngle / 2f);
                        textPath.moveTo(centreX + offsetX + ((generationRadius1 * scale) * (float) Math.cos(Math.toRadians(textAngle))), centreY + offsetY + ((generationRadius1 * scale) * (float) Math.sin(Math.toRadians(textAngle))));
                        textPath.lineTo(centreX + offsetX + ((generationRadius0 * scale) * (float) Math.cos(Math.toRadians(textAngle))), centreY + offsetY + ((generationRadius0 * scale) * (float) Math.sin(Math.toRadians(textAngle))));
                    }

                    if (individual.generation == 4 || individual.generation == 5) {
                        maxLines = 3;
                    } else if (individual.generation > 6) {
                        maxLines = 1;
                    } else {
                        maxLines = 2;
                    }
                } else {
                    maxLines = 3;
                    float textAngle = individual.startAngle + (individual.sweepAngle / 2f);
                    textPath.moveTo(centreX + offsetX + ((generationRadius1 * scale) * (float) Math.cos(Math.toRadians(textAngle))), centreY + offsetY + ((generationRadius1 * scale) * (float) Math.sin(Math.toRadians(textAngle))));
                    textPath.lineTo(centreX + offsetX + ((generationRadius0 * scale) * (float) Math.cos(Math.toRadians(textAngle))), centreY + offsetY + ((generationRadius0 * scale) * (float) Math.sin(Math.toRadians(textAngle))));

                    if (individual.generation > 3) {
                        maxLines = 1;
                    } else {
                        maxLines = 2;
                    }
                }
                drawText(canvas, individual.name, textPath, pathMeasure, maxLines, totalTextPadding, scale);
            }

            textPaint.setTextSize(38f);
            canvas.drawText(rootText, 10, textPaint.getTextSize() + 10, textPaint);

        }
    }
    public Bitmap renderBitmap() throws Exception {
        if (isLoaded) {
            textPaint.setColor(Color.parseColor("#222222"));
            femaleFillPaint.setColor(Color.parseColor("#FF0AB5"));
            maleFillPaint.setColor(Color.parseColor("#BABAFF"));

            float radius = 0;
            if (showAncestors) {
                radius = generationAncestorRadius[generations] + 100;
            } else {
                radius = generationDescendantRadius[generations] + 100;
            }

            Bitmap bitmap = Bitmap.createBitmap((int)radius * 2 + 100, (int)radius * 2 + 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(0, 0, radius * 2f + 100f, radius * 2f + 100f, backgroundPaint);

            this.drawChart(canvas, radius + 50f, radius + 50f, 0, 0, 1);

            return bitmap;
        } else {
            throw new Exception("Not Loaded");
        }
    }

    private void drawText(Canvas canvas, String text, Path textPath, PathMeasure pathMeasure, int maxLines, float textPadding, float scale) {
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

    private void processAncestorData() {
        this.isLoaded = false;
        individuals = new ArrayList<>();

        individuals.add(new FanChartIndividual(
                rootIndividual.individualId,
                AncestryUtil.generateName(rootIndividual, nameFormat),
                rootIndividual.gender,
                0f,
                360f,
                0));

        processAncestorGeneration(0, 0, 360, rootIndividual.parentFamilyId);

        this.isLoaded = true;
        invalidate();
    }

    private void processAncestorGeneration(int generation, float currentStartAngle, float currentSweepAngle, int familyId) {
        Family family = allFamiliesInTree.get(familyId);
        if (family == null)
            return;

        Individual father = allIndividualsInTree.get(family.husbandId);
        if (father != null) {
            individuals.add(new FanChartIndividual(
                    father.individualId,
                    AncestryUtil.generateName(father, nameFormat),
                    father.gender,
                    currentStartAngle,
                    currentSweepAngle / 2f,
                    generation + 1));
            if (generation < generations - 1 && father.parentFamilyId != 0) {
                processAncestorGeneration(generation + 1, currentStartAngle, currentSweepAngle / 2f, father.parentFamilyId);
            }
        }
        Individual mother = allIndividualsInTree.get(family.wifeId);
        if (mother != null) {
            individuals.add(new FanChartIndividual(
                    mother.individualId,
                    AncestryUtil.generateName(mother, nameFormat),
                    mother.gender,
                    currentStartAngle + (currentSweepAngle / 2f),
                    currentSweepAngle / 2f,
                    generation + 1));
            if (generation < generations - 1 && mother.parentFamilyId != 0) {
                processAncestorGeneration(generation + 1, currentStartAngle + (currentSweepAngle / 2f), currentSweepAngle / 2f, mother.parentFamilyId);
            }
        }
    }

    private void processDescendantData() {
        this.isLoaded = false;
        individuals = new ArrayList<>();

        individuals.add(new FanChartIndividual(
                rootIndividual.individualId,
                AncestryUtil.generateName(rootIndividual, nameFormat),
                rootIndividual.gender,
                0f,
                360f,
                0));

        processDescendantGeneration(0, 0, 360, rootIndividual.individualId);

        this.isLoaded = true;
        invalidate();
    }

    private void processDescendantGeneration(int generation, float currentStartAngle, float currentSweepAngle, int individualId) {
        List<Family> families = database.familyDao().getAllFamiliesForHusbandOrWife(treeId, individualId);
        List<Integer> childIds = new ArrayList<>();

        for(Family family : families) {
            List<FamilyChild> children = database.familyChildDao().getAllFamilyChildren(treeId, family.familyId);
            for(FamilyChild child : children) {
                childIds.add(child.individualId);
            }
        }
        if (childIds.size() == 0)
            return;

        float startAngle = currentStartAngle;
        float sweepAngle = currentSweepAngle / (float)childIds.size();

        for(int childId : childIds) {
            Individual child = allIndividualsInTree.get(childId);
            if (child != null) {
                individuals.add(new FanChartIndividual(
                        child.individualId,
                        AncestryUtil.generateName(child, nameFormat),
                        child.gender,
                        startAngle,
                        sweepAngle,
                        generation + 1));
                if (generation < generations - 1) {
                    processDescendantGeneration(generation + 1, startAngle, sweepAngle, child.individualId);
                }
                startAngle += sweepAngle;
            }
        }
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

        private int getGenerationFromRadius(float radius) {
            int generation = -1;

            if (showAncestors) {
                if (radius < generationAncestorRadius[0])
                    generation = 0;
                else if (radius < generationAncestorRadius[1])
                    generation = 1;
                else if (radius < generationAncestorRadius[2])
                    generation = 2;
                else if (radius < generationAncestorRadius[3])
                    generation = 3;
                else if (radius < generationAncestorRadius[4])
                    generation = 4;
                else if (radius < generationAncestorRadius[5])
                    generation = 5;
                else if (radius < generationAncestorRadius[6])
                    generation = 6;
                else if (radius < generationAncestorRadius[7])
                    generation = 7;
                else if (radius < generationAncestorRadius[8])
                    generation = 8;
            } else {
                if (radius < generationDescendantRadius[0])
                    generation = 0;
                else if (radius < generationDescendantRadius[1])
                    generation = 1;
                else if (radius < generationDescendantRadius[2])
                    generation = 2;
                else if (radius < generationDescendantRadius[3])
                    generation = 3;
                else if (radius < generationDescendantRadius[4])
                    generation = 4;
                else if (radius < generationDescendantRadius[5])
                    generation = 5;
                else if (radius < generationDescendantRadius[6])
                    generation = 6;
                else if (radius < generationDescendantRadius[7])
                    generation = 7;
                else if (radius < generationDescendantRadius[8])
                    generation = 8;
            }
            return generation;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            if (isConfigured && isLoaded) {
                float clickX = event.getX() - centreX - offsetX;
                float clickY = event.getY() - centreY - offsetY;
                float clickRadius = (float)Math.sqrt(clickX * clickX + clickY * clickY);
                int generation = getGenerationFromRadius(clickRadius);
                float angle = (float)Math.atan2(clickY, clickX) * 180f / (float)Math.PI;

                if (angle < 0)
                    angle = 360f + angle;

                if (generation >= 1) {
                    for (FanChartIndividual individual : individuals) {
                        if (generation == individual.generation && angle > individual.startAngle && angle < (individual.startAngle + individual.sweepAngle))
                        {
                            rootIndividual = allIndividualsInTree.get(individual.individualId);
                            mainActivity.setActiveIndividual(individual.individualId, true);
                            if (showAncestors) {
                                processAncestorData();
                            } else {
                                processDescendantData();
                            }
                            break;
                        }
                    }
                }
            }
        }


        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (isConfigured && isLoaded) {
                float clickX = event.getX() - centreX - offsetX;
                float clickY = event.getY() - centreY - offsetY;
                float clickRadius = (float)Math.sqrt(clickX * clickX + clickY * clickY);
                int generation = getGenerationFromRadius(clickRadius);
                float angle = (float)Math.atan2(clickY, clickX) * 180f / (float)Math.PI;

                if (generation == 0) {
                    if (showAncestors) {
                        showAncestors = false;
                        processDescendantData();
                    } else {
                        showAncestors = true;
                        processAncestorData();
                    }
                }

            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            offsetX -= distanceX;
            offsetY -= distanceY;
            invalidate();
            return true;
        }
    }
}
