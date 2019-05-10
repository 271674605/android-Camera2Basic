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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends AppCompatActivity {
    public int switchFunc = 0;
    public String TAG = "bruce";
    // 声明Button
    /**Timer对象**/
    Timer mTimer = null;

    /**TimerTask对象**/
    TimerTask mTimerTask = null;

    /**记录TimerID**/
    int mTimerID = 0;
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
            addMultiThreadDebug();
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
    public void addMultiThreadDebug(){
        Log.i(TAG, "to addMultiThreadDebug！");
        new Thread("bruce线程2") {
            @Override
            public void run() {
                Log.i(TAG, "bruce线程2！");
                //发送一条空的消息，空消息中必需带一个what字段，用于在handler中接收，这里暂时我先写成0
                mHandler.sendEmptyMessage(0);

            }
        }.start();
        StartTimer();
        StartAsync();
        handler.postDelayed(task,1000);//延迟调用
        Log.i(TAG, "to addMultiThreadDebug end！");
    }
    public void StartTimer() {

        if (mTimer == null) {
            mTimerTask = new TimerTask() {
                public void run() {
                    Log.i(TAG, "to TimerTask！");
                    Thread.currentThread().setName("bruce线程4");
                    //mTimerTask与mTimer执行的前提下每过1秒进一次这里
                    mTimerID ++;
                    Message msg = new Message();
                    msg.what = 1;
                    msg.arg1 = (int) (mTimerID);
                    handler.sendMessage(msg);
                }
            };
            mTimer = new Timer();

            //第一个参数为执行的mTimerTask
            //第二个参数为延迟的时间 这里写1000的意思是mTimerTask将延迟1秒执行
            //第三个参数为多久执行一次 这里写1000表示每1秒执行一次mTimerTask的Run方法
            mTimer.schedule(mTimerTask, 1000, 1000);
        }

    }

    public void CloseTimer() {

        //在这里关闭mTimer 与 mTimerTask
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask = null;
        }

        /**ID重置**/
        mTimerID = 0;

        //这里发送一条只带what空的消息
        handler.sendEmptyMessage(1);
    }
    private Handler handler = new Handler();
    private Runnable task = new Runnable(){
        public void run() {
            Thread.currentThread().setName("bruce线程3");
            handler.postDelayed(this,1000);//设置延迟时间，此处是time_interval秒
            //需要执行的代码
            Log.i(TAG, "bruce线程3！");
        }
    };
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            new Thread("bruce线程1") {
                @Override
                public void run() {
                    Log.i(TAG, "bruce线程1！");
                    //如果handler不指定looper的话，默认为mainlooper来进行消息循环，而当前是在一个新的线程中它没有默认的looper
                    //所以我们须要手动调用prepare()拿到他的loop，可以理解为在Thread创建Looper的消息队列
                    Looper.prepare();
                    Toast.makeText(CameraActivity.this, "收到消息",Toast.LENGTH_LONG).show();
                    //在这里执行这个消息循环如果没有这句，就好比只创建了Looper的消息队列而，没有执行这个队列那么上面Toast的内容是不会显示出来的
                    Looper.loop();
                    //如果没有   Looper.prepare();  与 Looper.loop();会抛出异常Can't create handler inside thread that has not called Looper.prepare()
                    //原因是我们新起的线程中是没有默认的looper所以须要手动调用prepare()拿到他的loop
                }
            }.start();
            return false;
        }
    });

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

    public void StartAsync() {
        new AsyncTask<Object, Object, Object>() {

            @Override
            protected void onPreExecute() {
                //首先执行这个方法，它在UI线程中 可以执行一些异步操作
                Log.i(TAG, "开始加载进度！");
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object... arg0) {
                //异步后台执行 ，执行完毕可以返回出去一个结果object对象

                //得到开始加载的时间
                Long startTime = System.currentTimeMillis();
                for (int i = 0; i < 100; i++) {
                    //执行这个方法会异步调用onProgressUpdate方法，可以用来更新UI
                    publishProgress(i);
                }
                //得到结束加载的时间
                Long endTime = System.currentTimeMillis();

                //将读取时间返回
                return endTime - startTime;
            }

            @Override
            protected void onPostExecute(Object result) {
                //doInBackground之行结束以后在这里可以接收到返回的结果对象
                super.onPostExecute(result);
            }


            @Override
            protected void onProgressUpdate(Object... values) {
                //时时拿到当前的进度更新UI
                Log.i(TAG, "当前加载进度！");
                super.onProgressUpdate(values);
            }
        }.execute();//可以理解为执行 这个AsyncTask
    }
}
