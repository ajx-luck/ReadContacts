package com.contacts.read.readcontacts;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.contacts.read.readcontacts.http.BaseResponse;
import com.contacts.read.readcontacts.http.ResultLoader;
import com.contacts.read.readcontacts.utils.Constant;
import com.umeng.analytics.MobclickAgent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ControlActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private CompositeSubscription sCompositeSubscription;
    EditText edit, editIndex, editCount,editTime, editName,editNub,editRepeat;
    Button addContacts;
    String deviceid;
    String name;
    int sex_pos = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        setTitle("群控模式");
        if (sCompositeSubscription == null || sCompositeSubscription.isUnsubscribed()) {
            sCompositeSubscription = new CompositeSubscription();
        }
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        deviceid = sharedPreferences.getString(Constant.DEVICEID,"");
        name = sharedPreferences.getString(Constant.USERNAME,"");
        initView();
    }

    private void initView() {
        edit = findViewById(R.id.edit);
        editIndex = findViewById(R.id.edit_index);
        editCount = findViewById(R.id.edit_count);
        addContacts = findViewById(R.id.btn_add_contacts);
        editTime = findViewById(R.id.edit_time);
        editName = findViewById(R.id.edit_name);
        editNub = findViewById(R.id.edit_nub);
        editRepeat = findViewById(R.id.edit_repeat);

        findViewById(R.id.btn_save).setOnClickListener(clickListener);
        findViewById(R.id.btn_add_contacts).setOnClickListener(clickListener);
        findViewById(R.id.btn_sendmsg).setOnClickListener(clickListener);
        findViewById(R.id.btn_stop).setOnClickListener(clickListener);
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
                case R.id.btn_save:
                    savePYQ();
                    break;
                case R.id.btn_add_contacts:
                    setAddFriends();
                    break;
                case R.id.btn_sendmsg:
                    sendMsg();
                    break;
                case R.id.btn_stop:
                    stopAll();
                    break;
            }
        }
    };

    private void stopAll() {
        Subscription subscription =
                new ResultLoader().stopall(name,deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ControlActivity",e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<String> str) {

                    }
                });
        addSubscription(subscription);
    }

    private void savePYQ() {
        if (!checkParams()) {
            return;
        }
        String index = editIndex.getText().toString();
        String count = editCount.getText().toString();
        String content = edit.getText().toString();

        Subscription subscription =
                new ResultLoader().savepyqinfo(content,index,count,name,deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ControlActivity",e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<String> str) {

                    }
                });
        addSubscription(subscription);
    }

    /**
     * 添加微信好友
     */
    private void setAddFriends() {
        String tempTime = editTime.getText().toString();
        String tempNub = editNub.getText().toString();
        String tempRepeat = editRepeat.getText().toString();
        String sex = sex_pos+"";
        Subscription subscription =
                new ResultLoader().saveaddfriends(tempTime+"",name,deviceid,tempNub,tempRepeat,sex).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("ControlActivity",e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<String> str) {

                    }
                });
        addSubscription(subscription);
    }

    private void sendMsg() {
        String names = editName.getText().toString();
        if(!TextUtils.isEmpty(names)){
            Subscription subscription =
                    new ResultLoader().savenames(names,name,deviceid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<BaseResponse<String>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d("ControlActivity",e.toString());
                        }

                        @Override
                        public void onNext(BaseResponse<String> str) {

                        }
                    });
            addSubscription(subscription);
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        editNub.setText(sharedPreferences.getInt(Constant.NUB,6)+"");
        editTime.setText(sharedPreferences.getInt(Constant.DELAYTIME,1)+"");
        editRepeat.setText(sharedPreferences.getInt(Constant.REPEAT,5)+"");
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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

    public void addSubscription(Subscription subscription) {
        sCompositeSubscription.add(subscription);
    }
}
