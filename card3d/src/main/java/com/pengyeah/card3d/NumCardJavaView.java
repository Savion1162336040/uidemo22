package com.pengyeah.card3d;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.pengyeah.card3d.func.CardRotateFunc;
import com.pengyeah.card3d.func.CardShadowDistanceFunc;
import com.pengyeah.card3d.func.CardShadowSizeFunc;
import com.pengyeah.flowview.func.IFunc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author: savion
 * @Date: 2021/12/24 16:09
 * @Des:
 **/
public class NumCardJavaView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    public static int[] colors = {
            Color.parseColor("#ef5b9c"),
            Color.parseColor("#f47920"),
            Color.parseColor("#87843b"),
            Color.parseColor("#102b6a"),
            Color.parseColor("#b7ba6b"),
            Color.parseColor("#843900"),
            Color.parseColor("#d71345"),
            Color.parseColor("#1d953f"),
            Color.parseColor("#8552a1"),
            Color.parseColor("#7a1723"),
    };
    private Camera mCamera = new Camera();
    private Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();
    /**
     * @author savion
     * @date 2021/12/24
     * @desc 3D位置参数
     **/
    private float depthZ = 0f;
    private float rotateX = 0f;
    private float rotateY = 0f;

    /**
     * @author savion
     * @date 2021/12/24
     * @desc 需要绘制的图片
     **/
    private List<Bitmap> numBms = new ArrayList<>();

    private List<Integer> numBmIds = new ArrayList<>();
    private int curShowNum = 0;
    //总页数
    private int totalPage = 0;
    private float paddingSize = 200f;
    private float cardWidth = 0f;
    private float cardHeight = 0f;
    private float cardShadowSize = 10f;
    private float cardShadowDistance = 10f;
    private boolean isNeedDrawUpCard = true;
    private boolean isNeedDrawMidCard = true;
    private boolean isNeedDrawDownCard = true;
    private IFunc cardRotateFunc = null;
    private IFunc cardShadowSizeFunc = null;
    private IFunc cardShadowDistanceFunc = null;

    private static final int STATE_LEFT_ING = 0X02;
    private static final int STATE_RIGHT_ING = 0X03;
    private static final int STATE_NORMAL = 0X04;
    private int curState = STATE_NORMAL;

    private boolean horizontal = true;

    //当前需要显示的总页数
    private int currentRenderPageSize = 7;
    //圆角大小
    private int corner = 40;
    //当前渲染页存储的图片下标序号
    private int[] currentRenderPageBms;
    private PageTransform[] pageTransforms;
    //渲染中的页码坐标等
    private PageTransform[] renderPageTrangeforms;

    private float maxRotate = 150f;
    private float midRotate = 90;
    private float minRotate = 30f;

    private float mperspScale = 1f;
    private float centerRotateYMax = 139F;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        surfaceLockDrawFrame();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    public void onDestory() {
        running = false;
        if (surfaceHolder != null) {
            surfaceHolder.getSurface().release();
            released = true;
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    public void run() {
        while (running) {
//            if (rendering) {
            long start = System.currentTimeMillis();
            surfaceLockDrawFrame();
            long end = System.currentTimeMillis();
            long offset = end - start - 40;
            //0
            //30
//                if (offset < 0) {
//                    Thread.sleep(Math.abs(offset));
//                }
//            }
        }
    }

    public static class PageTransform {
        public static final int LEFT = 1;
        public static final int RIGHT = 2;
        public static final int CENTER = 3;
        public float leftRotate = 0f;
        public float rightRotate = 0f;
        public RectF leftRect = new RectF();
        public RectF rightRect = new RectF();
        public int bmIndex = -1;
        public int direct = -1;
        //处于可见渲染下标序列
        public int renderIndex = -1;

        public PageTransform(float leftRotate, float rightRotate, RectF leftRect, RectF rightRect, int bmIndex, int direct, int renderIndex) {
            this.leftRotate = leftRotate;
            this.rightRotate = rightRotate;
            this.leftRect = leftRect;
            this.rightRect = rightRect;
            this.bmIndex = bmIndex;
            this.direct = direct;
            this.renderIndex = renderIndex;
        }

        public PageTransform(PageTransform pageTransform) {
            copyOf(pageTransform);
        }

        public void copyOf(PageTransform pageTransform) {
            if (pageTransform != null) {
                this.leftRotate = pageTransform.leftRotate;
                this.rightRotate = pageTransform.rightRotate;
                this.leftRect = pageTransform.leftRect;
                this.rightRect = pageTransform.rightRect;
                this.bmIndex = pageTransform.bmIndex;
                this.direct = pageTransform.direct;
                this.renderIndex = pageTransform.renderIndex;
            }
        }

        public boolean isLeft() {
            return direct == LEFT;
        }

        public boolean isRight() {
            return direct == RIGHT;
        }

        public boolean isCenter() {
            return direct == CENTER;
        }

        public PageTransform() {
        }

        public boolean isEmpty() {
            return leftRect == null
                    || leftRect.isEmpty()
                    || rightRect == null
                    || rightRect.isEmpty()
                    || bmIndex == -1;
        }
    }

    public NumCardJavaView(Context context) {
        this(context, null);
    }

    public NumCardJavaView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private SurfaceHolder surfaceHolder;

    public NumCardJavaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        numBmIds.add(R.drawable.p1_480);
        numBmIds.add(R.drawable.p2_480);
        numBmIds.add(R.drawable.p3_480);
        numBmIds.add(R.drawable.p4_480);
        numBmIds.add(R.drawable.p5_480);
        numBmIds.add(R.drawable.p6_480);
        numBmIds.add(R.drawable.p7_480);
        numBmIds.add(R.drawable.p8_480);
        numBmIds.add(R.drawable.p9_480);
        numBmIds.add(R.drawable.p10_480);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);

        mperspScale = getResources().getDisplayMetrics().density * 2;

        setBackgroundColor(Color.WHITE);


        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setZOrderOnTop(true);
        setFocusable(true);
        setKeepScreenOn(true);
        setFocusableInTouchMode(true);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        initNumBms();
    }

    private void initNumBms() {
        //初始化所有图片
        Paint xfermodePaint = new Paint();
        Paint roundRectPaint = new Paint();
        //roundRectPaint.setColor(Color.BLACK);
        roundRectPaint.setStyle(Paint.Style.FILL);
        roundRectPaint.setStrokeWidth(10);
        roundRectPaint.setTextSize(200);
        xfermodePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        xfermodePaint.setAntiAlias(true);
        for (int i = 0; i < numBmIds.size(); i++) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), numBmIds.get(i));
            if (b != null && !b.isRecycled()) {
                roundRectPaint.setColor(colors[i % colors.length]);
                Bitmap rb = Bitmap.createBitmap(b.getWidth(), b.getHeight(), b.getConfig());
                Canvas canvas = new Canvas(rb);
                canvas.drawRoundRect(0, 0, rb.getWidth(), rb.getHeight(), corner, corner, roundRectPaint);
                String str = String.valueOf(i);
                int measureWidth = (int) roundRectPaint.measureText(str);
                Paint.FontMetrics fm = roundRectPaint.getFontMetrics();
                int measureHeight = (int) (Math.abs(fm.ascent) + Math.abs(fm.descent));
                int measureBaseline = (int) (Math.abs(fm.ascent) - measureHeight / 2f);
                canvas.drawText(str,
                        canvas.getWidth() / 4f - measureWidth / 2f,
                        canvas.getHeight() / 2f + measureBaseline,
                        roundRectPaint);
                canvas.drawBitmap(b,
                        new Rect(0,
                                0,
                                b.getWidth(),
                                b.getHeight()),
                        new RectF(0,
                                0,
                                rb.getWidth(),
                                rb.getHeight()),
                        xfermodePaint);
                canvas.drawText(str,
                        canvas.getWidth() / 4f * 3f - measureWidth / 2f,
                        canvas.getHeight() / 2f + measureBaseline,
                        roundRectPaint);

                numBms.add(rb);
                b.recycle();
            }
        }
        totalPage = numBms.size();

        //初始化当前图片
        curShowNum = 0;
        //calShownCards(curShowNum);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (horizontal) {
            paddingSize = getWidth() * 0.15f;
            cardWidth = (getWidth() - paddingSize * 2) * 0.5f;
            cardHeight = cardWidth * 16f / 9f;
            //cardHeight = getHeight() - paddingSize * 2f;
        } else {
            cardWidth = getWidth() - paddingSize * 2;
            cardHeight = getHeight() / 2f - paddingSize;
        }
        configFunc();

        calShownCards(curShowNum);
    }

    private void configFunc() {
        cardRotateFunc = new CardRotateFunc();
        cardRotateFunc.setInParamMin(0f);
        if (horizontal) {
            cardRotateFunc.setInParamMax(cardWidth * 2);
        } else {
            cardRotateFunc.setInParamMax(cardHeight * 2);
        }
        cardRotateFunc.setOutParamMin(minRotate);
        cardRotateFunc.setOutParamMax(maxRotate);
        cardRotateFunc.setInitValue(45f);

        cardShadowSizeFunc = new CardShadowSizeFunc();
        cardShadowSizeFunc.setInParamMin(0f);
        cardShadowSizeFunc.setInParamMax(180f);
        cardShadowSizeFunc.setOutParamMax(50f);
        cardShadowSizeFunc.setOutParamMin(0f);
        cardShadowSizeFunc.setInitValue(10f);

        cardShadowDistanceFunc = new CardShadowDistanceFunc();
        cardShadowDistanceFunc.setInParamMin(0f);
        cardShadowDistanceFunc.setInParamMax(180f);
        cardShadowDistanceFunc.setOutParamMax(50f);
        cardShadowDistanceFunc.setOutParamMin(0f);
        cardShadowDistanceFunc.setInitValue(10f);

    }

    private void executeFunc(float offset) {
        float rate = (cardRotateFunc.getOutParamMin() - cardRotateFunc.getOutParamMax()) / (cardRotateFunc.getInParamMax() - cardRotateFunc.getInParamMin());
        float initH = ((cardRotateFunc.getOutParamMin() - cardRotateFunc.getOutParamMax()) + cardRotateFunc.getInitValue()) / rate;
        if (horizontal) {
            rotateY = cardRotateFunc.execute(initH + offset);
        } else {
            rotateX = cardRotateFunc.execute(initH + offset);
        }
        Log.e("savion", String.format("计算角度:ima:%s,imi:%s,ra:%s,iH:%s,oS:%s,%s==%s",
                cardRotateFunc.getInParamMax(),
                cardRotateFunc.getInParamMin(),
                rate,
                initH,
                offset,
                rotateX,
                rotateY));

        executeShadowFunc(rotateX);
    }

    private void executeShadowFunc(float rotate) {
        cardShadowSize = cardShadowSizeFunc.execute(rotate);
        cardShadowDistance = cardShadowDistanceFunc.execute(rotate);
    }


    private void resetInitValue() {
        if (horizontal) {
            Log.e("savion", String.format("惯性回弹y,重置initValue:%s___%s",
                    cardRotateFunc.getInitValue(),
                    rotateY));
            cardRotateFunc.setInitValue(rotateY);
        } else {
            Log.e("savion", String.format("惯性回弹s,重置initValue:%s___%s",
                    cardRotateFunc.getInitValue(),
                    rotateX));
            cardRotateFunc.setInitValue(rotateX);
        }
        cardShadowSizeFunc.setInitValue(cardShadowSize);
    }

    private boolean rendering = false;
    private boolean running = false;
    private boolean released = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //drawFrame(canvas);
    }

    private void hookInvalidate() {
//        if (this instanceof SurfaceView) {
//            surfaceLockDrawFrame();
//        } else {
//            invalidate();
//        }
    }

    private final Object lock = new Object();

    private void surfaceLockDrawFrame() {
        synchronized (lock) {
            if (surfaceHolder != null && !released) {
                Canvas canvas = surfaceHolder.lockCanvas();
                try {
                    drawFrame(canvas);
                } catch (Exception e) {
                    Log.e("savion", "绘制失败:" + e.getMessage());
                }
                if (!released) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void drawFrame(Canvas canvas) {
        //if (rendering) {
        //判断状态，不同状态绘制不同内容
        judgeState(curState);
        canvas.drawColor(Color.WHITE);

//        drawUpRestCard(canvas);
//        drawDownRestCard(canvas);
//        drawUpCard(canvas);
//        drawDownCard(canvas);
        drawTransformPage(canvas);
        //中间活动card最后绘制
//        drawMidCard(canvas);
        //}
    }

    private void drawTransformPage(Canvas canvas) {
        float startRotate =
                curState == STATE_LEFT_ING
                        ? minRotate
                        :
                        curState == STATE_RIGHT_ING
                                ? maxRotate
                                : -1;
        float endRotate =
                curState == STATE_LEFT_ING
                        ? maxRotate
                        :
                        curState == STATE_RIGHT_ING
                                ? minRotate
                                : -1;
        float percent = (rotateY - startRotate) / (endRotate - startRotate);
        int i = 0;
        if (pageTransforms[i].isCenter() && i == 0) {
            //首个并且是中心则认为是显示的第一个，则开始从倒数开始绘制
            i = pageTransforms.length - 1;
        }
        for (; pageTransforms != null && i < pageTransforms.length && i >= 0; ) {
            if (pageTransforms[i] != null && !pageTransforms[i].isEmpty()) {
                if (pageTransforms[i].isLeft()) {
                    //画左
                    transformLeft(canvas, percent, pageTransforms, i);
                } else if (pageTransforms[i].isRight()) {
                    //画右
                    transformRight(canvas, percent, pageTransforms, i);
                } else if (pageTransforms[i].isCenter()) {
                    //画中间
                    transformCenter(canvas, percent, pageTransforms, i);
                }
            }
            boolean hasNext = i + 1 < pageTransforms.length;
            boolean hasPrev = i - 1 >= 0;
            if (pageTransforms[i].isLeft()) {
                //当前是左半部分
                if (hasNext) {
                    if (!pageTransforms[i + 1].isCenter()) {
                        i++;
                    } else {
                        //如果下一个是中心，则跳转到最末尾开始遍历
                        i = pageTransforms.length - 1;
                    }
                } else {
                    i++;
                }
            } else if (pageTransforms[i].isRight()) {
                //当前是右半部分
                i--;
            } else if (pageTransforms[i].isCenter()) {
                //到达中心，结束关退出
                break;
            }
        }
    }

    /**
     * @author savion
     * @date 2021/12/29
     * @desc 绘制中间
     **/
    private void transformCenter(Canvas canvas, float percent, PageTransform[] pageTransforms, int i) {
        float rotateLeftOffset = 0;
        float rotateRightOffset = 0;
        float translateLeftOffset = 0;
        float translateRightOffset = 0;
        float currentRightRotate = pageTransforms[i].rightRotate;
        float currentRightTranslate = pageTransforms[i].rightRect.left;
        float currentLeftRotate = pageTransforms[i].leftRotate;
        float currentLeftTranslate = pageTransforms[i].leftRect.left;
        PageTransform prevPageTrans = null;
        PageTransform nextPageTrans = null;
        if (curState == STATE_LEFT_ING) {
            //向左偏移
            boolean hasPrev = i - 1 < pageTransforms.length
                    && i - 1 >= 0
                    && !pageTransforms[i - 1].isEmpty();
            boolean hasNext = i + 1 < pageTransforms.length
                    && i + 1 >= 0
                    && !pageTransforms[i + 1].isEmpty();
            if (hasPrev) {
                prevPageTrans = pageTransforms[i - 1];
                float nextLeftRotate = pageTransforms[i - 1].leftRotate;
                float nextLeftTranslate = pageTransforms[i - 1].leftRect.left;
                float nextRightRotate = pageTransforms[i - 1].rightRotate;
                float nextRightTranslate = pageTransforms[i - 1].rightRect.left;

                rotateLeftOffset = (nextLeftRotate - currentLeftRotate) * percent;
                translateLeftOffset = (nextLeftTranslate - currentLeftTranslate) * percent;

                rotateRightOffset = (nextRightRotate - currentRightRotate) * percent;
                translateRightOffset = (nextRightTranslate - currentRightRotate) * percent;
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex - 1 >= 0
                        && renderIndex - 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex - 1] != null) {
                    prevPageTrans = renderPageTrangeforms[renderIndex - 1];
                    float nextLeftRotate = renderPageTrangeforms[renderIndex - 1].leftRotate;
                    float nextLeftTranslate = renderPageTrangeforms[renderIndex - 1].leftRect.left;
                    float nextRightRotate = renderPageTrangeforms[renderIndex - 1].rightRotate;
                    float nextRightTranslate = renderPageTrangeforms[renderIndex - 1].rightRect.left;

                    rotateLeftOffset = (nextLeftRotate - currentLeftRotate) * percent;
                    translateLeftOffset = (nextLeftTranslate - currentLeftTranslate) * percent;

                    rotateRightOffset = (nextRightRotate - currentRightRotate) * percent;
                    translateRightOffset = (nextRightTranslate - currentRightTranslate) * percent;
                }
            }
            if (hasNext) {
                nextPageTrans = pageTransforms[i + 1];
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex + 1 >= 0
                        && renderIndex + 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex + 1] != null) {
                    nextPageTrans = renderPageTrangeforms[renderIndex + 1];
                }
            }
        } else if (curState == STATE_RIGHT_ING) {
            //向右偏移
            boolean hasNext = i + 1 < pageTransforms.length
                    && i + 1 >= 0
                    && !pageTransforms[i + 1].isEmpty();
            boolean hasPrev = i - 1 < pageTransforms.length
                    && i - 1 >= 0
                    && !pageTransforms[i - 1].isEmpty();
            if (hasNext) {
                nextPageTrans = pageTransforms[i + 1];
                float nextRightRotate = pageTransforms[i + 1].rightRotate;
                float nextRightTranslate = pageTransforms[i + 1].rightRect.left;
                float nextLeftRotate = pageTransforms[i + 1].leftRotate;
                float nextLeftTranslate = pageTransforms[i + 1].leftRect.left;

                rotateRightOffset = (nextRightRotate - currentRightRotate) * percent;
                translateRightOffset = (nextRightTranslate - currentRightTranslate) * percent;

                rotateLeftOffset = (nextLeftRotate - currentLeftRotate) * percent;
                translateLeftOffset = (nextLeftTranslate - currentLeftTranslate) * percent;
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex + 1 >= 0
                        && renderIndex + 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex + 1] != null) {
                    nextPageTrans = renderPageTrangeforms[renderIndex + 1];
                    float nextRightRotate = renderPageTrangeforms[renderIndex + 1].rightRotate;
                    float nextRightTranslate = renderPageTrangeforms[renderIndex + 1].rightRect.left;
                    float nextLeftRotate = renderPageTrangeforms[renderIndex + 1].leftRotate;
                    float nextLeftTranslate = renderPageTrangeforms[renderIndex + 1].leftRect.left;
                    rotateRightOffset = (nextRightRotate - currentRightRotate) * percent;
                    translateRightOffset = (nextRightTranslate - currentRightTranslate) * percent;

                    rotateLeftOffset = (nextLeftRotate - currentLeftRotate) * percent;
                    translateLeftOffset = (nextLeftTranslate - currentLeftTranslate) * percent;
                }
            }
            if (hasPrev) {
                prevPageTrans = pageTransforms[i - 1];
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex - 1 >= 0
                        && renderIndex - 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex - 1] != null) {
                    prevPageTrans = renderPageTrangeforms[renderIndex - 1];
                }
            }
        }
        if (curState == STATE_LEFT_ING) {
            //正在画左动画则去除右边
            //画左
            if (translateLeftOffset != 0) {
                RectF leftRectF = new RectF(pageTransforms[i].leftRect);
                leftRectF.offset(translateLeftOffset, 0);
                cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), leftRectF, 1f, pageTransforms[i].leftRotate + rotateLeftOffset, true);
            } else {
                cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].leftRect, 1f, pageTransforms[i].leftRotate + rotateLeftOffset, true);
            }

            if (percent <= 0.5f) {
                cameraCardCenterLeftNext(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i].rightRect,
                        pageTransforms[i],
                        nextPageTrans,
                        percent);
                //右边关页旋转角度从30-150
                cameraCardCenterLeftPrev(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i],
                        prevPageTrans,
                        percent);
            } else {
                //右边关页旋转角度从30-150
                cameraCardCenterLeftPrev(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i],
                        prevPageTrans,
                        percent);
                cameraCardCenterLeftNext(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i].rightRect,
                        pageTransforms[i],
                        nextPageTrans,
                        percent);

            }
        } else if (curState == STATE_RIGHT_ING) {
            //正在画右动画则去除左边
            //画右
            if (translateRightOffset != 0) {
                RectF rightRect = new RectF(pageTransforms[i].rightRect);
                rightRect.offset(translateRightOffset, 0);
                cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), rightRect, 1f, pageTransforms[i].rightRotate + rotateRightOffset, false);
            } else {
                cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].rightRect, 1f, pageTransforms[i].rightRotate + rotateRightOffset, false);
            }

            if (percent > 0.5f) {
                cameraCardCenterRightNext(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i].rightRect,
                        pageTransforms[i],
                        nextPageTrans,
                        percent);
                //右边关页旋转角度从30-150
                cameraCardCenterRightPrev(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i],
                        prevPageTrans,
                        percent);
            } else {
                //右边关页旋转角度从30-150
                cameraCardCenterRightPrev(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i],
                        prevPageTrans,
                        percent);
                cameraCardCenterRightNext(canvas,
                        numBms.get(pageTransforms[i].bmIndex),
                        pageTransforms[i].rightRect,
                        pageTransforms[i],
                        nextPageTrans,
                        percent);

            }

        } else {
            //画左
            cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].leftRect, 1f, pageTransforms[i].leftRotate, true);
            //画右
            cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].rightRect, 1f, pageTransforms[i].rightRotate, false);
        }

