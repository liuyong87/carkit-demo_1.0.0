package com.feasycom.feasyblue.Widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;


public class BaseDialog extends Dialog {

    public BaseDialog(Context context) {
        super(context);
        //去掉系统对话框的title和边框背景
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public BaseDialog(Context context, int styles) {
        super(context, styles);
        //去掉系统对话框的title和边框背景
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}
