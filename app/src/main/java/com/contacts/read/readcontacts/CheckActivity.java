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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.contacts.read.readcontacts.http.BaseResponse;
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

public class CheckActivity extends AppCompatActivity {
    private CompositeSubscription sCompositeSubscription;
    SharedPreferences sharedPreferences;
    TelephonyManager telephonyManager;
    LinearLayout opera;
    Button again,login;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        initView();
        if (sCompositeSubscription == null || sCompositeSubscription.isUnsubscribed()) {
            sCompositeSubscription = new CompositeSubscription();
        }
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        check();
    }

    private void initView() {
        opera = findViewById(R.id.opera);
        again = findViewById(R.id.again);
        login = findViewById(R.id.login);
        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckActivity.this,LoginActivity.class);
                startActivity(intent);

            }
        });
    }

    private void check() {
        requestGet();
        @SuppressLint("MissingPermission") final String deviceid = telephonyManager.getDeviceId();
        if(TextUtils.isEmpty(deviceid)){
            Toast.makeText(getApplicationContext(),"请点击再试一次并授予获取设备id的权限",Toast.LENGTH_LONG).show();
            opera.setVisibility(View.VISIBLE);
            return;
        }
        Subscription subscription =
                new ResultLoader().getCheck(deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("CheckActivity",e.toString());
                        Toast.makeText(getApplicationContext(),"如果设备已经激活，请再试一次",Toast.LENGTH_LONG).show();
                        opera.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNext(BaseResponse<String> str) {
                        Log.e("CheckActivity", str.data);
                        if(str.status == 200){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Constant.USERNAME,str.data);
                            editor.putString(Constant.DEVICEID,deviceid);
                            editor.commit();
                            Intent intent = new Intent(CheckActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(getApplicationContext(),"如果设备已经激活，请再试一次",Toast.LENGTH_LONG).show();
                            opera.setVisibility(View.VISIBLE);
                        }
                    }
                });
        addSubscription(subscription);
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
