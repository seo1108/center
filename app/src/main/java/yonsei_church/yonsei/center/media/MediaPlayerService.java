package yonsei_church.yonsei.center.media;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.activities.AudioActivity;
import yonsei_church.yonsei.center.activities.PlayerActivity;
import yonsei_church.yonsei.center.activities.WebViewActivity;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.receiver.NotificationDismissedReceiver;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;

public class MediaPlayerService  extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {        // , AudioManager.OnAudioFocusChangeListener
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_SEEK_TO = "action_seek_to";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;
    private String mAudioLink;
    private int mPosition;
    private String mTitle;
    private String mImage;
    private boolean mIsPlay = true;
    private boolean isFirstLoad = true;

    private NotificationManager notificationManager;

    private boolean isServiceStart = false;

    private static Timer mTimer;

    WifiManager.WifiLock wifiLock;
    PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onPrepared(MediaPlayer player) {
        player.seekTo(AppConst.MEDIA_CURRENT_POSITION);

        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        player.start();
        AppConst.MEDIA_NOTIFICATION_ISPLAY = true;

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new mainTask(), 0, 1000);

        isFirstLoad = false;

    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        stopSelf();
    }

    @Override
    public void onDestroy() {
        AppConst.MEDIA_NOTIFICATION_ISPLAY = false;
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        if (mMediaPlayer != null) mMediaPlayer.release();

        try {
            if (!wakeLock.isHeld()) wakeLock.acquire();
            if (null != wifiLock) wifiLock.release();
            mTimer.cancel();
            mTimer = null;
        } catch (Exception ex) {

        }
    }


    @SuppressLint("NewApi")
    private void handleIntent(Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;

        String action = intent.getAction();
        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_FAST_FORWARD ) ) {
            mController.getTransportControls().fastForward();
        } else if( action.equalsIgnoreCase( ACTION_REWIND ) ) {
            mController.getTransportControls().rewind();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            mController.getTransportControls().stop();
        } else if( action.equalsIgnoreCase( ACTION_SEEK_TO ) ) {
            mController.getTransportControls().seekTo(mPosition);
        }
    }


    private NotificationCompat.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }




    private void buildNotification( NotificationCompat.Action action ) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), AppConst.NOTIFICATION_MP_CHANNEL_ID);
        NotificationChannelSupport notificationChannelSupport = new NotificationChannelSupport();
        notificationChannelSupport.createNotificationChannel(getApplicationContext(), AppConst.NOTIFICATION_MP_CHANNEL_ID);

        Intent intent = new Intent(getApplicationContext(), AudioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("NOTIFICATION", AppConst.MEDIA_CURRENT_POSITION + "___" + AppConst.MEDIA_MP3_TITLE);
        builder.setContentTitle("연세중앙교회")
                .setContentText(AppConst.MEDIA_MP3_TITLE)
                .setSmallIcon(R.drawable.exo_notification_small_icon)
                //.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))

                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setColorized(true)
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(createOnDismissedIntent(getApplicationContext(), AppConst.NOTIFICATION_ID));

        builder.addAction(generateAction(R.drawable.exo_icon_previous, "Rewind", ACTION_REWIND));
        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.exo_icon_next, "Fast Foward", ACTION_FAST_FORWARD));
        builder.addAction(generateAction(android.R.drawable.ic_lock_power_off, "Next", ACTION_NEXT));
        int[] actionsViewIndexs = new int[]{0, 1, 2};

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(actionsViewIndexs));
        /*notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());*/
        startForeground(AppConst.NOTIFICATION_ID, builder.build());
    }

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
    }

    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {

        //mAudioLink = intent.getStringExtra("streamLink");
        //mPosition = intent.getIntExtra("position", 0);
        //mTitle = intent.getStringExtra("title");
        //mImage = intent.getStringExtra("image");
        //mIsPlay = intent.getBooleanExtra("isPlay", true);

        //AppConst.MEDIA_MP3_URL = mAudioLink;

        //Log.d("PREPARED1", "START__" + AppConst.MEDIA_MP3_TITLE + "__" + AppConst.MEDIA_MP3_URL + "__" + AppConst.MEDIA_MP3_ISPLAY + "___" + AppConst.MEDIA_CURRENT_POSITION);

        if( mManager == null ) {
            if (!isServiceStart) {
                initMediaSessions();
                isServiceStart = true;
                if (intent.getAction().equals(ACTION_PLAY) || intent.getAction().equals(ACTION_SEEK_TO)) {
                    Log.e( "MediaPlayerService", "START SERVICE " + mPosition);
                    try {
                        mMediaPlayer.setDataSource(AppConst.MEDIA_MP3_URL);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.setOnPreparedListener(this);
                    mMediaPlayer.prepareAsync();

                }
            }


        }

        handleIntent( intent );
        //return super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }


    @SuppressLint("NewApi")
    private void initMediaSessions() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        if (!wakeLock.isHeld()) wakeLock.acquire();

       wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock");

        if (!wifiLock.isHeld()) wifiLock.acquire();



        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback(){
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     Log.e( "MediaPlayerService", "onPlay " + mPosition);
                                     //buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                                     buildNotification( generateAction( R.drawable.exo_controls_pause, "Pause", ACTION_PAUSE ) );
                                     AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                     audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                                     /*if (!isFirstLoad) {
                                         AppConst.MEDIA_MP3_ISPLAY = true;
                                     }*/
                                     mMediaPlayer.start();
                                     AppConst.MEDIA_NOTIFICATION_ISPLAY = true;

                                     //am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.e( "MediaPlayerService", "onPause");
                                     //buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                     buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));
                                     //AppConst.MEDIA_MP3_ISPLAY = false;
                                     mMediaPlayer.pause();
                                     if (!wifiLock.isHeld()) wifiLock.acquire();
                                     AppConst.MEDIA_NOTIFICATION_ISPLAY = false;



                                    // length=mMediaPlayer.getCurrentPosition();
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     Log.e( "MediaPlayerService", "onSkipToNext");
                                     //Change media here
                                    // buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_NEXT ) );
                                     buildNotification( generateAction(R.drawable.exo_controls_pause, "Pause", ACTION_NEXT ) );

                                     //Stop media player here
                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                     notificationManager.cancel(AppConst.NOTIFICATION_ID);
                                     Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                                     stopService(intent);
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     Log.e( "MediaPlayerService", "onSkipToPrevious");
                                     //Change media here
                                     //buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );

                                 }

                                 @Override
                                 public void onFastForward() {
                                     super.onFastForward();
                                     Log.e( "MediaPlayerService", "onFastForward " + mMediaPlayer.getCurrentPosition());
                                     //Manipulate current media here
                                     buildNotification( generateAction(R.drawable.exo_controls_pause, "Play", ACTION_PAUSE ) );
                                     if (mMediaPlayer.getCurrentPosition() + 5000 > mMediaPlayer.getDuration()) {
                                         mMediaPlayer.seekTo(mMediaPlayer.getDuration());
                                     } else {
                                         mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 5000);
                                     }

                                 }

                                 @Override
                                 public void onRewind() {
                                     super.onRewind();
                                     Log.e( "MediaPlayerService", "onRewind " + + mMediaPlayer.getCurrentPosition());
                                     //Manipulate current media here
                                     buildNotification( generateAction(R.drawable.exo_controls_pause, "Play", ACTION_PAUSE ) );
                                     if (mMediaPlayer.getCurrentPosition() - 5000 > 0) {
                                         mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() - 5000);
                                     } else {
                                         mMediaPlayer.seekTo(0);
                                     }
                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     Log.e( "MediaPlayerService", "onStop");
                                     AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                     audioManager.abandonAudioFocus(focusChangeListener);
                                     //Stop media player here
                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                     notificationManager.cancel( 1 );
                                     Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                                     stopService( intent );
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                     Log.e( "MediaPlayerService", "onSeek " + mPosition);
                                     buildNotification( generateAction( R.drawable.exo_controls_pause, "Play", ACTION_PAUSE ) );
                                     mMediaPlayer.seekTo(AppConst.MEDIA_SEEK_TO_POSITION);
                                     mMediaPlayer.start();
                                 }

                                 @Override
                                 public void onSetRating(Rating rating) {
                                     super.onSetRating(rating);
                                 }
                             }
        );
    }

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onAudioFocusChange(int focusChange) {

                    switch (focusChange) {

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                            // Lower the volume while ducking.
                            //mediaPlayer.setVolume(0.2f, 0.2f);
                            Log.d("MPAUDIOFOCUS", "1 " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
                            if (mMediaPlayer.isPlaying()) {
                                buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));
                                mMediaPlayer.pause();
                                if (!wifiLock.isHeld()) wifiLock.acquire();
                            }
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            //pause();
                            Log.d("MPAUDIOFOCUS", "2 " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                            try {
                                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                    buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));
                                    mMediaPlayer.pause();

                                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                    audioManager.abandonAudioFocus(focusChangeListener);

                                    if (!wifiLock.isHeld()) wifiLock.acquire();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS :
                            /*stop();
                            ComponentName component =new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
                            am.unregisterMediaButtonEventReceiver(component);*/
                            Log.d("MPAUDIOFOCUS", "3 " + AudioManager.AUDIOFOCUS_LOSS);
                            try {
                                if (mMediaPlayer != null) {
                                    buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));
                                    mMediaPlayer.pause();

                                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                    audioManager.abandonAudioFocus(focusChangeListener);

                                    if (!wifiLock.isHeld()) wifiLock.acquire();
                                }
                                /*if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                                    buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));
                                    mMediaPlayer.pause();

                                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                    audioManager.abandonAudioFocus(focusChangeListener);

                                    if (!wifiLock.isHeld()) wifiLock.acquire();
                                }*/
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            /*NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(AppConst.NOTIFICATION_ID);
                            Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                            stopService(intent);*/

                           /* Intent intent1 = new Intent(getApplicationContext(), MediaPlayerService.class);
                            intent1.setAction(MediaPlayerService.ACTION_PLAY);
                            AppConst.MEDIA_MP3_ISPLAY = false;
                            startService(intent1);
*/
                            break;

                        case AudioManager.AUDIOFOCUS_GAIN :
                            // Return the volume to normal and resume if paused.
                            //mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            Log.d("MPAUDIOFOCUS", "4 " + AudioManager.AUDIOFOCUS_GAIN);
                            buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));

                            /*NotificationManager notificationManager1 = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager1.cancel(AppConst.NOTIFICATION_ID);
                            Intent intent2 = new Intent( getApplicationContext(), MediaPlayerService.class );
                            stopService(intent2);

                            Intent intent11 = new Intent(getApplicationContext(), MediaPlayerService.class);
                            intent11.setAction(MediaPlayerService.ACTION_PLAY);
                            AppConst.MEDIA_MP3_ISPLAY = true;
                            startService(intent11);*/


                            break;
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT :
                            // Return the volume to normal and resume if paused.
                            //mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            Log.d("MPAUDIOFOCUS", "5 " + AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                            break;
                        default: break;
                    }
                }
            };


    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onUnbind(Intent intent) {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(focusChangeListener);

        mSession.release();
        return super.onUnbind(intent);
    }


    /*private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    AudioManager am =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    switch (focusChange) {

                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) :
                            // Lower the volume while ducking.
                            //mediaPlayer.setVolume(0.2f, 0.2f);
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            //pause();
                            break;

                        case (AudioManager.AUDIOFOCUS_LOSS) :
                            *//*stop();
                            ComponentName component =new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
                            am.unregisterMediaButtonEventReceiver(component);*//*
                            break;

                        case (AudioManager.AUDIOFOCUS_GAIN) :
                            // Return the volume to normal and resume if paused.
                            //mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            break;
                        default: break;
                    }
                }
            };
*/

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
                if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                    Log.d("AUDIOACTIVITY", "PREPARED0 CURRENT POSITION : " + mMediaPlayer.getCurrentPosition() + "___" + mAudioLink);
                    AppConst.MEDIA_DURATION = mMediaPlayer.getDuration();
                    AppConst.MEDIA_CURRENT_POSITION = mMediaPlayer.getCurrentPosition();
                } else {
                    //Log.d("AUDIOACTIVITY", "CURRENT POSITION : STOP");
                }
            } catch (Exception ex) {
                Log.d("MediaPlayerService", "CURRENT POSITION : EXCEPTION" + ex.toString());

            }
        }
    };
}

