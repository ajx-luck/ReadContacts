package com.contacts.read.readcontacts;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import static android.view.accessibility.AccessibilityEvent.*;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.ACTION_LONG_CLICK;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;

import com.contacts.read.readcontacts.event.AddFriendsEvent;
import com.contacts.read.readcontacts.utils.Constant;
import com.contacts.read.readcontacts.utils.RxBus;

import anet.channel.util.StringUtils;

public class AddContactsService extends AccessibilityService {
    private static final String TAG = "AddContactsService";
    private static final int MSG_BACK = 233;
    private static final int MSG_NEWCAONTACTS = 235;
    public static boolean contactsClicked =false;
    public static boolean newContactsed = false;
    public static boolean addContactsed = false;
    public static boolean isLauncherUI = true;
    public AccessibilityNodeInfo node = null;
    private static int clickIndex = 0;
    private String currentClassName = "";
    private AccessibilityNodeInfo accessibilityNodeInfo;
    private final int TEMP = 2000;
    private int scrollTime = 0;
    private boolean isBank = false;
    public static boolean isAdd = false;
    private String phonename;
    AccessibilityNodeInfo phoneNode,addButton;
    SharedPreferences sharedPreferences;
    //找到新发送消息的人
    String username;
    //上次添加的人
    String prephonename;

    /**
     * 是否已经发送过朋友圈，true已经发送，false还未发送
     */
    public static boolean pyqflag = true;

    /*
     * 是否添加联系人，true正在添加，false不添加
     */
    public static boolean contactsflag = false;

