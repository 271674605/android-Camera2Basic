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