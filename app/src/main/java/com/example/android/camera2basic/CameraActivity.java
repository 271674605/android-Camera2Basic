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

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        if(switchFunc == 2) {//camera2basic默认功能
            setContentView(R.layout.activity_camera);
            if (null == savedInstanceState) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance())
                        .commit();
            }
            CreatThreadByAllMethod();//创建线程所有方式
            CreatThreadPoolByAllMethod();//创建线程池所有方式
            CreatHandlerByAllMethod();//创建Handler所有方式
            CreatHandlerInThreadByAllMethod();//线程中创建handler所有方式
            testMainAndSubThreadSendMessage();//主线程和子线程相互通信
            CreatSynchronizedByAllMethod();//创建synchronized所有方式
            testSynchronizedWaitNotifyAll();//测试synchronized/wait/notifyAll：多线程
            testSubThreadCallMainThreadAll();//子线程调用主线程所有方式
            testMemoryLeakAll();//测试内存泄露
            printThreadInProcess();//打印当前进程的所有线程信息
        }else if(switchFunc == 1){//Service demo
            teststartService();//测试后台服务
        }else if(switchFunc == 0){//在hello world demo中测试单个测试项
            setContentView(R.layout.activity_main);
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
        CreatThreadByMethod8();//8、启用方式通过IntentService启动线程
        CreatThreadByMethod9();//9、Handler.Callback: Handler使用Callback接口
        CreatThreadByMethod10();//10、TimerTask开启线程
        CreatThreadByMethod11();//11、AsyncTask开启线程
    }
    /////////////////////////////////////创建线程方式1：CreatThreadByMethod1///////////////////////////////////////////////////////////////////////
    public void CreatThreadByMethod1(){//1、继承Thread类的实现：首先要写一个子线程类，去继承Thread类，重写run方法
        myCreatThread myThread = new myCreatThread();
        myThread.start();
        //等价于
        new myCreatThread().start();//调用 Thread 类中的 start()方法，实际上是调用 run()方法
    }
    class myCreatThread extends Thread{
        @Override
        public void run() {
            super.run();
            //干你需要做的操作
            Thread.currentThread().setName("bruce线程:myCreatThread");
            try{
                System.out.println("在 run()方法中 -  这个线程休眠 2 秒");
                Thread.sleep(2000);
                System.out.println("在 run()方法中 -  继续运行");
            }catch (InterruptedException x) {
                System.out.println("在 run()方法中 -  中断线程");
                return;
            }
            System.out.println("在 run()方法中 -  休眠之后继续完成");
            System.out.println("在 run()方法中 -  正常退出");
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
/*
可见，实现 Runnable 接口相对于继承 Thread 类来说，有如下显著的优势：
（1）、  适合多个相同程序代码的线程去处理同一资源的情况，把虚拟 CPU（线
程）同程序的代码、数据有效分离，较好地体现了面向对象的设计思想。
（2）、  可以避免由于 Java 的单继承特性带来的局限。开发中经常碰到这样一种
情况，即：当要将已经继承了某一个类的子类放入多线程中，由于一个
类不能同时有两个父类，所以不能用继承 Thread 类的方式，那么就只能
采用实现 Runnable 接口的方式了。
（3）、  增强了程序的健壮性，代码能够被多个线程共享，代码与数据是独立的。
当多个线程的执行代码来自同一个类的实例时，即称它们共享相同的代
码。多个线程可以操作相同的数据，与它们的代码无关。当共享访问相
同的对象时，即共享相同的数据。当线程被构造时，需要的代码和数据
通过一个对象作为构造函数实参传递进去，这个对象就是一个实现了
Runnable 接口的类的实例。
     事实上，几乎所有多线程应用都可用第二种方式，即实现 Runnable 接口。
 */
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
/*
1. 定义
一个Android 已封装好的轻量级异步类
2. 作用
实现多线程:在工作线程中执行任务，如 耗时任务
异步通信、消息传递: 实现工作线程 & 主线程（UI线程）之间的通信，即：将工作线程的执行结果传递给主线程，从而在主线程中执行相关的UI操作
3. 优点
方便实现异步通信，即不需使用 “任务线程（如继承Thread类） + Handler”的复杂组合.
实际上，HandlerThread本质上是通过继承Thread类和封装Handler类的使用，从而使得创建新线程和与其他线程进行通信变得更加方便易用
4. 工作原理
内部原理 = Thread类 + Handler类机制，即：
通过继承Thread类，快速地创建1个带有Looper对象的新工作线程
通过封装Handler类，快速创建Handler & 与其他线程进行通信
 */
    public void CreatThreadByMethod6(){//6、HandlerThread方式开启线程
        mMainHandlerCallback = new Handler(getMainLooper());
        bruceHandlerThread = new HandlerThread("ruce线程6:bruceHandlerThread");// 步骤1：创建HandlerThread实例对象
        bruceHandlerThread.start();// 步骤2：启动线程
        // 步骤3：创建工作线程Handler & 复写handleMessage（）
        mBruceHandlerThreadSub =  new bruceHandlerThreadSub(bruceHandlerThread.getLooper());//获取bruceHandlerThread的Loop，通过它创建bruceHandlerThread的Handler.
        mBruceHandlerThreadSub.sendEmptyMessage(EXPRESSION);// 步骤4：使用工作线程Handler向工作线程的消息队列发送消息
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
                    // 通过主线程Handler.post方法进行在主线程的UI更新操作
                    mMainHandlerCallback.post(new Runnable() {
                        @Override
                        public void run () {
                            //text.setText("我爱学习");
                        }
                    });

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
            bruceHandlerThread.quitSafely();// 步骤5：结束线程，即停止线程的消息循环
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
    public void CreatThreadByMethod8(){//8、启用方式通过IntentService启动线程
        Intent intent = new Intent(this, MyIntentService.class);
        Bundle bundle = new Bundle();
        bundle.putString("1","one");
        startService(intent);
    }
    class MyIntentService extends IntentService {
        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         * @param name Used to name the worker thread, important only for debugging.
         */
        public MyIntentService(String name) {
            //此处可以做点准备工作什么的
            super(name);
        }
        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            //此处已经在IntentService开启的内部线程处理了
            Log.d("IntentServiceTest", (String) intent.getExtras().get("1"));
            //此处怎么把处理好的消息返回给Ui线程呢？两种方法1：本地广播2：handler（此处不述）
        }
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
    /////////////////////////////////////测试解决handler内存泄漏方式：testHandlerMemoryLeakAll()//////////////////////////////////////////////////////////////////////

    public void testMemoryLeakAll() {
        testHandlerMemoryLeakAll();//测试Handler造成内存泄露
        testListMemoryLeakAll();//测试List集合类造成内存泄露
        testStaticMemoryLeakAll();//测试Static关键字修饰的成员变量造成内存泄露
        testInnerClassMemoryLeakAll();//测试非静态内部类 / 匿名类造成内存泄露
    }
    /////////////////////////////////////测试非静态内部类 / 匿名类造成内存泄露方式：testInnerClassMemoryLeakAll()//////////////////////////////////////////////////////////////////////
    /*
    非静态内部类 / 匿名类
储备知识：非静态内部类 / 匿名类 默认持有 外部类的引用；而静态内部类则不会
常见情况：3种，分别是：非静态内部类的实例 = 静态、多线程、消息传递机制（Handler）

    非静态内部类的实例 = 静态
泄露原因：若 非静态内部类所创建的实例 = 静态（其生命周期 = 应用的生命周期），会因 非静态内部类默认持有外部类的引用 而导致外部类无法释放，最终 造成内存泄露
     */
    public void testInnerClassMemoryLeakAll(){
        testInnerClassMemoryLeak1();//
    }

    // 造成内存泄露的原因：
    // a. 当TestActivity销毁时，因非静态内部类单例的引用（innerClass）的生命周期 = 应用App的生命周期、持有外部类TestActivity的引用
    // b. 故 TestActivity无法被GC回收，从而导致内存泄漏
    public static InnerClass innerClass = null;    // 非静态内部类的实例的引用，注：设置为静态，生命周期 = 应用App的生命周期、持有外部类TestActivity的引用
    public void testInnerClassMemoryLeak1(){//测试非静态内部类 / 匿名类造成内存泄露
        // 保证非静态内部类的实例只有1个
        if (innerClass == null)
            innerClass = new InnerClass();
    }

    public void testSolveInnerClassMemoryLeak1(){//解决非静态内部类 / 匿名类造成内存泄露
//        解决方案：
//        1：将非静态内部类设置为：静态内部类（静态内部类默认不持有外部类的引用）
             /*
                 // 非静态内部类的定义
                private static class InnerClass {
                    //...
                }
              */
//       :2：该内部类抽取出来封装成一个单例

//       :3：尽量 避免 非静态内部类所创建的实例 = 静态
             //定义为public InnerClass innerClass = null;  //非静态实例引用
    }
    // 非静态内部类的定义
    private class InnerClass {
        //...
    }
    /////////////////////////////////////测试解决Static关键字修饰的成员变量造成内存泄漏方式：testListMemoryLeakAll()//////////////////////////////////////////////////////////////////////
/*
储备知识:被 Static 关键字修饰的成员变量的生命周期 = 应用程序的生命周期
泄露原因:若使被 Static 关键字修饰的成员变量 引用耗费资源过多的实例（如Context），则容易出现该成员变量的生命周期 > 引用实例生命周期的情况，
当引用实例需结束生命周期销毁时，会因静态变量的持有而无法被回收，从而出现内存泄露
解决方案
a 尽量避免 Static 成员变量引用资源耗费过多的实例（如 Context）,若需引用 Context，则尽量使用Applicaiton的Context
b 使用 弱引用（WeakReference） 代替 强引用 持有实例
 */
    public void testStaticMemoryLeakAll(){
        testStaticMemoryLeak1();//注意：这里可能无测试效果，因为只有一个activity，退出后，Application也退出了，生命周期也就结束了。
    }
/*
注：静态成员变量有个非常典型的例子 = 单例模式
储备知识: 单例模式 由于其静态特性，其生命周期的长度 = 应用程序的生命周期
泄露原因: 若1个对象已不需再使用 而单例对象还持有该对象的引用，那么该对象将不能被正常回收 从而 导致内存泄漏
实例演示
 */
    public void testStaticMemoryLeak1(){//测试Static关键字修饰的成员变量造成内存泄露
        SingleInstanceClass mSingleInstanceClass = new SingleInstanceClass(CameraActivity.this);//CameraActivity Context
    }
/*
解决方案:单例模式引用的对象的生命周期 = 应用的生命周期
如上述实例，应传递Application的Context，因Application的生命周期 = 整个应用的生命周期
 */
    public void testSolveStaticMemoryLeak1(){//解决Static关键字修饰的成员变量造成内存泄露
        SolveSingleInstanceClass mSolveSingleInstanceClass = new SolveSingleInstanceClass(CameraActivity.this);//CameraActivity Context,再获取Application 的context
    }
    /////////////////////////////////////测试解决List集合类内存泄漏方式：testListMemoryLeakAll()//////////////////////////////////////////////////////////////////////
    public void testListMemoryLeakAll(){
        testListMemoryLeak1();
    }

    // 通过 循环申请Object 对象 & 将申请的对象逐个放入到集合List
    List<Object> objectList = new ArrayList<>();
    public void testListMemoryLeak1(){//测试List集合类内存泄漏
        for (int i = 0; i < 10; i++) {
            Object o = new Object();
            objectList.add(o);
            // 虽释放了集合元素引用的本身：o=null）
            // 但集合List 仍然引用该对象，故垃圾回收器GC 依然不可回收该对象
            o = null;
        }
    }
    public void testSolveListMemoryLeak1(){//解决List集合类内存泄漏
        // 释放objectList
        objectList.clear();
        objectList=null;
    }
    /////////////////////////////////////测试解决handler内存泄漏方式：testHandlerMemoryLeakAll()//////////////////////////////////////////////////////////////////////
    public void testHandlerMemoryLeakAll(){
        testHandlerMemoryLeak1();//测试handler内存泄漏方式1：//新建Handler子类（内部类）造成的内存泄漏
        testHandlerMemoryLeak2();//测试handler内存泄漏方式2：//匿名Handler内部类造成的内存泄漏
        testSolveHandlerMemoryLeak1();//解决handler内存泄漏方式1：静态内部类+ 弱引用
        testSolveHandlerMemoryLeak2();//解决handler内存泄漏方式2：当外部类结束生命周期时，清空Handler内消息队列
    }
    /*
    1. 问题描述: Handler的一般用法 = 新建Handler子类（内部类） 、匿名Handler内部类
        a：testHandlerMemoryLeak1()和testHandlerMemoryLeak2（)虽都可运行成功，但代码会出现严重警告：
        b：警告的原因 = 该Handler类由于无设置为 静态类，从而导致了内存泄露
        c：最终的内存泄露发生在Handler类的外部类：MainActivity类

    2. 原因讲解
    2.1 储备知识
        a：主线程的Looper对象的生命周期 = 该应用程序的生命周期
        b：在Java中，非静态内部类 & 匿名内部类都默认持有 外部类的引用
    2.2 泄露原因描述
        从上述示例代码可知：
    a：上述的Handler实例的消息队列有2个分别来自线程1、2的消息（分别为延迟1s、6s）
    b：在Handler消息队列 还有未处理的消息 / 正在处理消息时，消息队列中的Message持有Handler实例的引用
    c：由于Handler = 非静态内部类 / 匿名内部类（2种使用方式），故又默认持有外部类的引用（即CameraActivity实例），引用关系如下：
        Handler内的消息（Message）  --->（引用）   Handler实例（非静态内部类对象）   --->（默认引用）    Activity实例（外部类）
        上述的引用关系会一直保持，直到Handler消息队列中的所有消息被处理完毕
    d：在Handler消息队列 还有未处理的消息 / 正在处理消息时，此时若需销毁外部类CameraActivity，但由于上述引用关系，垃圾回收器（GC）无法回收CameraActivity，从而造成内存泄漏。如下：
       Handler内的消息（Message） --->（引用） Handler实例（非静态内部类对象）--->（默认引用） Activity实例（外部类） ） --->（引用关系保持） 需销毁外部类CameraActivity  --->  CameraActivity实例无法被回收    --->  造成内存泄漏

    2.3 总结
    当Handler消息队列 还有未处理的消息 / 正在处理消息时，存在引用关系： “未被处理 / 正处理的消息 -> Handler实例 -> 外部类”
    若出现 Handler的生命周期 > 外部类的生命周期 时（即 Handler消息队列 还有未处理的消息 / 正在处理消息 而 外部类需销毁时），将使得外部类无法被垃圾回收器（GC）回收，从而造成 内存泄露

    3. 解决方案
    从上面可看出，造成内存泄露的原因有2个关键条件：
        a：存在“未被处理 / 正处理的消息 -> Handler实例 -> 外部类” 的引用关系
        b：Handler的生命周期 > 外部类的生命周期
        即 Handler消息队列 还有未处理的消息 / 正在处理消息 而 外部类需销毁，解决方案的思路 = 使得上述任1条件不成立 即可。

    解决方案1：静态内部类+ 弱引用
    原理：静态内部类 不默认持有外部类的引用，从而使得 “未被处理 / 正处理的消息 -> Handler实例 -> 外部类” 的引用关系 的引用关系 不复存在。
    具体方案：将Handler的子类设置成 静态内部类。
        同时，还可加上 使用WeakReference弱引用持有Activity实例
        原因：弱引用的对象拥有短暂的生命周期。在垃圾回收器线程扫描时，一旦发现了只具有弱引用的对象，不管当前内存空间足够与否，都会回收它的内存
    解决代码：testSolveHandlerMemoryLeak1()

    解决方案2：当外部类结束生命周期时，清空Handler内消息队列
    原理：不仅使得 “未被处理 / 正处理的消息 -> Handler实例 -> 外部类” 的引用关系 不复存在，同时 使得 Handler的生命周期（即 消息存在的时期） 与 外部类的生命周期 同步
    具体方案：当 外部类（此处以Activity为例） 结束生命周期时（此时系统会调用onDestroy（）），清除 Handler消息队列里的所有消息（调用removeCallbacksAndMessages(null)）
    具体代码：testSolveHandlerMemoryLeak2()

    使用建议：为了保证Handler中消息队列中的所有消息都能被执行，此处推荐使用解决方案1解决内存泄露问题，即 静态内部类 + 弱引用的方式
     */
    /////////////////////////////////////测试handler内存泄漏：testHandlerMemoryLeak1()//////////////////////////////////////////////////////////////////////
    private Handler showhandler;
    public void testHandlerMemoryLeak1(){//新建Handler子类（内部类）造成的内存泄漏
        //1. 实例化自定义的Handler类对象->>分析1
        //注：此处并无指定Looper，故自动绑定当前线程(主线程)的Looper、MessageQueue.// 主线程创建时便自动创建Looper & 对应的MessageQueue之后执行Loop()进入消息循环
        showhandler = new FHandler();

        // 2. 启动子线程1
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 1;// 消息标识
                msg.obj = "AA";// 消息存放
                // b. 传入主线程的Handler & 向其MessageQueue发送消息
                showhandler.sendMessage(msg);
            }
        }.start();

        new Thread() {// 3. 启动子线程2
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 2;// 消息标识
                msg.obj = "BB";// 消息存放
                // b. 传入主线程的Handler & 向其MessageQueue发送消息
                showhandler.sendMessage(msg);
            }
        }.start();
    }
    class FHandler extends Handler {// 分析1：自定义Handler子类（非静态内部类），默认持有外部类的引用（即CameraActivity实例）
        // 通过复写handlerMessage() 从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(TAG, "收到线程1的消息");
                    break;
                case 2:
                    Log.d(TAG, " 收到线程2的消息");
                    break;


            }
        }
    }
    /////////////////////////////////////测试handler内存泄漏：testHandlerMemoryLeak2()//////////////////////////////////////////////////////////////////////
    public void testHandlerMemoryLeak2(){//匿名Handler内部类造成的内存泄漏
        //注：此处并无指定Looper，故自动绑定当前线程(主线程)的Looper、MessageQueue
        showhandler = new  Handler(){//1. 通过匿名内部类实例化的Handler类对象，默认持有外部类的引用（即CameraActivity实例）
            // 通过复写handlerMessage()从而确定更新UI的操作
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Log.d(TAG, "收到线程1的消息");
                        break;
                    case 2:
                        Log.d(TAG, " 收到线程2的消息");
                        break;
                }
            }
        };

        // 2. 启动子线程1
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 1;// 消息标识
                msg.obj = "AA";// 消息存放
                // b. 传入主线程的Handler & 向其MessageQueue发送消息
                showhandler.sendMessage(msg);
            }
        }.start();

        // 3. 启动子线程2
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 2;// 消息标识
                msg.obj = "BB";// 消息存放
                // b. 传入主线程的Handler & 向其MessageQueue发送消息
                showhandler.sendMessage(msg);
            }
        }.start();
    }
    /////////////////////////////////////解决handler内存泄漏方式1：testSolveHandlerMemoryLeak1()//////////////////////////////////////////////////////////////////////
    public static final String TAG1 = "carson：";
    private Handler showhandlerByStaticClass;
    public void testSolveHandlerMemoryLeak1(){
        //1. 实例化自定义的Handler类对象->>分析1
        //注：
        // a. 此处并无指定Looper，故自动绑定当前线程(主线程)的Looper、MessageQueue；
        // b. 定义时需传入持有的Activity实例（弱引用）
        showhandlerByStaticClass = new FHandlerByStaticClass(this);

        // 2. 启动子线程1
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 1;// 消息标识
                msg.obj = "AA";// 消息存放
                // b. 传入主线程的Handler & 向其MessageQueue发送消息
                showhandlerByStaticClass.sendMessage(msg);
            }
        }.start();

        // 3. 启动子线程2
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // a. 定义要发送的消息
                Message msg = Message.obtain();
                msg.what = 2;// 消息标识
                msg.obj = "BB";// 消息存放
                // b. 传入主线程的Handler & 向其MessageQueue发送消息
                showhandlerByStaticClass.sendMessage(msg);
            }
        }.start();
    }
    // 分析1：自定义Handler子类
    // 设置为：静态内部类
    private static class FHandlerByStaticClass extends Handler{
        // 定义 弱引用实例
        private WeakReference<Activity> reference;

        // 在构造方法中传入需持有的Activity实例
        public FHandlerByStaticClass(Activity activity) {
            // 使用WeakReference弱引用持有Activity实例
            reference = new WeakReference<Activity>(activity); }

        // 通过复写handlerMessage() 从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(TAG1, "收到线程1的消息");
                    break;
                case 2:
                    Log.d(TAG1, " 收到线程2的消息");
                    break;


            }
        }
    }
    /////////////////////////////////////解决handler内存泄漏方式2：testSolveHandlerMemoryLeak2()//////////////////////////////////////////////////////////////////////
    public void testSolveHandlerMemoryLeak2(){
        showhandler.removeCallbacksAndMessages(null);// 外部类Activity生命周期结束onDestroy()时，同时清空消息队列 & 结束Handler生命周期
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

    /////////////////////////////////////创建synchronized所有方式：CreatSynchronizedByAllMethod()//////////////////////////////////////////////////////////////////////
/*
Java中关于多线程中使用的一些关键字和一些方法的作用
关键字  作用
volatile线程操作变量可见： 保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的。volatile关键字会强制将修改的值立即写入主存，使线程的工作内存中缓存变量行无效。
Lock  Java6.0增加的线程同步锁
synchronized线程同步锁
wait()让该线程处于等待状态
notify()唤醒一个正在wait该对象的线程，只是通知一个线程（至于是哪个线程就看JVM了）
notifyAll()唤醒所有正在wait该对象的线程。
sleep()线程休眠
join()使当线程处于阻塞状态，让指定的线程先执行完再执行其他线程，而且会阻塞主线程
yield()让出该线程的时间片给其他线程
注意：
1. wait()、notify()、notifyAll()都必须在synchronized中执行，否则会抛出异常
2. wait()、notify()、notifyAll()都是属于超类Object的方法
2. 一个对象只有一个锁（对象锁和类锁还是有区别的）

synchronized可以用在方法上也可以使用在代码块中，其中方法是实例方法和静态方法分别锁的是该类的实例对象和该类的对象。而使用在代码块中也可以分为三种，具体的可以看上面的表格。
这里的需要注意的是：如果锁的是类对象的话，尽管new多个实例对象，但他们仍然是属于同一个类依然会被锁住，即线程之间保证同步关系。
synchronized修饰方法和修饰一个代码块类似，只是作用范围不一样，修饰代码块是大括号括起来的范围，而修饰方法范围是整个函数。
 */
    public void CreatSynchronizedByAllMethod(){
        CreatSynchronizedByMethod1();
        CreatSynchronizedByMethod2();
        CreatSynchronizedByMethod3();
        CreatSynchronizedByMethod4();
        CreatSynchronizedByMethod5();
    }
    /////////////////////////////////////创建synchronized方式1：///////////////////////////////////////////////////////////////////////
    //锁定了整个方法时的内容。在调用该方法前，需要获得内置锁，否则就处于阻塞状态。等价于CreatSynchronizedByMethod2()。
    //注：同步是一种高开销的操作，因此应该尽量减少同步的内容。通常没有必要同步整个方法，使用synchronized代码块同步关键代码即可。
    public synchronized void CreatSynchronizedByMethod1() {//实例方法，锁住的是该类的实例对象,

    }
    /////////////////////////////////////创建synchronized方式2：///////////////////////////////////////////////////////////////////////
    public void CreatSynchronizedByMethod2() {//同步代码块，锁住的是该类的实例对象,
        synchronized (this){    // 这里用的this，则锁定了整个方法时的内容。等价于CreatSynchronizedByMethod1()。

        }
    }
    /////////////////////////////////////创建synchronized方式3：///////////////////////////////////////////////////////////////////////
    public static synchronized void CreatSynchronizedByMethod3() {//静态方法，锁住的是类对象，即将会锁住整个类的所有对象。

    }
    /////////////////////////////////////创建synchronized方式4：///////////////////////////////////////////////////////////////////////
    private static int count = 0;
    public void CreatSynchronizedByMethod4() {//同步代码块，锁住的是该类的类对象，即将会锁住整个类的所有对象。
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new CreatSynchronizedByMethod4Runnable());//开启十个线程
            thread.start();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("result: " + count);
    }
    class CreatSynchronizedByMethod4Runnable implements Runnable{
        @Override
        public void run() {
            synchronized (CameraActivity.class) {//同步代码块，锁住的是该类的类对象，即将会锁住整个类的所有对象。
                for (int i = 0; i < 1000; i++)
                    count++;
            }
        }
    }
    /*
  开启十个线程，每个线程在原值上累加1000次，最终正确的结果为10X1000=10000，这里能够计算出正确的结果是因为在做累加操作时使用了同步代码块，
  这样就能保证每个线程所获得共享变量的值都是当前最新的值，如果不使用同步的话，就可能会出现A线程累加后，而B线程做累加操作有可能是使用原来的就值，即“脏值”。
  这样，就导致最终的计算结果不是正确的。而使用Syncnized就可能保证内存可见性，保证每个线程都是操作的最新值。
  当两个并发线程(thread1和thread2)访问同一个对象(syncThread)中的synchronized代码块时，在同一时刻只能有一个线程得到执行，另一个线程受阻塞，
  必须等待当前线程执行完这个代码块以后才能执行该代码块。Thread1和thread2是互斥的，因为在执行synchronized代码块时会锁定当前的对象，
  只有执行完该代码块才能释放该对象锁，下一个线程才能执行并锁定该对象。

     */
    /////////////////////////////////////创建synchronized方式5：///////////////////////////////////////////////////////////////////////
