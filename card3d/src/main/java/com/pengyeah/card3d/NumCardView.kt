package com.pengyeah.card3d

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntRange

/**
 *  Created by pengyeah on 2020/10/9
 *  佛祖开光，永无bug
 *  God bless U
 */
class NumCardView : View {

    /**
     * 3D摄像头
     */
    var mCamera: Camera = Camera()

    var mPaint: Paint = Paint()
    var mMatrix: Matrix = Matrix()

    /**
     * 3D位置参数
     */
    private var depthZ: Float = 0F
    private var rotateX: Float = 100F
    private var rotateY: Float = 0F

    /**
     * 数字图片列表，0～9
     */
    private var numBms = ArrayList<Bitmap>()

    /*
    * 当前显示数字
     */
    @IntRange(from = 0, to = 9)
    var curShowNum: Int = 0

    /**
     * padding大小，为阴影绘制留出空间
     */
    var paddingSize: Float = 250F

    /**
     * 每片card的宽高
     */
    private var cardWidth: Float = 0F
    private var cardHeight: Float = 0F

    /**
     * 是否需要绘制上中下卡片
     */
    private var isNeedDrawUpCard = true
    private var isNeedDrawMidCard = true
    private var isNeedDrawDownCard = true

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        initNumBms()
    }

    private fun initNumBms() {

        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.RED


    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //设置card 大小
        cardWidth = width - paddingSize * 2
        cardHeight = height / 2 - paddingSize
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        setLayerType(LAYER_TYPE_SOFTWARE, null)
        canvas?.let {
            drawUpCard(it)
            drawDownCard(it)

            // 中间活动card最后绘制
            drawMidCard(it)
        }
    }

    /**
     * 上页片
     */
    fun drawUpCard(canvas: Canvas) {
        if (!isNeedDrawUpCard) return

        with(canvas) {
            mPaint.setShadowLayer(10F, 0F, 10F, Color.GRAY)
            mPaint.color = Color.WHITE
            drawRoundRect(
                    RectF(paddingSize, paddingSize, paddingSize + cardWidth, paddingSize + cardHeight),
                    20F,
                    20F,
                    mPaint
            )
        }
    }

    /**
     * 中页片（活动页片）
     */
    fun drawMidCard(canvas: Canvas) {
        if (!isNeedDrawMidCard) return

        with(canvas) {
            save()
            mMatrix.reset()
            mCamera.save()
            mCamera.translate(0F, 0F, depthZ)
            mCamera.rotateX(rotateX)
            mCamera.rotateY(rotateY)
            mCamera.getMatrix(mMatrix)
            mCamera.restore()

            val scale = resources.displayMetrics.density
            val mValues = FloatArray(9)
            mMatrix.getValues(mValues)
            mValues[6] = mValues[6] / scale
            mValues[7] = mValues[7] / scale
            mMatrix.setValues(mValues)

            mMatrix.preTranslate(-width / 2F, -height / 2F)
            mMatrix.postTranslate(width / 2F, height / 2F)
            concat(mMatrix)
            mPaint.color = Color.WHITE
            mPaint.setShadowLayer(15F, 0F, 10F, Color.GRAY)

            drawRoundRect(
                    RectF(paddingSize, paddingSize + cardHeight, paddingSize + cardWidth, paddingSize + cardHeight * 2),
                    20F,
                    20F,
                    mPaint
            )
            restore()
        }
    }

    /**
     * 下页片
     */
    fun drawDownCard(canvas: Canvas) {
        if (!isNeedDrawDownCard) return

        with(canvas) {
            mPaint.setShadowLayer(10F, 0F, 10F, Color.GRAY)
            mPaint.color = Color.WHITE
            drawRoundRect(
                    RectF(paddingSize, paddingSize + cardHeight, paddingSize + cardWidth, paddingSize + cardHeight * 2),
                    20F,
                    20F,
                    mPaint
            )
        }
    }

    /**
     * 手指按下的初始坐标
     */
    private var downX: Float = 0F
    private var downY: Float = 0F

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_MOVE -> {


            }
            MotionEvent.ACTION_UP -> {
                downX = 0F
                downY = 0F
            }
            else -> {

            }
        }
        return true
    }
}