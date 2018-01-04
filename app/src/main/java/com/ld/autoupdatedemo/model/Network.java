package com.ld.autoupdatedemo.model;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Network {

    private static volatile Network mInstance;
    private APi mApi;
    public static final String BASE_URL = "http://172.17.193.200/hdbr.wzgl/";
    //public static final String BASE_URL = "http://192.100.23.106/hdbr.wzgl/";
//    public static final String BASE_URL = "http://192.100.10.228/wzgl/";
//    public static final String BASE_URL = "http://172.17.197.124/wzgl/";

    private Network() {
        //构造函数私有化  单例模式
    }

    public static Network getInstance() {
        if (mInstance == null) {
            synchronized (Network.class) {
                if (mInstance == null) {
                    mInstance = new Network();
                }
            }
        }
        return mInstance;
    }

    public APi getApi(Context context) {
        if (mApi == null) {
            synchronized (Network.class) {
                if (mApi == null) {
                    String baseUrl = context
                            .getSharedPreferences("config", Context.MODE_PRIVATE)
                            .getString("key_port", "http://172.17.193.200/hdbr.wzgl/");

                    Retrofit retrofit = new Retrofit.Builder()
                            //使用自定义的mGsonConverterFactory
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(baseUrl)
                            .client(genericClient())
                            .build();
                    mApi = retrofit.create(APi.class);
                }
            }
        }
        return mApi;
    }

    public APi getApi(OkHttpClient client) {
        if (mApi == null) {
            synchronized (Network.class) {
                if (mApi == null) {

                    Retrofit retrofit = new Retrofit.Builder()
                            //使用自定义的mGsonConverterFactory
                            .addConverterFactory(GsonConverterFactory.create())
                            .baseUrl(BASE_URL)
                            .client(client)
                            .build();
                    mApi = retrofit.create(APi.class);
                }
            }
        }
        return mApi;
    }

    public OkHttpClient genericClient() {

        //得到本地存储的set-cookie
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(3000, TimeUnit.MILLISECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                                //.addHeader("Accept-Encoding", "gzip, deflate")
                                //.addHeader("Connection", "keep-alive")
                                //.addHeader("Accept", "*/*")
                                .addHeader("Cookie", "add cookies here")
                                .build();
                        return chain.proceed(request);
                    }
                }).build();

        return httpClient;
    }


}