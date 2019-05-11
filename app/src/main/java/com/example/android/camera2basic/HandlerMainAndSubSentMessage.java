package com.example.android.camera2basic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

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

class Constants {
	public static int SUB1_2_MAIN = 0;
	public static int MAIN_2_SUB2 = 1;
	public static int SUB_2_SUB = 2;
}