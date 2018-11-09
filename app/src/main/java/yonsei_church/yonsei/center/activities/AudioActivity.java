package yonsei_church.yonsei.center.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.media.MediaPlayerService;
import yonsei_church.yonsei.center.receiver.NotificationDismissedReceiver;

public class AudioActivity extends AppCompatActivity implements Runnable { //, MediaPlayer.OnPreparedListener
    MediaPlayer mediaPlayer = new MediaPlayer();

    SeekBar seekBar;
    //SeekBar seekVolumn;
    ImageView imageView;
    TextView txtTitle;
    TextView txtClose;
    TextView seekBarHint;
    //boolean wasPlaying = true;
    //FloatingActionButton fab;
    //FloatingActionButton fabExit;

    ImageButton fab;
    ImageButton fabExit;

    private String mMediaUrl;
    private String mTitle;
    private String mImage;
    private String mTotalDuration;

    private boolean isExit = false;

    WifiManager.WifiLock wifiLock;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        // 롤리팝 이후 버전일 경우
        if (Build.VERSION.SDK_INT >= 21 ){
            try {
                /*Intent intent = new Intent(getApplicationContext(), NotificationDismissedReceiver.class);
                intent.putExtra("notificationId", AppConst.NOTIFICATION_MP_CHANNEL_ID);
*/
                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(AppConst.NOTIFICATION_ID);
                Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                stopService(intent);
            } catch (Exception ex) {

            }
        }

        mMediaUrl = getIntent().getStringExtra("mediaUrl");
        mTitle = getIntent().getStringExtra("title");
        mImage = getIntent().getStringExtra("image");

        //Postition = getIntent().getIntExtra("position", 0);

        mMediaUrl = AppConst.MEDIA_MP3_URL;
        mTitle = AppConst.MEDIA_MP3_TITLE;
        mImage = AppConst.MEDIA_MP3_IMAGE;
        //mPostition = AppConst.MEDIA_CURRENT_POSITION;

        //wasPlaying = AppConst.MEDIA_MP3_ISPLAY;

       fab = findViewById(R.id.button);
        //fab.setBackgroundColor(Color.TRANSPARENT);
        //fab.setAlpha(0.25f);
        //fab.setRippleColor(getResources().getColor(R.color.playButton));
        //fab.setBackgroundTintList(getResources().getColorStateList(R.color.playButton));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });

        fabExit = findViewById(R.id.buttonExit);

        //fabExit.setBackgroundColor(Color.TRANSPARENT);
        //fabExit.setAlpha(0.0f);
        fabExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.abandonAudioFocus(focusChangeListener);
                AppConst.MEDIA_CURRENT_POSITION = mediaPlayer.getCurrentPosition();
                clearMediaPlayer();
                focusChangeListener = null;
                // 롤리팝 이후 버전일 경우
                if (Build.VERSION.SDK_INT >= 21 ){
                    Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                    intent.setAction(MediaPlayerService.ACTION_PLAY);


                    startService(intent);
                }



