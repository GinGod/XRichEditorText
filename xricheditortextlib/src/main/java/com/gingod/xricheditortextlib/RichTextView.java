package com.gingod.xricheditortextlib;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 显示富文本
 *
 * @author
 */
public class RichTextView extends ScrollView {
    /**
     * edittext常规padding是10dp
     */
    private static final int EDIT_PADDING = 10;
    /**
     * 新生的view都会打一个tag，对每个view来说，这个tag是唯一的。
     */
    private int viewTagIndex = 1;
    /**
     * 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
     */
    private LinearLayout allLayout;
    private LayoutInflater inflater;
    /**
     * 只在图片View添加或remove时，触发transition动画
     */
    private LayoutTransition mTransitioner;
    private int editNormalPadding = 0;
    /**
     * 图片点击事件
     */
    private OnClickListener btnListener;
    /**
     * 图片地址集合
     */
    private ArrayList<String> imagePaths;
    /**
     * 关键词高亮
     */
    private String keywords;
    /**
     * 图片点击监听
     */
    private OnRtImageClickListener onRtImageClickListener;
    /**
     * 自定义属性, 插入的图片显示高度, 为0显示原始高度
     */
    private int rtImageHeight = 0;
    /**
     * 两张相邻图片间距
     */
    private int rtImageBottom = 10;
    /**
     * 文字相关属性，初始提示信息，文字大小和颜色
     */
    private String rtTextInitHint = "没有内容";
    private int rtTextSize = 16;
    private int rtTextColor = Color.parseColor("#757575");
    private int rtTextLineSpace = 8;

    public RichTextView(Context context) {
        this(context, null);
    }

    public RichTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichTextView);
        rtImageHeight = ta.getInteger(R.styleable.RichTextView_rt_view_image_height, 0);
        rtImageBottom = ta.getInteger(R.styleable.RichTextView_rt_view_image_bottom, 10);
        rtTextSize = ta.getDimensionPixelSize(R.styleable.RichTextView_rt_view_text_size, 16);
        rtTextLineSpace = ta.getDimensionPixelSize(R.styleable.RichTextView_rt_view_text_line_space, 8);
        rtTextColor = ta.getColor(R.styleable.RichTextView_rt_view_text_color, Color.parseColor("#757575"));
        rtTextInitHint = ta.getString(R.styleable.RichTextView_rt_view_text_init_hint);
        ta.recycle();

        imagePaths = new ArrayList<>();
        inflater = LayoutInflater.from(context);

        // 1. 初始化allLayout
        allLayout = new LinearLayout(context);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        allLayout.setPadding(dip2px(context, 15), dip2px(context, 15), dip2px(context, 15), dip2px(context, 15));
        addView(allLayout, layoutParams);

        btnListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v instanceof DataImageView) {
                    DataImageView imageView = (DataImageView) v;
                    if (onRtImageClickListener != null) {
                        onRtImageClickListener.onRtImageClick(imageView, imageView.getAbsolutePath());
                    }
                }
            }
        };

        LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        TextView firstText = createTextView(rtTextInitHint, dip2px(context, EDIT_PADDING));
        allLayout.addView(firstText, firstEditParam);
    }

    /**
     * dip2px
     *
     * @return
     */
    private int dip2px(Context context, float dipValue) {
        float m = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

    /**
     * 图片点击
     */
    public interface OnRtImageClickListener {
        /**
         * 图片点击
         * @param view
         * @param imagePath
         */
        void onRtImageClick(View view, String imagePath);
    }

    /**
     * 图片点击
     */
    public void setOnRtImageClickListener(OnRtImageClickListener onRtImageClickListener) {
        this.onRtImageClickListener = onRtImageClickListener;
    }

    /**
     * 生成文本输入框
     */
    public TextView createTextView(String hint, int paddingTop) {
        TextView textView = (TextView) inflater.inflate(R.layout.rich_textview, null);
        textView.setTag(viewTagIndex++);
        textView.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
        textView.setHint(hint);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, rtTextSize);
        textView.setLineSpacing(rtTextLineSpace, 1.0f);
        textView.setTextColor(rtTextColor);
        return textView;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageLayout() {
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.edit_imageview, null);
        layout.setTag(viewTagIndex++);
        View closeView = layout.findViewById(R.id.image_close);
        closeView.setVisibility(GONE);
        DataImageView imageView = layout.findViewById(R.id.edit_imageView);
        imageView.setOnClickListener(btnListener);
        return layout;
    }

    /**
     * 关键字高亮显示
     *
     * @param target 需要高亮的关键字
     * @param text   需要显示的文字
     * @return spannable 处理完后的结果，记得不要toString()，否则没有效果
     */
    public static SpannableStringBuilder highlight(String text, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        CharacterStyle span;
        try {
            Pattern p = Pattern.compile(target);
            Matcher m = p.matcher(text);
            while (m.find()) {
                span = new ForegroundColorSpan(Color.parseColor("#EE5C42"));
                spannable.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spannable;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * 在特定位置插入EditText
     *
     * @param index   位置
     * @param editStr EditText显示的文字
     */
    public void addTextViewAtIndex(final int index, CharSequence editStr) {
        try {
            TextView textView = createTextView("", EDIT_PADDING);
            //搜索关键词高亮
            if (!TextUtils.isEmpty(keywords)) {
                SpannableStringBuilder textStr = highlight(editStr.toString(), keywords);
                textView.setText(textStr);
            } else {
                textView.setText(editStr);
            }

            allLayout.addView(textView, index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在特定位置添加ImageView
     */
    public void addImageViewAtIndex(final int index, final String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return;
        }
        imagePaths.add(imagePath);
        RelativeLayout imageLayout = createImageLayout();
        if (imageLayout == null) {
            return;
        }
        final DataImageView imageView = imageLayout.findViewById(R.id.edit_imageView);
        imageView.setAbsolutePath(imagePath);
        RichTextUtils.getInstance().loadImage(imagePath, imageView, rtImageHeight);
        allLayout.addView(imageLayout, index);
    }

    /**
     * 根据view的宽度，动态缩放bitmap尺寸
     *
     * @param width view的宽度
     */
    public Bitmap getScaledBitmap(String filePath, int width) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        BitmapFactory.Options options = null;
        try {
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            int sampleSize = options.outWidth > width ? options.outWidth / width
                    + 1 : 1;
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeFile(filePath, options);
    }

    public int getRtImageHeight() {
        return rtImageHeight;
    }

    public void setRtImageHeight(int rtImageHeight) {
        this.rtImageHeight = rtImageHeight;
    }

    public int getRtImageBottom() {
        return rtImageBottom;
    }

    public void setRtImageBottom(int rtImageBottom) {
        this.rtImageBottom = rtImageBottom;
    }

    public String getRtTextInitHint() {
        return rtTextInitHint;
    }

    public void setRtTextInitHint(String rtTextInitHint) {
        this.rtTextInitHint = rtTextInitHint;
    }

    public int getRtTextSize() {
        return rtTextSize;
    }

    public void setRtTextSize(int rtTextSize) {
        this.rtTextSize = rtTextSize;
    }

    public int getRtTextColor() {
        return rtTextColor;
    }

    public void setRtTextColor(int rtTextColor) {
        this.rtTextColor = rtTextColor;
    }

    public int getRtTextLineSpace() {
        return rtTextLineSpace;
    }

    public void setRtTextLineSpace(int rtTextLineSpace) {
        this.rtTextLineSpace = rtTextLineSpace;
    }
}
