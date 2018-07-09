package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.feasyblue.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {
    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.parameterDefining)
    Button parameterDefining;
    @BindView(R.id.otaButton)
    Button otaButton;
    @BindView(R.id.communication_button)
    ImageView communicationButton;
    @BindView(R.id.communication_button_text)
    TextView communicationButtonText;
    @BindView(R.id.setting_button)
    ImageView settingButton;
    @BindView(R.id.setting_button_text)
    TextView settingButtonText;
    @BindView(R.id.about_button)
    ImageView aboutButton;
    @BindView(R.id.about_button_text)
    TextView aboutButtonText;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    private Activity activity;
    private FscBleCentralApi fscBleCentralApi;
    private FscSppApi fscSppApi;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void refreshFooter() {
        communicationButton.setImageResource(R.drawable.communication_off);
        settingButton.setImageResource(R.drawable.setting_on);
        aboutButton.setImageResource(R.drawable.about_off);

        communicationButtonText.setTextColor(getResources().getColor(R.color.color_tb_text));
        settingButtonText.setTextColor(getResources().getColor(R.color.footer_on_text_color));
        aboutButtonText.setTextColor(getResources().getColor(R.color.color_tb_text));
    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setVisibility(View.GONE);
        headerRightText.setVisibility(View.GONE);
        headerTitle.setText(getString(R.string.settingTitle));
    }

    @Override
    public void initView() {

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
    }

    @OnClick(R.id.parameterDefining)
    public void parameterDefiningClick() {
        ParameterModificationActivity.actionStart(activity);
    }

    @OnClick(R.id.otaButton)
    public void otaButtonClick() {
        OtaActivity.actionStart(activity);
    }

    @OnClick(R.id.quickConnection)
    public void quickConnection() {
        QuickConnectionActivity.actionStart(activity);
    }


}
