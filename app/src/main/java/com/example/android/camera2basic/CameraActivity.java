/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2basic;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {
    public int switchFunc = 1;
    public String TAG = "bruce";
    // 声明Button
    private Button startBtn,stopBtn,bindBtn,unbindBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(switchFunc == 0) {//camera2basic默认功能
            setContentView(R.layout.activity_camera);
            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance())
                        .commit();
            }
        }else if(switchFunc == 1){
            // 设置当前布局视图
            setContentView(R.layout.myservice);
            // 实例化Button
            startBtn = (Button)findViewById(R.id.startButton01);
            stopBtn = (Button)findViewById(R.id.stopButton02);
            bindBtn = (Button)findViewById(R.id.bindButton03);
            unbindBtn = (Button)findViewById(R.id.unbindButton04);

            // 添加监听器
            startBtn.setOnClickListener(startListener);//启动Service========================
            stopBtn.setOnClickListener(stopListener);

            bindBtn.setOnClickListener(bindListener);// 綁定Service==========================
            unbindBtn.setOnClickListener(unBindListener);
        }
    }
    //---------------------------startService---------------------------------------------------
    // 启动Service监听器
    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "to start！");
            // 创建Intent
            Intent intent = new Intent();
            // 设置Action属性
            intent.setAction("om.example.android.camera2basic.MY_SERVICE");
            intent.setPackage("com.example.android.camera2basic");
            // 启动该Service
            startService(intent);
        }
    };

    // 停止Service监听器
    private View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "to stop！");
            // 创建Intent
            Intent intent = new Intent();
            // 设置Action属性
            intent.setAction("om.example.android.camera2basic.MY_SERVICE");
            intent.setPackage("com.example.android.camera2basic");
            // 启动该Service
            stopService(intent);
        }
    };
    //----------------------------bindService--------------------------------------------------
    // 连接对象
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Connected！");
            Toast.makeText(CameraActivity.this, "Connected！", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Disconnect！");
            Toast.makeText(CameraActivity.this, "Disconnect！", Toast.LENGTH_LONG).show();
        }
    };

    // 綁定Service监听器
    private View.OnClickListener bindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "to Bind！");
            // 创建Intent
            Intent intent = new Intent();
            // 设置Action属性
            intent.setAction("om.example.android.camera2basic.MY_SERVICE");
            intent.setPackage("com.example.android.camera2basic");
            // 绑定Service
            bindService(intent, conn, Service.BIND_AUTO_CREATE);
        }
    };


    // 解除绑定Service监听器
    private View.OnClickListener unBindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "to unBind！");
            // 创建Intent
            Intent intent = new Intent();
            // 设置Action属性
            intent.setAction("om.example.android.camera2basic.MY_SERVICE");
            intent.setPackage("com.example.android.camera2basic");
            // 解除绑定Service
            unbindService(conn);
        }
    };
}
