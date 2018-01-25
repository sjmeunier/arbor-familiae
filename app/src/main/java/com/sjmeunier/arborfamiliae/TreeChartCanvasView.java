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

import com.sjmeunier.arborfamiliae.data.FamilyIndividuals;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.data.TreeChartFamily;
import com.sjmeunier.arborfamiliae.data.TreeChartIndividual;
import com.sjmeunier.arborfamiliae.data.TreeChartIndividualType;
import com.sjmeunier.arborfamiliae.database.Family;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;

public class TreeChartCanvasView extends View {

    private boolean isLoaded = false;
    private Map<Integer, Individual> individuals;
    private Map<Integer, TreeChartIndividual> individualBoxes;
    private List<TreeChartFamily> familyBoxes;

    Context context;
    private Paint linePaint;
    private Paint thickLinePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint maleFillPaint;
    private Paint femaleFillPaint;

    private float scale = 0.5f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private static float MIN_ZOOM = 0.2f;
    private static float MAX_ZOOM = 5f;

    private float baseOriginX;
    private float getBaseOriginY;
    private float originX;
    private float originY;
    private float offsetX;
    private float offsetY;

    private float boxPadding = 20f;
    private float boxHalfWidth = 200f;
    private float boxHalfHeight = 100f;
    private float boxHorizontalSpacing = 100f;
    private float boxMinVerticalSpacing = 50f;

    private float textPadding = 5f;

    private float prevTouchX;
    private float prevTouchY;
    private static final float TOUCH_TOLERANCE = 5;

    private boolean currentlyMultiTouch = false;

