package yonsei_church.yonsei.center.media;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
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

public class MediaPlayerService  extends Service implements MediaPlayer.OnPreparedListener {
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_SEEK_TO = "actoin_seek_to";
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
    private String audioStreamLink;
    private int mPosition;

    private NotificationManager notificationManager;

    private boolean isServiceStart = false;

    private static Timer mTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /** Called when MediaPlayer is ready */
    @Override
    public void onPrepared(MediaPlayer player) {
        //player.start();
        player.seekTo(mPosition);
        player.start();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new mainTask(), 0, 1000);
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        if (mMediaPlayer != null) mMediaPlayer.release();
        mTimer.cancel();
        mTimer = null;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private NotificationCompat.Action generateAction( int icon, String title, String intentAction ) {
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( intentAction );
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification( NotificationCompat.Action action ) {
        Log.d("MEDIA001", "NOTIFICATION");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppConst.NOTIFICATION_MP_CHANNEL_ID);
        NotificationChannelSupport notificationChannelSupport = new NotificationChannelSupport();
        notificationChannelSupport.createNotificationChannel(this, AppConst.NOTIFICATION_MP_CHANNEL_ID);
        //PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, MediaPlayerService.class), 0);


        Intent intent = new Intent(this, AudioActivity.class );
        intent.putExtra("mediaUrl", audioStreamLink);
        intent.putExtra("position", mMediaPlayer.getCurrentPosition());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setAction(mMediaPlayer.getCurrentPosition() + "");
        intent.setAction(Long.toString(System.currentTimeMillis()));

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        //Bitmap large = Picasso.get().load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png");
        //Bitmap large = Glide.with(getApplicationContext()).asBitmap().load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png");

        builder.setContentTitle("연세중앙교회")
                .setContentText("강연입니다")
                .setSmallIcon(R.drawable.exo_notification_small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setColorized(true)
                .setColor(Color.parseColor("#f7da64"))
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(createOnDismissedIntent(getApplicationContext(), AppConst.NOTIFICATION_ID));

/*try {
            String picture = "http://i.stack.imgur.com/CE5lz.png";
            Bitmap bmp = Picasso.with(getApplicationContext()).load(picture).get();

            builder.setLargeIcon(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }*/



        //builder.addAction( generateAction( android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS ) );

/* builder.addAction( generateAction( android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND ) );
        builder.addAction( action );
        builder.addAction( generateAction( android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD ) );
        builder.addAction( generateAction( android.R.drawable.ic_lock_power_off, "Next", ACTION_NEXT ) );*/


        //builder.addAction( generateAction( R.drawable.exo_icon_previous, "Previous", ACTION_PREVIOUS ) );
        builder.addAction( generateAction( R.drawable.exo_icon_previous, "Rewind", ACTION_REWIND ) );
        builder.addAction( action );
        builder.addAction( generateAction( R.drawable.exo_icon_next, "Fast Foward", ACTION_FAST_FORWARD ) );
        builder.addAction( generateAction( android.R.drawable.ic_lock_power_off, "Next", ACTION_NEXT ) );
        //int[] actionsViewIndexs = new int[]{1,2,3};
        int[] actionsViewIndexs = new int[]{0,1,2};

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(actionsViewIndexs));
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
    }
/*
    public void sendOnSeekBar(View v) {
        final int progressMax = 100;

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, AppConst.NOTIFICATION_MP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentText("Seek")
                .setContentText("SeeKBar")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, false);

        notificationManager.notify(AppConst.NOTIFICATION_ID, notification.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for (int progress = 0; progress < progressMax; progress += 10) {
                    notification.setProgress(progressMax, progress, false);
                    notificationManager.notify(AppConst.NOTIFICATION_ID, notification.build());
                    SystemClock.sleep(1000);
                }
                notification.setContentText("music end")
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                notificationManager.notify(AppConst.NOTIFICATION_ID, notification.build());
            }
        }).start();
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void buildNotification( NotificationCompat.Action action ) {
        Log.d("MEDIA001", "NOTIFICATION");

        final int progressMax = mMediaPlayer.getDuration();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppConst.NOTIFICATION_MP_CHANNEL_ID);
        NotificationChannelSupport notificationChannelSupport = new NotificationChannelSupport();
        notificationChannelSupport.createNotificationChannel(this, AppConst.NOTIFICATION_MP_CHANNEL_ID);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 1, new Intent(this, MediaPlayerService.class), 0);

        builder.setContentTitle("연세중앙교회")
                .setContentText("강연입니다")
                .setSmallIcon(R.drawable.exo_notification_small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentIntent(contentPendingIntent)
                .setProgress(mMediaPlayer.getDuration(), 1000, true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(progressMax, 0, false)
                .setDeleteIntent(createOnDismissedIntent(getApplicationContext(), AppConst.NOTIFICATION_ID));

        builder.addAction( generateAction( R.drawable.exo_icon_previous, "Rewind", ACTION_REWIND ) );
        builder.addAction( action );
        builder.addAction( generateAction( R.drawable.exo_icon_next, "Fast Foward", ACTION_FAST_FORWARD ) );
        builder.addAction( generateAction( android.R.drawable.ic_lock_power_off, "Next", ACTION_NEXT ) );
        int[] actionsViewIndexs = new int[]{0,1,2};

        //builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(actionsViewIndexs));
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                for (int progress = 0; progress < progressMax; progress = mMediaPlayer.getCurrentPosition()) {
                    builder.setProgress(progressMax, progress, false);
                    notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
                    Log.d("MediaPlayerService", mMediaPlayer.getCurrentPosition() + "");
                    SystemClock.sleep(1000);
                }
                builder.setContentText("")
                        .setProgress(0, 0, false)
                        .setOngoing(false);
                notificationManager.notify(AppConst.NOTIFICATION_ID, builder.build());
            }
        }).start();

    }
*/

