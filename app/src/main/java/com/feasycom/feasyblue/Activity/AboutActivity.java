package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
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

public class AboutActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.version)
    TextView version;
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

    private Activity activity;
    private FscBleCentralApi fscBleCentralApi;
    private FscSppApi fscSppApi;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void refreshFooter() {
        communicationButton.setImageResource(R.drawable.communication_off);
        settingButton.setImageResource(R.drawable.setting_off);
        aboutButton.setImageResource(R.drawable.about_on);

        communicationButtonText.setTextColor(getResources().getColor(R.color.color_tb_text));
        settingButtonText.setTextColor(getResources().getColor(R.color.color_tb_text));
        aboutButtonText.setTextColor(getResources().getColor(R.color.footer_on_text_color));
    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setVisibility(View.INVISIBLE);
        headerTitle.setText(getResources().getString(R.string.about));
    }

    @Override
    public void initView() {
        version.setText(getResources().getString(R.string.appName) + " " + getResources().getString(R.string.appVersion));
    }

    @Override
    public void loadData() {
        activity = this;
        /**
         * anonymous inner class will hold the outer class object of activity
         * it is recommended to clear the object here
         */
        fscBleCentralApi=FscBleCentralApiImp.getInstance(activity);
        fscBleCentralApi.setCallbacks(null);

        fscSppApi= FscSppApiImp.getInstance(activity);
        fscSppApi.setCallbacks(null);
    }

    @OnClick(R.id.qr)
    public void qrCode() {
        QRCodeActivity.actionStart(activity);
    }

}
