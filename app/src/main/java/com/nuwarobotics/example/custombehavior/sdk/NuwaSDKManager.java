package com.nuwarobotics.example.custombehavior.sdk;

import android.content.Context;
import android.util.Log;

import com.nuwarobotics.example.custombehavior.service.CustomService;
import com.nuwarobotics.service.IClientId;
import com.nuwarobotics.service.agent.NuwaRobotAPI;
import com.nuwarobotics.service.agent.RobotEventListener;
import com.nuwarobotics.service.agent.VoiceEventListener;

import java.lang.ref.WeakReference;
/*
Implement a SDK Manager to provide all app usage.
 */
public class NuwaSDKManager {
    private final String TAG = this.getClass().getSimpleName();

    private static final NuwaSDKManager ourInstance = new NuwaSDKManager();

    public static NuwaSDKManager getInstance() {
        return ourInstance;
    }

    //
    private NuwaRobotAPI mRobotAPI;
    private IClientId mClientId;
    private Context mContext;
    private WeakReference<Context> mServiceReference;


    public NuwaSDKManager() {

    }

    public NuwaSDKManager init(CustomService service) {
        mServiceReference = new WeakReference<>(service);
        mContext = mServiceReference.get();

        NuwaSDKInit();
        return this;
    }

    public void destory() {
        NuwaSDKDestory();
    }

    public NuwaRobotAPI getNuwaRobotAPI() {
        return mRobotAPI;
    }

    public NuwaSDKManager setRobotEventListener(RobotEventListener listener) {
        Log.d(TAG, "register robot eventListener ");
        //Step 2 : Register receive Robot Event
        mRobotAPI.registerRobotEventListener(listener);//listen callback of robot service event
        return this;
    }

    public NuwaSDKManager setVoiceEventListener(VoiceEventListener listener) {
        Log.d(TAG, "register voice eventListener ");
        //Step 3 : Register receive Voice Event
        mRobotAPI.registerVoiceEventListener(listener);//listen callback of robot service event
        return this;
    }

    //Common
    private void NuwaSDKInit() {
        //Step 1 : Initial Nuwa API Object
        mClientId = new IClientId(mContext.getClass().getCanonicalName());
        mRobotAPI = new NuwaRobotAPI(mContext, mClientId);
    }

    private void NuwaSDKDestory() {
        if (mRobotAPI != null) {
            mRobotAPI.release();
            mRobotAPI = null;
        }
    }
}
