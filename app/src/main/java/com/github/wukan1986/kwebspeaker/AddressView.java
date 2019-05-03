package com.github.wukan1986.kwebspeaker;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;


public class AddressView extends LinearLayout {
    public static final String TAG = "AddressView";

    private final Activity mContext;
    WebView mWebView;
    EditText mAddress;
    Button mTxt;
    Button mUrl;
    Button mPaste;
    private ClipboardManager mClipboard = null;
    private AlertDialog dialog = null;

    public AddressView(Activity context) {
        super(context);
        mContext = context;
        initView();
    }

    public void Set(String txt, AlertDialog dialog) {
        mAddress.setText(txt);
        this.dialog = dialog;
    }

    private void initView() {
        mContext.getLayoutInflater().inflate(R.layout.address_view, this);
        mAddress = findViewById(R.id.address_edit);
        mPaste = findViewById(R.id.button_paste);
        mTxt = findViewById(R.id.button_txt);
        mUrl = findViewById(R.id.button_url);

        if (null == mClipboard) {
            mClipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        }

        mAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mAddress.setImeOptions(EditorInfo.IME_ACTION_NONE);

        mPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mClipboard.hasPrimaryClip()) {
                    String resultString = "";
                    ClipData clipData = mClipboard.getPrimaryClip();
                    int count = clipData.getItemCount();
                    for (int i = 0; i < count; ++i) {

                        ClipData.Item item = clipData.getItemAt(i);
                        CharSequence str = item
                                .coerceToText(mContext);
                        resultString += str;
                    }
                    mAddress.setText(resultString);
                }
            }
        });

        mTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = mAddress.getText().toString().trim();
                // 使用段落来分段
                txt = txt.replace("\n", "</p><p>");
                // 在Boox上显示乱码
                txt = "<html><meta charset='UTF-8'><p>" + txt + "</p></html>";
                // mWebView.loadData(txt, "text/html", "utf-8");
                // 只有换种方法才可以
                mWebView.loadData(txt, "text/html; charset=UTF-8", null);
                dialog.dismiss();
            }
        });

        mUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = mAddress.getText().toString().trim();
                if (txt.indexOf("://") > 0 && txt.indexOf("://") < 20) {
                    // 比较短，应当是出现了协议
                } else {
                    txt = "http://" + txt;
                }
                mWebView.loadUrl(txt);
                dialog.dismiss();
            }
        });

    }
}