//        //绘制测试描边
//        Paint paint = new Paint();
//        paint.setStrokeWidth(5);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.BLUE);
//        if (prevPageTrans != null && !prevPageTrans.isEmpty()) {
//            canvas.drawRect(prevPageTrans.leftRect, paint);
//            canvas.drawRect(prevPageTrans.leftRect.right,
//                    0,
//                    prevPageTrans.leftRect.right,
//                    getHeight(),
//                    paint);
//        }
//        canvas.drawRect(getWidth() / 2f,
//                0,
//                getWidth() / 2f,
//                getHeight(),
//                paint);
//        if (nextPageTrans != null && !nextPageTrans.isEmpty()) {
//            canvas.drawRect(nextPageTrans.rightRect, paint);
//            canvas.drawRect(nextPageTrans.leftRect.left,
//                    0,
//                    nextPageTrans.leftRect.left,
//                    getHeight(),
//                    paint);
//        }
    }


    private void cameraCardCenterLeftNext(Canvas canvas,
                                          Bitmap bitmap,
                                          RectF drawRect,
                                          PageTransform curPageTrans,
                                          PageTransform nextPageTrans,
                                          float percent) {
        if (bitmap != null
                && !bitmap.isRecycled()
                && drawRect != null
                && !drawRect.isEmpty()
                && curPageTrans != null && !curPageTrans.isEmpty()
                && nextPageTrans != null && !nextPageTrans.isEmpty()
                && nextPageTrans.bmIndex != -1) {
            Bitmap nextBitmap = numBms.get(nextPageTrans.bmIndex);
            if (nextBitmap == null
                    || nextBitmap.isRecycled()) {
                return;
            }

            float startRotate = centerRotateYMax;
            float endRotate = 30;
            float rotateY = percent * (endRotate - startRotate) + startRotate;

            if (rotateY > 90) {
                //未到达临界点时此页都是在背面，所以可以不用绘制此页
                return;
            }

            //旋转圆直径
            float circleWidth = Math.abs(curPageTrans.rightRect.left - nextPageTrans.leftRect.left);

            float xOffset = (1f - percent) * circleWidth;

            canvas.save();
            canvas.translate(xOffset, 0);

            mMatrix.reset();
            mCamera.save();
            mCamera.translate(0f, 0f, depthZ);
            mCamera.rotateX(0);
            mCamera.rotateY(rotateY);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();

            //float scale = getResources().getDisplayMetrics().density * 3;


            float[] mValues = new float[9];
            mMatrix.getValues(mValues);
            Log.e("savion", String.format("绘制图形3,%s_%s_%s_%s_%s_%s", xOffset, rotateY, percent, formatNum(mperspScale, 3), formatNum(mValues[6], 3), formatNum(mValues[7], 3)));
            mValues[6] = mValues[6] / mperspScale;
            mValues[7] = mValues[7] / mperspScale;
            mMatrix.setValues(mValues);

            //偏移旋转圆心后，图形坐标也会被偏移，所以需要将图形绘制x坐标向右偏移
            //偏移旋转中心
            mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
            mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            canvas.concat(mMatrix);
            canvas.drawBitmap(nextBitmap,
                    new Rect(0,
                            0,
                            nextBitmap.getWidth() / 2,
                            nextBitmap.getHeight()),
                    curPageTrans.leftRect,
                    mPaint);
            canvas.restore();
        }
    }

    public static float formatNum(float value, int scale) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(scale, RoundingMode.HALF_UP);
        return bigDecimal.floatValue();
    }

    /**
     * @author savion
     * @date 2021/12/30
     * @desc 由右向左旋转
     **/
    private void cameraCardCenterLeftPrev(Canvas canvas,
                                          Bitmap bitmap,
                                          PageTransform curPageTrans,
                                          PageTransform prevPageTrans,
                                          float percent) {
        if (bitmap != null
                && !bitmap.isRecycled()
                && curPageTrans != null && !curPageTrans.isEmpty()
                && prevPageTrans != null && !prevPageTrans.isEmpty()) {
            float startRotate = -30;
            float endRotate = -centerRotateYMax;
            float rotateY = percent * (endRotate - startRotate) + startRotate;

            if (rotateY < -90) {
                //到达临界点后就可以不用绘制这页了
                return;
            }

            //旋转圆直径
            float circleWidth = (curPageTrans.rightRect.left - prevPageTrans.rightRect.right);

            float xOffset = percent * circleWidth;

            canvas.save();
            canvas.translate(-xOffset, 0);
            mMatrix.reset();
            mCamera.save();
            mCamera.translate(0f, 0f, depthZ);
            mCamera.rotateX(0);
            mCamera.rotateY(rotateY);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            Log.e("savion", String.format("绘制图形222,%s__%s__%s", xOffset, percent, rotateY));
            //float scale = getResources().getDisplayMetrics().density * 3;
            float[] mValues = new float[9];
            mMatrix.getValues(mValues);
            mValues[6] = mValues[6] / mperspScale;
            mValues[7] = mValues[7] / mperspScale;
            mMatrix.setValues(mValues);

            //偏移旋转圆心后，图形坐标也会被偏移，所以需要将图形绘制x坐标向右偏移
            //偏移旋转中心
            mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
            mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            canvas.concat(mMatrix);
            canvas.drawBitmap(bitmap,
                    new Rect(bitmap.getWidth() / 2,
                            0,
                            bitmap.getWidth(),
                            bitmap.getHeight()),
                    curPageTrans.rightRect,
                    mPaint);
            canvas.restore();
        }
    }


    /**
     * @author savion
     * @date 2021/12/30
     * @desc 从左向右旋转当前页左半部分
     **/
    private void cameraCardCenterRightNext(Canvas canvas,
                                           Bitmap bitmap,
                                           RectF drawRect,
                                           PageTransform curPageTrans,
                                           PageTransform nextPageTrans,
                                           float percent) {
        if (bitmap != null
                && !bitmap.isRecycled()
                && drawRect != null
                && !drawRect.isEmpty()
                && curPageTrans != null && !curPageTrans.isEmpty()
                && nextPageTrans != null && !nextPageTrans.isEmpty()
                && nextPageTrans.bmIndex != -1) {

            float startRotate = 30;
            float endRotate = centerRotateYMax;
            float rotateY = percent * (endRotate - startRotate) + startRotate;

            if (rotateY > 90) {
                //未到达临界点时此页都是在背面，所以可以不用绘制此页
                return;
            }

            //旋转圆直径
            float circleWidth = Math.abs(curPageTrans.rightRect.left - nextPageTrans.leftRect.left);

            float xOffset = (percent) * circleWidth;
            Log.e("savion", String.format("绘制图形3,%s__%s__%s", xOffset, rotateY, percent));

            canvas.save();
            canvas.translate(xOffset, 0);

            mMatrix.reset();
            mCamera.save();
            mCamera.translate(0f, 0f, depthZ);
            mCamera.rotateX(0);
            mCamera.rotateY(rotateY);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();

            //float scale = getResources().getDisplayMetrics().density * 3;
            float[] mValues = new float[9];
            mMatrix.getValues(mValues);
            mValues[6] = mValues[6] / mperspScale;
            mValues[7] = mValues[7] / mperspScale;
            mMatrix.setValues(mValues);

            //偏移旋转圆心后，图形坐标也会被偏移，所以需要将图形绘制x坐标向右偏移
            //偏移旋转中心
            mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
            mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            canvas.concat(mMatrix);
            canvas.drawBitmap(bitmap,
                    new Rect(0,
                            0,
                            bitmap.getWidth() / 2,
                            bitmap.getHeight()),
                    curPageTrans.leftRect,
                    mPaint);
            canvas.restore();
        }
    }

    /**
     * @author savion
     * @date 2021/12/30
     * @desc 由左向右旋转前一页右半部分
     **/
    private void cameraCardCenterRightPrev(Canvas canvas,
                                           Bitmap bitmap,
                                           PageTransform curPageTrans,
                                           PageTransform prevPageTrans,
                                           float percent) {
        if (bitmap != null
                && !bitmap.isRecycled()
                && curPageTrans != null && !curPageTrans.isEmpty()
                && prevPageTrans != null && !prevPageTrans.isEmpty()
                && prevPageTrans.bmIndex != -1) {
            Bitmap preBitmap = numBms.get(prevPageTrans.bmIndex);
            if (preBitmap == null
                    || preBitmap.isRecycled()) {
                return;
            }

            float startRotate = -centerRotateYMax;
            float endRotate = -30;
            float rotateY = percent * (endRotate - startRotate) + startRotate;
//
            if (rotateY < -90) {
                //到达临界点后就可以不用绘制这页了
                return;
            }

            //旋转圆直径
            float circleWidth = (curPageTrans.rightRect.left - prevPageTrans.rightRect.right);

            float xOffset = (1f - percent) * circleWidth;

            canvas.save();
            canvas.translate(-xOffset, 0);
            mMatrix.reset();
            mCamera.save();
            mCamera.translate(0f, 0f, depthZ);
            mCamera.rotateX(0);
            mCamera.rotateY(rotateY);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            Log.e("savion", String.format("绘制图形222,%s__%s__%s", xOffset, percent, rotateY));
            //float scale = getResources().getDisplayMetrics().density * 3;
            float[] mValues = new float[9];
            mMatrix.getValues(mValues);
            mValues[6] = mValues[6] / mperspScale;
            mValues[7] = mValues[7] / mperspScale;
            mMatrix.setValues(mValues);

            //偏移旋转圆心后，图形坐标也会被偏移，所以需要将图形绘制x坐标向右偏移
            //偏移旋转中心
            mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
            mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            canvas.concat(mMatrix);
            canvas.drawBitmap(preBitmap,
                    new Rect(preBitmap.getWidth() / 2,
                            0,
                            preBitmap.getWidth(),
                            preBitmap.getHeight()),
                    curPageTrans.rightRect,
                    mPaint);
            canvas.restore();
        }
    }

    /**
     * @author savion
     * @date 2021/12/29
     * @desc 绘制右半部分
     **/
    private void transformRight(Canvas canvas, float percent, PageTransform[] pageTransforms, int i) {
        float rotateOffset = 0;
        float translateOffset = 0;
        float currentRightRotate = pageTransforms[i].rightRotate;
        float currentRightTranslate = pageTransforms[i].rightRect.left;
        if (curState == STATE_LEFT_ING) {
            //向左偏移
            boolean hasNext = i - 1 < pageTransforms.length
                    && i - 1 >= 0
                    && !pageTransforms[i - 1].isEmpty();
            if (hasNext) {
                float nextRightRotate = pageTransforms[i - 1].rightRotate;
                float nextRightTranslate = pageTransforms[i - 1].rightRect.left;
                rotateOffset = (nextRightRotate - currentRightRotate) * percent;
                translateOffset = (nextRightTranslate - currentRightTranslate) * percent;
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex - 1 >= 0
                        && renderIndex - 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex - 1] != null) {
                    float nextLeftRotate = renderPageTrangeforms[renderIndex - 1].rightRotate;
                    float nextLeftTranslate = renderPageTrangeforms[renderIndex - 1].rightRect.left;
                    rotateOffset = (nextLeftRotate - currentRightRotate) * percent;
                    translateOffset = (nextLeftTranslate - currentRightRotate) * percent;
                }
            }
        } else if (curState == STATE_RIGHT_ING) {
            //向右偏移
            boolean hasNext = i + 1 < pageTransforms.length
                    && i + 1 >= 0
                    && !pageTransforms[i + 1].isEmpty();
            if (hasNext) {
                float nextRightRotate = pageTransforms[i + 1].rightRotate;
                float nextRightTranslate = pageTransforms[i + 1].rightRect.left;
                rotateOffset = (nextRightRotate - currentRightRotate) * percent;
                translateOffset = (nextRightTranslate - currentRightTranslate) * percent;
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex + 1 >= 0
                        && renderIndex + 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex + 1] != null) {
                    float nextLeftRotate = renderPageTrangeforms[renderIndex + 1].rightRotate;
                    float nextLeftTranslate = renderPageTrangeforms[renderIndex + 1].rightRect.left;
                    rotateOffset = (nextLeftRotate - currentRightRotate) * percent;
                    translateOffset = (nextLeftTranslate - currentRightTranslate) * percent;
                }
            }
        }
        if (translateOffset != 0) {
            RectF rectF = new RectF(pageTransforms[i].rightRect);
            rectF.offset(translateOffset, 0);
            cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), rectF, 1f, pageTransforms[i].rightRotate + rotateOffset, false);
            //cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].leftRect, 1f, pageTransforms[i].leftRotate, false);
        } else {
            //cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].leftRect, 1f, pageTransforms[i].leftRotate, false);
            cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].rightRect, 1f, pageTransforms[i].rightRotate + rotateOffset, false);
        }
    }

    /**
     * @author savion
     * @date 2021/12/29
     * @desc 绘制左半部分
     **/
    private void transformLeft(Canvas canvas, float percent, PageTransform[] pageTransforms, int i) {
        //画左
        float rotateOffset = 0;
        float translateOffset = 0;
        float currentLeftRotate = pageTransforms[i].leftRotate;
        float currentLeftTranslate = pageTransforms[i].leftRect.left;
        if (curState == STATE_LEFT_ING) {
            //向左偏移
            boolean hasNext = i - 1 < pageTransforms.length
                    && i - 1 >= 0
                    && !pageTransforms[i - 1].isEmpty();
            if (hasNext) {
                float nextLeftRotate = pageTransforms[i - 1].leftRotate;
                float nextLeftTranslate = pageTransforms[i - 1].leftRect.left;
                rotateOffset = (nextLeftRotate - currentLeftRotate) * percent;
                translateOffset = (nextLeftTranslate - currentLeftTranslate) * percent;
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex - 1 >= 0
                        && renderIndex - 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex - 1] != null) {
                    float nextLeftRotate = renderPageTrangeforms[renderIndex - 1].leftRotate;
                    float nextLeftTranslate = renderPageTrangeforms[renderIndex - 1].leftRect.left;
                    rotateOffset = (nextLeftRotate - currentLeftRotate) * percent;
                    translateOffset = (nextLeftTranslate - currentLeftTranslate) * percent;
                }
            }
        } else if (curState == STATE_RIGHT_ING) {
            //向右偏移
            boolean hasNext = i + 1 < pageTransforms.length
                    && i + 1 >= 0
                    && !pageTransforms[i + 1].isEmpty();
            if (hasNext) {
                float nextLeftRotate = pageTransforms[i + 1].leftRotate;
                float nextLeftTranslate = pageTransforms[i + 1].leftRect.left;
                rotateOffset = (nextLeftRotate - currentLeftRotate) * percent;
                translateOffset = (nextLeftTranslate - currentLeftTranslate) * percent;
            } else {
                int renderIndex = pageTransforms[i].renderIndex;
                if (renderPageTrangeforms != null
                        && renderIndex + 1 >= 0
                        && renderIndex + 1 < renderPageTrangeforms.length
                        && renderPageTrangeforms[renderIndex + 1] != null) {
                    float nextLeftRotate = renderPageTrangeforms[renderIndex + 1].leftRotate;
                    float nextLeftTranslate = renderPageTrangeforms[renderIndex + 1].leftRect.left;
                    rotateOffset = (nextLeftRotate - currentLeftRotate) * percent;
                    translateOffset = (nextLeftTranslate - currentLeftTranslate) * percent;
                }
            }
        }
        if (translateOffset != 0) {
            RectF rectF = new RectF(pageTransforms[i].leftRect);
            rectF.offset(translateOffset, 0);
            cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), rectF, 1f, pageTransforms[i].leftRotate + rotateOffset, true);
        } else {
            cameraCard(canvas, numBms.get(pageTransforms[i].bmIndex), pageTransforms[i].leftRect, 1f, pageTransforms[i].leftRotate + rotateOffset, true);
        }
    }

    private void judgeState(int state) {
        switch (state) {
            case STATE_NORMAL:
                isNeedDrawMidCard = false;
                isNeedDrawUpCard = true;
                isNeedDrawDownCard = true;
                break;
            case STATE_LEFT_ING:
                isNeedDrawMidCard = true;
                if (curShowNum + 1 >= totalPage) {
                    isNeedDrawDownCard = false;
                }
                break;
            case STATE_RIGHT_ING:
                isNeedDrawMidCard = true;
                if (curShowNum - 1 < 0) {
                    isNeedDrawUpCard = false;
                }
                break;
            default:
                break;
        }
    }

    /**
     * @author savion
     * @date 2021/12/28
     * @desc 绘制剩余左边卡片
     **/
    private void drawUpRestCard(Canvas canvas) {
        if (horizontal) {
            //水平
            int end = currentRenderPageBms.length / 2;
            for (int i = 0; i < end; i++) {
                if (currentRenderPageBms[i] < curShowNum
                        && currentRenderPageBms[i] >= 0
                        && currentRenderPageBms[i] < numBms.size()) {
                    float leftFromRotate = 0;
                    float leftToRotate = minRotate;

                    float ratio = (i) * 1f / (end);

                    float rotate = ratio * leftToRotate;
                    float minScale = 0.7f;
                    float scale = ratio * (1f - minScale) + minScale;

                    float left = paddingSize * ratio;
                    Log.e("savion", String.format("画左边:%s__%s__%s__%s__%s", i, ratio, rotate, scale, left));
                    RectF rectF = new RectF(left, paddingSize, left + cardWidth, paddingSize + cardHeight);
                    cameraCard(canvas, numBms.get(currentRenderPageBms[i]), rectF, scale, rotate, true);
                }
            }
        }
    }

    private void drawDownRestCard(Canvas canvas) {
        if (horizontal) {
            //水平
            int start = currentRenderPageBms.length / 2;
            int end = currentRenderPageBms.length - 1;
            for (int i = end; i >= start; i--) {
                if (currentRenderPageBms[i] > curShowNum
                        && currentRenderPageBms[i] < numBms.size()) {
                    float leftFromRotate = 150;
                    float leftToRotate = 180;

                    float ratio = 1f - (i - start) * 1f / (end - start);//(1-0)

                    float rotate = ratio * (leftToRotate - leftFromRotate) * -1;

                    float minScale = 0.7f;
                    float scale = ratio * (1f - minScale) + minScale;

                    float right = paddingSize * ratio;
                    Log.e("savion", String.format("画右边:%s__%s__%s__%s__%s", i, ratio, rotate, scale, right));
                    RectF rectF = new RectF(getWidth() - cardWidth - right,
                            paddingSize,
                            getWidth() - right,
                            paddingSize + cardHeight);
                    cameraCard(canvas, numBms.get(currentRenderPageBms[i]), rectF, scale, rotate, false);
                }
            }
        }
    }

    private void cameraCard(Canvas canvas, Bitmap bitmap, RectF drawRect, float a_scale, float rotateY, boolean left) {
        if (bitmap != null && !bitmap.isRecycled()
                && drawRect != null && !drawRect.isEmpty()) {
//            mPaint.setShadowLayer(10f, 0f, 10f, Color.GRAY);
//            mPaint.setColor(Color.WHITE);

            canvas.save();
            mMatrix.reset();
            mCamera.save();
            mCamera.translate(0f, 0f, depthZ);
            mCamera.rotateX(0);
            mCamera.rotateY(rotateY);
            mCamera.getMatrix(mMatrix);
            mCamera.restore();
            Log.e("savion", String.format("绘制图形,%s_%s_%s_%s_%s_%s", left, rotateY, depthZ, mCamera.getLocationX(), mCamera.getLocationY(), mCamera.getLocationZ()));

            ///float scale = getResources().getDisplayMetrics().density * 3;

            float[] mValues = new float[9];
            mMatrix.getValues(mValues);
            mValues[6] = mValues[6] / mperspScale;
            mValues[7] = mValues[7] / mperspScale;
            mMatrix.setValues(mValues);

            mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
            mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
            canvas.concat(mMatrix);

//            canvas.drawRoundRect(drawRect,
//                    20f,
//                    20f,
//                    mPaint);

            //绘制数字
            mPaint.clearShadowLayer();
            if (left) {
                canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth() / 2, bitmap.getHeight()), drawRect, mPaint);
            } else {
                canvas.drawBitmap(bitmap, new Rect(bitmap.getWidth() / 2, 0, bitmap.getWidth(), bitmap.getHeight()), drawRect, mPaint);
            }

            canvas.restore();
        }
    }

    private void drawUpCard(Canvas canvas) {
        if (!isNeedDrawUpCard) {
            return;
        }
        mPaint.setShadowLayer(10f, 0f, 10f, Color.GRAY);
        mPaint.setColor(Color.WHITE);

        RectF rectF = new RectF(paddingSize, paddingSize, paddingSize + cardWidth, paddingSize + cardHeight);
        if (horizontal) {
            rectF.set(paddingSize, paddingSize, paddingSize + cardWidth, paddingSize + cardHeight);
        } else {
            rectF.set(paddingSize, paddingSize, paddingSize + cardWidth, paddingSize + cardHeight);
        }
        canvas.save();
        mMatrix.reset();
        mCamera.save();
        mCamera.translate(0f, 0f, depthZ);
        mCamera.rotateX(rotateX);
        if (horizontal) {
            mCamera.rotateY(minRotate);
        } else {
            mCamera.rotateY(minRotate);
        }
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        //float scale = getResources().getDisplayMetrics().density * 3;

        float[] mValues = new float[9];
        mMatrix.getValues(mValues);
        mValues[6] = mValues[6] / mperspScale;
        mValues[7] = mValues[7] / mperspScale;
        mMatrix.setValues(mValues);

        mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
        mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
        canvas.concat(mMatrix);

//        canvas.drawRoundRect(rectF,
//                20f,
//                20f,
//                mPaint);
        //绘制数字
        mPaint.clearShadowLayer();
        Bitmap curNumBm = numBms.get(curShowNum);
        if (curState == STATE_RIGHT_ING) {
            Bitmap tempBm = null;
            if (curShowNum - 1 >= 0) {
                tempBm = numBms.get(curShowNum - 1);
                if (horizontal) {
                    canvas.drawBitmap(tempBm, new Rect(0, 0, tempBm.getWidth() / 2, tempBm.getHeight()), rectF, mPaint);
                } else {
                    canvas.drawBitmap(tempBm, new Rect(0, 0, tempBm.getWidth(), tempBm.getHeight() / 2), rectF, mPaint);
                }
            }
        } else {
            if (horizontal) {
                canvas.drawBitmap(curNumBm, new Rect(0, 0, curNumBm.getWidth() / 2, curNumBm.getHeight()), rectF, mPaint);
            } else {
                canvas.drawBitmap(curNumBm, new Rect(0, 0, curNumBm.getWidth(), curNumBm.getHeight() / 2), rectF, mPaint);
            }
        }
        canvas.restore();
    }

    private void drawMidCard(Canvas canvas) {
        if (!isNeedDrawMidCard) {
            return;
        }
        canvas.save();
        mMatrix.reset();
        mCamera.save();
        mCamera.translate(0f, 0f, depthZ);
        mCamera.rotateX(rotateX);
        if (horizontal) {
            mCamera.rotateY(-rotateY);
        } else {
            mCamera.rotateY(rotateY);
        }
        mCamera.getMatrix(mMatrix);
        mCamera.restore();
        Log.e("savion", String.format("绘制中间:%s---%s---%s", rotateX, rotateY, depthZ));

        //float scale = getResources().getDisplayMetrics().density * 3;

        float[] mValues = new float[9];
        mMatrix.getValues(mValues);
        mValues[6] = mValues[6] / mperspScale;
        mValues[7] = mValues[7] / mperspScale;
        mMatrix.setValues(mValues);

        mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
        mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
        canvas.concat(mMatrix);
        mPaint.setColor(Color.WHITE);
        mPaint.setShadowLayer(cardShadowSize, 0f, cardShadowDistance, Color.GRAY);

        RectF rectF = new RectF(paddingSize,
                paddingSize + cardHeight,
                paddingSize + cardWidth,
                paddingSize + cardHeight * 2);
        if (horizontal) {
            rectF.set(paddingSize + cardWidth,
                    paddingSize,
                    paddingSize + cardWidth * 2,
                    paddingSize + cardHeight);
        } else {
            rectF.set(paddingSize,
                    paddingSize + cardHeight,
                    paddingSize + cardWidth,
                    paddingSize + cardHeight * 2);
        }
//        canvas.drawRoundRect(rectF, 20f, 20f, mPaint);
        mPaint.clearShadowLayer();
        Bitmap curNumBm = numBms.get(curShowNum);

        if (horizontal) {
            if (rotateY >= 90f) {
                Matrix matrix = new Matrix();
                //matrix.postRotate(180f);
                matrix.postScale(-1f, 1f);
                Bitmap tempBm = null;
                if (curState == STATE_LEFT_ING) {
                    if (curShowNum + 1 < totalPage) {
                        int width = curNumBm.getWidth();
                        int height = curNumBm.getHeight();
                        width = Math.min(numBms.get(curShowNum + 1).getWidth(), width);
                        height = Math.min(numBms.get(curShowNum + 1).getHeight(), height);
                        tempBm = Bitmap.createBitmap(numBms.get(curShowNum + 1),
                                0,
                                0,
                                width,
                                height,
                                matrix,
                                false);
                    }
                } else if (curState == STATE_RIGHT_ING) {
                    //往右翻
                    if (Math.abs(cardRotateFunc.getInitValue() - rotateY) >= 90) {
                        //绘制前一个数字
                        if (curShowNum - 1 >= 0) {
                            int width = curNumBm.getWidth();
                            int height = curNumBm.getHeight();
                            width = Math.min(numBms.get(curShowNum - 1).getWidth(), width);
                            height = Math.min(numBms.get(curShowNum - 1).getHeight(), height);
                            tempBm = Bitmap.createBitmap(numBms.get(curShowNum - 1),
                                    0,
                                    0,
                                    width,
                                    height,
                                    matrix,
                                    false);
                        } else {
                            int width = curNumBm.getWidth();
                            int height = curNumBm.getHeight();
                            width = Math.min(numBms.get(0).getWidth(), width);
                            height = Math.min(numBms.get(0).getHeight(), height);
                            tempBm = Bitmap.createBitmap(numBms.get(0),
                                    0,
                                    0,
                                    width,
                                    height,
                                    matrix,
                                    false);
                        }
                    } else {
                        int width = curNumBm.getWidth();
                        int height = curNumBm.getHeight();
                        width = Math.min(numBms.get(curShowNum).getWidth(), width);
                        height = Math.min(numBms.get(curShowNum).getHeight(), height);
                        tempBm = Bitmap.createBitmap(numBms.get(curShowNum),
                                0,
                                0,
                                width,
                                height,
                                matrix,
                                false);
                    }
                }
                if (tempBm != null
                        && !tempBm.isRecycled()) {
                    canvas.drawBitmap(tempBm,
                            new Rect(tempBm.getWidth() / 2,
                                    0, tempBm.getWidth(),
                                    tempBm.getHeight()),
                            rectF,
                            mPaint);
                }
            } else {
                if (Math.abs(cardRotateFunc.getInitValue() - rotateY) >= 90f) {
                    //绘制前一个数字
                    Bitmap tempBm = null;
                    if (curShowNum - 1 >= 0) {
                        tempBm = numBms.get(curShowNum - 1);
                    }
                    if (tempBm != null
                            && !tempBm.isRecycled()) {
                        canvas.drawBitmap(tempBm,
                                new Rect(tempBm.getWidth() / 2,
                                        0,
                                        tempBm.getWidth(),
                                        tempBm.getHeight()),
                                rectF,
                                mPaint);
                    }
                } else {
                    if (curNumBm != null
                            && !curNumBm.isRecycled()) {
                        canvas.drawBitmap(curNumBm,
                                new Rect(curNumBm.getWidth() / 2,
                                        0,
                                        curNumBm.getWidth(),
                                        curNumBm.getHeight()),
                                rectF,
                                mPaint);
                    }
                }
            }
        } else {
            if (rotateX >= 90f) {
                Matrix matrix = new Matrix();
                matrix.postRotate(180f);
                matrix.postScale(-1f, 1f);
                Bitmap tempBm = null;
                if (curState == STATE_LEFT_ING) {
                    //绘制下一个倒置翻转的数字图片
                    if (curShowNum + 1 < totalPage) {
                        int width = curNumBm.getWidth();
                        int height = curNumBm.getHeight();
                        width = Math.min(numBms.get(curShowNum + 1).getWidth(), width);
                        height = Math.min(numBms.get(curShowNum + 1).getHeight(), height);
                        tempBm = Bitmap.createBitmap(numBms.get(curShowNum + 1),
                                0,
                                0,
                                width,
                                height,
                                matrix,
                                false);
                    }
                } else if (curState == STATE_RIGHT_ING) {
                    //往下翻
                    if (Math.abs(cardRotateFunc.getInitValue() - rotateX) >= 90) {
                        //绘制前一个数字
                        if (curShowNum - 1 >= 0) {
                            int width = curNumBm.getWidth();
                            int height = curNumBm.getHeight();
                            width = Math.min(numBms.get(curShowNum - 1).getWidth(), width);
                            height = Math.min(numBms.get(curShowNum - 1).getHeight(), height);
                            tempBm = Bitmap.createBitmap(numBms.get(curShowNum - 1),
                                    0,
                                    0,
                                    width,
                                    height,
                                    matrix,
                                    false);
                        } else {
                            int width = curNumBm.getWidth();
                            int height = curNumBm.getHeight();
                            width = Math.min(numBms.get(0).getWidth(), width);
                            height = Math.min(numBms.get(0).getHeight(), height);
                            tempBm = Bitmap.createBitmap(numBms.get(0),
                                    0,
                                    0,
                                    width,
                                    height,
                                    matrix,
                                    false);
                        }
                    } else {
                        int width = curNumBm.getWidth();
                        int height = curNumBm.getHeight();
                        width = Math.min(numBms.get(curShowNum).getWidth(), width);
                        height = Math.min(numBms.get(curShowNum).getHeight(), height);
                        tempBm = Bitmap.createBitmap(numBms.get(curShowNum),
                                0,
                                0,
                                width,
                                height,
                                matrix,
                                false);
                    }
                }
                if (tempBm != null &&
                        !tempBm.isRecycled()) {
                    canvas.drawBitmap(tempBm,
                            new Rect(0, tempBm.getHeight() / 2,
                                    tempBm.getWidth(),
                                    tempBm.getHeight()),
                            rectF, mPaint);
                }
            } else {
                if (Math.abs(cardRotateFunc.getInitValue() - rotateX) >= 90f) {
                    //绘制前一个数字
                    Bitmap tempBm = null;
                    if (curShowNum - 1 >= 0) {
                        tempBm = numBms.get(curShowNum - 1);
                    }
                    if (tempBm != null
                            && !tempBm.isRecycled()) {
                        canvas.drawBitmap(tempBm,
                                new Rect(0, tempBm.getHeight() / 2,
                                        tempBm.getWidth(),
                                        tempBm.getHeight()),
                                rectF,
                                mPaint);
                    }
                } else {
                    if (curNumBm != null
                            && !curNumBm.isRecycled()) {
                        canvas.drawBitmap(curNumBm,
                                new Rect(0, curNumBm.getHeight() / 2,
                                        curNumBm.getWidth(),
                                        curNumBm.getHeight()),
                                rectF,
                                mPaint);
                    }
                }
            }
        }
        canvas.restore();
    }

    private void drawDownCard(Canvas canvas) {
        if (!isNeedDrawDownCard) {
            return;
        }
        mPaint.setShadowLayer(10f, 0f, 10f, Color.GRAY);
        mPaint.setColor(Color.WHITE);
        RectF rectF = new RectF(paddingSize, paddingSize + cardHeight, paddingSize + cardWidth, paddingSize + cardHeight * 2);
        if (horizontal) {
            rectF.set(paddingSize + cardWidth, paddingSize, paddingSize + cardWidth + cardWidth, paddingSize + cardHeight);
        } else {
            rectF.set(paddingSize, paddingSize + cardHeight, paddingSize + cardWidth, paddingSize + cardHeight * 2);
        }

        canvas.save();
        mMatrix.reset();
        mCamera.save();
        mCamera.translate(0f, 0f, depthZ);
        mCamera.rotateX(rotateX);
        if (horizontal) {
            mCamera.rotateY(-minRotate);
        } else {
            mCamera.rotateY(minRotate);
        }
        mCamera.getMatrix(mMatrix);
        mCamera.restore();

        //float scale = getResources().getDisplayMetrics().density * 3;

        float[] mValues = new float[9];
        mMatrix.getValues(mValues);
        mValues[6] = mValues[6] / mperspScale;
        mValues[7] = mValues[7] / mperspScale;
        mMatrix.setValues(mValues);

        mMatrix.preTranslate(-getWidth() / 2f, -getHeight() / 2f);
        mMatrix.postTranslate(getWidth() / 2f, getHeight() / 2f);
        canvas.concat(mMatrix);

//        canvas.drawRoundRect(rectF,
//                20f,
//                20f,
//                mPaint);
        //绘制数字
        mPaint.clearShadowLayer();
        Bitmap curNumBm = numBms.get(curShowNum);
        //往上翻，显示下一个数字
        if (curState == STATE_LEFT_ING) {
            Bitmap tempBm = numBms.get(curShowNum + 1);
            if (curShowNum + 1 < totalPage) {
                if (horizontal) {
                    canvas.drawBitmap(tempBm, new Rect(tempBm.getWidth() / 2, 0, tempBm.getWidth(), tempBm.getHeight()), rectF, mPaint);
                } else {
                    canvas.drawBitmap(tempBm, new Rect(0, tempBm.getHeight() / 2, tempBm.getWidth(), tempBm.getHeight()), rectF, mPaint);
                }
            }
        } else {
            if (horizontal) {
                canvas.drawBitmap(curNumBm, new Rect(curNumBm.getWidth() / 2, 0, curNumBm.getWidth(), curNumBm.getHeight()), rectF, mPaint);
            } else {
                canvas.drawBitmap(curNumBm, new Rect(0, curNumBm.getHeight() / 2, curNumBm.getWidth(), curNumBm.getHeight()), rectF, mPaint);
            }
        }
        canvas.restore();

    }

    private ValueAnimator cardRotateAnim = null;

    private void startCardUpAnim(final int curNum) {
        if (cardRotateAnim != null) {
            cardRotateAnim.cancel();
        }
        if (horizontal) {
            cardRotateAnim = ValueAnimator.ofFloat(rotateY, maxRotate);
        } else {
            cardRotateAnim = ValueAnimator.ofFloat(rotateX, maxRotate);
        }
        cardRotateAnim.setDuration(200);
        cardRotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (horizontal) {
                    rotateY = (float) animation.getAnimatedValue();
                    executeShadowFunc(rotateY);
                } else {
                    rotateX = (float) animation.getAnimatedValue();
                    executeShadowFunc(rotateX);
                }
                //postInvalidate();
                hookInvalidate();
            }
        });
        cardRotateAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                resetInitValue();
                curState = STATE_NORMAL;
                curShowNum = curNum;
                calShownCards(curShowNum);
            }
        });
        cardRotateAnim.start();
    }

    private void startCardDownAnim(final int curNum) {
        if (cardRotateAnim != null) {
            cardRotateAnim.cancel();
            if (horizontal) {
                cardRotateAnim = ValueAnimator.ofFloat(rotateY, minRotate);
            } else {
                cardRotateAnim = ValueAnimator.ofFloat(rotateX, minRotate);
            }
            cardRotateAnim.setDuration(200);
            cardRotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (horizontal) {
                        rotateY = (float) animation.getAnimatedValue();
                        executeShadowFunc(rotateY);
                    } else {
                        rotateX = (float) animation.getAnimatedValue();
                        executeShadowFunc(rotateX);
                    }
                    //postInvalidate();
                    hookInvalidate();
                }
            });
            cardRotateAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    resetInitValue();
                    curState = STATE_NORMAL;
                    curShowNum = curNum;
                    calShownCards(curShowNum);
                }
            });
            cardRotateAnim.start();
        }
    }

    private void calShownCards(int currentCardIndex) {
        currentRenderPageBms = new int[currentRenderPageSize];
        int index = 0;
        for (int i = (currentCardIndex - currentRenderPageSize / 2); i <= (currentCardIndex + currentRenderPageSize / 2); i++) {
            //0 1 2 3 4 5 6 7 8
            if (i >= 0 && i < numBms.size()) {
                currentRenderPageBms[index] = i;
            } else {
                currentRenderPageBms[index] = -1;
            }
            index++;
        }


        //水平延申距离
        int measureHoriPaddinghalfSize = currentRenderPageSize / 2;
        if (measureHoriPaddinghalfSize >= 2) {
            measureHoriPaddinghalfSize--;
        }
        float horiPaddingOff = paddingSize / measureHoriPaddinghalfSize;
        //实际左右padding宽度
        float horiPaddingTotal = paddingSize + horiPaddingOff;
        //左边起点，从屏幕外开始
        float horiLeftStart = -horiPaddingOff;
        //右边起点，从屏幕外开始
        float horiRightStart = getWidth() + horiPaddingOff;
        int renderPageStart = currentCardIndex - currentRenderPageSize / 2;
        int renderPageEnd = currentCardIndex + currentCardIndex / 2;
        //if (pageTransforms == null || pageTransforms.length == 0) {
        //pageTransforms = new PageTransform[numBms.size()];
        if (pageTransforms == null) {
            pageTransforms = new PageTransform[numBms.size()];
        }
        if (renderPageTrangeforms == null) {
            renderPageTrangeforms = new PageTransform[currentRenderPageSize];
            int renderCardIndex = renderPageTrangeforms.length / 2;
            for (int i = 0; i < renderPageTrangeforms.length; i++) {
                if (i < renderCardIndex) {
                    //左边
                    PageTransform leftPage = new PageTransform();

                    int start = 0;
                    int end = renderCardIndex;
                    float leftFromRotate = 0;
                    float leftToRotate = minRotate;

                    float ratio = (i - start) * 1f / (end - start);
                    float rotate = ratio * (leftToRotate - leftFromRotate);
                    float minScale = 0.7f;
                    float scale = ratio * (1f - minScale) + minScale;

                    //水平间隔延申到屏幕外，这样边界绘制能更自然
                    float left = horiPaddingTotal * ratio;
                    RectF rectF = new RectF(horiLeftStart + left, paddingSize, horiLeftStart + left + cardWidth, paddingSize + cardHeight);

                    leftPage.leftRect = rectF;
                    leftPage.leftRotate = rotate;

                    leftPage.rightRect = rectF;
                    leftPage.rightRotate = rotate + 15;
                    leftPage.bmIndex = i;
                    leftPage.direct = PageTransform.LEFT;
                    renderPageTrangeforms[i] = leftPage;
                } else if (i == renderPageTrangeforms.length / 2) {
                    //中间
                    PageTransform curPage = new PageTransform();
                    curPage.leftRotate = minRotate;
                    curPage.rightRotate = -minRotate;
                    curPage.leftRect = new RectF(paddingSize,
                            paddingSize,
                            paddingSize + cardWidth,
                            paddingSize + cardHeight);
                    curPage.rightRect = new RectF(paddingSize + cardWidth,
                            paddingSize,
                            paddingSize + cardWidth * 2,
                            paddingSize + cardHeight);
                    curPage.bmIndex = i;
                    curPage.direct = PageTransform.CENTER;
                    renderPageTrangeforms[i] = curPage;
                } else {
                    //右边
                    PageTransform rightPage = new PageTransform();

                    int start = renderCardIndex;
                    int end = renderPageTrangeforms.length - 1;

                    float leftFromRotate = maxRotate;
                    float leftToRotate = 180;

                    float ratio = 1f - (i - start) * 1f / (end - start);//(1-0)

                    float rotate = ratio * (leftToRotate - leftFromRotate) * -1;

                    float minScale = 0.7f;
                    float scale = ratio * (1f - minScale) + minScale;

                    float right = horiPaddingTotal * ratio;
                    RectF rectF = new RectF(horiRightStart - cardWidth - right, paddingSize, horiRightStart - right, paddingSize + cardHeight);

                    rightPage.rightRect = rectF;
                    rightPage.rightRotate = rotate;

                    rightPage.leftRect = rectF;
                    rightPage.leftRotate = rotate - 15;

                    rightPage.bmIndex = i;
                    rightPage.direct = PageTransform.RIGHT;

                    renderPageTrangeforms[i] = rightPage;
                }
            }
        }

        for (int i = 0; i < numBms.size(); i++) {
            if (i == currentCardIndex) {
                //当前页
//                PageTransform curPage = new PageTransform();
//                curPage.leftRotate = minRotate;
//                curPage.rightRotate = -minRotate;
//                curPage.leftRect = new RectF(paddingSize,
//                        paddingSize,
//                        paddingSize + cardWidth,
//                        paddingSize + cardHeight);
//                curPage.rightRect = new RectF(paddingSize + cardWidth,
//                        paddingSize,
//                        paddingSize + cardWidth * 2,
//                        paddingSize + cardHeight);
//                curPage.bmIndex = i;
//                curPage.direct = PageTransform.CENTER;
//                pageTransforms[i] = curPage;

                int renderIndex = (i - renderPageStart) % renderPageTrangeforms.length;
                pageTransforms[i] = new PageTransform();
                pageTransforms[i].copyOf(renderPageTrangeforms[renderIndex]);
                pageTransforms[i].renderIndex = renderIndex;
                pageTransforms[i].bmIndex = i;

            } else if (i >= currentCardIndex - currentRenderPageSize / 2 && i < currentCardIndex) {
                //当前页左边可见页
//                PageTransform leftPage = new PageTransform();
//
//                int start = currentCardIndex - currentRenderPageSize / 2;
//                int end = currentCardIndex;
//                float leftToRotate = minRotate;
//
//                float ratio = (i - start) * 1f / (end - start);
//                float rotate = ratio * leftToRotate;
//                float minScale = 0.7f;
//                float scale = ratio * (1f - minScale) + minScale;
//
//                //水平间隔延申到屏幕外，这样边界绘制能更自然
//                float left = (paddingSize) * ratio;
//                RectF rectF = new RectF(left, paddingSize, left + cardWidth, paddingSize + cardHeight);
//
//                leftPage.leftRect = rectF;
//                leftPage.leftRotate = rotate;
//
//                leftPage.rightRect = rectF;
//                leftPage.rightRotate = rotate + 15;
//                leftPage.bmIndex = i;
//                leftPage.direct = PageTransform.LEFT;
//                pageTransforms[i] = leftPage;
//                pageTransforms[i].renderIndex = (i - renderPageStart) % renderPageTrangeforms.length;

                //绑定渲染序号
                int renderIndex = (i - renderPageStart) % renderPageTrangeforms.length;
                pageTransforms[i] = new PageTransform();
                pageTransforms[i].copyOf(renderPageTrangeforms[renderIndex]);
                pageTransforms[i].renderIndex = renderIndex;
                pageTransforms[i].bmIndex = i;
            } else if (i <= currentCardIndex + currentRenderPageSize / 2 && i > currentCardIndex) {
                //当前页右边可见页
//                PageTransform rightPage = new PageTransform();
//
//                int start = currentCardIndex;
//                int end = currentCardIndex + currentRenderPageSize / 2;
//
//                float leftFromRotate = 150;
//                float leftToRotate = 180;
//
//                float ratio = 1f - (i - start) * 1f / (end - start);//(1-0)
//
//                float rotate = ratio * (leftToRotate - leftFromRotate) * -1;
//
//                float minScale = 0.7f;
//                float scale = ratio * (1f - minScale) + minScale;
//
//                float right = paddingSize * ratio;
//                RectF rectF = new RectF(getWidth() - cardWidth - right, paddingSize, getWidth() - right, paddingSize + cardHeight);
//
//                rightPage.rightRect = rectF;
//                rightPage.rightRotate = rotate;
//
//                rightPage.leftRect = rectF;
//                rightPage.leftRotate = rotate - 15;
//
//                rightPage.bmIndex = i;
//                rightPage.direct = PageTransform.RIGHT;
//
//                pageTransforms[i] = rightPage;
//
//                pageTransforms[i].renderIndex = (i - renderPageStart) % renderPageTrangeforms.length;

                int renderIndex = (i - renderPageStart) % renderPageTrangeforms.length;
                pageTransforms[i] = new PageTransform();
                pageTransforms[i].copyOf(renderPageTrangeforms[renderIndex]);
                pageTransforms[i].renderIndex = renderIndex;
                pageTransforms[i].bmIndex = i;

            } else if (i < currentCardIndex) {
                //当前页左边不可见页
                PageTransform leftRestPage = new PageTransform();
                leftRestPage.bmIndex = i;
                leftRestPage.direct = PageTransform.LEFT;

                pageTransforms[i] = leftRestPage;
            } else {
                //其余都是当前页右边不可见页
                PageTransform rightRestPage = new PageTransform();
                rightRestPage.bmIndex = i;
                rightRestPage.direct = PageTransform.RIGHT;

                pageTransforms[i] = rightRestPage;
            }
        }
//        }
    }

    private float downX = 0f;
    private float downY = 0f;
    private float offsetY = 0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rendering = true;
                downX = event.getX();
                downY = event.getY();
                if (horizontal) {
                    if (downX >= getWidth() / 2f) {
                        //绘制下方的mid card
                        //rotateX = 0f;
                        rotateY = minRotate;
                        curState = STATE_LEFT_ING;
                    } else {
                        //rotateX = 180f;
                        rotateY = maxRotate;
                        curState = STATE_RIGHT_ING;
                    }
                } else {
                    if (downY >= getHeight() / 2f) {
                        //绘制下方的mid card
                        rotateX = minRotate;
                        curState = STATE_LEFT_ING;
                    } else {
                        rotateX = maxRotate;
                        curState = STATE_RIGHT_ING;
                    }
                }
                resetInitValue();
