package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends Activity{
    Button btn;
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_start);
        btn=findViewById(R.id.btn_start);

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent detect=new Intent(getApplicationContext(),org.tensorflow.demo.DetectorActivity.class);       //Uri 를 이용하여 웹브라우저를 통해 웹페이지로 이동하는 기능
                startActivity((detect));
            }
        });
    }
}
