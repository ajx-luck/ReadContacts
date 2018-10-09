package com.contacts.read.readcontacts;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.contacts.read.readcontacts.event.AddFriendsEvent;
import com.contacts.read.readcontacts.utils.Constant;
import com.contacts.read.readcontacts.utils.RxBus;
import com.contacts.read.readcontacts.utils.Utils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static String firstName = "赵钱孙李周吴郑王冯陈褚卫蒋沈韩杨朱秦尤许何吕施张孔曹严华金魏陶姜戚谢邹喻柏水窦章云苏潘葛奚范彭郎鲁韦昌马苗凤花方俞任袁柳酆鲍史唐费廉岑薛雷贺倪汤滕殷罗毕郝邬安常乐于时傅皮卞齐康伍余元卜顾孟平黄和穆萧尹姚邵湛汪祁毛禹狄米贝明臧计伏成戴谈宋茅庞熊纪舒屈项祝董梁杜阮蓝闵席季麻强贾路娄危江童颜郭梅盛林刁钟徐邱骆高夏蔡田樊胡凌霍虞万支柯咎管卢莫经房裘缪干解应宗宣丁贲邓郁单杭洪包诸左石崔吉钮龚程嵇邢滑裴陆荣翁荀羊於惠甄魏加封芮羿储靳汲邴糜松井段富巫乌焦巴弓牧隗山谷车侯宓蓬全郗班仰秋仲伊宫宁仇栾暴甘钭厉戎祖武符刘姜詹束龙叶幸司韶郜黎蓟薄印宿白怀蒲台从鄂索咸籍赖卓蔺屠蒙池乔阴郁胥能苍双闻莘党翟谭贡劳逄姬申扶堵冉宰郦雍却璩桑桂濮牛寿通边扈燕冀郏浦尚农温别庄晏柴瞿阎充慕连茹习宦艾鱼容向古易慎戈廖庚终暨居衡步都耿满弘匡国文寇广禄阙东殴殳沃利蔚越夔隆师巩厍聂晁勾敖融冷訾辛阚那简饶空曾毋沙乜养鞠须丰巢关蒯相查后江红游竺权逯盖益桓公万俟司马上官欧阳夏侯诸葛闻人东方赫连皇甫尉迟公羊澹台公冶宗政濮阳淳于仲孙太叔申屠公孙乐正轩辕令狐钟离闾丘长孙慕容鲜于宇文司徒司空亓官司寇仉督子车颛孙端木巫马公西漆雕乐正壤驷公良拓拔夹谷宰父谷粱晋楚阎法汝鄢涂钦段干百里东郭南门呼延归海羊舌微生岳帅缑亢况后有琴梁丘左丘东门西门商牟佘佴伯赏南宫墨哈谯笪年爱阳佟第五言福百家姓续";
    private static String girl = "秀娟英华慧巧美娜静淑惠珠翠雅芝玉萍红娥玲芬芳燕彩春菊兰凤洁梅琳素云莲真环雪荣爱妹霞香月莺媛艳瑞凡佳嘉琼勤珍贞莉桂娣叶璧璐娅琦晶妍茜秋珊莎锦黛青倩婷姣婉娴瑾颖露瑶怡婵雁蓓纨仪荷丹蓉眉君琴蕊薇菁梦岚苑婕馨瑗琰韵融园艺咏卿聪澜纯毓悦昭冰爽琬茗羽希宁欣飘育滢馥筠柔竹霭凝晓欢霄枫芸菲寒伊亚宜可姬舒影荔枝思丽 ";
    private static String boy = "伟刚勇毅俊峰强军平保东文辉力明永健世广志义兴良海山仁波宁贵福生龙元全国胜学祥才发武新利清飞彬富顺信子杰涛昌成康星光天达安岩中茂进林有坚和彪博诚先敬震振壮会思群豪心邦承乐绍功松善厚庆磊民友裕河哲江超浩亮政谦亨奇固之轮翰朗伯宏言若鸣朋斌梁栋维启克伦翔旭鹏泽晨辰士以建家致树炎德行时泰盛雄琛钧冠策腾楠榕风航弘";

    EditText edit, editIndex, editCount, editPhone,editTime,editName,editNub,editRepeat;
    Button addContacts;
    PowerManager.WakeLock mWakeLock;

    AddFriendsService myService;
    SharedPreferences sharedPreferences;

    public static boolean isTaskStart = false;

    int sex_pos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("单机模式");
        initView();
        requestGet();
        requestWriter();
        getLock(getApplicationContext());
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        String name = sharedPreferences.getString(Constant.USERNAME,"he");
        String deviceid = sharedPreferences.getString(Constant.DEVICEID,"");
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.setAlias(deviceid, name, new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean b, String s) {
                Log.d("MainActivity",s);
            }
        });
        Intent myServiceIntent = new Intent(MainActivity.this, AddFriendsService.class);
        bindService(myServiceIntent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        edit = findViewById(R.id.edit);
        editIndex = findViewById(R.id.edit_index);
        editCount = findViewById(R.id.edit_count);
        editPhone = findViewById(R.id.edit_phone);
        addContacts = findViewById(R.id.btn_add_contacts);
        editTime = findViewById(R.id.edit_time);
        editName = findViewById(R.id.edit_name);
        editNub = findViewById(R.id.edit_nub);
        editRepeat = findViewById(R.id.edit_repeat);
        String btnName = isTaskStart ? "停止添加好友" :"开始添加好友";
        addContacts.setText(btnName);

        findViewById(R.id.open_accessibility_setting).setOnClickListener(clickListener);
        findViewById(R.id.btn_save).setOnClickListener(clickListener);
        findViewById(R.id.btn_save_contacts).setOnClickListener(clickListener);
        findViewById(R.id.btn_add_contacts).setOnClickListener(clickListener);
        findViewById(R.id.btn_sendmsg).setOnClickListener(clickListener);
        findViewById(R.id.get_wx_name).setOnClickListener(clickListener);
        findViewById(R.id.btn_delete_contacts).setOnClickListener(clickListener);
        findViewById(R.id.btn_get_contacts).setOnClickListener(clickListener);
        findViewById(R.id.btn_reply).setOnClickListener(clickListener);
        Spinner spinner = findViewById(R.id.spinner_sex);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sex_pos = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.open_accessibility_setting:
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
//                    OpenAccessibilitySettingHelper.jumpToSettingPage(getBaseContext());
                    break;
                case R.id.btn_save:
                    savePYQ();
                    break;
                case R.id.btn_save_contacts:
                    saveContacts();
                    break;
                case R.id.btn_add_contacts:
                    setAddFriends();
                    break;
                case R.id.btn_sendmsg:
                    sendMsg();
                    break;
                case R.id.btn_delete_contacts:
                    hintDelete();
                    break;
                case R.id.btn_get_contacts:
                    myService.getContacts();
                    break;
                case R.id.get_wx_name:
                    Intent intent1 = new Intent(MainActivity.this,ControlActivity.class);
                    startActivity(intent1);
                    break;
                case R.id.btn_reply:
                    Intent intent2 = new Intent(MainActivity.this,ReplySettingActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    };


    private void hintDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("确定要删除所有的联系人？");
        builder.setPositiveButton("是呀", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                delAllContacts();
            }
        });
        //为构造器设置取消按钮,若点击按钮后不需要做任何操作则直接为第二个参数赋值null
        builder.setNegativeButton("再考虑一下",null);
        builder.create().show();
    }

    /**
     * 发送消息给好友
     */
    private void sendMsg() {
        String name = editName.getText().toString();
        if(!TextUtils.isEmpty(name)){
            myService.sendMsg(name,name);
        }
    }

    /**
     * 添加微信好友
     */
    private void setAddFriends() {
        String tempTime = editTime.getText().toString();
        String tempNub = editNub.getText().toString();
        String tempRepeat = editRepeat.getText().toString();
        isTaskStart = !isTaskStart;
        String btnName = isTaskStart ? "停止添加好友" :"开始添加好友";
        addContacts.setText(btnName);
        if(isTaskStart){
            AddFriendsEvent event = new AddFriendsEvent();
            event.isAddFriendsStart = true;
            event.addnub = Integer.valueOf(tempNub);
            event.addrepeat = Integer.valueOf(tempRepeat);
            event.delaytime = Long.valueOf(tempTime);
            event.sex = sex_pos;
            RxBus.getDefault().post(event);
        }else{
            AddContactsService.contactsflag = false;
            myService.stopDelay();
        }
    }



    /**
     * 新建联系人
     */
    private void saveContacts() {

        String prePhone = editPhone.getText().toString();
        for(int i=0;i<100;i++) {
            String phone = getTel(prePhone);
            String name = getChineseName();
            addContact(name, phone);
        }
    }

    public boolean checkParams() {
        if (TextUtils.isEmpty(editIndex.getText().toString())) {
            Toast.makeText(getBaseContext(), "起始下标不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(editCount.getText().toString())) {
            Toast.makeText(getBaseContext(), "图片总数不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Integer.valueOf(editCount.getText().toString()) > 9) {
            Toast.makeText(getBaseContext(), "图片总数不能超过9张", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void savePYQ() {
        if (!checkParams()) {
            return;
        }

        int index = Integer.valueOf(editIndex.getText().toString());
        int count = Integer.valueOf(editCount.getText().toString());

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.CONTENT, edit.getText().toString());
        editor.putInt(Constant.INDEX, index);
        editor.putInt(Constant.COUNT, count);
        if (editor.commit()) {
            AddContactsService.pyqflag = false;
            AddContactsService.isLauncherUI = false;
            openWeChatApplication();//打开微信应用
        } else {
            Toast.makeText(getBaseContext(), "保存失败", Toast.LENGTH_LONG).show();
        }
    }

    private void openWeChatApplication() {
        if(!Utils.isAccessibilitySettingsOn(getApplicationContext())){
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(getBaseContext(), "请先打开辅助功能开关", Toast.LENGTH_LONG).show();
        }else {
            PackageManager packageManager = getBaseContext().getPackageManager();
            Intent it = packageManager.getLaunchIntentForPackage("com.tencent.mm");
            startActivity(it);
        }
    }


    public void addContact(String name, String phoneNumber) {
        // 创建一个空的ContentValues
        ContentValues values = new ContentValues();

        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        Uri rawContactUri = getContentResolver().insert(RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        values.clear();

        values.put(Data.RAW_CONTACT_ID, rawContactId);
        // 内容类型
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        // 联系人名字
        values.put(StructuredName.GIVEN_NAME, name);
        // 向联系人URI添加联系人名字
        getContentResolver().insert(Data.CONTENT_URI, values);
        values.clear();

        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        // 联系人的电话号码
        values.put(Phone.NUMBER, phoneNumber);
        // 电话类型
        values.put(Phone.TYPE, Phone.TYPE_MOBILE);
        // 向联系人电话号码URI添加电话号码
        getContentResolver().insert(Data.CONTENT_URI, values);
        values.clear();

        values.put(Data.RAW_CONTACT_ID, rawContactId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        // 联系人的Email地址
//        values.put(Email.DATA, "m"+phoneNumber+"@163.com");
        // 电子邮件的类型
//        values.put(Email.TYPE, Email.TYPE_WORK);
        // 向联系人Email URI添加Email数据
        getContentResolver().insert(Data.CONTENT_URI, values);

//        Toast.makeText(this, "联系人数据添加成功", Toast.LENGTH_SHORT).show();
    }


    public void requestWriter() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_CONTACTS)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CONTACTS,}, 1);
            }
        }
    }

    public void requestGet() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS,}, 1);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 666) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //申请读取联系人权限失败
                Toast.makeText(MainActivity.this, "申请写联系人权限失败", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start + 1) + start);
    }

    private static String[] telFirst = "134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153,188,185".split(",");

    private static String getTel(String number) {
        int index = getNum(0, telFirst.length - 1);
        String first = number;
        String second = "";
        int length = first.length() - 3;
        if (first == null || first == "") {
            first = telFirst[index];
            length = 0;
        }
        if (length == 4) {
            second = "";
        } else {
            second = String.valueOf(getNum(1, 888) + 10000).substring(1 + length);
        }
        String thrid = String.valueOf(getNum(1, 9999) + 10000).substring(1);
        return first + second + thrid;
    }

    /**
     * 50      * 返回中文姓名
     * 51
     */
    private static String name_sex = "";

    public static String getChineseName() {
        int index = getNum(0, firstName.length() - 1);
        String first = firstName.substring(index, index + 1);
        int sex = getNum(0, 1);
        String str = boy;
        int length = boy.length();
        if (sex == 0) {
            str = girl;
            length = girl.length();
            name_sex = "女";
        } else {
            name_sex = "男";
        }
        index = getNum(0, length - 1);
        String second = str.substring(index, index + 1);
        int hasThird = getNum(0, 1);
        String third = "";
        if (hasThird == 1) {
            index = getNum(0, length - 1);
            third = str.substring(index, index + 1);
        }
        return first + second + third;
    }


    /**
     * 删除全部联系人
     *
     * @return
     */
    public HashMap<String, Object> delAllContacts() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation op = null;
        Uri uri = null;
        HashMap<String, Object> delResult = new HashMap<String, Object>();
        int num = 0;//删除影响的行数
        getContentResolver().delete(Uri.parse(ContactsContract.RawContacts.CONTENT_URI.toString() + "?"
                        + ContactsContract.CALLER_IS_SYNCADAPTER + "=true"),
                ContactsContract.RawContacts._ID + ">0", null);
        //删除Data表的数据
        uri = Uri.parse(Data.CONTENT_URI.toString() + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        op = ContentProviderOperation.newDelete(uri)
                .withSelection(Data.RAW_CONTACT_ID + ">0", null)
                .withYieldAllowed(true)
                .build();
        ops.add(op);
        //删除RawContacts表的数据
        uri = Uri.parse(RawContacts.CONTENT_URI.toString() + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        op = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                .withSelection(RawContacts._ID + ">0", null)
                .withYieldAllowed(true)
                .build();
        ops.add(op);
        //删除Contacts表的数据
        uri = Uri.parse(ContactsContract.Contacts.CONTENT_URI.toString() + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        op = ContentProviderOperation.newDelete(uri)
                .withSelection(ContactsContract.Contacts._ID + ">0", null)
                .withYieldAllowed(true)
                .build();
        ops.add(op);
        //执行批量删除
        try {
            ContentProviderResult[] results = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            for (ContentProviderResult result : results) {
                num += result.count;

            }
            delResult.put("result", "1");
            delResult.put("obj", num);
        } catch (Exception e) {
            delResult.put("result", "-1");
            delResult.put("obj", "删除失败！" + e.getMessage());
        }
        if (delResult.size() == 0) {
            delResult.put("result", "0");
            delResult.put("obj", "无效删除，联系人信息不正确！");
        }
        return delResult;
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        String btnName = isTaskStart ? "停止添加好友" :"开始添加好友";
        addContacts.setText(btnName);
        editNub.setText(sharedPreferences.getInt(Constant.NUB,6)+"");
        editTime.setText(sharedPreferences.getLong(Constant.DELAYTIME,1)+"");
        editRepeat.setText(sharedPreferences.getInt(Constant.REPEAT,5)+"");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseLock();
        unbindService(mServiceConnection);
    }


    synchronized private void getLock(Context context){
        if(mWakeLock==null){
            PowerManager mgr=(PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock=mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,AddFriendsService.class.getName());
            mWakeLock.setReferenceCounted(true);
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis((System.currentTimeMillis()));
            int hour =c.get(Calendar.HOUR_OF_DAY);
            if(hour>=23||hour<=6){
                mWakeLock.acquire(5000);
            }else{
                mWakeLock.acquire(300000);
            }
        }

    }

    synchronized private void releaseLock()
    {
        if(mWakeLock!=null){
            if(mWakeLock.isHeld()) {
                mWakeLock.release();
            }

            mWakeLock=null;
        }
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((AddFriendsService.Binder) service).getService();
            myService.setReply();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

}
