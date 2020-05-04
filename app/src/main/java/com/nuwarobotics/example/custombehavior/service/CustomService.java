package com.nuwarobotics.example.custombehavior.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nuwarobotics.example.custombehavior.R;
import com.nuwarobotics.example.custombehavior.listener.EventListener;
import com.nuwarobotics.example.custombehavior.behavior.FortuneTell;
import com.nuwarobotics.example.custombehavior.behavior.PictureView;
import com.nuwarobotics.example.custombehavior.sdk.NuwaSDKManager;
import com.nuwarobotics.service.agent.RobotEventListener;
import com.nuwarobotics.service.custombehavior.BaseBehaviorService;
import com.nuwarobotics.service.custombehavior.CustomBehavior;

import org.json.JSONObject;

/**
 * NuwaSDK provide BaseBehaviorService to receive TrainingKit notify
 */
public class CustomService extends BaseBehaviorService {
    private final String TAG = this.getClass().getSimpleName();

    private Context mContext;

    private HandlerThread mBehaviorThread;
    private Handler mBehaviorHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate.");

        mContext = this;

        //initial a handler to execute some implementation
        mBehaviorThread = new HandlerThread("CustomService.example");
        mBehaviorThread.start();
        mBehaviorHandler = new Handler(mBehaviorThread.getLooper());

        //Initial nuwa sdk
        NuwaSDKManager.getInstance().init((CustomService) mContext);
        NuwaSDKManager.getInstance().setRobotEventListener(robotEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand: intent=" + intent + " flags=" + flags + " startId=" + startId);

        SetForeground();//This function use to start this service as Foreground Service and keep alive.

        return START_STICKY;
    }

    /**
     * After Android 8, android not allow background service.
     * We have to start this service as a foreground service and keep service alive by NotificationChannel
     * https://developer.android.com/about/versions/oreo/android-8.0-changes#back-all
     */
    private void SetForeground() {
        String NOTIFICATION_CHANNEL_ID = getApplicationContext().getPackageName();
        String channelName = getClass().getSimpleName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel chan = new android.app.NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            chan.setShowBadge(false);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        } else {
            Log.e(TAG, "system below Oreo, no NotificationChannel");
            return;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("ExtService is running in background") // notification text
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(1, notification);
    }

    //Basic behavior
    @Override
    public void onInitialize() {
        try {
            Log.d(TAG, "onInitialize: regist welcome sentence");
            String[] welcome = new String[]{getResources().getString(R.string.custom_welcome_sentence_example)};
            //When Robot on "Auto Speak mode", Kebbi auto scan people when PIR changed.
            //If kebbi detect a face(known or unknown), kebbit will say "Welcome Sentence"
            mSystemBehaviorManager.setWelcomeSentence(welcome);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CustomBehavior createCustomBehavior() {
        return new CustomBehavior.Stub() {
            @Override
            public void onWelcome(String name, long faceid) {
                Log.d(TAG, "onWelcome: name=" + name + ", faceid=" + faceid);
            }

            @Override
            public void prepare(final String parameter) {
                Log.d(TAG, "prepare: " + parameter);

            }

            @Override
            public void process(final String parameter) {
                Log.d(TAG, "process: " + parameter);
                //Developer can implement customize Robot response here
                //Step 1 : parser JSON to get your config
                JSONObject obj = parse(parameter);
                String cmd = (obj != null) ? obj.optString("word", "") : "";

                if (cmd.equals("FortuneTell")) { //うらない
                    //If NLP hit "うらない", execute your implementation
                    //Thread not MUST, you can call any api directly
                    mBehaviorHandler.postDelayed(new FortuneTell((CustomService) mContext, mEventListener), 500);
                } else if (cmd.equals("PictureView")) { //写真見せて
                    //If NLP hit "写真見せて", execute your implementation
                    mBehaviorHandler.postDelayed(new PictureView((CustomService) mContext, mEventListener), 500);
                }
            }

            @Override
            public void finish(final String parameter) {
                Log.d(TAG, "finish: " + parameter);
            }
        };
    }

    RobotEventListener robotEventListener = new RobotEventListener() {
        @Override
        public void onWikiServiceStart() {
            // Nuwa Robot SDK is ready now, you call call Nuwa SDK API now.
            Log.d(TAG, "onWikiServiceStart, robot ready to be control");
            //Start Control Robot after Service ready.

            //Here just speak welcome sentence once when app start.
            mBehaviorHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NuwaSDKManager.getInstance().getNuwaRobotAPI().startTTS(getResources().getString(R.string.app_welcome_sentence));
                }
            }, 500);
        }

        @Override
        public void onWikiServiceStop() {

        }

        @Override
        public void onWikiServiceCrash() {

        }

        @Override
        public void onWikiServiceRecovery() {

        }

        @Override
        public void onStartOfMotionPlay(String s) {

        }

        @Override
        public void onPauseOfMotionPlay(String s) {

        }

        @Override
        public void onStopOfMotionPlay(String s) {

        }

        @Override
        public void onCompleteOfMotionPlay(String s) {
            Log.d(TAG, "onCompleteOfMotionPlay, " + s);
        }

        @Override
        public void onPlayBackOfMotionPlay(String s) {

        }

        @Override
        public void onErrorOfMotionPlay(int i) {

        }

        @Override
        public void onPrepareMotion(boolean b, String s, float v) {

        }

        @Override
        public void onCameraOfMotionPlay(String s) {

        }

        @Override
        public void onGetCameraPose(float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8, float v9, float v10, float v11) {

        }

        @Override
        public void onTouchEvent(int i, int i1) {

        }

        @Override
        public void onPIREvent(int i) {

        }

        @Override
        public void onTap(int i) {

        }

        @Override
        public void onLongPress(int i) {

        }

        @Override
        public void onWindowSurfaceReady() {

        }

        @Override
        public void onWindowSurfaceDestroy() {

        }

        @Override
        public void onTouchEyes(int i, int i1) {

        }

        @Override
        public void onRawTouch(int i, int i1, int i2) {

        }

        @Override
        public void onFaceSpeaker(float v) {

        }

        @Override
        public void onActionEvent(int i, int i1) {

        }

        @Override
        public void onDropSensorEvent(int i) {

        }

        @Override
        public void onMotorErrorEvent(int i, int i1) {

        }
    };


    private void notifyBaseServiceFinish() {
        try {
            //if nothing need to do, call notifyBehaviorFinished to finish custombehavior
            notifyBehaviorFinished();
        } catch (RemoteException RE) {
            Log.e(TAG, "handleMessage, RemoteException", RE);
        }
    }

    private EventListener mEventListener = new EventListener() {
        @Override
        public void onCompleted(String RunnableName, String var) {
            Log.d(TAG, "onCompleted, runnableName=" + RunnableName);
            //Finish
            notifyBaseServiceFinish();
        }

        @Override
        public void onError(String RunnableName, String error) {
            Log.d(TAG, "onError, runnableName=" + RunnableName);
        }
    };

    /**
     * Parser string to JSON object
     */
    private static JSONObject parse(String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
        } catch (Exception e) {
            obj = null;
            e.printStackTrace();
        }
        return obj;
    }

}
