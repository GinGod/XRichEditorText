package com.gingod.xricheditortext;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gingod.xricheditortextlib.IImageLoader;
import com.gingod.xricheditortextlib.RichTextEditor;
import com.gingod.xricheditortextlib.RichTextUtils;
import com.gingod.xricheditortextlib.bean.EditData;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseSimpleActivity {
    @BindView(R.id.rte_main)
    RichTextEditor rte_main;

    private String imagePath = "http://img.hb.aicdn.com/a1f189d4a420ef1927317ebfacc2ae055ff9f212148fb-iEyFWS_fw658";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initValues() {
        RichTextUtils.getInstance().setImageLoader(new IImageLoader() {
            @Override
            public void loadImage(EditData.Data imageData, String imagePath, ImageView imageView, int imageHeight) {
                Glide.with(mActivity).load(imagePath)
                        .error(imageData.type == EditData.IMAGE ? com.gingod.xricheditortextlib.R.drawable.img_load_fail : com.gingod.xricheditortextlib.R.drawable.gray_rect_bg_gradient)
                        .into(imageView);
            }
        });

        rte_main.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(View view, EditData.Data imageData, String imagePath) {
                Toast.makeText(MainActivity.this, "图片点击!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRtVideoPlayClick(View view, EditData.Data imageData, String imagePath) {
                Toast.makeText(MainActivity.this, "视频播放!", Toast.LENGTH_SHORT).show();
            }
        });

        rte_main.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {
            @Override
            public void onRtImageDelete(EditData.Data imageData) {
                Toast.makeText(MainActivity.this, "图片删除", Toast.LENGTH_SHORT).show();
            }
        });

        String editData = BasisSPUtils.getStringPreferences(mActivity, "editData", "");
        if (!TextUtils.isEmpty(editData)) {
            Log.e("editData", editData);
            EditData editData1 = mGson.fromJson(editData, EditData.class);
            rte_main.setEditData(editData1.content);
        }
    }

    @OnClick({R.id.tv_main_pic, R.id.tv_main_video})
    public void onClick(View v) {
        EditData.Data imageData = new EditData.Data();
        imageData.imagePath = imagePath;
        imageData.videoPicPath = imagePath + "111";
        imageData.localVideoPath = imagePath;
        switch (v.getId()) {
            case R.id.tv_main_pic:
                imageData.type = EditData.IMAGE;
                break;
            case R.id.tv_main_video:
                imageData.type = EditData.VIDEO;
                break;
        }
        rte_main.insertImageOrVideo(imageData);
    }

    @Override
    protected void onDestroy() {
        EditData editData = new EditData();
        editData.content = rte_main.getEditData();
        BasisSPUtils.setStringPreferences(mActivity, "editData", mGson.toJson(editData));
        String data = mGson.toJson(editData);
        Log.e("123", data);
        String data1 = data.replaceAll("\\\\n|\\\\r\\\\n|\\\\r", "123");
        Log.e("123", data1);
        super.onDestroy();
    }
}