package com.gingod.xricheditortextlib.bean;

import java.util.List;

/**
 * 富文本数据
 *
 * @author
 */
public class RichEditData {
    /**
     * 文字
     */
    public final static int TEXT = 252;
    /**
     * 图片
     */
    public final static int IMAGE = 252 + 1;
    /**
     * 视频
     */
    public final static int VIDEO = 252 + 2;
    /**
     * 视频上传失败
     */
    public final static int UPLOAD_VIDEO_FAIL = 252 + 3;
    /**
     * 视频上传成功
     */
    public final static int UPLOAD_VIDEO_SUCCESS = 252 + 4;
    /**
     * 标题
     */
    public String title;
    /**
     * 编辑人
     */
    public String name;
    /**
     * 编辑时间
     */
    public String time;
    /**
     * 富文本编辑内容
     */
    public List<Data> content;

    public static class Data {
        /**
         * TEXT 文字, IMAGE 图片, VIDEO 视频
         */
        public int type;
        /**
         * 文字内容
         */
        public String inputStr;
        /**
         * 图片网络路径
         */
        public String imagePath;
        /**
         * 图片本地路径
         */
        public String localImagePath;
        /**
         * 视频网络路径
         */
        public String videoPath;
        /**
         * 视频本地路径
         */
        public String videoPicPath;
        /**
         * 视频上传进度, 进度条使用; 视频上传状态: UPLOAD_VIDEO_FAIL 成功, UPLOAD_VIDEO_SUCCESS 失败
         */
        public int videoProgress;
        /**
         * 视频上传进度Str, 用于展示进度
         */
        public String videoProgressStr;
        /**
         * 视频本地地址, 更新视频上传进度时以此为判断标准
         */
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