    /*
     * 聊天找人开关，true正在查找，false不查找
     */
    public static boolean msgflag = false;
    /*
     * 自动回复开关，true回复，false不回复
     */
    public static boolean replyflag = false;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BACK) {
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
//        pyqflag = false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        final int eventType = event.getEventType();
        final String className = event.getClassName().toString();
        Log.e(TAG, className);
        Log.e(TAG, "AccessibilityEventType："+eventType);
        accessibilityNodeInfo = getRootInActiveWindow();

        recycle(accessibilityNodeInfo,"0");

        switch (eventType){
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:// 通知栏事件
//                List<CharSequence> texts = event.getText();
//                if (!texts.isEmpty()) {
//                    for (CharSequence text : texts) {
//                        String content = text.toString();
//                        if (!TextUtils.isEmpty(content)) {
//                            sendNotifacationReply(event);
//                        }
//                    }
//                }


                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                if (event.getClassName().equals("com.tencent.mm.ui.LauncherUI")) {//第一次启动app
                    currentClassName = className;
                    isLauncherUI = true;
                    if(!pyqflag) {
                        pyqflag = false;
                        jumpToCircleOfFriends();//进入朋友圈页面
                    }else if(contactsflag){
                        jumpToNewFriends();
                    }else if(msgflag){
                        findAndPerformByString("搜索");
                    }else if(replyflag){
                        jumpToReply();
                    }
                }

                //回退到首页
                if(!isLauncherUI){
                    handler.sendEmptyMessageDelayed(MSG_BACK,new Random().nextInt(1000));
                    return;
                }

                if(!pyqflag &&event.getClassName().equals("com.tencent.mm.plugin.sns.ui.SnsTimeLineUI")){
                    //点击发朋友圈按钮
                    findAndPerformByString("拍照分享");
                    openAlbum();
                }

                if (!pyqflag && event.getClassName().equals("com.tencent.mm.plugin.sns.ui.SnsUploadUI")) {
                    String content = sharedPreferences.getString(Constant.CONTENT, "");
                    inputContentFinish(content);//写入要发送的朋友圈内容
                }

                if (!pyqflag && event.getClassName().equals("com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI")) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedPreferences != null) {
                                int index = sharedPreferences.getInt(Constant.INDEX, 0);
                                int count = sharedPreferences.getInt(Constant.COUNT, 0);
                                choosePicture(index, count);
                            }
                        }
                    }, TEMP);
                }


                if(contactsflag && "com.tencent.mm.plugin.subapp.ui.friend.FMessageConversationUI".equals(className)){
                    addContactsed = false;
                    currentClassName = className;
                    addFriends();
                }

                if("com.tencent.mm.plugin.profile.ui.ContactInfoUI".equals(className)){
                    currentClassName = className;
                    addContactsed = false;
                    if(!isBank) {
                        contactInfo();
                    }else if(!isAdd){
                        getContactInfo();
                    }
                }
                if("com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI".equals(className)){
                    currentClassName = className;
                    setSayHai();

                }

                if( "com.tencent.mm.ui.contact.ModRemarkNameUI".equals(className) || "com.tencent.mm.ui.contact.ContactRemarkInfoModUI".equals(className)){
                    currentClassName = className;
                    addContactsed = false;
                    setBankName();
                }

                if("com.tencent.mm.ui.chatting.ChattingUI".equals(className)){
                    currentClassName = className;
                    if(msgflag) {
                        String msg = sharedPreferences.getString(Constant.SENDMSG, "你好");
                        sendToFriends(msg);
                    }else if(replyflag){
                        autoreply();
                    }
                }

                if(replyflag && "com.tencent.mm.ui.contact.SelectContactUI".equals(className)){
                    currentClassName = className;
                    String card = sharedPreferences.getString(Constant.CARD,"");
                    if(StringUtils.isNotBlank(card)){
                        findFriends(card,card);
                        replyflag = false;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                findAndPerformByString("发送");
                            }
                        }, TEMP*2);
                    }

                }

                if("com.tencent.mm.plugin.fts.ui.FTSMainUI".equals(className)){
                    currentClassName = className;
                    if(msgflag) {
                        String name = sharedPreferences.getString(Constant.WXNAME, "你好");
                        String msg = sharedPreferences.getString(Constant.SENDMSG, "你好");
                        findFriends(name, msg);
                    }else if(replyflag){
                        if(StringUtils.isNotBlank(username)) {
                            findFriends(username, username);
                        }else{
                            handler.sendEmptyMessageDelayed(MSG_BACK,2000);
                        }
                    }
                }

                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
              break;


        }

    }

    //添加好友时候的验证信息
    private void setSayHai() {
//        findEditText(getRootInActiveWindow(),sharedPreferences.getString(Constant.SENDREPLY,""));
        findAndPerformByString("发送");
    }

    //自动回复
    private void autoreply() {
        sendToFriends(sharedPreferences.getString(Constant.REPLY1, ""));
        String reply1 = sharedPreferences.getString(Constant.REPLY1, "");
        String reply2 = sharedPreferences.getString(Constant.REPLY2, "");
        String reply3 = sharedPreferences.getString(Constant.REPLY3, "");
        String reply4 = sharedPreferences.getString(Constant.REPLY4, "");
        String card = sharedPreferences.getString(Constant.CARD, "");
        int index = 1;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendToFriends(sharedPreferences.getString(Constant.REPLY2, ""));
            }
        },TEMP*4*index);
        if(StringUtils.isNotBlank(reply2)){
            index = index+1;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendToFriends(sharedPreferences.getString(Constant.REPLY3, ""));
            }
        },TEMP*4*index);
        if(StringUtils.isNotBlank(reply3)){
            index = index+1;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendCard();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendToFriends(sharedPreferences.getString(Constant.REPLY4, ""));
                        sharedPreferences.edit().putBoolean(username,true).commit();
                        handler.sendEmptyMessageDelayed(MSG_BACK,6000);
                        handler.sendEmptyMessageDelayed(MSG_BACK,7000);
                    }
                },TEMP*4+2000);
            }
        },TEMP*4*index);

        if(StringUtils.isNotBlank(card)){
            index = index+1;
        }


    }


    //发送名片
    private void sendCard() {
        while (getTargetNode("000000001000300") == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        performClick( getTargetNode("000000001000300"));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        findAndPerformByString("名片");
    }

    //微信红点，新消息
    private synchronized void jumpToReply() {
        if(!replyflag){
            return;
        }
        replyflag = false;
        username = "";
        String name = "";
        performClick(getTargetNode("000000010001"));
        for(int i=0;i<10;i++){
            String index = String.format("00000000000%d01",i);
            AccessibilityNodeInfo target = getTargetNode(index);
            if(target != null && target.getText() != null){
                replyflag = true;
                name = String.format("00000000000%d1000",i);
                if(getTargetNode(name) == null){
                    continue;
                }
                username = getTargetNode(name).getText().toString();
                if(username.endsWith("...")){
                    username = username.substring(0,username.length()-3);
                }

               break;
            }
        }
        if(StringUtils.isNotBlank(username)){
            if(sharedPreferences.getBoolean(username,false)){
                performClick(getTargetNode(name));
                handler.sendEmptyMessageDelayed(MSG_BACK,2000);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        replyflag = true;
                        username = "";
                        findAndPerformByString("搜索");
                    }
                }, TEMP*2);
            }else{
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        replyflag = true;
                        findAndPerformByString("搜索");
                    }
                },TEMP*2);

            }
        }


    }

    //获取性别
    private void getContactInfo() {
        AccessibilityNodeInfo nodeInfo = getTargetNode("000000100010101");
        String sex = "";
        if(nodeInfo != null){
            Log.i(TAG, "contentDesc:" + nodeInfo.getContentDescription());
            sex = nodeInfo.getContentDescription()+"";
        }
        String targetsex = sharedPreferences.getString(Constant.SEX, "");
        if("全部".equals(targetsex)){
            findAndPerformByString("添加到通讯录");
            isAdd = true;
        }else if(targetsex.equals(sex)){
            findAndPerformByString("添加到通讯录");
            isAdd = true;
        }

    }


    private void sendNotifacationReply(AccessibilityEvent event) {
        if (event.getParcelableData() != null
                && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event
                    .getParcelableData();
            String content = notification.tickerText.toString();
            String[] cc = content.split(":");
            if(cc == null || cc.length < 2){
                return;
            }
            String name = cc[0].trim();
            String scontent = cc[1].trim();

            Log.i(TAG, "sender name =" + name);
            Log.i(TAG, "sender content =" + scontent);

            AddFriendsEvent addFriendsEvent = new AddFriendsEvent();
            addFriendsEvent.isuploadMSG = true;
            addFriendsEvent.sendnickname = name;
            addFriendsEvent.messagecontent = scontent;
            RxBus.getDefault().post(addFriendsEvent);
            PendingIntent pendingIntent = notification.contentIntent;
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }




    private void setBankName() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
//        AccessibilityNodeInfo target = getTargetNode(accessibilityNodeInfo,"00000100001");
//
//        pasteContent(target,simpleDateFormat.format(date)+phonename);
        findEditText(getRootInActiveWindow(),simpleDateFormat.format(date)+phonename);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isBank = true;
                findAndPerformByString("保存");
                findAndPerformByString("完成");
                AddFriendsEvent addFriendsEvent = new AddFriendsEvent();
                addFriendsEvent.isDeleteContacts = true;
                addFriendsEvent.displayName = phonename;
                RxBus.getDefault().post(addFriendsEvent);
            }
        }, TEMP*2);
    }

    //设置备注
    private void contactInfo() {
        findAndPerformByString("更多");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndPerformByString("设置备注及标签");
            }
        }, TEMP);
    }

    /**
     * 给好友发消息
     * @param msg
     */
    private void sendToFriends(String msg) {
        if(StringUtils.isBlank(msg)){
            return;
        }
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//        AccessibilityNodeInfo editnode = nodeInfo.getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(1).getChild(0).getChild(0).getChild(0).getChild(1).getChild(0);
////        pasteContent(nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ac8").get(0),msg);
//        pasteContent(editnode,msg);
        findEditText(nodeInfo,msg);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                findIdAndClick("acd", 0);
                findAndPerformByString("发送");
                msgflag = false;
            }
        }, 1000);

    }

    /**
     * 找到好友
     * @param name
     * @param content
     */
    private synchronized void findFriends(final String name,String content) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        findEditText(nodeInfo,name);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndPerformByString(name,0);
            }
        }, TEMP);

    }


    /**
     * 添加好友
     */
    private synchronized void addFriends() {

        if(addContactsed){
            return;
        }
        addContactsed = true;
        isAdd = false;
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (null == rootNode)
            return;
        phoneNode = findNodeByString("手机联系人");
        if(phoneNode == null || phoneNode.getParent() == null ||  phoneNode.getParent().getParent() == null || phoneNode.getParent().getParent().getChild(2) == null ){
            addButton = findNodeByString("添加");
            if(addButton == null){
                addButton = findNodeByString("正在验证");
            }
            if(addButton == null){
                addButton = findNodeByString("接受");
            }
            if (addButton != null){
                performLongClick(addButton.getParent());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                findAndPerformByString("删除");
                addButton = findNodeByString("添加");
            }
            AddFriendsEvent addFriendsEvent = new AddFriendsEvent();
            addFriendsEvent.isgetContacts = true;
            RxBus.getDefault().post(addFriendsEvent);
            return;
        }
        phonename = phoneNode.getText().toString();
        Toast.makeText(getBaseContext(),phonename,Toast.LENGTH_SHORT).show();
        addButton = phoneNode.getParent().getParent().getChild(2).getChild(0);
        if(addButton!= null && "添加".equals(addButton.getText().toString())) {
//            addButton.performAction(ACTION_CLICK);
        }else{
            //删除联系人
//            AddFriendsEvent addFriendsEvent = new AddFriendsEvent();
//            addFriendsEvent.isgetContacts = true;
//            RxBus.getDefault().post(addFriendsEvent);
            if(phonename.contains("手机联系人")) {
                phonename = phonename.substring(6, phonename.length());
            }
            AddFriendsEvent addFriendsEvent = new AddFriendsEvent();
            addFriendsEvent.isDeleteContacts = true;
            addFriendsEvent.displayName = phonename;
            RxBus.getDefault().post(addFriendsEvent);
            performLongClick(phoneNode);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            findAndPerformByString("删除");
            addContactsed = false;
            addFriends();
            return;

        }
        contactsflag = false;

        if(phonename.contains("手机联系人") && !phonename.equals(prephonename)) {
            prephonename = phonename;
            phonename = phonename.substring(6, phonename.length());
        }else{
            performLongClick(phoneNode);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            findAndPerformByString("删除");
            return;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isBank = false;
                findAndPerformByString(phonename);
            }
        }, TEMP);





    }

    private void findAndPerformByString(String string) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (null == rootNode) return;

        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText(string);
        if (null == list || list.size() == 0) return;
