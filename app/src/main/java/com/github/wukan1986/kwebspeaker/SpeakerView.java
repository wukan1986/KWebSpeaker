package com.github.wukan1986.kwebspeaker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SpeakerView extends LinearLayout implements SeekBar.OnSeekBarChangeListener {
    public static final String TAG = "SpeakerView";
    public static final String Preferences_KEY_Speed = "setSpeedRate";
    public static final String Preferences_KEY_Pitch = "setPitch";

    private final Activity mContext;
    private SeekBar mSpeedSeekBar;
    private SeekBar mPitchSeekBar;
    private TextView mSpeedTxt;
    private TextView mPitchTxt;
    WebSpeaker mWebSpeaker;
    float mSpeed;
    float mPitch;

    public SpeakerView(Activity context) {
        super(context);
        mContext = context;
        this.mSpeed = mContext.getPreferences(Context.MODE_PRIVATE).getFloat(Preferences_KEY_Speed, 1.5f);
        this.mPitch = mContext.getPreferences(Context.MODE_PRIVATE).getFloat(Preferences_KEY_Pitch, 1.0f);
        initView();
    }

    private void initView() {
        mContext.getLayoutInflater().inflate(R.layout.speaker_view, this);
        mSpeedSeekBar = findViewById(R.id.speed_seekbar);
        mPitchSeekBar = findViewById(R.id.pitch_seekbar);
        mSpeedTxt = findViewById(R.id.speed_txt);
        mPitchTxt = findViewById(R.id.pitch_txt);
        mSpeedSeekBar.setMax(20);
        mPitchSeekBar.setMax(20);
        this.mSpeedSeekBar.setOnSeekBarChangeListener(this);
        this.mPitchSeekBar.setOnSeekBarChangeListener(this);
        int pos = float_2_int(this.mSpeed);
        this.mSpeedSeekBar.setProgress(pos);
        pos = float_2_int(this.mPitch);
        this.mPitchSeekBar.setProgress(pos);

//        mGoto = findViewById(R.id.goto_settings);
//        mGoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    //Open Android Text-To-Speech Settings
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                        Intent intent = new Intent();
//                        intent.setAction("com.android.settings.TTS_SETTINGS");
//                        Intent intent1 = intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(intent);
//                    } else {
//                        Intent intent = new Intent();
//                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(intent);
//                    }
//                } catch (Exception ex) {
//                    Toast.makeText(mContext, R.string.goto_settings_error, Toast.LENGTH_LONG).show();
//                }
//            }
//        });
    }

    /**
     * 拖动条停止拖动的时候调用
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SharedPreferences.Editor paramSeekBar = mContext.getPreferences(Context.MODE_PRIVATE).edit();
        switch (seekBar.getId()) {
            case R.id.speed_seekbar:
                paramSeekBar.putFloat(Preferences_KEY_Speed, this.mSpeed);
                paramSeekBar.commit();
                this.mWebSpeaker.SetSpeed(this.mSpeed);
                break;
            case R.id.pitch_seekbar:
                paramSeekBar.putFloat(Preferences_KEY_Pitch, this.mPitch);
                paramSeekBar.commit();
                this.mWebSpeaker.SetPitch(this.mPitch);
                break;
        }
    }

    /**
     * 拖动条开始拖动的时候调用
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    /**
     * 拖动条进度改变的时候调用
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.speed_seekbar:
                this.mSpeed = int_2_float(progress);
                mSpeedTxt.setText("当前语速：" + this.mSpeed + "x");
                break;
            case R.id.pitch_seekbar:
                this.mPitch = int_2_float(progress);
                mPitchTxt.setText("当前音调：" + this.mPitch + "x");
                break;
        }
    }

    private int float_2_int(float f) {
        int i_min = 0;
        int i_max = 20;
        float f_min = 0.5f;
        float f_max = 2.5f;
        return (int) ((f - f_min) * (i_max - i_min) / (f_max - f_min));
    }

    private float int_2_float(int i) {
        int i_min = 0;
        int i_max = 20;
        float f_min = 0.5f;
        float f_max = 2.5f;
        return i * (f_max - f_min) / (i_max - i_min) + f_min;
    }
}
