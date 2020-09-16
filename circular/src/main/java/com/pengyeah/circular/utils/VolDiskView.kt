package com.pengyeah.circular.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.ColorInt

/**
 *  Created by pupu on 2020/9/16
 *  佛祖开光，永无bug
 *  God bless U
 */
class VolDiskView : SurfaceView, Runnable, SurfaceHolder.Callback {

    private var isDrawing: Boolean = false

    private var canvas: Canvas? = null

    var paint: Paint = Paint()

    @ColorInt
    var color: Int = Color.RED

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.color = color
//        paint.strokeWidth = 10F
        holder.addCallback(this)
        setZOrderOnTop(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    override fun run() {
        if (isDrawing) {
            canvas = holder.lockCanvas()
            canvas?.let {
                it.drawColor(Color.TRANSPARENT)
                it.drawCircle(100F, 100F, 100F, paint)
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        isDrawing = false
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        isDrawing = true
        Thread(this).start()
    }


}