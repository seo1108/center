package yonsei_church.yonsei.center.receiver;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import yonsei_church.yonsei.center.activities.AlertDialogActivity;
import yonsei_church.yonsei.center.activities.DownloadListActivity;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.app.DialogHelper;
import yonsei_church.yonsei.center.util.Util;

import static android.content.Context.MODE_PRIVATE;

public class CheckDownloadComplete extends BroadcastReceiver {
    SQLiteDatabase contentDB = null;
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        String action = intent.getAction();
        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Cursor cursor = manager.query(query);
            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0) {

                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    Long download_id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

                    // status contain Download Status
                    // download_id contain current download reference id

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        try {

                            String filePath = Util.urlDecode(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                            String title = filePath.substring(filePath.lastIndexOf("/")+1, filePath.lastIndexOf("."));
                            String source = Util.urlDecode(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)));
                            String image = Util.urlDecode(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)));



                            contentDB = context.openOrCreateDatabase(AppConst.DB_NAME, MODE_PRIVATE, null);
                            contentDB.execSQL("INSERT INTO TB_DOWNLOAD"
                                    + " (url, path, title, image, downDate)  VALUES ('" + source + "', '" +  filePath +"', '" + title  + "', '"+ image +"', datetime('now', 'localtime'))");

                            contentDB.close();

                            Intent i=new Intent(context.getApplicationContext(),AlertDialogActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);

                            //Toast.makeText(context, "다운로드 완료", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Toast.makeText(context, "다운로드 실패", Toast.LENGTH_SHORT).show();
                        }
                        //file contains downloaded file name

                        // do your stuff here on download success

                    }
                }
            }
            cursor.close();
        }
    }

    public void ShowYesNoDialog() {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent(mContext, DownloadListActivity.class);
                        mContext.startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("다운로드 화면으로 이동하시겠습니까?")
                .setPositiveButton("예", dialogClickListener)
                .setNegativeButton("아니오", dialogClickListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
}
