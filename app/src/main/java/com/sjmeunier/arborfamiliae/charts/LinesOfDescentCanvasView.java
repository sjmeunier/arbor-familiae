package com.sjmeunier.arborfamiliae.charts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.sjmeunier.arborfamiliae.MainActivity;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.data.RelationshipChartIndividual;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;
import com.sjmeunier.arborfamiliae.util.AncestryUtil;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class LinesOfDescentCanvasView extends View {

    private boolean isLoaded = false;

    private List<List<Integer>> lineages;
    private int currentLineage;
    private MainActivity mainActivity;
    private NameFormat nameFormat;

    private List<RelationshipChartIndividual> individualBoxes;
    private boolean isSamePerson;

    private String messageLine1;
    private String messageLine2;
    private String messageLine3;
    private String lineageLine;

    Context context;
    private Paint linePaint;
    private Paint thickLinePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint maleFillPaint;
    private Paint femaleFillPaint;
    private Paint backgroundPaint;

    private float scale = 0.5f;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    private static float MIN_ZOOM = 0.2f;
    private static float MAX_ZOOM = 5f;

    private float baseOriginX;
    private float baseOriginY;
    private float originX;
    private float originY;
    private float offsetX;
    private float offsetY;

    private float boxPadding = 10f;
    private float boxHalfWidth = 200f;
    private float boxHalfHeight = 100f;
    private float boxHalfHorizontalSpacing = 50f;
    private float boxHalfVerticalSpacing = 50f;

    private float textPadding = 5f;

    private float prevTouchX;
    private float prevTouchY;
    private static final float TOUCH_TOLERANCE = 5;

    private boolean currentlyMultiTouch = false;

    public LinesOfDescentCanvasView(Context c, AttributeSet attrs) {
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

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.parseColor("#FFFFFF"));
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        backgroundPaint.setStrokeJoin(Paint.Join.ROUND);

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

    public void configureChart(boolean isSamePerson, List<List<Integer>> lineages, MainActivity mainActivity, NameFormat nameFormat)
    {
        this.isLoaded = true;
        this.lineages = lineages;
        this.mainActivity = mainActivity;
        this.nameFormat = nameFormat;
        this.isSamePerson = isSamePerson;

        this.currentLineage = 0;
        prepareLineageForChart();

    }

    public void showNextLineage() {
        if (this.lineages == null)
            return;

        this.currentLineage++;
        if (this.currentLineage >= this.lineages.size())
            this.currentLineage = 0;
        prepareLineageForChart();
    }

    public void showPrevLineage() {
        if (this.lineages == null)
            return;

        this.currentLineage--;
        if (this.currentLineage < 0)
            this.currentLineage = this.lineages.size() - 1;
        prepareLineageForChart();
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

            drawChart(canvas, originX, originY, offsetX, offsetY, scale);
        }
    }

    private void prepareLineageForChart() {
        this.originX = 0;
        this.originY = 0;

        this.messageLine1 = "";
        this.messageLine2 = "";
        this.messageLine3 = "";
        this.lineageLine = "";
        this.individualBoxes = new ArrayList<>();

        if (lineages == null) {
            this.messageLine1 = "Individuals cannot be found";
        } else if (this.isSamePerson) {
            this.messageLine1 = AncestryUtil.generateName(mainActivity.activeIndividual, this.nameFormat) + " is the same person";
        } else if (lineages.size() == 0) {
            this.messageLine1 = AncestryUtil.generateName(mainActivity.individualsInActiveTree.get(mainActivity.rootIndividualId), this.nameFormat) + " and";
            this.messageLine2 = AncestryUtil.generateName(mainActivity.activeIndividual, this.nameFormat);
            this.messageLine3 = "are not directly related";
        } else {
            if (lineages.size() == 1)
                this.messageLine1 = String.valueOf(lineages.size()) + " line of descent found for";
            else
                this.messageLine1 = String.valueOf(lineages.size()) + " lines of descent found for";
            this.messageLine2 = AncestryUtil.generateName(mainActivity.individualsInActiveTree.get(mainActivity.rootIndividualId), this.nameFormat) + " and";
            this.messageLine3 = AncestryUtil.generateName(mainActivity.activeIndividual, this.nameFormat);

            this.lineageLine = "Lineage " + String.valueOf(this.currentLineage + 1) + " of " + String.valueOf(lineages.size());
            float boxCentreX = (boxHalfWidth + boxHalfHorizontalSpacing) * 2;
            float boxCentreY = boxHalfHeight + 200;

            List<Integer> lineage = this.lineages.get(currentLineage);

            for(int currentGeneration = lineage.size() - 1; currentGeneration >= 0; currentGeneration--) {
                Individual individual = mainActivity.individualsInActiveTree.get(lineage.get(currentGeneration));
                individualBoxes.add(new RelationshipChartIndividual(
                        individual.individualId,
                        currentGeneration,
                        AncestryUtil.generateName(individual, nameFormat),
                        AncestryUtil.generateBirthDeathDate(individual, true),
                        AncestryUtil.calculateRelationship(0, currentGeneration, individual.gender == GenderEnum.Male, true),
                        individual.gender,
                        boxCentreX,
                        boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * (lineage.size() - 1 - currentGeneration)))
                );
            }

        }

        invalidate();
    }

    private void drawChart(Canvas canvas, float originX, float originY, float offsetX, float offsetY, float scale) {
        textPaint.setStrokeWidth(2f);
        textPaint.setTextSize(28f * scale);
        fillPaint.setStrokeWidth(2f);
        linePaint.setStrokeWidth(2f);
        thickLinePaint.setStrokeWidth(6f * scale);

        for (RelationshipChartIndividual individualBox : individualBoxes) {
            if (individualBox.gender == GenderEnum.Male)
                canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), maleFillPaint);
            else
                canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), femaleFillPaint);

            canvas.drawRect(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), linePaint);

            if (individualBox.generationNumber > 0) {
                canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing + boxHalfVerticalSpacing) * scale), thickLinePaint);
            }

            textPaint.setTextSize(28f * scale);
            drawText(canvas, individualBox.name, individualBox.dates, individualBox.relationship, (boxHalfWidth * 2f - boxPadding) * scale, originX + offsetX + (individualBox.boxCentreX * scale), originY + offsetY + (individualBox.boxCentreY * scale), scale);
        }


        textPaint.setTextSize(38f);
        canvas.drawText(messageLine1, 10, textPaint.getTextSize() + 10, textPaint);
        canvas.drawText(messageLine2, 10, (textPaint.getTextSize() * 2) + 10, textPaint);
        canvas.drawText(messageLine3, 10, (textPaint.getTextSize() * 3) + 10, textPaint);

        canvas.drawText(lineageLine, 10, (textPaint.getTextSize() * 4) + 10, textPaint);

    }

    public Bitmap renderBitmap() throws Exception {
        if (isLoaded) {
            textPaint.setColor(Color.parseColor("#222222"));
            femaleFillPaint.setColor(Color.parseColor("#FF0AB5"));
            maleFillPaint.setColor(Color.parseColor("#BABAFF"));

            float width = (boxHalfWidth + boxHalfHorizontalSpacing) * 4f;
            int maxGen = lineages.get(this.currentLineage).size();
            float height = (boxHalfHeight + boxHalfVerticalSpacing) * 2f * maxGen + boxHalfHeight + 200 + boxHalfVerticalSpacing;

            Bitmap bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawRect(0, 0, width, height, backgroundPaint);

            this.drawChart(canvas, originX, originY, 0, 0, 1);

            return bitmap;
        } else {
            throw new Exception("Not Loaded");
        }
    }

    private void drawText(Canvas canvas, String name, String dates, String relationship, float maxWidth, float centreX, float centreY, float scale) {
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
                wrapped += "\n" + relationship;
                wrapped = wrapped.replaceAll("\n\n", "\n");
                lines = wrapped.split("\n");
                if (lines.length == 5) {
                    String[] fixedLines = new String[3];
                    fixedLines[0] = lines[0];
                    fixedLines[1] = lines[1] + " " + lines[2];
                    fixedLines[2] = lines[3];
                    fixedLines[3] = lines[4];
                    lines = fixedLines;
                }
            } catch (Exception e) {
                lines = new String[3];
                lines[0] = name;
                lines[1] = dates;
                lines[2] = relationship;
            }
        } else {
            lines = new String[3];
            lines[0] = name;
            lines[1] = dates;
            lines[2] = relationship;
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
        } else if (lines.length == 4) {
            textOffsetY = ((lineSpacing / 2f) * -1) - (textPaint.getTextSize() + lineSpacing);
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
