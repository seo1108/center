package yonsei_church.yonsei.center.media;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import yonsei_church.yonsei.center.app.AppConst;

public class AudioFocusService extends Service {
    private static Timer mTimer;
    AudioManager am;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

       /*int focusResult = am.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN); // 이건 focusChangeListener를 보면 알 수 있다.

        if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { // 오디오 써도 된다는 허락을 맡게 되면
            Log.d("AUDIOFOCUS", "5");
            //재생 / 일시정지 코드
        } else {
            Log.d("AUDIOFOCUS", "6");
        }*/

        AudioManager myAudioManager;
        myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new AudioFocusService.mainTask(), 0, 1000);

        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            currentPositionHandler.sendEmptyMessage(0);
        }
    }
    private final Handler currentPositionHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            try {
                Log.d("AudioFocusService", AppConst.MEDIA_NOTIFICATION_ISPLAY + "____" + am.isMusicActive() + "");
                try {
                    if (AppConst.MEDIA_NOTIFICATION_ISPLAY && am.isMusicActive()) {
                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(AppConst.NOTIFICATION_ID);
                        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                        stopService(intent);

                        Intent intent1 = new Intent(getApplicationContext(), MediaPlayerService.class);
                        intent1.setAction(MediaPlayerService.ACTION_PLAY);
                        AppConst.MEDIA_MP3_ISPLAY = false;
                        startService(intent1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } catch (Exception ex) {
                Log.d("AudioFocusService", ex.toString());

            }
        }
    };
}
