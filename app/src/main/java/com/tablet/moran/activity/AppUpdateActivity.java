package com.tablet.moran.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tablet.moran.R;
import com.tablet.moran.tools.AppUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppUpdateActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.back_back)
    ImageView backBack;
    @BindView(R.id.back_btn)
    TextView backBtn;
    @BindView(R.id.back)
    LinearLayout back;
    @BindView(R.id.cur_version_tv)
    TextView curVersionTv;
    @BindView(R.id.is_update_tv)
    TextView isUpdateTv;
    @BindView(R.id.select_flag_1)
    ImageView selectFlag1;
    @BindView(R.id.yes_btn)
    LinearLayout yesBtn;
    @BindView(R.id.select_flag_2)
    ImageView selectFlag2;
    @BindView(R.id.no_btn)
    LinearLayout noBtn;
    @BindView(R.id.tip_dialog_LL)
    LinearLayout tipDialogLL;

    private boolean isNeedUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);
        ButterKnife.bind(this);

        initView();

        setListener();
    }

    @Override
    protected void initView() {
        super.initView();
        if(isChinese()) {
            curVersionTv.setText("当前系统版本:" + AppUtils.getAppVersionName(getApplication()));
            isUpdateTv.setText("当前已经是最新版本");
        } else {
            curVersionTv.setText("Current version of system:" + AppUtils.getAppVersionName(getApplication()));
            isUpdateTv.setText("Already the lastest version");
        }
        yesBtn.setVisibility(View.GONE);
        noBtn.setVisibility(View.GONE);

        curResId = R.id.back_btn;
        curResIndex = 0;

        dialog.show();
        if(isChinese()) {
            dialog.getMsgView().setText("正在检查更新...");
        } else {
            dialog.getMsgView().setText("Detecting...");
        }
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        AppUtils.showToast(getApplicationContext(), isChinese() ? "已经是最新版本" : "Already the lastest version");
                        timer.cancel();
                    }
                });
            }
        }, 2000);

    }

    @Override
    protected void setListener() {
        super.setListener();

        backBtn.setOnClickListener(this);
        yesBtn.setOnClickListener(this);
        noBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.yes_btn:

                break;
            case R.id.no_btn:
                break;
        }
    }

    @Override
    protected void toBlRight() {
        super.toBlRight();

        toBlOk();
    }

    @Override
    protected void toBlOk() {
        super.toBlOk();

        switch (curResId) {
            case R.id.yes_btn:
                if (alterListener != null)
                    alterListener.positiveGo();
                break;
            case R.id.no_btn:
                if (alterListener != null)
                    alterListener.negativeGo();
                break;
            case R.id.back_btn:
                finish();
                break;

        }
    }

    @Override
    protected void toBlUp() {
        super.toBlUp();

        if(!isNeedUpdate)
            return;

        curResIndex--;
        curResIndex = curResIndex == -1 ? 2 : curResIndex;

        switch (curResIndex) {
            case 0:
                curResId = R.id.back_btn;
                yesFlag.setVisibility(View.INVISIBLE);
                noFlag.setVisibility(View.INVISIBLE);
                backBack.setVisibility(View.VISIBLE);
                break;
            case 1:
                curResId = R.id.yes_btn;
                yesFlag.setVisibility(View.VISIBLE);
                noFlag.setVisibility(View.INVISIBLE);
                backBack.setVisibility(View.INVISIBLE);
                break;
            case 2:
                curResId = R.id.no_btn;
                yesFlag.setVisibility(View.INVISIBLE);
                noFlag.setVisibility(View.VISIBLE);
                backBack.setVisibility(View.INVISIBLE);
                break;

        }
//
//        yesFlag.setVisibility(yesFlag.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
//        noFlag.setVisibility(noFlag.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
//        curResId = yesFlag.getVisibility() == View.VISIBLE ? R.id.yes_btn : R.id.no_btn;

    }

    @Override
    protected void toBlDown() {
        super.toBlDown();

        if(!isNeedUpdate)
            return;

        curResIndex++;
        curResIndex = curResIndex == 3 ? 0 : curResIndex;

        switch (curResIndex) {
            case 0:
                curResId = R.id.back_btn;
                yesFlag.setVisibility(View.INVISIBLE);
                noFlag.setVisibility(View.INVISIBLE);
                backBack.setVisibility(View.VISIBLE);
                break;
            case 1:
                curResId = R.id.yes_btn;
                yesFlag.setVisibility(View.VISIBLE);
                noFlag.setVisibility(View.INVISIBLE);
                backBack.setVisibility(View.INVISIBLE);
                break;
            case 2:
                curResId = R.id.no_btn;
                yesFlag.setVisibility(View.INVISIBLE);
                noFlag.setVisibility(View.VISIBLE);
                backBack.setVisibility(View.INVISIBLE);
                break;

        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            setContentView(R.layout.activity_app_update);
//            ButterKnife.bind(this);
            backBtn = (TextView) findViewById(R.id.back_btn);
            backBack = (ImageView) findViewById(R.id.back_back);
            curVersionTv = (TextView) findViewById(R.id.cur_version_tv);
            isUpdateTv = (TextView) findViewById(R.id.is_update_tv);
            yesBtn = (LinearLayout) findViewById(R.id.yes_btn);
            noBtn = (LinearLayout)findViewById(R.id.no_btn);
            selectFlag1 = (ImageView) findViewById(R.id.select_flag_1);
            selectFlag2 = (ImageView) findViewById(R.id.select_flag_2);

            initView();
            setListener();


        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_app_update);

            backBtn = (TextView) findViewById(R.id.back_btn);
            backBack = (ImageView) findViewById(R.id.back_back);
            curVersionTv = (TextView) findViewById(R.id.cur_version_tv);
            isUpdateTv = (TextView) findViewById(R.id.is_update_tv);
            yesBtn = (LinearLayout) findViewById(R.id.yes_btn);
            noBtn = (LinearLayout)findViewById(R.id.no_btn);
            selectFlag1 = (ImageView) findViewById(R.id.select_flag_1);
            selectFlag2 = (ImageView) findViewById(R.id.select_flag_2);

            initView();
            setListener();

        }
    }
}
