package com.ld.autoupdatedemo;

import android.util.Log;

/**
 * Created by Administrator on 2017/3/29.
 */

public class L {

    private static final  String TAG ="good" ;
    private static boolean debug=true;

    public static void e(String msg){

        if(debug){
            Log.e(TAG,msg);
        }
    }
}
