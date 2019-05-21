package com.example.android.camera2basic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.helloworldid);
        textView.setText("Hello World! in TestActivity");
    }

    public void click(View view) {
        testListMemoryLeak1();
        testSolveListMemoryLeak1();
    }

    // 通过 循环申请Object 对象 & 将申请的对象逐个放入到集合List
    List<Object> bruceListMemoryLeak = new ArrayList<>();
    public void testListMemoryLeak1(){//测试List集合类内存泄漏
        for (int i = 0; i < 100000; i++) {
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
}
