package com.feasycom.feasyblue.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feasycom.bean.BluetoothDeviceWrapper;
import com.feasycom.controler.FscBleCentralApi;
import com.feasycom.controler.FscBleCentralApiImp;
import com.feasycom.controler.FscSppApi;
import com.feasycom.controler.FscSppApiImp;
import com.feasycom.feasyblue.Controler.FscBleCentralCallbacksImpInformation;
import com.feasycom.feasyblue.Controler.FscSppCallbacksImpInformation;
import com.feasycom.feasyblue.R;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.feasycom.feasyblue.Activity.ParameterModificationDeviceListActivity.REQUEST_MODIFY_PARAMETER;

public class ParameterModifyInformationActivity extends BaseActivity {

    @BindView(R.id.header_left_image)
    ImageView headerLeftImage;
    @BindView(R.id.header_title)
    TextView headerTitle;
    @BindView(R.id.header_right_text)
    TextView headerRightText;
    @BindView(R.id.headerLinerLayout)
    LinearLayout headerLinerLayout;
    @BindView(R.id.modifyInformation)
    EditText modifyInformationEdit;

    private BluetoothDeviceWrapper bluetoothDeviceWrapper;
    private FscBleCentralApi fscBleCentralApi;
    private FscSppApi fscSppApi;
    private Activity activity;
    private Set<String> commandSet;
    private Handler handler = new Handler();
    private StringBuffer modifyInformation = new StringBuffer();
    public static final int MODIFY_SUCCESSFUL = 1;
    public static final int MODIFY_FAILED = 0;
    private int modifyResult = MODIFY_FAILED;
    private boolean BLE_MODE = false;
    /**
     *
     */
    private boolean isNoNeed = true;

