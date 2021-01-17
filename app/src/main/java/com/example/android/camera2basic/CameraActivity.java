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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.HandlerThread;
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
    private CountDownView mCountDownView;
    private View mRootView;
    private static final int EXPRESSION = 2;
    private static final int RECV_EXPRESSION = 4;
    Handler mMainHandlerCallback;
    Handler bruceHandlerThreadSub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        View rootLayout = getLayoutInflater().inflate(R.layout.fragment_camera2_basic, null, false);
//        mRootView = rootLayout.findViewById(R.id.fragment_container);
        if(switchFunc == 0) {//camera2basic默认功能
            setContentView(R.layout.activity_camera);
            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance())
                        .commit();
            }
            CreatThreadByAllMethod();
            testMainAndSubThreadSendMessage();
        }else if(switchFunc == 1){
            teststartService();
        }

//        View rootLayout = getLayoutInflater().inflate(R.layout.fragment_camera2_basic, null, false);
//        mRootView = rootLayout.findViewById(R.id.fragment_container);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //initCountDownView();
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
        //startCountDown(5, true);
        Log.i(TAG, "onResume------------------------------>");
    }

    private void initializeCountDown() {
        mRootView = (ViewGroup)this.getWindow().getDecorView();//获取ViewGroup
        this.getLayoutInflater().inflate(R.layout.count_down_to_capture,
                (ViewGroup) mRootView, true);
        mCountDownView = (CountDownView) (mRootView.findViewById(R.id.count_down_to_capture));
        //mCountDownView.setCountDownFinishedListener((CountDownView.OnCountDownFinishedListener) mModule);
        mCountDownView.bringToFront();
        //mCountDownView.setOrientation(mOrientation);
    }


    public boolean isCountingDown() {
        return mCountDownView != null && mCountDownView.isCountingDown();
    }

    public void cancelCountDown() {
        if (mCountDownView == null) return;
        mCountDownView.cancelCountDown();
        //showUIAfterCountDown();
    }

    public void initCountDownView() {
        if (mCountDownView == null) {
            initializeCountDown();
        } else {
            mCountDownView.initSoundPool();
        }
    }

    public void releaseSoundPool() {
        if (mCountDownView != null) {
            mCountDownView.releaseSoundPool();
        }
    }

    public void startCountDown(int sec, boolean playSound) {
        mCountDownView.startCountDown(sec, playSound);
        //hideUIWhileCountDown();
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
        mMainHandler.removeCallbacks(taskRunnable);
        Log.i(TAG, "onDestroy------------------------------>");
    }

