package com.example.android.camera2basic;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.android.camera2basic.CameraActivity;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    public static InnerClass innerClass = null;    // 非静态内部类的实例的引用，注：设置为静态，生命周期 = 应用App的生命周期、持有外部类TestActivity的引用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textViewhelloworldid = (TextView) findViewById(R.id.helloworldid);
        textViewhelloworldid.setText("Hello World! in TestActivity");
        // 保证非静态内部类的实例只有1个
        if (innerClass == null)
            innerClass = new InnerClass();
    }
    public void click(View view) {
        testListMemoryLeak1();
    }
    // 通过 循环申请Object 对象 & 将申请的对象逐个放入到集合List
    List<Object> bruceListMemoryLeak = new ArrayList<>();
    public void testListMemoryLeak1(){//测试List集合类内存泄漏
        for (int i = 0; i < 1000000; i++) {
            Object o = new Object();
            bruceListMemoryLeak.add(o);
            // 虽释放了集合元素引用的本身：o=null）
            // 但集合List 仍然引用该对象，故垃圾回收器GC 依然不可回收该对象
            o = null;
        }
    }
    public void testSolveListMemoryLeak1(){//解决List集合类内存泄漏
        // 释放objectList
        bruceListMemoryLeak.clear();
        bruceListMemoryLeak=null;
    }
    // 非静态内部类的定义
    private class InnerClass {
        void doSomeThing() {
            System.out.println("InnerClass!!!");
        }
    }
}
