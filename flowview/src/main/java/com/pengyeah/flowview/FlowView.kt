package com.pengyeah.flowview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.pengyeah.flowview.coordinate.Coordinate
import com.pengyeah.flowview.func.Func5
import com.pengyeah.flowview.func.Func6

/**
 *  Created by pupu on 2020/9/1
 *  佛祖开光，永无bug
 *  God bless U
 */
class FlowView : View {

    val TAG: String? = FlowView::class.simpleName

    /**
     * 浪高
     */
    var waveHeight: Float = 0F

    /**
     * 初始浪高
     */
    var oriWaveHeight: Float = 0F

    /**
     * 构成波浪的关键五点坐标
     */
    var pointA: Coordinate = Coordinate()
    var pointB: Coordinate = Coordinate()
    var pointC: Coordinate = Coordinate()
    var pointD: Coordinate = Coordinate()
    var pointE: Coordinate = Coordinate()
    var pointF: Coordinate = Coordinate()
    var pointG: Coordinate = Coordinate()

    var path: Path = Path()

    var paint: Paint = Paint()

    @ColorInt
    var color: Int = Color.RED

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }


    fun initView(context: Context?, attrs: AttributeSet?) {
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.color = color
        paint.strokeWidth = 10F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawWave(canvas)
    }

    private fun drawWave(canvas: Canvas?) {
        canvas?.let {
            configPath()
            it.drawPath(path, paint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        oriWaveHeight = width / 6.toFloat()

        waveHeight = oriWaveHeight

        pointA.x = width - oriWaveHeight / 4
        pointA.y = h / 2F - oriWaveHeight
        pointA.xFunc = Func6(pointA.x, width.toFloat())


        pointB.x = width - oriWaveHeight / 4
        pointB.y = h / 2F - 3 * oriWaveHeight / 4
        pointB.xFunc = Func6(pointB.x, width.toFloat())

        pointC.x = width - oriWaveHeight / 2
        pointC.y = h / 2F - oriWaveHeight / 2
        pointC.xFunc = Func5(pointC.x, width.toFloat())

        pointD = getPointDCoordinate(pointB, pointC)

        pointE.x = width - oriWaveHeight / 2
        pointE.y = h / 2F + oriWaveHeight / 2
        pointE.xFunc = Func5(pointE.x, width.toFloat())

        pointF.x = width - oriWaveHeight / 4
        pointF.y = h / 2F + 3 * oriWaveHeight / 4
        pointF.xFunc = Func6(pointF.x, width.toFloat())

        pointG.x = width - oriWaveHeight / 4
        pointG.y = h / 2F + oriWaveHeight
        pointG.xFunc = Func6(pointG.x, width.toFloat())

        //init path
        configPath()
    }

    private fun configPath(): Path {

        path.reset()
        path.moveTo(width.toFloat(), 0F)
        path.lineTo(pointA.x, 0F)
        path.lineTo(pointA.x, pointA.y)

        path.quadTo(pointB.x, pointB.y, pointC.x, pointC.y)
        path.quadTo(pointD.x, pointD.y, pointE.x, pointE.y)
        path.quadTo(pointF.x, pointF.y, pointG.x, pointG.y)

//        path.lineTo(pointB.x, pointB.y)
//        path.lineTo(pointC.x, pointC.y)
//        path.lineTo(pointD.x, pointD.y)
//        path.lineTo(pointE.x, pointE.y)
//        path.lineTo(pointF.x, pointF.y)

        path.lineTo(pointG.x, pointG.y)
        path.lineTo(pointG.x, height.toFloat())
        path.lineTo(width.toFloat(), height.toFloat())
        path.close()

        return path
    }

    fun getPointDCoordinate(pointB: Coordinate, pointC: Coordinate): Coordinate {
        val dy = Math.abs(pointC.y - pointB.y)
        val dx = Math.abs(pointC.x - pointB.x)
        //B点到D点的距离
        val tempDy = height / 2F - pointB.y
        pointD.x = pointB.x - (dx * tempDy / dy)
        pointD.y = height / 2F
        return pointD
    }

    private fun executePointFunc(point: Coordinate, offset: Float) {
        point.xFunc?.let {
            point.x = it.execute(offset)
        }
        point.yFunc?.let {
            point.y = it.execute(offset)
        }
    }

    private fun resetInitValueFunc(point: Coordinate) {
        point.xFunc?.let {
            it.initValue = point.x
        }

        point.yFunc?.let {
            it.initValue = point.y
        }
    }

    var downX: Float = 0F
    var downY: Float = 0F
    var offsetX: Float = 0F

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_UP -> {
                    downX = 0F
                    downY = 0F
                    offsetX = 0F

                    resetInitValueFunc(pointA)
                    resetInitValueFunc(pointB)
                    resetInitValueFunc(pointC)
                    getPointDCoordinate(pointB, pointC)
                    resetInitValueFunc(pointE)
                    resetInitValueFunc(pointF)
                    resetInitValueFunc(pointG)
                }
                MotionEvent.ACTION_MOVE -> {

                    offsetX = it.x - downX

                    executePointFunc(pointA, offsetX)
                    executePointFunc(pointB, offsetX)
                    executePointFunc(pointC, offsetX)
                    getPointDCoordinate(pointB, pointC)
                    Log.i("pengyeah", "pointB==>" + pointB.toString())
                    Log.i("pengyeah", "pointC==>" + pointC.toString())
                    Log.i("pengyeah", "pointD==>" + pointD.toString())
                    executePointFunc(pointE, offsetX)
                    executePointFunc(pointF, offsetX)
                    executePointFunc(pointG, offsetX)

                }
                MotionEvent.ACTION_DOWN -> {
                    downX = it.x
                    downY = it.y
                }
            }
            postInvalidate()
        }
        return true
    }
}