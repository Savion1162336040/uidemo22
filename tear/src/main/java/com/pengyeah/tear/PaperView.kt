package com.pengyeah.tear

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.pengyeah.tear.coordinate.Coordinate

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
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            drawPaper(it)
        }
    }

    private fun drawPaper(canvas: Canvas) {
        with(canvas) {
            canvas.drawRect((width - paperWidth) / 2F, (height - paperHeight) / 2F, (width - paperWidth) / 2F + paperWidth, (height - paperHeight) / 2F + paperHeight, mPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
}