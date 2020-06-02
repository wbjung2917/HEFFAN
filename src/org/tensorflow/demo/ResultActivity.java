package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import androidx.core.view.ViewCompat;

import static org.tensorflow.demo.HEFFAN_filter.getFilterResults;


public class ResultActivity extends Activity {
    public void makeCards(ArrayList<ArrayList<String>> arr){
        int i,j=0,id=0;
        ConstraintLayout scroll_view=findViewById(R.id.scrollview_layout);
        for(i=0;i<arr.size();i++)
        {
            MaterialCardView cardView=new MaterialCardView(this);
            ConstraintLayout cl=new ConstraintLayout(this);

            cardView.setId(ViewCompat.generateViewId());
            ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
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
            textView1.setText(arr.get(i).get(0));
            textView1.setLayoutParams(params1);
            textView1.setTextSize(24.0F);
            cl.addView(textView1);

            TextView textView2=new TextView(this);
            ConstraintLayout.LayoutParams params2=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params2.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
            params2.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
            params2.setMargins(20,5,20,5);
            textView2.setText(arr.get(i).get(1));
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
            textView3.setText(Html.fromHtml(arr.get(i).get(2)));
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
        Intent intent =getIntent();
        ArrayList<ArrayList<String>> arr=new ArrayList<>();
        arr= (ArrayList<ArrayList<String>>) intent.getSerializableExtra("delivered_arraylist");
        /*
        Log.d("InResult",arr.get(0).get(0));
        Log.d("InResult",arr.get(0).get(1));
        Log.d("InResult",arr.get(0).get(2));
        Log.d("InResultLength",Integer.toString(arr.size()));
        */
        /*sample
        ArrayList<String> a1=new ArrayList<>();
        a1.add("Sodium");
        a1.add("10mcg");
        a1.add("50%");
        ArrayList<String> a2=new ArrayList<>();
        a2.add("Protein");
        a2.add("20g");
        a2.add("90%");
        ArrayList<String> a3=new ArrayList<>();
        a3.add("Vitamin");
        a3.add("10mg");
        a3.add("100%");
        arr.add(a1);
        arr.add(a2);
        arr.add(a3);
        */
        //{"Sodium","10mcg","50%"},{"Protein","20g","90%"},{"Vitamin","10mg","100%"}
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_result);
        makeCards(arr);
    }
}
