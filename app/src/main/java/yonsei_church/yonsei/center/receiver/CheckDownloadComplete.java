package yonsei_church.yonsei.center.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import yonsei_church.yonsei.center.app.AppConst;

import static android.content.Context.MODE_PRIVATE;

public class CheckDownloadComplete extends BroadcastReceiver {
    SQLiteDatabase contentDB = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

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

                            String filePath = urlDecode(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
                            String title = filePath.substring(filePath.lastIndexOf("/")+1, filePath.lastIndexOf("."));
                            String source = urlDecode(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)));
                            String image = urlDecode(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)));

                            Toast.makeText(context, "다운로드 완료", Toast.LENGTH_SHORT).show();

                            contentDB = context.openOrCreateDatabase(AppConst.DB_NAME, MODE_PRIVATE, null);
                            contentDB.execSQL("INSERT INTO TB_DOWNLOAD"
                                    + " (url, path, title, image, downDate)  VALUES ('" + source + "', '" +  filePath +"', '" + title  + "', + '" + image + "', datetime('now', 'localtime'))");



                            String sql = "SELECT url, path, title, downDate from TB_DOWNLOAD";
                            Cursor results = contentDB.rawQuery(sql, null);

                            results.moveToFirst();

                            while(!results.isAfterLast()){
                                /*int id = results.getInt(0);
                                String voca = results.getString(1);*/
                                Log.d("DownloadManager1", results.getString(0) + "_____________" + results.getString(1) + "__________" + results.getString(2) + "_________" + results.getString(3));
                                results.moveToNext();
                            }
                            results.close();

                            contentDB.close();

                        } catch (Exception ex) {

                        }
                        //file contains downloaded file name

                        // do your stuff here on download success

                    }
                }
            }
            cursor.close();
        }
    }

    public String urlDecode(String str) {
        String result = "";
        try {
            result = URLDecoder.decode(str, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }
}
