package com.ivan.compass;

import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static android.R.attr.width;

/**
 * @author: yifan.lin
 * @description:
 * @projectName: Compass
 * @date: 2018-05-30
 * @time: 11:16
 */
public class CompassView extends View {

    private Context mContext;
    private Canvas mCanvas;

    private int mWidth;

    private int mCenterX, mCenterY;

    private int mTextHeight;
    private int mOutSideRadius;
    private int mRingRadius;

    private Paint mDarkRedPaint;
    private Paint mDeepGrayPaint;
    private Paint mLightGrayPaint;
    private Paint mTextPaint;
    private Paint mRingPaint;
    private Paint mOutSideRingPaint;
    private Paint mNorthPaint;
    private Paint mOthersPaint;
    private Paint mCenterPaint;
    private Paint mSmallDegreePaint;
    private Paint mInnerPaint;
    private Paint mAnglePaint;

    private Rect mTextRect;
    private Rect mPositionRect;
    private Rect mCenterTextRect;
    private Rect mSecondRect;
    private Rect mThirdRect;

    private Path mOutsideTriangle;
    private Path mRingTriangle;

    private Shader mInnerShader;
    private Matrix mCameraMatrix;
    private Camera mCamera;

    private ValueAnimator mValueAnimator;
    private float mCameraRotateX;
    private float mCameraRotateY;
    private float mMaxCameraRotate = 10;

    private float mCameraTranslateX;
    private float mCameraTranslateY;
    private float mMaxCameraTranslate;

    private float mVal = 0f;
    private float mValCompare;

