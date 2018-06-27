package com.github.wukan1986.kwebspeaker;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    long exitTime = 0;
    WebView mWebView;
    String mAddress;
    WebSpeaker mWebSpeaker;
    FloatingWindowManager mFloatingWindowManager;
    FloatingView mFloatingView;
    private static String Preferences_KEY_setBlockNetworkImage = "setBlockNetworkImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mWebView = findViewById(R.id.webview);
        initWebView();

        mFloatingWindowManager = new FloatingWindowManager(this);
        mFloatingView = new FloatingView(this);
        mFloatingView.mWebSpeaker = mWebSpeaker;
        mFloatingWindowManager.addView(mFloatingView);

        Intent intent = getIntent();
        startIntent(intent, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        startIntent(intent, false);
    }

    private void startIntent(Intent intent, boolean bOnCreate) {
        String txt = "file:///android_asset/" + "index.html";
        switch (intent.getAction()) {
            case Intent.ACTION_VIEW:
                txt = intent.getData().toString();
                break;
            case Intent.ACTION_SEND:
                txt = intent.getStringExtra(Intent.EXTRA_TEXT);
                break;
            case Intent.ACTION_SEND_MULTIPLE:
                txt = intent.getStringExtra(Intent.EXTRA_TEXT);
                break;
            case Intent.ACTION_MAIN:
                // 解决重新进入时网页新刷的问题
                if (!bOnCreate)
                    return;
                mWebView.loadUrl(txt);
                return;
        }
        ShowAddressDialog(txt);
    }

    protected void onDestroy() {
        super.onDestroy();
        mWebSpeaker.StopTts();
    }

    private void ShowAddressDialog(String txt) {
        final View view = getLayoutInflater().inflate(R.layout.address_view, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.url_or_txt)//设置对话框的标题
                .setView(view)
                .setPositiveButton(R.string.as_url, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText et = view.findViewById(R.id.address_edit);
                        mAddress = et.getText().toString().trim();
                        if (mAddress.indexOf("://") > 0 && mAddress.indexOf("://") < 20) {
                            // 比较短，应当是出现了协议
                        } else {
                            mAddress = "http://" + mAddress;
                        }
                        mWebView.loadUrl(mAddress);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.as_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText et = view.findViewById(R.id.address_edit);
                        mAddress = et.getText().toString().trim();
                        // 使用段落来分段
                        mAddress = mAddress.replace("\n", "</p><p>");
                        mAddress = "<p>" + mAddress + "</p>";
                        mWebView.loadData(mAddress, "text/html", "utf-8");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
        EditText et = dialog.findViewById(R.id.address_edit);
        et.setText(txt);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setImeOptions(EditorInfo.IME_ACTION_NONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem disable_img = menu.findItem(R.id.disable_img);
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        boolean bSetBlockNetworkImage = pref.getBoolean(Preferences_KEY_setBlockNetworkImage, false);
        disable_img.setChecked(bSetBlockNetworkImage);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.url_or_txt:
                ShowAddressDialog(mAddress);
                break;
            case R.id.disable_img:
                SharedPreferences.Editor pref = getPreferences(Context.MODE_PRIVATE).edit();
                pref.putBoolean(Preferences_KEY_setBlockNetworkImage, !item.isChecked());
                pref.commit();
                WebSettings ws = mWebView.getSettings();
                ws.setBlockNetworkImage(!item.isChecked());
                break;
            case R.id.review:
                startActivityForOtherApp("market://details?id=com.github.wukan1986.kwebspeaker");
                break;
            case R.id.recommend:
                String txt = getString(R.string.recommend_msg);
                // 从API11开始android推荐使用android.content.ClipboardManager
                // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText("share", txt);
                cm.setPrimaryClip(mClipData);
                Toast.makeText(this, "复制成功，可以发给朋友们了", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView() {
        //mProgressBar.setVisibility(View.VISIBLE);
        WebSettings ws = mWebView.getSettings();
        // 网页内容的宽度是否可大于WebView控件的宽度
        ws.setLoadWithOverviewMode(false);
        // 保存表单数据
        ws.setSaveFormData(true);
        // 是否应该支持使用其屏幕缩放控件和手势缩放
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        // 启动应用缓存
        ws.setAppCacheEnabled(true);
        // 设置缓存模式
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        // setDefaultZoom  api19被弃用
        // 设置此属性，可任意比例缩放。
        //ws.setUseWideViewPort(true);
        // 不缩放
        //webView.setInitialScale(100);
        // 告诉WebView启用JavaScript执行。默认的是false。
        ws.setJavaScriptEnabled(true);

        //  页面加载好以后，再放开图片
        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);
        boolean bSetBlockNetworkImage = pref.getBoolean(Preferences_KEY_setBlockNetworkImage, false);
        ws.setBlockNetworkImage(bSetBlockNetworkImage);

        // 使用localStorage则必须打开
        ws.setDomStorageEnabled(true);
        // 排版适应屏幕
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        // WebView是否新窗口打开(加了后可能打不开网页)
        // ws.setSupportMultipleWindows(true);

        // webview从5.0开始默认不允许混合模式,https中不能加载http资源,需要设置开启。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 在pc上调试webview
        // chrome://inspect/#devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 这个是否开启，在访问www.baidu.com上时效果不一样
            // 感觉把它当成了一个PC浏览器在处理了
            mWebView.setWebContentsDebuggingEnabled(true);
        }

        /** 设置字体默认缩放大小(改变网页字体大小,setTextSize  api14被弃用)*/
        //ws.setTextZoom(100);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView paramAnonymousWebView, int paramAnonymousInt) {
                setTitle("Loading...");
                //WebSpeakerActivityPro.this.mActivity.setProgress(paramAnonymousInt * 100);
                if (paramAnonymousInt == 100) {
                    setTitle(R.string.app_name);
                }
            }
        });

        mWebView.setWebViewClient(new HelloWebViewClient());
        this.mWebSpeaker = new WebSpeaker(this.mWebView);
        float speed = this.getPreferences(Context.MODE_PRIVATE).getFloat(SpeakerView.Preferences_KEY_Speed, 1.5f);
        this.mWebSpeaker.InitTts(speed);
        this.mWebView.addJavascriptInterface(this.mWebSpeaker, this.mWebSpeaker.jsInterface);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else if ((System.currentTimeMillis() - exitTime) > 2000)  //System.currentTimeMillis()无论何时调用，肯定大于2000
            {
                Toast.makeText(getApplicationContext(), R.string.exit_msg, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startActivityForOtherApp(String url) {
        try {
            Intent intent1 = new Intent();
            intent1.setAction(Intent.ACTION_VIEW);
            intent1.setData(Uri.parse(url));
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startActivityForSettings(String url) {
        try {
            Intent intent = new Intent();
            Uri uri = Uri.parse(url);
            intent.setAction(uri.getQueryParameter("action")); // "com.android.settings.TTS_SETTINGS"
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class HelloWebViewClient
            extends WebViewClient {
        private HelloWebViewClient() {
        }

        public void onPageFinished(WebView paramWebView, String paramString) {
            mAddress = paramString;
            setTitle(paramWebView.getTitle());
            MainActivity.this.mWebSpeaker.StartPage();
            super.onPageFinished(paramWebView, paramString);
        }

        public void onPageStarted(WebView paramWebView, String paramString, Bitmap paramBitmap) {

        }

        public boolean shouldOverrideUrlLoading(WebView paramWebView, String paramString) {
            if ((paramString != null) && (paramString.equals("about:blank"))) {
                return false;
            }
            if (paramString.startsWith("http://")
                    || paramString.startsWith("https://")
                    || paramString.startsWith("file://")) {
                paramWebView.loadUrl(paramString);
                return true;
            }
            if (paramString.startsWith("kwebspeaker://")) {
                startActivityForSettings(paramString);
            } else {
                startActivityForOtherApp(paramString);
            }

            return true;
        }
    }
}
