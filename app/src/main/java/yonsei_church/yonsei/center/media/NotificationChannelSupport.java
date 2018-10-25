package yonsei_church.yonsei.center.media;

import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;

import yonsei_church.yonsei.center.R;

public class NotificationChannelSupport {
    public NotificationChannelSupport() {
    }

    public void createNotificationChannel(Context ctx, String ChannelID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.channel_notification_name);
            String description = ctx.getString(R.string.channel_notification_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(ChannelID, name, importance);
            channel.setDescription(description);


            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
