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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    HandlerThread bruceHandlerThread;
    private static final int EXPRESSION = 2;
    private static final int RECV_EXPRESSION = 4;
    Handler mMainHandlerCallback;
    Handler mBruceHandlerThreadSub;
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
            CreatThreadByAllMethod();
            CreatThreadPoolByAllMethod();
            CreatHandlerByAllMethod();
            CreatHandlerInThreadByAllMethod();
            testMainAndSubThreadSendMessage();
        }else if(switchFunc == 1){
            teststartService();
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
        mMainHandler.removeCallbacks(taskRunnable);
        DestroyThreadByMethod6();
        if (mMainHandler!=null){
            mMainHandler.removeMessages(3);
        }
        mMainHandler.getLooper().quitSafely();//Handler可能会处理延时消息，这时候如果Activity已经finish了，mHandler释放，执行消息则会导致程序崩溃。
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
    /////////////////////////////////////testSub2Sub()//////////////////////////////////////////////////////////////////////
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
    class Runnable3 implements Runnable {
        public Handler  handler3;
        public Looper myLooper3 ;
        public String TAG = "bruce";
        @Override
        public void run() {
            Thread.currentThread().setName("bruce线程SUB_2_SUB");
            Looper.prepare();
            //handler3= new Handler(Looper.getMainLooper()){    //如果这样，则明明在Thread3中的handler，却在MainThread中了。那就是指定了Looper，则Looper所在线程就是Handler回调所在线程。
            handler3= new Handler(){//没有指定，则在当前线程。
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(msg.what==Constants.SUB_2_SUB){
                        Log.i(TAG,"I am from other thread");
                        Log.i(TAG,"I am thread="+Thread.currentThread().getName());
                    }
                }
            };
            myLooper3= Looper.myLooper();
            Looper.loop();

        }
    }
    class Runnable4 implements Runnable {
        Handler handler;
        public String TAG = "bruce";
        public Runnable4 (Handler handler){
            this.handler = handler;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("bruce线程SUB_2_SUB-2");
            Message msg = new Message();
            msg.what =Constants.SUB_2_SUB;
            Log.i(TAG,"start: I am thread="+Thread.currentThread().getName());
            handler.sendMessage(msg);

        }
    }
    /////////////////////////////////////testMain2Sub()//////////////////////////////////////////////////////////////////////
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
    class Runnable2 implements Runnable {
        public Looper mylooper2;
        public Handler  handler2;
        public String TAG = "bruce";
        @Override
        public void run() {
            Thread.currentThread().setName("bruce线程MAIN_2_SUB2");
            Looper.prepare();              //创建一个新的looper，并将其放入sThreadLocal中
            handler2= new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(msg.what==Constants.MAIN_2_SUB2){
                        Log.i(TAG,"I am from main thread");
                        Log.i(TAG,"I am thread="+Thread.currentThread().getName());
                    }
                }
            };
            mylooper2 = Looper.myLooper();//从threadlocal中取出当前线程的looper
            Looper.loop();
        }
    }
    /////////////////////////////////////testSub2Main()//////////////////////////////////////////////////////////////////////
    private void testSub2Main() {
        //子线程向主线程发消息
        new Thread(new Runnable1(mMainHandler)).start();
    }
    class Runnable1 implements Runnable {
        Handler handler;
        public String TAG = "bruce";
        public Runnable1(Handler handler){
            this.handler = handler;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("bruce线程SUB1_2_MAIN");
            Message msg = new Message();
            msg.what=Constants.SUB1_2_MAIN;
            Log.i(TAG,"start: I am thread="+Thread.currentThread().getName());
            handler.sendMessage(msg);
        }
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
    class myCreatThread extends Thread{
        @Override
        public void run() {
            super.run();
            //干你需要做的操作
            Thread.currentThread().setName("bruce线程:myCreatThread");
        }
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
    class myCreatRunnable implements Runnable{
        @Override
        public void run() {
            //干你需要做的操作
            Thread.currentThread().setName("bruce线程:myCreatRunnable");
        }
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
        bruceHandlerThread = new HandlerThread("ruce线程6:bruceHandlerThread");
        bruceHandlerThread.start();
        mBruceHandlerThreadSub =  new bruceHandlerThreadSub(bruceHandlerThread.getLooper());//获取bruceHandlerThread的Loop，通过它创建bruceHandlerThread的Handler
        mBruceHandlerThreadSub.sendEmptyMessage(EXPRESSION);
    }
    public class bruceHandlerThreadSub extends Handler {

        public bruceHandlerThreadSub(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int id = msg.arg1;
            switch (msg.what) {
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
        }
    }
    public void DestroyThreadByMethod6(){
        if (bruceHandlerThread != null)
            bruceHandlerThread.quitSafely();
        try {
            if (bruceHandlerThread != null) {
                bruceHandlerThread.join();
                bruceHandlerThread = null;
            }
            mBruceHandlerThreadSub = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    /////////////////////////////////////线程中创建handler所有方式：CreatHandlerByAllMethod()//////////////////////////////////////////////////////////////////////
    public void CreatHandlerByAllMethod(){
        CreatHandlerByMethod1();
        CreatHandlerByMethod2();
    }
    /////////////////////////////////////线程中创建handler方式1：CreatHandlerByAllMethod1()///////////////////////////////////////////////////////////////////////
    public void CreatHandlerByMethod1(){//1.方法1（创建Handler实例，重载handleMessage方法，来处理消息。）
        Handler HandlerByMethod1 = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(getApplicationContext(), "handler msg", Toast.LENGTH_LONG).show();
            }
        };
        HandlerByMethod1.sendEmptyMessage(0);
    }
    /////////////////////////////////////线程中创建handler方式2：CreatHandlerByAllMethod2()///////////////////////////////////////////////////////////////////////
    public void CreatHandlerByMethod2(){//2.方法2 :继承自Handler，相同要实现handleMessage(Message msg)方法。
        Handler mHandlerByMethod2 =  new HandlerByMethod2(getMainLooper());
        mHandlerByMethod2.sendEmptyMessage(EXPRESSION);
    }
    public class HandlerByMethod2 extends Handler {

        public HandlerByMethod2(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            int id = msg.arg1;
            switch (msg.what) {
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
        }
    }
    /////////////////////////////////////线程中创建handler所有方式：CreatHandlerInThreadByAllMethod()//////////////////////////////////////////////////////////////////////
    public void CreatHandlerInThreadByAllMethod(){
        CreatHandlerInThreadByMethod1();
        CreatHandlerInThreadByMethod2();
    }
    /////////////////////////////////////线程中创建handler方式1：CreatHandlerInThreadByAllMethod1()///////////////////////////////////////////////////////////////////////
    public void CreatHandlerInThreadByMethod1(){//1.方法1（直接获取当前子线程的looper）
        new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setName("bruce handler1");
                Looper.prepare();// 此处获取到当前线程的Looper，并且prepare()
                Handler handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        Toast.makeText(getApplicationContext(), "handler msg", Toast.LENGTH_LONG).show();
                    }
                };
                handler.sendEmptyMessage(1);
                Looper.loop();
            };
        }).start();

    }
    /////////////////////////////////////线程中创建handler方式2：CreatHandlerInThreadByAllMethod2()///////////////////////////////////////////////////////////////////////
    public void CreatHandlerInThreadByMethod2(){//2.方法2（获取主线程的looper，或者说是UI线程的looper）
        new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().setName("bruce handler2");
                Handler handler = new Handler(Looper.getMainLooper()){ // 区别在这！！！！
                    @Override
                    public void handleMessage(Message msg) {
                        Toast.makeText(getApplicationContext(), "handler msg", Toast.LENGTH_LONG).show();
                    }
                };
                handler.sendEmptyMessage(1);
            };
        }).start();

    }

    /////////////////////////////////////线程池创建所有方式：//////////////////////////////////////////////////////////////////////
    /*
    一：使用线程池的原因
在android开发中经常会使用多线程异步来处理相关任务，而如果用传统的newThread来创建一个子线程进行处理，会造成一些严重的问题：
1：在任务众多的情况下，系统要为每一个任务创建一个线程，而任务执行完毕后会销毁每一个线程，所以会造成线程频繁地创建与销毁。
2：多个线程频繁地创建会占用大量的资源，并且在资源竞争的时候就容易出现问题，同时这么多的线程缺乏一个统一的管理，容易造成界面的卡顿。
3:多个线程频繁地销毁，会频繁地调用GC机制，这会使性能降低，又非常耗时。
总而言之：频繁地为每一个任务创建一个线程，缺乏统一管理，降低性能，并且容易出现问题。
为了解决这些问题，就要用到今天的主角——线程池.
线程池使用的好处：
1：对多个线程进行统一地管理，避免资源竞争中出现的问题。
2：（重点）：对线程进行复用，线程在执行完任务后不会立刻销毁，而会等待另外的任务，这样就不会频繁地创建、销毁线程和调用GC。
3：JAVA提供了一套完整的ExecutorService线程池创建的api，可创建多种功能不一的线程池，使用起来很方便。

四：各个线程池总结及适用场景
newCachedThreadPool：
底层：返回ThreadPoolExecutor实例，corePoolSize为0；maximumPoolSize为Integer.MAX_VALUE；keepAliveTime为60L；unit为TimeUnit.SECONDS；workQueue为SynchronousQueue(同步队列)
通俗：当有新任务到来，则插入到SynchronousQueue中，由于SynchronousQueue是同步队列，因此会在池中寻找可用线程来执行，若有可以线程则执行，若没有可用线程则创建一个线程来执行该任务；若池中线程空闲时间超过指定大小，则该线程会被销毁。
适用：执行很多短期异步的小程序或者负载较轻的服务器
newFixedThreadPool：
底层：返回ThreadPoolExecutor实例，接收参数为所设定线程数量nThread，corePoolSize为nThread，maximumPoolSize为nThread；keepAliveTime为0L(不限时)；unit为：TimeUnit.MILLISECONDS；WorkQueue为：new LinkedBlockingQueue<Runnable>() 无解阻塞队列
通俗：创建可容纳固定数量线程的池子，每隔线程的存活时间是无限的，当池子满了就不再添加线程了；如果池中的所有线程均在繁忙状态，对于新任务会进入阻塞队列中(无界的阻塞队列)
适用：执行长期的任务，性能好很多
newSingleThreadExecutor:
底层：FinalizableDelegatedExecutorService包装的ThreadPoolExecutor实例，corePoolSize为1；maximumPoolSize为1；keepAliveTime为0L；unit为：TimeUnit.MILLISECONDS；workQueue为：new LinkedBlockingQueue<Runnable>() 无解阻塞队列
通俗：创建只有一个线程的线程池，且线程的存活时间是无限的；当该线程正繁忙时，对于新任务会进入阻塞队列中(无界的阻塞队列)
适用：一个任务一个任务执行的场景
NewScheduledThreadPool:
底层：创建ScheduledThreadPoolExecutor实例，corePoolSize为传递来的参数，maximumPoolSize为Integer.MAX_VALUE；keepAliveTime为0；unit为：TimeUnit.NANOSECONDS；workQueue为：new DelayedWorkQueue() 一个按超时时间升序排序的队列
通俗：创建一个固定大小的线程池，线程池内线程存活时间无限制，线程池可以支持定时及周期性任务执行，如果所有线程均处于繁忙状态，对于新任务会进入DelayedWorkQueue队列中，这是一种按照超时时间排序的队列结构
适用：周期性执行任务的场景

     */
    public void CreatThreadPoolByAllMethod(){
        CreatThreadPoolByMethod1();//1：ThreadPoolExecutor 创建基本线程池
        CreatThreadPoolByMethod2();//2：FixedThreadPool (可重用固定线程数)
        CreatThreadPoolByMethod3();//3：CachedThreadPool (按需创建)
        CreatThreadPoolByMethod4();//4：SingleThreadPool(单个核线的fixed)
        CreatThreadPoolByMethod5();//5：ScheduledThreadPool(定时延时执行)
        CreatThreadPoolByMethod6();//6：自定义的PriorityThreadPool(队列中有优先级比较的线程池)
    }
    /////////////////////////////////////线程池创建方式1：///////////////////////////////////////////////////////////////////////
    public void CreatThreadPoolByMethod1() {//1：ThreadPoolExecutor 创建基本线程池
        for (int i = 0; i < 30; i++) {
            final int finali = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);//线程休眠2秒
                        Log.d("Thread", "run: " + finali);
                        Log.d("当前线程：", Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            threadPoolExecutor.execute(runnable);
        }
    }
    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3,5,1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100));    //创建基本线程池
