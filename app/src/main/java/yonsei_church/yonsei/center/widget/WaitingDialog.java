package yonsei_church.yonsei.center.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.view.WindowManager;
import android.os.Handler;
import java.util.logging.Logger;

import yonsei_church.yonsei.center.R;

public class WaitingDialog {
    private static volatile Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final Object waitingDialogLock = new Object();
    private static Dialog waitingDialog;


    private static Dialog getWaitingDialog(Context context) {
        synchronized (waitingDialogLock) {
            if (waitingDialog != null) {
                return waitingDialog;
            }

            waitingDialog = new Dialog(context, R.style.CustomProgressDialog);
            return waitingDialog;
        }
    }

    public static void showWaitingDialog(Context context) {
        showWaitingDialog(context, false);
    }

    public static void showWaitingDialog(final Context context, final boolean cancelable) {
        showWaitingDialog(context, cancelable, null);
    }

    public static void showWaitingDialog(final Context context, final boolean cancelable, final DialogInterface.OnCancelListener listener) {
        cancelWaitingDialog();
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                waitingDialog = getWaitingDialog(context);
                // here we set layout of progress dialog
                waitingDialog.setContentView(R.layout.layout_waiting_dialog);
                waitingDialog.setCancelable(cancelable);
                if (listener != null) {
                    waitingDialog.setOnCancelListener(listener);
                }
                waitingDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                waitingDialog.getWindow().setDimAmount(0.3f);
                waitingDialog.show();
            }
        });
    }

    public static void cancelWaitingDialog() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (waitingDialog != null) {
                        synchronized (waitingDialogLock) {
                            waitingDialog.cancel();
                            waitingDialog = null;
                        }
                    }
                } catch (Exception e) {

                }
            }
        });
    }
}
