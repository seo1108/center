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
import yonsei_church.yonsei.center.R;
import yonsei_church.yonsei.center.app.AppConst;
import yonsei_church.yonsei.center.media.MediaPlayerService;

public class AudioActivity extends AppCompatActivity implements Runnable {
    private boolean isPlaying = true;
    SeekBar seekBar;
    ImageView imageView;
    TextView txtTitle;
    TextView txtClose;
    TextView seekBarHint;

    ImageButton fab;
    ImageButton fabExit;

    private String mMediaUrl;
    private String mTitle;
    private String mImage;
    private String mTotalDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        mMediaUrl = getIntent().getStringExtra("mediaUrl");
        mTitle = getIntent().getStringExtra("title");
        mImage = getIntent().getStringExtra("image");

        //Postition = getIntent().getIntExtra("position", 0);

        mMediaUrl = AppConst.MEDIA_MP3_URL;
        mTitle = AppConst.MEDIA_MP3_TITLE;
        mImage = AppConst.MEDIA_MP3_IMAGE;
        fab = findViewById(R.id.button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playSong();
            }
        });

        fabExit = findViewById(R.id.buttonExit);
        fabExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imageView = findViewById(R.id.image);

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
                finish();
            }
        });

        fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_pause_white_24));

        mTotalDuration = getTotalDuration();

        seekBarHint = (TextView) findViewById(R.id.textView);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        seekBar.setMax(AppConst.MEDIA_DURATION);
        seekBar.setProgress(AppConst.MEDIA_CURRENT_POSITION);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                //seekBarHint.setVisibility(View.VISIBLE);
                try {
                    //if (null != mediaPlayer) AppConst.MEDIA_CURRENT_POSITION = mediaPlayer.getCurrentPosition();

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

                AppConst.MEDIA_SEEK_TO_POSITION = seekBar.getProgress();
                Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                intent.setAction( MediaPlayerService.ACTION_SEEK_TO);
                startService(intent);
            }
        });

        seekBarHint.setVisibility(View.GONE);

        mTotalDuration = getTotalDuration();

        seekBarHint = findViewById(R.id.textView);

        new Thread(this).start();

        Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
        intent.setAction(MediaPlayerService.ACTION_PLAY);
        startService(intent);
    }

    public String getTotalDuration() {
        int seconds = (int) (AppConst.MEDIA_DURATION / 1000) % 60 ;
        int minutes = (int) (AppConst.MEDIA_DURATION / (1000*60) % 60);
        int hours   = (int) (AppConst.MEDIA_DURATION / (1000*60*60) % 24);

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

    public void playSong() {
        Log.d("PLAYSONG", isPlaying + "");
        try {
            if (isPlaying) {
                isPlaying = false;

                Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                intent.setAction( MediaPlayerService.ACTION_PAUSE);
                startService(intent);
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_play_arrow_white_24));

            } else {
                isPlaying = true;

                Intent intent = new Intent( getApplicationContext(), MediaPlayerService.class );
                intent.setAction( MediaPlayerService.ACTION_PLAY);
                startService(intent);
                fab.setImageDrawable(ContextCompat.getDrawable(AudioActivity.this, R.drawable.baseline_pause_white_24));

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void run() {
        int currentPosition = AppConst.MEDIA_CURRENT_POSITION;
        int total = AppConst.MEDIA_DURATION;


        while (currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = AppConst.MEDIA_CURRENT_POSITION;
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
//        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        audioManager.abandonAudioFocus(focusChangeListener);
//        AppConst.MEDIA_CURRENT_POSITION = mediaPlayer.getCurrentPosition();
        //       clearMediaPlayer();

        // 롤리팝 이후 버전일 경우
        /*if (Build.VERSION.SDK_INT >= 21 ){
            Intent intent = new Intent(getApplicationContext(), MediaPlayerService.class);
            intent.setAction(MediaPlayerService.ACTION_PLAY);


            startService(intent);
        }*/



        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
