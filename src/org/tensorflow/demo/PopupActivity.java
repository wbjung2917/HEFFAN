package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

public class PopupActivity extends Activity{

    public ArrayList<ArrayList<ArrayList<String>>> makeStandardArray()
    {
        ArrayList<ArrayList<ArrayList<String>>> standard=new ArrayList<>();
        ArrayList<ArrayList<String>> male=new ArrayList<>();
        ArrayList<ArrayList<String>> female=new ArrayList<>();

        ArrayList<String> m0=new ArrayList<>();//Sodium
        m0.add("110mg");
        m0.add("370mg");
        m0.add("800mg");
        m0.add("1000mg");
        m0.add("1200mg");
        m0.add("1500mg");
        m0.add("1500mg");
        m0.add("1500mg");
        m0.add("1500mg");
        m0.add("1500mg");
        ArrayList<String> m1=new ArrayList<>();//Vitamin C
        m1.add("40mg");
        m1.add("50mg");
        m1.add("15mg");
        m1.add("25mg");
        m1.add("45mg");
        m1.add("75mg");
        m1.add("90mg");
        m1.add("90mg");
        m1.add("90mg");
        m1.add("90mg");
        ArrayList<String> m2=new ArrayList<>();//Protein
        m2.add("9g");
        m2.add("11g");
        m2.add("13g");
        m2.add("19g");
        m2.add("34g");
        m2.add("52g");
        m2.add("56g");
        m2.add("56g");
        m2.add("56g");
        m2.add("56g");
        ArrayList<String> m3=new ArrayList<>();//Total Carbohydrate
        m3.add("60g");
        m3.add("95g");
        m3.add("130g");
        m3.add("130g");
        m3.add("130g");
        m3.add("130g");
        m3.add("130g");
        m3.add("130g");
        m3.add("130g");
        m3.add("130g");
        ArrayList<String> m4=new ArrayList<>();//Dietary Fiber
        m4.add("-g");
        m4.add("-g");
        m4.add("19g");
        m4.add("25g");
        m4.add("31g");
        m4.add("38g");
        m4.add("38g");
        m4.add("38g");
        m4.add("30g");
        m4.add("30g");
        ArrayList<String> m5=new ArrayList<>();//Vitamin D
        m5.add("10mcg");
        m5.add("10mcg");
        m5.add("15mcg");
        m5.add("15mcg");
        m5.add("15mcg");
        m5.add("15mcg");
        m5.add("15mcg");
        m5.add("15mcg");
        m5.add("15mcg");
        m5.add("20mcg");
        ArrayList<String> m6=new ArrayList<>();//Calcium
        m6.add("200mg");
        m6.add("260mg");
        m6.add("700mg");
        m6.add("1000mg");
        m6.add("1300mg");
        m6.add("1300mg");
        m6.add("1000mg");
        m6.add("1000mg");
        m6.add("1000mg");
        m6.add("1200mg");
        ArrayList<String> m7=new ArrayList<>();//Iron
        m7.add("-mg");
        m7.add("11mg");
        m7.add("7mg");
        m7.add("10mg");
        m7.add("8mg");
        m7.add("11mg");
        m7.add("8mg");
        m7.add("8mg");
        m7.add("8mg");
        m7.add("8mg");
        ArrayList<String> m8=new ArrayList<>();//Potassium
        m8.add("400mg");
        m8.add("860mg");
        m8.add("2000mg");
        m8.add("2300mg");
        m8.add("2500mg");
        m8.add("3000mg");
        m8.add("3400mg");
        m8.add("3400mg");
        m8.add("3400mg");
        m8.add("3400mg");

        male.add(m0);
        male.add(m1);
        male.add(m2);
        male.add(m3);
        male.add(m4);
        male.add(m5);
        male.add(m6);
        male.add(m7);
        male.add(m8);

        ArrayList<String> f0=new ArrayList<>();//Sodium
        f0.add("110mg");
        f0.add("370mg");
        f0.add("800mg");
        f0.add("1000mg");
        f0.add("1200mg");
        f0.add("1500mg");
        f0.add("1500mg");
        f0.add("1500mg");
        f0.add("1500mg");
        f0.add("1500mg");
        ArrayList<String> f1=new ArrayList<>();//Vitamin C
        f1.add("40mg");
        f1.add("50mg");
        f1.add("15mg");
        f1.add("25mg");
        f1.add("45mg");
        f1.add("65mg");
        f1.add("75mg");
        f1.add("75mg");
        f1.add("75mg");
        f1.add("75mg");
        ArrayList<String> f2=new ArrayList<>();//Protein
        f2.add("9g");
        f2.add("11g");
        f2.add("13g");
        f2.add("19g");
        f2.add("34g");
        f2.add("46g");
        f2.add("46g");
        f2.add("46g");
        f2.add("46g");
        f2.add("46g");
        ArrayList<String> f3=new ArrayList<>();//Total Carbohydrate
        f3.add("60g");
        f3.add("95g");
        f3.add("130g");
        f3.add("130g");
        f3.add("130g");
        f3.add("130g");
        f3.add("130g");
        f3.add("130g");
        f3.add("130g");
        f3.add("130g");
        ArrayList<String> f4=new ArrayList<>();//Dietary Fiber
        f4.add("-g");
        f4.add("-g");
        f4.add("19g");
        f4.add("25g");
        f4.add("26g");
        f4.add("26g");
        f4.add("25g");
        f4.add("25g");
        f4.add("21g");
        f4.add("21g");
        ArrayList<String> f5=new ArrayList<>();//Vitamin D
        f5.add("10mcg");
        f5.add("10mcg");
        f5.add("15mcg");
        f5.add("15mcg");
        f5.add("15mcg");
        f5.add("15mcg");
        f5.add("15mcg");
        f5.add("15mcg");
        f5.add("15mcg");
        f5.add("20mcg");
        ArrayList<String> f6=new ArrayList<>();//Calcium
        f6.add("200mg");
        f6.add("260mg");
        f6.add("700mg");
        f6.add("1000mg");
        f6.add("1300mg");
        f6.add("1300mg");
        f6.add("1000mg");
        f6.add("1000mg");
        f6.add("1200mg");
        f6.add("1200mg");
        ArrayList<String> f7=new ArrayList<>();//Iron
        f7.add("-mg");
        f7.add("11mg");
        f7.add("7mg");
        f7.add("10mg");
        f7.add("8mg");
        f7.add("15mg");
        f7.add("18mg");
        f7.add("18mg");
        f7.add("8mg");
        f7.add("8mg");
        ArrayList<String> f8=new ArrayList<>();//Potassium
        f8.add("400mg");
        f8.add("860mg");
        f8.add("2000mg");
        f8.add("2300mg");
        f8.add("2300mg");
        f8.add("2300mg");
        f8.add("2600mg");
        f8.add("2600mg");
        f8.add("2600mg");
        f8.add("2600mg");

        female.add(f0);
        female.add(f1);
        female.add(f2);
        female.add(f3);
        female.add(f4);
        female.add(f5);
        female.add(f6);
        female.add(f7);
        female.add(f8);

        standard.add(male);
        standard.add(female);
        return standard;
    }
    public void makeAnalysisCard(ArrayList<ArrayList<String>> arr, int gender, int age)
    {
        ArrayList<ArrayList<ArrayList<String>>> standard=makeStandardArray();
        ConstraintLayout scroll_view=findViewById(R.id.popup_scroll_layout);
        int j=0, id=0;
        for(int i=0; i<arr.size(); i++)
        {
            int nutrition_code=700;
            String metric="";
            switch(arr.get(i).get(0))
            {
                case "Sodium":
                    nutrition_code=0;
                    metric="mg";
                    break;
                case "Vitamin C":
                    nutrition_code=1;
                    metric="mg";
                    break;
                case "Protein":
                    nutrition_code=2;
                    metric="g";
                    break;
                case "Total Carbohydrate":
                    nutrition_code=3;
                    metric="g";
                    break;
                case "Dietary Fiber":
                    nutrition_code=4;
                    metric="g";
                    break;
                case "Vitamin D":
                    nutrition_code=5;
                    metric="mcg";
                    break;
                case "Calcium":
                    nutrition_code=6;
                    metric="mg";
                    break;
                case "Iron":
                    nutrition_code=7;
                    metric="mg";
                    break;
                case "Potassium":
                    nutrition_code=8;
                    metric="mg";
                    break;
                default:
                    break;
            }
            //Log.d("Nutrition Code",Integer.toString(nutrition_code));
            if(nutrition_code!=700 && !(nutrition_code==4&&(age==0||age==1)))
            {
                ConstraintLayout card=new ConstraintLayout(this);
                card.setId(ViewCompat.generateViewId());
                ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
                params.rightToRight=ConstraintLayout.LayoutParams.PARENT_ID;
                if(j!=0)
                {
                    params.topToBottom=id;
                }
                params.setMargins(20,20,20,20);
                card.setLayoutParams(params);
                card.setBackgroundColor(ContextCompat.getColor(this,R.color.backGround));

                TextView textView1=new TextView(this);
                ConstraintLayout.LayoutParams params1=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params1.leftToLeft=ConstraintLayout.LayoutParams.PARENT_ID;
                params1.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
                params1.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
                params1.setMargins(20,5,20,5);
                textView1.setText(arr.get(i).get(0));
                textView1.setTextColor(Color.parseColor("#000000"));
                textView1.setTextSize(24.0F);
                textView1.setLayoutParams(params1);

                String s_string=standard.get(gender).get(nutrition_code).get(age);
                s_string=s_string.replaceAll("[^0-9]","");

                String a_string=arr.get(i).get(1);
                a_string=a_string.replaceAll("[^0-9]","");

                int res=Integer.parseInt(a_string)-Integer.parseInt(s_string);

                String tv2_string=Integer.toString(res)+metric;
                String tv2_color="#000000";
                if(res>0){
                    tv2_color="#06BA63";
                }
                else if(res<0){
                    tv2_color="#ff0000";
                }

                TextView textView2=new TextView(this);
                ConstraintLayout.LayoutParams params2=new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);
                params2.rightToRight=ConstraintLayout.LayoutParams.PARENT_ID;
                params2.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
                params2.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
                params2.setMargins(20,5,20,5);
                textView2.setText(tv2_string);
                textView2.setTextColor(Color.parseColor(tv2_color));
                textView2.setTextSize(24.0F);
                textView2.setLayoutParams(params2);

                j=1;
                id=card.getId();
                card.addView(textView1);
                card.addView(textView2);
                scroll_view.addView(card);
            }
        }
    }
    public void makeAnalysisResult(ArrayList<ArrayList<String>> arr,String gender, String age){

        if(gender=="남"){
            switch(age)
            {
                case "6개월이하":
                    makeAnalysisCard(arr,0,0);
                    break;
                case "6개월 ~ 12개월":
                    makeAnalysisCard(arr,0,1);
                    break;
                case "1세 ~ 3세":
                    makeAnalysisCard(arr,0,2);
                    break;
                case "4세 ~ 8세":
                    makeAnalysisCard(arr,0,3);
                    break;
                case "9세 ~ 13세":
                    makeAnalysisCard(arr,0,4);
                    break;
                case "14세 ~ 18세":
                    makeAnalysisCard(arr,0,5);
                    break;
                case "19세 ~ 30세":
                    makeAnalysisCard(arr,0,6);
                    break;
                case "31세 ~ 50세":
                    makeAnalysisCard(arr,0,7);
                    break;
                case "51세 ~ 70세":
                    makeAnalysisCard(arr,0,8);
                    break;
                case "71세이상":
                    makeAnalysisCard(arr,0,9);
                    break;
                default:
                    break;
            }

        }
        else{
            switch(age)
            {
                case "6개월이하":
                    makeAnalysisCard(arr,1,0);
                    break;
                case "6개월 ~ 12개월":
                    makeAnalysisCard(arr,1,1);
                    break;
                case "1세 ~ 3세":
                    makeAnalysisCard(arr,1,2);
                    break;
                case "4세 ~ 8세":
                    makeAnalysisCard(arr,1,3);
                    break;
                case "9세 ~ 13세":
                    makeAnalysisCard(arr,1,4);
                    break;
                case "14세 ~ 18세":
                    makeAnalysisCard(arr,1,5);
                    break;
                case "19세 ~ 30세":
                    makeAnalysisCard(arr,1,6);
                    break;
                case "31세 ~ 50세":
                    makeAnalysisCard(arr,1,7);
                    break;
                case "51세 ~ 70세":
                    makeAnalysisCard(arr,1,8);
                    break;
                case "71세이상":
                    makeAnalysisCard(arr,1,9);
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle saveInstanceState){
        Intent intent =getIntent();
        ArrayList<ArrayList<String>> arr=new ArrayList<>();
        arr= (ArrayList<ArrayList<String>>) intent.getSerializableExtra("array");
        String gender=intent.getStringExtra("gender");
        String age=intent.getStringExtra("age");
        Log.d("InPopup",gender);
        Log.d("InPopup",age);
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);

        makeAnalysisResult(arr,gender,age);

        final ArrayList<ArrayList<String>> arr2=arr;
        Button btn=findViewById(R.id.popup_close);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent rintent = new Intent(getApplicationContext(), ResultActivity.class);
                rintent.putExtra("delivered_arraylist", arr2);
                startActivityForResult(rintent,1);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed(){
        return;
    }
}
