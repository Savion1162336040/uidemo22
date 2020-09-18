package com.pengyeah.card3d

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.pengyeah.card3d.utils.DisplayUtils
import com.pengyeah.card3d.R

/**
 *  Created by pengyeah on 2020/9/11
 *  佛祖开光，永无bug
 *  God bless U
 */
class Card3DView : ViewGroup {

    var mCamera: Camera = Camera()
    var mPaint: Paint = Paint()
    var mMatrix: Matrix = Matrix()

    var mSrcBm: Bitmap? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    private fun initView(context: Context?, attrs: AttributeSet?) {
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.RED


        mSrcBm = BitmapFactory.decodeResource(resources, R.drawable.img_sample)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            mCamera.translate(-100F,0F,0F)

            it.save()
            mCamera.save()
            mCamera.translate(0F, 0F, 300F)
            mCamera.rotateX(30F)
            mCamera.applyToCanvas(it)
            mCamera.restore()

            mPaint.color = Color.GRAY
            it.drawRoundRect(RectF(width / 4F, height / 4F, width * 3 / 4F, height * 3 / 4F), 20F, 20F, mPaint)
            it.restore()

            it.save()
            mCamera.save()
            mCamera.translate(0F, 0F, 200F)
            mCamera.rotateX(30F)
            mCamera.applyToCanvas(it)
            mCamera.restore()

            mPaint.color = Color.RED
            it.drawRoundRect(RectF(width / 4F, height / 4F, width * 3 / 4F, height * 3 / 4F), 20F, 20F, mPaint)
            it.restore()

            it.save()
            mCamera.save()
            mCamera.translate(0F, 0F, 100F)
            mCamera.rotateX(30F)
            mCamera.applyToCanvas(it)
            mCamera.restore()

            mPaint.color = Color.GRAY
            it.drawRoundRect(RectF(width / 4F, height / 4F, width * 3 / 4F, height * 3 / 4F), 20F, 20F, mPaint)
            it.restore()

            it.save()
            mCamera.save()
            mCamera.translate(0F, 0F, 0F)
            mCamera.rotateX(30F)
            mCamera.applyToCanvas(it)
            mCamera.restore()

            mPaint.color = Color.RED
            it.drawRoundRect(RectF(width / 4F, height / 4F, width * 3 / 4F, height * 3 / 4F), 20F, 20F, mPaint)
            it.restore()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        return super.drawChild(canvas, child, drawingTime)
    }

}