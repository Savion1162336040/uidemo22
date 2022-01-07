package com.pengyeah.card3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * @Author: savion
 * @Date: 2021/12/30 10:43
 * @Des:
 **/
public class CusView extends View {

    private Camera camera;
    private Matrix matrix;
    private Bitmap bitmap;
    private Paint paint;

    public CusView(Context context) {
        this(context, null);
    }

    public CusView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        camera = new Camera();
        matrix = new Matrix();
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.p2_480);
        bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bm, null,
                new RectF(0, 0, 300, 300),
                null);
        bm.recycle();

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);
    }

    private float rotate = 0;

    public void setRotate(float rotate) {
        this.rotate = rotate;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        matrix.reset();
        camera.save();
        //camera旋转是逆时针方向
//        camera.setLocation(camera.getLocationX(),camera.getLocationY(),-30);
//        camera.rotate(0, 0, 30);
        //camera.translate(-50,0,-8);
//        camera.setLocation(-2,0,-8);
        camera.rotateY(rotate);
        camera.getMatrix(matrix);
        camera.restore();
        //matrix.postTranslate(getWidth() / 2f - bitmap.getWidth() / 2f, 300);
//        canvas.concat(matrix);
//        canvas.drawBitmap(bitmap,
//                new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
//                new RectF(canvas.getWidth()/2-bitmap.getWidth()/2, 0, 200, 200),
//                null);
        canvas.save();
        canvas.translate(50, 50);
        canvas.drawBitmap(bitmap, matrix, null);
        canvas.restore();

        matrix.reset();
        camera.save();
        //camera.setLocation(0,0,0);
        camera.translate(0, 0, 0);
        camera.rotate(0, -rotate, 0);
        camera.getMatrix(matrix);
        camera.restore();

        float scale = getResources().getDisplayMetrics().density;
        float[] mValues = new float[9];
        matrix.getValues(mValues);
        mValues[6] = mValues[6] / scale;
        mValues[7] = mValues[7] / scale;
        matrix.setValues(mValues);

        matrix.preTranslate(-getWidth() / 2f-50, -getHeight() / 2f);
        matrix.postTranslate(getWidth() / 2f-50, getHeight() / 2f);

        float left = getWidth() / 2f+100;
        float top = getHeight() / 2f - bitmap.getHeight() / 2f;
        float right = left + bitmap.getWidth();
        float bottom = top + bitmap.getHeight();
        canvas.drawRect(getWidth()/2,0,getWidth()/2,getHeight(),paint);
        canvas.drawRect(getWidth()/2-50,0,getWidth()/2-50,getHeight(),paint);
        canvas.drawRect(left, top, right, bottom, paint);
        canvas.save();
        canvas.concat(matrix);
        canvas.drawBitmap(bitmap, null,
                new RectF(left, top, right, bottom), null);
        canvas.restore();
        ;
    }
}
