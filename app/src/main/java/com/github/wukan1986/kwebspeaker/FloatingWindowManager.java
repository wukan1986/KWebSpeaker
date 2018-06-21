package com.github.wukan1986.kwebspeaker;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class FloatingWindowManager {
    private Context mContext;
    private WindowManager mWindowManager;

    public FloatingWindowManager(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    private static final WindowManager.LayoutParams LAYOUT_PARAMS;

    static {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 0;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.LEFT | Gravity.CENTER;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        LAYOUT_PARAMS = params;
    }

    public void addView(View view) {
        if (view != null) {

            view.setLayoutParams(LAYOUT_PARAMS);

            mWindowManager.addView(view, LAYOUT_PARAMS);
        }
    }

    public void removeView(View view) {
        if (view != null) {
            mWindowManager.removeView(view);
            view = null;
        }
    }
}
