package com.contacts.read.readcontacts;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.contacts.read.readcontacts.utils.Constant;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "a41e3f566433bfc0b53ea1975c7bfcd9");
        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constant.PUSHTONKEN, deviceToken);
                editor.commit();
                Log.d("MyApplication",deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e("MyApplication",s+s1);
            }
        });
        mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);
    }
}
