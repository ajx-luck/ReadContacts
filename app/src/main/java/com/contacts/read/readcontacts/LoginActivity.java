package com.contacts.read.readcontacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.contacts.read.readcontacts.http.BaseResponse;
import com.contacts.read.readcontacts.http.ChatResp;
import com.contacts.read.readcontacts.http.ResultLoader;
import com.contacts.read.readcontacts.utils.Constant;
import com.umeng.analytics.MobclickAgent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LoginActivity extends AppCompatActivity {
    static final String TAG = "LoginActivity";
    private CompositeSubscription sCompositeSubscription;
    EditText username, key;
    Button access;
    SharedPreferences sharedPreferences;
    TelephonyManager telephonyManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestGet();
        requestWriter();
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        initView();
        if (sCompositeSubscription == null || sCompositeSubscription.isUnsubscribed()) {
            sCompositeSubscription = new CompositeSubscription();
        }
    }

    private void initView() {
        username = findViewById(R.id.username);
        key = findViewById(R.id.key);
        access = findViewById(R.id.access);
        access.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = username.getText().toString();
                String secret = key.getText().toString();
                String pushkey = sharedPreferences.getString(Constant.PUSHTONKEN, "");
                @SuppressLint("MissingPermission") String deviceid = telephonyManager.getDeviceId();
                if(TextUtils.isEmpty(pushkey)){
                    Toast.makeText(getApplicationContext(),"激活失败，请关闭本软件并杀掉进程后重试",Toast.LENGTH_LONG).show();
                }else {
                    getAccess(pushkey, secret, deviceid, name);
                }

            }
        });
    }

    private void getAccess(String pushkey, String secret, String deviceid, final String name) {
        if(TextUtils.isEmpty(deviceid)){
            requestGet();
        }else{
            Subscription subscription =
                    new ResultLoader().getAccess(pushkey,secret,deviceid,name).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<String>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(TAG, e.toString());
                        }

                        @Override
                        public void onNext(BaseResponse<String> str) {
                            Log.e(TAG, str.message);
                            if(200 == str.status){
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(Constant.USERNAME,name);
                                editor.commit();
                                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(),"激活失败，请检测激活码是否正确",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            addSubscription(subscription);
        }
    }

    public void requestGet() {
        //判断是否已经赋予权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限
            } else {
                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE,}, 1);
            }
        }
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

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void addSubscription(Subscription subscription) {
        sCompositeSubscription.add(subscription);
    }
}
