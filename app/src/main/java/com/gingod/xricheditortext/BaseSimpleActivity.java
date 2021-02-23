package com.gingod.xricheditortext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import butterknife.ButterKnife;

/**
 * BaseSimpleActivity
 *
 * @author
 */

public abstract class BaseSimpleActivity extends AppCompatActivity {
    public BaseSimpleActivity mActivity;
    public static String TAG = "BaseSimpleActivity: ";
    /**
     * 界面是否可见; 显示网络异常
     */
    public boolean isBaseStart = false, isBaseResume = false, isShowNetError = true;

    public Handler mBaseHandler = new Handler();
    public Gson mGson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mActivity = this;
            setContentView(getLayoutId());
            ButterKnife.bind(this);
            initValues();
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 页面布局
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化
     */
    protected abstract void initValues();

    /**
     * 初始化
     */
    protected void initData() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        isBaseStart = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isBaseStart = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBaseResume = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isBaseResume = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isBaseStart = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Activity的当前跳转
     */
    protected void openActivity(Class<?> cls) {
        openActivity(cls, null, false);
    }

    /**
     * Activity的当前跳转
     */
    protected void openActivity(Class<?> cls, boolean isActivityFinish) {
        openActivity(cls, null, isActivityFinish);
    }

    /**
     * Activity之间的跳转
     */
    protected void openActivity(Class<?> cls, Bundle bundle, boolean isActivityFinish) {
        try {
            Intent intent = new Intent(this, cls);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            startActivity(intent);
            if (isActivityFinish) {
                this.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Activity之间获取结果的跳转
     */
    protected void openActivityForResult(Class<?> cls, int requestCode) {
        openActivityForResult(cls, requestCode, null);
    }

    /**
     * Activity之间获取结果的跳转
     */
    protected void openActivityForResult(Class<?> cls, int requestCode, Bundle bundle) {
        try {
            Intent intent = new Intent(this, cls);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭软键盘
     */
    protected void closeKeyBoard() {
        try {
            View view = getWindow().peekDecorView();
            if (view != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收消息广播
     */
    private ChatMessageReceiver chatMessageReceiver;

    /**
     * 动态注册广播
     */
    protected void doRegisterReceiver() {
        try {
            chatMessageReceiver = new ChatMessageReceiver();
            IntentFilter filter = new IntentFilter("com.qn.millennium.content");
            registerReceiver(chatMessageReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消注册广播
     */
    protected void unregisterReceiver() {
        try {
            if (chatMessageReceiver != null) {
                unregisterReceiver(chatMessageReceiver);
                chatMessageReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收消息广播
     */
    private class ChatMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String message = intent.getStringExtra("message");
                handleMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理接收到的消息
     */
    protected void handleMessage(String message) {
    }

}
