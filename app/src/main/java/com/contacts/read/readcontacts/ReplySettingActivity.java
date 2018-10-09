package com.contacts.read.readcontacts;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.contacts.read.readcontacts.utils.Constant;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ReplySettingActivity extends AppCompatActivity {
    CheckBox autoReply;
    LinearLayout llReply;
    EditText editReply1,editReply2,editReply3,editReply4,editCard,editSendReply;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_setting);
        setTitle("回复设置");
        autoReply = findViewById(R.id.auto_reply);
        llReply = findViewById(R.id.ll_reply);
        editReply1 = findViewById(R.id.edit_reply1);
        editReply2 = findViewById(R.id.edit_reply2);
        editReply3 = findViewById(R.id.edit_reply3);
        editReply4 = findViewById(R.id.edit_reply4);
        editSendReply = findViewById(R.id.edit_send_reply);
        editCard = findViewById(R.id.edit_card);
        sharedPreferences = getSharedPreferences(Constant.WECHAT_STORAGE, Activity.MODE_MULTI_PROCESS);
        boolean isReply = sharedPreferences.getBoolean(Constant.AUTOREPLY,false);
        autoReply.setChecked(isReply);
        llReply.setVisibility(isReply?View.VISIBLE:View.GONE);
        autoReply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                llReply.setVisibility(b?View.VISIBLE:View.GONE);
            }
        });
        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constant.REPLY1,editReply1.getText().toString());
        editor.putString(Constant.REPLY2,editReply2.getText().toString());
        editor.putString(Constant.REPLY3,editReply3.getText().toString());
        editor.putString(Constant.REPLY4,editReply4.getText().toString());
        editor.putString(Constant.CARD,editCard.getText().toString());
        editor.putString(Constant.SENDREPLY,editSendReply.getText().toString());
        editor.putBoolean(Constant.AUTOREPLY,autoReply.isChecked());
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences.edit().putBoolean(Constant.AUTOREPLY,false).commit();
        editReply1.setText(sharedPreferences.getString(Constant.REPLY1,""));
        editReply2.setText(sharedPreferences.getString(Constant.REPLY2,""));
        editReply3.setText(sharedPreferences.getString(Constant.REPLY3,""));
        editReply4.setText(sharedPreferences.getString(Constant.REPLY4,""));
        editCard.setText(sharedPreferences.getString(Constant.CARD,""));
        editSendReply.setText(sharedPreferences.getString(Constant.SENDREPLY,""));
    }
}
