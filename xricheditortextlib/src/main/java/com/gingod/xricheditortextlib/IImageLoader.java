package com.gingod.xricheditortextlib;

import android.widget.ImageView;

import com.gingod.xricheditortextlib.bean.EditData;

/**
 * 图片加载
 *
 * @author
 */
public interface IImageLoader {
    void loadImage(EditData.Data imageData, String imagePath, ImageView imageView, int imageHeight);
}
