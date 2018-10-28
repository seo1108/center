package yonsei_church.yonsei.center.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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

public class AudioActivity extends AppCompatActivity implements Runnable, MediaPlayer.OnPreparedListener {
    MediaPlayer mediaPlayer = new MediaPlayer();
    SeekBar seekBar;
    ImageView imageView;
    boolean wasPlaying = false;
    FloatingActionButton fab;

    private String mMediaUrl;
    private int mPostition;
    private String mTotalDuration;

    private String defaultImage = "https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);



        mMediaUrl = getIntent().getStringExtra("mediaUrl");
        //Postition = getIntent().getIntExtra("position", 0);
        mPostition = AppConst.MEDIA_CUURECT_POSITION;

        Log.d("AUDIOACTIVITY" , "position : " + mPostition + " " + mMediaUrl);
        fab = findViewById(R.id.button);
        fab.setRippleColor(getResources().getColor(R.color.playButton));
        fab.setBackgroundTintList(getResources().getColorStateList(R.color.playButton));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });

        imageView = findViewById(R.id.image);
        //Picasso.with(this).load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png").into(imageView);
        Glide.with(this).load("https://dispatch.cdnser.be/wp-content/uploads/2017/12/20171227221249_1.png").diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        //initPlay();
        fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_pause));
        try {
            this.mediaPlayer.setDataSource(mMediaUrl);
            this.mediaPlayer.prepare();
        } catch (IOException ex) {

        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AppConst.NOTIFICATION_ID);
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        stopService(intent);

        this.mediaPlayer.setVolume(0.5f, 0.5f);
        this.mediaPlayer.seekTo(mPostition);
        this.mediaPlayer.start();
        Log.d("AUDIOACTIVITY", "REVEICEPOS : " + mPostition + "CURPOS : " + this.mediaPlayer.getCurrentPosition() + "___" + this.mediaPlayer.getDuration());
        this.mediaPlayer.setLooping(false);
        //seekBar.setMax(this.mediaPlayer.getDuration());






        mTotalDuration = getTotalDuration();

        final TextView seekBarHint = findViewById(R.id.textView);

        seekBar = findViewById(R.id.seekbar);
        seekBar.setMax(this.mediaPlayer.getDuration());
        seekBar.setProgress(mPostition);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("AUDIOACTIVITY", "onStartTrackingTouch");
                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                //seekBarHint.setVisibility(View.VISIBLE);
                Log.d("AUDIOACTIVITY", "PROGRESS : " + progress);
                int seconds = (int) (progress / 1000) % 60 ;
                int minutes = (int) ((progress / (1000*60)) % 60);
                int hours   = (int) ((progress / (1000*60*60)) % 24);

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

        new Thread(this).start();
    }

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

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.d("AUDIOACTIVITY", "PREPARED  : " + this.mediaPlayer.getCurrentPosition() + "___" + this.mediaPlayer.getDuration());
    }
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

    public void initPlay() {
        fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_pause));
        try {
            this.mediaPlayer.setDataSource(mMediaUrl);
            this.mediaPlayer.prepare();
        } catch (IOException ex) {

        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(AppConst.NOTIFICATION_ID);
        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        stopService(intent);

        this.mediaPlayer.setVolume(0.5f, 0.5f);
        this.mediaPlayer.seekTo(mPostition);
        this.mediaPlayer.start();
        Log.d("AUDIOACTIVITY", "CURPOS : " + this.mediaPlayer.getCurrentPosition() + "___" + this.mediaPlayer.getDuration());
        this.mediaPlayer.setLooping(false);
        //seekBar.setMax(this.mediaPlayer.getDuration());

        new Thread(this).start();

        Log.d("AUDIOACTIVITY", "prepared0 : " + this.mediaPlayer.getCurrentPosition());
        Log.d("AUDIOACTIVITY", "prepared1 : " + this.mediaPlayer.getDuration());
    }



    public void playSong() {
        try {
            Log.d("AUDIOACTIVITY", "PLAYSONG " + this.mediaPlayer.isPlaying());
            if (this.mediaPlayer != null && this.mediaPlayer.isPlaying()) {
                /*clearMediaPlayer();
                seekBar.setProgress(0);
                wasPlaying = true;
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_play));*/
                this.mediaPlayer.pause();
                wasPlaying = true;
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_play));

            } else if (this.mediaPlayer != null && !this.mediaPlayer.isPlaying()) {
                wasPlaying = true;
                this.mediaPlayer.start();
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, android.R.drawable.ic_media_pause));

            }


            /*if (!wasPlaying) {

                if (this.mediaPlayer == null) {
                    this.mediaPlayer = new MediaPlayer();
                }

                this.mediaPlayer.start();
                new Thread(this).start();

            }*/

            wasPlaying = false;
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
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
        intent.setAction( MediaPlayerService.ACTION_SEEK_TO );
        intent.putExtra("streamLink","https://s3-ap-northeast-2.amazonaws.com/webaudio.ybstv.com/mp3/yn20181021-322.mp3");
        intent.putExtra("position",this.mediaPlayer.getCurrentPosition());
        //AppConst.MEDIA_CUURECT_POSITION = 0;
        startService(intent);

        clearMediaPlayer();
    }

    private void clearMediaPlayer() {
        if (null != this.mediaPlayer) {
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
    }
}