//////////////////////testSub2Sub/////testMain2Sub//////testSub2Main////主线程和子线程相互通信///////////////////////////////////////////////////////////////////////
    public void testMainAndSubThreadSendMessage(){
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
    }
    private Handler mMainHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==Constants.SUB1_2_MAIN){
                Log.i(TAG, "I am from Sub thread");
                Log.i(TAG, "I am thread="+Thread.currentThread().getName());
            }
            if(isMainThread()){
                Log.i(TAG, "mMainHandler Now is Main Thread");
            }else{
                Log.i(TAG, "mMainHandler Now is now Main Thread");
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

    /////////////////////////////////////判断是否为主线程：isMainThread///////////////////////////////////////////////////////////////////////
//ThreadLocal 是个更神奇的东西，可以一个实例走遍所有Looper 和线程。啥意思？就是你在主线中，通过ThreadLocal的对象，取出来的Looper是主线程的。用同一个实例对象，在子线程中，调用同一个方法，取出来的是子线程的Looper .
//Handler其实根本不在乎自己在哪里。主要是Looper在哪个线程里。
//Handler是个神奇的东西，可以在线程中畅行无阻。你可以在主线程中创建子线程的Handler，也可以在子线程中创建主线程的Handler. 就是这么神奇。
//如果没有指定Looper，那么Looper就是当前线程的Looper，如果指定了Looper，那就不一定是当前线程的Looper了。那么这个时候Handler所在线程就得随着Looper而改变了。
    /**
     * Returns the application's main looper, which lives in the main thread of the application.
     public static Looper getMainLooper() {
         synchronized (Looper.class) {
             return sMainLooper;
         }
     }
     */

    /**
     * Return the Looper object associated with the current thread.  Returns
     * null if the calling thread is not associated with a Looper.
     public static @Nullable Looper myLooper() {
         return sThreadLocal.get();
     }
     */
    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
    public boolean isMainThread2() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
    public boolean isMainThread1() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    /////////////////////////////////////创建线程所有方式：CreatThreadByMethod///////////////////////////////////////////////////////////////////////
    public void CreatThreadByAllMethod(){
        CreatThreadByMethod1();//1、继承Thread类的实现：首先要写一个子线程类，去继承Thread类，重写run方法
        CreatThreadByMethod2();//2、直接用匿名内部类来实现，就不用单独写一个类
        CreatThreadByMethod3();//3、通过实现runnable接口来实现,同样，可以单独写一个类实现runnable接口
        CreatThreadByMethod4();//4、同样，启动过程也可以用匿名内部类实现
        CreatThreadByMethod5();//5、如果线程要操作的内容比较少，也可以直接用匿名类方式新建线程，启动线程一气呵成
        CreatThreadByMethod6();//6、HandlerThread方式开启线程
        CreatThreadByMethod7();//7、启用方式通过Handler启动线程
        CreatThreadByMethod8();//8、线程池
        CreatThreadByMethod9();//9、Handler.Callback: Handler使用Callback接口
        CreatThreadByMethod10();//10、TimerTask开启线程
        CreatThreadByMethod11();//11、AsyncTask开启线程
    }
    /////////////////////////////////////创建线程方式1：CreatThreadByMethod1///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod1(){//1、继承Thread类的实现：首先要写一个子线程类，去继承Thread类，重写run方法
        myCreatThread myThread = new myCreatThread();
        myThread.start();
        //等价于
        new myCreatThread().start();
    }
    /////////////////////////////////////创建线程方式2：CreatThreadByMethod2///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod2(){//2、直接用匿名内部类来实现，就不用单独写一个类
        new Thread(){
            @Override
            public void run() {
                super.run();
                //进行自己的操作
                Thread.currentThread().setName("bruce线程2");//
            }
        }.start();
    }
    /////////////////////////////////////创建线程方式3：CreatThreadByMethod3///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod3(){//3、通过实现runnable接口来实现,同样，可以单独写一个类实现runnable接口
        myCreatRunnable firstRunnable = new myCreatRunnable();
        new Thread(firstRunnable).start();
    }
    /////////////////////////////////////创建线程方式4：CreatThreadByMethod4///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod4(){//4、同样，启动过程也可以用匿名内部类实现
        new Thread(new myCreatRunnable()).start();
    }
    /////////////////////////////////////创建线程方式5：CreatThreadByMethod5///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod5(){//5、如果线程要操作的内容比较少，也可以直接用匿名类方式新建线程，启动线程一气呵成
        new Thread(new Runnable() {
            @Override
            public void run() {
                //进行自己的操作
                Thread.currentThread().setName("bruce线程5");//
            }
        }).start();
    }
    ////////////////////////////////创建线程方式6：CreatThreadByMethod6: HandlerThread方式开启线程////////////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod6(){//6、HandlerThread方式开启线程
        mMainHandlerCallback = new Handler(getMainLooper());
        HandlerThread bruceHandlerThread = new HandlerThread("ruce线程6:bruceHandlerThread");
        bruceHandlerThread.start();
        bruceHandlerThreadSub =  new Handler(bruceHandlerThread.getLooper());//获取bruceHandlerThread的Loop，通过它创建bruceHandlerThread的Handler
        bruceHandlerThreadSub.sendEmptyMessage(EXPRESSION);
    }
    public boolean handleMessage(Message msg) {
        //单独线程处理消息
        switch(msg.what){
            case EXPRESSION:
                //处理表情
                Log.i(TAG,"HandlerThread 收到表情消息");
                mMainHandlerCallback.sendEmptyMessage(RECV_EXPRESSION);
                break;
            case RECV_EXPRESSION:
                //主线程界面出现提示框
                Log.i(TAG,"HandlerThread 收到子线程发来的消息");
                Toast.makeText(getApplicationContext(), "收到子线程打来的消息", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        //回收消息对象
        //		msg.recycle();此行代码会带来异常
        return true;
    }
    /////////////////////////////////////创建线程方式7：CreatThreadByMethod7///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod7(){//7、启用方式通过Handler启动线程
        mMainHandler.postDelayed(taskRunnable,1000);//延迟调用，//给自己发送消息，自运行
    }
    private Runnable taskRunnable = new Runnable(){
        public void run() {
            //Thread.currentThread().setName("bruce线程7");//main线程
            mMainHandler.postDelayed(this,1000);//设置延迟时间，此处是time_interval秒。//发送消息，启动线程运行
            //需要执行的代码
            Log.i(TAG, "MainHandler.postDelayed bruce线程3  main！");
        }
    };
    /////////////////////////////////////创建线程方式8：CreatThreadByMethod8///////////////////////////////////////////////////////////////////////
    //线程池
    public void CreatThreadByMethod8(){




    }

    ////////////////////////////////创建线程方式9：CreatThreadByMethod9:Handler.Callback: Handler使用Callback接口////////////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod9(){//Handler.Callback: Handler使用Callback接口
        new Thread("bruce线程9:CreatThreadByMethod9") {
            @Override
            public void run() {
                Log.i(TAG, "Handler.Callback  bruce线程2！");
                //发送一条空的消息，空消息中必需带一个what字段，用于在handler中接收，这里暂时我先写成0
                mHandlerCallback.sendEmptyMessage(0);
            }
        }.start();
    }
    private Handler mHandlerCallback = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i(TAG, "Handler.Callback  bruce handleMessage");
            new Thread("bruce线程9:mHandlerCallback") {
                @Override
                public void run() {
                    Log.i(TAG, "Handler.Callback  bruce线程1！");
                    //如果handler不指定looper的话，默认为mainlooper来进行消息循环，而当前是在一个新的线程中它没有默认的looper
                    //所以我们须要手动调用prepare()拿到他的loop，可以理解为在Thread创建Looper的消息队列
                    Looper.prepare();
                    Toast.makeText(CameraActivity.this, "收到消息",Toast.LENGTH_LONG).show();
                    //在这里执行这个消息循环如果没有这句，就好比只创建了Looper的消息队列而，没有执行这个队列那么上面Toast的内容是不会显示出来的
                    Looper.loop();
                    //如果没有   Looper.prepare();  与 Looper.loop();会抛出异常Can't create handler inside thread that has not called Looper.prepare()
                    //原因是我们新起的线程中是没有默认的looper所以须要手动调用prepare()拿到他的loop
                    if(isMainThread()){
                        Log.i(TAG, "Handler.Callback Now is Main Thread");
                    }else{
                        Log.i(TAG, "Handler.Callback Now is now Main Thread");
                    }
                }
            }.start();
            return false;
        }
    });
    //////////////////////////////创建线程方式10：CreatThreadByMethod10:TimerTask开启线程//////////////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod10(){//TimerTask开启线程
        StartTimer();
    }
    public void StartTimer() {
        if (mTimer == null) {
            mTimerTask = new TimerTask() {
                public void run() {
                    Log.i(TAG, "to TimerTask！");
                    Thread.currentThread().setName("bruce线程10:StartTimer");
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
    /////////////////////////////////////创建线程方式11：CreatThreadByMethod11:AsyncTask开启线程///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod11(){//AsyncTask开启线程
        StartAsync();
    }
    public void StartAsync() {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected void onPreExecute() {
                //首先执行这个方法，它在UI线程中 可以执行一些异步操作
                Log.i(TAG, "AsyncTask 开始加载进度！");
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
                Log.i(TAG, "AsyncTask 当前加载进度！");
                super.onProgressUpdate(values);
            }
        }.execute();//可以理解为执行 这个AsyncTask
    }









    /////////////////////////////////////startService开启后台服务///////////////////////////////////////////////////////////////////////
    public void teststartService(){
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
    // 启动Service监听器
    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "startService to start！");
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
            Log.i(TAG, "startService to stop！");
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
            Log.i(TAG, "bindService Connected！");
            Toast.makeText(CameraActivity.this, "Connected！", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "bindService Disconnect！");
            Toast.makeText(CameraActivity.this, "Disconnect！", Toast.LENGTH_LONG).show();
        }
    };

    // 綁定Service监听器
    private View.OnClickListener bindListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "bindService to Bind！");
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
            Log.i(TAG, "bindService to unBind！");
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

class myCreatThread extends Thread{
    @Override
    public void run() {
        super.run();
        //干你需要做的操作
        Thread.currentThread().setName("bruce线程:myCreatThread");
    }
}
class myCreatRunnable implements Runnable{
    @Override
    public void run() {
        //干你需要做的操作
        Thread.currentThread().setName("bruce线程:myCreatRunnable");
    }
}