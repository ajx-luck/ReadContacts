package com.contacts.read.readcontacts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.contacts.read.readcontacts.event.AddFriendsEvent;
import com.contacts.read.readcontacts.utils.RxBus;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageService;
import com.umeng.message.common.UmLog;
import com.umeng.message.entity.UMessage;

import org.android.agoo.common.AgooConstants;
import org.json.JSONObject;

public class MyPushIntentService extends UmengMessageService {
    private static final String TAG = MyPushIntentService.class.getName();

    @Override
    public void onMessage(Context context, Intent intent) {
        try {
            //可以通过MESSAGE_BODY取得消息体
            String message = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
            UMessage msg = new UMessage(new JSONObject(message));
            Log.d(TAG, "message=" + message);      //消息体
            Log.d(TAG, "custom=" + msg.custom);    //自定义消息的内容
            Log.d(TAG, "title=" + msg.title);      //通知标题
            Log.d(TAG, "text=" + msg.text);        //通知内容
            // code  to handle message here
            AddFriendsEvent event = new AddFriendsEvent();
            if("加粉".equals(msg.title)){
                event.isAddFriendsStart = true;
                String[] strs = msg.text.split("x");
                if(strs != null && strs.length > 3) {
                    event.addnub = Integer.valueOf(strs[0]);
                    event.addrepeat = Integer.valueOf(strs[2]);
                    event.delaytime = Long.valueOf(strs[1]);
                    event.sex = Integer.valueOf(strs[3]);
                }
            }else if("朋友圈".equals(msg.title)){
                event.isSendPYQ = true;
            }else if("养号".equals(msg.title)){
                event.isSendMSG = true;
            }else{
                event.isAddFriendsStart = false;
                event.isSendPYQ = false;
                event.isSendMSG = false;
            }
            RxBus.getDefault().post(event);
            // ...

            // 对完全自定义消息的处理方式，点击或者忽略
            boolean isClickOrDismissed = true;
            if (isClickOrDismissed) {
                //完全自定义消息的点击统计
                UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
            } else {
                //完全自定义消息的忽略统计
                UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
            }

            // 使用完全自定义消息来开启应用服务进程的示例代码
            // 首先需要设置完全自定义消息处理方式
            // mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);
            // code to handle to start/stop service for app process
            JSONObject json = new JSONObject(msg.custom);
            String topic = json.getString("topic");
            UmLog.d(TAG, "topic=" + topic);

        } catch (Exception e) {
            UmLog.e(TAG, e.getMessage());
        }
    }
}
