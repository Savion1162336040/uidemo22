package com.pengyeah.card3d

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.pupu.card3d.R

/**
 *  Created by pupu on 2020/9/11
 *  佛祖开光，永无bug
 *  God bless U
 */
class Card3DView : View {

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

        mCamera.getMatrix(mMatrix)

        mSrcBm = BitmapFactory.decodeResource(resources, R.drawable.img_sample)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {

//            mCamera.save()
            mMatrix.reset()
//            mCamera.translate(width / 4F, -height / 4F, 0F)
            mCamera.rotateX(80F)
            mCamera.getMatrix(matrix)

            mCamera.applyToCanvas(it)
//            mCamera.restore()

            mMatrix.preTranslate(-width / 2F, -height / 2F)
            mMatrix.postTranslate(width / 2F, height / 2F)


            it.drawRect(Rect(width / 4, height / 4, width * 3 / 4, height * 3 / 4), mPaint)
//            it.drawBitmap(mSrcBm!!,0F,0F,mPaint)
//            it.drawBitmap(mSrcBm!!, mMatrix, mPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }
}