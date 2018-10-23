package yonsei_church.yonsei.center.fcm;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import yonsei_church.yonsei.center.activities.MainActivity;
import yonsei_church.yonsei.center.R;

/**
 * Created by Administrator on 2018-07-09.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = FirebaseMessagingService.class.getSimpleName();


    // 메시지 수신
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "onMessageReceived");

        /*Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("content");

        Log.i(TAG, "onMessageReceived " + title + "_________" + message);*/

        Log.d("FCMMSG", remoteMessage.getData().toString());

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        //sendNotification(bundle);
        sendNotification(bundle);

    }

    private void sendNotification(Bundle bundle) {
        String message = bundle.getString("content");

        Intent intent = null;
        if (isAppIsInBackground(this)) {
            Log.d(TAG, "######################################");
            Log.d(TAG, "FCM App is Not Running");
            Log.d(TAG, "######################################");
            // 앱이 실행중이 아닌 경우
            // 앱 시작
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            // 앱이 실행중일 경우
            // 새로운 태스크로 메인액티비티 시작
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        int id = UUID.randomUUID().hashCode();
        String channelId = String.valueOf(id);

        String image = bundle.getString("image");

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder;

        if (null != image && !"".equals(image)) {
            notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_launcher)
                            //.setContentTitle("연세중앙교회")
                            //.setContentText(message)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(getBitmapfromUrl(image))
                                    .setBigContentTitle("연세중앙교회")
                                    .setSummaryText(message))
                            .setContentIntent(pendingIntent);
        } else {
            notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setContentTitle("연세중앙교회")
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);
        }


        //notificationBuilder.setLargeIcon(getBitmapfromUrl(image));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(id/* ID of notification */, notificationBuilder.build());
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }


    /*private void sendNotification(Bundle bundle) {
        Intent intent = null;
        Activity currentActivity = GlobalApplication.getCurrentActivity();

// 푸시 클릭 시 처리
        if (isAppIsInBackground(this)) {
            Log.d(TAG, "######################################");
            Log.d(TAG, "FCM App is Not Running");
            Log.d(TAG, "######################################");
            // 앱이 실행중이 아닌 경우
            // 앱 시작
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            // 앱이 실행중일 경우
            // 새로운 태스크로 메인액티비티 시작
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        String message = bundle.getString("content");
        if (message != null) {
            Log.d(TAG, "FCM Notification " + message);
            int id = UUID.randomUUID().hashCode();
            Notification.Builder builder = new Notification.Builder(this);
            // 작은 아이콘 이미지.
            builder.setSmallIcon(R.mipmap.ic_launcher);
            // 알림이 출력될 때 상단에 나오는 문구.
            builder.setTicker(bundle.getString("message"));
            // 알림 출력 시간.
            builder.setWhen(System.currentTimeMillis());
            // 알림 제목.
            builder.setContentTitle("연세중앙교회");
            // 알림 내용.
            builder.setContentText(message);
            // 알림시 사운드, 진동, 불빛을 설정 가능.
            //builder.setDefaults(Notification.DEFAULT_SOUND);

            // 앱이 실행중이고 채팅창일때 진동알림 안함 & sendBroadcast
            if (isAppIsInBackground(this) == false) {
            } else {
                builder.setDefaults(Notification.DEFAULT_VIBRATE);
            }
            // 알림 터치시 반응.
            PendingIntent pendingIntent = PendingIntent.getActivity(this, id, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            builder.setContentIntent(pendingIntent);
            // 알림 터치시 반응 후 알림 삭제 여부.
            builder.setAutoCancel(true);
            // 우선순위.
            builder.setPriority(Notification.PRIORITY_MAX);
            // 고유ID로 알림을 생성.
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(id, builder.build());

        }
    }*/

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
}
