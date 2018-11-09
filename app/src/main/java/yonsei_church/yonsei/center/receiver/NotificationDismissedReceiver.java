package yonsei_church.yonsei.center.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.activities.AudioActivity;
import yonsei_church.yonsei.center.media.MediaPlayerService;

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("notificationId");
        /* Your code to handle the event here */
        Log.d("NOTIFICATION", "DISMiSS " + notificationId);
        context.stopService(new Intent(context, MediaPlayerService.class));
    }
}
