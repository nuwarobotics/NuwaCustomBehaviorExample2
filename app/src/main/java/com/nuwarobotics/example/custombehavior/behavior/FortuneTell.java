package com.nuwarobotics.example.custombehavior.behavior;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.nuwarobotics.example.custombehavior.R;
import com.nuwarobotics.example.custombehavior.listener.EventListener;
import com.nuwarobotics.example.custombehavior.sdk.NuwaSDKManager;
import com.nuwarobotics.example.custombehavior.service.CustomService;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.SimpleGrammarData;
import com.nuwarobotics.service.agent.VoiceEventListener;
import com.nuwarobotics.service.agent.VoiceResultJsonParser;

import java.lang.ref.WeakReference;

public class FortuneTell implements Runnable {
    private final String TAG = this.getClass().getSimpleName();

    public static final int msgEventStart = 0;
    public static final int msgEventTTS1 = 1;
    public static final int msgEventTTS2 = 2;
    public static final int msgEventASR = 3;
    public static final int msgEventYesResponsed1 = 4;
    public static final int msgEventYesResponsed2 = 5;
    public static final int msgEventNoResponsed = 6;
    public static final int msgEventEnd = 7;

    private final static int EVENT_DELAY = 500; //ms

    private Context mContext;
    private WeakReference<Context> mServiceReference;
    private EventListener mEventListener;
    private NuwaRobotAPI mRobotAPI;

    private Handler mHandler;
    private HandlerThread mThread;
    private int mMsgState = -1;

    public FortuneTell() {

    }

    public FortuneTell(CustomService service, EventListener eventlistener) {
        mServiceReference = new WeakReference<>(service);
        mContext = mServiceReference.get();
        mEventListener = eventlistener;

        mRobotAPI = NuwaSDKManager.getInstance().getNuwaRobotAPI();

        initHandle();
    }

    @Override
    public void run() {
        //TODO
        mRobotAPI.registerVoiceEventListener(mVoiceEventListener);

        mHandler.sendEmptyMessageDelayed(msgEventStart, EVENT_DELAY);
        mMsgState = msgEventStart;
    }

