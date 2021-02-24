package com.gingod.xricheditortextlib.bean;

import java.util.List;

public class EditData {
    public String title;
    public String name;
    public String time;
    public List<Data> content;

    public static class Data {
        public String inputStr;
        public String imagePath;
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
