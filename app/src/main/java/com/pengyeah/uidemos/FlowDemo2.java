package com.pengyeah.uidemos;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.pengyeah.flowview.FlowGuideView;
import com.pengyeah.flowview.FlowView;

import static com.pengyeah.flowview.ConstantKt.STATE_EXPANDED;
import static com.pengyeah.flowview.ConstantKt.STATE_MOVING;

public class FlowDemo2 extends AppCompatActivity {

    FlowView fv1, fv2;

    FlowGuideView fgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_demo2);

        fv1 = findViewById(R.id.fv1);
        fv2 = findViewById(R.id.fv2);

        fv1.setOnStateChangedListener(new FlowView.OnStateChangedListener() {
            @Override
            public void onStateChanged(int state) {
                if (state == STATE_EXPANDED) {
                    fv2.showWithAnim();
                } else if (state == STATE_MOVING) {
                    if (fv2.getVisibility() == View.VISIBLE) {
                        fv2.setVisibility(View.GONE);
                    }
                } else {
                    fv2.setVisibility(View.GONE);
                }
            }
        });

        fv2.setOnStateChangedListener(new FlowView.OnStateChangedListener() {
            @Override
            public void onStateChanged(int state) {
                if (state == STATE_EXPANDED) {

                } else if (state == STATE_MOVING) {

                } else {

                }
            }
        });

        fgv = findViewById(R.id.fgv);
        fgv.addGuides(R.mipmap.png_app_guide1, R.mipmap.png_app_guide2,R.mipmap.app_guide3);
    }
}
