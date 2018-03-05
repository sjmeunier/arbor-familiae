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

import com.sjmeunier.arborfamiliae.util.AncestryUtil;
import com.sjmeunier.arborfamiliae.data.NameFormat;
import com.sjmeunier.arborfamiliae.data.RelationshipChartIndividual;
import com.sjmeunier.arborfamiliae.database.GenderEnum;
import com.sjmeunier.arborfamiliae.database.Individual;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

public class RelationshipCanvasView extends View {

    private boolean isLoaded = false;

    private List<Individual> rootLineage;
    private List<Individual> targetLineage;
    private List<RelationshipChartIndividual> individualBoxes;
    private boolean drawSpouse;
    private boolean isDirectDescendant;

    private String messageLine1;
    private String messageLine2;
    private String messageLine3;

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

    public RelationshipCanvasView(Context c, AttributeSet attrs) {
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


    public void configureChart(List<Individual> rootLineage, List<Individual> targetLineage, Individual ancestorSpouse, NameFormat nameFormat)
    {
        this.isLoaded = true;
        this.rootLineage = rootLineage;
        this.targetLineage = targetLineage;

        this.originX = 0;
        this.originY = 0;

        this.messageLine1 = "";
        this.messageLine2 = "";
        this.messageLine3 = "";
        this.individualBoxes = new ArrayList<>();
        drawSpouse = false;

        if (rootLineage == null || targetLineage == null || rootLineage.size() == 0 || targetLineage.size() == 0) {
            this.messageLine1 = "Individuals cannot be found";
        } else {
            if (rootLineage.size() == 1 && targetLineage.size() == 1) {
                if (rootLineage.get(0).individualId == targetLineage.get(0).individualId) {
                    this.messageLine1 = AncestryUtil.generateName(rootLineage.get(0), nameFormat) + " is the same person";
                } else {
                    this.messageLine1 = AncestryUtil.generateName(rootLineage.get(0), nameFormat) + " and";
                    this.messageLine2 = AncestryUtil.generateName(targetLineage.get(0), nameFormat);
                    this.messageLine3 = "are not directly related";
                }
            } else {
                String relationship = AncestryUtil.calculateRelationship(rootLineage.size() - 1, targetLineage.size() - 1, targetLineage.get(targetLineage.size() - 1).gender == GenderEnum.Male, false);
                this.messageLine1 = AncestryUtil.generateName(targetLineage.get(targetLineage.size() - 1), nameFormat);
                this.messageLine2 = "is the " + relationship + " of";
                this.messageLine3 = AncestryUtil.generateName(rootLineage.get(rootLineage.size() - 1), nameFormat);

                isDirectDescendant = (rootLineage.size() == 1 && targetLineage.size() > 1) || (rootLineage.size() > 1 && targetLineage.size() == 1);

                float boxCentreX = (boxHalfWidth + boxHalfHorizontalSpacing) * 2;
                float boxCentreY = boxHalfHeight + 200;

                if (isDirectDescendant) {
                    if (targetLineage.size() == 1) {
                        int generation = 0;
                        for(Individual individual : rootLineage) {
                            if (generation == 0) {
                                float rootBoxCentreX = boxCentreX;
                                individualBoxes.add(new RelationshipChartIndividual(
                                        individual.individualId,
                                        generation,
                                        AncestryUtil.generateName(individual, nameFormat),
                                        AncestryUtil.generateBirthDeathDate(individual, true),
                                        AncestryUtil.calculateRelationship(rootLineage.size() - 1, generation, individual.gender == GenderEnum.Male, true),
                                        individual.gender,
                                        rootBoxCentreX,
                                        boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                                );
                            } else {
                                individualBoxes.add(new RelationshipChartIndividual(
                                        individual.individualId,
                                        generation,
                                        AncestryUtil.generateName(individual, nameFormat),
                                        AncestryUtil.generateBirthDeathDate(individual, true),
                                        AncestryUtil.calculateRelationship(rootLineage.size() - 1, generation, individual.gender == GenderEnum.Male, true),
                                        individual.gender,
                                        boxCentreX,
                                        boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                                );
                            }
                            generation++;
                        }
                    } else {
                        int generation = 0;
                        for(Individual individual : targetLineage) {
                            if (generation == 0) {
                                float rootBoxCentreX = boxCentreX;
                                individualBoxes.add(new RelationshipChartIndividual(
                                        individual.individualId,
                                        generation,
                                        AncestryUtil.generateName(individual, nameFormat),
                                        AncestryUtil.generateBirthDeathDate(individual, true),
                                        AncestryUtil.calculateRelationship(generation, targetLineage.size() - 1, individual.gender == GenderEnum.Male, true),
                                        individual.gender,
                                        rootBoxCentreX,
                                        boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                                );
                            } else {
                                individualBoxes.add(new RelationshipChartIndividual(
                                        individual.individualId,
                                        generation,
                                        AncestryUtil.generateName(individual, nameFormat),
                                        AncestryUtil.generateBirthDeathDate(individual, true),
                                        AncestryUtil.calculateRelationship(generation, targetLineage.size() - 1, individual.gender == GenderEnum.Male, true),
                                        individual.gender,
                                        boxCentreX,
                                        boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                                );
                            }
                            generation++;
                        }
                    }
                } else {
                    int generation = 0;
                    for(Individual individual : rootLineage) {
                        if (generation == 0) {
                            float rootBoxCentreX = boxCentreX;
                            if (ancestorSpouse != null) {
                                drawSpouse = true;
                                rootBoxCentreX = rootBoxCentreX - boxHalfWidth - boxHalfHorizontalSpacing;

                                individualBoxes.add(new RelationshipChartIndividual(
                                        ancestorSpouse.individualId,
                                        -1,
                                        AncestryUtil.generateName(ancestorSpouse, nameFormat),
                                        AncestryUtil.generateBirthDeathDate(ancestorSpouse, true),
                                        AncestryUtil.calculateRelationship(rootLineage.size() - 1, generation, ancestorSpouse.gender == GenderEnum.Male, true),
                                        ancestorSpouse.gender,
                                        rootBoxCentreX + ((boxHalfWidth + boxHalfHorizontalSpacing) * 2),
                                        boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                                );
                            }

                            individualBoxes.add(new RelationshipChartIndividual(
                                    individual.individualId,
                                    generation,
                                    AncestryUtil.generateName(individual, nameFormat),
                                    AncestryUtil.generateBirthDeathDate(individual, true),
                                    AncestryUtil.calculateRelationship(rootLineage.size() - 1, generation, individual.gender == GenderEnum.Male, true),
                                    individual.gender,
                                    rootBoxCentreX,
                                    boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                            );
                            boxCentreX = boxCentreX - boxHalfWidth - boxHalfHorizontalSpacing;
                        } else {
                            individualBoxes.add(new RelationshipChartIndividual(
                                    individual.individualId,
                                    generation,
                                    AncestryUtil.generateName(individual, nameFormat),
                                    AncestryUtil.generateBirthDeathDate(individual, true),
                                    AncestryUtil.calculateRelationship(rootLineage.size() - 1, generation, individual.gender == GenderEnum.Male, true),
                                    individual.gender,
                                    boxCentreX,
                                    boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                            );
                        }
                        generation++;
                    }

                    generation = 0;
                    boxCentreX = boxCentreX + ((boxHalfWidth + boxHalfHorizontalSpacing) * 2);
                    for(Individual individual : targetLineage) {
                        if (generation > 0) {
                            individualBoxes.add(new RelationshipChartIndividual(
                                    individual.individualId,
                                    generation,
                                    AncestryUtil.generateName(individual, nameFormat),
                                    AncestryUtil.generateBirthDeathDate(individual, true),
                                    AncestryUtil.calculateRelationship(rootLineage.size() - 1, generation, individual.gender == GenderEnum.Male, false),
                                    individual.gender,
                                    boxCentreX,
                                    boxCentreY + ((boxHalfHeight + boxHalfVerticalSpacing) * 2 * generation))
                            );
                        }
                        generation++;
                    }
                }
            }
        }
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
        if (isLoaded) {
            textPaint.setColor(Color.parseColor("#DDDDDD"));
            femaleFillPaint.setColor(Color.parseColor("#44FF0AD5"));
            maleFillPaint.setColor(Color.parseColor("#44BA0AFF"));

            drawChart(canvas, originX, originY, offsetX, offsetY, scale);
        }
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

            if (individualBox.generationNumber == 0) {

                if (drawSpouse) {
                    canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), thickLinePaint);
                    canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), thickLinePaint);
                    canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), thickLinePaint);
                    canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing + boxHalfVerticalSpacing) * scale), thickLinePaint);
                    canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing + boxHalfVerticalSpacing) * scale), thickLinePaint);
                } else {
                    if (isDirectDescendant) {
                        canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing + boxHalfVerticalSpacing) * scale), thickLinePaint);
                    } else {
                        canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), thickLinePaint);
                        canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing + boxHalfVerticalSpacing) * scale), thickLinePaint);
                        canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth - boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth - boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing + boxHalfVerticalSpacing) * scale), thickLinePaint);
                        canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX - boxHalfWidth - boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), originX + offsetX + ((individualBox.boxCentreX + boxHalfWidth + boxHalfHorizontalSpacing) * scale), originY + offsetY + ((individualBox.boxCentreY + boxHalfHeight + boxHalfVerticalSpacing) * scale), thickLinePaint);
                    }
                }

            } else if (individualBox.generationNumber > 1) {
                canvas.drawLine(originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight) * scale), originX + offsetX + ((individualBox.boxCentreX) * scale), originY + offsetY + ((individualBox.boxCentreY - boxHalfHeight - boxHalfVerticalSpacing - boxHalfVerticalSpacing) * scale), thickLinePaint);
            }

            textPaint.setTextSize(28f * scale);
            drawText(canvas, individualBox.name, individualBox.dates, individualBox.relationship, (boxHalfWidth * 2f - boxPadding) * scale, originX + offsetX + (individualBox.boxCentreX * scale), originY + offsetY + (individualBox.boxCentreY * scale), scale);
        }


        textPaint.setTextSize(38f);
        canvas.drawText(messageLine1, 10, textPaint.getTextSize() + 10, textPaint);
        canvas.drawText(messageLine2, 10, (textPaint.getTextSize() * 2) + 10, textPaint);
        canvas.drawText(messageLine3, 10, (textPaint.getTextSize() * 3) + 10, textPaint);

    }

    public Bitmap renderBitmap() throws Exception {
        if (isLoaded) {
            textPaint.setColor(Color.parseColor("#222222"));
            femaleFillPaint.setColor(Color.parseColor("#FF0AB5"));
            maleFillPaint.setColor(Color.parseColor("#BABAFF"));

            float width = (boxHalfWidth + boxHalfHorizontalSpacing) * 4f;
            int maxGen = Math.max(rootLineage.size(), targetLineage.size());
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
