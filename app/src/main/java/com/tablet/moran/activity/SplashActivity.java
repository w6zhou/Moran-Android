package com.tablet.moran.activity;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.igexin.sdk.PushManager;
import com.tablet.moran.R;
import com.tablet.moran.config.Constant;
import com.tablet.moran.model.Device;
import com.tablet.moran.model.LocalPlayList;
import com.tablet.moran.model.Paint;
import com.tablet.moran.model.Picture;
import com.tablet.moran.model.PlayPictureBack;
import com.tablet.moran.model.TitleInfo;
import com.tablet.moran.presenter.implPresenter.SetNameActivityImpl;
import com.tablet.moran.presenter.implView.ISetPadActivity;
import com.tablet.moran.tools.PreferencesUtils;
import com.tablet.moran.tools.SLogger;
import com.tablet.moran.tools.ThreadPoolManager;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends BaseActivity implements ISetPadActivity {

    public static final String LOCAL_PATH = "/mnt/internal_sd/Android/data/moran";

    @BindView(R.id.gesture_img)
    ImageView gestureImg;
    private Timer timer;

    SetNameActivityImpl setNameImpl;

    String deviceName;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        setNameImpl = new SetNameActivityImpl(this, token, this);

//        PreferencesUtils.putString(getApplicationContext(), Constant.FIRST_USE, "");

        //设置隐藏系统栏
        sendBroadcast(new Intent("com.outform.hidebar"));

        initView();
        setListener();

        getPermission();

        SLogger.d("<<", "-name-->" + PreferencesUtils.getString(getApplicationContext(), Constant.CLIENT_NAME));
    }


    @Override
    protected void initView() {
        super.initView();
        gestureImg.setVisibility(View.GONE);

    }


    private void getPermission() {

        if (!isAdminActive) {//这一句一定要有...
            Intent intent = new Intent();
            //指定动作
            intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            //指定给那个组件授权
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            startActivity(intent);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        ThreadPoolManager.getinstance().execute(new Runnable() {
            @Override
            public void run() {
                while (!isAdminActive) {
                    try {
                        //为了避免程序一直while循环，让它睡个100毫秒在检测……
                        isAdminActive = devicePolicyManager.isAdminActive(componentName);
                        Thread.currentThread();
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(PreferencesUtils.getString(getApplicationContext(), Constant.FIRST_USE)))
                            initLocalFile();
                        else {
//                            initLocalFile();
                            if (connected) {
                                dialog.show();

                                deviceName = PreferencesUtils.getString(getApplicationContext(), Constant.CLIENT_NAME);

                                if (TextUtils.isEmpty(deviceName)) {
                                    deviceName = "C" + System.currentTimeMillis();
                                    String client_id = PushManager.getInstance().getClientid(SplashActivity.this);
                                    Device device = new Device();
                                    device.setDevice_name(deviceName);
                                    device.setDevice_id(client_id);
                                    setNameImpl.setPadNameSplash(device);
                                } else {
                                    setNameImpl.getNews();
                                }
                            }

                            timer = new Timer();
                            startTime();
                        }


                    }
                });

            }
        });


//        } else {
//            startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));

