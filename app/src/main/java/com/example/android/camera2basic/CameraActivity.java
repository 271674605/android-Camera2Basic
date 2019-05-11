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
import android.widget.Toast;
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
            //Handler消息传递:子线程到主线程---------------------------------------
            //2018-10-31 08:16:52.218 5310-5330/? I/bruce: start: I am thread=bruce线程SUB1_2_MAIN
            //2018-10-31 08:16:52.251 5310-5310/? I/bruce: I am from Sub thread
            //2018-10-31 08:16:52.251 5310-5310/? I/bruce: I am thread=main
            testSub2Main();

            //Handler消息传递:主线程到子线程---------------------------------------
//            2019-04-16 15:51:00.053 7029-7029/com.example.android.camera2basic I/bruce: start: I am thread=main
//            2019-04-16 15:51:00.053 7029-7048/com.example.android.camera2basic I/bruce: I am from main thread
//            2019-04-16 15:51:00.053 7029-7048/com.example.android.camera2basic I/bruce: I am thread=bruce线程MAIN_2_SUB2
            testMain2Sub();

            //Handler消息传递:子线程到子线程---------------------------------------
//            2019-04-16 15:52:17.625 7153-7173/? I/bruce: start: I am thread=bruce线程SUB_2_SUB-2
//            2019-04-16 15:52:17.625 7153-7172/? I/bruce: I am from other thread
//            2019-04-16 15:52:17.625 7153-7172/? I/bruce: I am thread=bruce线程SUB_2_SUB
            testSub2Sub();

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


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart------------------------------>");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart------------------------------>");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume------------------------------>");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //CloseTimer();
        Log.i(TAG, "onPause------------------------------>");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop------------------------------>");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy------------------------------>");
    }

//////////////////////testSub2Sub/////testMain2Sub//////testSub2Main///////////////////////////////////////////////////////////////////////////
    private Handler mMainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==Constants.SUB1_2_MAIN){
                Log.i(TAG, "I am from Sub thread");
                Log.i(TAG, "I am thread="+Thread.currentThread().getName());
            }
        }
    };
    private void testSub2Sub() {
        Runnable3 runnable3 = new Runnable3();
        new Thread(runnable3).start(); //启动线程3
        //注意：这里由于是线程异步，程序执行到这里的时候，子线程的run方法不一定执行完，可能会导致myLooper3为空，所以，这里循环等待，知道初始化完
        while (true){
            if(runnable3.myLooper3!=null){
                Runnable4 runnable4 = new Runnable4(runnable3.handler3);
                new Thread(runnable4).start();
                return;
            }
        }
    }
    private void testMain2Sub() {
        //主线程向子线程发消息
        Runnable2 runnable2 = new Runnable2();
        new Thread(runnable2).start(); //启动线程2
        Message msg = new Message();
        msg.what=Constants.MAIN_2_SUB2;

        //注意：这里由于是线程异步，程序执行到这里的时候，子线程的run方法不一定执行完，可能会导致myLooper3为空，所以，这里循环等待，知道初始化完
        while (true){
            if(runnable2.mylooper2!=null){
                Log.i(TAG,"start: I am thread="+Thread.currentThread().getName());
                runnable2.handler2.sendMessage(msg);
                return;
            }
        }
    }
    private void testSub2Main() {
        //子线程向主线程发消息
        new Thread(new Runnable1(mMainHandler)).start();
    }

    ////////////////////////////////addMultiThreadDebug////////////////////////////////////////////////////////////////////////////
    public void addMultiThreadDebug(){
        Log.i(TAG, "to addMultiThreadDebug！");
        new Thread("bruce线程2") {
            @Override
            public void run() {
                Log.i(TAG, "bruce线程2！");
                //发送一条空的消息，空消息中必需带一个what字段，用于在handler中接收，这里暂时我先写成0
                mHandlerCallback.sendEmptyMessage(0);
            }
        }.start();
        //StartTimer();
        //StartAsync();
        mMainHandler.postDelayed(taskRunnable,1000);//延迟调用
        Log.i(TAG, "to addMultiThreadDebug end！");
    }
		///////////////////////Handler延迟调用/////////////////////////////////////////////////////////////////////////////////////

    private Runnable taskRunnable = new Runnable(){
        public void run() {
            //Thread.currentThread().setName("bruce线程3");//main线程
            mMainHandler.postDelayed(this,1000);//设置延迟时间，此处是time_interval秒
            //需要执行的代码
            Log.i(TAG, "bruce线程3  main！");
        }
    };
	//////////////////////////////TimerTask//////////////////////////////////////////////////////////////////////////////

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
                    mMainHandler.sendMessage(msg);
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
        mMainHandler.sendEmptyMessage(1);
    }
	////////////////////////////////Handler.Callback////////////////////////////////////////////////////////////////////////////		
    private Handler mHandlerCallback = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
        	Log.i(TAG, "bruce mHandlerCallback");
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

	/////////////////////////////////////startService///////////////////////////////////////////////////////////////////////
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
		/////////////////////////////////////StartAsync///////////////////////////////////////////////////////////////////////

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
                Long startTime = System.currentTimeMillis(); //得到开始加载的时间
                for (int i = 0; i < 100; i++) {
                    publishProgress(i);//执行这个方法会异步调用onProgressUpdate方法，可以用来更新UI
                }
                Long endTime = System.currentTimeMillis();//得到结束加载的时间
                return endTime - startTime;//将读取时间返回
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
