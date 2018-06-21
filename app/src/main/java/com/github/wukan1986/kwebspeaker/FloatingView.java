package com.github.wukan1986.kwebspeaker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FloatingView extends LinearLayout {
    public static final String TAG = "FloatingView";

    private final Activity mContext;
    private final WindowManager mWindowManager;
    private ImageButton mPlay;
    private ImageButton mPause;
    private ImageButton mSettings;
    WebSpeaker mWebSpeaker;

    public FloatingView(Activity context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        mContext.getLayoutInflater().inflate(R.layout.floating_view, this);
        mPlay =  findViewById(R.id.play);
        mPause = findViewById(R.id.pause);
        mSettings = findViewById(R.id.settings);

        mPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebSpeaker.Play();
            }
        });
        mPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebSpeaker.Pause();
            }
        });
        mSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeakerView view  = new SpeakerView(mContext);
                view.mWebSpeaker = mWebSpeaker;
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.speed)//设置对话框的标题
                        .setView(view)
                        .create();
                dialog.show();
            }
        });
    }

    Point preP, curP;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int) event.getRawX(), (int) event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int) event.getRawX(), (int) event.getRawY());
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                layoutParams.x += dx;
                layoutParams.y += dy;
                mWindowManager.updateViewLayout(this, layoutParams);

                preP = curP;
                break;
        }

        return false;
    }
}