    private PendingIntent createOnDismissedIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("notificationId", notificationId);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context.getApplicationContext(),
                        notificationId, intent, 0);
        return pendingIntent;
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

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public int onStartCommand(Intent intent, int flags, int startId) {
        audioStreamLink = intent.getStringExtra("streamLink");
        mPosition = intent.getIntExtra("position", 0);
        Log.d("MediaPlayerService", "GET POSITION : " + mPosition);
        if( mManager == null ) {
            if (!isServiceStart) {
                initMediaSessions();
                isServiceStart = true;
                if (intent.getAction().equals(ACTION_PLAY) || intent.getAction().equals(ACTION_SEEK_TO)) {
                    Log.e( "MediaPlayerService", "START SERVICE " + mPosition);
                    try {
                        mMediaPlayer.setDataSource(audioStreamLink);

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
        return Service.START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initMediaSessions() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mSession = new MediaSession(getApplicationContext(), "simple player session");
        mController =new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(new MediaSession.Callback(){
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     Log.e( "MediaPlayerService", "onPlay " + mPosition);
                                     //buildNotification( generateAction( android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE ) );
                                     buildNotification( generateAction( R.drawable.exo_controls_pause, "Pause", ACTION_PAUSE ) );
                                     mMediaPlayer.start();

                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.e( "MediaPlayerService", "onPause");
                                     //buildNotification(generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY));
                                     buildNotification(generateAction(R.drawable.exo_controls_play, "Play", ACTION_PLAY));

                                     mMediaPlayer.pause();
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
                                     buildNotification( generateAction(R.drawable.exo_controls_pause, "Pause", ACTION_PAUSE ) );
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
                                     buildNotification( generateAction(R.drawable.exo_controls_pause, "Pause", ACTION_PAUSE ) );
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
                                     buildNotification( generateAction( R.drawable.exo_controls_pause, "Pause", ACTION_PAUSE ) );
                                    /* mMediaPlayer.seekTo(mPosition*1000);
                                     mMediaPlayer.start();*/
                                 }

                                 @Override
                                 public void onSetRating(Rating rating) {
                                     super.onSetRating(rating);
                                 }
                             }
        );
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onUnbind(Intent intent) {
        mSession.release();
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
                if (null != mMediaPlayer && mMediaPlayer.isPlaying()) {
                    //Log.d("AUDIOACTIVITY", "CURRENT POSITION : " + mMediaPlayer.getCurrentPosition());
                    AppConst.MEDIA_CUURECT_POSITION = mMediaPlayer.getCurrentPosition();
                } else {
                    //Log.d("AUDIOACTIVITY", "CURRENT POSITION : STOP");
                }
            } catch (Exception ex) {
                Log.d("MediaPlayerService", "CURRENT POSITION : EXCEPTION" + ex.toString());

            }
        }
    };
}

