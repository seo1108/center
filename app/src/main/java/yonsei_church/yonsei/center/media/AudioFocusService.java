package yonsei_church.yonsei.center.media;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import yonsei_church.yonsei.center.app.AppConst;

public class AudioFocusService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int focusResult = am.requestAudioFocus(focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN); // 이건 focusChangeListener를 보면 알 수 있다.

        if (focusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { // 오디오 써도 된다는 허락을 맡게 되면
            Log.d("AUDIOFOCUS", "5");
            //재생 / 일시정지 코드
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    AudioManager am =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                            // Lower the volume while ducking.
                            //mediaPlayer.setVolume(0.2f, 0.2f);
                            Log.d("AUDIOFOCUS", "1");
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            //pause();
                            Log.d("AUDIOFOCUS", "2");
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS) :
                            /*stop();
                            ComponentName component =new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
                            am.unregisterMediaButtonEventReceiver(component);*/
                            Log.d("AUDIOFOCUS", "3");

                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(AppConst.NOTIFICATION_ID);
                            Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                            stopService(intent);

                            Intent intent1 = new Intent(getApplicationContext(), MediaPlayerService.class);
                            intent1.setAction(MediaPlayerService.ACTION_PLAY);
                            AppConst.MEDIA_MP3_ISPLAY = false;
                            startService(intent1);

                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN) :
                            // Return the volume to normal and resume if paused.
                            //mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            Log.d("AUDIOFOCUS", "4");


                            NotificationManager notificationManager1 = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager1.cancel(AppConst.NOTIFICATION_ID);
                            Intent intent2 = new Intent( getApplicationContext(), MediaPlayerService.class );
                            stopService(intent2);

                            Intent intent11 = new Intent(getApplicationContext(), MediaPlayerService.class);
                            intent11.setAction(MediaPlayerService.ACTION_PLAY);
                            AppConst.MEDIA_MP3_ISPLAY = true;
                            startService(intent11);


                            break;
                        default: break;
                    }
                }
            };
}
