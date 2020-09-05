package com.pengyeah.flowview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.BounceInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import com.pengyeah.flowview.coordinate.Coordinate
import com.pengyeah.flowview.func.Func5
import com.pengyeah.flowview.func.Func6
import com.pengyeah.flowview.func.Func7
import com.pengyeah.flowview.func.Func8

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
     * 构成波浪的关键点坐标
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

    /**
     * 是否为展开状态
     */
    var isExpanded: Boolean = false

    /**
     * 波浪基线高度
     */
    var waveLineHeight: Float = 0F

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

        waveLineHeight = h / 2F

        pointA.x = width - oriWaveHeight / 4
        pointA.y = waveLineHeight - oriWaveHeight
        pointA.xFunc = Func5(pointA.x, pointA.x)
        val pointAyFunc = Func7(pointA.y, pointA.y)
        pointAyFunc.rate = 3 * width / height.toFloat()
        pointA.yFunc = pointAyFunc


        pointB.x = width - oriWaveHeight / 4
        pointB.y = waveLineHeight - 3 * oriWaveHeight / 4
        pointB.xFunc = Func5(pointB.x, pointB.x)
        val pointByFunc = Func7(pointB.y, pointB.y)
        pointByFunc.rate = 2 * width / height.toFloat()
        pointB.yFunc = pointByFunc

        pointC.x = width - oriWaveHeight / 2
        pointC.y = waveLineHeight - oriWaveHeight / 2
        pointC.xFunc = Func5(pointC.x, pointC.x)
        val pointCyFunc = Func7(pointC.y, pointC.y)
        pointCyFunc.rate = width / height.toFloat()
        pointC.yFunc = pointCyFunc

        pointD = getPointDCoordinate(pointB, pointC)

        pointE.x = width - oriWaveHeight / 2
        pointE.y = waveLineHeight + oriWaveHeight / 2
        pointE.xFunc = Func5(pointE.x, pointE.x)
        val pointEyFunc = Func8(pointE.y, height.toFloat())
        pointEyFunc.rate = width / height.toFloat()
//        pointEyFunc.rate = 2 * ((height - pointE.y) / (height - pointE.y - oriWaveHeight / 4)) * width / height.toFloat()
        pointEyFunc.inParamMin = pointE.y
        pointE.yFunc = pointEyFunc

        pointF.x = width - oriWaveHeight / 4
        pointF.y = waveLineHeight + 3 * oriWaveHeight / 4
        pointF.xFunc = Func5(pointF.x, pointF.x)
        val pointFyFunc = Func8(pointF.y, height.toFloat())
        pointFyFunc.rate = 2 * width / height.toFloat()
        pointFyFunc.inParamMin = pointF.y
        pointF.yFunc = pointFyFunc

        pointG.x = width - oriWaveHeight / 4
        pointG.y = waveLineHeight + oriWaveHeight
        pointG.xFunc = Func5(pointG.x, pointG.x)
        val pointGyFunc = Func8(pointG.y, height.toFloat())
        pointGyFunc.rate = 3 * width / height.toFloat()
        pointGyFunc.inParamMin = pointG.y
        pointG.yFunc = pointGyFunc

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

        path.addCircle(pointA.x, pointA.y, 10F, Path.Direction.CW)
        path.addCircle(pointB.x, pointB.y, 10F, Path.Direction.CW)
        path.addCircle(pointC.x, pointC.y, 10F, Path.Direction.CW)
        path.addCircle(pointD.x, pointD.y, 10F, Path.Direction.CW)
        path.addCircle(pointE.x, pointE.y, 10F, Path.Direction.CW)
        path.addCircle(pointF.x, pointF.y, 10F, Path.Direction.CW)
        path.addCircle(pointG.x, pointG.y, 10F, Path.Direction.CW)

        path.close()

        return path
    }

    fun getPointDCoordinate(pointB: Coordinate, pointC: Coordinate): Coordinate {
        val dy = Math.abs(pointC.y - pointB.y)
        val dx = Math.abs(pointC.x - pointB.x)
        //B点到D点的距离
        val tempDy = waveLineHeight - pointB.y
        pointD.x = pointB.x - (dx * tempDy / dy)
        pointD.y = waveLineHeight
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

    var offsetAnimator: ValueAnimator? = null

    fun startAnim() {
        offsetAnimator?.cancel()
        offsetAnimator = ValueAnimator.ofFloat(offsetX, -width.toFloat())
        offsetAnimator?.let {
            it.duration = 300L
            it.interpolator = BounceInterpolator()
            it.addUpdateListener {
                val tempOffsetX: Float = it.animatedValue as Float
                executePointFunc(pointA, tempOffsetX)
                executePointFunc(pointB, tempOffsetX)
                executePointFunc(pointC, tempOffsetX)
                getPointDCoordinate(pointB, pointC)
                Log.i("pengyeah", "pointB==>" + pointB.toString())
                Log.i("pengyeah", "pointC==>" + pointC.toString())
                Log.i("pengyeah", "pointD==>" + pointD.toString())
                executePointFunc(pointE, tempOffsetX)
                executePointFunc(pointF, tempOffsetX)
                executePointFunc(pointG, tempOffsetX)

                postInvalidate()
            }

            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    resetInitValueFunc(pointA)
                    resetInitValueFunc(pointB)
                    resetInitValueFunc(pointC)
                    getPointDCoordinate(pointB, pointC)
                    resetInitValueFunc(pointE)
                    resetInitValueFunc(pointF)
                    resetInitValueFunc(pointG)
                }
            })
            it.start()
        }

    }

    var downX: Float = 0F
    var downY: Float = 0F
    var offsetX: Float = 0F

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_UP -> {
//                    startAnim()

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

                    postInvalidate()
                }
                MotionEvent.ACTION_DOWN -> {
                    downX = it.x
                    downY = it.y

                    postInvalidate()
                }
            }

        }
        return true
    }
}