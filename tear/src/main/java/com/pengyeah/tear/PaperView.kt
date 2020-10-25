package com.pengyeah.tear

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import com.pengyeah.tear.coordinate.Coordinate
import com.pengyeah.tear.func.CrimpSizeFunc
import com.pengyeah.tear.utils.BazierUtils
import kotlin.math.abs

/**
 *  Created by pengyeah on 2020/10/16
 *  佛祖开光，永无bug
 *  God bless U
 */
class PaperView : RelativeLayout {

    val TAG: String = javaClass.simpleName

    /**
     * 纸张宽高
     */
    var paperWidth: Float = 0F
    var paperHeight: Float = 0F

    var mPaint: Paint = Paint()

    @ColorInt
    var paperColor: Int = Color.WHITE

    /**
     * 坐标点
     */
    var pointA: Coordinate = Coordinate()
    var pointB: Coordinate = Coordinate()
    var pointC: Coordinate = Coordinate()
    var pointD: Coordinate = Coordinate()
    var pointE: Coordinate = Coordinate()
    var pointF: Coordinate = Coordinate()
    var pointG: Coordinate = Coordinate()
    //outer状态下的顶点坐标
    var pointH: Coordinate = Coordinate()
    var pointI: Coordinate = Coordinate()

    /**
     * 内容路径
     */
    var contentPath: Path = Path()

    /**
     * 纸张卷角
     */
    var dogEaredPath: Path = Path()

    /**
     * 组合路径 = 内容路径+纸张卷角
     */
    var unionPath: Path = Path()

    var crimpSize: Float = 0F

    /**
     * 阴影颜色
     */
    @ColorInt
    var shadowColor: Int = 0x44888888

    /**
     * 卷角在内状态
     */
    val STATE_INNER = 0x01

    /**
     * 卷角在外状态
     */
    val STATE_OUTER = 0x02

    var state = STATE_INNER

    /**
     * 各个变化函数
     */
    var crimpSizeFunc: CrimpSizeFunc? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        mPaint.color = paperColor
        mPaint.isAntiAlias = true

        setWillNotDraw(false)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        paperWidth = w * 3 / 4F
        paperHeight = paperWidth

