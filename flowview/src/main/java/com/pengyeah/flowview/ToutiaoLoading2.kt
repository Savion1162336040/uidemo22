package com.pengyeah.flowview

import android.graphics.*
import android.graphics.drawable.Drawable
import com.pengyeah.flowview.coordinate.Point
import com.pengyeah.flowview.func.Func1
import com.pengyeah.flowview.func.Func2

class ToutiaoLoading2 : Drawable {

    val pointA: Point = Point()

    val pointB: Point = Point(func = Func1())

    val pointC: Point = Point(func = Func1())

    val pointD: Point = Point()

    val mPath: Path = Path()

    val mPaint: Paint = Paint()

    constructor() {
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.color = Color.RED
        mPaint.strokeWidth = 10F
    }

    override fun draw(canvas: Canvas) {

        mPath.reset()
        mPath.moveTo(pointA.x.toFloat(), pointA.y.toFloat())
        mPath.lineTo(pointB.x.toFloat(), pointB.y.toFloat())
        mPath.lineTo(pointC.x.toFloat(), pointC.y.toFloat())
        mPath.lineTo(pointD.x.toFloat(), pointD.y.toFloat())
        mPath.close()

        canvas.drawPath(mPath, mPaint)

    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        //初始化A、B、C、D
        pointA.x = mPaint.strokeWidth.toInt()
        pointA.y = mPaint.strokeWidth.toInt() + bounds.height() / 10

        pointB.x = bounds.right - mPaint.strokeWidth.toInt()
        pointB.y = bounds.bottom - mPaint.strokeWidth.toInt()

        pointC.x = bounds.right - mPaint.strokeWidth.toInt()
        pointC.y = mPaint.strokeWidth.toInt() + bounds.height() / 10

        pointD.x = mPaint.strokeWidth.toInt()
        pointD.y = bounds.bottom - mPaint.strokeWidth.toInt()

    }

    override fun setAlpha(alpha: Int) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    fun transform(offset: Int) {
        if (offset <= bounds.height() / 2) {
            pointB.y = bounds.bottom - mPaint.strokeWidth.toInt() - pointB.func.execute(2 * offset)
            pointC.y = pointC.func.execute(2 * offset)
        } else {
            pointB.y = mPaint.strokeWidth.toInt()
            pointC.y = bounds.bottom - mPaint.strokeWidth.toInt() - bounds.height() / 10
        }
        invalidateSelf()
    }
}