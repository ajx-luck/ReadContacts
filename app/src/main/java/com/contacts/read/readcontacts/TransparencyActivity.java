package com.contacts.read.readcontacts;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;


import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class TransparencyActivity extends Activity {
    final int ADD_FRIENDS = 0;
    final int ADD_PYQ = 1;
    int task = ADD_FRIENDS;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparency);
        task = getIntent().getIntExtra("TASK",0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PackageManager packageManager = getBaseContext().getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        },1000);
    }
}
