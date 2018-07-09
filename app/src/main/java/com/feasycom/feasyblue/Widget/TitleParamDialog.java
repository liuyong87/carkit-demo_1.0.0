package com.feasycom.feasyblue.Widget;

import android.content.Context;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feasycom.feasyblue.Bean.BaseEvent;
import com.feasycom.feasyblue.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;


public class TitleParamDialog extends BaseDialog {


    private final Context context;
    @BindView(R.id.input_param)
    EditText inputParam;
    @BindView(R.id.no)
    Button no;
    @BindView(R.id.yes)
    Button yes;
    @BindView(R.id.title)
    TextView title;

    private String paramString;
    private View view;

    public TitleParamDialog(Context context) {
        super(context);
        this.context = context;
        initUI();
    }

    public TitleParamDialog(Context context, int inputType,String title) {
        super(context);
        this.context = context;
        initUI();
        inputParam.setInputType(inputType);
        this.title.setText(title);
    }

    private void initUI() {
        view = getLayoutInflater().inflate(R.layout.dialog_param_input, null,
                false);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addContentView(view, lp);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(false);
    }

    @OnTextChanged(R.id.input_param)
    protected void inputPin(Editable s) {
        this.paramString = s.toString();
    }

    @OnClick(R.id.yes)
    protected void onConfirmClick() {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        super.dismiss();
        BaseEvent be = new BaseEvent(BaseEvent.COMPLETE_COUNT_CHANGE);
        be.setParam(paramString);
        EventBus.getDefault().post(be);
    }

    @Override
    @OnClick(R.id.no)
    public void dismiss() {
        super.dismiss();
    }


}
