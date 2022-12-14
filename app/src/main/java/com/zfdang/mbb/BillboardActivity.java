package com.zfdang.mbb;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.zfdang.MarqueeTextView;
import com.zfdang.mbb.databinding.SettingDialogBinding;
import com.zfdang.mbb.databinding.ActivityBillboardBinding;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BillboardActivity extends AppCompatActivity {
    private static final String TAG = "BillboardActivity";

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
        mMarqueeTextView.setTextColor(mSetting.textColor);
        mMarqueeTextView.setBackgroundColor(mSetting.bgColor);
        mMarqueeTextView.setRepeat(mSetting.loopMode);
        mMarqueeTextView.start();
    }

    private void setFullScreenMode(Window window) {
        //????????????????????????
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //??????????????????????????????
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        //?????????????????????
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
        final ColorPickerView pickerTextColor = dialogBinding.colorPickerViewText;
        final BrightnessSlideBar brightnessSlideBarText = dialogBinding.slidebarText;
        pickerTextColor.attachBrightnessSlider(brightnessSlideBarText);
        final EditText etTextColor = dialogBinding.settingTextColorValue;
        final ColorPickerView pickerBgColor = dialogBinding.colorPickerViewBg;
        final BrightnessSlideBar brightnessSlideBarBg = dialogBinding.slidebarBg;
        pickerBgColor.attachBrightnessSlider(brightnessSlideBarBg);
        final EditText etBgColor = dialogBinding.settingBgColorValue;

        // init dialog values from setting
        etText.setText(mSetting.text);
        rbSingle.setChecked(mSetting.loopMode == Setting.LM_SINGLE);
        rbLoop.setChecked(mSetting.loopMode == Setting.LM_LOOP);
        rbFillLoop.setChecked(mSetting.loopMode == Setting.LM_FILL_LOOP);
        sbTextSize.setProgress(mSetting.textSize);
        sbSpeed.setProgress(mSetting.speed);
        pickerTextColor.setInitialColor(mSetting.textColor);
        etTextColor.setText(pickerTextColor.getColorEnvelope().getHexCode());
        pickerBgColor.setInitialColor(mSetting.bgColor);
        etBgColor.setText(pickerBgColor.getColorEnvelope().getHexCode());

        // add color change listeners for color picker
        pickerTextColor.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                etTextColor.setText(envelope.getHexCode());
                mMarqueeTextView.setTextColor(envelope.getColor());
            }
        });
        pickerBgColor.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                etBgColor.setText(envelope.getHexCode());
                mMarqueeTextView.setBackgroundColor(envelope.getColor());
            }
        });

        // add listeners for seekbar
        sbTextSize.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mMarqueeTextView.setTextSize(seekBar.getProgress());
            }
        });
        sbSpeed.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mMarqueeTextView.setSpeed(seekBar.getProgress());
            }
        });

        // set ratio button listeners
        rbSingle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    mMarqueeTextView.setRepeat(Setting.LM_SINGLE);
                }
            }
        });
        rbLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    mMarqueeTextView.setRepeat(Setting.LM_LOOP);
                }

            }
        });
        rbFillLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    mMarqueeTextView.setRepeat(Setting.LM_FILL_LOOP);
                }
            }
        });

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mMarqueeTextView.setText(editable.toString());
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this) // ??????android.support.v7.app.AlertDialog
                .setView(dialogBinding.getRoot()) // ????????????
                .setCancelable(false) // ??????????????????????????????
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = etText.getText().toString();
                        if (TextUtils.isEmpty(text)) {
                            // ?????????????????????????????????
                            Toast.makeText(BillboardActivity.this, R.string.alert_content_empty, Toast.LENGTH_SHORT).show();
                        } else {
                            mSetting.text = text;
                        }

                        // save all settings
                        mSetting.speed = sbSpeed.getProgress();
                        mSetting.textSize = sbTextSize.getProgress();
                        mSetting.textColor = pickerTextColor.getColor();
                        mSetting.bgColor = pickerBgColor.getColor();
                        if(rbFillLoop.isChecked()) {
                            mSetting.loopMode = Setting.LM_FILL_LOOP;
                        } else if(rbLoop.isChecked()) {
                            mSetting.loopMode = Setting.LM_LOOP;
                        } else if(rbSingle.isChecked()) {
                            mSetting.loopMode = Setting.LM_SINGLE;
                        }

                        updateMarqueeTextView();

                        // enter full screen again
                        delayedSetFullScreen(100);
                    }
                }) // ??????????????????????????????????????????})
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // reset all values by mSetting
                        updateMarqueeTextView();

                        // enter full screen again
                        delayedSetFullScreen(100);
                    }
                }) // ??????????????????????????????????????????
                .setNeutralButton(R.string.dialog_exit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                }) // ?????????????????????
                .create(); // ???????????????

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