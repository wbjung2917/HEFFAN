package org.tensorflow.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.Html;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import androidx.core.view.ViewCompat;


public class ResultActivity extends Activity {
    String [][] arr={{"Sodium","10mcg","50%"},{"Protein","20g","90%"},{"Vitamin","10mg","100%"},{"Sodium","10mcg","50%"},{"Protein","20g","90%"},{"Vitamin","10mg","100%"},{"Sodium","10mcg","50%"},{"Protein","20g","90%"},{"Vitamin","10mg","100%"},{"Sodium","10mcg","50%"},{"Protein","20g","90%"},{"Vitamin","10mg","100%"}};
    public void makeCards(String[][] arr){
        int i,j=0,id=0;
        ConstraintLayout scroll_view=findViewById(R.id.scrollview_layout);
        for(i=0;i<arr.length;i++)
        {
            MaterialCardView cardView=new MaterialCardView(this);
            ConstraintLayout cl=new ConstraintLayout(this);

            cardView.setId(ViewCompat.generateViewId());
            ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(1200,220);
            params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            params.rightToRight=ConstraintLayout.LayoutParams.PARENT_ID;
            if(j!=0)
            {
                params.topToBottom=id;
            }
            params.setMargins(20,20,20,20);
            cardView.setLayoutParams(params);

            TextView textView1=new TextView(this);
            ConstraintLayout.LayoutParams params1=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params1.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            params1.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
            params1.setMargins(20,5,20,5);
            textView1.setTextColor(getResources().getColor(R.color.colorPrimary));
            textView1.setText(arr[i][0]);
            textView1.setLayoutParams(params1);
            textView1.setTextSize(24.0F);
            cl.addView(textView1);

            TextView textView2=new TextView(this);
            ConstraintLayout.LayoutParams params2=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params2.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            params2.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
            params2.setMargins(20,5,20,5);
            textView2.setText(arr[i][1]);
            textView2.setLayoutParams(params2);
            textView2.setTextSize(20.0F);
            cl.addView(textView2);

            TextView textView3=new TextView(this);
            ConstraintLayout.LayoutParams params3=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params3.rightToRight=ConstraintLayout.LayoutParams.PARENT_ID;
            params3.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
            params3.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
            params3.setMargins(20,10,20,10);
            textView3.setTextColor(getResources().getColor(R.color.colorPrimary));
            textView3.setText(Html.fromHtml(arr[i][2]));
            textView3.setLayoutParams(params3);
            textView3.setTextSize(36.0F);
            cl.addView(textView3);

            j=1;
            id=cardView.getId();
            cardView.addView(cl);
            scroll_view.addView(cardView);
        }
    }
    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_result);
        makeCards(arr);
    }
}
