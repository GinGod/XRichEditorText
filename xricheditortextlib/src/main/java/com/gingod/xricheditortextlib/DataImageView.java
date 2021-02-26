package com.gingod.xricheditortextlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.gingod.xricheditortextlib.bean.RichEditData;

/**
 * 自定义ImageView，可以存放Path等信息(可画边框)
 *
 * @author
 */
public class DataImageView extends AppCompatImageView {
    /**
     * 是否显示边框, 边框颜色和宽度
     */
    private boolean showBorder = false;
    private int borderColor = Color.GRAY;
    private int borderWidth = 5;
    private Paint paint;

    /**
     * 图片或者视频信息
     */
    private RichEditData.Data imageData;
    private String absolutePath;

    public DataImageView(Context context) {
        this(context, null);
    }

    public DataImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData();
    }

    private void initData() {
        //画笔
        paint = new Paint();
        //设置颜色
        paint.setColor(borderColor);
        //设置画笔的宽度
        paint.setStrokeWidth(borderWidth);
        //设置画笔的风格-不能设成填充FILL否则看不到图片了
        paint.setStyle(Paint.Style.STROKE);
    }

    public RichEditData.Data getImageData() {
        return imageData;
    }

    public void setImageData(RichEditData.Data imageData) {
        this.imageData = imageData;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBorder) {
            //画边框
            Rect rec = canvas.getClipBounds();
            // 这两句可以使底部和右侧边框更大
            //rec.bottom -= 2;
            //rec.right -= 2;
            canvas.drawRect(rec, paint);
        }
    }
}
