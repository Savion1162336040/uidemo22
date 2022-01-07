package com.pengyeah.uidemos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.pengyeah.card3d.CusView;
import com.pengyeah.card3d.NumCardJavaView;

public class Card3DDemo extends AppCompatActivity {
    NumCardJavaView numCardJavaView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card3_d_demo);
        final CusView cusView = findViewById(R.id.curview);
        SeekBar seekBar = findViewById(R.id.seekbar);
        numCardJavaView = findViewById(R.id.card3d_java);
        AppCompatButton autoPlay = findViewById(R.id.autoplay);
        autoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numCardJavaView.autoPlay(1000,1000);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rotate = 360 * (progress/100f);
                cusView.setRotate(rotate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        numCardJavaView.onDestory();
        super.onDestroy();
    }
}