/*
1）一个线程访问一个对象中的synchronized(this)同步代码块时，其他试图访问该对象的线程将被阻塞
2）当一个线程访问对象的一个synchronized(this)同步代码块时，另一个线程仍然可以访问该对象中的非synchronized(this)同步代码块。
 */
    final Object mObject = new Object();
    public void CreatSynchronizedByMethod5() {//同步代码块，锁住的是配置的实例对象mObject
        synchronized (mObject){ // 锁定了synchronized{}整个大括号里的内容。

        }
    }
    /////////////////////////////////////测试synchronized/wait/notifyAll：多线程///////////////////////////////////////////////////////////////////////
    public void testSynchronizedWaitNotifyAll(){
        testSynchronizedWaitNotify();//测试多线程同步 生产者—消费者 通用模式
        testSynchronizedyDeadLock();//测试多线程死锁
        testSynchronizedyByTwoThread1();//测试双线程同步：互斥
        testSynchronizedyByTwoThread2();//测试双线程同步：并行
    }

    /////////////////////////////////////测试synchronized/wait/notifyAll：多线程 生产者—消费者 通用模式///////////////////////////////////////////////////////////////////////
/*
 //生产者——消费者 通用模式
// 1、while（）
// 2、notifyAll（）
    wait、notify、notifyAll 这三个方法只能在 synchronized 方法中调用，即无论线程调用一个对象的wait还是notify方法，该线程必须先得到该对象的锁标记，这样， notify
只能唤醒同一对象监视器中调用 wait 的线程，使用多个对象监视器，就可以分别有多个 wait、notify的情况，同组里的 wait 只能被同组的 notify唤醒。
//理解思路：
// 1、标记flag，理解为: 仓库里有产品，则Producer不再生产改变标记标记flag。在不再生产前，唤醒消费线程Consumer。
// 2、生产的线程Producer则因为循环，执行等待。
// 3、等待的线程被唤醒后，第一件事是重新判断标记flag，是否仓库有货，即while循环的条件判断，如没货才可以生产，有货则消费。
 */
    public void testSynchronizedWaitNotify(){//ProducerConsumerDemo:多线程 生产者—消费者 通用模式
        Resource r = new Resource();
        Producer mProducer = new Producer(r);
        Consumer mConsumer = new Consumer(r);
        //多个生产者消费者时：
        new Thread(mProducer).start();//启动第一个Producer线程pA
        new Thread(mProducer).start();//启动第二个Producer线程pB
        new Thread(mConsumer).start();//启动第一个Consumer线程cA
        new Thread(mConsumer).start();//启动第二个Consumer线程cB
    }
    class Resource { //生产者&&消费者公共资源仓库
        private String name;
        private int count = 0;
        private boolean flag = false;
        public synchronized void produce(String name) {//锁住produce方法
            while (flag) {//flag = true，表示仓库有产品。一直循环到consume()消费者线程将flag = false。
                try {
                    wait();//pA阻塞，后来的线程要等待，即第二个Producer线程pB将无法执行同步方法produce
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.name = name + (++count);
            System.out.println("生产：" + this.name);
            flag = !flag;
            notifyAll();//唤醒最先到达的线程，即上面while循环一直到consume()消费者线程将flag = false，此时notifyAll所有Producer线程pA和pB,进一步生产。
        }
        public synchronized void consume() {//锁住consume方法
            while (!flag) {//flag = false，表示仓库没有产品。一直循环到produce()消费者线程将flag = true。
                try {
                    wait();//cA阻塞，后来的线程要等待，即第二个Consumer线程cB将无法执行同步方法consume
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("...........消费：" + name);
            flag = !flag;
            notifyAll();//唤醒最先到达的线程，即上面while循环一直到produce()生产者线程将flag = false，此时notifyAll所有Consumer线程cA和cB,进一步消费。
        }
    }
    class Producer implements Runnable {
        private Resource r;
        public Producer(Resource r) {
            this.r = r;
        }
        public void run() {
            while (true) {//循环生产
                r.produce("商品");
            }
        }
    }
    class Consumer implements Runnable {
        private Resource r;
        public Consumer(Resource r) {
            this.r = r;
        }
        public void run() {
            while (true) {//循环消费
                r.consume();
            }
        }
    }
/*
输出：
生产：商品
...........消费：商品
生产：商品
...........消费：商品
生产：商品
...........消费：商品
实际要求的结果是，Producer放一次数据，Consumer就取一次；反之，Producer也必须等到 Consumer 取完后才能放入新的数据，而这一问题的解决就
需要使用下面所要讲到的线程间的通信。 Java 是通过 Object 类的 wait、 notify、 notifyAll这几个方法来实现线程间的通信的，
又因为所有的类都是从 Object 继承的，所以任何类都可以直接使用这些方法。下面是这三个方法的简要说明：
    wait：告诉当前线程放弃监视器并进入睡眠状态，直到其它线程进入同一监视器并调用 notify为止。
    notify：唤醒同一对象监视器中调用 wait 的第一个线程。类似排队买票，一个人买完之后，后面的人可以继续买。
notifyAll：唤醒同一对象监视器中调用 wait 的所有线程，具有最高优先级的线程首先被唤醒并执行。

如果想符合预先的设计需求，必须在类 P 中定义一个新的成员变量flag来表示数据存储空间的状态，当 Consumer 线程取走数据后，flag值为 false，当
Producer线程放入数据后，flag 值为 true。只有 flag 为true 时，Consumer 线程才能取走数据，否则就必须等待 Producer 线程放入新的数据后的通知；反之，只有 flag
为 false，Producer 线程才能放入新的数据，否则就必须等待 Consumer 线程取走数据后的通知。
 */
    /////////////////////////////////////测试synchronized/wait/notifyAll：多线程 死锁///////////////////////////////////////////////////////////////////////
    public void testSynchronizedyDeadLock(){//DeadLockDemo:多线程 死锁
        new TaskA().start();
        new TaskB().start();
    }
    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    private static class TaskA extends Thread {
        @Override
        public void run() {
            try {
                synchronized (LOCK_A) {//TaskA持有LOCK_A
                    System.out.println(Thread.currentThread() + "I hold the LOCK_A");
                    Thread.sleep(5000);
                    System.out.println(Thread.currentThread() + "I am wake up and try to get lock");
                    synchronized (LOCK_B) {//TaskB已经持有LOCK_B，因此这个同步代码块无法执行
                        System.out.println(Thread.currentThread() + "I get the LOCK_B");
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private static class TaskB extends Thread {
        @Override
        public void run() {
            try {
                synchronized (LOCK_B) {//TaskB持有LOCK_B
                    System.out.println(Thread.currentThread() + "I hold the LOCK_B");
                    Thread.sleep(5000);
                    System.out.println(Thread.currentThread() + "I am wake up and try to get lock");
                    synchronized (LOCK_A) {//TaskA已经持有LOCK_A，因此这个同步代码块无法执行
                        System.out.println(Thread.currentThread() + "I get the LOCK_A");
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
/*输出：两个线程都因为获取不到指定锁而阻塞
2019-05-14 14:19:12.322 5219-5313/com.example.android.camera2basic I/System.out: Thread[Thread-32,5,main]I hold the LOCK_B
2019-05-14 14:19:12.322 5219-5312/com.example.android.camera2basic I/System.out: Thread[Thread-31,5,main]I hold the LOCK_A
2019-05-14 14:19:17.322 5219-5313/com.example.android.camera2basic I/System.out: Thread[Thread-32,5,main]I am wake up and try to get lock
2019-05-14 14:19:17.323 5219-5312/com.example.android.camera2basic I/System.out: Thread[Thread-31,5,main]I am wake up and try to get lock
 */
    /////////////////////////////////////测试synchronized/wait/notifyAll：双线程同步：互斥阻塞///////////////////////////////////////////////////////////////////////
/*
当两个并发线程(thread1和thread2)访问同一个对象s(syncThread)中的synchronized代码块时，在同一时刻只能有一个线程得到执行，
另一个线程受阻塞，必须等待当前线程执行完这个代码块以后才能执行该代码块。Thread1和thread2是互斥的，
因为在执行synchronized代码块时会锁定当前的对象，只有执行完该代码块才能释放该对象锁，下一个线程才能执行并锁定该对象
 */
    public void testSynchronizedyByTwoThread1(){//测试双线程同步：互斥
        SyncThread s = new SyncThread();
        Thread t1 = new Thread(s);//使用同一个对象s，synchronized(this)时将阻塞。
        Thread t2 = new Thread(s);
        t1.start();
        t2.start();
    }
    /////////////////////////////////////测试synchronized/wait/notifyAll：双线程同步：并行///////////////////////////////////////////////////////////////////////
/*
当两个并发线程(thread1和thread2)分别访问两个对象s1和s2(syncThread)中的synchronized代码块时，在同一时刻能有两个线程得到执行，线程并没有受阻塞，
 */
    public void testSynchronizedyByTwoThread2(){//测试双线程同步：并行
        SyncThread s1 = new SyncThread();
        SyncThread s2 = new SyncThread();
        Thread t1 = new Thread(s1);//使用不同的两个对象s，synchronized(this)时并不会阻塞。
        Thread t2 = new Thread(s2);
        t1.start();
        t2.start();
    }
    private static int countSyncThread;
    class SyncThread implements Runnable {
        public SyncThread() {
            countSyncThread = 0;
        }
        public  void run() {
            synchronized(this) {
                for (int i = 0; i < 5; i++) {
                    try {
                        System.out.println(Thread.currentThread().getName() + ":" + (countSyncThread++));
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /////////////////////////////////////打印当前进程的所有线程信息///////////////////////////////////////////////////////////////////////
    private void printThreadInProcess() {//打印当前进程的所有线程信息
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        Set<Thread> set = stacks.keySet();
        for (Thread key : set) {
            StackTraceElement[] stackTraceElements = stacks.get(key);
            Log.d(TAG, "---- print thread: " + key.getName() + " start ----");
            for (StackTraceElement st : stackTraceElements) {
                Log.d(TAG, "StackTraceElement: " + st.toString());
            }
            Log.d(TAG, "---- print thread: " + key.getName() + " end ----");
        }
    }

    /////////////////////////////////////子线程调用主线程所有方式///////////////////////////////////////////////////////////////////////
    public void testSubThreadCallMainThreadAll(){
        Context context = null;
        //testSubThreadCallMainThread1(context);//context null
        //testSubThreadCallMainThread2();//textView null
        //testSubThreadCallMainThread3();//环境不在子线程
    }
    /////////////////////////////////////子线程调用主线程方式1:activity.runOnUiThread(Runnable action)//////////////////////////////////////////////////////////////////////
    /*
    这是我认为第二简单的方法了，一般我的上下文（context）是大部分类都会传到的，而这个 context 其实就是我的 MainActivity，
    我会直接强制转换成 Activity 然后用activity.runOnUiThread(Runnable action)方法进行更新UI。
    如果没有上下文（context）怎么办？用view.getContext()可以得到上下文（不过你为什么不直接用方法一呢？）
    跳过context直接用new Activity().runOnUiThread(Runnable action)来切换到主线程。
     */
    /*假设该更新方法在子线程中运行 * @param context 上下文 */
    public void testSubThreadCallMainThread1(final Context context){//2,activity.runOnUiThread(Runnable action)
        ((CameraActivity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //已在主线程中，可以更新UI
            }
        });
    }
    /////////////////////////////////////子线程调用主线程方式2:view.post(Runnable action)///////////////////////////////////////////////////////////////////////
    /*
    这是view自带的方法，比较简单，如果你的子线程里可以得到要更新的view的话，可以用此方法进行更新。
    view还有一个方法view.postDelayed(Runnable action, long delayMillis)用来延迟发送。
     */
    public void testSubThreadCallMainThread2(){//2.view.post(Runnable action)
        final TextView textView = null;
        textView.post(new Runnable() {
            @Override public void run() {
                textView.setText("更新啦！");
                //还可以更新其他的控件
            }
        });

    }
    /////////////////////////////////////子线程调用主线程方式3:Handler mainHandler = new Handler()///////////////////////////////////////////////////////////////////////
    /*
Handler 是最常用也是比上面稍微复杂一点的方法。
首先在主线程中定义Handler，Handler mainHandler = new Handler();（必须要在主线程中定义才能操作主线程，如果想在其他地方定义声明时要这样写
Handler mainHandler = new Handler(Looper.getMainLooper())，来获取主线程的 Looper 和 Queue ）
获取到 Handler 后就很简单了，用handler.post(Runnable r)方法把消息处理放在该 handler 依附的消息队列中（也就是主线程消息队列），
这也是为什么我们第一步一定要获取主线程的 handler，如果在子线程中直接声明 handler，调用handler.post(Runnable r)其实还是在子线程中调用
     */
    public void testSubThreadCallMainThread3(){//3.Handler mainHandler = new Handler()
            //假设已在子线程
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override public void run() {
                //已在主线程中，可以更新UI
            }
        });
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
class Constants {
    public static int SUB1_2_MAIN = 0;
    public static int MAIN_2_SUB2 = 1;
    public static int SUB_2_SUB = 2;
}
// 创建单例时，需传入一个Context
// 若传入的是Activity的Context，此时单例 则持有该Activity的引用
// 由于单例一直持有该Activity的引用（直到整个应用生命周期结束），即使该Activity退出，该Activity的内存也不会被回收
// 特别是一些庞大的Activity，此处非常容易导致OOM
class SingleInstanceClass {
    private static SingleInstanceClass instance;
    private Context mContext;
    public SingleInstanceClass(Context context) {
        this.mContext = context; // 传递的是Activity的context
    }
    public SingleInstanceClass getInstance(Context context) {
        if (instance == null) {
            instance = new SingleInstanceClass(context);
        }
        return instance;
    }
}
//单例模式引用的对象的生命周期 = 应用的生命周期
//如上述实例，应传递Application的Context，因Application的生命周期 = 整个应用的生命周期
class SolveSingleInstanceClass {
    private static SolveSingleInstanceClass instance;
    private Context mContext;
    public SolveSingleInstanceClass(Context context) {
        this.mContext = context.getApplicationContext(); // 传递的是Application 的context
    }

    public SolveSingleInstanceClass getInstance(Context context) {
        if (instance == null) {
            instance = new SolveSingleInstanceClass(context);
        }
        return instance;
    }
}