    public TreeChartCanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        scale = 1;

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.parseColor("#0F5858"));
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        thickLinePaint = new Paint();
        thickLinePaint.setAntiAlias(true);
        thickLinePaint.setColor(Color.parseColor("#0F5858"));
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(Color.parseColor("#0A6969"));
        fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        fillPaint.setStrokeJoin(Paint.Join.ROUND);

        maleFillPaint = new Paint();
        maleFillPaint.setAntiAlias(true);
        maleFillPaint.setColor(Color.parseColor("#44BA0AFF"));
        maleFillPaint.setStyle(Paint.Style.FILL);

        femaleFillPaint = new Paint();
        femaleFillPaint.setAntiAlias(true);
        femaleFillPaint.setColor(Color.parseColor("#44FF0AD5"));
        femaleFillPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#DDDDDD"));
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeJoin(Paint.Join.ROUND);
        invalidate();
    }


    public void configureChart(Map<Integer, Individual> individuals, List<FamilyIndividuals> familiesWithIndividuals, int maxGenerations, NameFormat nameFormat)
    {
        this.individuals = individuals;
        Individual individual;

        //Create ancestor boxes
        int highestAhnenNumber = 0;

        highestAhnenNumber = AncestryUtil.getHighestAhnenNumberForGeneration(maxGenerations);
        int highestGeneration = AncestryUtil.getGenerationNumberFromAhnenNumber(highestAhnenNumber);
        int maxBoxesPerGeneration = (int)Math.pow(2, highestGeneration);

        float maxReachY = (maxBoxesPerGeneration / 2f) * (boxHalfHeight * 2f + boxMinVerticalSpacing);

        individualBoxes = new HashMap<Integer, TreeChartIndividual>();
        for(int ahnenNumber = 1; ahnenNumber <= highestAhnenNumber; ahnenNumber++) {
            int generation = AncestryUtil.getGenerationNumberFromAhnenNumber(ahnenNumber);
            int boxIndex = ahnenNumber - (int)Math.pow(2, generation);
            int boxesForThisGeneration = (int)Math.pow(2, generation);

            float boxCentreX = generation * (boxHalfWidth * 2f + boxHorizontalSpacing);

            float boxTotalVerticalHeight = (maxReachY * 2f) / boxesForThisGeneration;

            float boxCentreY = ((boxTotalVerticalHeight / 2f) + (boxIndex * boxTotalVerticalHeight)) - maxReachY;

            int childAhnenNumber = AncestryUtil.getChildAhnenNumber(ahnenNumber);

            if (individuals.containsKey(ahnenNumber)) {
                individual = individuals.get(ahnenNumber);
                individualBoxes.put(ahnenNumber, new TreeChartIndividual(
                        individual.individualId,
                        ahnenNumber,
                        AncestryUtil.generateName(individual, nameFormat),
                        AncestryUtil.generateBirthDeathDate(individual, true),
                        individual.gender,
                        boxCentreX,
                        boxCentreY,
                        childAhnenNumber,
                        true));
            } else {
                individualBoxes.put(ahnenNumber, new TreeChartIndividual(0, ahnenNumber, "", "", GenderEnum.Unknown, boxCentreX, boxCentreY, childAhnenNumber, false));
            }
        }

        //Create immediate family boxes
        familyBoxes = new ArrayList<>();

        float boxSpouseCentreX = individualBoxes.get(1).boxCentreX;
        float boxChildCentreX = individualBoxes.get(1).boxCentreX - (boxHalfWidth * 2f + boxHorizontalSpacing);
        float boxCentreY = individualBoxes.get(1).boxCentreY + boxHalfHeight * 2f + boxMinVerticalSpacing;

        for(FamilyIndividuals familyIndividuals : familiesWithIndividuals) {
            TreeChartFamily treeChartFamily = new TreeChartFamily();

            if (familyIndividuals.spouse == null) {
                treeChartFamily.spouse = new TreeChartIndividual(0, 0, "", "", GenderEnum.Unknown, boxSpouseCentreX, boxCentreY, 0, false);
            } else {
                treeChartFamily.spouse = new TreeChartIndividual(
                        familyIndividuals.spouse.individualId,
                        0,
                        AncestryUtil.generateName(familyIndividuals.spouse, nameFormat),
                        AncestryUtil.generateBirthDeathDate(familyIndividuals.spouse, true),
                        familyIndividuals.spouse.gender,
                        boxSpouseCentreX,
                        boxCentreY,
                        0,
                        true);
            }

            for(Individual child : familyIndividuals.children) {
                if (child != null){
                    treeChartFamily.children.add(new TreeChartIndividual(
                            child.individualId,
                            0,
                            AncestryUtil.generateName(child, nameFormat),
                            AncestryUtil.generateBirthDeathDate(child, true),
                            child.gender,
                            boxChildCentreX,
                            boxCentreY,
                            0,
                            true));
                    boxCentreY += (boxHalfHeight * 2f + boxMinVerticalSpacing);
                }
            }

            familyBoxes.add(treeChartFamily);
        }

        this.isLoaded = true;
        Log.d("ARBORFAMILIAE-LOG", "Set up canvas with " + String.valueOf(individuals.size()) + " individuals and " + String.valueOf(maxGenerations) + " generations");
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
        TreeChartIndividual individualBox;
        TreeChartIndividual individualConnectingBox;
        if (isLoaded) {
            textPaint.setStrokeWidth(2f);
            textPaint.setTextSize(28f * scale);
            fillPaint.setStrokeWidth(2f);
            linePaint.setStrokeWidth(2f);
            thickLinePaint.setStrokeWidth(6f * scale);

            originX = boxHalfWidth + boxHorizontalSpacing * scale;
            originY = getHeight() / 2f;

            //Draw ancestors
            for (Map.Entry<Integer, TreeChartIndividual> entry : individualBoxes.entrySet()) {
                individualBox = entry.getValue();
                if (individualBox.childAhnenNumber > 0)
                    individualConnectingBox = individualBoxes.get(individualBox.childAhnenNumber);
                else
                    individualConnectingBox = null;
                drawBox(canvas, entry.getValue(), individualConnectingBox, TreeChartIndividualType.Ancestor);
            }

            //Draw families
            individualConnectingBox = individualBoxes.get(1);
            for(TreeChartFamily family : familyBoxes) {
                drawBox(canvas, family.spouse, individualConnectingBox, TreeChartIndividualType.Spouse);

                individualConnectingBox = family.spouse;
                for(TreeChartIndividual child : family.children) {
                    drawBox(canvas, child, individualConnectingBox, TreeChartIndividualType.Child);
                }
            }
        }
    }

    private void drawBox(Canvas canvas, TreeChartIndividual individualBox, TreeChartIndividual individualConnectingBox, TreeChartIndividualType individualType) {
        //Draw individual box
        if (individualBox.recordExists) {
            if (individualBox.gender == GenderEnum.Male)
                canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), maleFillPaint);
            else
                canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), femaleFillPaint);

            canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), linePaint);
        } else {
            canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), linePaint);
        }

        if (individualType == TreeChartIndividualType.Ancestor && individualConnectingBox != null) {
            //Connect lines for ancestors
            canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth - (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), thickLinePaint);
            canvas.drawLine(originX + offsetX + ((individualConnectingBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY) * scale), originX + offsetX + ((individualConnectingBox.boxCentreX + boxHalfWidth + (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY) * scale), thickLinePaint);
            canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth - (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), originX + offsetX + ((individualConnectingBox.boxCentreX + boxHalfWidth + (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY) * scale), thickLinePaint);
        } else if (individualType == TreeChartIndividualType.Spouse && individualConnectingBox != null) {
            //Connect lines for spouse
            canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualConnectingBox.boxCentreX) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY + boxHalfHeight) * scale), thickLinePaint);
        } else if (individualType == TreeChartIndividualType.Child && individualConnectingBox != null) {
            //Connect lines for children
            canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), thickLinePaint);
            canvas.drawLine(originX + offsetX + ((individualConnectingBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY) * scale), originX + offsetX + ((individualConnectingBox.boxCentreX - boxHalfWidth - (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY) * scale), thickLinePaint);
            canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), originX + offsetX + ((individualConnectingBox.boxCentreX - boxHalfWidth - (boxHorizontalSpacing / 2f)) * scale), originY + offsetY + ((individualConnectingBox.boxCentreY) * scale), thickLinePaint);
        }

        textPaint.setTextSize(28f * scale);
        drawText(canvas, individualBox.name, individualBox.dates, (boxHalfWidth * 2f - boxPadding) * scale, originX + offsetX + (individualBox.boxCentreX * scale), originY + offsetY + (individualBox.boxCentreY * scale));

    }

    private void drawText(Canvas canvas, String name, String dates, float maxWidth, float centreX, float centreY) {
        name = name.trim();
        while(name.contains("  "))
            name = name.replace("  ", " ");

        float lineSpacing = 4f * scale;
        textPaint.setTextSize(28f * scale);

        String[] lines;

        if (textPaint.measureText(name) > maxWidth && name.contains(" ")) {

            try {
                String wrapped = WordUtils.wrap(name, name.length() / 2, "\n", false).replaceAll("\n\n", "\n");
                wrapped += "\n" + dates;
                wrapped = wrapped.replaceAll("\n\n", "\n");
                lines = wrapped.split("\n");
                if (lines.length == 4) {
                    String[] fixedLines = new String[3];
                    fixedLines[0] = lines[0];
                    fixedLines[1] = lines[1] + " " + lines[2];
                    fixedLines[2] = lines[3];
                    lines = fixedLines;
                }
            } catch (Exception e) {
                lines = new String[2];
                lines[0] = name;
                lines[1] = dates;
            }
        } else {
            lines = new String[2];
            lines[0] = name;
            lines[1] = dates;
        }


        boolean textFits = false;
        while(!textFits) {
            textFits = true;
            for(int i = 0; i < lines.length; i++) {
                if (textPaint.measureText(lines[i]) > maxWidth)
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
            float textOffsetX = (textPaint.measureText(lines[i]) / 2f);
            canvas.drawText(lines[i], centreX - textOffsetX, centreY + textOffsetY, textPaint);
            textOffsetY += textPaint.getTextSize() + lineSpacing;
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
            offsetX *= detector.getScaleFactor();
            offsetY *= detector.getScaleFactor();
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
