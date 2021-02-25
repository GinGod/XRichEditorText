package com.gingod.xricheditortextlib.bean;

import java.util.List;

/**
 * 富文本数据
 *
 * @author
 */
public class EditData {
    public final static int TEXT = 252;
    public final static int IMAGE = 252 + 1;
    public final static int VIDEO = 252 + 2;
    public final static int UPLOAD_VIDEO_FAIL = 252 + 3;
    public final static int UPLOAD_VIDEO_SUCCESS = 252 + 4;
    public String title;
    public String name;
    public String time;
    public List<Data> content;

    public static class Data {
        /**
         * 0 文字, 1 图片, 2 视频
         */
        public int type;
        public String inputStr;
        /**
         * 图片相关
         */
        public String imagePath;
        public String localImagePath;
        /**
         * 视频相关
         */
        public String videoPath;
        public String videoPicPath;
        public int videoProgress;
        public String videoProgressStr;
        public String localVideoPath;
    }

    @Override
    public String toString() {
        return "EditData{" +
                "title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", content=" + content +
                '}';
    }
}
