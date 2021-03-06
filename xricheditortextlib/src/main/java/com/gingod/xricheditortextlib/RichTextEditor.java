package com.gingod.xricheditortextlib;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gingod.xricheditortextlib.bean.RichEditData;
import com.gingod.xricheditortextlib.bean.RichVideoViewBean;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑富文本
 *
 * @author
 */
public class RichTextEditor extends ScrollView {
    /**
     * 视频控件信息
     */
    private List<RichVideoViewBean> richVideoViewBeans = new ArrayList<>();
    /**
     * edittext常规padding是10dp
     */
    private static int EDIT_PADDING = 10;
    /**
     * 新生的view都会打一个tag，对每个view来说，这个tag是唯一的
     */
    private int viewTagIndex = 1;
    /**
     * 这个是所有子view的容器，scrollView内部的唯一一个ViewGroup
     */
    private LinearLayout allLayout;
    private LayoutInflater inflater;
    /**
     * 所有EditText的软键盘监听器
     */
    private OnKeyListener keyListener;
    /**
     * 所有EditText的焦点监听listener, 记录正在获取焦点状态的Edittext
     */
    private OnFocusChangeListener focusListener;
    /**
     * 最近被聚焦的EditText
     */
    private EditText lastFocusEdit;
    /**
     * 图片视频View添加或remove时，触发transition动画
     */
    private LayoutTransition mTransitioner;
    /**
     * 默认padding为0
     */
    private int editNormalPadding = 0;
    /**
     * 要删除的view的索引, 即tag
     */
    private int disappearingImageIndex = 0;
    /**
     * 关键词高亮
     */
    private String keywords;
    /**
     * 自定义属性, 插入的图片显示高度
     **/
    private int rtImageHeight = 500;
    /**
     * 两张相邻图片间距
     */
    private int rtImageBottom = 10;
    /**
     * 文字相关属性，初始提示信息，文字大小和颜色
     */
    private String rtTextInitHint = "输入文字";
    private int rtTextSize = 16;
    private int rtTextColor = Color.parseColor("#212121");
    private int rtTextLineSpace = 16;
    /**
     * 图片点击监听器(包含图片和删除)
     */
    private OnClickListener btnListener;
    /**
     * 删除图片的接口
     */
    private OnRtImageDeleteListener onRtImageDeleteListener;
    /**
     * 查看图片接口
     */
    private OnRtImageClickListener onRtImageClickListener;

