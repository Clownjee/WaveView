package com.ghizzoniandrea.waveview.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ghizzoniandrea.waveview.R;

// y=Asin(ωx+φ)+k
class Wave extends View {
    private final int WAVE_HEIGHT_LARGE = 28;
    private final int WAVE_HEIGHT_MIDDLE = 12;
    private final int WAVE_HEIGHT_LITTLE = 5;

    private final float WAVE_LENGTH_MULTIPLE_LARGE = 1.5f;
    private final float WAVE_LENGTH_MULTIPLE_MIDDLE = 1f;
    private final float WAVE_LENGTH_MULTIPLE_LITTLE = 0.5f;

    private final float WAVE_HZ_FAST = 0.13f;
    private final float WAVE_HZ_NORMAL = 0.09f;
    private final float WAVE_HZ_SLOW = 0.05f;

    public final int DEFAULT_ABOVE_WAVE_ALPHA = 80;
    public final int DEFAULT_MIDDLE_WAVE_ALPHA = 45;
    public final int DEFAULT_BELOW_WAVE_ALPHA = 20;

    private final float X_SPACE = 40;
    private final double PI2 = 2 * Math.PI;

    private Path mAboveWavePath = new Path();
    private Path mMiddleWavePath = new Path();
    private Path mBelowWavePath = new Path();

    private Paint mAboveWavePaint = new Paint();
    private Paint mMiddleWavePaint = new Paint();
    private Paint mBelowWavePaint = new Paint();

    private int mAboveWaveColor;
    private int mMiddleWaveColor;
    private int mBelowWaveColor;

    private float mWaveMultiple;
    private float mWaveLength;
    private int mWaveHeight;
    private float mMaxRight;
    private float mWaveHz;

    // wave animation
    private float mAboveOffset = 0.0f;
    private float mMiddleOffset;
    private float mBelowOffset;

    private RefreshProgressRunnable mRefreshProgressRunnable;

    private int left, right, bottom;
    // ω
    private double omega;