                finish();
            }
        });


        imageView = findViewById(R.id.image);
        //Picasso.with(this).load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png").into(imageView);
        if (!"".equals(AppConst.MEDIA_MP3_IMAGE)) {
            Glide.with(this).load(mImage).into(imageView);
        } else {
            //Glide.with(this).load(AppConst.DEFAULT_IMAGE).into(imageView);
            Glide.with(this).load(R.drawable.ic_action_name).into(imageView);
        }

        txtTitle = findViewById(R.id.title);
        txtTitle.setText(mTitle);

        txtClose = findViewById(R.id.txtClose);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.abandonAudioFocus(focusChangeListener);
                AppConst.MEDIA_CURRENT_POSITION = mediaPlayer.getCurrentPosition();
                clearMediaPlayer();
                focusChangeListener = null;
                // 롤리팝 이후 버전일 경우
                if (Build.VERSION.SDK_INT >= 21 ){
                    Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
                    intent.setAction(MediaPlayerService.ACTION_PLAY);


                    startService(intent);
                }



                finish();
            }
        });
        //initPlay();
        fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_pause_white_24));
        try {
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK );
            this.mediaPlayer.setDataSource(AppConst.MEDIA_MP3_URL);
            this.mediaPlayer.prepare();

            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyApp::MyWakelockTag");
            if (!wakeLock.isHeld()) wakeLock.acquire();

            wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mylock");

            if (!wifiLock.isHeld()) wifiLock.acquire();
            //this.mediaPlayer.setOnPreparedListener(this);

            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.abandonAudioFocus(focusChangeListener);

            audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        this.mediaPlayer.seekTo(AppConst.MEDIA_CURRENT_POSITION);
        this.mediaPlayer.start();

        seekBarHint = (TextView) findViewById(R.id.textView);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(this.mediaPlayer.getDuration());
        seekBar.setProgress(AppConst.MEDIA_CURRENT_POSITION);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("AUDIOACTIVITY", "onStartTrackingTouch");
                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                //seekBarHint.setVisibility(View.VISIBLE);
                try {
                    Log.d("PREPARED3", "PROGRESS : " + progress);
                    if (null != mediaPlayer) AppConst.MEDIA_CURRENT_POSITION = mediaPlayer.getCurrentPosition();

                    int seconds = (int) (progress / 1000) % 60;
                    int minutes = (int) ((progress / (1000 * 60)) % 60);
                    int hours = (int) ((progress / (1000 * 60 * 60)) % 24);

                    String time = "";
                    if (hours > 0) {
                        time += hours + ":";
                    }

                    if (minutes > 0) {
                        if (hours > 0 && minutes < 10) {
                            time += "0" + minutes + ":";
                        } else {
                            time += minutes + ":";
                        }
                    } else {
                        time += "00:";
                    }

                    if (seconds < 10) {
                        time += "0" + seconds;
                    } else {
                        time += seconds;
                    }


                    int x = (int) Math.ceil(progress / 1000f);

                    if (x < 10)
                        seekBarHint.setText("0:0" + x);
                    else
                        seekBarHint.setText("0:" + x);

                    double percent = progress / (double) seekBar.getMax();
                    int offset = seekBar.getThumbOffset();
                    int seekWidth = seekBar.getWidth();
                    int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                    int labelWidth = seekBarHint.getWidth();
                    seekBarHint.setX(offset + seekBar.getX() + val
                            - Math.round(percent * offset)
                            - Math.round(percent * labelWidth / 2));

                    seekBarHint.setText(time + " / " + mTotalDuration);
                } catch (Exception ex) {
                }

                /*if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                    fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_play));
                    AudioActivity.this.seekBar.setProgress(0);
                }*/

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.GONE);

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        seekBarHint.setVisibility(View.GONE);




        //this.mediaPlayer.setVolume(0.5f, 0.5f);



        this.mediaPlayer.setLooping(false);
        //seekBar.setMax(this.mediaPlayer.getDuration());



        mTotalDuration = getTotalDuration();

        seekBarHint = findViewById(R.id.textView);

        new Thread(this).start();

        /*seekVolumn = findViewById(R.id.seekbar_volume);
        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int nMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int nCurrentVolumn = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekVolumn.setMax(nMax); seekVolumn.setProgress(nCurrentVolumn);
        seekVolumn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

        });*/


    }

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {


                @Override
                public void onAudioFocusChange(int focusChange) {

                    switch (focusChange) {

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                            // Lower the volume while ducking.
                            //mediaPlayer.setVolume(0.2f, 0.2f);
                            Log.d("MPAUDIOFOCUS1", "1 " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
                            if (mediaPlayer.isPlaying()) {
                                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_play_arrow_white_24));
                                mediaPlayer.pause();

                            }
                            break;
                        case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) :
                            //pause();
                            Log.d("MPAUDIOFOCUS1", "2 " + AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                            fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_play_arrow_white_48));
                            mediaPlayer.pause();

                            AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                            audioManager.abandonAudioFocus(focusChangeListener);

                            break;

                        case AudioManager.AUDIOFOCUS_LOSS :
                            /*stop();
                            ComponentName component =new ComponentName(AudioPlayerActivity.this,MediaControlReceiver.class);
                            am.unregisterMediaButtonEventReceiver(component);*/
                            Log.d("MPAUDIOFOCUS1", "3 " + AudioManager.AUDIOFOCUS_LOSS);
                            try {
                                /*if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                    fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_play_arrow_white_24));
                                    mediaPlayer.pause();

                                    audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                    audioManager.abandonAudioFocus(focusChangeListener);
                                }*/
                                if (mediaPlayer != null) {
                                    fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_play_arrow_white_24));
                                    mediaPlayer.pause();

                                    AudioManager audioManager1 = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                    audioManager1.abandonAudioFocus(focusChangeListener);

                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            /*NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(AppConst.NOTIFICATION_ID);
                            Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                            stopService(intent);

                           Intent intent1 = new Intent(getApplicationContext(), MediaPlayerService.class);
                            intent1.setAction(MediaPlayerService.ACTION_PLAY);
                            AppConst.MEDIA_MP3_ISPLAY = false;
                            startService(intent1);*/

                            break;

                        case AudioManager.AUDIOFOCUS_GAIN :
                            // Return the volume to normal and resume if paused.
                            //mediaPlayer.setVolume(1f, 1f);
                            //mediaPlayer.start();
                            Log.d("MPAUDIOFOCUS1", "4 " + AudioManager.AUDIOFOCUS_GAIN);

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
                            Log.d("MPAUDIOFOCUS1", "5 " + AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                            break;
                        default: break;
                    }
                }
            };

    public String getTotalDuration() {
        int seconds = (int) (this.mediaPlayer.getDuration() / 1000) % 60 ;
        int minutes = (int) ((this.mediaPlayer.getDuration() / (1000*60)) % 60);
        int hours   = (int) ((this.mediaPlayer.getDuration() / (1000*60*60)) % 24);

        String time = "";
        if (hours > 0) {
            time += hours + ":";
        }

        if (minutes > 0) {
            if (hours > 0 && minutes < 10) {
                time += "0" + minutes + ":";
            } else {
                time += minutes + ":";
            }
        } else {
            time += "00:";
        }

        if (seconds < 10) {
            time += "0" + seconds;
        } else {
            time += seconds;
        }

        return time;
    }

    /*@Override
    public void onPrepared(MediaPlayer player) {
        Log.d("AUDIOACTIVITY", "PREPARED  : " + this.mediaPlayer.getCurrentPosition() + "___" + this.mediaPlayer.getDuration() + "___" + AppConst.MEDIA_CURRENT_POSITION);

    }*/
