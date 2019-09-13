package www.develpoeramit.linechartviewlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Amit on 2019/9/13.
 */

public class LineChartView extends View {

    private Paint linePaint;
    private Paint pointPaint;
    private Paint tablePaint;
    private Paint textRulerPaint;
    private Paint textPointPaint;

    private Path linePath;
    private Path tablePath;

    private int mWidth, mHeight;

    private List<Data> dataList = new ArrayList<>();

    private Point[] linePoints;
    private int stepStart;
    private int stepEnd;
    private int stepSpace;
    private int stepSpaceDefault = 10;
    private int stepSpaceDP = stepSpaceDefault;
    private int topSpace, bottomSpace;
    private int tablePadding;
    private int tablePaddingDP = 20;

    private int maxValue, minValue;
    private int rulerValueDefault = 10;
    private int rulerValue = rulerValueDefault;
    private int rulerValuePadding;
    private int rulerValuePaddingDP = 8;
    private float heightPercent = 0.618f;

    private int lineColor = Color.parseColor("#286DD4");
    private float lineWidthDP = 2f;

    private int pointColor = Color.parseColor("#FF4081");
    private float pointWidthDefault = 8f;
    private float pointWidthDP = pointWidthDefault;

    private int tableColor = Color.parseColor("#BBBBBB");
    private float tableWidthDP = 0.5f;

    private int rulerTextColor = tableColor;
    private float rulerTextSizeSP = 10f;

    private int pointTextColor = Color.parseColor("#009688");
    private float pointTextSizeSP = 10f;

    private boolean isShowTable = false;
    private boolean isBezierLine = false;
    private boolean isCubePoint = false;
    private boolean isInitialized = false;
    private boolean isPlayAnim = false;