//        Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
        AccessibilityNodeInfo parent = list.get(list.size() - 1);
        while (null != parent) {
            if (parent.isClickable()) {
                parent.performAction(ACTION_CLICK);
                break;
            }
            parent = parent.getParent();
        }
    }

    private void performLongClick(AccessibilityNodeInfo node){
        AccessibilityNodeInfo parent = node;
        while (parent != null){
            if(parent.isLongClickable()){
                parent.performAction(ACTION_LONG_CLICK);
                break;
            }
            parent = parent.getParent();
        }

    }

    private void findAndPerformByString(String string,int index) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (null == rootNode) return;

        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText(string);
        if (null == list || list.size() == 0) return;
        Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
        AccessibilityNodeInfo parent = list.get(index);
        while (null != parent) {
            if (parent.isClickable()) {
                parent.performAction(ACTION_CLICK);
                break;
            }
            parent = parent.getParent();
        }
    }

    private String findIdByString(String string,int index){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (null == rootNode) return "";

        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText(string);
        if (null == list || list.size() == 0) return "";
        AccessibilityNodeInfo node = list.get(index);
        return node.getViewIdResourceName();
    }

    private AccessibilityNodeInfo findNodeByString(String string) {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (null == rootNode) return null;

        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText(string);
//        Toast.makeText(getApplicationContext(),string+"1",Toast.LENGTH_SHORT).show();
        if (null == list || list.size() == 0) return null;
//        Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
        AccessibilityNodeInfo parent = list.get(0);
        return parent;
    }

    /**
     * 模拟点击id事件
     *
     * @param id
     * @param i
     */
    public synchronized void findIdAndClick(String id, int i) {
        // 查找当前窗口中id为“id”的按钮
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        AccessibilityNodeInfo targetNode = null;
        if(!id.startsWith("com.tencent.mm:id/")){
            id = "com.tencent.mm:id/" + id;
        }
        node = null;
        if (node == null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByViewId(id);
            if (list.size() > 0 && i < list.size()) {
                node = list.get(i);
            }
        }
        targetNode = node;

        if (targetNode != null) {
            final AccessibilityNodeInfo n = targetNode;
            performClick(n);
        }
    }





    /**
     * 执行具体的点击
     *
     * @param nodeInfo
     */
    public void performClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            performClick(nodeInfo.getParent());
        }

    }

    public void performScroll(AccessibilityNodeInfo nodeInfo,int gesture){
        if (nodeInfo == null) {
            return;
        }
        if (nodeInfo.isScrollable()) {
            nodeInfo.performAction(gesture);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            performScroll(nodeInfo.getParent(),gesture);
        }
    }

    /**
     * 跳进新的朋友
     */
    private void jumpToNewFriends() {
        findAndPerformByString("通讯录");
//        findAndPerformByString("更多功能按钮");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndPerformByString("新的朋友");
            }
        }, TEMP);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndPerformByString("朋友推荐");
            }
        }, TEMP*2);
    }


    /**
     * 跳进朋友圈
     */
    private void jumpToCircleOfFriends() {
        findAndPerformByString("发现");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndPerformByString("朋友圈");
            }
        }, TEMP);
    }

    /**
     * 粘贴文本
     *
     * @param tempInfo
     * @param contentStr
     * @return true 粘贴成功，false 失败
     */
    private boolean pasteContent(AccessibilityNodeInfo tempInfo, String contentStr) {
        if (tempInfo == null) {
            return false;
        }
        if (tempInfo.isEnabled() && tempInfo.isClickable() && tempInfo.isFocusable()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("text", contentStr);
            if (clipboard == null) {
                return false;
            }
            clipboard.setPrimaryClip(clip);
            tempInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "");
            tempInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            tempInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            return true;
        }
        return false;
    }

    private boolean sendMsg() {

        List<AccessibilityNodeInfo> list = accessibilityNodeInfo.findAccessibilityNodeInfosByText("发表");//微信6.6.6版本修改为发表
        if (performClickBtn(list)) {
            pyqflag = true;//标记为已发送
            return true;
        }
        return false;
    }

    /**
     * 写入朋友圈内容
     *
     * @param contentStr
     */
    private void inputContentFinish(final String contentStr) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessibilityNodeInfo == null) {
                    return;
                }
                findEditText(accessibilityNodeInfo,contentStr);

            }
        }, TEMP);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMsg();
                handler.sendEmptyMessageDelayed(MSG_BACK,4200);
            }
        },TEMP*2);
    }

    /**
     * @param accessibilityNodeInfoList
     * @return
     */
    private boolean performClickBtn(List<AccessibilityNodeInfo> accessibilityNodeInfoList) {
        if (accessibilityNodeInfoList != null && accessibilityNodeInfoList.size() != 0) {
            for (int i = 0; i < accessibilityNodeInfoList.size(); i++) {
                AccessibilityNodeInfo accessibilityNodeInfo = accessibilityNodeInfoList.get(i);
                if (accessibilityNodeInfo != null) {
                    if (accessibilityNodeInfo.isClickable() && accessibilityNodeInfo.isEnabled()) {
                        accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 选择图片
     *
     * @param startPicIndex 从第startPicIndex张开始选
     * @param picCount      总共选picCount张
     */
    private void choosePicture(final int startPicIndex, final int picCount) {
        if (accessibilityNodeInfo == null) {
            return;
        }
        String id = accessibilityNodeInfo.getChild(0).getChild(0).getChild(0).getChild(1).getChild(0).getChild(0).getChild(0).getChild(0).getChild(2).getViewIdResourceName();
        for (int j = startPicIndex; j < startPicIndex + picCount; j++) {
//            findIdAndClick("bt4",j);
            findIdAndClick(id,j);

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

//                findIdAndClick("hg",0);
                findAndPerformByString("完成");

            }
        }, TEMP*2);
    }


    /**
     * 点击发送朋友圈按钮
     */
    private void clickCircleOfFriendsBtn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accessibilityNodeInfo == null) {
                    return;
                }
                List<AccessibilityNodeInfo> accessibilityNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("更多功能按钮");
                performClickBtn(accessibilityNodeInfoList);
                openAlbum();
            }
        }, TEMP);
    }


    /**
     * 打开相册
     */
    private void openAlbum() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findAndPerformByString("从相册选择");

            }
        }, TEMP);
    }


    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        if(StringUtils.isBlank(content)){
            return false;
        }
        int count = rootNode.getChildCount();

        android.util.Log.d("maptrix", "root class=" + rootNode.getClassName() + ","+ rootNode.getText()+","+count);
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                android.util.Log.d("maptrix", "nodeinfo = null");
                continue;
            }


            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                android.util.Log.i("maptrix", "==================");
                String index = "";
                pasteContent(nodeInfo,content);
