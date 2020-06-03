package org.tensorflow.demo;

import java.util.ArrayList;
import java.util.HashMap;

public class HEFFAN_filter {
    static int cnt = 0;
    static String result_texts="";
    static ArrayList<ArrayList<String>> filtered_texts;
    static boolean isFinished=false;
    public void sample(){
        ArrayList<ArrayList<String>> before=new ArrayList<ArrayList<String>>();

        ArrayList<String> text = new ArrayList<String>();
        text.add("Sosdium 3g 50%");
        text.add("Sodium2 3g 50%");
        text.add("Sodium 3g 50%");
        text.add("Sodium 3h 50%");
        text.add("Sodium 3g 50*");
        text.add("Calories 3mg 50%");
        text.add("Protein 111mcg 1%");
        before.add(text);

        supplement_filter(before);
    }

    public static void collectTexts(int loop,String text){
        System.out.println("colleced Texts");
        cnt++;
        result_texts+=text;
        if(cnt>=loop){
            cnt=0;
            text_adapter(result_texts);
        }
    }
    public static void text_adapter(String texts){

        System.out.println("FILTER : public void text_adapter(String texts)");
        System.out.println("BEFORE FILTER"+texts);
        ArrayList<ArrayList<String>> before=new ArrayList<ArrayList<String>>();
        ArrayList<String> text = new ArrayList<String>();
        String[] text_arr=texts.split("\n");
        for(int i=0;i<text_arr.length;i++){
            System.out.println("FILTER : text.add(text_arr[i]) "+text_arr[i]);
            text.add(text_arr[i]);
        }
        before.add(text);
        setFilterResult(supplement_filter(before)); // 최종 끝나는 부분
        isFinished=true;
    }

    public static ArrayList<ArrayList<String>> supplement_filter(ArrayList<ArrayList<String>> before){

        String [] filter= {"Sodium","Calories","Protein","Vitamin C"
                ,"Sugars","Total Fat","Saturated Fat","Trans Fat"
                ,"Cholesterol","Protein"};

        ArrayList<ArrayList<String>> result=new ArrayList<ArrayList<String>>();

        HashMap<String, String> sup=new HashMap<String, String>();



        /*
         * String a="3g"; if(a.matches(filter_g)) { System.out.println("44"); }
         */


        for(int i=0; i<before.size();i++) {
            for(int j=0;j<before.get(i).size();j++) {
                for(int k=0; k<filter.length;k++) {
                    if(before.get(i).get(j).contains(filter[k])) {
                        String tmp=before.get(i).get(j);
                        String tmp2=tmp.substring(filter[k].length()+1); //  I Sodium ���Ÿ�
                        String[] tmp3=tmp2.split(" ");
                        if((tmp3[0].matches("^[0-9]+mg$") || tmp3[0].matches("^[0-9]+mcg$")|| tmp3[0].matches("^[0-9]+g$")) && (tmp3[1].matches("^[0-9]+%$"))) {
                            //System.out.println("yeahA");
                            sup.put(filter[k], tmp3[0]+" "+tmp3[1]);

                        }
                    }
                }

            }

        }

        for( String key : sup.keySet()) {
            ArrayList<String> name_gram_per=new ArrayList<String>();
            name_gram_per.add(key);
            String[] tmp=sup.get(key).split(" ");
            name_gram_per.add(tmp[0]);
            name_gram_per.add(tmp[1]);
            result.add(name_gram_per);
        }

        System.out.println("AFTER FILTER "+result);
        return result;
    }
    public static ArrayList<ArrayList<String>> getFilterResults(){
        return filtered_texts;
    }
    public static void setFilterResult(ArrayList<ArrayList<String>> texts){
        filtered_texts=texts;
    }
    public static boolean checkFinishedFiltering(){
        return isFinished;
    }
}