    public static void actionStart(Context context, BluetoothDeviceWrapper bluetoothDeviceWrapper, Set<String> commandSet, int operation, boolean mode) {
        Intent intent = new Intent(context, ParameterModifyInformationActivity.class);
        intent.putExtra("mode", mode);
        Bundle bundle = new Bundle();
        bundle.putSerializable("bluetoothDeviceWrapper", bluetoothDeviceWrapper);
        bundle.putSerializable("commandSet", (Serializable) commandSet);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, REQUEST_MODIFY_PARAMETER);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_parameter_modify_information;
    }

    @Override
    public void refreshFooter() {

    }

    @Override
    public void refreshHeader() {
        headerLeftImage.setImageResource(R.drawable.goback);
        headerTitle.setText(getString(R.string.properitiesDefining));
    }

    @Override
    public void initView() {
        modifyInformationEdit.setText(modifyInformation);
    }

    @Override
    public void loadData() {
        activity = this;
        bluetoothDeviceWrapper = (BluetoothDeviceWrapper) getIntent().getSerializableExtra("bluetoothDeviceWrapper");
        modifyInformation.append(getResources().getString(R.string.name) + " " + bluetoothDeviceWrapper.getName() + "\r\n");
        modifyInformation.append(getResources().getString(R.string.addr) + " " + bluetoothDeviceWrapper.getAddress() + "\r\n\r\n");
        commandSet = (Set<String>) getIntent().getSerializableExtra("commandSet");
        BLE_MODE = getIntent().getBooleanExtra("mode", false);
        if (BLE_MODE) {
            fscBleCentralApi = FscBleCentralApiImp.getInstance(activity);
            fscBleCentralApi.connect(bluetoothDeviceWrapper.getAddress());
        } else {
            fscSppApi = FscSppApiImp.getInstance(activity);
            fscSppApi.connect(bluetoothDeviceWrapper.getAddress());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if (BLE_MODE) {
                fscBleCentralApi.disconnect();
            } else {
                fscSppApi.disconnect();
            }
            setResult(modifyResult);
            finish();
        }
        return true;
    }

    @OnClick(R.id.header_left_image)
    public void goBack() {
        if (BLE_MODE) {
            fscBleCentralApi.disconnect();
        } else {
            fscSppApi.disconnect();
        }
        setResult(modifyResult);
        activity.finish();
    }

    public void addState(String string) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int msecond = c.get(Calendar.MILLISECOND);
        String strTime = String.format("【%02d:%02d:%02d.%03d】", hour, minute, second, msecond);
        modifyInformation.append(strTime + string + "\r\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                modifyInformationEdit.setText(modifyInformation);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((fscBleCentralApi != null) || (fscSppApi != null)) {
            setCallBacks();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setCallBacks() {
        if (BLE_MODE) {
            fscBleCentralApi.setCallbacks(new FscBleCentralCallbacksImpInformation(new WeakReference<ParameterModifyInformationActivity>((ParameterModifyInformationActivity) activity)));
//            fscBleCentralApi.setCallbacks(new FscBleCentralCallbacksImp() {
//
//                @Override
//                public void atCommandCallBack(String command, String param, String status) {
//                    if (CommandBean.COMMAND_BEGIN.equals(command)) {
//                        if (status == CommandBean.COMMAND_SUCCESSFUL) {
//                            addState(getResources().getString(R.string.openEngineSuccess) + "\r\n");
//                        } else if (status == CommandBean.COMMAND_FAILED) {
//                            addState(getResources().getString(R.string.openEngineFailed) + "\r\n");
//                            fscBleCentralApi.disconnect();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setResult(modifyResult);
//                                    activity.finish();
//                                }
//                            }, 2500);
//                        } else if (status == CommandBean.COMMAND_TIME_OUT) {
//                            addState(getResources().getString(R.string.openEngineTimeOut) + "\r\n");
//                            fscBleCentralApi.disconnect();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setResult(modifyResult);
//                                    activity.finish();
//                                }
//                            }, 2500);
//                        }
//                    } else if (commandSet.contains(command)) {
//                        if (status == CommandBean.COMMAND_SUCCESSFUL) {
//                            if (command.contains("=")) {
//                                /**
//                                 *  modify parameter
//                                 */
//                                addState(getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + getResources().getString(R.string.success) + "\r\n");
//                            } else {
//                                /**
//                                 * inquery information
//                                 */
//                                addState(getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + getResources().getString(R.string.success));
//                                addState(param + "\r\n");
//                            }
//                        } else if (status == CommandBean.COMMAND_NO_NEED) {
//                            addState(getResources().getString(R.string.same) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + "\r\n");
//                        } else if (status == CommandBean.COMMAND_FAILED) {
//                            if (command.contains("=")) {
//                                /**
//                                 *  modify parameter
//                                 */
//                                addState(getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + getResources().getString(R.string.failed) + "\r\n");
//                            } else {
//                                /**
//                                 * inquery information
//                                 */
//                                addState(getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + getResources().getString(R.string.failed) + "\r\n");
//                            }
//                        } else if (status == CommandBean.COMMAND_TIME_OUT) {
//                            if (command.contains("=")) {
//                                /**
//                                 *  modify parameter
//                                 */
//                                addState(getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + getResources().getString(R.string.timeout) + "\r\n");
//                            } else {
//                                /**
//                                 * inquery information
//                                 */
//                                addState(getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + getResources().getString(R.string.timeout) + "\r\n");
//                            }
//                            fscBleCentralApi.disconnect();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setResult(modifyResult);
//                                    activity.finish();
//                                }
//                            }, 2500);
//                        }
//                    } else if (status == CommandBean.COMMAND_FINISH) {
//                        /**
//                         * remember to disconnect after the modification is complete
//                         */
//                        addState(getResources().getString(R.string.modifyComplete) + "\r\n");
//                        String temp = new String(modifyInformation);
//                        if (temp.contains(getResources().getString(R.string.failed))) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                        } else {
//                            modifyResult = MODIFY_SUCCESSFUL;
//                        }
//                        fscBleCentralApi.disconnect();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                setResult(modifyResult);
//                                activity.finish();
//                            }
//                        }, 3000);
//                    }
//                }
//
//                @Override
//                public void blePeripheralConnected(BluetoothGatt gatt, BluetoothDevice device) {
//                    addState(getResources().getString(R.string.connected));
//                    /**
//                     * after the connection is successful, it is recommended to wait for a while before modifying
//                     */
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            fscBleCentralApi.sendATCommand(commandSet);
//                        }
//                    }, 2500);
//                }
//
//                @Override
//                public void blePeripheralDisonnected(BluetoothGatt gatt, BluetoothDevice device) {
//                    addState(getResources().getString(R.string.disconnected));
//                }
//            });
        } else {
            fscSppApi.setCallbacks(new FscSppCallbacksImpInformation(new WeakReference<ParameterModifyInformationActivity>((ParameterModifyInformationActivity) activity)));
//            fscSppApi.setCallbacks(new FscSppCallbacksImp() {
//                @Override
//                public void sppConnected(BluetoothDevice device) {
//                    addState(getResources().getString(R.string.connected));
//                    /**
//                     * after the connection is successful, it is recommended to wait for a while before modifying
//                     */
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            fscSppApi.sendATCommand(commandSet);
//                        }
//                    }, 2000);
//                }
//
//                @Override
//                public void sppDisconnected(BluetoothDevice device) {
//                    addState(getResources().getString(R.string.disconnected));
//                }
//
//                @Override
//                public void atCommandCallBack(String command, String param, String status) {
//                    if (CommandBean.COMMAND_BEGIN.equals(command)) {
//                        if (status == CommandBean.COMMAND_SUCCESSFUL) {
//                            addState(getResources().getString(R.string.openEngineSuccess) + "\r\n");
//                        } else if (status == CommandBean.COMMAND_FAILED) {
//                            addState(getResources().getString(R.string.openEngineFailed) + "\r\n");
//                            fscSppApi.disconnect();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setResult(modifyResult);
//                                    activity.finish();
//                                }
//                            }, 2500);
//                        } else if (status == CommandBean.COMMAND_TIME_OUT) {
//                            addState(getResources().getString(R.string.openEngineTimeOut) + "\r\n");
//                            fscSppApi.disconnect();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setResult(modifyResult);
//                                    activity.finish();
//                                }
//                            }, 2500);
//                        }
//                    } else if (commandSet.contains(command)) {
//                        if (status == CommandBean.COMMAND_SUCCESSFUL) {
//                            if (command.contains("=")) {
//                                /**
//                                 *  modify parameter
//                                 */
//                                addState(getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + getResources().getString(R.string.success) + "\r\n");
//                            } else {
//                                /**
//                                 * inquery information
//                                 */
//                                addState(getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + getResources().getString(R.string.success));
//                                addState(param + "\r\n");
//                            }
//                        } else if (status == CommandBean.COMMAND_NO_NEED) {
//                            addState(getResources().getString(R.string.same) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + "\r\n");
//                        } else if (status == CommandBean.COMMAND_FAILED) {
//                            if (command.contains("=")) {
//                                /**
//                                 *  modify parameter
//                                 */
//                                addState(getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + getResources().getString(R.string.failed) + "\r\n");
//                            } else {
//                                /**
//                                 * inquery information
//                                 */
//                                addState(getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + getResources().getString(R.string.failed) + "\r\n");
//                            }
//                        } else if (status == CommandBean.COMMAND_TIME_OUT) {
//                            if (command.contains("=")) {
//                                /**
//                                 *  modify parameter
//                                 */
//                                addState(getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + getResources().getString(R.string.timeout) + "\r\n");
//                            } else {
//                                /**
//                                 * inquery information
//                                 */
//                                addState(getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + getResources().getString(R.string.timeout) + "\r\n");
//                            }
//                            fscSppApi.disconnect();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                            handler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setResult(modifyResult);
//                                    activity.finish();
//                                }
//                            }, 2500);
//                        }
//                    } else if (status == CommandBean.COMMAND_FINISH) {
//                        /**
//                         * remember to disconnect after the modification is complete
//                         */
//                        addState(getResources().getString(R.string.modifyComplete) + "\r\n");
//                        String temp = new String(modifyInformation);
//                        if (temp.contains(getResources().getString(R.string.failed))) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    modifyInformationEdit.setTextColor(getResources().getColor(R.color.red));
//                                }
//                            });
//                        } else {
//                            modifyResult = MODIFY_SUCCESSFUL;
//                        }
//                        fscSppApi.disconnect();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                setResult(modifyResult);
//                                activity.finish();
//                            }
//                        }, 3000);
//                    }
//                }
//            });
        }
    }

    public FscBleCentralApi getFscBleCentralApi() {
        return fscBleCentralApi;
    }

    public FscSppApi getFscSppApi() {
        return fscSppApi;
    }

    public Handler getHandler() {
        return handler;
    }

    public Set<String> getCommandSet() {
        return commandSet;
    }

    public EditText getModifyInformationEdit() {
        return modifyInformationEdit;
    }

    public int getModifyResult() {
        return modifyResult;
    }

    public void setModifyResult(int modifyResult) {
        this.modifyResult = modifyResult;
    }

    public StringBuffer getModifyInformation() {
        return modifyInformation;
    }

    public boolean isNoNeed() {
        return isNoNeed;
    }

    public void setNoNeed(boolean noNeed) {
        isNoNeed = noNeed;
    }

}
