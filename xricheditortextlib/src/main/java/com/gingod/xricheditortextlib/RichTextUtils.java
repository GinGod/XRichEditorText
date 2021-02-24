package com.gingod.xricheditortextlib;

import android.widget.ImageView;

/**
 * 图片加载框架
 * @author
 */
public class RichTextUtils {
    private static RichTextUtils instance;
    private IImageLoader imageLoader;

    public static RichTextUtils getInstance(){
        if (instance == null){
            synchronized (RichTextUtils.class){
                if (instance == null){
                    instance = new RichTextUtils();
                }
            }
        }
        return instance;
    }

    public void setImageLoader(IImageLoader imageLoader){
        this.imageLoader = imageLoader;
    }

    public void loadImage(String imagePath, ImageView imageView, int imageHeight){
        if (imageLoader != null){
            imageLoader.loadImage(imagePath, imageView, imageHeight);
        }
    }
}