/*
结果会每2s打印三个日志。
具体过程：
1.execute一个线程之后，如果线程池中的线程数未达到核心线程数，则会立马启用一个核心线程去执行。
2.execute一个线程之后，如果线程池中的线程数已经达到核心线程数，且workQueue未满，则将新线程放入workQueue中等待执行。
3.execute一个线程之后，如果线程池中的线程数已经达到核心线程数但未超过非核心线程数，且workQueue已满，则开启一个非核心线程来执行任务。
4.execute一个线程之后，如果线程池中的线程数已经超过非核心线程数，则拒绝执行该任务，采取饱和策略，并抛出RejectedExecutionException异常。
demo中设置的任务队列长度为100，所以不会开启额外的5-3=2个非核心线程，如果将任务队列设为25，则前三个任务被核心线程执行，剩下的30-3=27个任务进入队列会满，此时会开启2个非核心线程来执行剩下的两个任务。
//新开启了thread-4与thread-5执行剩下的超出队列的两个任务28和29
2019-03-28 15:54:07.879 22284-22618/com.example.threadpooltest D/Thread:: 1
2019-03-28 15:54:07.879 22284-22617/com.example.threadpooltest D/Thread:: 0
2019-03-28 15:54:07.879 22284-22617/com.example.threadpooltest D/当前线程：: pool-1-thread-1
2019-03-28 15:54:07.879 22284-22618/com.example.threadpooltest D/当前线程：: pool-1-thread-2
2019-03-28 15:54:07.880 22284-22619/com.example.threadpooltest D/Thread:: 2
2019-03-28 15:54:07.880 22284-22619/com.example.threadpooltest D/当前线程：: pool-1-thread-3
2019-03-28 15:54:07.881 22284-22620/com.example.threadpooltest D/Thread:: 28
2019-03-28 15:54:07.881 22284-22620/com.example.threadpooltest D/当前线程：: pool-1-thread-4
2019-03-28 15:54:07.881 22284-22621/com.example.threadpooltest D/Thread:: 29
2019-03-28 15:54:07.881 22284-22621/com.example.threadpooltest D/当前线程：: pool-1-thread-5

疑问：每个for循环里都有一个sleep（2000），为何会每隔2s打印三个任务？
原因：因为一开始的时候只是声明runnable对象并且重写run()方法，并没有运行，而后execute(runnable) 才会sleep，又因为一开始创建线程池的时候声明的核心线程数为3，
所以会首先开启三个核心线程，然后执行各自的run方法，虽然有先后顺序，但这之间的间隔很短，所以2s后同时打印3个任务。
 */

    /////////////////////////////////////线程中创建方式2：///////////////////////////////////////////////////////////////////////
    public void CreatThreadPoolByMethod2(){//2：FixedThreadPool (可重用固定线程数)
        for(int i = 0;i<30;i++){
            final int finali = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        Log.d("Thread", "run: "+finali);
                        Log.d("当前线程：",Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            fixedThreadPool.execute(runnable);
        }
    }
    //创建fixed线程池.特点：参数为核心线程数，只有核心线程，无非核心线程，并且阻塞队列无界。
    final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
/*
    结果为每2s打印5次任务，跟上面的基础线程池类似。
 */

    /////////////////////////////////////线程中创建方式3：///////////////////////////////////////////////////////////////////////
    public void CreatThreadPoolByMethod3(){//3：CachedThreadPool (按需创建)
        for(int i = 0;i<30;i++){
            final int finali = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        Log.d("Thread", "run: "+finali);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            cachedThreadPool.execute(runnable);

        }
    }
    //创建Cached线程池:特点：没有核心线程，只有非核心线程，并且每个非核心线程空闲等待的时间为60s，采用SynchronousQueue队列。
    final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
/*
结果：过2s后直接打印30个任务
结果分析：
因为没有核心线程，其他全为非核心线程，SynchronousQueue是不存储元素的，每次插入操作必须伴随一个移除操作，一个移除操作也要伴随一个插入操作。
当一个任务执行时，先用SynchronousQueue的offer提交任务，如果线程池中有线程空闲，则调用SynchronousQueue的poll方法来移除任务并交给线程处理；如果没有线程空闲，则开启一个新的非核心线程来处理任务。
由于maximumPoolSize是无界的，所以如果线程处理任务速度小于提交任务的速度，则会不断地创建新的线程，这时需要注意不要过度创建，应采取措施调整双方速度，不然线程创建太多会影响性能。
从其特点可以看出，CachedThreadPool适用于有大量需要立即执行的耗时少的任务的情况。
 */

    /////////////////////////////////////线程中创建方式：///////////////////////////////////////////////////////////////////////
    public void CreatThreadPoolByMethod4(){//4：SingleThreadPool(单个核线的fixed)
        for(int i = 0;i<30;i++){
            final int finali = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        Log.d("Thread", "run: "+finali);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            singleThreadExecutor.execute(runnable);
        }
    }

    //创建Single线程池
    final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
/*
结果：每2s打印一个任务，由于只有一个核心线程，当被占用时，其他的任务需要进入队列等待。
 */
    /////////////////////////////////////线程中创建方式：///////////////////////////////////////////////////////////////////////
    public void CreatThreadPoolByMethod5(){//5：ScheduledThreadPool(定时延时执行)
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Log.d("Thread", "This task is delayed to execute");
            }
        };
        scheduledThreadPool.schedule(runnable,10,TimeUnit.SECONDS);//延迟启动任务
        //scheduledThreadPool.scheduleAtFixedRate(runnable,5,1,TimeUnit.SECONDS);//延迟5s后启动，每1s执行一次
        //scheduledThreadPool.scheduleWithFixedDelay(runnable,5,1,TimeUnit.SECONDS);//启动后第一次延迟5s执行，后面延迟1s执行
    }
    //创建Scheduled线程池
    final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3);

    /////////////////////////////////////线程中创建方式：///////////////////////////////////////////////////////////////////////
    public void CreatThreadPoolByMethod6(){//6：自定义的PriorityThreadPool(队列中有优先级比较的线程池)
        for(int i = 0;i<30;i++){
            final int priority = i;
            priorityThreadPool.execute(new PriorityRunnable(priority) {
                @Override
                protected void doSomeThing() {
                    Log.d("MainActivity", "优先级为 "+priority+"  的任务被执行");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    //创建自定义线程池(优先级线程)
    final ExecutorService priorityThreadPool = new ThreadPoolExecutor(3,3,0, TimeUnit.SECONDS,new PriorityBlockingQueue<Runnable>());
    //利用抽象类继承Comparable接口重写其中的compareTo方法来比较优先级。
    public abstract class PriorityRunnable implements Runnable,Comparable<PriorityRunnable> {
        private int priority;

        public  PriorityRunnable(int priority){
            if(priority <0) {
                throw new IllegalArgumentException();
            }
            this.priority = priority;
        }
        public int getPriority() {
            return priority;
        }
        @Override
        public int compareTo(@NonNull PriorityRunnable another) {
            int me = this.priority;
            int anotherPri=another.getPriority();
            return me == anotherPri ? 0 : me < anotherPri ? 1 : -1;
        }
        @Override
        public void run() {
            doSomeThing();
        }
        protected abstract void doSomeThing();
    }
/*
结果：前三个任务被创建的三个核心线程执行，之后的27个任务进入队列并且调用compareTo方法进行排序，之后打印出来的是经过排序后从大到小的顺序。
 */






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
class Constants {
    public static int SUB1_2_MAIN = 0;
    public static int MAIN_2_SUB2 = 1;
    public static int SUB_2_SUB = 2;
}