    public Wave(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.waveViewStyle);
    }

    public Wave(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mBelowWavePath, mBelowWavePaint);
        canvas.drawPath(mMiddleWavePath, mMiddleWavePaint);
        canvas.drawPath(mAboveWavePath, mAboveWavePaint);
    }

    public void setAboveWaveColor(int aboveWaveColor) {
        this.mAboveWaveColor = aboveWaveColor;
    }

    public void setMiddleWaveColor(int middleWaveColor) {
        this.mMiddleWaveColor = middleWaveColor;
    }

    public void setBlowWaveColor(int blowWaveColor) {
        this.mBelowWaveColor = blowWaveColor;
    }

    public Paint getAboveWavePaint() {
        return mAboveWavePaint;
    }

    public Paint getMiddleWavePaint() {
        return mMiddleWavePaint;
    }

    public Paint getBlowWavePaint() {
        return mBelowWavePaint;
    }

    public void initializeWaveSize(int waveMultiple, int waveHeight, int waveHz) {
        mWaveMultiple = getWaveMultiple(waveMultiple);
        mWaveHeight = getWaveHeight(waveHeight);
        mWaveHz = getWaveHz(waveHz);
        mMiddleOffset = mWaveHeight * 0.4f;
        mBelowOffset = mWaveHeight * 0.8f;
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mWaveHeight * 2);
        setLayoutParams(params);
    }

    public void initializePainters() {
        mAboveWavePaint.setColor(mAboveWaveColor);
        mAboveWavePaint.setAlpha(DEFAULT_ABOVE_WAVE_ALPHA);
        mAboveWavePaint.setStyle(Paint.Style.FILL);
        mAboveWavePaint.setAntiAlias(true);

        mMiddleWavePaint.setColor(mMiddleWaveColor);
        mMiddleWavePaint.setAlpha(DEFAULT_MIDDLE_WAVE_ALPHA);
        mMiddleWavePaint.setStyle(Paint.Style.FILL);
        mMiddleWavePaint.setAntiAlias(true);

        mBelowWavePaint.setColor(mBelowWaveColor);
        mBelowWavePaint.setAlpha(DEFAULT_BELOW_WAVE_ALPHA);
        mBelowWavePaint.setStyle(Paint.Style.FILL);
        mBelowWavePaint.setAntiAlias(true);
    }

    private float getWaveMultiple(int size) {
        switch (size) {
            case WaveView.LARGE:
                return WAVE_LENGTH_MULTIPLE_LARGE;
            case WaveView.MIDDLE:
                return WAVE_LENGTH_MULTIPLE_MIDDLE;
            case WaveView.LITTLE:
                return WAVE_LENGTH_MULTIPLE_LITTLE;
        }
        return 0;
    }

    private int getWaveHeight(int size) {
        switch (size) {
            case WaveView.LARGE:
                return WAVE_HEIGHT_LARGE;
            case WaveView.MIDDLE:
                return WAVE_HEIGHT_MIDDLE;
            case WaveView.LITTLE:
                return WAVE_HEIGHT_LITTLE;
        }
        return 0;
    }

    private float getWaveHz(int size) {
        switch (size) {
            case WaveView.LARGE:
                return WAVE_HZ_FAST;
            case WaveView.MIDDLE:
                return WAVE_HZ_NORMAL;
            case WaveView.LITTLE:
                return WAVE_HZ_SLOW;
        }
        return 0;
    }

    /**
     * calculate wave track
     */
    private void calculatePath() {
        mAboveWavePath.reset();
        mMiddleWavePath.reset();
        mBelowWavePath.reset();

        getWaveOffset();

        float y;
        mAboveWavePath.moveTo(left, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x + mAboveOffset) + mWaveHeight);
            mAboveWavePath.lineTo(x, y);
        }
        mAboveWavePath.lineTo(right, bottom);

        mMiddleWavePath.moveTo(left, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x + mMiddleOffset) + mWaveHeight);
            mMiddleWavePath.lineTo(x, y);
        }
        mMiddleWavePath.lineTo(right, bottom);

        mBelowWavePath.moveTo(left, bottom);
        for (float x = 0; x <= mMaxRight; x += X_SPACE) {
            y = (float) (mWaveHeight * Math.sin(omega * x + mBelowOffset) + mWaveHeight);
            mBelowWavePath.lineTo(x, y);
        }
        mBelowWavePath.lineTo(right, bottom);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (View.GONE == visibility) {
            removeCallbacks(mRefreshProgressRunnable);
        } else {
            removeCallbacks(mRefreshProgressRunnable);
            mRefreshProgressRunnable = new RefreshProgressRunnable();
            post(mRefreshProgressRunnable);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (mWaveLength == 0) {
                startWave();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mWaveLength == 0) {
            startWave();
        }
    }

    private void startWave() {
        if (getWidth() != 0) {
            int width = getWidth();
            mWaveLength = width * mWaveMultiple;
            left = getLeft();
            right = getRight();
            bottom = getBottom() + 6;
            mMaxRight = right + X_SPACE;
            omega = PI2 / mWaveLength;
        }
    }

    private void getWaveOffset() {
        if (mBelowOffset > Float.MAX_VALUE - 100) {
            mBelowOffset = 0;
        } else {
            mBelowOffset += mWaveHz;
        }

        if (mMiddleOffset > Float.MAX_VALUE - 100) {
            mMiddleOffset = 0;
        } else {
            mMiddleOffset += mWaveHz;
        }

        if (mAboveOffset > Float.MAX_VALUE - 100) {
            mAboveOffset = 0;
        } else {
            mAboveOffset += mWaveHz;
        }
    }

    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (Wave.this) {
                long start = System.currentTimeMillis();

                calculatePath();

                invalidate();

                long gap = 16 - (System.currentTimeMillis() - start);
                postDelayed(this, gap < 0 ? 0 : gap);
            }
        }
    }

}
