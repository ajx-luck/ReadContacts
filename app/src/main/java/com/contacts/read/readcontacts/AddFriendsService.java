package com.contacts.read.readcontacts;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.contacts.read.readcontacts.event.AddFriendsEvent;
import com.contacts.read.readcontacts.http.BaseResponse;
import com.contacts.read.readcontacts.http.ChatResp;
import com.contacts.read.readcontacts.http.ContactsInfo;
import com.contacts.read.readcontacts.http.PYQInfo;
import com.contacts.read.readcontacts.http.ResultLoader;
import com.contacts.read.readcontacts.utils.Constant;
import com.contacts.read.readcontacts.utils.MyTimeTask;
import com.contacts.read.readcontacts.utils.NameUtils;
import com.contacts.read.readcontacts.utils.RxBus;
import com.contacts.read.readcontacts.utils.Utils;

import java.util.List;
import java.util.Random;
import java.util.TimerTask;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class AddFriendsService extends Service {
    static final String TAG = "AddFriendsService";
    MyTimeTask task, delaytask,replytask;
    static final int TOAST = 888;
    private CompositeSubscription sCompositeSubscription;
    private int namepos = 0;
    private SharedPreferences sharedPreferences;
    private String deviceid;
    private String username;
    private String pushkey;
    private long currenttimes = 0;
    //每次加人个数
    private int nub = 0;
    //重复多少次后停止
    private int repeat = 0;

    private int finalnum;

    final String[] sexs = {"全部","男","女"};
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TOAST:
                    Toast.makeText(getBaseContext(), "请先打开辅助功能开关", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    };

    public AddFriendsService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (sCompositeSubscription == null || sCompositeSubscription.isUnsubscribed()) {
            sCompositeSubscription = new CompositeSubscription();
        }
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        deviceid = sharedPreferences.getString(Constant.DEVICEID, "");
        username = sharedPreferences.getString(Constant.USERNAME, "");
        pushkey = sharedPreferences.getString(Constant.PUSHTONKEN, "");
        RxBus.getDefault().toObservable(AddFriendsEvent.class)
                .subscribe(new Subscriber<AddFriendsEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(AddFriendsEvent addFriendsEvent) {
                        Log.d(TAG, "收到消息");
                        if (addFriendsEvent.isAddFriendsStart) {
                            nub = addFriendsEvent.addnub;
                            finalnum = nub;
                            repeat = addFriendsEvent.addrepeat;
                            int sex = addFriendsEvent.sex;
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            setDelay(addFriendsEvent.delaytime);
                            editor.putInt(Constant.SEXNUB,sex);
                            editor.putInt(Constant.NUB,nub);
                            editor.putLong(Constant.DELAYTIME,addFriendsEvent.delaytime);
                            editor.putInt(Constant.REPEAT,repeat);
                            editor.putString(Constant.SEX, sexs[sex]);
                            editor.commit();

                        } else if (addFriendsEvent.isSendPYQ) {
                            getPYQ();
                        } else if (addFriendsEvent.isSendMSG) {
                            getWxFriends();
                        } else if (addFriendsEvent.isDeleteContacts) {
                            deleteContact(addFriendsEvent.displayName);
                        } else if (addFriendsEvent.isgetContacts) {
                            getContacts();
                        } else if (addFriendsEvent.isuploadMSG) {
                            addmessage(addFriendsEvent.sendnickname, addFriendsEvent.messagecontent);
                        } else {
                            AddContactsService.pyqflag = true;
                            AddContactsService.isLauncherUI = false;
                            AddContactsService.contactsflag = false;
                            AddContactsService.msgflag = false;
                            AddContactsService.replyflag = false;
                            stopDelay();
                        }
                    }
                });
    }

    //上传聊天消息
    private void addmessage(String sendnickname, String messagecontent) {
        Subscription subscription = new ResultLoader().addmessage(pushkey, pushkey, deviceid, username, sendnickname, messagecontent).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<String> listBaseResponse) {

                    }
                });
        addSubscription(subscription);
    }

    //获取联系人
    public void getContacts() {
        if (System.currentTimeMillis() - currenttimes < 1000 * 60*60) {
            return;
        }
        currenttimes = System.currentTimeMillis();
        Subscription subscription = new ResultLoader().getphone(username, deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaseResponse<List<ContactsInfo>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                        Toast.makeText(getBaseContext(), "网络或权限问题，保存失败了", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(BaseResponse<List<ContactsInfo>> listBaseResponse) {
                        Toast.makeText(getBaseContext(), "联系人拉取成功", Toast.LENGTH_LONG).show();
                        List<ContactsInfo> list = listBaseResponse.data;
                        for (ContactsInfo contactsInfo : list) {
                            addContact(NameUtils.getChineseName(), contactsInfo.phonenumber);
                        }
                        int length = listBaseResponse.data == null ? 0 : list.size();
                        Toast.makeText(getBaseContext(), String.format("%d位联系人保存成功", length), Toast.LENGTH_LONG).show();
                    }
                });
        addSubscription(subscription);

    }

    private void getPYQ() {
        Subscription subscription =
                new ResultLoader().getPYQInfo(username, deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<PYQInfo>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<PYQInfo> chatResp) {
                        PYQInfo info = chatResp.data;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constant.CONTENT, info.content);
                        editor.putInt(Constant.INDEX, Integer.valueOf(info.sindex));
                        editor.putInt(Constant.COUNT, Integer.valueOf(info.asum));
                        if (editor.commit()) {
                            AddContactsService.pyqflag = false;
                            AddContactsService.isLauncherUI = false;
                            Intent intent = new Intent(getBaseContext(), TransparencyActivity.class);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getBaseContext(), "保存失败", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        addSubscription(subscription);

    }

    public void addSubscription(Subscription subscription) {
        Log.d(TAG, "add subscription");
        sCompositeSubscription.add(subscription);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public class Binder extends android.os.Binder {
        public AddFriendsService getService() {
            return AddFriendsService.this;
        }
    }



    //加好友定时任务,isCount为真不限制次数
    public void setAddFriends(long delayTime, final boolean isCount) {
        if (task != null) {
            stopAddFriends();
        }

        task = new MyTimeTask(delayTime, new TimerTask() {
            @Override
            public void run() {
                if (nub > 0 || isCount) {
                    //只有加成功了才减去1
                    if(AddContactsService.isAdd) {
                        nub = nub - 1;
                    }
                    setAddFriendsTask();
                } else {
                    stopAddFriends();
                }
            }
        });
        task.start();

    }


    //定时操作
    public void setDelay(long delayTime) {
        if (delaytask != null) {
            stopDelay();
        }

        delaytask = new MyTimeTask(delayTime * 1000 * 60 * 60, new TimerTask() {
            @Override
            public void run() {
                MainActivity.isTaskStart = true;
                if (repeat > 0) {
                    repeat = repeat - 1;
                    nub = finalnum;
                    setAddFriends(1000 * 40, false);
                } else {
                    stopDelay();
                }
            }
        });
        delaytask.start();

    }
    //定时回复
    public void setReply() {
        if (replytask != null) {
            stopReply();
        }

            replytask = new MyTimeTask(1000 * 90, new TimerTask() {
                @Override
                public void run() {
                    if (task == null && sharedPreferences.getBoolean(Constant.AUTOREPLY,false)) {
                        AddContactsService.replyflag = true;
                        AddContactsService.isLauncherUI = false;
                        gotoWX();
                    }
                }
            });
            replytask.start();


    }

    public void stopReply() {
        if (replytask != null) {
            replytask.stop();
            replytask = null;
        }
    }

    public void stopDelay() {
        stopAddFriends();
        if (delaytask != null) {
            MainActivity.isTaskStart = false;
            delaytask.stop();
            delaytask = null;
        }
    }


    public void stopAddFriends() {
        if (task != null) {
            task.stop();
            task = null;
        }
    }

    private void setAddFriendsTask() {
        AddContactsService.pyqflag = true;
        AddContactsService.isLauncherUI = false;
        AddContactsService.contactsflag = true;
        AddContactsService.msgflag = false;
        AddContactsService.replyflag = false;

        gotoWX();
    }
    //打开微信
    private void gotoWX() {
        if (!Utils.isAccessibilitySettingsOn(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {
            Log.d(TAG, "添加一位好友");
            Intent intent = new Intent(getBaseContext(), TransparencyActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


    public void sendMsg(String name, String msg) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.WXNAME, name);
        editor.putString(Constant.SENDMSG, msg);
        if (editor.commit()) {
            AddContactsService.pyqflag = true;
            AddContactsService.isLauncherUI = false;
            AddContactsService.contactsflag = false;
            AddContactsService.msgflag = true;
            AddContactsService.replyflag = false;
            Log.d(TAG, "发消息给好友");
            Intent intent = new Intent(getBaseContext(), TransparencyActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void getWxFriends() {

        Subscription subscription =
                new ResultLoader().getNames(username, deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<ChatResp>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<ChatResp> chatResp) {
                        namepos = 0;
                        if (chatResp != null && chatResp.data != null) {
                            sendMsgTask(chatResp.data);
                        }
//                        String name = chatResp.data.names.get(0);
//                        String msg = "在不在啊？又换微信号了？";
//                        sendMsg(name, msg);
//                        Log.d(TAG, chatResp.data.names.get(0));
                    }
                });
        addSubscription(subscription);
    }

    private void sendMsgTask(final ChatResp data) {
        if (task != null) {
            stopAddFriends();
        }
        task = new MyTimeTask(1000 * 60, new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "" + namepos);
                if (namepos >= data.names.size()) {
                    namepos = 0;
                }
                List<String> list = data.names;
                String name = list.get(namepos);
                String msg = MainActivity.getChineseName();
                int leng = new Random().nextInt(30) + 1;
                for (int i = 0; i < leng; i++) {
                    msg = msg + MainActivity.getChineseName();
                }
                sendMsg(name, msg);
                namepos = namepos + 1;

            }
        });
        task.start();
    }


    public void deleteContact(String name) {
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Data._ID}, "display_name=?", new String[]{name}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            resolver.delete(uri, "display_name=?", new String[]{name});
            uri = Uri.parse("content://com.android.contacts/data");
            resolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
        }

    }

    public void addContact(String name, String phoneNumber) {
        // 创建一个空的ContentValues
        ContentValues values = new ContentValues();

        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        // 内容类型
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        // 联系人名字
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        // 向联系人URI添加联系人名字
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // 联系人的电话号码
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);
        // 电话类型
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
        values.clear();

        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        // 联系人的Email地址
//        values.put(Email.DATA, "m"+phoneNumber+"@163.com");
        // 电子邮件的类型
//        values.put(Email.TYPE, Email.TYPE_WORK);
        // 向联系人Email URI添加Email数据
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

//        Toast.makeText(this, "联系人数据添加成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, new Notification());
        //绑定建立链接

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.stop();
            task = null;
        }
        if (sCompositeSubscription != null) {
            Log.d(TAG, "base activity unscbscribe");
            sCompositeSubscription.unsubscribe();
        }
    }


}
