package com.pengyeah.tear

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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
class PaperView : View {

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

    var crimpSize: Float = 80F

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        mPaint.color = paperColor
        mPaint.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        paperWidth = w * 3 / 4F
        paperHeight = paperWidth

        configPath()
    }

    private fun configPath() {
        pointA.x = 0F
        pointA.y = paperHeight / 2F

        pointB.x = 0F
        pointB.y = paperHeight / 2F + crimpSize

        pointC.x = 40F
        pointC.y = paperHeight / 2F + crimpSize


        pointE.x = paperWidth / 2F - crimpSize
        pointE.y = paperHeight - crimpSize

        pointF.x = paperWidth / 2F - crimpSize
        pointF.y = paperHeight

        pointG.x = paperWidth / 2F
        pointG.y = paperHeight

        pointD.x = pointE.x
        pointD.y = pointC.y

        contentPath.moveTo(0F, 0F)
        contentPath.lineTo(pointA.x, pointA.y)
        contentPath.quadTo(pointB.x, pointB.y, pointC.x, pointC.y)
        contentPath.lineTo(pointD.x, pointD.y)
        contentPath.lineTo(pointE.x, pointE.y)
        contentPath.quadTo(pointF.x, pointF.y, pointG.x, pointG.y)
        contentPath.lineTo(paperWidth, paperHeight)
        contentPath.lineTo(paperWidth, 0F)
        contentPath.close()

        var pointb = Coordinate()
        var pointbF = BazierUtils.getBezierPoint(PointF(pointA.x, pointA.y), PointF(pointB.x, pointB.y), PointF(pointC.x, pointC.y), 0.7F)
        pointb.x = pointbF.x
        pointb.y = pointbF.y
//        pointb.x = pointA.x + 40F - 40F * Math.sin(45.toDouble()).toFloat()
//        pointb.y = pointA.y + 34F * Math.sin(45.toDouble()).toFloat()

        var pointf = Coordinate()
        var pointfF = BazierUtils.getBezierPoint(PointF(pointE.x, pointE.y), PointF(pointF.x, pointF.y), PointF(pointG.x, pointG.y), 0.7F)
        pointf.x = pointfF.x
        pointf.y = pointfF.y
//        pointf.x = pointE.x + 40F - 40F * (Math.cos(45.toDouble())).toFloat()
//        pointf.y = pointE.y + 40F * (Math.cos(45.toDouble())).toFloat()

        dogEaredPath.moveTo(pointb.x, pointb.y)
        dogEaredPath.lineTo(pointD.x, pointD.y)
        dogEaredPath.lineTo(pointf.x, pointf.y)
        dogEaredPath.close()

        dogEaredPath.op(contentPath, Path.Op.DIFFERENCE)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawPaper(it)
        }
    }

    private fun drawPaper(canvas: Canvas) {
        with(canvas) {
            //            drawRect((width - paperWidth) / 2F, (height - paperHeight) / 2F, (width - paperWidth) / 2F + paperWidth, (height - paperHeight) / 2F + paperHeight, mPaint)
            save()
            translate((width - paperWidth) / 2F, (height - paperHeight) / 2F)
            mPaint.style = Paint.Style.STROKE
            mPaint.color = Color.BLUE
            mPaint.clearShadowLayer()
            drawPath(contentPath, mPaint)
            mPaint.style = Paint.Style.FILL
            mPaint.color = Color.RED
            mPaint.setShadowLayer(30F, 10F, -10F, Color.GRAY)
            drawPath(dogEaredPath, mPaint)
            mPaint.setShader(LinearGradient(0F, height.toFloat(), pointD.x, pointD.y, Color.GRAY, Color.WHITE, Shader.TileMode.MIRROR))
            drawPath(dogEaredPath, mPaint)
            restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
}