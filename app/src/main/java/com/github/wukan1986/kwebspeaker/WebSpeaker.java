/*
Copyright 2018 wukan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.wukan1986.kwebspeaker;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

enum ESpeak {
    eNextSpeak,
    eStopSpeak,
}

public class WebSpeaker implements TextToSpeech.OnInitListener {
    // 调试模式
    // 为了调试Js的方便，将从外部存储加载Js脚本，调试完后要改回来
    boolean mDebug = true;
    String mCmd;
    String mLog;
    String mTtsLang;
    String mTextAll;
    String mStr;
    String mSkipLetter;

    int mMaxTextLen = 500;
    int mReadPhrase = 0;
    int mSpeakFlag = 0; // 末尾时为1，其它时使用为0，用于到最后时停止
    boolean mJoinPhrase = false;

    WebView mWebView;
    Handler mHandler;

    TextToSpeech mTts;
    HashMap<String, String> myHashAlarm = new HashMap();

    ESpeak mSpeak;

    static String UTTERANCE_ID = "wukan1986";
    static String jsScript;
    public static String jsInterface = "bell_pepper_info";
    public static String jsFile = "bell_pepper_info.js";

    public WebSpeaker(WebView webView) {
        this.mStr = "";
        this.myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);
        this.mSkipLetter = "|[]";

        this.mWebView = webView;
        this.mHandler = new Handler();
    }

    private String getScript() {
        // /storage/emulated/0/Android/data/com.github.wukan1986.kwebspeaker/cache
        String pathString = mWebView.getContext().getExternalCacheDir() + "/" + jsFile;
        File f = new File(pathString);
        InputStream is;
        try {
            if (f.exists()) {
                // 想更新生效，刷新网页即可
                is = new FileInputStream(pathString);
            } else {
                if (jsScript != null)
                    return jsScript;
                is = this.mWebView.getContext().getAssets().open(jsFile);
            }
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer); // 需要看一下是否有BOM头
            is.close();
            // 用"javascript: ;"的方式忽略BOM头的处理，防止开发人员再编辑js文件时由于没有注意格式导致加载失败
            // 其中代码注释需要使用/**/，不能使用//
            jsScript = "javascript:" + new String(buffer);
            return jsScript;
        } catch (Exception ex) {

        }

        return jsScript;
    }

    private void DoCmd(String paramString) {
        String url = "javascript:(typeof(gBellPepperInfo) == 'undefined' ? bell_pepper_info.InitRun('gBellPepperInfo." + paramString + "') : gBellPepperInfo." + paramString + " )";
        // 是否需要在这个地方对javascript做一下混淆？防止出现安全问题
        this.mWebView.loadUrl(url);
    }

    @JavascriptInterface
    public void InitRun(String paramString) {
        // 必须设成public，不加修饰时，调试正常，但发布后，就发现脚本无法调用
        this.mCmd = paramString;
        this.mHandler.post(new Runnable() {
            public void run() {
                mWebView.loadUrl(getScript() + mCmd);
            }
        });
    }

    @JavascriptInterface
    public void Log(String paramString) {
        // 由网页调用来记录日志，这里先屏蔽
        this.mLog = ("\r\n" + paramString);
        this.mHandler.post(new Runnable() {
            public void run() {
                if (mLog != "") {
                    //Print(mLog);
                    mLog = "";
                }
            }
        });
    }

    @JavascriptInterface
    public String TtsLang() {
        return this.mTtsLang;
    }

    @JavascriptInterface
    public void FileSaveAs(String paramString) {
        this.mTextAll = paramString;
//        this.mHandler.post(new Runnable() {
//            public void run() {
//                showDialog(10);
//            }
//        });
    }

    @JavascriptInterface
    public int MaxTextLen() {
        return this.mMaxTextLen;
    }

    @JavascriptInterface
    public int ReadPhrase() {
        return this.mReadPhrase;
    }

    @JavascriptInterface
    public void Speak(int paramInt, String paramString) {
        for (int i = 0; i < mSkipLetter.length(); ++i) {
            paramString = paramString.replace(this.mSkipLetter.charAt(i), ' ');
        }
        this.mSpeakFlag = paramInt;
        this.mStr = paramString;
        this.mHandler.post(new Runnable() {
            public void run() {
                if (mTts != null) {
                    if (mTts.isSpeaking()) {
                        mTts.stop();
                    }
                    mTts.speak(mStr, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                }
                mStr = "";
            }
        });
    }

    @JavascriptInterface
    public int SplitSent(String paramString) {
        // 有点像是遇到长文字时，根据句号进行断句，此功能一般用不到
        int m = paramString.length();
        int j = 0;
        int i = 0;
//        for (; ; ) {
//            int k = paramString.indexOf('。', j);
//            i = k;
//            if (k == -1) {
//                i = paramString.indexOf('.', j);
//            }
//            if (i == -1) {
//                k = -1;
//            }
//            label54:
//            label61:
//            do {
//                do {
//                    return k;
//                    j = i + 1;
//                    if (j < m) {
//                        break;
//                    }
//                    k = j;
//                } while (j != i + 1);
//                k = j;
//            } while (paramString.charAt(i) == '。');
//            for (; ; ) {
//                if (i + 1 >= m) {
//                }
//                do {
//                    if ((i <= 0) || (i + 1 >= m) || (!isDigit(paramString.charAt(i - 1))) || (!isDigit(paramString.charAt(i + 1)))) {
//                        break label206;
//                    }
//                    j = i + 1;
//                    break;
//                    k = paramString.charAt(j);
//                    if ((k != 34) && (k != 39)) {
//                        break label61;
//                    }
//                    j += 1;
//                    break label54;
//                    j = paramString.indexOf('.', i + 1);
//                } while ((j == -1) || (i + 5 < j));
//                i = j;
//            }
//            label206:
//            if (i - 2 < 0) {
//                break label262;
//            }
//            j = paramString.charAt(i - 2);
//            char c = paramString.charAt(i - 1);
//            if (((j != 46) && (j != 32)) || (!Character.isLetter(c))) {
//                break;
//            }
//            j = i + 1;
//        }
//        return i + 1;
//        label262:
        return i + 1;
    }

    public void InitTts(float speed, float pitch) {
        if (this.mTts == null) {
            this.mTts = new TextToSpeech(mWebView.getContext(), this);
            this.mTts.setOnUtteranceProgressListener(new TtsUtteranceListener());

            SetSpeed(speed);
            SetPitch(pitch);
        }
    }

    public void onInit(int var1) {
        if (this.mTts == null) {
            return;
        }
        if (this.mTts.isSpeaking()) {
            this.mTts.stop();
        }
    }

    private void StopSpeak() {
        this.mSpeak = ESpeak.eStopSpeak;
        if (this.mTts != null) {
            this.mTts.stop();
        }
    }

    public void StopTts() {
        if (this.mTts == null) {
            return;
        }
        try {
            this.mTts.stop();
            this.mTts.shutdown();
            this.mTts = null;
            return;
        } catch (Exception localException) {
        }
    }

    public void StartPage() {
        // 在页面加载完后再调用，这里做到
        StopSpeak();
        DoCmd("StartPage()");
    }

    public void Pause() {
        StopSpeak();
        DoCmd("StopTimer()");
    }

    public void Play() {
        this.mSpeak = ESpeak.eNextSpeak;
        DoCmd("SpeakCur()");
    }

    public void SetSpeed(float speed) {
        this.mTts.setSpeechRate(speed);
    }

    public void SetPitch(float pitch) {
        this.mTts.setPitch(pitch);
    }

    private class TtsUtteranceListener extends UtteranceProgressListener {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {
            if (!utteranceId.equals(UTTERANCE_ID)) {
                return;
            }
            mHandler.post(new Runnable() {
                public void run() {
                    if (mSpeakFlag == 1) {
                        Pause();
                        return;
                    }
                    DoCmd("SpeakNext()");
                }
            });
        }

        @Override
        public void onError(String utteranceId) {

        }
    }
}