//        }

    }

    private void initLocalFile() {
        dialog.show();
        dialog.getMsgView().setText("正在初始化系统...");

        ThreadPoolManager.getinstance().execute(new Runnable() {
            @Override
            public void run() {

                File localParentFile = new File(LOCAL_PATH);
                File[] files = localParentFile.listFiles();

                for (int i = 0; i < files.length; i++) {
                    File item = files[i];
                    SLogger.d("<<", "-->" + item.getAbsolutePath());
                    setFilePicMap(item);
                }

                if (connected) {

                    deviceName = PreferencesUtils.getString(getApplicationContext(), Constant.CLIENT_NAME);

                    if (TextUtils.isEmpty(deviceName)) {
                        deviceName = "C" + System.currentTimeMillis();
                        String client_id = PushManager.getInstance().getClientid(SplashActivity.this);
                        Device device = new Device();
                        device.setDevice_name(deviceName);
                        device.setDevice_id(client_id);
                        setNameImpl.setPadNameSplash(device);
                    } else {
                        setNameImpl.getNews();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        PreferencesUtils.putString(getApplicationContext(), Constant.FIRST_USE, Constant.FIRST_USE);

                        startTime();
                    }
                });

            }
        });

    }

    private void setFilePicMap(File file) {

        try {
            Picture picture = new Picture();

            String name = file.getName();
            String name_l[] = name.split("\\.");
            String id = name_l[0];
            String title = "画作主题";
            String time = "画作年代";

            picture.setTitle(title);
            picture.setTime(time);

            //本地已经存入的映射
            if(id.contains("v"))
                id = id.substring(1, id.length());
            Picture picture1 = diskLruCacheHelper.getAsSerializable(id);

            //代表是video
            if (name.contains("mp4")) {

                picture.setPicture_id(Integer.valueOf(id));
                picture.setVideoFile(file.getAbsolutePath());
                //存储视频至对应画作
                if (picture1 != null) {
                    picture1.setVideoFile(file.getAbsolutePath());
                    diskLruCacheHelper.put(id, picture1);
                } else {
                    diskLruCacheHelper.put(id, picture);
                }
            } else {
                picture.setPicture_id(Integer.valueOf(id));
                picture.setPicture_url(file.getAbsolutePath());

                if (picture1 != null) {
                    picture1.setPicture_url(file.getAbsolutePath());
                    diskLruCacheHelper.put(id, picture1);
                } else {
                    diskLruCacheHelper.put(id, picture);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startTime() {
        timer = new Timer();
        SLogger.d("<<", "---->>>>11111111111111111");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isFinishing()) {
//                    if (TextUtils.isEmpty(PreferencesUtils.getString(getApplicationContext(), Constant.CLIENT_NAME)))
//                        startActivity(new Intent(SplashActivity.this, SetPadNameActivity.class));
                    SLogger.d("<<", "---->>>>333333333333333333333333");
                    finish();
                    startActivity(new Intent(SplashActivity.this, LiningActivity.class));
                    SLogger.d("<<", "---->>>>2222222222222222222");
                }
            }
        }, 13000);
    }

    @Override
    protected void setListener() {
        super.setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    @Override
    protected void toUp() {
        super.toUp();

        gestureImg.setImageDrawable(getResources().getDrawable(R.mipmap.gesture_top));
        gestureImg.setVisibility(View.VISIBLE);
        gestureImg.postDelayed(new Runnable() {
            @Override
            public void run() {
                gestureImg.setVisibility(View.GONE);
            }
        }, 500);

        timer.cancel();
        startTime();
    }

    @Override
    protected void toRight() {
        super.toRight();
        gestureImg.setImageDrawable(getResources().getDrawable(R.mipmap.gesture_right));
        gestureImg.setVisibility(View.VISIBLE);
        gestureImg.postDelayed(new Runnable() {
            @Override
            public void run() {
                gestureImg.setVisibility(View.GONE);
            }
        }, 500);

        timer.cancel();
         startTime();

    }

    @Override
    protected void toDown() {
        super.toDown();
        gestureImg.setImageDrawable(getResources().getDrawable(R.mipmap.gesture_below));
        gestureImg.setVisibility(View.VISIBLE);
        gestureImg.postDelayed(new Runnable() {
            @Override
            public void run() {
                gestureImg.setVisibility(View.GONE);
            }
        }, 500);

        timer.cancel();
        timer = null;
        startTime();
    }

    @Override
    protected void toLeft() {
        super.toLeft();
        gestureImg.setImageDrawable(getResources().getDrawable(R.mipmap.gesture_left));
        gestureImg.setVisibility(View.VISIBLE);
        gestureImg.postDelayed(new Runnable() {
            @Override
            public void run() {
                gestureImg.setVisibility(View.GONE);
            }
        }, 500);

        timer.cancel();
        timer = null;
        startTime();
    }

    @Override
    protected void bigVol() {
        super.bigVol();
        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void smallVol() {
        super.smallVol();
        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void toBlRight() {
        super.toBlRight();
//        if (TextUtils.isEmpty(PreferencesUtils.getString(getApplicationContext(), Constant.CLIENT_NAME)))
//            startActivity(new Intent(SplashActivity.this, SetPadNameActivity.class));

        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void toBlBack() {
//        if (TextUtils.isEmpty(PreferencesUtils.getString(getApplicationContext(), Constant.CLIENT_NAME)))
//            startActivity(new Intent(SplashActivity.this, SetPadNameActivity.class));

        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void toBlUp() {
        super.toBlUp();
        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void toBlDown() {
        super.toBlDown();
        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void toBlOk() {
        super.toBlOk();
        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    protected void toBlLeft() {
        super.toBlLeft();

        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }


    @Override
    protected void toHome() {
        super.toHome();

        startActivity(new Intent(SplashActivity.this, LiningActivity.class));
        timer.cancel();
        timer = null;
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            setContentView(R.layout.activity_splash);
//            ButterKnife.bind(this);
            gestureImg = (ImageView) findViewById(R.id.gesture_img);

        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_splash);
//            ButterKnife.bind(this);

            gestureImg = (ImageView) findViewById(R.id.gesture_img);

        }
    }


    @Override
    public void showProgressDialog() {
        dialog.show();
    }

    @Override
    public void hidProgressDialog() {
        dialog.hide();
    }

    @Override
    public void showError(String error) {
        dialog.hide();
//        AppUtils.showToast(getApplicationContext(), error);
//        PreferencesUtils.putString(getApplicationContext(), Constant.CLIENT_NAME, "C" + System.currentTimeMillis());
    }


    @Override
    public void postSuccess(Paint paint) {

        PlayPictureBack pb = new PlayPictureBack();
        TitleInfo t = new TitleInfo();
        t.setPaint_id(paint.getPaint_id());
        t.setDetail_url(paint.getTitle_detail_url());
        t.setGauss_img_url(paint.getGauss_img_url());
        t.setPaint_title(paint.getPaint_title());
        t.setNews(true);
        pb.setTitle_info(t);
        pb.setPictures(paint.getPicture_ids());
        pb.setNews(true);
        pb.setPaint_id(paint.getPaint_id());

        LocalPlayList lp = (LocalPlayList) diskLruCacheHelper.getAsSerializable(Constant.LOCAL_ALL_LIST);

        if(lp == null) {
            lp = new LocalPlayList();
            PlayPictureBack localPb = JSONObject.parseObject(PreferencesUtils.getString(this, Constant.LOCAL_LIST), PlayPictureBack.class);
//            PlayPictureBack testPb = JSONObject.parseObject(PreferencesUtils.getString(this, Constant.PAINT_TEST), PlayPictureBack.class);
            lp.getList().add(localPb);
//            lp.getList().add(testPb);
            lp.getList().add(pb);
            diskLruCacheHelper.put(Constant.LOCAL_ALL_LIST, lp);
        } else {
            List<PlayPictureBack> list = lp.getList();
            boolean isHas = false;
            for(int i = 0; i < list.size(); i++) {
                PlayPictureBack p = list.get(i);
                if(p.getPaint_id() == pb.getPaint_id()){
                    isHas = true;
                    break;
                }
            }
            if(!isHas) {
                list.add(pb);
                lp.setList(list);
                diskLruCacheHelper.put(Constant.LOCAL_ALL_LIST, lp);
            }

        }

        PreferencesUtils.putString(getApplicationContext(), Constant.CLIENT_NAME, deviceName);
//        AppUtils.showToast(getApplicationContext(), "设置成功");
        startActivity(new Intent(this, LiningActivity.class));
    }
}
