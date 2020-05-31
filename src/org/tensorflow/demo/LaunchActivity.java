package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LaunchActivity extends Activity {
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_launch);

        Handler handler= new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                startActivity(new Intent(LaunchActivity.this,StartActivity.class));

            }
        };
        handler.sendEmptyMessageDelayed(0,3000);
    }
}
