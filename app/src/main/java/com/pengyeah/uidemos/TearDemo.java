package com.pengyeah.uidemos;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.pengyeah.tear.PaperLayout;

public class TearDemo extends AppCompatActivity {

    public PaperLayout paperLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tear_demo);

        paperLayout = findViewById(R.id.paperLayout);
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paperLayout.startTearAnim();
            }
        });
    }
}
