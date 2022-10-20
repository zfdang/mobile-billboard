package com.zfdang.mbb;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.warkiz.widget.IndicatorSeekBar;
import com.zfdang.MarqueeTextView;
import com.zfdang.mbb.databinding.SettingDialogBinding;
import com.zfdang.mbb.databinding.ActivityBillboardBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BillboardActivity extends AppCompatActivity {
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private MarqueeTextView mMarqueeTextView;

    private final Handler mHideHandler = new Handler(Looper.myLooper());

    private ActivityBillboardBinding activityBinding;

    private FloatingActionButton btSetting;
    private Setting mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityBinding = ActivityBillboardBinding.inflate(getLayoutInflater());
        setContentView(activityBinding.getRoot());

        mMarqueeTextView = activityBinding.marqueeTextview;
        btSetting = activityBinding.fabSetting;

        // show billboard in landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        // add floating button for config page
        btSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettingDialog();
            }
        });

        initSetting();
        updateMarqueeTextView();
    }

    private void initSetting() {
        mSetting = new Setting();
        mSetting.text = getString(R.string.defaut_marquee_text);
    }

    private void updateMarqueeTextView() {
        mMarqueeTextView.stop();
        mMarqueeTextView.setText(mSetting.text);
        mMarqueeTextView.setTextSize(mSetting.textSize);
        mMarqueeTextView.setSpeed(mSetting.speed);
        mMarqueeTextView.setTextColor(mSetting.textColor.toArgb());
        mMarqueeTextView.setBackgroundColor(mSetting.bgColor.toArgb());
        mMarqueeTextView.setRepeat(mSetting.loopMode);
        mMarqueeTextView.start();
    }

    private void setFullScreenMode(Window window) {
        //设置永不休眠模式
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏系统工具栏方式一
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        //隐藏底部导航栏
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                View decorView = getWindow().getDecorView();
                int uiState = decorView.getSystemUiVisibility();
                if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                    if (uiState != View.GONE) decorView.setSystemUiVisibility(View.GONE);
                } else if (Build.VERSION.SDK_INT >= 19) {
                    if (uiState != (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN))
                        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_FULLSCREEN);
                }
            }
        });
    }

    void openSettingDialog() {
        SettingDialogBinding dialogBinding = SettingDialogBinding.inflate(getLayoutInflater());

        // binding controls
        final EditText etText = dialogBinding.settingText;
        final RadioButton rbSingle = dialogBinding.radioButtonSingle;
        final RadioButton rbLoop = dialogBinding.radioButtonLoop;
        final RadioButton rbFillLoop = dialogBinding.radioButtonFillLoop;
        final IndicatorSeekBar sbTextSize = dialogBinding.seekBarTextSize;
        final IndicatorSeekBar sbSpeed = dialogBinding.seekBarSpeed;
        final EditText etTextColor = dialogBinding.settingTextColorValue;
        final EditText etBgColor = dialogBinding.settingBgColorValue;

        // show selected color value
        final ColorPickerView pickerTextColor = dialogBinding.colorPickerViewText;
        final ColorPickerView pickerBgColor = dialogBinding.colorPickerViewBg;
        pickerTextColor.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                etTextColor.setText(envelope.getHexCode());
            }
        });
        pickerBgColor.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                etBgColor.setText(envelope.getHexCode());
            }
        });

        // init dialog values from setting
        etText.setText(mSetting.text);
        rbSingle.setChecked(mSetting.loopMode == Setting.LM_SINGLE);
        rbLoop.setChecked(mSetting.loopMode == Setting.LM_LOOP);
        rbFillLoop.setChecked(mSetting.loopMode == Setting.LM_FILL_LOOP);
        sbTextSize.setProgress(mSetting.textSize);
        sbSpeed.setProgress(mSetting.speed);

        AlertDialog dialog = new AlertDialog.Builder(this) // 使用android.support.v7.app.AlertDialog
                .setView(dialogBinding.getRoot()) // 设置布局
                .setCancelable(false) // 设置点击空白处不关闭
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = etText.getText().toString();
                        if (TextUtils.isEmpty(text)) {
                            // 判断输入的内容是否为空
                            Toast.makeText(BillboardActivity.this, R.string.alert_content_empty, Toast.LENGTH_SHORT).show();
                        } else {
                            mMarqueeTextView.setText(text);
                        }

                        // enter full screen again
                        delayedSetFullScreen(100);
                    }
                }) // 设置确定按钮，并设置监听事件})
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // enter full screen again
                        delayedSetFullScreen(100);
                    }
                }) // 设置取消按钮，并设置监听事件
                .setNeutralButton(R.string.dialog_exit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }) // 设置退出对话框
                .create(); // 创建对话框

        final Window window = dialog.getWindow();
        setFullScreenMode(window);
        dialog.show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been created
        delayedSetFullScreen(100);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedSetFullScreen(int delayMillis) {
        // mHideRunnable --> mHidePart2Runnable
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            // Hide UI first
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
            // Schedule a runnable to remove the status and navigation bar after a delay
            mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
        }
    };

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mMarqueeTextView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mMarqueeTextView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };

}