//                Bundle arguments = new Bundle();
//                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
//                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
//                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
//                        true);
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
//                        arguments);
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
//                ClipData clip = ClipData.newPlainText("label", content);
//                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                clipboardManager.setPrimaryClip(clip);
//                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }

            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }

        return false;
    }





    @Override
    public void onInterrupt() {

    }


    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    public void recycle(AccessibilityNodeInfo info,String index) {
        if(info == null){
            return;
        }
        if (info.getChildCount() == 0) {
            Log.i(TAG, "child widget----------------------------" + info.getClassName());
            Log.i(TAG, "Text：" + info.getText());
            Log.i(TAG, "viewIdResourceName:" + info.getViewIdResourceName());
            Log.i(TAG, "contentDesc:" + info.getContentDescription());
            Log.i(TAG, "windowId:" + info.getWindowId());
            Log.i(TAG, "index:" + index);
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if(info.getChild(i)!=null){
                    recycle(info.getChild(i),index+i);
                }
            }
        }
    }


    private AccessibilityNodeInfo getTargetNode(String index){
        AccessibilityNodeInfo target = accessibilityNodeInfo;
        AccessibilityNodeInfo tem;
        for (int i=0;i<index.length();i++){
            int pos = Integer.valueOf(index.substring(i+0,i+1));
            if(target ==null){
                return null;
            }
            tem = target.getChild(pos);
            if(tem != null){
                Log.i(TAG, "viewIdResourceName:" + tem.getViewIdResourceName());
                target = tem;
            }else{
                return null;
            }
        }
        return target;
    }

}
