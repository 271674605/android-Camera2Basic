package com.example.android.camera2basic;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent=new Intent();
        intent.setClass(MainActivity.this, TestActivity.class);
        //封装数据
        Bundle bundle=new Bundle();
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
