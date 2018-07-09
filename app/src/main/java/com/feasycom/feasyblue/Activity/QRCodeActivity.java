package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.feasycom.feasyblue.Controler.ActivityCollector;
import com.feasycom.feasyblue.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QRCodeActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    private Activity activity;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, QRCodeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_qrcode;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getResources().getString(R.string.wc));
    }

    @Override
    public void initView() {

    }

    @Override
    public void loadData() {
        activity = this;
    }

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
}