    private String mText;

    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        initPaint();
        initRect();
        initPath();
        mCameraMatrix = new Matrix();
        mCamera = new Camera();
    }

    private void initPaint() {
        mDarkRedPaint = new Paint();
        mDarkRedPaint.setStyle(Paint.Style.STROKE);
        mDarkRedPaint.setAntiAlias(true);
        mDarkRedPaint.setColor(mContext.getResources().getColor(R.color.darkRed));

        mDeepGrayPaint = new Paint();
        mDeepGrayPaint.setStyle(Paint.Style.STROKE);
        mDeepGrayPaint.setAntiAlias(true);
        mDeepGrayPaint.setColor(mContext.getResources().getColor(R.color.deepGray));

        mLightGrayPaint = new Paint();
        mLightGrayPaint.setStyle(Paint.Style.FILL);
        mLightGrayPaint.setAntiAlias(true);
        mLightGrayPaint.setColor(mContext.getResources().getColor(R.color.lightGray));

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(80);
        mTextPaint.setColor(mContext.getResources().getColor(R.color.white));

        mRingPaint = new Paint();
        mRingPaint.setStyle(Paint.Style.FILL);
        mRingPaint.setAntiAlias(true);
        mRingPaint.setColor(mContext.getResources().getColor(R.color.red));

        mOutSideRingPaint = new Paint();
        mOutSideRingPaint.setStyle(Paint.Style.FILL);
        mOutSideRingPaint.setAntiAlias(true);
        mOutSideRingPaint.setColor(mContext.getResources().getColor(R.color.lightGray));

        mNorthPaint = new Paint();
        mNorthPaint.setStyle(Paint.Style.STROKE);
        mNorthPaint.setAntiAlias(true);
        mNorthPaint.setTextSize(40);
        mNorthPaint.setColor(mContext.getResources().getColor(R.color.red));

        mOthersPaint = new Paint();
        mOthersPaint.setStyle(Paint.Style.STROKE);
        mOthersPaint.setAntiAlias(true);
        mOthersPaint.setTextSize(40);
        mOthersPaint.setColor(mContext.getResources().getColor(R.color.white));

        mCenterPaint = new Paint();
        mCenterPaint.setStyle(Paint.Style.STROKE);
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setTextSize(120);
        mCenterPaint.setColor(mContext.getResources().getColor(R.color.white));

        mSmallDegreePaint = new Paint();
        mSmallDegreePaint.setStyle(Paint.Style.STROKE);
        mSmallDegreePaint.setAntiAlias(true);
        mSmallDegreePaint.setTextSize(30);
        mSmallDegreePaint.setColor(mContext.getResources().getColor(R.color.lightGray));

        mInnerPaint = new Paint();
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setAntiAlias(true);

        mAnglePaint = new Paint();
        mAnglePaint.setStyle(Paint.Style.STROKE);
        mAnglePaint.setAntiAlias(true);
        mAnglePaint.setColor(mContext.getResources().getColor(R.color.red));
    }

    private void initRect() {
        mTextRect = new Rect();
        mPositionRect = new Rect();
        mCenterTextRect = new Rect();
        mSecondRect = new Rect();
        mThirdRect = new Rect();
    }

    private void initPath() {
        mOutsideTriangle = new Path();
        mRingTriangle = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mWidth = Math.min(widthSize, heightSize);
        if (widthMode == MeasureSpec.UNSPECIFIED) {
            mWidth = heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            mWidth = widthSize;
        }
        mTextHeight = mWidth / 3;
        mCenterX = mWidth / 2;
        mCenterY = mWidth / 2 + mTextHeight;
        mOutSideRadius = mWidth * 3 / 8;
        mRingRadius = mOutSideRadius * 4 / 5;
        mMaxCameraTranslate = 0.02f * mOutSideRadius;

        setMeasuredDimension(mWidth, mWidth + mWidth / 3);
    }

    public void setVal(float val) {
        this.mVal = val;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        //设置Camera矩阵 实现3D效果
        set3DMatrix();
        //画文字
        drawText();
        //画指南针外圈
        drawCompassOutSide();
        //画指南针外接圆
        drawCompassRing();
        //画内部渐变颜色圆
        drawInnerCircle();
        //画指南针内部刻度
        drawCompassMeter();
        //画圆心数字
        drawCenterText();
    }

    private void set3DMatrix() {
        mCameraMatrix.reset();
        mCamera.save();
        mCamera.rotateX(mCameraTranslateX);
        mCamera.rotateY(mCameraTranslateY);
        mCamera.getMatrix(mCameraMatrix);
        mCamera.restore();

        mCameraMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        mCameraMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
        mCanvas.concat(mCameraMatrix);
    }

    private void drawText() {
        if (mVal <= 15 || mVal >= 345) {
            mText = "北";
        } else if (mVal > 15 || mVal <= 75) {
            mText = "东北";
        } else if (mVal > 75 || mVal <= 105) {
            mText = "东";
        } else if (mVal > 105 || mVal <= 165) {
            mText = "东南";
        } else if (mVal > 165 || mVal <= 195) {
            mText = "南";
        } else if (mVal > 195 || mVal <= 255) {
            mText = "西南";
        } else if (mVal > 255 || mVal <= 285) {
            mText = "西";
        } else if (mVal > 285 || mVal <= 345) {
            mText = "西北";
        }

        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
        int textWidth = mTextRect.width();
        mCanvas.drawText(mText, mWidth / 2 - textWidth / 2, mTextHeight / 2, mTextPaint);
    }

    private void drawCompassOutSide() {
        mCanvas.save();
        int triangleHeight = 40;
        mOutsideTriangle.moveTo(mWidth / 2, mTextHeight - triangleHeight);

        float triangleSide = 46.18f;

        mOutsideTriangle.lineTo(mWidth / 2 - triangleSide / 2, mTextHeight);
        mOutsideTriangle.lineTo(mWidth / 2 + triangleSide / 2, mTextHeight);
        mOutsideTriangle.close();
        mCanvas.drawPath(mOutsideTriangle, mOutSideRingPaint);

        mDarkRedPaint.setStrokeWidth(5f);
        mLightGrayPaint.setStrokeWidth(5f);
        mDeepGrayPaint.setStrokeWidth(3f);
        mLightGrayPaint.setStyle(Paint.Style.STROKE);
        mCanvas.drawArc(mWidth / 2 - mOutSideRadius, mTextHeight, mWidth / 2 + mOutSideRadius, mTextHeight + mOutSideRadius * 2, -80, 120, false, mLightGrayPaint);
        mCanvas.drawArc(mWidth / 2 - mOutSideRadius, mTextHeight, mWidth / 2 + mOutSideRadius, mTextHeight + mOutSideRadius * 2, 40, 20, false, mDeepGrayPaint);
        mCanvas.drawArc(mWidth / 2 - mOutSideRadius, mTextHeight, mWidth / 2 + mOutSideRadius, mTextHeight + mOutSideRadius * 2, -100, -20, false, mLightGrayPaint);
        mCanvas.drawArc(mWidth / 2 - mOutSideRadius, mTextHeight, mWidth / 2 + mOutSideRadius, mTextHeight + mOutSideRadius * 2, -120, -120, false, mDarkRedPaint);
        mCanvas.restore();
    }

    private void drawCompassRing() {
        mCanvas.save();

        int triangleHeight = (mOutSideRadius - mRingRadius) / 2;

        mCanvas.rotate(-mVal, mWidth / 2, mOutSideRadius + mTextHeight);
        mRingTriangle.moveTo(mWidth / 2, triangleHeight + mTextHeight);

        float triangleSide = (float) ((triangleHeight / (Math.sqrt(3))) * 2);
        mRingTriangle.lineTo(mWidth / 2 - triangleSide / 2, mTextHeight + triangleHeight * 2);
        mRingTriangle.lineTo(mWidth / 2 + triangleSide / 2, mTextHeight + triangleHeight * 2);
        mRingTriangle.close();
        mCanvas.drawPath(mRingTriangle, mRingPaint);
        mCanvas.drawArc(mWidth / 2 - mRingRadius, mTextHeight + mOutSideRadius - mRingRadius, mWidth / 2 + mRingRadius, mTextHeight + mOutSideRadius + mRingRadius, -85, 350, false, mDeepGrayPaint);
        mAnglePaint.setStrokeWidth(5f);
        if (mVal <= 180) {
            mValCompare = mVal;
            mCanvas.drawArc(mWidth / 2 - mRingRadius, mTextHeight + mOutSideRadius - mRingRadius, mWidth / 2 + mRingRadius, mTextHeight + mOutSideRadius + mRingRadius, -85, mValCompare, false, mAnglePaint);
        } else {
            mValCompare = 350 - mVal;

            mCanvas.drawArc(mWidth / 2 - mRingRadius, mTextHeight + mOutSideRadius - mRingRadius, mWidth / 2 + mRingRadius, mTextHeight + mOutSideRadius + mRingRadius, -95, mValCompare, false, mAnglePaint);
        }
        mCanvas.restore();
    }

    private void drawInnerCircle() {
        mInnerShader = new RadialGradient(mWidth / 2, mOutSideRadius + mTextHeight, mRingRadius - 40, Color.parseColor("#323232"), Color.parseColor("#000000"), Shader.TileMode.CLAMP);
        mInnerPaint.setShader(mInnerShader);
        mCanvas.drawCircle(mWidth / 2, mOutSideRadius + mTextHeight, mRingRadius - 40, mInnerPaint);
    }

    private void drawCompassMeter() {
        mCanvas.save();
        mNorthPaint.getTextBounds("N", 0, 1, mPositionRect);
        int nTextWidth = mPositionRect.width();
        int nTextHeight = mPositionRect.height();

        mNorthPaint.getTextBounds("W", 0, 1, mPositionRect);
        int wTextWidth = mPositionRect.width();
        int wTextHeight = mPositionRect.height();

        mSmallDegreePaint.getTextBounds("30", 0, 1, mSecondRect);
        int secondTextWidth = mSecondRect.width();
        int secondTextHeight = mSecondRect.height();

        mSmallDegreePaint.getTextBounds("30", 0, 1, mThirdRect);
        int thirdTextWidth = mThirdRect.width();
        int thirdTextHeight = mThirdRect.height();

        mCanvas.rotate(-mVal, mWidth / 2, mOutSideRadius + mTextHeight);

        for (int i = 0; i < 240; i++) {
            if (i == 0 || i == 60 || i == 120 || i == 180) {
                mCanvas.drawLine(getWidth() / 2, mTextHeight + mOutSideRadius - mRingRadius + 10,
                        getWidth() / 2, mTextHeight + mOutSideRadius - mRingRadius + 30, mDeepGrayPaint);
            } else {
                mCanvas.drawLine(getWidth() / 2, mTextHeight + mOutSideRadius - mRingRadius + 10,
                        getWidth() / 2, mTextHeight + mOutSideRadius - mRingRadius + 30, mLightGrayPaint);
            }
            float x = mWidth / 2;
            float y = mTextHeight + mOutSideRadius - mRingRadius + 40;
            switch (i) {
                case 0:
                    mCanvas.drawText("N", x - nTextWidth / 2, y + nTextHeight, mNorthPaint);
                    break;
                case 20:
                    mCanvas.drawText("30", x - secondTextWidth / 2, y + secondTextHeight, mSmallDegreePaint);
                    break;
                case 40:
                    mCanvas.drawText("60", x - secondTextWidth / 2, y + secondTextHeight, mSmallDegreePaint);
                    break;
                case 60:
                    mCanvas.drawText("E", x - nTextWidth / 2, y + nTextHeight, mOthersPaint);
                    break;
                case 80:
                    mCanvas.drawText("120", x - thirdTextWidth / 2, y + thirdTextHeight, mSmallDegreePaint);
                    break;
                case 100:
                    mCanvas.drawText("150", x - thirdTextWidth / 2, y + thirdTextHeight, mSmallDegreePaint);
                    break;
                case 120:
                    mCanvas.drawText("S", x - nTextWidth / 2, y + nTextHeight, mOthersPaint);
                    break;
                case 140:
                    mCanvas.drawText("210", x - thirdTextWidth / 2, y + thirdTextHeight, mSmallDegreePaint);
                    break;
                case 160:
                    mCanvas.drawText("240", x - thirdTextWidth / 2, y + thirdTextHeight, mSmallDegreePaint);
                    break;
                case 180:
                    mCanvas.drawText("W", x - wTextWidth / 2, y + wTextHeight, mOthersPaint);
                    break;
                case 200:
                    mCanvas.drawText("300", x - thirdTextWidth / 2, y + thirdTextHeight, mSmallDegreePaint);
                    break;
                case 220:
                    mCanvas.drawText("330", x - thirdTextWidth / 2, y + thirdTextHeight, mSmallDegreePaint);
                    break;
            }
            mCanvas.rotate(1.5f, mCenterX, mOutSideRadius + mTextHeight);
        }
        mCanvas.restore();
    }

    private void drawCenterText() {
        String centerText = (int) mVal + "°";
        mCenterPaint.getTextBounds(centerText, 0, centerText.length(), mCenterTextRect);
        int centerTextWidth = mCenterTextRect.width();
        int centerTextHeight = mCenterTextRect.height();
        mCanvas.drawText(centerText, mWidth / 2 - centerTextWidth / 2, mTextHeight + mOutSideRadius + centerTextHeight / 5, mCenterPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mValueAnimator != null && mValueAnimator.isRunning()) {
                    mValueAnimator.cancel();
                }
                getCameraRotate(event);
                getCameraTranslate(event);
                break;
            case MotionEvent.ACTION_MOVE:
                getCameraRotate(event);
                getCameraTranslate(event);
                break;
            case MotionEvent.ACTION_UP:
                startRestore();
                break;
        }
        return true;
    }

    private void startRestore() {
        final String cameraRotateXName = "cameraRotateX";
        final String cameraRotateYName = "cameraRotateY";
        final String canvasTranslateXName = "canvasTranslateX";
        final String canvasTranslateYName = "canvasTranslateY";

        PropertyValuesHolder cameraRotateXHolder = PropertyValuesHolder.ofFloat(cameraRotateXName, mCameraRotateX, 0);
        PropertyValuesHolder cameraRotateYHolder = PropertyValuesHolder.ofFloat(cameraRotateYName, mCameraRotateY, 0);
        PropertyValuesHolder canvasTranslateXHolder = PropertyValuesHolder.ofFloat(canvasTranslateXName, mCameraTranslateX, 0);
        PropertyValuesHolder canvasTranslateYHolder = PropertyValuesHolder.ofFloat(canvasTranslateYName, mCameraTranslateY, 0);

        mValueAnimator = ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder, cameraRotateYHolder, canvasTranslateXHolder, canvasTranslateYHolder);
        mValueAnimator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                float f = 0.571429f;
                return (float) (Math.pow(2, -2 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });
        mValueAnimator.setDuration(1000);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCameraRotateX = (float) animation.getAnimatedValue(cameraRotateXName);
                mCameraRotateY = (float) animation.getAnimatedValue(cameraRotateYName);
                mCameraTranslateX = (float) animation.getAnimatedValue(canvasTranslateXName);
                mCameraTranslateX = (float) animation.getAnimatedValue(canvasTranslateYName);
            }
        });
        mValueAnimator.start();
    }

    private void getCameraTranslate(MotionEvent event) {
        float translateX = event.getX() - getWidth() / 2;
        float translateY = event.getY() - getHeight() / 2;

        float[] percentArr = getPercent(translateX, translateY);
        mCameraRotateX = percentArr[0] * mMaxCameraRotate;
        mCameraRotateY = percentArr[1] * mMaxCameraRotate;
    }

    private void getCameraRotate(MotionEvent event) {
        float rotateX = -(event.getY() - getHeight() / 2);
        float rotateY = event.getX() - getWidth() / 2;

        float[] percentArr = getPercent(rotateX, rotateY);
        mCameraRotateX = percentArr[0] * mMaxCameraRotate;
        mCameraRotateY = percentArr[1] * mMaxCameraRotate;
    }

    private float[] getPercent(float mCameraRotateX, float mCameraRotateY) {
        float[] percentArr = new float[2];
        float percentX = mCameraRotateX / mWidth;
        float percentY = mCameraRotateY / mWidth;

        //处理一下比例值
        if (percentX > 1) {
            percentX = 1;
        } else if (percentX < -1) {
            percentX = -1;
        }
        if (percentY > 1) {
            percentY = 1;
        } else if (percentY < -1) {
            percentY = -1;
        }
        percentArr[0] = percentX;
        percentArr[1] = percentY;
        return percentArr;
    }
}