//                postInvalidate();
                hookInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (horizontal) {
                    offsetY = event.getX() - downX;
                } else {
                    offsetY = event.getY() - downY;
                }
                executeFunc(offsetY);
                //postInvalidate();
                hookInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                //判断是上翻还是下翻
                if (horizontal) {
                    //水平
                    if (rotateY >= midRotate) {
                        if (Math.abs(cardRotateFunc.getInitValue() - rotateY) >= (maxRotate - minRotate) / 2) {
                            if (curShowNum + 1 < totalPage) {
                                Log.e("savion", String.format("惯性回弹2:%s___%s___%s",
                                        "1",
                                        cardRotateFunc.getInitValue(),
                                        rotateY));
                                startCardUpAnim(curShowNum + 1);
                            } else {
                                Log.e("savion", String.format("惯性回弹2:%s___%s___%s",
                                        "2",
                                        cardRotateFunc.getInitValue(),
                                        rotateY));
                                curShowNum = totalPage - 1;
                                calShownCards(curShowNum);
                                startCardDownAnim(totalPage - 1);
                            }
                        } else {
                            Log.e("savion", String.format("惯性回弹2:%s___%s___%s",
                                    "3",
                                    cardRotateFunc.getInitValue(),
                                    rotateY));
                            startCardUpAnim(curShowNum);
                        }
                    } else {
                        if (Math.abs(cardRotateFunc.getInitValue() - rotateY) >= (maxRotate - minRotate) / 2) {
                            if (curShowNum - 1 >= 0) {
                                Log.e("savion", String.format("惯性回弹:%s___%s___%s",
                                        "1",
                                        cardRotateFunc.getInitValue(),
                                        rotateY));
                                startCardDownAnim(curShowNum - 1);
                            } else {
                                Log.e("savion", String.format("惯性回弹:%s___%s___%s",
                                        "2",
                                        cardRotateFunc.getInitValue(),
                                        rotateY));
                                curShowNum = 0;
                                calShownCards(curShowNum);
                                startCardUpAnim(0);
                            }
                        } else {
                            Log.e("savion", String.format("惯性回弹:%s___%s___%s",
                                    "3",
                                    cardRotateFunc.getInitValue(),
                                    rotateY));
                            startCardDownAnim(curShowNum);
                        }
                    }
                } else {
                    //垂直
                    if (rotateX >= midRotate) {
                        if (Math.abs(cardRotateFunc.getInitValue() - rotateX) >= (maxRotate - minRotate) / 2) {
                            if (curShowNum + 1 < totalPage) {
                                startCardUpAnim(curShowNum + 1);
                            } else {
                                curShowNum = totalPage - 1;
                                calShownCards(curShowNum);
                                startCardDownAnim(totalPage - 1);
                            }
                        } else {
                            startCardUpAnim(curShowNum);
                        }
                    } else {
                        if (Math.abs(cardRotateFunc.getInitValue() - rotateX) >= (maxRotate - minRotate) / 2) {
                            if (curShowNum - 1 >= 0) {
                                startCardDownAnim(curShowNum - 1);
                            } else {
                                curShowNum = 0;
                                calShownCards(curShowNum);
                                startCardUpAnim(0);
                            }
                        } else {
                            startCardDownAnim(curShowNum);
                        }
                    }
                }
                downX = 0f;
                downY = 0f;
                rendering = false;
                break;
            default:
                break;
        }
        return true;
    }

    private Disposable disposable;
    private Interpolator interpolator = new DecelerateInterpolator();

    /**
     * @param pageAnimMill  单页动画时长
     * @param pagePauseMill 单页停顿时长
     * @author savion
     * @date 2021/12/31
     * @desc 自动播放
     **/
    public void autoPlay(long pageAnimMill, long pagePauseMill) {
        //一秒25帧
        long fps = 25;
        //总时长
        long totalMill = pageAnimMill * (numBms.size() - 1) + pagePauseMill * numBms.size();
        long singlePageMill = pageAnimMill + pagePauseMill;
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        long fpsMills = 1000 / fps;
        int frames = (int) (totalMill / fpsMills);
        disposable = Observable.intervalRange(0, frames, 0, fpsMills, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    aLong *= fpsMills;
                    rendering = true;
                    //当前页码
                    curShowNum = (int) (aLong / singlePageMill);
                    calShownCards(curShowNum);
                    long a = aLong % singlePageMill;
                    float percent = 0f;
                    if (a < pagePauseMill) {
                        //停顿
                        curState = STATE_NORMAL;
                        rotateY = minRotate;
                        percent = 0f;
                    } else {
                        //翻页
                        curState = STATE_LEFT_ING;
                        percent = (a - pagePauseMill) * 1f / pageAnimMill;
                        percent = interpolator.getInterpolation(percent);
                        rotateY = (maxRotate - minRotate) * percent + minRotate;
                    }
                    //invalidate();
                    hookInvalidate();
                    Log.e("savion", String.format("自动播放进行:%s__%s__%s__%s__%s__%s__%s__%s",
                            aLong,
                            totalMill,
                            singlePageMill,
                            a,
                            rotateY,
                            curState,
                            curShowNum,
                            percent));
                }, throwable -> {
                    Log.e("savion", "自动播放失败:" + throwable.getMessage());
                    rendering = false;
                }, () -> {
                    rendering = false;
                }, disposable1 -> {
                    curShowNum = 0;
                    rendering = true;
                    curState = STATE_NORMAL;
                });
    }
}