        configPoint(0F, paperHeight)
        combinePath()
        initFunc()
    }

    /**
     * 根据D点配置各个关键点坐标
     * @param dx D点x坐标
     * @param dy D点y坐标
     */
    private fun configPoint(dx: Float, dy: Float) {
        if (dx >= paperWidth) {
            state = STATE_OUTER
        } else {
            state = STATE_INNER
        }
        if (state == STATE_INNER) {
            pointD.x = dx
            pointD.y = dy

            pointA.x = 0F
            pointA.y = pointD.y - crimpSize

            pointB.x = 0F
            pointB.y = pointD.y

            pointC.x = pointB.x + crimpSize
            pointC.y = pointD.y

            pointE.x = pointD.x
            pointE.y = paperHeight - crimpSize

            pointF.x = pointD.x
            pointF.y = paperHeight

            pointG.x = pointD.x + crimpSize
            pointG.y = paperHeight
        } else if (state == STATE_OUTER) {
            pointD.x = dx
            pointD.y = dy

            pointH.x = pointD.x - paperWidth
            pointH.y = pointD.y

            pointI.x = pointD.x
            pointI.y = pointD.y + paperHeight

            pointA.x = pointH.x
            pointA.y = -crimpSize

            pointB.x = pointA.x
            pointB.y = 0F

            pointC.x = pointB.x + crimpSize
            pointC.y = 0F

            pointE.x = paperWidth
            pointE.y = paperHeight + pointD.y - crimpSize

            pointF.x = pointE.x
            pointF.y = paperHeight + pointD.y

            pointG.x = pointF.x + crimpSize
            pointG.y = pointF.y
        }
    }

    private fun combinePath() {
        if (state == STATE_INNER) {
            contentPath.reset()
            contentPath.moveTo(0F, 0F)
            contentPath.lineTo(pointA.x, pointA.y)
            contentPath.quadTo(pointB.x, pointB.y, pointC.x, pointC.y)
            contentPath.lineTo(pointD.x, pointD.y)
            contentPath.lineTo(pointE.x, pointE.y)
            contentPath.quadTo(pointF.x, pointF.y, pointG.x, pointG.y)
            contentPath.lineTo(paperWidth, paperHeight)
            contentPath.lineTo(paperWidth, 0F)
            contentPath.close()

            val pointb = Coordinate()
            val pointbF = BazierUtils.getBezierPoint(PointF(pointA.x, pointA.y), PointF(pointB.x, pointB.y), PointF(pointC.x, pointC.y), 0.5F)
            pointb.x = pointbF.x
            pointb.y = pointbF.y

            val pointf = Coordinate()
            val pointfF = BazierUtils.getBezierPoint(PointF(pointE.x, pointE.y), PointF(pointF.x, pointF.y), PointF(pointG.x, pointG.y), 0.5F)
            pointf.x = pointfF.x
            pointf.y = pointfF.y

            dogEaredPath.reset()
            dogEaredPath.moveTo(pointb.x, pointb.y)
            dogEaredPath.lineTo(pointD.x, pointD.y)
            dogEaredPath.lineTo(pointf.x, pointf.y)
            dogEaredPath.close()

            dogEaredPath.op(contentPath, Path.Op.DIFFERENCE)
        } else if (state == STATE_OUTER) {
            contentPath.reset()
            contentPath.moveTo(pointD.x, pointD.y)
            contentPath.lineTo(pointH.x, pointH.y)
            contentPath.lineTo(pointA.x, pointA.y)
            contentPath.quadTo(pointB.x, pointB.y, pointC.x, pointC.y)
            contentPath.lineTo(paperWidth, 0F)
            contentPath.lineTo(paperWidth, pointE.y)
            contentPath.quadTo(pointF.x, pointF.y, pointG.x, pointG.y)
            contentPath.lineTo(pointI.x, pointI.y)
            contentPath.close()

            val pointb = Coordinate()
            val pointbF = BazierUtils.getBezierPoint(PointF(pointA.x, pointA.y), PointF(pointB.x, pointB.y), PointF(pointC.x, pointC.y), 0.5F)
            pointb.x = pointbF.x
            pointb.y = pointbF.y

            val pointf = Coordinate()
            val pointfF = BazierUtils.getBezierPoint(PointF(pointE.x, pointE.y), PointF(pointF.x, pointF.y), PointF(pointG.x, pointG.y), 0.5F)
            pointf.x = pointfF.x
            pointf.y = pointfF.y

            dogEaredPath.reset()
            dogEaredPath.moveTo(pointb.x, pointb.y)
            dogEaredPath.lineTo(pointD.x, pointD.y)
            dogEaredPath.lineTo(pointf.x, pointf.y)
            dogEaredPath.close()

            dogEaredPath.op(contentPath, Path.Op.DIFFERENCE)

            unionPath.reset()
            unionPath.op(dogEaredPath, Path.Op.UNION)
            unionPath.op(contentPath, Path.Op.UNION)
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        canvas?.let {
            drawPaper(it)
        }
    }

    private fun drawPaper(canvas: Canvas) {
        with(canvas) {
            when (state) {
                STATE_INNER -> {
                    save()
                    translate((width - paperWidth) / 2F, (height - paperHeight) / 2F)

                    mPaint.shader = null
                    mPaint.setShadowLayer(50F, -10F, 10F, shadowColor)
                    unionPath.reset()
                    unionPath.op(contentPath, Path.Op.UNION)
                    unionPath.op(dogEaredPath, Path.Op.UNION)
                    drawPath(unionPath, mPaint)

                    mPaint.style = Paint.Style.FILL
                    mPaint.color = Color.WHITE
                    mPaint.clearShadowLayer()
                    drawPath(contentPath, mPaint)
                    mPaint.style = Paint.Style.FILL
                    mPaint.color = Color.WHITE
                    mPaint.setShadowLayer(20F, 10F, -10F, shadowColor)
                    drawPath(dogEaredPath, mPaint)

                    mPaint.shader = LinearGradient(pointD.x / 2F, pointD.y + pointD.x / 2F, pointD.x * 3 / 4F, pointD.y + pointD.x / 4F, shadowColor, Color.WHITE, Shader.TileMode.CLAMP)
                    drawPath(dogEaredPath, mPaint)
                    restore()
                }
                STATE_OUTER -> {
                    save()
                    translate((width - paperWidth) / 2F, (height - paperHeight) / 2F)
                    mPaint.style = Paint.Style.FILL
                    mPaint.color = Color.WHITE
                    mPaint.shader = LinearGradient(pointD.x / 2F, pointD.y + pointD.x / 2F, pointD.x * 3 / 4F, pointD.y + pointD.x / 4F, shadowColor, Color.WHITE, Shader.TileMode.CLAMP)
                    drawPath(unionPath, mPaint)
                    mPaint.setShadowLayer(50F, -10F, 10F, shadowColor)
                    drawPath(unionPath, mPaint)
                    restore()
                }
                else -> {

                }
            }

        }
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        canvas?.save()
        canvas?.translate((width - paperWidth) / 2F, (height - paperHeight) / 2F)
        canvas?.clipPath(contentPath)

        val flag = super.drawChild(canvas, child, drawingTime)
        canvas?.restore()
        return flag
    }

    private fun initFunc() {
        crimpSizeFunc = CrimpSizeFunc()
        with(crimpSizeFunc!!) {
            outParamMax = 80F
            outParamMin = 0F

            inParamMax = paperWidth * 2
            inParamMin = 0F

            initValue = 0F
        }
    }

    private fun executeCrimpSizeFunc(offset: Float) {
        crimpSizeFunc?.let {
            crimpSize = it.execute(offset)
        }
    }

    var downX: Float = 0F
    var downY: Float = 0F
    var offset: Float = 0F
    var dStartX: Float = 0F
    var dStartY: Float = 0F

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                dStartX = pointD.x
                dStartY = pointD.y
            }
            MotionEvent.ACTION_MOVE -> {
                offset = event.x - downX
                pointD.x = dStartX + offset
                pointD.y = dStartY - offset
                executeCrimpSizeFunc(dStartX + offset)
                configPoint(pointD.x, pointD.y)
                combinePath()
                postInvalidate()
            }
            MotionEvent.ACTION_UP -> {
                downX = 0F
                downY = 0F
                offset = 0F
            }
            else -> {

            }
        }
        return true
    }
}