    //NuwaSDK voiceEventListen implement
    VoiceEventListener mVoiceEventListener = new VoiceEventListener() {
        @Override
        public void onWakeup(boolean isError, String score, float direction) {

        }

        @Override
        public void onTTSComplete(boolean isError) {
            Log.d(TAG, "onTTSComplete:" + !isError);
            controlMouse(false);
            doNextAction(mMsgState, 0);
        }

        @Override
        public void onSpeechRecognizeComplete(boolean isError, ResultType iFlyResult, String json) {
            //Log.d(TAG, "onSpeechRecognizeComplete:" + !isError + ", json:" + json);
        }

        @Override
        public void onSpeech2TextComplete(boolean isError, String json) {
            //Log.d(TAG, "onSpeech2TextComplete:" + !isError + ", json:" + json);
        }

        @Override
        public void onMixUnderstandComplete(boolean isError, ResultType resultType, String json) {
            Log.d(TAG, "onMixUnderstandComplete isError:" + !isError + ", resultType=" + resultType + ", json:" + json);
            String ASRResult;
            if (!isError && (json != null && !json.isEmpty())) {
                ASRResult = VoiceResultJsonParser.parseVoiceResult(json);
                //TODO: Check state
                if (ASRResult != null && !ASRResult.isEmpty()) {
                    if (ASRResult.equals(mContext.getResources().getString(R.string.custom_asr_option1))) {
                        doNextAction(mMsgState, 1);
                    } else {
                        doNextAction(mMsgState, 0);
                    }
                } else {
                    doNextAction(mMsgState, 0);
                }
            } else {
                doNextAction(mMsgState, 0); //Not OK
            }
        }

        @Override
        public void onSpeechState(ListenType listenType, SpeechState speechState) {

        }

        @Override
        public void onSpeakState(SpeakType speakType, SpeakState speakState) {

        }

        @Override
        public void onGrammarState(boolean isError, String s) {
            //Create new thread to start localcmd
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //if(!isLocalCMD) {
                    {
                        mRobotAPI.startLocalCommand();//Start listen without wakeup, callback on onMixUnderstandComplete
                    }
                }
            });
        }

        @Override
        public void onListenVolumeChanged(ListenType listenType, int i) {

        }

        @Override
        public void onHotwordChange(HotwordState hotwordState, HotwordType hotwordType, String s) {

        }
    };

    private void initHandle() {
        //initial
        mThread = new HandlerThread("FortuneTell_Thread");
        mThread.start();

        mHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(final Message msg) {
                mMsgState = msg.what;
                switch (msg.what) {
                    case msgEventStart:
                        mRobotAPI.registerVoiceEventListener(mVoiceEventListener);
                        doNextAction(mMsgState, 0);
                        break;
                    case msgEventTTS1:
                        mRobotAPI.stopTTS();
                        mRobotAPI.startTTS(mContext.getResources().getString(R.string.fortune_tell_tts_notify));
                        controlMouse(true);
                        break;
                    case msgEventTTS2:
                        mRobotAPI.stopTTS();
                        mRobotAPI.startTTS(mContext.getResources().getString(R.string.fortune_tell_tts_check));
                        controlMouse(true);
                        break;
                    case msgEventASR:
                        mRobotAPI.stopListen();
                        String[] cmdList = mContext.getResources().getStringArray(R.array.example_cmd_options);
                        SimpleGrammarData grammardata = new SimpleGrammarData("ExtBahavior");
                        for (String string : cmdList) {
                            grammardata.addSlot(string);
                            Log.d(TAG, "add string : " + string);
                        }
                        //generate grammar data
                        grammardata.updateBody();
                        mRobotAPI.stopListen();
                        mRobotAPI.createGrammar(grammardata.grammar, grammardata.body);
                        break;
                    case msgEventYesResponsed1:
                        mRobotAPI.stopTTS();
                        mRobotAPI.startTTS(mContext.getResources().getString(R.string.fortune_tell_tts_query_yes_1));
                        controlMouse(true);
                        break;
                    case msgEventYesResponsed2:
                        mRobotAPI.stopTTS();
                        mRobotAPI.startTTS(mContext.getResources().getString(R.string.fortune_tell_tts_query_yes_2));
                        controlMouse(true);
                        mRobotAPI.motionPlay("666_EM_Happy03", false);
                        break;
                    case msgEventNoResponsed:
                        mRobotAPI.stopTTS();
                        mRobotAPI.startTTS(mContext.getResources().getString(R.string.fortune_tell_tts_query_not_1));
                        controlMouse(true);
                        break;
                    case msgEventEnd:
                        //Finish and callback to main service
                        mRobotAPI.registerVoiceEventListener(null);
                        if (mEventListener != null) {
                            mEventListener.onCompleted(getClass().getSimpleName(), "");
                        }
                        mEventListener = null;
                        break;
                }
            }
        };
    }

    private void doNextAction(int current, int param) {
        int nextaction = -1;

        switch (current) {
            case msgEventStart:
                nextaction = msgEventTTS1;
                break;
            case msgEventTTS1:
                nextaction = msgEventTTS2;
                break;
            case msgEventTTS2:
                nextaction = msgEventASR;
                break;
            case msgEventASR:
                if (param == 1)
                    nextaction = msgEventYesResponsed1;
                else
                    nextaction = msgEventNoResponsed;
                break;
            case msgEventYesResponsed1:
                nextaction = msgEventYesResponsed2;
                break;
            case msgEventYesResponsed2:
                nextaction = msgEventEnd;
                break;
            case msgEventNoResponsed:
                nextaction = msgEventEnd;
                break;
        }

        if (nextaction > -1) {
            Log.d(TAG, "doNextAction, next: " + nextaction);
            mHandler.sendEmptyMessageDelayed(nextaction, EVENT_DELAY);
        }
    }

    private boolean controlMouse(boolean on) {
        if (on)
            mRobotAPI.mouthOn(100);
        else
            mRobotAPI.mouthOff();

        return true;
    }
}
