package yonsei_church.yonsei.center.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import yonsei_church.yonsei.center.R;

public class AlertDialogActivity  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide activity title
        setContentView(R.layout.activity_my_alert_dialog);

        AlertDialog.Builder Builder=new AlertDialog.Builder(this)
                .setMessage("다운로드가 완료되었습니다. 다운로드 리스트로 이동하시겠습니까?")
                .setTitle("연세중앙교회")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialogActivity.this.finish();
                    }
                })
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), DownloadListActivity.class);
                        startActivity(intent);
                        AlertDialogActivity.this.finish();
                    }
                });
        AlertDialog alertDialog=Builder.create();
        alertDialog.show();

    }

}
