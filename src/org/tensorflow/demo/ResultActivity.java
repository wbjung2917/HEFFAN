package org.tensorflow.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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
    public void makeChart(ArrayList<ArrayList<String>> arr)
    {
        ArrayList<String> name=new ArrayList<>();
        ArrayList<Float> value=new ArrayList<>();
        float sum=0;
        for (int i = 0; i < arr.size(); i++) {
            String s=arr.get(i).get(1);
            String[] tmp={s.replaceAll("[^0-9]",""),s.replaceAll("[0-9]","")};
            Log.d("Value",tmp[0] + " " + tmp[1]);
            if(tmp[1].equals("g"))
            {
                name.add(arr.get(i).get(0));
                value.add(Float.parseFloat(tmp[0])*1000);
                sum+=Float.parseFloat(tmp[0])*1000;
            }
            else if(tmp[1].equals("mg"))
            {
                name.add(arr.get(i).get(0));
                value.add(Float.parseFloat(tmp[0]));
                sum+=Float.parseFloat(tmp[0]);
            }
            else if(tmp[1].equals("mcg"))
            {
                name.add(arr.get(i).get(0));
                value.add(Float.parseFloat(tmp[0])/1000);
                sum+=Float.parseFloat(tmp[0])/1000;
            }
            else
            {
                Log.d("MetricError","It is not g, mg, mcg");
            }
        }
        PieChart pieChart=findViewById(R.id.pieChart);
        pieChart.setUsePercentValues(true);

        ArrayList<PieEntry> values=new ArrayList<>();


        int counter =0;
        float small_sum=0;
        for(int i=0; i<name.size(); i++)
        {
            if(value.get(i)>(sum/100))
            {
                values.add(new PieEntry(value.get(i), name.get(i)));
            }
            else
            {
                counter++;
                small_sum+=value.get(i);
            }
        }
        if(counter>0)
        {
            if(small_sum> (sum/100))
            {
                values.add(new PieEntry(small_sum, "ETC"));
            }
            else
            {
                values.add(new PieEntry((float) (sum/100), "ETC"));
            }
        }

        Description des=new Description();
        des.setText("단위 : %");
        des.setTextSize(8);
        pieChart.setDescription(des);

        PieDataSet dataSet=new PieDataSet(values,"");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data=new PieData((dataSet));
        data.setValueTextSize(15f);

        pieChart.setData(data);
    }
    @Override
    protected void onCreate(Bundle saveInstanceState){
        Intent intent =getIntent();
        ArrayList<ArrayList<String>> arr=new ArrayList<>();
        arr= (ArrayList<ArrayList<String>>) intent.getSerializableExtra("delivered_arraylist");
        ArrayList<ArrayList<String>> arr2=new ArrayList<>();
        /*
        Log.d("InResult",arr.get(0).get(0));
        Log.d("InResult",arr.get(0).get(1));
        Log.d("InResult",arr.get(0).get(2));
        Log.d("InResultLength",Integer.toString(arr.size()));
        */
        ArrayList<String> a1=new ArrayList<>();
        a1.add("Sodium");
        a1.add("800mg");
        a1.add("50%");
        ArrayList<String> a2=new ArrayList<>();
        a2.add("Protein");
        a2.add("48g");
        a2.add("90%");
        ArrayList<String> a3=new ArrayList<>();
        a3.add("Vitamin");
        a3.add("50g");
        a3.add("100%");
        ArrayList<String> a4=new ArrayList<>();
        a4.add("Colesterol");
        a4.add("800mg");
        a4.add("50%");
        arr2.add(a1);
        arr2.add(a2);
        arr2.add(a3);
        arr2.add(a4);
        //{"Sodium","10mcg","50%"},{"Protein","20g","90%"},{"Vitamin","10mg","100%"}
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_result);
        makeCards(arr2);
        makeChart(arr2);
    }
}