/*

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d("AUDIOACTIVITY", "prepared : " + player.getCurrentPosition());
        //Stop media player here
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AppConst.NOTIFICATION_ID);
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        stopService(intent);

        player.setVolume(0.5f, 0.5f);
        player.seekTo(mPostition);
        player.start();
        Log.d("AUDIOACTIVITY", "CURPOS : " + player.getCurrentPosition());
        player.setLooping(false);
        seekBar.setMax(player.getDuration());

        new Thread(this).start();
    }
*/





    public void playSong() {
        try {

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(AppConst.NOTIFICATION_ID);

            if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
                /*clearMediaPlayer();
                seekBar.setProgress(0);
                wasPlaying = true;
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_play));*/
                this.mediaPlayer.pause();

                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_play_arrow_white_24));

            } else if (this.mediaPlayer != null && !this.mediaPlayer.isPlaying()) {
                //AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                //audioManager.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

                this.mediaPlayer.start();
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_pause_white_24));

            }


            /*if (!wasPlaying) {

                if (this.mediaPlayer == null) {
                    this.mediaPlayer = new MediaPlayer();
                }

                this.mediaPlayer.start();
                new Thread(this).start();

            }*/

            //wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void run() {

        int currentPosition = this.mediaPlayer.getCurrentPosition();
        int total = this.mediaPlayer.getDuration();


        while (this.mediaPlayer != null && this.mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = this.mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(focusChangeListener);
        AppConst.MEDIA_CURRENT_POSITION = mediaPlayer.getCurrentPosition();
        clearMediaPlayer();
        focusChangeListener = null;
        // 롤리팝 이후 버전일 경우
        if (Build.VERSION.SDK_INT >= 21 ){
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            intent.setAction(MediaPlayerService.ACTION_PLAY);


            startService(intent);
        }



        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       // if (!isExit) {
//        Log.d("PREPARED8", AppConst.MEDIA_CURRENT_POSITION + "____" +  this.mediaPlayer.getCurrentPosition());


    }

    private void clearMediaPlayer() {
        if (null != this.mediaPlayer) {

            if (null != wifiLock) wifiLock.release();
            if (!wakeLock.isHeld()) wakeLock.release();
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }
}
