package yonsei_church.yonsei.center.media;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.activities.WebViewActivity;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.receiver.NotificationDismissedReceiver;

public class DownloadContentService extends Service {
    DownloadManager downloadManager;
    private String mUrl;
    private String title;
    private NotificationManager notificationManager;

    public void download(String url, String filename) {
        try {

            downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(mUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            //request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, videoName);
            //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(), "centerChurchVideos/" + filename);
            request.setDestinationInExternalFilesDir(getApplicationContext(), null, "centerChurchVideos/" + filename);
            request.setTitle(mUrl);
            //request.setDescription("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png");
            Toast.makeText(getApplicationContext(), "다운로드 시작 " + mUrl, Toast.LENGTH_SHORT).show();
            Long reference = downloadManager.enqueue(request);
            Log.d("DOWNLOADVIDEO", reference + "");

            //buildNotification();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppConst.NOTIFICATION_DOWNLOAD_CHANNEL_ID);
        NotificationChannelSupport notificationChannelSupport = new NotificationChannelSupport();
        notificationChannelSupport.createNotificationChannel(this, AppConst.NOTIFICATION_DOWNLOAD_CHANNEL_ID);
        //PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, MediaPlayerService.class), 0);


        //Intent intent = new Intent(this, DownloadListActivity.class );
        Intent intent = new Intent(this, WebViewActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setAction(mMediaPlayer.getCurrentPosition() + "");
        intent.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Bitmap large = Picasso.get().load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png");
        //Bitmap large = Glide.with(getApplicationContext()).asBitmap().load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png");

        builder.setContentTitle("연세중앙교회")
                .setContentText("다운로드")
                .setSmallIcon(R.drawable.ic_launcher)
                //.setOngoing(true)
                .setColorized(true)
                .setColor(Color.parseColor("#f7da64"))
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(createOnDismissedIntent(getApplicationContext(), AppConst.DOWNLOAD_NOTIFICATION_ID));

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DOWNLOADCONTENT", "START");
        mUrl = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        download(mUrl, title);
        return Service.START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }
}