    public RichTextEditor(Context context) {
        this(context, null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        try {
            inflater = LayoutInflater.from(context);
            EDIT_PADDING = dip2px(context, 10);

            //获取自定义属性
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RichTextEditor);
            rtImageHeight = ta.getDimensionPixelSize(R.styleable.RichTextEditor_rt_editor_image_height, dip2px(context, 500));
            rtImageBottom = ta.getDimensionPixelSize(R.styleable.RichTextEditor_rt_editor_image_bottom, dip2px(context, 10));
            rtTextSize = ta.getDimensionPixelSize(R.styleable.RichTextEditor_rt_editor_text_size, dip2px(context, 16));
            rtTextLineSpace = ta.getDimensionPixelSize(R.styleable.RichTextEditor_rt_editor_text_line_space, dip2px(context, 16));
            rtTextColor = ta.getColor(R.styleable.RichTextEditor_rt_editor_text_color, Color.parseColor("#212121"));
            rtTextInitHint = ta.getString(R.styleable.RichTextEditor_rt_editor_text_init_hint);
            ta.recycle();
            //默认输入提示文字
            if (TextUtils.isEmpty(rtTextInitHint)) {
                rtTextInitHint = "输入文字";
            }

            // 1. 初始化allLayout
            allLayout = new LinearLayout(context);
            allLayout.setOrientation(LinearLayout.VERTICAL);
            //载入删除动画, 并设置textview合并
            setupLayoutTransitions();
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            //设置间距，防止生成图片时文字太靠边，不能用margin，否则有黑边
            allLayout.setPadding(dip2px(context, 15), dip2px(context, 15), dip2px(context, 15), dip2px(context, 15));
            addView(allLayout, layoutParams);

            // 2. 初始化键盘退格监听
            // 主要用来处理点击回删按钮时，view的一些列合并操作
            keyListener = new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                        EditText edit = (EditText) v;
                        onBackspacePress(edit);
                    }
                    return false;
                }
            };

            // 图片点击监听
            btnListener = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    hideKeyBoard();
                    //点击图片
                    if (v instanceof DataImageView) {
                        DataImageView imageView = (DataImageView) v;
                        if (onRtImageClickListener != null) {
                            if (imageView.getImageData().type == RichEditData.IMAGE) {
                                onRtImageClickListener.onRtImageClick(imageView, imageView.getImageData(), imageView.getAbsolutePath());
                            } else if (imageView.getImageData().type == RichEditData.VIDEO) {
                                onRtImageClickListener.onRtVideoPlayClick(imageView, imageView.getImageData(), imageView.getAbsolutePath());
                            }
                        }
                        //点击删除
                    } else if (v instanceof ImageView) {
                        RelativeLayout parentView = (RelativeLayout) v.getParent();
                        onImageCloseClick(parentView);
                    }
                }
            };

            //焦点变化监听
            focusListener = new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        lastFocusEdit = (EditText) v;
                    }
                }
            };

            //添加第一个Edittext
            addFirstEditText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加第一个Edittext
     */
    private void addFirstEditText() {
        try {
            LinearLayout.LayoutParams firstEditParam = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            EditText firstEdit = createEditText(rtTextInitHint, EDIT_PADDING);
            allLayout.addView(firstEdit, firstEditParam);
            lastFocusEdit = firstEdit;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化transition动画
     */
    private void setupLayoutTransitions() {
        try {
            mTransitioner = new LayoutTransition();
            allLayout.setLayoutTransition(mTransitioner);
            mTransitioner.addTransitionListener(new LayoutTransition.TransitionListener() {

                @Override
                public void startTransition(LayoutTransition transition,
                                            ViewGroup container, View view, int transitionType) {

                }

                @Override
                public void endTransition(LayoutTransition transition,
                                          ViewGroup container, View view, int transitionType) {
                    try {
                        if (!transition.isRunning() && transitionType == LayoutTransition.CHANGE_DISAPPEARING) {
                            // transition动画结束，合并EditText
                            mergeEditText();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mTransitioner.setDuration(300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理软键盘backSpace回退事件
     *
     * @param editTxt 光标所在的文本输入框
     */
    private void onBackspacePress(EditText editTxt) {
        try {
            int startSelection = editTxt.getSelectionStart();
            // 只有在光标已经顶到文本输入框的最前方，在判定是否删除之前的图片，或两个View合并
            if (startSelection == 0) {
                int editIndex = allLayout.indexOfChild(editTxt);
                View preView = allLayout.getChildAt(editIndex - 1);
                // 如果editIndex-1<0,则返回的是null
                if (null != preView) {
                    if (preView instanceof RelativeLayout) {
                        // 光标EditText的上一个view对应的是图片, 则删除图片
                        onImageCloseClick(preView);
                    } else if (preView instanceof EditText) {
                        // 光标EditText的上一个view对应的还是文本框EditText
                        String str1 = editTxt.getText().toString();
                        EditText preEdit = (EditText) preView;
                        String str2 = preEdit.getText().toString();

                        // 合并文本view时，不需要transition动画
                        allLayout.setLayoutTransition(null);
                        allLayout.removeView(editTxt);
                        allLayout.setLayoutTransition(mTransitioner);

                        // 文本合并
                        preEdit.setText(String.valueOf(str2 + str1));
                        preEdit.requestFocus();
                        preEdit.setSelection(str2.length(), str2.length());
                        lastFocusEdit = preEdit;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理图片叉掉的点击事件
     *
     * @param view 整个image对应的relativeLayout view
     * @type 删除类型 0代表backspace删除 1代表按红叉按钮删除
     */
    private void onImageCloseClick(View view) {
        try {
            if (!mTransitioner.isRunning()) {
                disappearingImageIndex = allLayout.indexOfChild(view);
                DataImageView dataImageView = view.findViewById(R.id.edit_imageView);
                RichEditData.Data editData = dataImageView.getImageData();
                if (onRtImageDeleteListener != null) {
                    onRtImageDeleteListener.onRtImageDelete(editData);
                }
                allLayout.removeView(view);
                //合并上下EditText内容
                mergeEditText();
                //关闭视频时清空记录的信息
                for (int i = 0; i < richVideoViewBeans.size(); i++) {
                    RichVideoViewBean richVideoViewBean = richVideoViewBeans.get(i);
                    RichEditData.Data imageData1 = richVideoViewBean.edit_imageView.getImageData();
                    //视频控件已经存在, 更新信息
                    if (editData.localVideoPath.equals(imageData1.localVideoPath)) {
                        richVideoViewBeans.remove(i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片删除的时候，如果上下方都是EditText，则合并处理
     */
    private void mergeEditText() {
        try {
            View preView = allLayout.getChildAt(disappearingImageIndex - 1);
            View nextView = allLayout.getChildAt(disappearingImageIndex);
            if (preView instanceof EditText && nextView instanceof EditText) {
                EditText preEdit = (EditText) preView;
                EditText nextEdit = (EditText) nextView;
                String str1 = preEdit.getText().toString();
                String str2 = nextEdit.getText().toString();
                String mergeText = "";
                if (str2.length() > 0) {
                    mergeText = str1 + "\r\n" + str2;
                } else {
                    mergeText = str1;
                }

                allLayout.setLayoutTransition(null);
                allLayout.removeView(nextEdit);
                preEdit.setText(mergeText);
                preEdit.requestFocus();
                preEdit.setSelection(str1.length(), str1.length());
                allLayout.setLayoutTransition(mTransitioner);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成文本输入框
     */
    private EditText createEditText(String hint, int paddingTop) {
        EditText editText = (EditText) inflater.inflate(R.layout.rich_edittext, null);
        try {
            editText.setOnKeyListener(keyListener);
            editText.setTag(viewTagIndex++);
            editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop);
            editText.setHint(hint);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, rtTextSize);
            editText.setTextColor(rtTextColor);
            editText.setLineSpacing(rtTextLineSpace, 1.0f);
            editText.setOnFocusChangeListener(focusListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return editText;
    }

    /**
     * 生成图片View
     */
    private RelativeLayout createImageOrVideoLayout(RichEditData.Data imageData) {
        RelativeLayout layout = null;
        try {
            if (imageData.type == RichEditData.IMAGE) {
                layout = (RelativeLayout) inflater.inflate(R.layout.rich_imageview, null);
            }
            if (imageData.type == RichEditData.VIDEO) {
                layout = (RelativeLayout) inflater.inflate(R.layout.rich_viedoview, null);
            }
            layout.setTag(viewTagIndex++);
            View closeView = layout.findViewById(R.id.image_close);
            closeView.setTag(layout.getTag());
            closeView.setOnClickListener(btnListener);
            DataImageView imageView = layout.findViewById(R.id.edit_imageView);
            imageView.setOnClickListener(btnListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }

    /**
     * 插入一张图片或者视频
     */
    public void insertImageOrVideo(RichEditData.Data imageData) {
        try {
            //信息不存在
            if (imageData == null) {
                return;
            }
            if (imageData.type == RichEditData.IMAGE && TextUtils.isEmpty(imageData.imagePath) && TextUtils.isEmpty(imageData.localImagePath)) {
                return;
            }
            if (imageData.type == RichEditData.VIDEO && TextUtils.isEmpty(imageData.localVideoPath) && TextUtils.isEmpty(imageData.videoPath)) {
                return;
            }
            //查看是否为视频上传更新
            if (imageData.type == RichEditData.VIDEO) {
                for (int i = 0; i < richVideoViewBeans.size(); i++) {
                    RichVideoViewBean richVideoViewBean = richVideoViewBeans.get(i);
                    RichEditData.Data imageData1 = richVideoViewBean.edit_imageView.getImageData();
                    //视频控件已经存在, 更新信息
                    if (imageData.localVideoPath.equals(imageData1.localVideoPath)) {
                        //上传视频失败
                        if (imageData.videoProgress == RichEditData.UPLOAD_VIDEO_FAIL) {
                            richVideoViewBean.ll_video_upload.setVisibility(VISIBLE);
                            richVideoViewBean.tv_video_state.setText("上传失败!");
                            richVideoViewBean.tv_video_state.setTextColor(Color.RED);
                            richVideoViewBean.tv_video_progress.setText("");
                            richVideoViewBean.pb_progress_video.setProgress(0);
                            //上传视频完成
                        } else if (imageData.videoProgress == RichEditData.UPLOAD_VIDEO_SUCCESS) {
                            richVideoViewBean.ll_video_upload.setVisibility(GONE);
                            loadVideoPic(imageData, richVideoViewBean.edit_imageView);
                            //正在上传视频
                        } else {
                            richVideoViewBean.ll_video_upload.setVisibility(VISIBLE);
                            richVideoViewBean.tv_video_state.setText("上传中... ");
                            richVideoViewBean.tv_video_state.setTextColor(Color.WHITE);
                            richVideoViewBean.tv_video_progress.setText(imageData.videoProgressStr);
                            richVideoViewBean.pb_progress_video.setProgress(imageData.videoProgress);
                        }
                        richVideoViewBean.edit_imageView.setImageData(imageData);
                        return;
                    }
                }
            }
            insertView(imageData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入一张图片或者视频
     */
    private void insertView(RichEditData.Data imageData) {
        try {
            //lastFocusEdit获取焦点的EditText
            String lastEditStr = lastFocusEdit.getText().toString();
            //获取光标所在位置
            int cursorIndex = lastFocusEdit.getSelectionStart();
            //获取光标前面的字符串
            String editStr1 = lastEditStr.substring(0, cursorIndex).trim();
            //获取光标后的字符串
            String editStr2 = lastEditStr.substring(cursorIndex).trim();
            //获取焦点的EditText所在位置
            int lastEditIndex = allLayout.indexOfChild(lastFocusEdit);

            if (lastEditStr.length() == 0) {
                //如果当前获取焦点的EditText为空，直接在EditText下方插入图片，并且插入空的EditText
                addEditTextAtIndex(lastEditIndex + 1, "");
                //会将上个插入的Edittext挤下去
                addViewAtIndex(lastEditIndex + 1, imageData);
            } else if (editStr1.length() == 0) {
                //如果光标已经顶在了editText的最前面，则直接插入图片，并且EditText下移即可
                addViewAtIndex(lastEditIndex, imageData);
                //同时插入一个空的EditText，防止插入多张图片无法写文字
                addEditTextAtIndex(lastEditIndex + 1, "");
            } else if (editStr2.length() == 0) {
                // 如果光标已经顶在了editText的最末端，则需要添加新的imageView和EditText
                addEditTextAtIndex(lastEditIndex + 1, "");
                addViewAtIndex(lastEditIndex + 1, imageData);
            } else {
                //如果光标已经顶在了editText的最中间，则需要分割字符串，分割成两个EditText，并在两个EditText中间插入图片
                //把光标前面的字符串保留，设置给当前获得焦点的EditText（此为分割出来的第一个EditText）
                lastFocusEdit.setText(editStr1);
                //把光标后面的字符串放在新创建的EditText中（此为分割出来的第二个EditText）
                addEditTextAtIndex(lastEditIndex + 1, editStr2);
                //在第二个EditText的位置插入一个空的EditText，以便连续插入多张图片时，有空间写文字，第二个EditText下移
                addEditTextAtIndex(lastEditIndex + 1, "");
                //在空的EditText的位置插入图片布局，空的EditText下移
                addViewAtIndex(lastEditIndex + 1, imageData);
            }
            hideKeyBoard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关键字高亮显示
     *
     * @param target 需要高亮的关键字
     * @param text   需要显示的文字
     * @return spannable 处理完后的结果，记得不要toString()，否则没有效果
     */
    private static SpannableStringBuilder highlight(String text, String target) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        CharacterStyle span;
        try {
            Pattern p = Pattern.compile(target);
            Matcher m = p.matcher(text);
            while (m.find()) {
                // 需要重复！
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
    private void addEditTextAtIndex(final int index, CharSequence editStr) {
        try {
            EditText editText2 = createEditText("输入文字", EDIT_PADDING);
            //搜索关键词高亮
            if (!TextUtils.isEmpty(keywords)) {
                SpannableStringBuilder textStr = highlight(editStr.toString(), keywords);
                editText2.setText(textStr);
                //判断插入的字符串是否为空，如果没有内容则显示hint提示信息
            } else if (!TextUtils.isEmpty(editStr)) {
                editText2.setText(editStr);
            }
            editText2.setOnFocusChangeListener(focusListener);

            // 请注意此处，EditText添加、或删除不触动Transition动画
            allLayout.setLayoutTransition(null);
            allLayout.addView(editText2, index);
            // remove之后恢复transition动画
            allLayout.setLayoutTransition(mTransitioner);
            //插入新的EditText之后，修改lastFocusEdit的指向
            lastFocusEdit = editText2;
            lastFocusEdit.requestFocus();
            lastFocusEdit.setSelection(editStr.length(), editStr.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在特定位置添加View
     */
    private void addViewAtIndex(final int index, final RichEditData.Data editData) {
        try {
            if (RichEditData.IMAGE == editData.type) {
                addImageViewAtIndex(index, editData);
            } else if (RichEditData.VIDEO == editData.type) {
                addVideoViewAtIndex(index, editData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在特定位置添加video
     */
    private void addVideoViewAtIndex(final int index, final RichEditData.Data imageData) {
        try {
            final RelativeLayout imageLayout = createImageOrVideoLayout(imageData);
            DataImageView imageView = imageLayout.findViewById(R.id.edit_imageView);
            ImageView imageViewPlay = imageLayout.findViewById(R.id.video_play);
            imageViewPlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRtImageClickListener != null) {
                        onRtImageClickListener.onRtVideoPlayClick(imageView, imageView.getImageData(), imageView.getAbsolutePath());
                    }
                }
            });

            loadVideoPic(imageData, imageView);
            imageView.setImageData(imageData);
            allLayout.addView(imageLayout, index);

            //记录视频控件信息
            storeVideoView(imageLayout, imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 记录视频控件信息
     */
    private void storeVideoView(RelativeLayout imageLayout, DataImageView imageView) {
        try {
            TextView tv_video_state = imageLayout.findViewById(R.id.tv_video_state);
            TextView tv_video_progress = imageLayout.findViewById(R.id.tv_video_progress);
            ProgressBar pb_progress_video = imageLayout.findViewById(R.id.pb_progress_video);
            View ll_video_upload = imageLayout.findViewById(R.id.ll_video_upload);
            RichVideoViewBean richVideoViewBean = new RichVideoViewBean();
            richVideoViewBean.edit_imageView = imageView;
            richVideoViewBean.tv_video_state = tv_video_state;
            richVideoViewBean.tv_video_progress = tv_video_progress;
            richVideoViewBean.pb_progress_video = pb_progress_video;
            richVideoViewBean.ll_video_upload = ll_video_upload;
            richVideoViewBeans.add(richVideoViewBean);

            RichEditData.Data imageData = imageView.getImageData();
            //上传视频失败
            if (imageData.videoProgress == RichEditData.UPLOAD_VIDEO_FAIL) {
                richVideoViewBean.ll_video_upload.setVisibility(VISIBLE);
                richVideoViewBean.tv_video_state.setText("上传失败!");
                richVideoViewBean.tv_video_state.setTextColor(Color.RED);
                richVideoViewBean.tv_video_progress.setText("");
                richVideoViewBean.pb_progress_video.setProgress(0);
                //上传视频完成
            } else if (imageData.videoProgress == RichEditData.UPLOAD_VIDEO_SUCCESS) {
                richVideoViewBean.ll_video_upload.setVisibility(GONE);
                loadVideoPic(imageData, richVideoViewBean.edit_imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载视频图片
     */
    private void loadVideoPic(RichEditData.Data imageData, DataImageView imageView) {
        try {
            //视频封面
            String imagePath = imageData.videoPicPath;
            //本地视频地址
            if (TextUtils.isEmpty(imagePath)) {
                imagePath = imageData.localVideoPath;
            }
            //网络视频地址
            if (TextUtils.isEmpty(imagePath)) {
                imagePath = imageData.videoPath;
            }
            imageView.setAbsolutePath(imagePath);
            RichTextUtils.getInstance().loadImage(imageData, imagePath, imageView, rtImageHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在特定位置添加ImageView
     */
    private void addImageViewAtIndex(final int index, final RichEditData.Data imageData) {
        try {
            final RelativeLayout imageLayout = createImageOrVideoLayout(imageData);
            DataImageView imageView = imageLayout.findViewById(R.id.edit_imageView);

            String imagePath = imageData.imagePath;
            if (TextUtils.isEmpty(imagePath)) {
                imagePath = imageData.localImagePath;
            }
            imageView.setAbsolutePath(imagePath);
            imageView.setImageData(imageData);
            RichTextUtils.getInstance().loadImage(imageData, imagePath, imageView, rtImageHeight);

//			// 调整imageView的高度，根据宽度等比获得高度
//			int imageHeight ; //解决连续加载多张图片导致后续图片都跟第一张高度相同的问题
//			if (rtImageHeight > 0) {
//				imageHeight = rtImageHeight;
//			} else {
//				Bitmap bmp = BitmapFactory.decodeFile(imagePath);
//				int layoutWidth = allLayout.getWidth() - allLayout.getPaddingLeft() - allLayout.getPaddingRight();
//				imageHeight = layoutWidth * bmp.getHeight() / bmp.getWidth();
//				//imageHeight = allLayout.getWidth() * bmp.getHeight() / bmp.getWidth();
//			}
//			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//					LayoutParams.MATCH_PARENT, imageHeight);//固定图片高度，记得设置裁剪剧中
//			lp.bottomMargin = rtImageBottom;
//			imageView.setLayoutParams(lp);
//
//			if (rtImageHeight > 0){
//				XRichText.getInstance().loadImage(imagePath, imageView, true);
//			} else {
//				XRichText.getInstance().loadImage(imagePath, imageView, false);
//			}

            // onActivityResult无法触发动画，此处post处理
            allLayout.addView(imageLayout, index);
//			allLayout.postDelayed(new Runnable() {
//				@Override
//				public void run() {
//					allLayout.addView(imageLayout, index);
//				}
//			}, 200);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 对外提供的接口, 生成编辑数据上传
     */
    public List<RichEditData.Data> getEditData() {
        List<RichEditData.Data> dataList = new ArrayList<RichEditData.Data>();
        try {
            int num = allLayout.getChildCount();
            for (int index = 0; index < num; index++) {
                View itemView = allLayout.getChildAt(index);
                RichEditData.Data itemData = null;
                if (itemView instanceof EditText) {
                    EditText item = (EditText) itemView;
                    String inputStr = item.getText().toString();
                    if (!TextUtils.isEmpty(inputStr)) {
                        itemData = new RichEditData.Data();
                        itemData.type = RichEditData.TEXT;
                        itemData.inputStr = inputStr;
                    }
                } else if (itemView instanceof RelativeLayout) {
                    DataImageView item = (DataImageView) itemView.findViewById(R.id.edit_imageView);
                    itemData = item.getImageData();
                }
                if (itemData != null) {
                    dataList.add(itemData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataList;
    }

    /**
     * 将数据展示
     */
    public void setEditData(List<RichEditData.Data> editData) {
        try {
            for (int i = 0; i < editData.size(); i++) {
                RichEditData.Data data = editData.get(i);
                //图片
                if (data.type == RichEditData.IMAGE || data.type == RichEditData.VIDEO) {
                    insertImageOrVideo(data);
                    //文字
                } else if (data.type == RichEditData.TEXT) {
                    //最后一个为EditText且内容为空, 则直接设置内容
                    View childAt = allLayout.getChildAt(allLayout.getChildCount() - 1);
                    if (childAt instanceof EditText) {
                        EditText view = (EditText) childAt;
                        String text = view.getText().toString();
                        if (TextUtils.isEmpty(text)) {
                            view.setText(data.inputStr);
                            lastFocusEdit = view;
                            lastFocusEdit.requestFocus();
                            lastFocusEdit.setSelection(data.inputStr.length(), data.inputStr.length());
                            continue;
                        }
                    }
                    //新插入一个TextView
                    addEditTextAtIndex(getLastIndex(), data.inputStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyBoard() {
        try {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && lastFocusEdit != null) {
                imm.hideSoftInputFromWindow(lastFocusEdit.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * 清空所有布局
     */
    public void clearAllLayout() {
        try {
            allLayout.removeAllViews();
            addFirstEditText();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取最后一个view位置
     */
    private int getLastIndex() {
        int childCount = allLayout.getChildCount();
        return childCount;
    }

    /**
     * 图片删除监听
     */
    public interface OnRtImageDeleteListener {
        /**
         * 图片删除
         *
         * @param imageData
         */
        void onRtImageDelete(RichEditData.Data imageData);
    }

    /**
     * 设置图片删除监听
     */
    public void setOnRtImageDeleteListener(OnRtImageDeleteListener onRtImageDeleteListener) {
        this.onRtImageDeleteListener = onRtImageDeleteListener;
    }

    /**
     * 图片点击监听
     */
    public interface OnRtImageClickListener {
        /**
         * 图片点击查看
         *
         * @param view
         * @param imageData
         * @param imagePath
         */
        void onRtImageClick(View view, RichEditData.Data imageData, String imagePath);

        /**
         * 视频播放点击
         *
         * @param view
         * @param imageData
         * @param imagePath
         */
        void onRtVideoPlayClick(View view, RichEditData.Data imageData, String imagePath);
    }

    /**
     * 设置图片点击监听
     */
    public void setOnRtImageClickListener(OnRtImageClickListener onRtImageClickListener) {
        this.onRtImageClickListener = onRtImageClickListener;
    }

    private float startY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            switch (ev.getAction()) {
                //记录初始值
                case MotionEvent.ACTION_DOWN:
                    startY = ev.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    //有滑动, 则关闭软键盘
                    float endY = Math.abs(ev.getRawY() - startY);
                    if (endY > 52) {
                        hideKeyBoard();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.dispatchTouchEvent(ev);
    }
}
