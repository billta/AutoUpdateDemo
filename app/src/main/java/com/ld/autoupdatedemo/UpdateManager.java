package com.ld.autoupdatedemo;/**
 * Created by xiehehe on 16/8/22.
 */

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ld.autoupdatedemo.bean.LibBeans;
import com.ld.autoupdatedemo.bean.UpdateBean;
import com.ld.autoupdatedemo.model.APi;
import com.ld.autoupdatedemo.model.Network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * User: xiehehe
 * Date: 2016-10-12
 * Time: 20:33
 * FIXME
 */
public class UpdateManager {
    private Context mContext;
    private static final int DOWNLOAD = 0;//下载
    private static final int DOWNLOAD_FINISH = 1;//下载完成
    private String mSavePath;//保存路径
    private int progress;//进度值
    private ProgressBar mProgress;//进度条
    private Dialog mDownloadDialog;//更新窗口
    private boolean cancelUpdate = false;//取消更新
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        if (isUpdate()) {
            // 显示提示对话框
            showNoticeDialog();
        } else {
            Toast.makeText(mContext, "没有新版", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 判断是否有更新，需要跟后台产生信息交互
     *
     * @return
     */
    int serviceCode;
    private boolean isUpdate() {
        // 获取当前软件版本
        final int versionCode = getVersionCode(mContext);


        // 调用方法获取服务器可用版本信息，此处模拟为大于当前版本的定值
//        int serviceCode = 4;
      //请求服务器数据
        APi api = Network.getInstance().getApi(mContext);
        Call<LibBeans<UpdateBean>> update = api.update();
        update.enqueue(new Callback<LibBeans<UpdateBean>>() {
            @Override
            public void onResponse(Call<LibBeans<UpdateBean>> call, Response<LibBeans<UpdateBean>> response) {
                ArrayList<UpdateBean> data = response.body().data;
                String[] datas=new String[data.size()];
                for (int i=0;i<data.size();i++){
                   datas[i]=data.get(i).newVision;
                    String newVision=data.get(i).newVision;

                   //string 的newVision转变成int
                    double newversioncode = Double
                            .parseDouble(newVision);
                    serviceCode = (int) (newversioncode);

              L.e("serviceCode"+serviceCode+"........."+"versionCode"+versionCode);

                }
            }

            @Override
            public void onFailure(Call<LibBeans<UpdateBean>> call, Throwable t) {

            }
        });


        // 版本判断
        if (serviceCode > versionCode) {
            return true;
        }
        return false;
    }

    /**
     * 获取本地软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;

            L.e("vvvvvv" + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        // 构造对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_update_title);
        builder.setMessage(R.string.soft_update_info);
        // 更新
        builder.setPositiveButton(R.string.soft_update_updatebtn, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        // 构造软件下载对话框
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.soft_updating);
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        builder.setView(v);
        // 取消更新
        builder.setNegativeButton(R.string.soft_update_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });
        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        // 现在文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    //                    mSavePath = sdpath + "download";
                    mSavePath = sdpath + mContext.getPackageName();
                    URL url = new URL("http://172.17.193.200/hdbr.wzgl/Uploads/AutoUpdateDemo.apk");
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    //"xigou.apk"等于软件名字
                    File apkFile = new File(mSavePath, "AutoUpdateDemo.apk");
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }

    private void installApk() {
        File apkfile = new File(mSavePath, "AutoUpdateDemo.apk");
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", apkfile);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            i.setDataAndType(Uri.fromFile(apkfile),
                    "application/vnd.android.package-archive");
        }


//        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
