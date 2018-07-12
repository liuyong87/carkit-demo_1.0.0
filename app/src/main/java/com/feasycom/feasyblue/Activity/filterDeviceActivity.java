package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Util.SettingConfigUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class filterDeviceActivity extends BaseActivity {
    /*绑定控件*/
    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    @BindView(R.id.filter_switch)
    Switch filterSwitch;
    @BindView(R.id.min_rssi_text)
    TextView minRssiText;
    @BindView(R.id.rssi_value_text)
    TextView rssiValueText;
    public static SeekBar rssiSeekBar;

    private Activity activity;
    private FscBleCentralApi fscBleCentralApi;
    private FscSppApi fscSppApi;
    public static int filterValue = -100;
    public static int isFilterEnable = 0;


    public static void actionStart(Context context) {
        Intent intent = new Intent(context, filterDeviceActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_filter_device;
    }

    @Override
    public void refreshFooter() {
    }
    /*隐藏后退按钮*/
    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerRightText.setVisibility(View.GONE);
    }

    /*初始化过滤界面*/
    @Override
    public void initView() {
        minRssiText.setVisibility(View.GONE);
        rssiValueText.setVisibility(View.GONE);
        rssiSeekBar.setVisibility(View.GONE);

        filterSwitch.setChecked((boolean) SettingConfigUtil.getData(getApplicationContext(), "filter_switch", false));
        rssiSeekBar.setProgress((int) SettingConfigUtil.getData(getApplicationContext(), "filter_value", -100));
    }

    @Override
    public void loadData() {
        activity = this;
        /**
         * anonymous inner class will hold the outer class object of activity
         * it is recommended to clear the object here
         */
        fscBleCentralApi= FscBleCentralApiImp.getInstance(activity);
        fscBleCentralApi.setCallbacks(null);

        fscSppApi= FscSppApiImp.getInstance(activity);
        fscSppApi.setCallbacks(null);

        rssiSeekBar = (SeekBar) findViewById(R.id.rssi_seek_bar);

        rssiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//监听进度条
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rssiValueText.setText(":  "+ String.valueOf(progress-100) + " dB");
                filterValue = progress - 100;//让进度条初始值为-100

                SettingConfigUtil.saveData(getApplicationContext(), "filter_value", filterValue+100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                int start = seekBar.getProgress();
                rssiValueText.setText(String.valueOf(start-100) + " dB");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int end = seekBar.getProgress();
                rssiValueText.setText(String.valueOf(end-100) + " dB");
            }
        });
    }

    /*按下返回键结束当前activity*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            activity.finish();
        }
        return true;
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        activity.finish();
    }

    /*监听过滤开关*/
    @OnCheckedChanged(R.id.filter_switch)
    public void rssiSwitch(CompoundButton v, boolean flag) {

        if(flag) {
            minRssiText.setVisibility(View.VISIBLE);
            rssiValueText.setVisibility(View.VISIBLE);
            rssiSeekBar.setVisibility(View.VISIBLE);
            isFilterEnable = 1;
        }
        else{
            minRssiText.setVisibility(View.GONE);
            rssiValueText.setVisibility(View.GONE);
            rssiSeekBar.setVisibility(View.GONE);
            isFilterEnable = 0;
        }

        SettingConfigUtil.saveData(getApplicationContext(), "filter_switch", flag);
    }

}