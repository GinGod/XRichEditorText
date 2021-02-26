package com.gingod.xricheditortextlib;

import android.widget.ImageView;

import com.gingod.xricheditortextlib.bean.RichEditData;

/**
 * 图片加载
 *
 * @author
 */
public interface IImageLoader {
    void loadImage(RichEditData.Data imageData, String imagePath, ImageView imageView, int imageHeight);
}
