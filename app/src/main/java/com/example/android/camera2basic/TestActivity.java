package com.example.android.camera2basic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {
    private List<ImageView> list = new ArrayList<>();
    public static InnerClass innerClass = null;    // 非静态内部类的实例的引用，注：设置为静态，生命周期 = 应用App的生命周期、持有外部类TestActivity的引用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.helloworldid);
        textView.setText("Hello World! in TestActivity");

        // 保证非静态内部类的实例只有1个
        if (innerClass == null)
            innerClass = new InnerClass();
    }

    public void click(View view) {
        for (int i = 0; i < 10000; i++) {
            ImageView imageView = new ImageView(this);
            list.add(imageView);
        }
    }
    // 非静态内部类的定义
    private class InnerClass {
        void doSomeThing() {
            System.out.println("InnerClass!!!");
        }
    }
}
