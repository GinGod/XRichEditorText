package com.gingod.xricheditortext;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gingod.xricheditortextlib.IImageLoader;
import com.gingod.xricheditortextlib.RichTextEditor;
import com.gingod.xricheditortextlib.RichTextUtils;
import com.gingod.xricheditortextlib.bean.EditData;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseSimpleActivity {
    @BindView(R.id.rte_main)
    RichTextEditor rte_main;
    @BindView(R.id.tv_main_pic)
    TextView tv_main_pic;

    private String imagePath = "http://b.zol-img.com.cn/sjbizhi/images/10/640x1136/1572123845476.jpg";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initValues() {
        RichTextUtils.getInstance().setImageLoader(new IImageLoader() {
            @Override
            public void loadImage(String imagePath, ImageView imageView, int imageHeight) {
                Glide.with(mActivity).load(imagePath).into(imageView);
            }
        });

        rte_main.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(View view, String imagePath) {
                Toast.makeText(MainActivity.this, "图片点击!", Toast.LENGTH_SHORT).show();
            }
        });

        rte_main.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {
            @Override
            public void onRtImageDelete(String imagePath) {
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

    @OnClick(R.id.tv_main_pic)
    public void onClick() {
        rte_main.insertImage(imagePath);
    }

    @Override
    protected void onDestroy() {
        EditData editData = new EditData();
        editData.content = rte_main.getEditData();
        BasisSPUtils.setStringPreferences(mActivity, "editData", mGson.toJson(editData));
        super.onDestroy();
    }
}