    private ValueAnimator valueAnimator;
    private float currentValue = 0f;
    private boolean isAnimating = false;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setupView();
    }

    private void setupView() {
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(dip2px(lineWidthDP));

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(pointColor);
        pointPaint.setStrokeWidth(dip2px(pointWidthDP));

        tablePaint = new Paint();
        tablePaint.setAntiAlias(true);
        tablePaint.setStyle(Paint.Style.STROKE);
        tablePaint.setColor(tableColor);
        tablePaint.setStrokeWidth(dip2px(tableWidthDP));

        textRulerPaint = new Paint();
        textRulerPaint.setAntiAlias(true);
        textRulerPaint.setStyle(Paint.Style.FILL);
        textRulerPaint.setTextAlign(Paint.Align.CENTER);
        textRulerPaint.setColor(rulerTextColor);
        textRulerPaint.setTextSize(sp2px(rulerTextSizeSP));

        textPointPaint = new Paint();
        textPointPaint.setAntiAlias(true);
        textPointPaint.setStyle(Paint.Style.FILL);
        textPointPaint.setTextAlign(Paint.Align.CENTER);
        textPointPaint.setColor(pointTextColor);
        textPointPaint.setTextSize(sp2px(pointTextSizeSP));

        linePath = new Path();
        tablePath = new Path();

        resetParam();
    }

    private void initAnim() {
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(dataList.size() * 150);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentValue = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                currentValue = 0f;
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                currentValue = 1f;
                isAnimating = false;
                isPlayAnim = false;
            }
        });
        valueAnimator.setStartDelay(500);
    }

    private void resetParam() {
        linePath.reset();
        tablePath.reset();
        stepSpace = dip2px(stepSpaceDP);
        tablePadding = dip2px(tablePaddingDP);
        rulerValuePadding = dip2px(rulerValuePaddingDP);
        stepStart = tablePadding * (isShowTable ? 2 : 1);
        stepEnd = stepStart + stepSpace * (dataList.size() - 1);
        topSpace = bottomSpace = tablePadding;
        linePoints = new Point[dataList.size()];

        initAnim();
        isInitialized = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = tablePadding + getTableEnd() + getPaddingLeft() + getPaddingRight();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == heightMode) {
            height = getPaddingTop() + getPaddingBottom() + height;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.translate(0f, mHeight / 2f + (getViewDrawHeight() + topSpace + bottomSpace) / 2f);

        if (!isInitialized) {
            setupLine();
        }

        if (isShowTable) {
            drawTable(canvas);
        }
        drawLine(canvas);
        drawLinePoints(canvas);
    }

    private void drawText(Canvas canvas, Paint textPaint, String text, float x, float y) {
        canvas.drawText(text, x, y, textPaint);
    }


    private void drawRulerYText(Canvas canvas, String text, float x, float y) {
        textRulerPaint.setTextAlign(Paint.Align.RIGHT);
        Paint.FontMetrics fontMetrics = textRulerPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offsetY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = y + offsetY;
        float newX = x - rulerValuePadding;
        drawText(canvas, textRulerPaint, text, newX, newY);
    }

    private void drawRulerXText(Canvas canvas, String text, float x, float y) {
        textRulerPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = textRulerPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offsetY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = y + offsetY + rulerValuePadding;
        drawText(canvas, textRulerPaint, text, x, newY);
    }
    private void drawLinePointText(Canvas canvas, String text, float x, float y) {
        textPointPaint.setTextAlign(Paint.Align.CENTER);
        float newY = y - rulerValuePadding;
        drawText(canvas, textPointPaint, text, x, newY);
    }

    private int getTableStart() {
        return isShowTable ? stepStart + tablePadding : stepStart;
    }

    private int getTableEnd() {
        return isShowTable ? stepEnd + tablePadding : stepEnd;
    }

    private void drawTable(Canvas canvas) {
        int tableEnd = getTableEnd();

        int rulerCount = maxValue / rulerValue;
        int rulerMaxCount = maxValue % rulerValue > 0 ? rulerCount + 1 : rulerCount;
        int rulerMax = rulerValue * rulerMaxCount + rulerValueDefault;

        tablePath.moveTo(stepStart, -getValueHeight(rulerMax));
        tablePath.lineTo(stepStart, 0);
        tablePath.lineTo(tableEnd, 0);

        int startValue = minValue - (minValue > 0 ? 0 : minValue % rulerValue);
        int endValue = (maxValue + rulerValue);

        do {
            int startHeight = -getValueHeight(startValue);
            tablePath.moveTo(stepStart, startHeight);
            tablePath.lineTo(tableEnd, startHeight);
            drawRulerYText(canvas, String.valueOf(startValue), stepStart, startHeight);
            startValue += rulerValue;
        } while (startValue < endValue);

        canvas.drawPath(tablePath, tablePaint);
        drawRulerXValue(canvas);
    }

    private void drawRulerXValue(Canvas canvas) {
        if (linePoints == null) return;
        for (int i = 0; i < linePoints.length; i++) {
            Point point = linePoints[i];
            if (point == null) break;
            drawRulerXText(canvas, String.valueOf(i), linePoints[i].x, 0);
        }
    }

    private void drawLine(Canvas canvas) {
        if (isPlayAnim) {
            Path dst = new Path();
            PathMeasure measure = new PathMeasure(linePath, false);
            measure.getSegment(0, currentValue * measure.getLength(), dst, true);
            canvas.drawPath(dst, linePaint);
        } else {
            canvas.drawPath(linePath, linePaint);
        }
    }

    private void drawLinePoints(Canvas canvas) {
        if (linePoints == null) return;

        float pointWidth = dip2px(pointWidthDP) / 2;
        int pointCount = linePoints.length;
        if (isPlayAnim) {
            pointCount = Math.round(currentValue * linePoints.length);
        }
        for (int i = 0; i < pointCount; i++) {
            Point point = linePoints[i];
            if (point == null) break;
            if (isCubePoint) {
                canvas.drawPoint(point.x, point.y, pointPaint);
            } else {
                canvas.drawCircle(point.x, point.y, pointWidth, pointPaint);
            }

            drawLinePointText(canvas, String.valueOf(dataList.get(i).getValue()), point.x, point.y);
        }
    }
    private int getValueHeight(int value) {
        float valuePercent = Math.abs(value - minValue) * 100f / (Math.abs(maxValue - minValue) * 100f);
        return (int) (getViewDrawHeight() * valuePercent + bottomSpace + 0.5f);
    }

    private float getViewDrawHeight() {
        return getMeasuredHeight() * heightPercent;
    }

    private void setupLine() {
        if (dataList.isEmpty()) return;

        int stepTemp = getTableStart();
        Point pre = new Point();
        pre.set(stepTemp, -getValueHeight(dataList.get(0).getValue()));
        linePoints[0] = pre;
        linePath.moveTo(pre.x, pre.y);

        if (dataList.size() == 1) {
            isInitialized = true;
            return;
        }

        for (int i = 1; i < dataList.size(); i++) {
            Data data = dataList.get(i);
            Point next = new Point();
            next.set(stepTemp += stepSpace, -getValueHeight(data.getValue()));

            if (isBezierLine) {
                int cW = pre.x + stepSpace / 2;

                Point p1 = new Point();
                p1.set(cW, pre.y);

                Point p2 = new Point();
                p2.set(cW, next.y);

                linePath.cubicTo(p1.x, p1.y, p2.x, p2.y, next.x, next.y);
            } else {
                linePath.lineTo(next.x, next.y);
            }

            pre = next;
            linePoints[i] = next;
        }

        isInitialized = true;
    }

    private int dip2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private void refreshLayout() {
        resetParam();
        requestLayout();
        postInvalidate();
    }

    public void setData(List<Data> dataList) {
        if (dataList == null) {
            throw new RuntimeException("dataList cannot is null!");
        }
        if (dataList.isEmpty()) return;
        this.dataList.clear();
        this.dataList.addAll(dataList);

        maxValue = Collections.max(this.dataList, new Comparator<Data>() {
            @Override
            public int compare(Data o1, Data o2) {
                return o1.getValue() - o2.getValue();
            }
        }).getValue();

        minValue = Collections.min(this.dataList, new Comparator<Data>() {
            @Override
            public int compare(Data o1, Data o2) {
                return o1.getValue() - o2.getValue();
            }
        }).getValue();

        refreshLayout();
    }

    public void setShowTable(boolean showTable) {
        isShowTable = showTable;
        refreshLayout();
    }

    public void setBezierLine(boolean isBezier) {
        isBezierLine = isBezier;
        refreshLayout();
    }

    public void setCubePoint(boolean isCube) {
        isCubePoint = isCube;
        refreshLayout();
    }
 public void setRulerYSpace(int space) {
        if (space <= 0) {
            space = rulerValueDefault;
        }
        this.rulerValue = space;
        refreshLayout();
    }
    public void setStepSpace(int dp) {
        if (dp < stepSpaceDefault) {
            dp = stepSpaceDefault;
        }
        this.stepSpaceDP = dp;
        refreshLayout();
    }
    public void setPointWidth(float dp) {
        if (dp <= 0) {
            dp = pointWidthDefault;
        }
        this.pointWidthDP = dp;
        refreshLayout();
    }

    public void playAnim() {
        this.isPlayAnim = true;
        if (isAnimating) return;
        if (valueAnimator != null) {
            valueAnimator.start();
        }
    }

    public static class Data {

        int value;

        public Data(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }
}
