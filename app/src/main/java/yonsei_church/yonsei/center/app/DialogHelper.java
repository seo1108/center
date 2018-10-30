package yonsei_church.yonsei.center.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogHelper {
    public static void alert(Context context, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("연세중앙교회");
        alert.setMessage(message);
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public static void alert(Context context, String message, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("연세중앙교회");
        alert.setMessage(message);
        alert.setPositiveButton("확인", positiveListener);
        alert.setCancelable(false);
        alert.show();
    }

    public static void alert(Context context, String message, String positiveName, DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("연세중앙교회");
        alert.setMessage(message);
        alert.setCancelable(false);
        alert.setPositiveButton(positiveName, positiveListener);
        alert.show();
    }

}
