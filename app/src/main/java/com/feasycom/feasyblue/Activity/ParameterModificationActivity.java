package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feasycom.feasyblue.Bean.BaseEvent;
import com.feasycom.feasyblue.R;
import com.feasycom.feasyblue.Util.SettingConfigUtil;
import com.feasycom.feasyblue.Widget.CustomizeAtCommandView;
import com.feasycom.feasyblue.Widget.ForceAtCommandView;
import com.feasycom.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static android.view.View.GONE;

public class ParameterModificationActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_button)
    Button headerRightButton;
    @BindView(R.id.customizeCommandButton)
    Button customizeCommandButton;
    @BindView(R.id.customizeCommand1)
    CustomizeAtCommandView customizeCommand1;
    @BindView(R.id.customizeCommand2)
    CustomizeAtCommandView customizeCommand2;
    @BindView(R.id.customizeCommand3)
    CustomizeAtCommandView customizeCommand3;
    @BindView(R.id.customizeCommand4)
    CustomizeAtCommandView customizeCommand4;
    @BindView(R.id.customizeCommand5)
    CustomizeAtCommandView customizeCommand5;
    @BindView(R.id.customizeCommand6)
    CustomizeAtCommandView customizeCommand6;
    @BindView(R.id.customizeCommand7)
    CustomizeAtCommandView customizeCommand7;
    @BindView(R.id.customizeCommand8)
    CustomizeAtCommandView customizeCommand8;
    @BindView(R.id.customizeCommand9)
    CustomizeAtCommandView customizeCommand9;

    @BindView(R.id.customizeSelectCound)
    TextView customizeSelectCound;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.commandName)
    ForceAtCommandView commandName;
    @BindView(R.id.commandPin)
    ForceAtCommandView commandPin;
    @BindView(R.id.commandBaud)
    ForceAtCommandView commandBaud;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.passwordLL)
    LinearLayout passwordLL;
    private Activity activity;
    public static int CUSTOMIZE_COMMAND_COUNT = 0;
    public static final int CUSTOMIZE_COMMAND_COUNT_CHANGE = 1;

    private Set<String> commandSet = new HashSet<>();

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ParameterModificationActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_parameter_modification;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getString(R.string.properitiesDefining));
        headerRightButton.setText(getString(R.string.begin));
    }

    @Override
    public void initView() {
        if (inputPassWordShow) {
            passwordLL.setVisibility(View.VISIBLE);
            headerRightButton.setVisibility(View.INVISIBLE);
        } else {
            passwordLL.setVisibility(View.GONE);
            headerRightButton.setVisibility(View.VISIBLE);
        }
        if (commandSet != null) {
            commandSet.toString();
            for (String command : commandSet) {
                if (command.contains("AT+NAME")) {
                    commandName.setCommandInfo(command.substring(command.indexOf("=") + 1, command.length()));
                } else if (command.contains("AT+PIN")) {
                    commandPin.setCommandInfo(command.substring(command.indexOf("=") + 1, command.length()));
                } else if (command.contains("AT+BAUD")) {
                    commandBaud.setCommandInfo(command.substring(command.indexOf("=") + 1, command.length()));
                } else if (command.contains("=")) {
                    String comm = command.substring(command.indexOf("+") + 1, command.indexOf("="));
                    String param = command.substring(command.indexOf("=") + 1, command.length());
                    if ("".equals(customizeCommand1.getCommandInfo())) {
                        customizeCommand1.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand2.getCommandInfo())) {
                        customizeCommand2.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand3.getCommandInfo())) {
                        customizeCommand3.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand4.getCommandInfo())) {
                        customizeCommand4.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand5.getCommandInfo())) {
                        customizeCommand5.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand6.getCommandInfo())) {
                        customizeCommand6.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand7.getCommandInfo())) {
                        customizeCommand7.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand8.getCommandInfo())) {
                        customizeCommand8.setCommandInfo(comm, param);
                    } else if ("".equals(customizeCommand9.getCommandInfo())) {
                        customizeCommand9.setCommandInfo(comm, param);
                    }
                }
            }
        }
    }

    @Override
    public void loadData() {
        activity = this;
        EventBus.getDefault().register(this);
        commandSet = (HashSet<String>) SettingConfigUtil.getData(getApplicationContext(), "commandSet", commandSet);
    }

    @OnTextChanged(R.id.password)
    public void checkPassword(CharSequence s) {
        if ("20138888".equals(s.toString())) {
            passwordLL.setVisibility(GONE);
            inputPassWordShow = false;
            headerRightButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            finish();
        }
        return true;
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        finish();
    }


    @OnClick(R.id.header_right_button)
    public void beginParameterModify() {
        /**
         *  empty the command before saving
         */
        commandSet.clear();
        /**
         * before modifying the parameters, we can first check the version information
         */
        if (!commandSet.contains("AT+VER")) {
            commandSet.add("AT+VER");
        }
        if (commandName.getCommandInfo() != null) {
            commandSet.add("AT+NAME=" + commandName.getCommandInfo());
//            commandSet.add("AT+LENAME="+commandName.getCommandInfo());
        }
        if (commandPin.getCommandInfo() != null) {
            commandSet.add(commandPin.getCommandInfo());
        }
        if (commandBaud.getCommandInfo() != null) {
            commandSet.add(commandBaud.getCommandInfo());
        }
        if (!customizeCommand1.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand1.getCommandInfo());
        }
        if (!customizeCommand2.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand2.getCommandInfo());
        }
        if (!customizeCommand3.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand3.getCommandInfo());
        }
        if (!customizeCommand4.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand4.getCommandInfo());
        }
        if (!customizeCommand5.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand5.getCommandInfo());
        }
        if (!customizeCommand6.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand6.getCommandInfo());
        }
        if (!customizeCommand7.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand7.getCommandInfo());
        }
        if (!customizeCommand8.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand8.getCommandInfo());
        }
        if (!customizeCommand9.getCommandInfo().equals("")) {
            commandSet.add(customizeCommand9.getCommandInfo());
        }
        if (commandSet.size() > 1) {
            SettingConfigUtil.saveData(getApplicationContext(), "commandSet", commandSet);
            ParameterModificationDeviceListActivity.actionStart(activity, commandSet);
        } else {
            ToastUtil.show(activity,getString(R.string.commandNull));
        }
    }

    @Subscribe
    public void onEventMainThread(BaseEvent event) {
        switch (event.getEventId()) {
            case BaseEvent.CUSTOMIZE_COMMAND_COUNT_CHANGE:
                customizeSelectCound.setText(Integer.valueOf(CUSTOMIZE_COMMAND_COUNT).toString());
                break;
        }
    }

    @OnClick(R.id.customizeCommandButton)
    public void commandButtonClick() {
        if (customizeCommandButton.getText().toString().equals(getString(R.string.customizeCommand))) {
            customizeCommandButton.setText(R.string.customizeCommandHid);
            customizeCommand1.setVisibility(View.INVISIBLE);
            customizeCommand2.setVisibility(View.INVISIBLE);
            customizeCommand3.setVisibility(View.INVISIBLE);
            customizeCommand4.setVisibility(View.INVISIBLE);
            customizeCommand5.setVisibility(View.INVISIBLE);
            customizeCommand6.setVisibility(View.INVISIBLE);
            customizeCommand7.setVisibility(View.INVISIBLE);
            customizeCommand8.setVisibility(View.INVISIBLE);
            customizeCommand9.setVisibility(View.INVISIBLE);
        } else {
            customizeCommandButton.setText(getString(R.string.customizeCommand));
            customizeCommand1.setVisibility(View.VISIBLE);
            customizeCommand2.setVisibility(View.VISIBLE);
            customizeCommand3.setVisibility(View.VISIBLE);
            customizeCommand4.setVisibility(View.VISIBLE);
            customizeCommand5.setVisibility(View.VISIBLE);
            customizeCommand6.setVisibility(View.VISIBLE);
            customizeCommand7.setVisibility(View.VISIBLE);
            customizeCommand8.setVisibility(View.VISIBLE);
            customizeCommand9.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        CUSTOMIZE_COMMAND_COUNT = 0;
        EventBus.getDefault().unregister(this);
    }
}
