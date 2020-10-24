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
import com.pengyeah.tear.utils.BazierUtils
import java.lang.Math.cos
import kotlin.math.cos

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
    }

    /**
     * 根据D点配置各个关键点坐标
     */
    private fun configPoint(dx: Float, dy: Float) {
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
    }

    private fun combinePath() {
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
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        canvas?.save()
        canvas?.translate((width - paperWidth) / 2F, (height - paperHeight) / 2F)
        canvas?.clipPath(contentPath)

        val flag = super.drawChild(canvas, child, drawingTime)
        canvas?.restore()
        return flag
    }

    var downX: Float = 0F
    var downY: Float = 0F
    var offset: Float = 0F

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = 0F
                downY = 0F
            }
            MotionEvent.ACTION_MOVE -> {
                offset = event.x - downX
                Log.i(TAG,"offset==>$offset")
                configPoint(offset, paperHeight - offset)
                combinePath()
                postInvalidate()
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