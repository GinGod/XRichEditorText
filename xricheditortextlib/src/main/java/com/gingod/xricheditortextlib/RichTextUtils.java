package com.gingod.xricheditortextlib;

import android.widget.ImageView;

import com.gingod.xricheditortextlib.bean.RichEditData;

/**
 * 图片加载框架
 *
 * @author
 */
public class RichTextUtils {
    private static RichTextUtils instance;
    private IImageLoader imageLoader;

    public static RichTextUtils getInstance() {
        if (instance == null) {
            synchronized (RichTextUtils.class) {
                if (instance == null) {
                    instance = new RichTextUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 自定义图片加载框架
     */
    public void setImageLoader(IImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    /**
     * 图片加载
     */
    public void loadImage(RichEditData.Data imageData, String imagePath, ImageView imageView, int imageHeight) {
        try {
            if (imageLoader != null) {
                imageLoader.loadImage(imageData, imagePath, imageView, imageHeight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
