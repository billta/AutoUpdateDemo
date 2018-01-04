package com.ld.autoupdatedemo.model;


import com.ld.autoupdatedemo.bean.LibBeans;
import com.ld.autoupdatedemo.bean.UpdateBean;

import retrofit2.Call;
import retrofit2.http.POST;

/**
 * Created by Airmour@163.com on 2017/3/6
 * <p>
 * 接口
 */
public interface APi {


    //登录
//    @POST("api/login")
//    Call<User> login(@Query("name") String name, @Query("password") String pwd);
    //更新app
    @POST("MMGoodsItems/AndroidUpdateapk")
    Call<LibBeans<UpdateBean>> update();


}
