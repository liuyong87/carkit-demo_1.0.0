package com.feasycom.feasyblue.Controler;


import android.bluetooth.BluetoothDevice;

import com.feasycom.bean.CommandBean;
import com.feasycom.controler.FscSppCallbacksImp;
import com.feasycom.feasyblue.Activity.ParameterModifyInformationActivity;
import com.feasycom.feasyblue.R;
import com.feasycom.util.LogUtil;

import java.lang.ref.WeakReference;

import static com.feasycom.feasyblue.Activity.ParameterModifyInformationActivity.MODIFY_SUCCESSFUL;

public class FscSppCallbacksImpInformation extends FscSppCallbacksImp {
    private final String TAG = "SppCallBacks";
    private WeakReference<ParameterModifyInformationActivity> activityWeakReference;

    public FscSppCallbacksImpInformation(WeakReference<ParameterModifyInformationActivity> activityWeakReference) {
        this.activityWeakReference = activityWeakReference;
    }

    @Override
    public void sppConnected(BluetoothDevice device) {
        if (activityWeakReference.get() == null) {
            return;
        }
        activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.connected));
        /**
         * after the connection is successful, it is recommended to wait for a while before modifying
         */
        activityWeakReference.get().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activityWeakReference.get().getFscSppApi().sendATCommand(activityWeakReference.get().getCommandSet());
            }
        }, 2000);
    }

    @Override
    public void sppDisconnected(BluetoothDevice device) {
        if (activityWeakReference.get() == null) {
            return;
        }
        activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.disconnected));
    }

    @Override
    public void atCommandCallBack(String command, String param, String status) {
        if (activityWeakReference.get() == null) {
            return;
        }

        if (CommandBean.COMMAND_BEGIN.equals(command)) {
            if (status == CommandBean.COMMAND_SUCCESSFUL) {
                activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.openEngineSuccess) + "\r\n");
            } else if (status == CommandBean.COMMAND_FAILED) {
                activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.openEngineFailed) + "\r\n");
                activityWeakReference.get().getFscSppApi().disconnect();
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().getModifyInformationEdit().setTextColor(activityWeakReference.get().getResources().getColor(R.color.red));
                    }
                });
                activityWeakReference.get().getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().setResult(activityWeakReference.get().getModifyResult());
                        activityWeakReference.get().finish();
                    }
                }, 2500);
            } else if (status == CommandBean.COMMAND_TIME_OUT) {
                activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.openEngineTimeOut) + "\r\n");
                activityWeakReference.get().getFscSppApi().disconnect();
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().getModifyInformationEdit().setTextColor(activityWeakReference.get().getResources().getColor(R.color.red));
                    }
                });
                activityWeakReference.get().getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().setResult(activityWeakReference.get().getModifyResult());
                        activityWeakReference.get().finish();
                    }
                }, 2500);
            }
        } else if (activityWeakReference.get().getCommandSet().contains(command)) {
            if (status == CommandBean.COMMAND_SUCCESSFUL) {
                if (command.contains("=")) {
                    /**
                     *  modify parameter
                     */
                    activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + activityWeakReference.get().getResources().getString(R.string.success) + "\r\n");
                    activityWeakReference.get().setNoNeed(false);
                } else {
                    /**
                     * inquery information
                     */
                    activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + activityWeakReference.get().getResources().getString(R.string.success));
                    activityWeakReference.get().addState(param + "\r\n");
                }
            } else if (status == CommandBean.COMMAND_NO_NEED) {
                activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.same) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + "\r\n");
            } else if (status == CommandBean.COMMAND_FAILED) {
                if (command.contains("=")) {
                    /**
                     *  modify parameter
                     */
                    activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + activityWeakReference.get().getResources().getString(R.string.failed) + "\r\n");
                } else {
                    /**
                     * inquery information
                     */
                    activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + activityWeakReference.get().getResources().getString(R.string.failed) + "\r\n");
                }
            } else if (status == CommandBean.COMMAND_TIME_OUT) {
                if (command.contains("=")) {
                    /**
                     *  modify parameter
                     */
                    activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.modify) + " " + command.substring(command.indexOf("+") + 1, command.indexOf("=")) + " " + activityWeakReference.get().getResources().getString(R.string.timeout) + "\r\n");
                } else {
                    /**
                     * inquery information
                     */
                    activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.read) + " " + command.substring(command.indexOf("+") + 1, command.length()) + " " + activityWeakReference.get().getResources().getString(R.string.timeout) + "\r\n");
                }
                activityWeakReference.get().getFscSppApi().disconnect();
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().getModifyInformationEdit().setTextColor(activityWeakReference.get().getResources().getColor(R.color.red));
                    }
                });
            }
        } else if (status == CommandBean.COMMAND_FINISH) {
            /**
             * remember to disconnect after the modification is complete
             */
            if (!activityWeakReference.get().isNoNeed()) {
                activityWeakReference.get().addState(activityWeakReference.get().getResources().getString(R.string.modifyComplete) + "\r\n");
            }
            String temp = new String(activityWeakReference.get().getModifyInformation());
            if (temp.contains(activityWeakReference.get().getResources().getString(R.string.failed)) || (activityWeakReference.get().isNoNeed())) {
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().getModifyInformationEdit().setTextColor(activityWeakReference.get().getResources().getColor(R.color.red));
                    }
                });
            } else {
                activityWeakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().getModifyInformationEdit().setTextColor(activityWeakReference.get().getResources().getColor(R.color.dar_green));
                    }
                });
                activityWeakReference.get().setModifyResult(MODIFY_SUCCESSFUL);
                activityWeakReference.get().getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activityWeakReference.get().setResult(activityWeakReference.get().getModifyResult());
                        activityWeakReference.get().finish();
                    }
                }, 3000);
            }
            activityWeakReference.get().getFscSppApi().disconnect();
        }

    }
}
